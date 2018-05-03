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

import actors.TaromActor
import akka.actor.Actor
import play.api.Logger
import java.net.InetSocketAddress
import play.api.{ GlobalSettings, Logger }
import play.api.Play.current
import play.api.cache.Cache
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.io.{ IO, Tcp }
import akka.io.Tcp.{ Bind, Bound, CommandFailed, Connected }
import play.libs.Akka
import scala.concurrent.duration._
import akka.actor.Scheduler
import scala.io.Source
import scala.util.Try
import play.api.Application

@Deprecated
case class DbActorCommand(fieldid: Int, value: String)
@Deprecated
case class TaromActorCommand(fieldid: Int, value: String)
@Deprecated
case class SpaActorCommand(fieldid: Int, value: String)
@Deprecated
case class XBeeActorCommand(fieldid: Int, value: String)
@Deprecated
case class SosActorCommand(fieldid: Int, value: String)

// like a central logging daemon, so actors dont necessarily need to write log by themselves
@Deprecated
class SupervisorActor extends Actor {

  // TODO hier koennten wir die moxas und taroms etc aus der CONFIG auslesen
  // und entsprechend die actors generieren
  // TODO SREI use props to configure actor! otherwise testing might become difficult
  var moxaSpaPort: InetSocketAddress = new InetSocketAddress(play.Play.application.configuration.getString("sensorweb.moxa.base.ip"), play.Play.application.configuration.getString("sensorweb.moxa.spa.port").toInt)
  var moxaTaromPort: InetSocketAddress = new InetSocketAddress(play.Play.application.configuration.getString("sensorweb.moxa.base.ip"), play.Play.application.configuration.getString("sensorweb.moxa.tarom.port").toInt)

  var xbeeComport = play.Play.application.configuration.getString("sensorweb.xbee.gateway.comport")
  //  val xbeeComport = "/dev/ttyS0"

  var dbActor: ActorRef = null

  override def preStart(): Unit = {
    Logger.debug("SupervisorActor is now getting ready...")
    Logger.debug("supervisior wants to start db actor now")
    dbActor = Akka.system.actorOf(Props[DbActor], DbActor.ActorName)
  }

  def receive = {

    // TEST for general comms
    case message: String => {
      Logger.debug(s"supervisor-actor got message: $message")
      dbActor ! LogDataMessage("info from supervisor", s"supervisor-actor got random message: $message")
    }

    // DB and Parser
    case DbActorCommand(fieldid, value) => {
      value match {
        case "start" => {
          Logger.info("supervisior wants to start db actor now")
          Logger.warn("gibbet nich!")
          //val dbActor = Akka.system.actorOf(Props[DbActor], DbActor.ACTOR_NAME)
        }
        case "stop" => {
          // why would we stop the DB actor?
          Logger.debug("supervisior wants to stop db actor now, should not, should it?")
          // by sending message to the DBActor which can then shutdown itself .. but DB should stay active
        }
      }
    }
    // TAROM
    case TaromActorCommand(fieldid, value) => {
      value match {
        case "start" => {
          Logger.debug("supervisior wants to start tarom actor now")
          val taromActor = Akka.system.actorOf(TaromActor.props(moxaTaromPort, self), TaromActor.ACTOR_NAME)
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to start tarom actor now")
        }
        case "stop" => {
          Logger.debug("supervisior wants to stop tarom actor now")
          // by sending message to the Actor which can then shutdown itself .. but DB should stay active
          val shutDownActor = Akka.system.actorSelection("/user/" + TaromActor.ACTOR_NAME)
          shutDownActor ! TaromActorCommand(1, "stop")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to stop tarom actor now")
        }
        case "startmoxamock" => {
          Logger.debug("supervisior wants to start moxamock actor for TAROM  now")
          val moxaMockTaromActor = Akka.system.actorOf(MoxaMockActor.props(moxaTaromPort, self), MoxaMockActor.ACTOR_NAME +"tarom")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to start moxamock actor for TAROM  now")
        }
        case "stopmoxamock" => {
          Logger.debug("supervisior wants to stop moxamock actor for TAROM  now")
          // by sending message to the Actor which can then shutdown itself .. but DB should stay active
          val shutDownActor = Akka.system.actorSelection("/user/"+MoxaMockActor.ACTOR_NAME +"tarom")
          shutDownActor ! TaromActorCommand(1, "stop")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to stop moxamock actor for TAROM  now")
        }

      }
    }
    // SPA
    case SpaActorCommand(fieldid, value) => {
      value match {
        case "start" => {
          Logger.debug("supervisior wants to start spa actor now")
          // val spaActor = Akka.system.actorOf(SpaActor.props(moxaSpaPort, self), "spa-actor")
          Logger.debug("beware we use spa LEGACY actor")
          val spaActorLegacy = Akka.system.actorOf(SpaActorLegacy.props(moxaSpaPort, self), SpaActor.ACTOR_NAME)
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to start spa actor now")
          dbActor ! LogDataMessage("info from supervisor", "beware we use spa LEGACY actor")
        }
        case "stop" => {
          Logger.debug("supervisior wants to stop spa actor now")
          // by sending message to the Actor which can then shutdown itself .. but DB should stay active
          val shutDownActor = Akka.system.actorSelection("/user/" + SpaActor.ACTOR_NAME)
          shutDownActor ! SpaActorCommand(1, "stop")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to stop spa actor now")
        }
        case "startmoxamock" => {
          Logger.debug("supervisior wants to start moxamock actor for SPA  now")
          val moxaMockSpaActor = Akka.system.actorOf(MoxaMockActor.props(moxaSpaPort, self), MoxaMockActor.ACTOR_NAME +"spa")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to start moxamock actor for SPA  now")
        }
        case "stopmoxamock" => {
          Logger.debug("supervisior wants to stop moxamock actor for SPA  now")
          // by sending message to the Actor which can then shutdown itself .. but DB should stay active
          val shutDownActor = Akka.system.actorSelection("/user/" + MoxaMockActor.ACTOR_NAME +"spa")
          shutDownActor ! SpaActorCommand(1, "stop")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to stop moxamock actor for SPA  now")
        }

      }
    }
    // XBEE
    case XBeeActorCommand(fieldid, value) => {
      value match {
        case "start" => {
/*
          Logger.debug("supervisior wants to start xbee actor now")
          val xbeeActor = Akka.system.actorOf(XBeeActor.props(xbeeComport, self), XBeeActor.ActorName)
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to start xbee actor now")
*/
        }
        case "stop" => {
          Logger.debug("supervisior wants to stop xbee actor now")
          val shutDownActor = Akka.system.actorSelection("/user/" + XBeeActor.ActorName)
          shutDownActor ! XBeeActorCommand(1, "stop")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to stop xbee actor now")
        }
      }
    }
    // SOS Uplink
    case SosActorCommand(fieldid, value) => {
      value match {
        case "start" => {
          Logger.debug("supervisior wants to start sos actor now")
          val sosActor = Akka.system.actorOf(Props[SosActor], SosActor.ActorName)
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to start sos actor now")
        }
        case "stop" => {
          Logger.debug("supervisior wants to stop sos actor now")
          val shutDownActor = Akka.system.actorSelection("/user/" + SosActor.ActorName)
          shutDownActor ! SosActorCommand(1, "stop")
          dbActor ! LogDataMessage("info from supervisor", "supervisior wants to stop sos actor now")
        }
      }
    }
  }
}