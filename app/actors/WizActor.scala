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

import java.sql.Timestamp

import actors.ActorSupervisor.CmdStatus
import actors.WizActor._
import akka.actor._
import akka.io.IO
import akka.util.ByteString
import com.github.jodersky.flow.Serial._
import com.github.jodersky.flow.{AccessDeniedException, Parity, Serial, SerialSettings}
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.libs.Akka
import utils.MyLogger

import scala.concurrent.duration._
import scala.util.matching.Regex


object WizActor {

  sealed trait WizActorCommand

  case class SetMeasurementFrequency(low: Boolean) extends WizActorCommand

  sealed trait WizCommand {def command: String}

  //these commands are copied out of systea's manual
  //see http://dev.smart-project.info/opendev/documents/5
  case object StartCompleteCycle extends WizCommand {val command = "#C\r\n"}

  case object RequestSystemStatus extends WizCommand {val command = "#S\r\n"}

  case object ReadActiveMethods extends WizCommand {val command = "#J+\r\n"}

  case object ReadMethodNames extends WizCommand {val command = "#L+\r\n"}

  case class SetActiveMethods(methods: String) extends WizCommand {val command = s"#${methods }\r\n"}

  case object ReadLastResult extends WizCommand {val command = "#D0\r\n"}


  sealed trait WizResponse {def regex: Regex}

  case object WizResponseStandBy extends WizResponse {val regex = "STAND-BY\r\n".r}

  case object WizResponseBusy extends WizResponse {val regex = "BUSY\r\n".r}

  case object WizResponseAck extends WizResponse {val regex = "\u0006\r\n".r}

  case object WizResponseNak extends WizResponse {val regex = "\u0015\r\n".r}

  case object WizResponseMethodNames extends WizResponse {val regex = "(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)\r\n".r}

  case object WizResponseLastResultLine1 extends WizResponse {val regex = "000\\s+(\\d+)/(\\d+)/(\\d+)\\s+(\\d+):(\\d+)\\s*\r\n".r}

  case object WizResponseLastResultLine2 extends WizResponse {val regex = "[\\+\\-](\\d+\\.\\d+)\\s[\\+\\-](\\d+\\.\\d+)\\s[\\+\\-](\\d+\\.\\d+)\\s[\\+\\-](\\d+\\.\\d+)\r\n".r}

  case object WizRepsonseLine extends WizResponse {val regex = "(.*?\r\n)".r}


  val ActorName = "wiz"

  //TODO SREI pass a subpart of config here
  def props(config: Configuration) = Props(new WizActor(config = config))
}

class WizActor(config: Configuration) extends Actor with MyLogger {
  //FIXME SREI this has to be passed via Props!
  lazy val dbActorSel = context.system.actorSelection(s"/user/${ActorSupervisor.ActorName }/${DbActor.ActorName }")
  lazy val sosActorSel = context.system.actorSelection(s"/user/${ActorSupervisor.ActorName }/${SosActor.ActorName }")

  private var _statusStandby: Boolean = false
  private var _measFrequencyLow: Boolean = true
  private var _lastResultRead: Boolean = false
  private var _lastResultTime: Timestamp = Timestamp.valueOf("1970-01-01 00:00:00")

  def isStatusStandby: Boolean = _statusStandby

  def isMeasFrequencyLow: Boolean = _measFrequencyLow

  def isLastResultRead: Boolean = _lastResultRead

  private var _requestStatusCancelable: Option[Cancellable] = None
  private var _startMeasCycleCancelable: Option[Cancellable] = None

  /**
   * Sends command to WIZ and apply partial function to result (ByteString.decodeString("ASCII"))
   * @param command
   * @param pf
   */
  private def sendCommand(serial: ActorRef, command: WizCommand, pf: PartialFunction[String, Boolean]) = {
    //TODO SREI i have no fucking clue if this is done correctly, but it seems to work :-)
    //this is the default behaviour for response parsing
    val default: PartialFunction[String, Boolean] = {
      case resp => {
        logger.warn(s"${resp } could not be parsed.")
        false
      }
    }

    logger.info(s"Sending ${command } to WIZ...")
    serial ! Write(ByteString.fromString(command.command))
    //    var lineBuffer = List[String]()
    var byteBuffer = ByteString.empty
    context.setReceiveTimeout(2.seconds)
    context.become(discardOld = false, behavior = {
      case Received(data) => {
        logger.debug(s"response: ${data.decodeString("ASCII") }")
        logger.debug(s"as Bytes: ${data }")

        byteBuffer = byteBuffer ++ data
        logger.debug(s"byteBuffer is: ${byteBuffer }")

        val lineBuffer = WizRepsonseLine.regex.findAllIn(byteBuffer.decodeString("ASCII")).toList
        logger.debug(s"lineBuffer is: ${lineBuffer.mkString("|").replaceAll("\n", "<LF>").replaceAll("\r", "<CR>") }")

        logger.debug(s"Dropping ${lineBuffer.mkString("").length } elements from byteBuffer")
        byteBuffer = byteBuffer.drop(lineBuffer.mkString("").length)
        logger.debug(s"byteBuffer is: ${byteBuffer }")

        if (lineBuffer.map((line: String) => pf.orElse(default)(line)).contains(true)) {
          logger.info(s"${command } processed.")
          context.setReceiveTimeout(Duration.Undefined)
          context.unbecome
        }
      }
      case ReceiveTimeout => {
        logger.warn(s"${command } received timeout.")
        if (byteBuffer.nonEmpty) {
          logger.info(s"Received ${byteBuffer.length } bytes already, trying to parse...")
          if (pf.orElse(default)(byteBuffer.decodeString("ASCII"))) {
            logger.info(s"${command } processed.")
          }
          else {
            logger.warn(s"${command } failed.")
          }
        }
        context.setReceiveTimeout(Duration.Undefined)
        context.unbecome
      }
    })
  }

