/*
 * Copyright 2015 Smart Aquifer Characterisation (SAC) Programme (http://www.gns.cri.nz/Home/Our-Science/Environment-and-Materials/Groundwater/Research-Programmes/SMART-Aquifer-Characterisation)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package actors

import java.net.InetSocketAddress
import java.util.UUID

import actors.ActorMqtt.{CmdConnect, CmdMqttPublish}
import actors.ActorSupervisor.CmdStatus
import actors.MqttGetCapabilitiesResp
import actors.WizActor.SetMeasurementFrequency
import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import net.sigusr.mqtt.api._
import play.api.Configuration
import play.api.libs.json.Json
import utils.MyLogger

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import play.libs.Akka

import scala.collection.mutable
import scala.util.{Failure, Success}

/**
 * These traits / objects hold the prefixes for different services
 */
trait MqttPrefix {
  def topic: String

  override def toString: String = topic
}

sealed trait MqttPrefixes extends MqttPrefix

object SpsAll extends MqttPrefixes {
  val topic = "sps/+"
}

object SpsGetCapabilities extends MqttPrefixes {
  val topic = "sps/getCapabilities"
}

object SpsDescribeTask extends MqttPrefixes {
  val topic = "sps/describeTask"
}

object SpsSubmitTask extends MqttPrefixes {
  val topic = "sps/submitTask"
}

object SensorwebEventAll extends MqttPrefixes {
  val topic = "event/+"
}

object SensorwebEventStatus extends MqttPrefixes {
  val topic = "event/status"
}

object SensorwebObservations extends MqttPrefixes {
  val topic = "observations"
}


object ActorMqtt {
  val ActorName = "mqtt"

  case class CmdMqttPublish(msgType: MqttPrefix, topic: String, body: String, retain: Boolean)

  case class CmdConnect()

  def props(config: Configuration) = Props(new ActorMqtt(config = config))
}

/**
 * Provides a connection to an MQTT broker
 */
class ActorMqtt(config: Configuration) extends Actor with MyLogger {
  lazy val wizActorSel: ActorSelection =context.system.actorSelection(s"/user/${ActorSupervisor.ActorName}/${WizActor.ActorName}")
  lazy val procexecActorSel: ActorSelection = context.system.actorSelection(s"/user/${ActorSupervisor.ActorName }/${ProcessExecActor.ActorName }")

  //read config
  val host: String = config.getString("host").getOrElse("localhost")
  val port: Int = config.getInt("port").getOrElse(1883)
  val clientId: String = config.getString("clientid").getOrElse("gateway")
  val user: Option[String] = config.getString("username")
  val password: Option[String] = config.getString("password")
  val topicSubscribePrefix: mutable.Buffer[String] = config.getStringList("topic.prefix.subscribe").get.asScala
  val topicPublishPrefix: String = config.getString("topic.prefix.publish").getOrElse("sensorweb/test")
  val connectRetryTimeout: Int = config.getInt("connectRetryTimeout").getOrElse(60)
  lazy val managerRef: ActorRef = context.actorOf(Manager.props(new InetSocketAddress(host, port)))

  private var lastMessageId = 1

  def nextMessageId: Int = {
    lastMessageId = if (lastMessageId >= 65535)
      1
    else
      lastMessageId + 1
    lastMessageId
  }

  override def preStart() {
    logger.info("Starting MqttActor")
    connect()
  }

  private def connect(): Unit = {
    logger.debug(s"Started MqttManager as $managerRef")

    managerRef ! Connect(
      clientId = clientId,
      user = user,
      password = password,
      cleanSession = false,
      will = Option(Will(
        retain = false,
        qos = AtLeastOnce,
        topic = s"${SensorwebEventStatus.topic}/$topicPublishPrefix/lastwill",
        message = s"I disconnected disgracefully.")
      )
    )
  }

  /**
   * Default Actor behaviour
   * @see akka.actor.Actor.receive
   */
  override def receive: Receive = {
    case CmdStatus => {
      logger.info("Received CmdStatus")
      val status = s"${ActorMqtt.ActorName}: running as ${self.path}.\nNot connected."
      sender() ! scala.util.Success(status)
    }

    case CmdConnect => {
      logger.info("Received CmdConnect")
      connect()
    }

    case Connected =>
      logger.info(s"Successfully connected to $host:$port")

      //SREI ok, a little bit of magic. Take the list, make to vector and zip it with a new vector filled with AtMostOnce
      //TODO SREI we need to think about how to configure this in a more flexible way. At the moment this works, as we're only interested in SPS-Commands
      val zippedVector = topicSubscribePrefix.map((x) => {
        s"${SpsAll.topic}/${x}/#" //we only subscribe to SPS stuff
      }).toVector.zip(Vector.fill(topicSubscribePrefix.length)({
        AtMostOnce
      }))
      logger.debug(zippedVector.mkString(",\n"))
      sender() ! Subscribe(zippedVector, 1)
      context.become(ready(sender()))

    case ConnectionFailure(reason) =>
      import context.system
      import scala.concurrent.ExecutionContext.Implicits.global
      logger.error(s"Connection to localhost:1883 failed [$reason]")
      logger.error(s"Will try again in $connectRetryTimeout seconds.")
      system.scheduler.scheduleOnce(connectRetryTimeout.seconds, self, CmdConnect)
  }

