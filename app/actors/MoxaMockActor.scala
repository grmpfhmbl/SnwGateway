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

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated }
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
import play.api.Logger
import scala.io.Source
import akka.util.ByteString
import play.api.Application
import scala.concurrent.duration._
import akka.actor.Scheduler

/**
 * this thing should go into test package soon, deactivate in Global.scala if you need to
 */
object MoxaMockActor {
  val ACTOR_NAME = "moxa-mock"
  def props(endpoint: InetSocketAddress, replies: ActorRef) =
    Props(classOf[MoxaMockActor], endpoint)
}

class MoxaMockActor(endpoint: InetSocketAddress) extends Actor {

  import context.system
  import play.api.libs.concurrent.Execution.Implicits._

  // actor internal state
  var client: ActorRef = null

  override def preStart(): Unit = {
    val host = endpoint.getHostName()
    val port = endpoint.getPort().toString
    Logger.debug(s"MoxaMockActor is now getting ready... trying to bind $host:$port ")
    IO(Tcp) ! Tcp.Bind(self, endpoint)
  }

  override def receive: Receive = {
    case Tcp.Connected(remote, _) => {

      val port = endpoint.getPort().toString()
      Logger.debug(s"Remote address $remote connected, we are $port")
      client = sender
      if (port.equalsIgnoreCase(play.Play.application.configuration.getString("sensorweb.moxa.spa.port"))) {
        val mockspa = context.actorOf(TcpEchoConnectionHandler.props(remote, sender), "mock-spa")
        sender() ! Tcp.Register(mockspa)
        Logger.debug(mockspa.toString())
        Logger.debug("registering new handler, setting up mock data for SPA")
        val cancelSpaMockStart = system.scheduler.schedule(10.seconds, 60.seconds, mockspa, SpaActorCommand(1, "pumptcpspa"))
      }
      if (port.equalsIgnoreCase(play.Play.application.configuration.getString("sensorweb.moxa.tarom.port"))) {
        val mocktarom = context.actorOf(TcpEchoConnectionHandler.props(remote, sender), "mock-tarom")
        Logger.debug(mocktarom.toString())
        sender() ! Tcp.Register(mocktarom)
        Logger.debug("registering new handler, setting up mock data for TAROM")
        val cancelSpaMockStart = system.scheduler.schedule(10.seconds, 60.seconds, mocktarom, TaromActorCommand(1, "pumptcptarom"))
      }
    }
    case SpaActorCommand(fieldid, value) => {
      value match {
        case "stop" => {
          val name = self.path.toString()
          Logger.info(s"shutting myself down: $name , hope my child the registered tcp handler mock-spa also stops, other wise we have to send shutdown message explicitely")
          context.stop(self)
        }
      }
    }
    case TaromActorCommand(fieldid, value) => {
      value match {
        case "stop" => {
          val name = self.path.toString()
          Logger.info(s"shutting myself down: $name , hope my child the registered tcp handler mock-tarom also stops, other wise we have to send shutdown message explicitely")
        }
      }
    }
  }
}

object TcpEchoConnectionHandler {

  def props(remote: InetSocketAddress, connection: ActorRef): Props =
    Props(new TcpEchoConnectionHandler(remote, connection))
}

class TcpEchoConnectionHandler(remote: InetSocketAddress, connection: ActorRef) extends Actor {

  import scala.language.postfixOps
  context.watch(connection) // We want to know when the connection dies without sending a `Tcp.ConnectionClosed`

  def receive: Receive = {
    case Tcp.Received(data) => {
      val text = data.utf8String.trim
      Logger.debug(s"Received $text from remote address $remote, not intended")
      text match {
        case "close" => context.stop(self)
        case _ => sender() ! Tcp.Write(data)
      }
    }
    case SpaActorCommand(fieldid, value) => {
      value match {
        case "pumptcpspa" => {

          val datafile = "public/files/spa_dump.txt"
          Logger.debug(s" trying to pump datafile via TCP MOCK to SpaACtor")
          for (line <- Source fromFile datafile getLines) {
            // Logger.debug(line)
            connection ! Tcp.Write(ByteString.fromString(line))
          }
        }
        case _ => Logger.debug(s"got unexpected SpaActorCommand message")
      }
    }
    case TaromActorCommand(fieldid, value) => {
      value match {
        case "pumptcptarom" => {

          val datafile = "public/files/solar-panel.txt"
          Logger.debug(s" trying to pump datafile via TCP MOCK to TaromActor")
          for (line <- Source fromFile datafile getLines) {
            // Logger.debug(line)
            connection ! Tcp.Write(ByteString.fromString(line))
          }

        }
        case _ => Logger.debug(s"got unexpected TaromActorCommand message")
      }
    }
    case _: Tcp.ConnectionClosed => {
      Logger.debug(s"Stopping, because connection for remote address $remote closed")
      context.stop(self)
    }
    case Terminated(`connection`) => {
      Logger.debug(s"Stopping, because connection for remote address $remote died")
      context.stop(self)
    }
  }
}
