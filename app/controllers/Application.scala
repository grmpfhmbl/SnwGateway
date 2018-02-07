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

import scala.io.Source
import play.api.Logger
import play.api.mvc.{ Action, Controller }
import models._
import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  val nodeForm = Form(
    mapping(
      "idsensornode" -> longNumber,
      "extendedaddress" -> nonEmptyText,
      "name" -> nonEmptyText,
      "description" -> text,
      "latitude" -> bigDecimal,
      "longitude" -> bigDecimal,
      "altitude" -> bigDecimal) // SensorNodeForm -> SensorNode
      ((idsensornode, extendedaddress, name, description, latitude, longitude, altitude) => SensorNode(idsensornode, extendedaddress, name, description, latitude.toDouble, longitude.toDouble, altitude.toDouble)) // SensorNode -> SensorNodeForm
      ((sn: SensorNode) => Some(sn.idsensornode, sn.extendedaddress, sn.name, sn.description, sn.latitude, sn.longitude, sn.altitude)))

  val typeForm = Form(
    mapping(
      "idsensortype" -> longNumber,
      "sensid" -> longNumber,
      "sensorname" -> nonEmptyText,
      "placement" -> bigDecimal,
      "phenomenon" -> nonEmptyText,
      "unit" -> nonEmptyText,
      "description" -> text)((idsensortype, sensid, sensorname, placement, phenomenon, unit, description) => SensorType(idsensortype, sensid, sensorname, placement.toDouble, phenomenon, unit, description)) // SensorNode -> SensorNodeForm (unapply)
      ((st: SensorType) => Some(st.idsensortype, st.sensid, st.sensorname, st.placement, st.phenomenon, st.unit, st.description)))

  def index = Action {
    Ok(views.html.index("Gateway2 Akka / Scala / Play Port - Sensor Gateway."))
  }

  def redir = Action {
    Redirect(routes.Application.index)
  }

  def checkMeasurementCount = Action {
    val obscount = SensorMeasurement.getMeasurementCount.toString
    val obs = SensorMeasurement.get100AscWithParser
    val nodeList = SensorNode.getAllWithParser
    val typeList = SensorType.getAllWithParser
    Ok(views.html.latestMeasurements(obs, nodeList, typeList, s" $obscount overall OBS in DB now"))
  }

  def checkFailedMeasurementCount = Action {
    val obscount = SensorMeasurement.getMeasurementCount.toString
    val obs = SensorMeasurement.get100AscWithParser
    val nodeList = SensorNode.getAllWithParser
    val typeList = SensorType.getAllWithParser
    Ok(views.html.latestMeasurements(obs, nodeList, typeList, s" $obscount overall OBS in DB now"))
  }

  def checkSysMessages = Action {
    val obscount = SensorTextObservation.getTextObservationCount.toString
    val obs = SensorTextObservation.get1000AscWithParser
    val nodeList = SensorNode.getAllWithParser
    val typeList = SensorType.getAllWithParser
    Ok(views.html.latestSystemMessages(obs, nodeList, typeList, s" $obscount overall OBS in DB now"))
  }

  def listInventoryNodes = Action {

    val nodes = SensorNode.getAllWithParser
    val types = SensorType.getAllWithParser

    Ok(views.html.inventorynodes(nodes, s"Inventory NODES"))
  }

  def listInventoryTypes = Action {

    val types = SensorType.getAllWithParser

    Ok(views.html.inventorytypes(types, s"Inventory TYPES"))
  }

  def addNodeForm = Action {
    Ok(views.html.addNodeForm(nodeForm, "if it's a new entry and you don't want to chose the ID, please enter -1", "new"))
  }

  def updateNodeForm(id: Long) = Action {
    val node = SensorNode.getSensorNodeByID(id)
    val filledForm = nodeForm.fill(node)
    Ok(views.html.addNodeForm(filledForm, "don't change the ID", "update"))
  }

  def addNodePost = Action {
    implicit request =>
      nodeForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.addNodeForm(formWithErrors, "There are errors in the form", "new"))
        },
        sensorNode => {
          /* binding success, you get the actual value. */
          // val sn = models.SensorNode(userData.name, userData.age)
          val sn = sensorNode
          if (sn.idsensornode <= 0) {
            SensorNode.insertNoID(sn)
          } else {
            SensorNode.insert(sn)
          }

          Redirect(routes.Application.listInventoryNodes)
        })
  }

  def updateNodePost = Action {
    implicit request =>
      nodeForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.addNodeForm(formWithErrors, "There are errors in the form", "update"))
        },
        sensorNode => {
          /* binding success, you get the actual value. */
          // val sn = models.SensorNode(userData.name, userData.age)
          val sn = SensorNode.getSensorNodeByID(sensorNode.idsensornode)

          val retval = SensorNode.update(sensorNode)

          Redirect(routes.Application.listInventoryNodes)
        })
  }

  def addTypeForm = Action {
    Ok(views.html.addTypeForm(typeForm, "if it's a new entry and you don't want to chose the ID, please enter -1", "new"))
  }

  def updateTypeForm(id: Long) = Action {
    val sensType = SensorType.getSensorTypeByID(id)
    val filledForm = typeForm.fill(sensType)
    Ok(views.html.addTypeForm(filledForm, "Don't change the ID", "update"))
  }

  def addTypePost = Action {
    implicit request =>
      typeForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.addTypeForm(formWithErrors, "There are errors in the form", "new"))
        },
        sensorType => {
          /* binding success, you get the actual value. */
          val st = sensorType
          if (st.idsensortype <= 0) {
            SensorType.insertNoID(st)
          } else {
            SensorType.insert(st)
          }

          Redirect(routes.Application.listInventoryTypes)
        })
  }

  def updateTypePost = Action {
    implicit request =>
      typeForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.addTypeForm(formWithErrors, "There are errors in the form", "update"))
        },
        sensorType => {
          /* binding success, you get the actual value. */
          val st = SensorType.getSensorTypeByID(sensorType.idsensortype)

          val retval = SensorType.update(sensorType)

          Redirect(routes.Application.listInventoryTypes)
        })
  }
}