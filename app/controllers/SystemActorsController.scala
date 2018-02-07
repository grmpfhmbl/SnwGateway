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
import actors.{ TaromActorCommand, XBeeActorCommand, SpaActorCommand }
import models._

object SystemActorsController extends Controller {

  def callLogActor(name: String) = Action {

    Logger.debug(s"hello $name")

    import play.api.libs.concurrent.Execution.Implicits._
    val actor = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    actor ! LogDataMessage("SystemActorsController.callLogActor", s"hello $name from callLogActor controller")

    Ok(views.html.index(s"pumping hello $name from controller to supervisoractor / please check the log"))
  }

  def callDbActor(demo: String) = Action {

    // suggested imports for visibility (otherwise compile warnigns)
    import scala.language.postfixOps
    import play.api.libs.concurrent.Execution.Implicits._

    val dbactor = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    val charlist: List[String] = List("#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8",
      "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F")

    val ret_file = demo match {
      case "tarom" => {

        val ret = "public/files/solar-panel.txt"
        Logger.debug(s" trying to pump $ret to dbactor")
        dbactor ! LogDataMessage("SystemActorsController.callDbActor", s" trying to pump $ret to dbactor")

        // poll data from sensor  , man koennte hier besser Try / GetOrElse oder sowas machen  
        for (line <- Source fromFile ret getLines) {
          // Logger.debug("ping /user/supervisor-actor/"+DbActor.ACTOR_NAME)
          dbactor ! TaromDataMessage(-1, line)

        }

        // finally yield a return value
        ret
      }
      case "spa" => {
        val ret = "public/files/spa_dump.txt"
        Logger.debug(s" trying to pump $ret to dbactor")
        dbactor ! LogDataMessage("SystemActorsController.callDbActor", s" trying to pump $ret to dbactor")
        // poll data from sensor  , man koennte hier besser Try / GetOrElse oder sowas machen  
        for (line <- Source fromFile ret getLines) {
          // Logger.debug(line)
          dbactor ! SpaDataMessage(line)
        }

        // finally yield a return value
        ret
      }
    }

    Ok(views.html.index(s" trying to pump $ret_file to dbactor / please check the log or the measurement list"))
  }

  def callSosActor(command: String) = Action {

    Logger.debug(s"sending config message to SosActor: $command")

    import play.api.libs.concurrent.Execution.Implicits._
    val actorSel = Akka.system.actorSelection("/user/"+SosActor.ActorName)
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    if (command.equalsIgnoreCase("config") || command.equalsIgnoreCase("bulkupload") || command.equalsIgnoreCase("redeemupload")
      || command.equalsIgnoreCase("bulkuploadtext") || command.equalsIgnoreCase("redeemuploadtext")
      || command.equalsIgnoreCase("checkcapa") || command.equalsIgnoreCase("stop")) {
      dbActorSel ! LogDataMessage("SystemActorsController.callSosActor", s"sending config message to SosActor: $command")

      actorSel ! SosActorCommand(1, command)
      Ok(views.html.index(s"pumping command $command from controller to SosActor / please check the log"))
    } else {
      Ok(views.html.index(s"Command not allowd $command"))
    }

  }

  def checkSosActor = Action {

    Logger.debug("asking SOS actor if alive")

    import scala.concurrent.duration._

    import scala.concurrent.{ Await, Future }
    import akka.actor.{ ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, Props }
    import akka.pattern.AskableActorSelection
    import akka.util.Timeout

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves, if not then it's gone and we tell supervisor
    val actorsel = Akka.system.actorSelection("/user/"+SosActor.ActorName)
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("SystemActorsController.checkSosActor", "asking SOS actor if alive")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "sos actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("SystemActorsController.checkSosActor", msg)

      }
      case Failure(ex) => {
        val msg = "sos actor dead"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("SystemActorsController.checkSosActor", msg)
      }
    }

    Ok(views.html.index(s"Enquiring SosActor status / please check the log"))
  }

  def reconnectSosActor = Action {

    Logger.debug("asking SOS actor if alive and if not trying to restart  via supervisor.")

    import scala.concurrent.duration._

    import scala.concurrent.{ Await, Future }
    import akka.actor.{ ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, Props }
    import akka.pattern.AskableActorSelection
    import akka.util.Timeout
    import java.net.InetSocketAddress

    import play.api.libs.concurrent.Execution.Implicits._

    // check if Tarom-Actor selection resolves, if not then it's gone and we tell supervisor
    val actorsel = Akka.system.actorSelection("/user/"+SosActor.ActorName)
    val superVisorSel = Akka.system.actorSelection("/user/supervisor-actor")
    val dbActorSel = Akka.system.actorSelection("/user/"+DbActor.ActorName)

    dbActorSel ! LogDataMessage("SystemActorsController.reconnectSosActor", "asking SOS actor if alive and if not trying to restart  via supervisor.")

    actorsel.resolveOne(5.second).onComplete {
      case Success(actor) => {
        val msg = "actor alive"
        Logger.debug(msg)
        dbActorSel ! LogDataMessage("SystemActorsController.reconnectSosActor", msg)

      }
      case Failure(ex) => {
        val msg = "actor dead, trying to restart"
        Logger.debug(msg)
        // superVisorSel ! "hello from controller, seems tarom is dead"
        dbActorSel ! LogDataMessage("SystemActorsController.reconnectSosActor", msg)
        superVisorSel ! SosActorCommand(1, "start")
        msg
      }
    }

    Ok(views.html.index(s"Enquiring SosActor status / please check the log"))
  }
}