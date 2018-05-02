package actors

import actors.ActorSupervisor.CmdStatus
import actors.ActorTarom.CmdConnect
import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import akka.io.IO
import akka.serial.{AccessDeniedException, Parity, Serial, SerialSettings}
import play.api.Configuration
import utils.MyLogger

object ActorTarom {
  val ActorName = "tarom"

  def props(config: Configuration) = Props(new ActorTarom(config = config))
  case class CmdConnect()
}


class ActorTarom(config: Configuration) extends Actor with MyLogger {

  lazy val dbActorSel: ActorSelection = context.system.actorSelection(
    s"/user/${ActorSupervisor.ActorName}/${DbActor.ActorName}")

  lazy val readFrom = config.getString("readfrom").getOrElse("serial")
  assert(readFrom.trim().equals("serial") || readFrom.trim.equals("tcp"), s"tarom.readfrom must either be 'serial' or 'tcp' but was $readFrom")
  private def isSerial = readFrom.trim().equals("serial")

  lazy val serialPort = config.getString("serial.port").getOrElse("dev/ttyUSB0")
  val serialSettings = SerialSettings(
    baud = config.getInt("serial.baud").getOrElse(9600),
    characterSize = 8,
    twoStopBits = false,
    parity = Parity.None
  )

  private var serialOperator: Option[ActorRef] = None


  override def preStart() {
    logger.info("Starting ActorTarom")
    connect()
  }

  //default behaviour is disconnected
  override def receive: Receive = disconnected

  /**
    * Actor behaviour when disconnected. Waits for connection being established.
    * @return
    */
  def disconnected: Receive = {
    case CmdStatus => {
      sender ! "Disconnected"
    }

    case CmdConnect => {
      connect()
    }

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
    * @return
    */
  def connected: Receive = {
    //FIXME make decide wether serial of TCP connection
    case CmdStatus => sender ! "Connected."
    case Serial.Received(data) => logger.info("Received data: " + data.toString)
    case default => logger.warn(s"Received Message $default, but cannot handle.")
  }

  /**
    * Establishes connection according to configuration.
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
        logger.error(s"Cannot connect via $default. Probably configuration error. Check 'tarom.readfrom' value in configuration.")
        throw new IllegalStateException(s"Cannot connect via $default. Probably configuration error. Check 'tarom.readfrom' value in configuration.")
      }
    }
  }
}