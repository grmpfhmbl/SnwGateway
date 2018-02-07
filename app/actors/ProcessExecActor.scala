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
import akka.pattern.ask
import akka.util.Timeout
import scala.util.Success

import java.io._
import java.util.ArrayList
import java.util.Arrays
import java.util.List

import utils.MyLogger

object ProcessExecActor {
  val ActorName = "procexec"
}

class ProcessExecActor extends Actor with MyLogger {

  import play.api.libs.concurrent.Execution.Implicits._

  lazy val dbActorSel = context.system.actorSelection(s"/user/${ActorSupervisor.ActorName }/${DbActor.ActorName }")

  override def preStart(): Unit = {
    logger.info(s"Starting ProcessExecActor ${ProcessExecActor.ActorName}")
  }

  def receive = {
    case message: String => {
      logger.debug(s" ${ProcessExecActor.ActorName} got message: $message")

      val stdout = message match {
        case "ps" => {
          val pb = new ProcessBuilder("/bin/ps", "-ef")
          val process = pb.start()
          val is = process.getInputStream()
          val output = scala.io.Source.fromInputStream(is).mkString
          val exitValue = process.waitFor()
          if (exitValue != 0) {
            throw new IOException("process didn't complete successfully")
          }
          try {
            is.close()
          } finally {
            process.destroy()
          }
          output
        }
        case "df" => {
          val pb = new ProcessBuilder("/bin/df", "-h")
          val process = pb.start()
          val is = process.getInputStream()
          val output = scala.io.Source.fromInputStream(is).mkString
          val exitValue = process.waitFor()
          if (exitValue != 0) {
            throw new IOException("process didn't complete successfully")
          }
          try {
            is.close()
          } finally {
            process.destroy()
          }
          output
        }
        case "free" => {
          val pb = new ProcessBuilder("/usr/bin/free", "-h")
          val process = pb.start()
          val is = process.getInputStream()
          val output = scala.io.Source.fromInputStream(is).mkString
          val exitValue = process.waitFor()
          if (exitValue != 0) {
            throw new IOException("process didn't complete successfully")
          }
          try {
            is.close()
          } finally {
            process.destroy()
          }
          output
        }
        case "uptime" => {
          val pb = new ProcessBuilder("/usr/bin/uptime")
          val process = pb.start()
          val is = process.getInputStream()
          val output = scala.io.Source.fromInputStream(is).mkString
          val exitValue = process.waitFor()
          if (exitValue != 0) {
            throw new IOException("process didn't complete successfully")
          }
          try {
            is.close()
          } finally {
            process.destroy()
          }
          output
        }
        case "reboot" => {
          // needs root / sudo rights
          "reboot - not yet implemented"
        }
        case "fetchremotecmd" => {
          // will include download of a bash script from a pre-defined sort of secure location on the smart server
          // and then execute that command like the others above
          "fetchremotecmd - not yet implemented"
        }
        case _ =>
          "unknown command"
      }

      sender ! s" ${ProcessExecActor.ActorName} got message: $stdout"
    }
    case _ => {
      logger.debug(s" ${ProcessExecActor.ActorName} got unknown message")
      sender ! s" ${ProcessExecActor.ActorName} got unknown message"
    }
  }

}
