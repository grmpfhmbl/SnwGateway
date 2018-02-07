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
import akka.io.{ IO, Tcp }
import akka.io.IO._
import akka.util.ByteString

object SpaActor {
  val ACTOR_NAME = "spa"
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[SpaActor], remote, replies)
}

class SpaActor(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import context.system // implicitly used by IO(Tcp)
  import Tcp._

  //TODO SREI why is this here and why isn't it used?
//  val manager = IO(Tcp)

  lazy val dbActorSel = context.system.actorSelection("/user/" + DbActor.ActorName)

  IO(Tcp) ! Connect(remote)

  override def preStart(): Unit = {
    val host = remote.getHostName()
    val port = remote.getPort().toString
    Logger.debug(s"SpaActor is now getting ready... trying to open socket $host:$port ")
    dbActorSel ! LogDataMessage("info from SpaActor", s"SpaActor is now getting ready... trying to open socket $host:$port ")
  }

  def receive = {

    case CommandFailed(_: Connect) =>
      listener ! "SpaActor connect failed"
      Logger.debug("SpaActor connect failed")
      dbActorSel ! LogDataMessage("info from SpaActor", "SpaActor connect failed")
      context stop self

    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
          Logger.debug("SpaActor Write(data)")

        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "SpaActor write failed"
          Logger.debug("SpaActor write failed")

        case Received(data) =>

          // here muesste man for CRLF oder so schauen 
          // um die Bytes dann wirklich zeilenweise auslesen zu koennne

          parseSendToDB(data)

          val length = data.length.toString
          // Logger.debug(s"SpaActor got $length bytes data")
          // listener was/is the instantiating Actor (aka the supervisor, but we now only need to talk to db-actor)
          // listener ! s"SpaActor got $length bytes data"
          dbActorSel ! LogDataMessage("info from SpaActor", s"SpaActor got $length bytes data")

        case "close" =>
          connection ! Close
          Logger.debug("SpaActor connection close")
          dbActorSel ! LogDataMessage("info from SpaActor", "SpaActor connection close")

        case _: ConnectionClosed =>
          listener ! "SpaActor connection closed"
          Logger.debug("SpaActor connection closed, shut down actor")
          dbActorSel ! LogDataMessage("info from SpaActor", "SpaActor connection closed, shut down actor")
          context stop self
      }
    case SpaActorCommand(fieldid, value) => {
      value match {
        case "stop" => {
          val name = self.path.toString()
          Logger.info(s"shutting myself down: $name ")
          dbActorSel ! LogDataMessage("info from SpaActor", s"shutting myself down: $name")
          context.stop(self)
        }
        case _ => {
          Logger.debug(s"SpaActor got message: $value")
          dbActorSel ! LogDataMessage("info from SpaActor", s"SpaActor got other unhandled SpaActorCommand: $value")
        }
      }
    }
  }

  def ascii(bytes: ByteString): String = bytes.decodeString("US-ASCII")

  def parseSendToDB(datapacket: ByteString) {
    // val CRLF = ByteString("\r\n")
    //    val textascii = ascii(datapacket)
    //    dbActorSel ! SpaDataMessage(textascii)
    val textutf8 = datapacket.utf8String.trim

    for (element <- textutf8.split(";")) {
      dbActorSel ! SpaDataMessage(element.trim())
    }

  }

}
