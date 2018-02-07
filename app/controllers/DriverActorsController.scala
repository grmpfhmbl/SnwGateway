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

package controllers

import scala.concurrent.duration._

import scala.concurrent.{ Await, Future }
import akka.actor.{ ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, Props }
import akka.pattern.AskableActorSelection
import akka.util.Timeout

import java.net.InetSocketAddress

import scala.io.Source
import scala.util.{ Failure, Success }
import play.api.Logger
import play.api.mvc.{ Action, Controller }
import actors._

import play.libs.Akka
import models._

object DriverActorsController extends Controller {

  def checkXbeeActor = Action {

    Logger.debug("asking xbee actor if alive")

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves
    val actorsel = Akka.system.actorSelection("/user/" + XBeeActor.ActorName)
    val dbActorSel = Akka.system.actorSelection("/user/" + DbActor.ActorName)

    dbActorSel ! LogDataMessage("DriverActorsController.checkXbeeActor", "asking xbee actor if alive")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "xbee actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.checkXbeeActor", msg)
      }
      case Failure(ex) => {
        val msg = "xbee actor dead"
        Logger.debug(msg)

        dbActorSel ! LogDataMessage("DriverActorsController.checkXbeeActor", msg)
      }
    }

    Ok(views.html.index(s"Enquiring XbeeActor status / please check the log"))
  }

  def reconnectXbeeActor = Action {

    Logger.debug("asking xbee actor if alive and if not trying to restart via supervisor.")

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves, if not then it's gone and we tell supervisor
    val actorsel = Akka.system.actorSelection("/user/"+XBeeActor.ActorName)
    val superVisorSel = Akka.system.actorSelection("/user/supervisor-actor")
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("DriverActorsController.reconnectXbeeActor", "asking xbee actor if alive and if not trying to restart via supervisor.")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "xbee actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.reconnectXbeeActor", msg)
      }
      case Failure(ex) => {
        val msg = "xbee actor dead, trying to restart"
        Logger.debug(msg)
        // superVisorSel ! "hello from controller, seems tarom is dead"
        dbActorSel ! LogDataMessage("DriverActorsController.reconnectXbeeActor", msg)
        superVisorSel ! XBeeActorCommand(1, "start")
      }
    }

    Ok(views.html.index(s"Enquiring XbeeActor status / please check the log"))
  }

  def callXBeeActor(command: String) = Action {

    Logger.debug(s"sending config message to XBeeActor: $command")

    import play.api.libs.concurrent.Execution.Implicits._
    val actorSel = Akka.system.actorSelection("/user/"+XBeeActor.ActorName)
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    if (command.equalsIgnoreCase("checkthread") || command.equalsIgnoreCase("startthread") || command.equalsIgnoreCase("endthread") || command.equalsIgnoreCase("stop")) {
      dbActorSel ! LogDataMessage("DriverActorsController.callXBeeActor", s"sending config message to XBeeActor: $command")

      actorSel ! XBeeActorCommand(1, command)
      Ok(views.html.index(s"pumping command $command from controller to XBeeActor / please check the log"))
    } else {
      Ok(views.html.index(s"XBeeCommand not allowd $command"))
    }

  }

  def checkTaromActor = Action {

    Logger.debug("asking tarom actor if alive")

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves
    val actorsel = Akka.system.actorSelection("/user/tarom-actor")

    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("DriverActorsController.checkTaromActor", "asking tarom actor if alive")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "tarom actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.checkTaromActor", msg)
      }
      case Failure(ex) => {
        val msg = "tarom actor dead"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.checkTaromActor", msg)
      }
    }
    Ok(views.html.index(s"Enquiring TaromActor status / please check the log"))

  }

  def reconnectTaromActor = Action {

    Logger.debug("asking tarom actor if alive and if not trying to restart  via supervisor.")

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves, if not then it's gone and we tell supervisor
    val actorsel = Akka.system.actorSelection("/user/tarom-actor")
    val superVisorSel = Akka.system.actorSelection("/user/supervisor-actor")

    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("DriverActorsController.reconnectTaromActor", "asking tarom actor if alive and if not trying to restart  via supervisor.")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "tarom actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.reconnectTaromActor", msg)
        Ok(views.html.index(s"TaromActor status: msg"))
      }
      case Failure(ex) => {
        val msg = "tarom actor dead, trying to restart"
        Logger.debug(msg)
        // superVisorSel ! "hello from controller, seems tarom is dead"
        dbActorSel ! LogDataMessage("DriverActorsController.reconnectTaromActor", msg)
        superVisorSel ! TaromActorCommand(1, "start")
        Ok(views.html.index(s"TaromActor status: msg"))
      }
    }
    Ok(views.html.index(s"Enquiring TaromActor status / please check the log"))

  }

  def callTaromActor(command: String) = Action {

    Logger.debug(s"sending config message to TaromActor: $command")

    import play.api.libs.concurrent.Execution.Implicits._
    val actorSel = Akka.system.actorSelection("/user/tarom-actor")
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    if (command.equalsIgnoreCase("stop")) {
      dbActorSel ! LogDataMessage("DriverActorsController.callTaromActor", s"sending config message to TaromActor: $command")

      actorSel ! TaromActorCommand(1, command)
      Ok(views.html.index(s"pumping command $command from controller to TaromActor / please check the log"))
    } else {
      Ok(views.html.index(s"TaromCommand not allowd $command"))
    }

  }

  def checkSpaActor = Action {

    Logger.debug("asking spa actor if alive")

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves
    val actorsel = Akka.system.actorSelection("/user/"+SpaActor.ACTOR_NAME)

    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("DriverActorsController.checkSpaActor", "asking spa actor if alive")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "spa actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.checkSpaActor", msg)
      }
      case Failure(ex) => {
        val msg = "spa actor dead"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.checkSpaActor", msg)
      }
    }

    Ok(views.html.index(s"Enquiring SpaActor status / please check the log"))
  }

  def reconnectSpaActor = Action {

    Logger.debug("asking spa actor if alive and if not trying to restart  via supervisor.")

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves, if not then it's gone and we tell supervisor
    val actorsel = Akka.system.actorSelection("/user/"+SpaActor.ACTOR_NAME)
    val superVisorSel = Akka.system.actorSelection("/user/supervisor-actor")

    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("DriverActorsController.reconnectSpaActor", "asking spa actor if alive and if not trying to restart  via supervisor.")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "spa actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("DriverActorsController.reconnectSpaActor", msg)
      }
      case Failure(ex) => {
        val msg = "spa actor dead, trying to restart"
        Logger.debug(msg)
        // superVisorSel ! "hello from controller, seems tarom is dead"
        dbActorSel ! LogDataMessage("DriverActorsController.reconnectSpaActor", msg)
        superVisorSel ! SpaActorCommand(1, "start")

      }
    }

    Ok(views.html.index(s"Enquiring SpaActor status / please check the log"))
  }

  def callSpaActor(command: String) = Action {

    Logger.debug(s"sending config message to SpaActor: $command")

    import play.api.libs.concurrent.Execution.Implicits._
    val actorSel = Akka.system.actorSelection("/user/"+SpaActor.ACTOR_NAME)
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    if (command.equalsIgnoreCase("stop")) {
      dbActorSel ! LogDataMessage("DriverActorsController.callSpaActor", s"sending config message to SpaActor: $command")

      actorSel ! SpaActorCommand(1, command)
      Ok(views.html.index(s"pumping command $command from controller to SpaActor / please check the log"))
    } else {
      Ok(views.html.index(s"SpaCommand not allowd $command"))
    }

  }

}