package actors

import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.ChronoField

import actors.ActorSupervisor.CmdStatus
import actors.ActorTarom.{CmdConnect, CmdProcessData}
import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import akka.io.IO
import akka.serial.{AccessDeniedException, Parity, Serial, SerialSettings}
import akka.util.ByteString
import models.SensorNode
import play.api.Configuration
import utils.MyLogger

object ActorSontekIq {
  val ActorName = "sontek-iq"

  def props(config: Configuration) = Props(new ActorSontekIq(config = config))

  case class CmdConnect()

  case class CmdProcessData(line: String)

  val SENS_ID_SONTEK_IQ_ID = 401
  val SENS_ID_SAMPLE_NUMBER = 402
  val SENS_ID_YEAR = 403
  val SENS_ID_MONTH = 404
  val SENS_ID_DAY = 405
  val SENS_ID_HOUR = 406
  val SENS_ID_MINUTE = 407
  val SENS_ID_SECOND = 408
  val SENS_ID_FLOW_RATE = 409
  val SENS_ID_STAGE = 410
  val SENS_ID_MEAN_VELOCITY = 411
  val SENS_ID_VOLUME_TOTAL = 412
  val SENS_ID_DEPTH = 413
  val SENS_ID_INDEX_VELOCITY = 414
  val SENS_ID_AREA = 415
  val SENS_ID_TEMPERATURE = 416
  val SENS_ID_SYSTEM_STATUS = 417
  val SENS_ID_VELOCITY_XZ_X_CENTER = 418
  val SENS_ID_VELOCITY_XZ_Z_CENTER = 419
  val SENS_ID_VELOCITY_XZ_X_LEFT = 420
  val SENS_ID_VELOCITY_XZ_X_RIGHT = 421
  val SENS_ID_BATTERY = 422
  val SENS_ID_PITCH = 423
  val SENS_ID_ROLL = 424
  val SENS_ID_SYSTEM_IN_WATER = 425
  val SENS_ID_RANGE = 426
  val SENS_ID_ADJUSTED_PRESSURE = 427
  val SENS_ID_POSITIVE_VOLUME = 428
  val SENS_ID_NEGATIVE_VOLUME = 429
  val SENS_ID_CELL_END = 430
  val SENS_ID_SNR_BEAM_1 = 431
  val SENS_ID_SNR_BEAM_2 = 432
  val SENS_ID_SNR_BEAM_3 = 433
  val SENS_ID_SNR_BEAM_4 = 434
}


class ActorSontekIq(config: Configuration) extends Actor with MyLogger {

  private lazy val DATE_FORMAT = new DateTimeFormatterBuilder()
    .appendPattern("yyyy/MM/dd HH:mm:ss")
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter()


  private lazy val dbActorSel: ActorSelection = context.system.actorSelection(
    s"/user/${ActorSupervisor.ActorName}/${DbActor.ActorName}")

  private lazy val readFrom = config.getString("readfrom").getOrElse("serial")
  assert(readFrom.trim().equals("serial") || readFrom.trim.equals("tcp"),
    s"tarom.readfrom must either be 'serial' or 'tcp' but was $readFrom")
  private lazy val serialPort = config.getString("serial.port").getOrElse("dev/ttyUSB0")
  private lazy val serialSettings = SerialSettings(
    baud = config.getInt("serial.baud").getOrElse(9600),
    characterSize = 8,
    twoStopBits = false,
    parity = Parity.None
  )

  private val serialNumber = config.getString("serialnumber")
  assert(serialNumber.isDefined, "sontekIq.serialnumber must be set in configuration.")

  private var serialOperator: Option[ActorRef] = None
  private var dataBufferString: String = ""

  override def preStart() {
    logger.info("Starting ActorSontekIq")
    logger.info("Searching Sontek node in database...")

    val nodes = SensorNode.getSensorNodeByExtendedAddress(serialNumber.get)

    if (nodes.isEmpty) {
      throw new IllegalStateException(
        s"Could not find Sontek IQ in database. Make sure extended address in SensorNodes " +
          s"equals configured serial number '${serialNumber.get}.")
    }
    else if (nodes.length > 1) {
      throw new IllegalStateException(s"Found more than one node with address $serialNumber.")
    }

    connect()
  }

  override def postStop(): Unit = {
    super.postStop()
    disconnect()
  }

  /**
    * Establishes connection according to configuration.
    *
    * @return
    */
  private def connect() = {
    readFrom match {
      case "serial" => {
        import context.system
        logger.info(s"Trying to connect to $serialPort...")
        IO(Serial) ! Serial.Open(serialPort, serialSettings)
      }
      case "tcp" => throw new NotImplementedError("Connect via TCP is not implemented yet.")
      case default => {
        logger.error(
          s"Cannot connect via $default. Probably configuration error. Check 'sontekIq.readfrom' value in configuration.")
        throw new IllegalStateException(
          s"Cannot connect via $default. Probably configuration error. Check 'sontekIq.readfrom' value in configuration.")
      }
    }
  }

  private def disconnect(): Unit = {
    readFrom match {
      case "serial" => {
        if (serialOperator.isDefined) {
          serialOperator.get ! Serial.Close
        }
      }
      case "tcp" => throw new NotImplementedError("Disconnect via TCP is not implemented yet.")
      case default => {
        logger.error(
          s"Cannot connect via $default. Probably configuration error. Check 'sontekIq.readfrom' value in configuration.")
        throw new IllegalStateException(
          s"Cannot connect via $default. Probably configuration error. Check 'sontekIq.readfrom' value in configuration.")
      }
    }
  }

