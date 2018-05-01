package actors

import actors.ActorSupervisor.CmdStatus
import akka.actor.{Actor, ActorSelection, Props}
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

  override def preStart() {
    logger.info("Starting ActorTarom")
  }

  override def receive: Receive = {
    case CmdStatus => {
      sender ! "Alive and rockin!!!!"
    }

    case _ => logger.error("implement me")
  }

  private def connect() = {

  }
}