  override def preStart() = {
    logger.info(s"WizActor started as '${self.path }'")
    //TODO SREI figure out, how to make sure, this actor is only started once!

    val dbActor = context.actorOf(Props[DbActor], DbActor.ActorName)
    dbActor ! LogDataMessage(s"INFO from '${WizActor.ActorName }'", s"WizActor started as '${self.path }'")

    val port = config.getString("serial.port").getOrElse("/dev/ttyS0")
    val settings = SerialSettings(
      baud = 9600,
      characterSize = 8,
      twoStopBits = false,
      parity = Parity.None
    )

    implicit val actorSystem = Akka.system()
    IO(Serial) ! Serial.Open(port, settings)
  }

  override def receive: Receive = {
    case CommandFailed(cmd: Open, reason: AccessDeniedException) =>
      logger.warn("you're not allowed to open that port!")
    case CommandFailed(cmd: Open, reason) =>
      logger.warn(s"could not open port for some other reason: ${reason.getMessage }")
    case Opened(port) => {
      logger.info(s"Connected to WIZ @'${port }'")
      val serial = sender
      context.become(connected(serial))
      _requestStatusCancelable = Some(Akka.system().scheduler.schedule(45.seconds, 1.minutes, self, RequestSystemStatus))
    }
    case CmdStatus => {
      logger.info("Received CmdStatus")
      val status = s"${WizActor.ActorName }: running as ${self.path }."
      sender() ! scala.util.Success(status)
    } //CmdStatus
    case default => {
      val sender = context.sender()
      logger.warn(s"Received unknown command '${default }' from '${sender }'")
    }
  }

