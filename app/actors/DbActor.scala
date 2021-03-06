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

import scala.util.Try
import play.api.Logger
import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneId}

import actors.ActorSupervisor.CmdStatus
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import akka.actor.Actor
import models._

case class InsertMeasurementMessage(addr: String, sid: Int, ts: Timestamp, raw: Double, data: Double)
case class XBeeDataMessage(addr: String, sid: Int, ts: Timestamp, raw: Double, data: Double)

@deprecated case class TaromDataMessage(fieldid: Int, value: String)
@deprecated case class SpaDataMessage(value: String)
@deprecated case class LogDataMessage(loglevel: String, logtext: String)
@deprecated case class WizDataMessage(sid: Int, ts: Timestamp, data: Double)

/**
 * String addr = sensDataFrame.getAddr64asString();
 * int sid = sd.getSensorID();
 * java.sql.Timestamp ts = sensDataFrame.getTimestamp();
 * Double raw = sd.getRawDataAsDouble();
 * Double data = sd.getData();
 */

//TODO SREI refactor
object DbActor {
  /** the internal name of the actor */
  val ActorName = "database"
}

class DbActor extends Actor {
  val logger = Logger(this.getClass())

  override def preStart(): Unit = {
    logger.info(s"DbActor started as '${self.path}'")
    createInsertLogData("info from DbActor", "DbActor is now getting ready...")
  }

  val format: DateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")

  def receive = {
    // I gues we'll need message type for all the different "Drivers
    case TaromDataMessage(fieldid, value) => {
      logger.debug(s"got Tarom Data $fieldid - $value")
      // we could use the field length as indicator that something is wrong?
      // I set -1 intentionally from  the tarom line reader
      if (fieldid == -1) {
        parseAndInsertTaromString(value)
      } else if (fieldid == 1337) {
        preprocessTaromRubbish(value)
      }
    }
    // I gues we'll need message type for all the different "Drivers
    case SpaDataMessage(value) => {
      logger.debug(s"got Spa Data Line $value")
      val spameas = SpaDataFrame.parseSpaMessage(value)
      insertSpaMeasurements(spameas)
    }
    // here could come case class matchers for eg ZigBee
    // I gues we'll need message type for all the different "Drivers
    case XBeeDataMessage(addr, sid, ts, raw, data) => {
      logger.debug(s"got XBee Data: $addr, $sid, $ts, $raw, $data")
      //TODO waspmotes deliver wrong timestamps, here workaround take actual system timesamp
      val systemTime: Timestamp = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")))
      insertMeasurement(addr, sid, ts, raw, data)
    }

    case InsertMeasurementMessage(addr, sid, ts, raw, data) => {
      logger.debug(s"Insert new measurement: $addr, $sid, $ts, $raw, $data")
      insertMeasurement(addr, sid, ts, raw, data)
    }

    // big logging test
    case LogDataMessage(loglevel, logtext) => {
      // later we could match on the loglevel thing and do more logic
      // FIXME switch off logging here or at all the sender places
      logger.info(s"got log message $loglevel - $logtext")
      //createInsertLogData(loglevel, logtext)
    }
    case WizDataMessage(sid, ts, data) => {
      logger.debug(s"got WIZ data: ${sid}, ${ts}, ${data}")
      insertWizMeasurement(sid, ts, data)
    }

    case CmdStatus => {
      logger.debug(s"Received CmdStatus from ${sender()}")
      sender() ! "Alive and rocking!!!"
    }

    case default => {
      logger.warn(s"Received unknown message ${default} from ${sender()}")
    }
  }

  @deprecated
  def preprocessTaromRubbish(value: String) = {
    parseAndInsertTaromString("1; " + value)
  }

