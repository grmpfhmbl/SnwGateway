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

import actors.ActorSupervisor.CmdGetOrStart
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import play.api.{Configuration, Logger}
import utils.{MyLogger, WaspProcess}

import scala.concurrent.Await
import scala.concurrent.duration._

object XBeeActor {
  val ActorName = "xbee"

  def props(config: Configuration) = Props(new XBeeActor(config))
}

//TODO SREI implicit val DbActor, so the actor does not have to find the Database by itself
class XBeeActor(config: Configuration) extends Actor with MyLogger {
  val Comport = config.getString("gateway.comport").getOrElse("/dev/ttyUSB0")

  // doch wieder irgendwie singleton pattern machen,
  // aber scala mag den private constructor wohl nicht und bildet singletons als object anstatt classess ab ...
  var waspi: WaspProcess = null
  var t: Thread = null

  //TODO SREI the name has technically to be injected via props, or we just assume, that out parent is always the supervisor?
  lazy val actorSupervisor = context.system.actorSelection("/user/" + ActorSupervisor.ActorName)
/*
  //TODO SREI I'm unsure if I like this. It feels wrong, but it works for now. At least the asInstanceOf sucks :-)
  lazy val dbActorSel: ActorRef = {
    implicit val timeout = Timeout(2.seconds)
    Await.result(actorSupervisor ? CmdGetOrStart(DbActor.ActorName), 10.seconds).asInstanceOf[ActorRef]
  }
*/

  /**
   * alternatively try something different :-p
   *
   * ... e.g. https://github.com/jodersky/flow
   */

  def startWaspProcess() {
    waspi = new WaspProcess(Comport)
    t = new Thread(waspi)
    t.start() // --- Start thread
  }

  //TODO SREI we need to close the serial device properly, otherwise we're fucked
  def stopWaspProcess() {
    val afterburn: Thread = t
    t = null
    afterburn.interrupt()
  }

  override def preStart(): Unit = {
    logger.debug(s"XBeeActor is now getting ready... initialising waspi thread with $Comport")
    startWaspProcess()
  }

  def receive = {
    // TEST for general comms
    case message: String => {
      logger.debug(s"XBeeActor got message: $message")
    }
    case XBeeActorCommand(fieldid, value) => {
      value match {
        case "startthread" => {
          logger.debug("message wants new thread start")
          if (waspi == null && t == null) {
            logger.debug("waspi and thread are null, trying normal start")
            startWaspProcess()
          } else if (t != null || waspi != null) {
            logger.debug("there was some previous initialisation, trying full restart")
            stopWaspProcess()
            startWaspProcess()
          }
        }
        case "checkthread" => {
          logger.debug("message wants to check thread start")
          if (t == null) {
            logger.debug("waspi thread is null")
          } else if (t.isAlive()) {
            logger.debug("waspi thread seems alive")
          } else {
            logger.debug("waspi thread seems dead or unknown but not null")
          }
        }
        case "endthread" => {
          logger.debug("message wants to check thread start")
          if (t == null) {
            logger.debug("waspi thread is null")
          } else if (t.isAlive()) {
            logger.debug("waspi thread seems alive - going to put him down")
            stopWaspProcess()
          } else {
            logger.debug("waspi thread seems dead (or unknown)")
          }
        }
        case "stop" => {
          val name = self.path.toString()
          logger.info(s"shutting myself down: $name ")
          context.stop(self)
        }
        case _ => {
          logger.debug(s"XBeeActor got message: $value")
        }
      }
    }
  }
}