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

import java.util.concurrent.TimeoutException

import actors.ActorMqtt.CmdMqttPublish
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import models.SensorNode
import play.api.libs.concurrent.Execution.Implicits._
import play.api.{Configuration, Logger}
import play.libs.Akka
import utils.MyLogger

import scala.concurrent.Future
import scala.concurrent.duration._


object ActorSupervisor {
  val ActorName = "supervisor"

  case class CmdGetOrStart(actorName: String)

  case class CmdStop(actorName: String)

  //TODO SREI think about this. here it posts status to MQTT, in other Actors this shall return a String. Two Commands? PostStatus and GetStatus?
  case class CmdStatus()

  //TODO SREI pass a subpart of config here
  def props(config: Configuration) = Props(new ActorSupervisor(config = config))
}

/**
 * Actor that controls all the other actors (start / stop etc)
 */
class ActorSupervisor(config: Configuration) extends Actor with MyLogger {

  import actors.ActorSupervisor._

  override def preStart = {
    logger.info(s"ActorSupervisor started as '${self.path}'")
    //TODO SREI figure out, how to make sure, this actor is only started once!

    //TODO SREI I don't really like this, as it's working around the messaging. But sending a message to yourself and waiting for for yourself at this point doesn't work either.
    val dbActor = context.actorOf(Props[DbActor], DbActor.ActorName)

    if (config.getBoolean("supervisor.heartbeat.enabled").getOrElse(false)) {
      logger.info("Scheduling heartbeat...")
      Akka.system.scheduler.schedule(10.seconds, 600.seconds, self, CmdStatus)
    }
  }


  override def postStop = {
    logger.info(s"Stopping '${ActorSupervisor.ActorName}'...")
  }


  //TODO SREI split this up into different states like "ready" etc.
  override def receive: Receive = {
    case CmdGetOrStart(actorName) => {
      val sender = context.sender()
      logger.debug(s"Received command CmdGetOrStart(${actorName}) from '${sender.path}'")

      implicit val timeout = Timeout(5.seconds)
      context.actorSelection(actorName).resolveOne().onComplete {
        case scala.util.Success(actor) => {
          //actor was already started
          logger.debug(s"Actor '${actorName}' already started as '${actor.path}%s'.")
          sender ! akka.actor.Status.Success(actor)
        }
        case scala.util.Failure(ex) => {
          //actor not found, so we try to start it
          logger.info(s"Trying to start actor ${actorName}...")
          logger.debug(s"Actor ${actorName} could not be resolved. Reason: ${ex.getLocalizedMessage}")

          actorName match {
            //TODO SREI check if actorOf actually gave us a reference
            case DbActor.ActorName => {
              val actor = context.actorOf(Props[DbActor], DbActor.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case XBeeActor.ActorName => {
              //FIXME SREI errorhandling!!!
              val props = XBeeActor.props(config.getConfig("xbee").get)
              val actor = context.actorOf(props, XBeeActor.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case ActorMqtt.ActorName => {
              //FIXME SREI errorhandling!!!
              val props = ActorMqtt.props(config.getConfig("uplink.mqtt").get)
              val actor = context.actorOf(props, ActorMqtt.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case WizActor.ActorName => {
              //FIXME SREI errorhandling!!!
              val props = WizActor.props(config.getConfig("wiz").get)
              val actor = context.actorOf(props, WizActor.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case SosActor.ActorName => {
              //FIXME SREI errorhandling!!!
              //val props = SosActor.props(config.getConfig("uplink.sos").get) this is how it needs to be done!
              val actor = context.actorOf(Props[SosActor], SosActor.ActorName)
              actor ! SosActorCommand(1, "config")
              sender ! akka.actor.Status.Success(actor)
            }
            case ActorTarom.ActorName => {
              //FIXME SREI errorhandling!!!
              val props = ActorTarom.props(config.getConfig("tarom").get)
              val actor = context.actorOf(props, ActorTarom.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case ActorSontekIq.ActorName => {
              //FIXME SREI errorhandling!!!
              val props = ActorSontekIq.props(config.getConfig("sontekIq").get)
              val actor = context.actorOf(props, ActorSontekIq.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case ProcessExecActor.ActorName => {
              //FIXME SREI errorhandling!!!
              val actor = context.actorOf(Props[ProcessExecActor], ProcessExecActor.ActorName)
              sender ! akka.actor.Status.Success(actor)
            }
            case default => {
              logger.error(s"Could not start '${actorName}': Unkown Actor.")
              sender ! akka.actor.Status.Failure(ex) //TODO SREI not sure if this is the correct exception, but anyways, I'm lazy :-)
            }
          }
        }
      }
    } //CmdStart
    case CmdStop(actorName) => {
      val sender = context.sender()
      //TODO SREI implement stopping / restarting of actors
      actorName match {
        case default => {
          logger.info(f"Received CmdStop('${default}%s') from '${sender}%s'")
        }
      }
    } //CmdStop()
    case CmdStatus => {
      logger.info("Received CmdStatus")
      implicit val timeout = Timeout(100.milliseconds)
      //TODO make this (and all the actors) return an "ActorStatus" thingy so we can use this so show on a web page.
      var futureList = List[Future[(String, String)]]()
      context.children.foreach((actorRef) => {
        futureList ::= (actorRef ? CmdStatus)
          .map( (s: Any) => (actorRef.path.name, s.toString()) )
          .recover({ case ex:TimeoutException => (actorRef.path.name, ex.getLocalizedMessage) })
      })
      Future.sequence(futureList).map( _.map(_.productIterator.toList.mkString(":\n\t")).mkString("\n") ).andThen({
        case scala.util.Success(statusString) => {
          logger.info(s"$statusString - ${System.currentTimeMillis / 1000}")
        }
        case scala.util.Failure(ex) => {
          logger.warn("Something went wrong.", ex)
        }
      })
    } //CmdStatus
    case default => {
      val sender = context.sender()
      logger.warn(f"Received unknown message '${default}%s' from '${sender}%s'")
    }
  }

}
