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
import models.SensorNode
import play.api.{GlobalSettings, Logger, Play, PlayException}
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
    val subConfig = config.getConfig("sensorweb").get

    logger.info("Checking basic configuration...")
    lazy val UPLINK_SOS_NODE_EQUIVALENT: String = subConfig.getString("uplink.sos.node_equivalent").getOrElse("")
    logger.info(s"Finding SOS-UPLINK node (${UPLINK_SOS_NODE_EQUIVALENT}) in Database.")
    if (SensorNode.getSensorNodeByExtendedAddress(UPLINK_SOS_NODE_EQUIVALENT).isEmpty) {
      logger.error(s"Configured uplink node application.config does not equal uplink node in database.")
      //FIXME this is for sure not the way of doing this correctly...
      throw new PlayException(s"Configured uplink node in application.config (${UPLINK_SOS_NODE_EQUIVALENT}) does not equal uplink node in database.",
        s"Configured uplink node in application.config (${UPLINK_SOS_NODE_EQUIVALENT}) does not equal uplink node in database.")
    }

    logger.info("Creating Supervisor Actor...")
    val actorSupervisor = Akka.system().actorOf(ActorSupervisor.props(subConfig), ActorSupervisor.ActorName)
    val actorScheduleTimeout = subConfig.getInt("supervisor.actorschedule.timeout").getOrElse(45)
    val actorInitialScheduleTimeout = 5

    import play.api.libs.concurrent.Execution.Implicits._
    implicit val timeout = Timeout(10.seconds)
    var actorsScheduled = 0;

    // doesn't do much for initialisation
    //Akka.system().scheduler.scheduleOnce(20.seconds, actorSupervisor,CmdGetOrStart(ProcessExecActor.ActorName))

    def scheduleActorStart(actorName: String) = {
      val timeout = actorScheduleTimeout * actorsScheduled + actorInitialScheduleTimeout
      logger.info(s"Start of ${actorName} scheduled in ${timeout.seconds}...")
      Akka.system().scheduler.scheduleOnce(timeout.seconds, actorSupervisor, CmdGetOrStart(actorName))
      actorsScheduled += 1;
    }

    if (subConfig.getBoolean("xbee.enabled").getOrElse(false)) {
      scheduleActorStart(XBeeActor.ActorName)
    }

    if (subConfig.getBoolean("uplink.mqtt.enabled").getOrElse(false)) {
      scheduleActorStart(ActorMqtt.ActorName)
    }

    if (subConfig.getBoolean("uplink.sos.enabled").getOrElse(false)) {
      scheduleActorStart(SosActor.ActorName)
    }

    if (subConfig.getBoolean("wiz.enabled").getOrElse(false)) {
      scheduleActorStart(WizActor.ActorName)
    }

    if (subConfig.getBoolean("tarom.enabled").getOrElse(false)) {
      scheduleActorStart(ActorTarom.ActorName)
    }

    if (subConfig.getBoolean("sontekIq.enabled").getOrElse(false)) {
      scheduleActorStart(ActorSontekIq.ActorName)
    }
  }

  override def onStop(application: play.api.Application) {
    logger.info("onStop()")
  }
}