  /**
   * Once the Actor is Connected we enter the "ready" state
   * @param mqttManager
   * @return
   */
  def ready(mqttManager: ActorRef): Receive = {
    case CmdStatus => {
      logger.info("Received CmdStatus")
      val status = s"${ActorMqtt.ActorName}: running as ${self.path}.\nConnected via ${mqttManager.path}"
      sender() ! scala.util.Success(status)
    }
    case Subscribed(vQoS, MessageId(1)) => {
      logger.info(s"Successfully subscribed to ${topicSubscribePrefix}")
    }
    case Published(messageId) => {
      //TODO SREI implement resent and stuff, in case a message could not be published.
      logger.debug(s"Message with ID ${messageId.identifier} has been published.")
    }

    //let's implement the shit out of it
    // AKMO missing "sections" list GetCapabilities (even empty req) requests should possible, then return full Capa as default
      /*
      tested with:
      mosquitto_pub -u mobile -P mobile2014 -t "sps/getCapabilities/sensorweb/admin/outbox/gateway0013A20040BA23BE" -m "{ \"messageUUID\" : \"f6fd0652-7a1a-4309-82bd-0f3f8d117a12\"}

      mosquitto_pub -u mobile -P mobile2014 -t "sps/getCapabilities/sensorweb/admin/outbox/gateway0013A20040BA23BE" -m "{ }"
       */
    case Message(topic, payload) => {
      val message = new String(payload.to[Array], "UTF-8")
      topic.split("/").take(2).mkString("/") match {
        //TODO SREI create apply unapply in SpsGetCapa etc
        // TODO also make Capabilities more comprehensive, ServiceIdentification should for example include something like the nodeequivalent IDs?
        // TODO or make a similar approach for SOS (actually the better way to do it), SOS describeSensor could then list stuff from the Database nodes and types etc
        case SpsGetCapabilities.topic => {
          logger.debug("Preparing getCapabilities response.")
          // create a JsValue from Request
          val request = MqttGetCapabilitiesReq.fromJsValue(Json.parse(message))
          val reply = if (request.sections.isDefined) {
            val theSections = request.sections.getOrElse(List("serviceIdentification", "operations","tasks"))
            new MqttGetCapabilitiesResp(
              messageUUID = Some(UUID.randomUUID()),
              serviceIdentification =
                (if (theSections.contains("serviceIdentification"))
                  Some("Scala Sensorweb Gateway Version -0.0.1 pre-alpha")
                else
                  None
                  ),
              operations = (if (theSections.contains("operations"))
                Some(List("getCapabilities", "describeTask" , "submitTask"))
              else
                None
                ),
              tasks = (if (theSections.contains("tasks"))
                Some(List("setWizMode","ps","df","free","reboot"))
              else None
                )
            )
          } else {
            new MqttGetCapabilitiesResp(
              messageUUID = Some(UUID.randomUUID()),
              serviceIdentification = Some("Scala Sensorweb Gateway Version -0.0.1 pre-alpha"),
              operations = Some(List("getCapabilities", "describeTask" , "submitTask")),
              tasks = Some(List("setWizMode","ps","df","free","reboot"))
            )
          }

          //create reply message

          logger.debug(s"request ${Json.prettyPrint(request.asJson)}")
          logger.debug(s"reply ${Json.prettyPrint(reply.asJson)}")

          self ! CmdMqttPublish(msgType = SpsGetCapabilities,
            topic = request.messageUUID.map(_.toString()).getOrElse("public"),
            body = Json.stringify(reply.asJson),
            retain = false)
        } // GetCapabilities
        case SpsDescribeTask.topic => {
          logger.debug("Preparing describeTask response")
          val request = MqttDescribeTaskReq.fromJsValue(Json.parse(message))
          val (descr, params) = request.task match {
            case "setWizMode" => ("Sets the measurement frequency for WIZ. Mode can either be.",
              List[String]("mode - can either be 'high' or 'low' or 'off'"))
            case "ps" => {
              ("retrieves a ps -ef shell cmd",
                List[String]())
            }
            case "df" => {
              ("issues a df -h shell cmd",
                List[String]())
            }
            case "free" => {
              ("issues a free -h shell cmd",
                List[String]())
            }
            case "reboot" => {
              ("not implemented",
                List[String]())
            }
            case "fetchremotecmd" => {
              ("not implemented",
                List[String]("cmdname - remote cmd name, location is predefined on SMART server"))
            }
            case _ => ("Error: Unkown Task", List[String]())
          }
          val reply = new MqttDescribeTaskResp(
            messageUUID = Some(UUID.randomUUID()),
            task = request.task,
            description = descr,
            parameter = params
          )

          logger.debug(s"request ${Json.prettyPrint(request.asJson)}")
          logger.debug(s"reply ${Json.prettyPrint(reply.asJson)}")
          self ! CmdMqttPublish(msgType = SpsDescribeTask,
            topic = request.messageUUID.map(_.toString()).getOrElse("public"),
            body = Json.stringify(reply.asJson),
            retain = false)
        } //DescribeTask
        case SpsSubmitTask.topic => {
          logger.debug("Preparing submitTask response")
          val request = MqttSubmitTaskReq.fromJsValue(Json.parse(message))
          logger.debug(s"request ${Json.prettyPrint(request.asJson)}")

          // this is tightly coupled with unittest logic on Jenkins
          /*
tested with:
mosquitto_pub -u mobile -P mobile2014 -t "sps/submitTask/sensorweb/admin/outbox/gateway0013A20040BA23BE" -m "{ \"messageUUID\" : \"f6fd0652-7a1a-4309-82bd-7f3f8d117a12\", \"task\" : \"setWizMode\" , \"parameter\" : { \"mode\" : \"FAILED\"  }}"
mosquitto_pub -u mobile -P mobile2014 -t "sps/submitTask/sensorweb/admin/outbox/gateway0013A20040BA23BE" -m "{ \"messageUUID\" : \"f6fd0652-7a1a-4309-82bd-7f3f8d117a12\", \"task\" : \"setWizMode\" , \"parameter\" : { \"mode\" : \"SUCCESS\"  }}"
mosquitto_pub -u mobile -P mobile2014 -t "sps/submitTask/sensorweb/admin/outbox/gateway0013A20040BA23BE" -m "{ \"messageUUID\" : \"f6fd0652-7a1a-4309-82bd-7f3f8d117a12\", \"task\" : \"setWizMode\" , \"parameter\" : { \"mode\" : \"NOTHANDLED\"  }}"
 */
          val (status, params) = request.task match {
            case "setWizMode" => {
              logger.info("Setting WIZ mode...")
              if (request.parameter.isEmpty) {
                ("Error: request.parameter.isEmpty", Map[String, String]())
              } else {
                if (!request.parameter.contains("mode")) {
                  ("Error: mode parameter not available", Map[String, String]())
                } else {
                  if (request.parameter.get("mode").isDefined && request.parameter.get("mode").isDefined) {
                    val modeValue = request.parameter.get("mode").get
                    modeValue match {
                      case "SUCCESS" => {
                        logger.info("Setting LOW measurement frequency.")
                        import scala.concurrent._
                        import play.api.libs.concurrent.Execution.Implicits._
                        implicit val _timeout = Timeout(10.seconds)

                        wizActorSel.resolveOne.onComplete {
                          case Success(actor) => actor ! SetMeasurementFrequency(low = true)
                          case Failure(ex) => logger.warn("No active WIZ actor", ex)
                        }
                        ("OK: low frequency", Map[String, String]())
                      }
                      case "FAILURE" => {
                        logger.info("Setting HIGH measurement frequency.")
                        import scala.concurrent._
                        import play.api.libs.concurrent.Execution.Implicits._
                        implicit val _timeout = Timeout(10.seconds)

                        wizActorSel.resolveOne.onComplete {
                          case Success(actor) => actor ! SetMeasurementFrequency(low = false)
                          case Failure(ex) => logger.warn("No active WIZ actor", ex)
                        }
                        ("OK: high frequency", Map[String, String]())
                      }
                      case _ => {
                        logger.warn(s"not handled... ${modeValue}")
                        ("Warn: not handled", Map[String, String]())
                      }
                    }
                  } else {
                    ("Error: mode value not available", Map[String, String]())
                  }
                }
              }
            }
            case "ps" => {

              import scala.concurrent._
              import play.api.libs.concurrent.Execution.Implicits._
              implicit val _timeout = Timeout(10.seconds)

              logger.info("issueing ps -ef ...")
              val stdoutFuture = procexecActorSel ? "ps" andThen {
                case scala.util.Success(result) => result
                case scala.util.Failure(ex) => s"Could not ask ${ProcessExecActor.ActorName}}, ${ex.getLocalizedMessage}"
              }
              val stdout = Await.result(stdoutFuture, 10.seconds).asInstanceOf[String]
              (stdout, Map[String, String]())
            }
            case "df" => {

              import scala.concurrent._
              import play.api.libs.concurrent.Execution.Implicits._
              implicit val _timeout = Timeout(10.seconds)

              logger.info("issueing df -h ...")
              val stdoutFuture = procexecActorSel ? "df" andThen {
                case scala.util.Success(result) => result
                case scala.util.Failure(ex) => s"Could not ask ${ProcessExecActor.ActorName}}, ${ex.getLocalizedMessage}"
              }
              val stdout = Await.result(stdoutFuture, 10.seconds).asInstanceOf[String]
              (stdout, Map[String, String]())
            }
            case "free" => {

              import scala.concurrent._
              import play.api.libs.concurrent.Execution.Implicits._
              implicit val _timeout = Timeout(10.seconds)

              logger.info("issueing free -h ...")
              val stdoutFuture = procexecActorSel ? "free" andThen {
                case scala.util.Success(result) => result
                case scala.util.Failure(ex) => s"Could not ask ${ProcessExecActor.ActorName}}, ${ex.getLocalizedMessage}"
              }
              val stdout = Await.result(stdoutFuture, 10.seconds).asInstanceOf[String]
              (stdout, Map[String, String]())
            }
            case "reboot" => {

              import scala.concurrent._
              import play.api.libs.concurrent.Execution.Implicits._
              implicit val _timeout = Timeout(10.seconds)

              logger.info("issuing reboot ...")
              val stdoutFuture = procexecActorSel ? "reboot" andThen {
                case scala.util.Success(result) => result
                case scala.util.Failure(ex) => s"Could not ask ${ProcessExecActor.ActorName}}, ${ex.getLocalizedMessage}"
              }
              val stdout = Await.result(stdoutFuture, 10.seconds).asInstanceOf[String]
              (stdout, Map[String, String]())
            }
            case "fetchremotecmd" => {

              import scala.concurrent._
              import play.api.libs.concurrent.Execution.Implicits._
              implicit val _timeout = Timeout(10.seconds)

              logger.info("issuing fetchremotecmd ...")
              val stdoutFuture = procexecActorSel ? "fetchremotecmd" andThen {
                case scala.util.Success(result) => result
                case scala.util.Failure(ex) => s"Could not ask ${ProcessExecActor.ActorName}}, ${ex.getLocalizedMessage}"
              }
              val stdout = Await.result(stdoutFuture, 10.seconds).asInstanceOf[String]
              (stdout, Map[String, String]())
            }
            case _ => {
              ("Error: Unkown Task", Map[String, String]())
            }
          }

          val reply = new MqttSubmitTaskResp(
            messageUUID = Some(UUID.randomUUID()),
            task = request.task,
            parameter = params,
            status = status
          )

          // TODO AKMO here could be UpdateSENSORML based on tracking the status change

          logger.debug(s"reply ${Json.prettyPrint(reply.asJson)}")

          self ! CmdMqttPublish(msgType = SpsSubmitTask,
            topic = request.messageUUID.map(_.toString()).getOrElse("public"),
            body = Json.stringify(reply.asJson),
            retain = false)

          //to simulate something long running, we first reply with running and then with success
//          import play.api.Play.current
//          import play.api.libs.concurrent.Execution.Implicits._
//          Akka.system.scheduler.scheduleOnce(30.seconds,
//            self,
//            CmdMqttPublish(msgType = SpsGetCapabilities,
//              topic = request.messageUUID.map(_.toString()).getOrElse("public"),
//              body = Json.stringify(replySuccess.asJson),
//              retain = false)
//          )
        } //SpsSubmitTask
        case _ => {
          logger.debug(s"Message of unknown topic received: [$topic] $message")
        }
      }
    }
    case CmdMqttPublish(msgType, topic, body, retain) => {
      logger.debug(s"Publishing [${msgType.topic}/${topicPublishPrefix}/${topic}] ${body}")
      mqttManager ! Publish(
        topic = s"${msgType.topic}/${topicPublishPrefix}/${topic}",
        payload = body.getBytes("UTF-8").to[Vector],
        qos = AtLeastOnce,
        messageId = Option(MessageId(this.nextMessageId)),
        retain = retain
      )
    }
  }

  /*
    def disconnecting(): Receive = {
      case Disconnected ⇒
        println("Disconnected from localhost:1883")
        ActorMqtt.shutdown()
    }
  */

}