  //default behaviour is disconnected
  override def receive: Receive = disconnected

  /**
    * Actor behaviour when disconnected. Waits for connection being established.
    *
    * @return
    */
  def disconnected: Receive = {
    case CmdStatus => sender ! "Disconnected"

    case CmdConnect => connect()

    //Akka-Serial connection responses
    case Serial.CommandFailed(cmd: Serial.Open, reason: AccessDeniedException) =>
      logger.error(s"Could not connect to $serialPort. Reason: Insufficient privileges.", reason)
    case Serial.CommandFailed(cmd: Serial.Open, reason) =>
      logger.error(s"Could not connect to $serialPort. Reason: ${reason.getMessage}", reason)
    case Serial.Opened(settings) => {
      logger.info(s"Connected to $serialPort.")
      this.serialOperator = Some(sender)
      context.become(connected)
    }

    case default => logger.warn(s"Received Message $default, but cannot handle.")
  }

  /**
    * Actor behaviour when connected. Handles incoming data and reconnects if necessary
    *
    * @return
    */
  def connected: Receive = {
    //FIXME make decide wether serial of TCP connection
    case CmdStatus => sender ! "Connected."
    case Serial.Received(data) => queueNewData(data)
    case CmdProcessData(line) => processLine(line)

    case default => logger.warn(s"Received Message $default, but cannot handle.")
  }

  /**
    * Puts new data in receiving queue and sends CmdProcessData() for every complete line to self
    *
    * @param data
    */
  private def queueNewData(data: ByteString) = {
    //TODO maybe instead of all this string-decode / regex magic just itereate over the ByteString and look for 0x10
    val strData = data.decodeString(StandardCharsets.US_ASCII.name()).replaceAll("\r", "")
    logger.debug(s"Received ${data.length} bytes of data.")
    logger.trace(s"Decoded to:\n'$strData'")
    dataBufferString += strData;
    logger.trace(s"Updated data buffer\n'$dataBufferString'")

    val lastEOL = dataBufferString.lastIndexOf("\n")
    val splitBuffer = dataBufferString.splitAt(lastEOL)
    dataBufferString = splitBuffer._2.drop(1) //splitAt() leaves the \n at the beginning and we don't want it.
    val linesToProcess = splitBuffer._1.split("\n")

    logger.debug(s"Data to process:\n'${splitBuffer._1}'")
    logger.debug(s"Remaining buffer:\n'${dataBufferString}'")
    logger.info(s"Got ${linesToProcess.length} complete lines of data. ${dataBufferString.length}" +
      s"bytes remaining in buffer.")

    //process all complete lines
    linesToProcess.foreach(line => self ! CmdProcessData(line))
  }

  def processLine(line: String): Unit = {
    /** little helper function to get the correct value out of the values array and send the message to db */
    def sendMessage(sensId: Int, timestamp: LocalDateTime, values: Array[String]) = {
      //TODO some values (module 2) can have "#" as value if they are not enabled. Parse for it here?
      dbActorSel ! XBeeDataMessage(serialNumber.get, sensId, Timestamp.valueOf(timestamp),
        values(sensId - 401).toDouble, values(sensId - 401).toDouble)
    }

    //logger.debug(s"Processing $line")
    if (!line.matches("^(-?\\d+(?:\\.\\d+)?,?){34}$")) {
      logger.warn(s"Could not process '$line'. Invalid format. Ignoring.")
    }
    else {
      val values = line.split(",")
      try {
        // val timestamp = LocalDateTime.parse(values(1) + " " + values(2), DATE_FORMAT)
        val timestamp = LocalDateTime.parse(
          s"${values(2)}/${values(3)}/${values(4)} ${values(5)}:${values(6)}:${values(7)}", DATE_FORMAT)
        sendMessage(ActorSontekIq.SENS_ID_SAMPLE_NUMBER, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_FLOW_RATE, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_STAGE, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_MEAN_VELOCITY, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_VOLUME_TOTAL, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_DEPTH, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_INDEX_VELOCITY, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_AREA, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_TEMPERATURE, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_SYSTEM_STATUS, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_VELOCITY_XZ_X_CENTER, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_VELOCITY_XZ_Z_CENTER, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_VELOCITY_XZ_X_LEFT, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_VELOCITY_XZ_X_RIGHT, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_BATTERY, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_PITCH, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_ROLL, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_SYSTEM_IN_WATER, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_RANGE, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_ADJUSTED_PRESSURE, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_POSITIVE_VOLUME, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_NEGATIVE_VOLUME, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_CELL_END, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_SNR_BEAM_1, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_SNR_BEAM_2, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_SNR_BEAM_3, timestamp, values)
        sendMessage(ActorSontekIq.SENS_ID_SNR_BEAM_4, timestamp, values)
      }
      catch {
        case ex: DateTimeParseException => logger.warn(s"Could not parse date. Reason: ${ex.getMessage}", ex)
        case ex: NumberFormatException => logger.warn(s"Could not parse number. Reason: ${ex.getMessage}", ex)
      }
    }
  }

  /** Checks if configured for serial connection
    *
    * @return true if configured for serial connection otherwise false.
    */
  private def isSerial = readFrom.trim().equals("serial")
}