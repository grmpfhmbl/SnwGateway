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
import play.api.Logger
import akka.actor.{ Actor, ActorRef, Props }
import akka.io.IO
import akka.util.ByteString
import utils.SpaCommLegacy

@deprecated
object SpaActorLegacy {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[SpaActorLegacy], remote, replies)
}

@deprecated
class SpaActorLegacy(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  // doch wieder irgendwie singleton pattern machen,
  // aber scala mag den private constructor wohl nicht und bildet singletons als object anstatt classess ab ...
  var spa: SpaCommLegacy = null
  var t: Thread = null

  lazy val dbActorSel = context.system.actorSelection("/user/" + DbActor.ActorName)

  def startSpaCommProcess() {
    spa = new SpaCommLegacy(remote.getHostName(), remote.getPort())
    t = new Thread(spa)
    t.start() // --- Start thread
  }

  def stopSpaCommProcess() {
    val afterburn: Thread = t
    t = null
    afterburn.interrupt()
  }

  override def preStart(): Unit = {
    val host = remote.getHostName()
    val port = remote.getPort().toString
    Logger.debug(s"SpaActorLegacy is now getting ready... trying to open socket $host:$port ")
    dbActorSel ! LogDataMessage("info from SpaActorLegacy", s"SpaActorLegacy is now getting ready... trying to open socket $host:$port")
    startSpaCommProcess()
  }

  def receive = {
    // TEST for general comms
    case message: String => {
      Logger.debug(s"SpaActorLegacy got message: $message")
      dbActorSel ! LogDataMessage("info from SpaActorLegacy", s"SpaActorLegacy got unknown message: $message")
    }
    case SpaActorCommand(fieldid, value) => {
      value match {
        case "startthread" => {
          Logger.debug("message wants new thread start")
          dbActorSel ! LogDataMessage("info from SpaActorLegacy", "message wants new thread start")
          if (spa == null && t == null) {
            Logger.debug("spacomm and thread are null, trying normal start")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm and thread are null, trying normal start")
            startSpaCommProcess()

          } else if (t != null || spa != null) {
            Logger.debug("there was some previous initialisation, trying full restart")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "there was some previous initialisation, trying full restart")
            stopSpaCommProcess()
            startSpaCommProcess()
          }
        }
        case "checkthread" => {
          Logger.debug("message wants to check thread start")
          dbActorSel ! LogDataMessage("info from SpaActorLegacy", "message wants to check thread start")
          if (t == null) {
            Logger.debug("spacomm thread is null")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm thread is null")
          } else if (t.isAlive()) {
            Logger.debug("spacomm thread seems alive")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm thread seems alive")
          } else {
            Logger.debug("spacomm thread seems dead or unknown but not null")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm thread seems dead or unknown but not null")
          }
        }
        case "endthread" => {
          Logger.debug("message wants to check thread start")
          dbActorSel ! LogDataMessage("info from SpaActorLegacy", "message wants to check thread start")
          if (t == null) {
            Logger.debug("spacomm thread is null")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm thread is null")
          } else if (t.isAlive()) {
            Logger.debug("spacomm thread seems alive - going to put him down")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm thread seems alive - going to put him down")
            stopSpaCommProcess()
          } else {
            Logger.debug("spacomm thread seems dead (or unknown)")
            dbActorSel ! LogDataMessage("info from SpaActorLegacy", "spacomm thread seems dead (or unknown)")
          }
        }
        case "stop" => {
          val name = self.path.toString()
          Logger.info(s"shutting myself down: $name ")
          dbActorSel ! LogDataMessage("info from SpaActorLegacy", s"shutting myself down: $name ")
          context.stop(self)
        }
        case _ => {
          Logger.debug(s"SpaActorLegacy got message: $value")
          dbActorSel ! LogDataMessage("info from SpaActorLegacy", s"SpaActorLegacy got other unhandled SpaActorCommand: $value")
        }
      }
    }
  }

}