  def connected(serial: ActorRef): Receive = {
    case RequestSystemStatus => sendCommand(serial, RequestSystemStatus, {
      case WizResponseStandBy.regex(_*) => {
        logger.debug("WIZ in STANDBY")
        this._statusStandby = true
        logger.debug("Checking if last result is read.")
        if (!isLastResultRead) {
          logger.info("reading last result.")
          self ! ReadLastResult
        }
        if (_startMeasCycleCancelable.isEmpty) {
          logger.info("No scheduled measurement cycle found.")
          _startMeasCycleCancelable = Some(scheduleNewMeasCycle(false))
        }

        true
      }
      case WizResponseBusy.regex(_*) => {
        logger.debug("WIZ is BUSY")
        this._statusStandby = false
        true
      }
    })
    case StartCompleteCycle => {
      if (!isLastResultRead) {
        logger.warn("Tried to start new measurement cycle before last result was read. Rescheduling")
        self ! ReadLastResult
        _startMeasCycleCancelable = Some(scheduleNewMeasCycle(true))
      }
      else {
        _startMeasCycleCancelable = Some(scheduleNewMeasCycle())
        sendCommand(serial, StartCompleteCycle, {
          case WizResponseBusy.regex(_*) => {
            logger.warn("Could not start complete measurement cycle. WIZ is BUSY.")
            _statusStandby = false
            true
          }
          case WizResponseAck.regex(_*) => {
            logger.info("Complete measurement cycle started")
            _statusStandby = false
            _lastResultRead = false
            true
          }
          case WizResponseNak.regex(_*) => {
            logger.warn("Could not start complete measurement cycle, because of <NAK>. WIZ Error?")
            _statusStandby = true
            true
          }
        })
      }
    }
    case ReadMethodNames => sendCommand(serial, ReadMethodNames, {
      case WizResponseMethodNames.regex(m1, m2, m3, m4) => {
        logger.debug(s"Method names are: ${m1 }, ${m2 }, ${m3 }, ${m4 }")
        true
      }
    })
    case ReadLastResult => sendCommand(serial, ReadLastResult, {
      case WizResponseLastResultLine1.regex(month, day, year, hour, minute) => {
        logger.debug(s"Last result time: 20${year }-${month }-${day } ${hour }:${minute }")
        _lastResultTime = Timestamp.valueOf(s"20${year }-${month }-${day } ${hour }:${minute }:00")
        false
      }
      case WizResponseLastResultLine2.regex(concMeth1, concMeth2, concMeth3, concMeth4) => {
        def toDouble(s: String) = try {s.toDouble} catch {case _: NumberFormatException => 0.0}
        logger.debug(s"concentrations: ${concMeth1 }/${concMeth2 }/${concMeth3 }/${concMeth4 }/")
        if (toDouble(concMeth1) != 0.0) {
          dbActorSel ! WizDataMessage(51, _lastResultTime, toDouble(concMeth1))
        }
        if (toDouble(concMeth2) != 0.0) {
          dbActorSel ! WizDataMessage(52, _lastResultTime, toDouble(concMeth2))
        }
        if (toDouble(concMeth3) != 0.0) {
          dbActorSel ! WizDataMessage(53, _lastResultTime, toDouble(concMeth3))
        }
        _lastResultRead = true
        true
      }
    })
    case Received(data) => {
      logger.debug(s"Received without Command: ${data.decodeString("ASCII") }")
      logger.debug(s"as Bytes:                 ${data }")
    }
    case SetMeasurementFrequency(low) => {
      logger.info(s"Setting measurement frequency to low: ${low }")
      //switch from low to high
      if (isMeasFrequencyLow && !low) {
        if (_startMeasCycleCancelable.isDefined && !_startMeasCycleCancelable.get.isCancelled) {
          logger.info("Cancelling old schedule")
          _startMeasCycleCancelable.get.cancel()
        }
        scheduleNewMeasCycle(true)
        //TODO SREI SosActor ! Change SensorML(high freq)
        //TODO sort of hardcoded """(updatewizsml)<>(http.*)<>(HIGH|LOW)"""
        sosActorSel ! """updatewizsml<>http://vocab.smart-project.info/sensorweb/procedure/koppl/wiztest01/p53_orthophosphate<>HIGH"""
        // sosActorSel ! """updatewizsml<>hhttp://vocab.smart-project.info/sensorweb/procedure/koppl/wiztest01/p52_bioavailable_phosphorus<>HIGH"""
        // sosActorSel ! """updatewizsml<>http://vocab.smart-project.info/sensorweb/procedure/koppl/wiztest01/p51_total_phosphorus<>HIGH"""
      }
      //switch from high to low
      else if (!isMeasFrequencyLow && low) {
        scheduleNewMeasCycle(false)
        //TODO SREI SosActor ! Change SensorML(lowfreq)
        sosActorSel ! """updatewizsml<>http://vocab.smart-project.info/sensorweb/procedure/koppl/wiztest01/p53_orthophosphate<>LOW"""
        // sosActorSel ! """updatewizsml<>http://vocab.smart-project.info/sensorweb/procedure/koppl/wiztest01/p52_bioavailable_phosphorus<>LOW"""
        // sosActorSel ! """updatewizsml<>http://vocab.smart-project.info/sensorweb/procedure/koppl/wiztest01/p51_total_phosphorus<>LOW"""
      }
      else {
        logger.info(s"Frequency low was already ${isMeasFrequencyLow }")
      }
      //FIXME SR on switch from high to low having the assignment here will give another high freq measurement as schedule checks for _measFrequencyLow. Move inside the IF.
      _measFrequencyLow = low
    }
    case _ => {
      logger.info("I'm so connected...")
    }
  }

  private def scheduleNewMeasCycle(immediate: Boolean = false): Cancellable = {
    def calcInterval: Long = {
      import com.github.nscala_time.time.Imports._
      if (DateTime.now < DateTime.now.hour(6).minute(0).second(0))
        (DateTime.now to DateTime.now.hour(6).minute(0).second(0)).millis / 1000
      else if (DateTime.now < DateTime.now.hour(12).minute(0).second(0))
        (DateTime.now to DateTime.now.hour(12).minute(0).second(0)).millis / 1000
      else if (DateTime.now < DateTime.now.hour(18).minute(0).second(0))
        (DateTime.now to DateTime.now.hour(18).minute(0).second(0)).millis / 1000
      else
        (DateTime.now to DateTime.tomorrow.hour(6).minute(0).second(0)).millis / 1000
    }
    val duration = if (immediate) 10.seconds
                   else if (_measFrequencyLow) calcInterval.seconds
                   else 20.minutes

    //TODO SREI set active methods here
    logger.info(s"MeasFrequencyLow ${_measFrequencyLow}")
    logger.info(s"Scheduling new measurement cycle to start in ${duration }")
    Akka.system().scheduler.scheduleOnce(duration, self, StartCompleteCycle)
  }
}
