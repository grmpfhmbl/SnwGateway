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
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.io.IO._
import akka.util.ByteString

object TaromActor {
  val ACTOR_NAME = "tarom"

  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[TaromActor], remote, replies)
}

// der referenzierte listener is der actor der dann die datenpakete weiterverarbeiten soll
// in Global.scala setzen bei instanzierung
class TaromActor(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import context.system

  // implicitly used by IO(Tcp)

  import Tcp._

  val manager = IO(Tcp)

  IO(Tcp) ! Connect(remote)

  lazy val dbActorSel = context.system.actorSelection("/user/" + DbActor.ActorName)

/*
  // TODO SREI what is this good for? It's never used?!
  val charlist: List[String] = List("#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8",
    "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F")
*/

  // internal actor state
  var messageBuilder = new StringBuilder()
  val CR: Int = 13
  val LF: Int = 10
  val NULLBYTE: Int = 0

  override def preStart(): Unit = {
    val host = remote.getHostName()
    val port = remote.getPort().toString
    Logger.debug(s"TaromActor is now getting ready... trying to open socket $host:$port ")
    dbActorSel ! LogDataMessage("info from TaromActor", s"TaromActor is now getting ready... trying to open socket $host:$port ")
  }

  def receive = {

    case CommandFailed(_: Connect) =>
      listener ! "TaromActor connect failed"
      Logger.debug("TaromActor connect failed")
      dbActorSel ! LogDataMessage("info from TaromActor", "TaromActor connect failed")
      context stop self

    case c@Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
          Logger.debug("TaromActor Write(data)")

        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "TaromActor write failed"
          Logger.debug("TaromActor write failed")

        case Received(data) =>

          // here muesste man for CRLF oder so schauen
          // um die Bytes dann wirklich zeilenweise auslesen zu koennne

          parseSendToDB(data)

        //          val length = data.length.toString
        //          listener ! s"TaromActor got $length bytes data"

        case "close" =>
          connection ! Close
          Logger.debug("TaromActor connection close")
          dbActorSel ! LogDataMessage("info from TaromActor", "TaromActor connection close")

        case _: ConnectionClosed =>
          listener ! "TaromActor connection closed"
          Logger.debug("TaromActor connection closed, shut down actor")
          dbActorSel ! LogDataMessage("info from TaromActor", "TaromActor connection closed, shut down actor")
          context stop self
      }
    case TaromActorCommand(fieldid, value) => {
      value match {
        case "stop" => {
          val name = self.path.toString()
          Logger.info(s"shutting myself down: $name ")
          dbActorSel ! LogDataMessage("info from TaromActor", s"shutting myself down: $name ")
          context.stop(self)
        }
        case _ => {
          Logger.debug(s"TaromActor got message: $value")
          dbActorSel ! LogDataMessage("info from TaromActor", s"TaromActor got other unhandled TaromActorCommand: $value")
        }
      }
    }
  }

  def ascii(bytes: ByteString): String = bytes.decodeString("US-ASCII")

  def parseSendToDB(datapacket: ByteString) {

    val textutf8 = datapacket.utf8String.trim
    val length = datapacket.length.toString
    Logger.debug(s"TaromActor got $length bytes")
    dbActorSel ! LogDataMessage("info from TaromActor", s"TaromActor got $length bytes")

    var resultbuilder = new StringBuilder()
    var resultbuilder2 = new StringBuilder()
    var resultbuilder3 = new StringBuilder()

    datapacket.foreach(abyte => {
      Logger.debug("original byte: " + abyte + " 0x" + Integer.toHexString(abyte))
      val byteAbs = Math.abs(abyte)
      Logger.debug("absolute val: " + byteAbs + " 0x" + Integer.toHexString(byteAbs))
      val intAbs = byteAbs << 24 | byteAbs << 16 | byteAbs << 8 | byteAbs
      Logger.debug("int val: " + intAbs + " 0x" + Integer.toHexString(intAbs))
      val intRot = Integer.rotateLeft(intAbs, 1) & 0x000000FF
      Logger.debug("rot val: " + intRot + " 0x" + Integer.toHexString(intRot))

      val aint = abyte.toInt
      var aintadd: Int = 0
      resultbuilder2.append(s"$aint  ")

      if (aint < 0) {
        //  128 - Math.abs(aint.toInt) + count die scheinen ziemlich gut :-p
        aintadd = (Math.abs(aint.toInt) - 1) / 2 // 128 - Math.abs(aint.toInt) + count

      } else if (aint > 0) {
        aintadd = (aint - 1) / 2 // 128 - aint + count
      } else {
        aintadd = aint // 0
      }

      resultbuilder3.append(s"$aintadd  ")

      var character = aintadd.toByte.toChar.toString()
      resultbuilder.append(character)

      messageBuilder.append(character)

      val checkarr = messageBuilder.toString.getBytes()

      if (checkarr.length > 3) {

        val last = checkarr.apply(checkarr.length - 1)
        val prelast = checkarr.apply(checkarr.length - 2)
        val preprelast = checkarr.apply(checkarr.length - 3)
        // Logger.debug(s"checkarr $preprelast $prelast $last")

        if (preprelast == CR && prelast == LF && last == NULLBYTE) {
          dbActorSel ! TaromDataMessage(1337, messageBuilder.toString.trim)
          messageBuilder.clear()
          messageBuilder.setLength(0)
        }
      }

    })

    //    val result = resultbuilder.toString()
    //    val result2 = resultbuilder2.toString()
    //    val result3 = resultbuilder3.toString()

    //      Logger.debug(s"# $count, $semicolon ;, $fits fits, $drops drops, $negs negs, $pos pos")
    //    Logger.debug(s"$result ($length bytes)")
    //    Logger.debug(s"org [ $result2 ]")
    //    Logger.debug(s"res [ $result3 ]")

  }
}