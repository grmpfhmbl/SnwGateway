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

import actors.ActorSupervisor.CmdGetOrStart
import actors._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigValueType
import play.api.{GlobalSettings, Logger}
import play.libs.Akka
import utils.MyLogger

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object Global extends GlobalSettings with MyLogger {

  override def onStart(application: play.api.Application) {
    logger.info("onStart()" + logger.logger.getName)

    val config = application.configuration
    val sortedItems = config.keys.filter(_.startsWith("sensorweb")).toList.sorted
    for (key <- sortedItems) {
      val cfgVal =
        if (key.contains("password"))
          "****"
        else {
          config.underlying.getValue(key).valueType() match {
            case ConfigValueType.LIST => config.getStringList(key).get.asScala.mkString(", ")
            case _ => config.getString(key).get
          }
        }
      logger.debug(f"${key}%45s = ${cfgVal}")
    }

    logger.info("Creating Supervisor Actor...")
    //FIXME SREI this is pretty fucked up. make real error handling here
    val subConfig = config.getConfig("sensorweb").get
    val actorSupervisor = Akka.system().actorOf(ActorSupervisor.props(subConfig), ActorSupervisor.ActorName)

    import play.api.libs.concurrent.Execution.Implicits._
    implicit val timeout = Timeout(10.seconds)

    // doesn't do much for initialisation
    //Akka.system().scheduler.scheduleOnce(20.seconds, actorSupervisor,CmdGetOrStart(ProcessExecActor.ActorName))

    //TODO SREI think of better startup sequence / waiting time
    if (subConfig.getBoolean("xbee.gateway.enabled").getOrElse(false)) {
      Akka.system().scheduler.scheduleOnce(10.seconds, actorSupervisor,CmdGetOrStart(XBeeActor.ActorName))
    }
    if (subConfig.getBoolean("uplink.mqtt.enabled").getOrElse(false)) {
      Akka.system().scheduler.scheduleOnce(30.seconds, actorSupervisor,CmdGetOrStart(ActorMqtt.ActorName))

    if (subConfig.getBoolean("uplink.sos.enabled").getOrElse(false)) {
      Akka.system().scheduler.scheduleOnce(60.seconds, actorSupervisor,CmdGetOrStart(SosActor.ActorName))
    }

    if (subConfig.getBoolean("wiz.enabled").getOrElse(false)) {
      Akka.system().scheduler.scheduleOnce(90.seconds, actorSupervisor,CmdGetOrStart(WizActor.ActorName))
    }

    }

    /*
        if (play.Play.application.configuration.getString("sensorweb.moxa.spa.enabled").equalsIgnoreCase("true")) {
          val cancelSpaStart = Akka.system.scheduler.scheduleOnce(5.seconds, actorSupervisor, SpaActorCommand(1, "start"))
        }

        if (play.Play.application.configuration.getString("sensorweb.moxa.tarom.enabled").equalsIgnoreCase("true")) {
          val cancelTaromStart = Akka.system.scheduler.scheduleOnce(5.seconds, actorSupervisor, TaromActorCommand(1, "start"))
        }
    */
  }

  override def onStop(application: play.api.Application) {
    logger.info("onStop()")
  }
}
