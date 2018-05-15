package actors

import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
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

object ActorTarom {
  val ActorName = "tarom"

  def props(config: Configuration) = Props(new ActorTarom(config = config))

  case class CmdConnect()

  case class CmdProcessData(line: String)

  val SENSID_VERSION                 = 330
  val SENSID_DATE                    = 331
  val SENSID_TIME                    = 332
  val SENSID_BAT_VOLTAGE             = 333
  val SENSID_MODULE_VOLT_1           = 334
  val SENSID_MODULE_VOLT_2           = 335
  val SENSID_STATE_OF_CHARGE         = 336
  val SENSID_STATE_OF_HEALTH         = 337
  val SENSID_TOTAL_BAT_CURRENT       = 338
  val SENSID_MAX_IN_CURRENT_MODULE_1 = 339
  val SENSID_MAX_IN_CURRENT_MODULE_2 = 340
  val SENSID_MOMENTARY_IN_CURRENT    = 341
  val SENSID_TOTAL_CHARGE_CURRENT    = 342
  val SENSID_DEVICE_LOAD_CURRENT     = 343
  val SENSID_TOTAL_DISCHARGE_CURRENT = 344
  val SENSID_TEMPERATURE_BAT_SENSOR  = 345
  val SENSID_ERROR_STATUS            = 346
  val SENSID_CHARGING_MODE           = 347
  val SENSID_LOAD                    = 348
  val SENSID_AUX_1                   = 349
  val SENSID_AUX_2                   = 350
  val SENSID_MAX_AH_IN_BAT_24H       = 351
  val SENSID_MAX_AH_IN_BAT_ALL       = 352
  val SENSID_MAX_AH_LOAD_24H         = 353
  val SENSID_MAX_AH_LOAD_ALL         = 354
  val SENSID_DERATING                = 355
  val SENSID_CRC                     = 356
}


class ActorTarom(config: Configuration) extends Actor with MyLogger {

  private lazy val DATE_FORMAT = new DateTimeFormatterBuilder()
    .appendPattern("yyyy/MM/dd HH:mm")
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
  assert(serialNumber.isDefined, "tarom.serialnumber must be set in configuration.")

  private var serialOperator: Option[ActorRef] = None
  private var dataBufferString: String = ""

  override def preStart() {
    logger.info("Starting ActorTarom")
    logger.info("Searching Tarom node in database...")

    val nodes = SensorNode.getSensorNodeByExtendedAddress(serialNumber.get)

    if (nodes.isEmpty) {
      throw new IllegalStateException(s"Could not find Tarom in database. Make sure extended address in Sensor_Nodes " +
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
          s"Cannot connect via $default. Probably configuration error. Check 'tarom.readfrom' value in configuration.")
        throw new IllegalStateException(
          s"Cannot connect via $default. Probably configuration error. Check 'tarom.readfrom' value in configuration.")
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
          s"Cannot connect via $default. Probably configuration error. Check 'tarom.readfrom' value in configuration.")
        throw new IllegalStateException(
          s"Cannot connect via $default. Probably configuration error. Check 'tarom.readfrom' value in configuration.")
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
    val EOL = "\\r?\\n"
    val strData = data.decodeString(StandardCharsets.US_ASCII.name())
    logger.debug(s"Received ${data.length} bytes of data.")
    logger.trace(s"Decoded to:\n'$strData'")
    dataBufferString += strData;
    logger.trace(s"Updated data buffer\n'$dataBufferString'")

    val lastEOL = dataBufferString.lastIndexOf("\n")
    val splitBuffer = dataBufferString.splitAt(lastEOL)
    dataBufferString = splitBuffer._2.drop(1) //splitAt() leaves the \n at the beginning and we don't want it.
    val linesToProcess = splitBuffer._1.split(EOL)

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
      dbActorSel ! XBeeDataMessage(serialNumber.get, sensId, Timestamp.valueOf(timestamp), values(sensId - 330).toDouble, values(sensId - 330).toDouble)
    }

    logger.debug(s"Processing $line")
    if (!line.matches("^1;(?:[^;]+?;){25}[0-9A-F]{4}")) {
      logger.warn(s"Could not process $line because of invalid format. Ignoring.")
    }
    //TODO CRC Check
    // see https://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java and
    // https://en.wikipedia.org/wiki/Cyclic_redundancy_check <- search for 0cx8408
    else {
      val values = line.split(";")
      try {
        val timestamp = LocalDateTime.parse(values(1) + " " + values(2), DATE_FORMAT)
        logger.debug(s"Timestamp: $timestamp")

        sendMessage(ActorTarom.SENSID_BAT_VOLTAGE, timestamp, values)
        sendMessage(ActorTarom.SENSID_MODULE_VOLT_1, timestamp, values)
        sendMessage(ActorTarom.SENSID_STATE_OF_CHARGE, timestamp, values)
        sendMessage(ActorTarom.SENSID_TOTAL_BAT_CURRENT, timestamp, values)
        sendMessage(ActorTarom.SENSID_MAX_IN_CURRENT_MODULE_1, timestamp, values)
        sendMessage(ActorTarom.SENSID_MOMENTARY_IN_CURRENT, timestamp, values)
        sendMessage(ActorTarom.SENSID_TOTAL_CHARGE_CURRENT, timestamp, values)
        sendMessage(ActorTarom.SENSID_DEVICE_LOAD_CURRENT, timestamp, values)
        sendMessage(ActorTarom.SENSID_TOTAL_DISCHARGE_CURRENT, timestamp, values)
        sendMessage(ActorTarom.SENSID_TEMPERATURE_BAT_SENSOR, timestamp, values)
        sendMessage(ActorTarom.SENSID_ERROR_STATUS, timestamp, values)
        sendMessage(ActorTarom.SENSID_LOAD, timestamp, values)
        sendMessage(ActorTarom.SENSID_AUX_1, timestamp, values)
        sendMessage(ActorTarom.SENSID_AUX_2, timestamp, values)
        sendMessage(ActorTarom.SENSID_MAX_AH_IN_BAT_24H, timestamp, values)
        sendMessage(ActorTarom.SENSID_MAX_AH_IN_BAT_ALL, timestamp, values)
        sendMessage(ActorTarom.SENSID_MAX_AH_LOAD_24H, timestamp, values)
        sendMessage(ActorTarom.SENSID_MAX_AH_LOAD_ALL, timestamp, values)
        sendMessage(ActorTarom.SENSID_DERATING, timestamp, values)
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