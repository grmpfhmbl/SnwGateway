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

import java.lang
import java.time.{ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter

import actors.ActorMqtt.CmdMqttPublish
import akka.actor.{Actor, ActorSelection}
import akka.pattern.ask
import models._
import models.sos._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws._
import play.libs.Akka
import utils.MyLogger

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.util.matching.Regex

object SosActor {
  /** the internal name of the actor */
  val ActorName = "sos"
}

class SosActor extends Actor with MyLogger {

  lazy val dbActorSel: ActorSelection = context.system.actorSelection(
    s"/user/${ActorSupervisor.ActorName}/${DbActor.ActorName}")
  lazy val mqttActorSel: ActorSelection = context.system.actorSelection(
    s"/user/${ActorSupervisor.ActorName}/${ActorMqtt.ActorName}")

  lazy val UPLINK_SOS_PUBLISH_VIA_MQTT: lang.Boolean = play.Play.application.configuration.getBoolean(
    "sensorweb.uplink.sos.publishViaMqtt")

  lazy val UPLINK_SOS_URL: String = play.Play.application.configuration.getString("sensorweb.uplink.sos.url")
  lazy val UPLINK_SOS_TIMEOUT: Int = play.Play.application.configuration.getString("sensorweb.uplink.sos.timeout").toInt

  // nodeequivalent also used as api key to allow per station in SOS admin to recognise
  lazy val UPLINK_SOS_NODE_EQUIVALENT: String = play.Play.application.configuration.getString(
    "sensorweb.uplink.sos.node_equivalent")
  lazy val UPLINK_SOS_SECURITY_TOKEN: String = play.Play.application.configuration.getString(
    "sensorweb.uplink.sos.securitytoken")

  lazy val UPLINK_SOS_BATCH_SIZE: Int = play.Play.application.configuration.getInt(
    "sensorweb.uplink.sos.uploadBatchSize")

  lazy val VOCAB_NETWORK_IDENTIFIER: String = play.Play.application.configuration.getString(
    "sensorweb.vocab.network.identifier")
  lazy val VOCAB_PREFIX_PROCEDURE: String = play.Play.application.configuration.getString(
    "sensorweb.vocab.prefix.procedure")
  lazy val VOCAB_PREFIX_OFFERING: String = play.Play.application.configuration.getString(
    "sensorweb.vocab.prefix.offering")
  lazy val VOCAB_PREFIX_FEATURE: String = play.Play.application.configuration.getString(
    "sensorweb.vocab.prefix.feature")
  lazy val VOCAB_PREFIX_PHENOMENON: String = play.Play.application.configuration.getString(
    "sensorweb.vocab.prefix.phenomenon")

  val UPLINK_SOS_UPLOAD_SCHEDULE_TIMEOUT: Int = play.Play.application.configuration.getInt(
    "sensorweb.uplink.sos.uploadScheduleTimeout")
  val UPLINK_SOS_UPLOAD_FAILED_SCHEDULE_TIMEOUT: Int = play.Play.application.configuration.getInt(
    "sensorweb.uplink.sos.uploadFailedScheduleTimeout")

  var sosDcpKvp = ""
  var sosDcpPox = ""
  var nodeEquivalentID: Long = 0

  // TODO sort of hardcoded
  val WizSosPattern: Regex =
    """(updatewizsml)<>(http.*)<>(HIGH|LOW)""".r

  override def preStart(): Unit = {
    logger.info(s"Starting SosActor for $UPLINK_SOS_URL")

    val urlmatcher = new scala.util.matching.Regex("(http://.*)\\?.*", "baseurl")
    val urlmatcher(baseurl) = UPLINK_SOS_URL
    sosDcpKvp = baseurl
    sosDcpPox = baseurl.replace("/kvp", "/pox")

    logger.info("Finding SOS-UPLINK node in Database ID " + UPLINK_SOS_NODE_EQUIVALENT)
    nodeEquivalentID = SensorNode.getSensorNodeByExtendedAddress(UPLINK_SOS_NODE_EQUIVALENT).head.idsensornode

    logger.info(s"start sos actor now with NODE_EQUIVALENT $UPLINK_SOS_NODE_EQUIVALENT = $nodeEquivalentID")
    logger.info(s"start sos actor now with $sosDcpKvp with sosDcpKvp")
    logger.info(s"start sos actor now with $sosDcpPox with sosDcpPox")
  }

  def receive: PartialFunction[Any, Unit] = {

    case SosActorCommand(fieldid, value) => {

      value match {
        case "config" => {
          logger.info("received 'config' command")
          if (UPLINK_SOS_PUBLISH_VIA_MQTT) {
            logger.info("Configured uploader: publishing Observations via MQTT")
            logger.info(s"Starting upload schedulers... new every $UPLINK_SOS_UPLOAD_SCHEDULE_TIMEOUT minutes," +
              s"failed will retry every $UPLINK_SOS_UPLOAD_FAILED_SCHEDULE_TIMEOUT minutes")
            import context.system
            val cancelSosUploadStart = system.scheduler.schedule(30.seconds, UPLINK_SOS_UPLOAD_SCHEDULE_TIMEOUT.minutes, self,
              SosActorCommand(1, "bulkupload"))
            val cancelSosRedeemStart = system.scheduler.schedule(90.seconds, UPLINK_SOS_UPLOAD_FAILED_SCHEDULE_TIMEOUT.minutes, self,
              SosActorCommand(1, "redeemupload"))
          }
          else {
            logger.info("Configured uploader: Publishing Observations directly into SOS")
            logger.info(s"Checking Capabilities of $UPLINK_SOS_URL")

            val getCapaFuture = checkCapabilities(sosDcpKvp)

            getCapaFuture.map {
              returnVal =>
                returnVal match {
                  case 0 => {
                    logger.info(s"SosActor.config.getCapaFuture returned nicely $returnVal")
                    logger.info("preparing sosSystemSetup...")

                    val setupFuture = scala.concurrent.Future {
                      sosSystemSetup()
                    }
                    setupFuture.onComplete {
                      case Success(_) => {
                        import context.system
                        logger.info("SosActor.setupSos overall initial setupSos.")
                        logger.info(s"config message: starting upload schedule in 30 sec upload every $UPLINK_SOS_UPLOAD_SCHEDULE_TIMEOUT mins.")
                        val cancelSosUploadStart = system.scheduler.schedule(30.seconds, UPLINK_SOS_UPLOAD_SCHEDULE_TIMEOUT.minutes, self,
                          SosActorCommand(1, "bulkupload"))
                        logger.info(s"config message: starting upload failed schedule in 90 sec upload every $UPLINK_SOS_UPLOAD_FAILED_SCHEDULE_TIMEOUT mins.")
                        val cancelSosRedeemStart = system.scheduler.schedule(90.seconds, UPLINK_SOS_UPLOAD_FAILED_SCHEDULE_TIMEOUT.minutes, self,
                          SosActorCommand(1, "redeemupload"))
                      }
                      case Failure(ex) =>
                        logger.warn("Exception on 'config'", ex)
                    }
                  }
                  case _ =>
                    logger.warn("SosActor.config.getCapaFuture returned, but bad exit code " + returnVal)
                }
            }
            getCapaFuture.recover {
              case e: Exception => {
                val message = s"${e.getClass.getCanonicalName} when starting SOS actor: ${e.getMessage}. Trying again in 10 minutes."
                logger.warn(message, e)
                Akka.system().scheduler.scheduleOnce(10.minutes, self, SosActorCommand(1, "config"))
              }
            }
          }
        }
        case "bulkupload" => {
          logger.info("Uploading new observations...")
          // fire and forget :-p
          val uploadFuture = scala.concurrent.Future {
            upload(fetchFailedObservations = false, UPLINK_SOS_BATCH_SIZE)
          }
          // we could still map through those ... but they are handled further down the stream,
          // and each web call has the default timeout, we shouldn't set an overall but rather let it stream dripple
        }

        case "redeemupload" => {
          logger.info("Uploading previously failed observations...")
          // fire and forget :-p
          val uploadFuture = scala.concurrent.Future {
            upload(fetchFailedObservations = true, UPLINK_SOS_BATCH_SIZE)
          }
          // we could still map through those ... but they are handled further down the stream,
          // and each web call has the default timeout, we shouldn't set an overall but rather let it stream dripple
        }

        case "bulkuploadtext" => {
          logger.info("Uploading new text observations...")

          // fire and forget :-p
          //TODO rename newOrFailed to "fetchFailedObservations" and invert true/false
          val uploadFuture = scala.concurrent.Future {
            uploadTextObservations(newOrFailed = true, UPLINK_SOS_BATCH_SIZE)
          }
          // we could still map through those ... but they are handled further down the stream,
          // and each web call has the default timeout, we shouldn't set an overall but rather let it stream dripple
        }

        case "redeemuploadtext" => {
          logger.info("Uploading previously failed text observations...")

          // fire and forget :-p
          val uploadFuture = scala.concurrent.Future {
            uploadTextObservations(newOrFailed = false, UPLINK_SOS_BATCH_SIZE)
          }
          // we could still map through those ... but they are handled further down the stream,
          // and each web call has the default timeout, we shouldn't set an overall but rather let it stream dripple
        }

        case "checkcapa" => {
          if (!UPLINK_SOS_PUBLISH_VIA_MQTT) {
            logger.info(s"checkcapa against $UPLINK_SOS_URL")
            val getCapaFuture = checkCapabilities(UPLINK_SOS_URL)

            val capaTimeoutFuture = play.api.libs.concurrent.Promise.timeout("overall getcapa: timeout / exception",
              (UPLINK_SOS_TIMEOUT * 10).millisecond)
            Future.firstCompletedOf(Seq(getCapaFuture, capaTimeoutFuture)).map {
              case i: Int =>
                logger.info(s"overall getcapa return code: $i")
              case t: String =>
                logger.info(s"GetCapa return: $t")
            }
          }
        }

        // FIXME sort of hardcoded
        case WizSosPattern(wiz, sensorURI, smlCharacteristic) => {
          logger.info(s"trying update sensorml for $sensorURI to $smlCharacteristic")
          val uploadFuture = scala.concurrent.Future {
            updateSensorMeasureModeCharacteristic(sensorURI, smlCharacteristic)
          }
        }

        case "stop" => {
          val name = self.path.toString
          logger.info(s"shutting myself down: $name ")
          context.stop(self)
        }
        case _ => {
          logger.error(s"sos actor got unknown message $value")
        }
      }
    }
  }

  /**
    * upload observations either to SOS or MQTT
    * depends on config setting UPLINK_SOS_PUBLISH_VIA_MQTT
    *
    * @param fetchFailedObservations
    * @param maxNum
    */
  def upload(fetchFailedObservations: Boolean, maxNum: Long): Unit = {
    // val updateListMeas = SensorMeasurement.getAllNewSosForUpload
    //FIXME implement something like "still uploading" to prevent from multiple uploads interfering.
    val updateListMeas = SensorMeasurement.getSelectForSosUpload(fetchFailedObservations, maxNum)
    val obsToUpload = updateListMeas.length

    logger.info(s"Fetching ${
      if (fetchFailedObservations) "failed"
      else "new"
    } observations (max: $maxNum). - $obsToUpload found for upload")

    val nodesList = for {
      sensorNode <- SensorNode.getAllWithParser
    } yield (sensorNode.idsensornode, sensorNode)

    val nodesMap = nodesList.toMap

    val typesList = for {
      sensorType <- SensorType.getAllWithParser
    } yield (sensorType.idsensortype, sensorType)

    val typesMap = typesList.toMap

    //    var returnCode = 0

    for (obs <- updateListMeas) {
      // templates for string-based XML generation reside under models.sos
      logger.debug(s"Uploading measurement ID ${obs.idsensormeasurement}")

      if (UPLINK_SOS_PUBLISH_VIA_MQTT)
        publishObservationToMqtt(obs, nodesMap, typesMap)
      else {
        logger.debug(
          s"we assume all procedureIDs are well-known and exist in SOS (because checked/refreshed at startup and on Node/type editing (to be implemented :-p))")
        publishObservationToSos(obs, nodesMap, typesMap)
      }
    }
  }

  /**
    * publishes an observation directly to SOS
    *
    * @param nodesMap
    * @param typesMap
    * @param obs
    * @return
    */
  private def publishObservationToSos(obs: SensorMeasurement, nodesMap: Map[Long, SensorNode],
                                      typesMap: Map[Long, SensorType]): Unit = {
    val nodeID = nodesMap.apply(obs.sensornodes_idsensornode)
    val typeID = typesMap.apply(obs.sensortypes_idsensortype)
    val phenTime = obs.meastime
    val uomCode = typeID.unit
    val value = obs.calcvalue
    val position: Array[lang.Double] = Array(nodeID.latitude, nodeID.longitude, nodeID.altitude)

    val offeringURI = VOCAB_PREFIX_OFFERING + "/" + VOCAB_NETWORK_IDENTIFIER + "/" + urlify(
      nodeID.name) + "/" + "p" + urlify(typeID.sensid + "_" + typeID.phenomenon)
    val sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + VOCAB_NETWORK_IDENTIFIER + "/" + urlify(
      nodeID.name) + "/" + "p" + urlify(typeID.sensid + "_" + typeID.phenomenon)
    val phenomenonURI = VOCAB_PREFIX_PHENOMENON + "/" + urlify(typeID.phenomenon)
    val featureURI = VOCAB_PREFIX_FEATURE + "/" + urlify(nodeID.name)
    val obsType = "MEASUREMENT"
    val codeSpace = "http://zgis.at"
    val sampledFeatureURI = "http://sweet.jpl.nasa.gov/2.3/realm.owl#Atmosphere"

    val myOmXml: ObservationDescription = new ObservationDescription(phenTime, offeringURI,
      sensorURI, phenomenonURI, uomCode,
      value, featureURI, position, obsType,
      codeSpace, sampledFeatureURI)

    val insertObsFuture = insertObservation(myOmXml, offeringURI, obs.idsensormeasurement)

    insertObsFuture.map {
      returnCode =>
        returnCode match {
          case 0 => {
            logger.debug(s"InsertObservation returned: OK ($returnCode) for ObservationID: ${obs.idsensormeasurement}")
            SensorMeasurement.updateSosState(obs.idsensormeasurement, true, returnCode)
          }

          case _ => {
            logger.warn(s"InsertObservation returned: ERROR ($returnCode) for ObservationID: ${obs.idsensormeasurement}")

            val existsFuture = existsSensor(sensorURI)

            existsFuture.map {
              returnCode =>
                returnCode match {
                  case 1 => {
                    logger.warn("Sensor does not exists -> inserting")
                    val placement: Array[lang.Double] = Array(nodeID.latitude, nodeID.longitude, nodeID.altitude)
                    val obsProp = urlify(typeID.phenomenon)
                    val platformName = urlify(nodeID.name)
                    val childSensorName = "p" + urlify(typeID.sensid + "_" + typeID.phenomenon)
                    val insertChildSensorFuture = insertChildSensor(childSensorName, platformName,
                      VOCAB_NETWORK_IDENTIFIER, obsProp, uomCode, placement, obsType, codeSpace)

                    insertChildSensorFuture.map {
                      returnCode =>
                        returnCode match {
                          case 0 => {
                            logger.debug("Child sensor inserted... continuing")

                            insertObservation(myOmXml, offeringURI, obs.idsensormeasurement).map { returnCode =>
                              returnCode match {
                                case 0 => {
                                  logger.debug("Insert observation succeeded.")
                                  SensorMeasurement.updateSosState(obs.idsensormeasurement, true, returnCode)
                                }
                                case _ => {
                                  logger.warn(s"Could not insert observation after inserting child sensor '${nodeID.name}:${nodeID.extendedaddress}'")
                                  SensorMeasurement.updateSosState(obs.idsensormeasurement, false, returnCode)
                                }
                              }
                            }
                          }
                          case _ => {
                            logger.warn(s"Could not insert child sensor ${nodeID.name}:${nodeID.extendedaddress}")
                            SensorMeasurement.updateSosState(obs.idsensormeasurement, false, returnCode)
                          }
                        }
                    }
                  }
                  case _ =>
                    logger.warn(s"Error while inserting observation ${obs.idsensormeasurement} into SOS $returnCode.")
                    SensorMeasurement.updateSosState(obs.idsensormeasurement, false, returnCode)
                }
            }
          }
        }
    }
  }

  /**
    * Publishes a SensorObservation to MQTT. This can't guarantee that the Observation will be read by anyone!
    * @param observation
    * @param nodesMap
    * @param typesMap
    */
  private def publishObservationToMqtt(observation: SensorMeasurement, nodesMap: Map[Long, SensorNode],
                                       typesMap: Map[Long, SensorType]): Unit = {

    val sensorNode = nodesMap.apply(observation.sensornodes_idsensornode)
    val sensorType = typesMap.apply(observation.sensortypes_idsensortype)

    val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    val measIsoTime = observation.meastime.toInstant
      .atZone(ZoneId.systemDefault()).withZoneSameLocal(ZoneId.of("UTC"))
      .format(dateTimeFormatter).replace("[UTC]", "") //get rid of the [UTC] at the end of the string.


    logger.debug(s"Telling $mqttActorSel CmdMqttPublish() ObservationID: ${observation.idsensormeasurement}")
    mqttActorSel ! CmdMqttPublish(msgType = SensorwebObservations,
      topic = urlify(s"${sensorNode.name}/p${sensorType.sensid}_${sensorType.phenomenon}"),
      body = s"$measIsoTime;${observation.calcvalue};${sensorType.unit}",
      retain = false)

    //TODO mark measurements as failed when error!

    SensorMeasurement.updateSosState(observation.idsensormeasurement, true, 0)
  }

  // here former SosConnector Stuff?
  @deprecated
  def uploadTextObservations(newOrFailed: Boolean, maxNum: Long): Unit = {

    // val updateListMeas = SensorMeasurement.getAllNewSosForUpload
    val updateListTextObs = SensorTextObservation.getSelectForSosUpload(newOrFailed, maxNum)

    val obsToUpload = updateListTextObs.length

    // logger.debug(s"we assume all procedureIDs are well-known and exist in SOS (because checked/refreshed at startup and on Node/type editing (to be implemented :-p))")
    logger.debug(s"updateListTextObs where SosUpload is false - $obsToUpload open SensorMeasurements")

    val nodesList = for {
      sensorNode <- SensorNode.getAllWithParser
    } yield (sensorNode.idsensornode, sensorNode)

    val nodesMap = nodesList.toMap

    val typesList = for {
      sensorType <- SensorType.getAllWithParser
    } yield (sensorType.idsensortype, sensorType)

    val typesMap = typesList.toMap

    var returnCode = 0

    for (obs <- updateListTextObs) {

      // templates for string-based XML generation reside under models.sos
      logger.debug(s"observation insert for ${obs.idsensortextobservation}")

      val nodeID = nodesMap.apply(obs.sensornodes_idsensornode)
      val typeID = typesMap.apply(obs.sensortypes_idsensortype)

      val phenTime = obs.meastime

      // only nodes or the network equivalent do have status text messages as of now
      var offeringURI = VOCAB_PREFIX_OFFERING + "/" + VOCAB_NETWORK_IDENTIFIER + "/" + urlify(nodeID.name)
      var sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + VOCAB_NETWORK_IDENTIFIER + "/" + urlify(nodeID.name)

      if (nodeID.idsensornode == nodeEquivalentID) {
        offeringURI = VOCAB_PREFIX_OFFERING + "/" + VOCAB_NETWORK_IDENTIFIER
        sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + VOCAB_NETWORK_IDENTIFIER
      }

      val phenomenonURI = VOCAB_PREFIX_PHENOMENON + "/" + urlify(typeID.phenomenon)
      // val uomCode = typeID.unit
      val category = obs.category
      val textvalue = obs.textvalue
      val featureURI = VOCAB_PREFIX_FEATURE + "/" + urlify(nodeID.name)
      // val position: Array[java.lang.Double] = Array(nodeID.latitude, nodeID.longitude, nodeID.altitude)
      val obsType = "TEXT"
      val codeSpace = "http://zgis.at"
      val sampledFeatureURI = sensorURI

      val myOmXml: ObservationDescription = new ObservationDescription(phenTime, offeringURI,
        sensorURI, phenomenonURI,
        category + ": " + textvalue, featureURI, null, obsType,
        codeSpace, sampledFeatureURI)

      val insertObsFuture = insertObservation(myOmXml, offeringURI, obs.idsensortextobservation)

      insertObsFuture.map {
        returnCode =>
          returnCode match {
            case 0 => {
              logger.info(s"Sucessfully inserted observation ${obs.idsensortextobservation}")
              SensorTextObservation.updateSosState(obs.idsensortextobservation, false, returnCode)
            }
            case _ => {
              logger.warn(s"Error while inserting observation ${obs.idsensortextobservation}, check if sensor $sensorURI exists...")
              val existsFuture = existsSensor(sensorURI)

              existsFuture.map {
                returnCode =>
                  returnCode match {
                    case 0 => {
                      logger.warn(s"Sensor $sensorURI does not exists -> inserting")

                      insertObservation(myOmXml, offeringURI, obs.idsensortextobservation).map { returnCode =>
                        returnCode match {
                          case 0 => {
                            logger.debug(s"Observation ${obs.idsensortextobservation} inserted.")
                            SensorTextObservation.updateSosState(obs.idsensortextobservation, true,
                              returnCode)
                          }
                          case _ => {
                            logger.warn(s"observation insert error ${obs.idsensortextobservation}")
                            SensorTextObservation.updateSosState(obs.idsensortextobservation, false,
                              returnCode)
                          }
                        }
                      }
                    }
                    case _ => {
                      logger.warn(s"observation insert error ${obs.idsensortextobservation}")
                      SensorTextObservation.updateSosState(obs.idsensortextobservation, false, returnCode)
                    }
                  }
              }
            }
          }
      }
    }

  }

  def checkCapabilities(capabilitiesUrl: String): Future[Int] = {
    logger.info(s"Checking capabilities for $capabilitiesUrl...")

    val holder: WSRequestHolder = WS.url(capabilitiesUrl)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withQueryString("service" -> "SOS")
      .withQueryString("AcceptVersions" -> "2.0.0")
      .withQueryString("request" -> "GetCapabilities")
      .withQueryString("Sections" -> "OperationsMetadata")
    //.withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    val futureResult: Future[Int] = complexHolder.get().map {
      response => {
        var returnCode: Int = 5

        if (response.body.contains("ows:Exception")) {
          if (response.body.contains("InvalidParameterValue")) {
            logger.warn("GetCapabilities: InvalidParameterValue")
            returnCode = 1
          }
          else {
            logger.warn("GetCapabilities: some other ows:Exception")
            returnCode = 5
          }
        }
        else {
          if (response.body.contains("sos:Capabilities")) {
            if (response.body.contains("ows:OperationsMetadata")) {
              logger.info("GetCapabilities ows:OperationsMetadata")
              returnCode = 0
            }
            else {
              logger.warn("GetCapabilities: unexpected error")
              returnCode = 5
            }
          }
          else {
            logger.warn("Unexpected response to GetCapabilites.")
            logger.warn(response.body)
            returnCode = 5
          }
        }
        returnCode
      }
    }
    // trying to be unblocking as long as possible
    futureResult.recover {
      case e: Exception => {
        logger.debug(s"GetCapabilities: timeout / exception ${e.getMessage}", e)
        5
      }
    }
    futureResult
  }

  /**
    * Sends "describeSensor" to SOS to check if a Sensor exists.
    * @param sensorURI
    * @return
    */
  def existsSensor(sensorURI: String): Future[Int] = {
    logger.debug(s"Checking if $sensorURI exists in SOS")

    val holder: WSRequestHolder = WS.url(sosDcpKvp)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withQueryString("service" -> "SOS")
      .withQueryString("version" -> "2.0.0")
      .withQueryString("request" -> "DescribeSensor")
      .withQueryString("procedureDescriptionFormat" -> "http://www.opengis.net/sensorML/1.0.1")
      .withQueryString("procedure" -> sensorURI)
    //.withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    //FIXME refactor this var returnCode mess.
    val futureResult: Future[Int] = complexHolder.get().map {
      response => {
        logger.trace(s"Response Body: ${response.body}")
        var returnCode: Int = 5

        if (response.body.contains("ows:Exception")) {
          if (response.body.contains("InvalidParameterValue")) {
            logger.warn(s"existsSensor: InvalidParameterValue - sensorID '$sensorURI' does not exist")
            returnCode = 1
          }
          else {
            logger.warn("existsSensor: some other ows:Exception")
            returnCode = 5
          }
        }
        else {
          if (response.body.contains("swes:DescribeSensorResponse")) {
            if (response.body.contains(sensorURI)) {
              logger.debug(s"existsSensor: response indicates sensor $sensorURI does very likely exist.")
              returnCode = 0
            }
            else {
              logger.warn("existSensor: Unexpected response. SWES response does not contain SensorID")
              returnCode = 5
            }
          }
          else {
            logger.error("existsSensor: This path is unlikely to be executed at all. Refactor me!!!")
            returnCode = 5
          }
        }
        returnCode
      }
    }

    futureResult.recover {
      case e: Exception => {
        logger.warn("existsSensor: timeout / exception " + e.getMessage)
        5
      }
    }

    futureResult
  }

  def getSensorDescription(sensorURI: String): Future[String] = {

    logger.info(s"Get SensorDescription $sensorURI")
    val holder: WSRequestHolder = WS.url(sosDcpKvp)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withQueryString("service" -> "SOS")
      .withQueryString("version" -> "2.0.0")
      .withQueryString("request" -> "DescribeSensor")
      .withQueryString("procedureDescriptionFormat" -> "http://www.opengis.net/sensorML/1.0.1")
      .withQueryString("procedure" -> sensorURI)
    //.withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    val futureResult: Future[String] = complexHolder.get().map {
      response => {
        // body.xml maybe ?!
        response.body
      }
    }

    // trying to be unblocking as long as possible
    futureResult.recover {
      case e: Exception => {
        logger.warn(s"getSensorDescription: timeout / exception: ${e.getMessage}", e)
        //return value
        s"<err>${e.getMessage}</err>"
      }
    }
    futureResult
  }

  def deleteSensor(sensorURI: String): Future[Int] = {
    logger.info(s"deleteSensor: $sensorURI")

    val holder: WSRequestHolder = WS.url(sosDcpKvp)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withQueryString("service" -> "SOS")
      .withQueryString("version" -> "2.0.0")
      .withQueryString("request" -> "DeleteSensor")
      .withQueryString("procedure" -> sensorURI)
      .withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    val futureResult: Future[Int] = complexHolder.get().map {
      response => {
        var returnCode: Int = 5
        if (response.body.contains("ows:Exception")) {
          if (response.body.contains("InvalidParameterValue")) {
            logger.info(
              s"deleteSensor: InvalidParameterValue - sensorID $sensorURI does not exist, but therefore is kind of deleted")
            returnCode = 2
          }
          else {
            logger.info("deleteSensor: some other ows:Exception")
            returnCode = 5
          }
        }
        else {
          if (response.body.contains("swes:deletedProcedure")) {

            if (response.body.contains(sensorURI)) {
              logger.info(s"deleteSensor: sensorID $sensorURI deleted")
              returnCode = 0
            }
            else {
              logger.warn("deleteSensor: unexpected error, swes response does not contain sensor ID")
              returnCode = 5
            }
          }
          else {
            logger.warn("Unexpected SOS response")
            logger.warn(response.body)
            returnCode = 5
          }
        }
        returnCode
      }
    }
    // trying to be unblocking as long as possible
    futureResult.recover {
      case e: Exception => {
        logger.warn(s"deleteSensor: timeout / exception ${e.getMessage}", e)
        // return value!
        5
      }
    }
    futureResult
  }

  def insertSensor(sensorType: SOSConstants.EntityType, uniqueSensorID: String, mySml: SensorDescription,
                   sensorURI: String,
                   phenomenonURI: String, obsType: String): Future[Int] = {

    logger.info(s"Inserting sensor $sensorURI...")
    val obsTypeUri = getObsTypeURI(obsType)
    val samplingDefUri = SOSConstants.SAMPLINGPOINT_DEF

    // default headers, schema and soap envelope
    val sosInsertXmlHeader = SOSConstants.InsertSensorHeaders
    val sensorML = mySml.getSensorML

    // BEWARE horrible readability :-) full of string interpolation
    val insertXML: String =
      s"""$sosInsertXmlHeader
          |$sensorML
          |</swes:procedureDescription>
          |<swes:observableProperty>$phenomenonURI</swes:observableProperty>
          |<swes:metadata><sos:SosInsertionMetadata>
          |<sos:observationType>$obsTypeUri</sos:observationType>
          |<sos:featureOfInterestType>$samplingDefUri</sos:featureOfInterestType>
          |</sos:SosInsertionMetadata>
          |</swes:metadata>
          |</swes:InsertSensor>
          |""".stripMargin

    // logger.debug(insertXML)

    val holder: WSRequestHolder = WS.url(sosDcpPox)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withHeaders("Content-Type" -> "application/xml")
      .withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    val futureResult: Future[Int] = complexHolder.post(insertXML).map {
      response => {
        var returnCode: Int = 5
        if (response.body.contains("ows:Exception")) {

          if (response.body.contains("InvalidParameterValue")) {
            logger.warn(s"insertSensor: InvalidParameterValue $sensorURI")
            returnCode = 2
          }
          else {
            logger.warn(s"insertSensor: some other ows:Exception $sensorURI")
            returnCode = 5
          }
        }
        else {
          if (response.body.contains("swes:InsertSensorResponse")) {

            if (response.body.contains(uniqueSensorID)) {
              logger.info(s"insertSensor: sensorID $uniqueSensorID inserted")
              returnCode = 0
            }
            else {
              logger.warn(s"insertSensor: unexpected error, swes response does not contain sensor ID $sensorURI")
              returnCode = 5
            }
          }
          else {
            logger.warn("Unexpected SOS response.")
            logger.warn(response.body)
            returnCode = 5
          }
        }
        returnCode
      }
    }
    // trying to be unblocking as long as possible
    futureResult.recover {
      case e: Exception => {
        logger.debug(s"insertSensor: timeout / exception ${e.getMessage}")
        // return value!
        5
      }
    }
    futureResult
  }

  def updateSensor(sensorType: SOSConstants.EntityType, uniqueSensorID: String, mySml: SensorDescription,
                   sensorURI: String,
                   phenomenonURI: String, obsType: String): Future[Int] = {

    logger.debug("updateSensor:" + sensorURI)
    val obsTypeUri = getObsTypeURI(obsType)
    val samplingDefUri = SOSConstants.SAMPLINGPOINT_DEF

    // default headers, schema and soap envelope
    val sosUpdateXmlHeader = SOSConstants.UpdateSensorHeaders
    val sensorML = mySml.getSensorML

    // BEWARE horrible readability :-) full of string interpolation
    val insertXML: String =
      s"""$sosUpdateXmlHeader
<swes:procedure>$sensorURI</swes:procedure>
<swes:procedureDescriptionFormat>http://www.opengis.net/sensorML/1.0.1</swes:procedureDescriptionFormat>
<swes:description>
<swes:SensorDescription>
<swes:data>
$sensorML
</swes:data>
</swes:SensorDescription>
</swes:description>
</swes:UpdateSensorDescription>\n"""

    val holder: WSRequestHolder = WS.url(sosDcpPox)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withHeaders("Content-Type" -> "application/xml")
      .withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    val futureResult: Future[Int] = complexHolder.post(insertXML).map {
      response => {
        var returnCode: Int = 5
        if (response.body.contains("ows:Exception")) {

          if (response.body.contains("InvalidParameterValue")) {
            logger.warn("updateSensor: InvalidParameterValue")
            returnCode = 2
          }
          else {
            logger.warn("updateSensor: some other ows:Exception")
            returnCode = 5
          }
        }
        else {
          if (response.body.contains("swes:InsertSensorResponse")) {

            if (response.body.contains(uniqueSensorID)) {
              logger.info(s"updateSensor: sensorID $uniqueSensorID inserted")
              returnCode = 0
            }
            else {
              logger.warn("updateSensor: unexpected error, swes response does not contain sensor ID")
              returnCode = 5
            }
          }
          else {
            logger.warn("Unexpected SOS response.")
            logger.warn(response.body)
            returnCode = 5
          }
        }
        returnCode
      }
    }
    // trying to be unblocking as long as possible
    futureResult.recover {
      case e: Exception => {
        logger.debug(s"updateSensor: timeout / exception ${e.getMessage}")
        // return value!
        5
      }
    }
    futureResult
  }

  def updateSensorMeasureModeCharacteristic(sensorURI: String, smlCharacteristic: String): Unit = {

    logger.debug(s"updateSensor: $sensorURI")

    val mySml = getSensorDescription(sensorURI).map { smltext =>
      val sml: String = if (smltext.contains(sensorURI) && smltext.contains("LOW") && (!smltext.contains("<err>"))) {
        smltext.replace("LOW", "HIGH")
      }
                        else if (smltext.contains(sensorURI) && smltext.contains("HIGH") && (!smltext.contains(
        "<err>"))) {
        smltext.replace("HIGH", "LOW")
      }
                        else {
                          "ERROR"
                        }
      sml
    }

    mySml.onSuccess {
      case sensorML: String => {

        if (sensorML.equalsIgnoreCase("ERROR")) {
          throw new java.io.IOException(s"cant read sensorml")
        }
        // default headers, schema and soap envelope
        val sosUpdateXmlHeader = SOSConstants.UpdateSensorHeaders

        // BEWARE horrible readability :-) full of string interpolation
        val insertXML: String =
          s"""$sosUpdateXmlHeader
  <swes:procedure>$sensorURI</swes:procedure>
  <swes:procedureDescriptionFormat>http://www.opengis.net/sensorML/1.0.1</swes:procedureDescriptionFormat>
  <swes:description>
  <swes:SensorDescription>
  <swes:data>
  $sensorML
  </swes:data>
  </swes:SensorDescription>
  </swes:description>
  </swes:UpdateSensorDescription>\n"""

        val holder: WSRequestHolder = WS.url(sosDcpPox)

        val complexHolder: WSRequestHolder = holder
          .withRequestTimeout(UPLINK_SOS_TIMEOUT)
          .withHeaders("Content-Type" -> "application/xml")
          .withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

        val futureResult: Future[Int] = complexHolder.post(insertXML).map {
          response => {
            var returnCode: Int = 5
            if (response.body.contains("ows:Exception")) {

              if (response.body.contains("InvalidParameterValue")) {
                logger.warn("updateSensorMeasureModeCharacteristic: InvalidParameterValue")
                returnCode = 2
              }
              else {
                logger.warn("updateSensorMeasureModeCharacteristic: some other ows:Exception")
                returnCode = 5
              }
            }
            else {
              if (response.body.contains("swes:UpdateSensorResponse")) {

                if (response.body.contains(sensorURI)) {
                  logger.info(s"updateSensorMeasureModeCharacteristic: sensorID $sensorURI updated")
                  returnCode = 0
                }
                else {
                  logger.warn("updateSensorMeasureModeCharacteristic: unexpected error, swes response does not contain sensor ID")
                  returnCode = 5
                }
              }
              else {
                logger.warn("Unexpected SOS response.")
                logger.warn(response.body)
                returnCode = 5
              }
            }
            returnCode
          }
        }
        // trying to be unblocking as long as possible
        futureResult.recover {
          case e: Exception => {
            logger.debug(s"updateSensorMeasureModeCharacteristic: timeout / exception ${e.getMessage}")
            // return value!
            5
          }
        }

      }
      case _ => logger.error("updateSensorMeasureModeCharacteristic: wrong update message format received")

    }
  }

  def insertObservation(myOMXML: ObservationDescription, offeringURI: String, obsID: Long): Future[Int] = {

    //    import play.api.libs.concurrent.Execution.Implicits._
    //    import play.api.Play.current

    logger.debug(s"insertObservation: $offeringURI")

    val omXml = myOMXML.getOM_Member
    val sosOMInsertXmlHeader = SOSConstants.InsertObservationHeaders

    // BEWARE horrible readability :-) full of string interpolation
    val insertXML: String =
      s"""$sosOMInsertXmlHeader
$omXml
</sos:InsertObservation>"""

    val holder: WSRequestHolder = WS.url(sosDcpPox)

    val complexHolder: WSRequestHolder = holder
      .withRequestTimeout(UPLINK_SOS_TIMEOUT)
      .withHeaders("Content-Type" -> "application/xml")
      .withHeaders("Authorization" -> UPLINK_SOS_SECURITY_TOKEN)

    val futureResult: Future[Int] = complexHolder.post(insertXML).map {
      response => {
        var returnCode: Int = 5
        if (response.body.contains("ows:Exception")) {
          if (response.body.contains("InvalidParameterValue")) {
            logger.warn(s"InsertObservation: InvalidParameterValue (Observation-ID: $obsID)")

            returnCode = 2
          }
          else {
            logger.info(s"InsertObservation: some other ows:Exception (Observation-ID: $obsID)")
            returnCode = 5
          }
        }
        else {
          if (response.body.contains("sos:InsertObservationResponse")) {
            logger.debug(s"InsertObservation: inserted (Observation-ID: $obsID)")
            returnCode = 0
          }
          else {
            logger.warn(s"InsertObservation: Unexpected response from sos. (Observation-ID: $obsID)")
            logger.warn(response.body)
            returnCode = 5
          }
        }
        returnCode
      }
    }
    // trying to be unblocking as long as possible
    futureResult.recover {
      case e: Exception => {
        logger.debug(s"insertObservation: timeout / exception - ${e.getMessage}", e)
        // return value
        5
      }
    }
    futureResult
  }

  def getObsTypeURI(typeofObservation: String): String = {
    // ObservationType MEASUREMENT, COUNT, TEXT, CATEGORY, TRUTH
    typeofObservation match {
      case "MEASUREMENT" => SOSConstants.MEASUREMENT_OBS_DEF
      case "COUNT" => SOSConstants.COUNT_OBS_DEF
      case "TEXT" => SOSConstants.TEXT_OBS_DEF
      case "CATEGORY" => SOSConstants.CATEGORY_OBS_DEF
      case "TRUTH" => SOSConstants.TRUTH_OBS_DEF
    }
  }

  private def urlify(term: String): String = {
    logger.debug(s"Urlify: $term")
    val resulttext = term.toLowerCase().replaceAll("\\ ", "_").replaceAll("\\+", "-").replaceAll("\\(", ".").replaceAll(
      "\\)", ".")
    logger.debug(s"result: $resulttext")
    resulttext
  }

  def insertNetworkSensor(networkID: String, obsProp: String,
                          uomCode: String, obsType: String, codeSpace: String): Future[Int] = {

    //    import play.api.libs.concurrent.Execution.Implicits._
    //    import play.api.Play.current

    val mySml: SensorDescription = new SensorDescription(networkID, obsProp,
      uomCode, obsType, codeSpace, true)
    val sensorType = SOSConstants.EntityType.NETWORK
    val sensorURI: String = VOCAB_PREFIX_PROCEDURE + "/" + networkID
    val phenomenonURI: String = VOCAB_PREFIX_PHENOMENON + "/" + obsProp

    val insertFuture = insertSensor(sensorType, networkID, mySml, sensorURI,
      phenomenonURI, obsType)

    insertFuture
  }

  def insertPlatformSensor(platformID: String, networkID: String, obsProp: String,
                           uomCode: String, position: Array[java.lang.Double], obsType: String,
                           codeSpace: String): Future[Int] = {

    val mySml: SensorDescription = new SensorDescription(platformID, networkID, obsProp,
      uomCode, position, obsType, codeSpace)
    val sensorType = SOSConstants.EntityType.PLATFORM
    val sensorURI: String = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/" + platformID
    val phenomenonURI: String = VOCAB_PREFIX_PHENOMENON + "/" + obsProp

    val insertFuture = insertSensor(sensorType, networkID, mySml, sensorURI,
      phenomenonURI, obsType)

    insertFuture
  }

  def insertChildSensor(childSensorID: String, platformID: String, networkID: String, obsProp: String,
                        uomCode: String, placement: Array[java.lang.Double], obsType: String,
                        codeSpace: String): Future[Int] = {

    val mySml: SensorDescription = new SensorDescription(childSensorID, platformID, networkID, obsProp,
      uomCode, placement, obsType, codeSpace)
    val sensorType = SOSConstants.EntityType.SENSOR
    val sensorURI: String = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/" + platformID + "/" + childSensorID
    val phenomenonURI: String = VOCAB_PREFIX_PHENOMENON + "/" + obsProp

    val insertFuture = insertSensor(sensorType, networkID, mySml, sensorURI,
      phenomenonURI, obsType)

    insertFuture
  }

  def sosSystemSetup(): Unit = {

    logger.info("preparing SOS System Setup. Inserting Sensors into SOS if they do not exist.")

    // log system status sensortype is defined 50 in SQL
    val typeID = SensorType.getSensorTypeByID(50)

    // val sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + VOCAB_NETWORK_IDENTIFIER + "/" + urlify(nodeID.extendedaddress) + "/" + "p" + urlify(typeID.sensid) + "_" +urlify(typeID.phenomenons)
    val obsProp = urlify(typeID.phenomenon)
    val uomCode = typeID.unit
    val phenomenonURI = VOCAB_PREFIX_PHENOMENON + "/" + obsProp
    val obsType = "TEXT"
    val codeSpace = "http://zgis.at"

    val networkURI = VOCAB_PREFIX_PROCEDURE + "/" + VOCAB_NETWORK_IDENTIFIER
    val networkExistsFuture = existsSensor(networkURI)

    networkExistsFuture.map {
      returnCode =>
        returnCode match {
          case 0 => {
            logger.debug(s"Network $networkURI exists, ok")
            0
          }
          case 1 => {
            logger.info(s"Network $networkURI does not exsist. Inserting...")

            val insertNetworkFuture = insertNetworkSensor(VOCAB_NETWORK_IDENTIFIER, obsProp, uomCode, obsType,
              codeSpace)

            insertNetworkFuture.map {
              returnCode =>
                returnCode match {
                  case 0 => {
                    logger.debug(s"Network $networkURI created.")
                    0
                  }
                  case _ =>
                    logger.warn(s"Could not insert network $networkURI. Aborting.")
                    5
                }
            }
          }
          case _ => {
            logger.warn(s"Exception while querying network $networkURI.")
            5
          }
        }
    }

    networkExistsFuture.onComplete {

      case Success(returnCode) => {
        val nodes = SensorNode.getAllWithParser

        nodes.map {
          sensorNode => {
            if (sensorNode.idsensornode != nodeEquivalentID) {

              val platformName = urlify(sensorNode.name)
              val platformURI = VOCAB_PREFIX_PROCEDURE + "/" + VOCAB_NETWORK_IDENTIFIER + "/" + platformName
              logger.info(s"Finding platform $platformURI...")

              val platformExistsFuture = existsSensor(platformURI)

              platformExistsFuture.map {
                returnCode =>
                  returnCode match {
                    case 0 => {
                      logger.info(s"Platform $platformURI exists.")
                    }
                    case 1 => {
                      logger.warn(s"Platform $platformURI does not exists. Inserting...")

                      val position: Array[java.lang.Double] = Array(sensorNode.latitude, sensorNode.longitude,
                        sensorNode.altitude)
                      val insertPlatformFuture = insertPlatformSensor(platformName, VOCAB_NETWORK_IDENTIFIER, obsProp,
                        uomCode, position, obsType, codeSpace)

                      insertPlatformFuture.map {
                        returnCode =>
                          returnCode match {
                            case 0 => {
                              logger.info(s"Platform $platformURI insert sucessful.")
                            }
                            case _ => {
                              logger.warn(s"Could not insert platform $platformURI. SOS return code: $returnCode. (${sensorNode.name}:${sensorNode.extendedaddress})")
                            }
                          }
                      }
                    }
                    case _ => {
                      logger.warn(s"Querying $platformURI resulted in error. SOS return code: $returnCode. (${sensorNode.name}:${sensorNode.extendedaddress})")
                    }
                  }
              }
            }
          }
        }
      }
      case Failure(ex) => {
        logger.warn(s"SOS system setup failed: ${ex.getMessage}", ex)
      }
    }

  }
}