  @deprecated
  def parseAndInsertTaromString(value: String) = {

    val parseWithIndex = value.split(";").zipWithIndex

    var datestring = ""
    var timestring = ""

    for ((str_val, index_val) <- parseWithIndex) {

      if (index_val == 0) {
        // versionsnummer
        // val vers = str_val
      } else if (index_val == 1) {
        datestring = str_val
      } else if (index_val == 2) {
        timestring = str_val
      } else if (index_val == 3) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 4) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 5) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 6) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 7) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 8) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 9) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 10) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 11) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 12) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 13) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 14) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 15) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 16) {
        // fehlerstatus code (aber auch ne nummer)
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 17) {
        // Lademodus buchstabe code
        // val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 18) {
        // lastschalter code (aber auch ne nummer)
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 19) {
        // relais 1 code (aber auch ne nummer)
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 20) {
        // relais 2 code (aber auch ne nummer)
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 21) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 22) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 23) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 24) {
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 25) {
        // Derating code (aber auch ne nummer)
        val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      } else if (index_val == 26) {
        //  CRC-16-CCITT/openUART code (aber auch ne nummer)
        // val meas = createInsertMeasurement(datestring, timestring, 300 + index_val, str_val)
      }
    }
  }

  @deprecated
  def createInsertMeasurement(date: String, time: String, fieldid: Long, value: String) {
    // wenn das net funzt, dann pfui -> "#" is sortof NoData
    val parseValueTry: Try[Double] = Try(value.toDouble)

    if (parseValueTry.isSuccess) {

      val parsedValue = parseValueTry.get
      val datevalue = parseDate(date, time) getOrElse (new DateTime)
      // sensortype ID 1-23 div wasp types, 24-32 SPA types, 300-326 TAROM types
      // NODE ID 1,2 wasp, 3 SPA, 15 TAROM
      val meas = SensorMeasurement(-1, datevalue.toDate(), 0.0, 0.0, 0.0, parsedValue, parsedValue, sostransmitted = false, -1, 15, fieldid)

      // here inserted!
      val retval = SensorMeasurement.insertWithoutId(meas)
    }
  }

  @deprecated
  def parseDate(date: String, time: String): Try[DateTime] = {
    Try(format.parseDateTime(s"$date $time"))
  }

  def insertSpaMeasurements(spams: List[SpaDataFrame]) = {

    // sensortype ID 1-23 div wasp types, 24-32 SPA types, 300-326 TAROM types
    // NODE ID 1,2 wasp, 3 SPA, 15 TAROM
    val now = new DateTime

    for (spam <- spams) {

      spam.sensorid match {

        case 304 => {
          val fieldid = 24
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case 305 => {
          val fieldid = 25
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case 306 => {
          val fieldid = 26
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)

        }
        case 307 => {
          val fieldid = 27
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case 316 => {
          val fieldid = 28
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case 318 => {
          val fieldid = 29
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case 300 => {
          val fieldid = 30
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case 302 => {
          val fieldid = 32
          val meas = SensorMeasurement(-1, now.toDate(), 0.0, 0.0, 0.0, spam.rawdata, spam.data, sostransmitted = false, -1, 3, fieldid)
          SensorMeasurement.insertWithoutId(meas)
        }
        case _ => {
          logger.debug("unhandled SPA fieldid sensid")
        }
      }
    }
  }

  def insertMeasurement(extendedAddress: String, sensorId: Int, ts: Timestamp, raw: Double, data: Double): Unit = {
    // eventuell noch some data scrubbing?
    val nodes = SensorNode.getSensorNodeByExtendedAddress(extendedAddress)
    val types = SensorType.getSensorTypeBySensID(sensorId.toLong)
    val nodeslen = nodes.size
    val typeslen = types.size

    if (nodeslen <= 0) {
      logger.error("Could not find Sensor Node: " + extendedAddress)
    }
    else if (nodeslen > 1) {
      logger.error("SensorNode is not unique: " + extendedAddress)
    }
    else if (typeslen < 0) {
      logger.error("Could not find Sensor Type: " + sensorId)
    }
    else if (typeslen > 1) {
      logger.error("SensorType is not unique: " + sensorId)
    }
    else {
        // this gives an exception, when the list is empty!
      val senstype = types.head
      val node = nodes.head

      val meas = SensorMeasurement(-1, ts, 0.0, 0.0, 0.0, raw, data, sostransmitted = false, -1, node.idsensornode, senstype.idsensortype)
      val retval = SensorMeasurement.insertWithoutId(meas)
    }
  }

  @deprecated
  def createInsertLogData(loglevel: String, logtext: String): Unit = {
    val datevalue = new DateTime
    // NODE ID the local gateway service :-p which is the network
    // sensortype 50 SystemMessages
    // TODO FIXME Replace magic number 16 by "select id from sensornodes where macid == application.conf.sensorweb.uplink.sos.node_equivalent. Altn, lass mich nie wieder so lange suchen, warum da im LOG der scheiß name vno dem NOde nicht stimmt! Solange bitte den Sensor in der DB die ID 1!
    val meas = SensorTextObservation(-1, datevalue.toDate(), 0.0, 0.0, 0.0, loglevel, logtext, sostransmitted = false, -1, 1, 50)

    // here inserted!
    val retval = SensorTextObservation.insertNoID(meas)
  }

  def insertWizMeasurement(sid: Int, ts: Timestamp, data: Double): Unit = {
    //FIXME WIZ Actor has to find it's ID (nodeId) by itself! Should be configured in the application.conf via extendedAddress / serialNumber
    if (!SensorMeasurement.existsTimestamp(15, sid, ts)) {
      val meas = SensorMeasurement(-1, ts, 0.0, 0.0, 0.0, data, data, sostransmitted = false, -1, 15, sid)
      SensorMeasurement.insertWithoutId(meas)
    }
    else {
      logger.warn(s"WIZ measurement already in databse for timestamp: ${ts}")
    }
  }
}

