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

import java.util.UUID

import play.api.libs.json._

/*
//http://stackoverflow.com/questions/5485817/can-i-perform-matching-on-a-type-parameter-in-scala-to-see-if-it-implements-a-tr
object MqttMessage extends Object with MyLogger {
  def fromJsObject[A >: MqttMessage](implicit ev: ClassManifest[A], json: JsObject): A = {
    val clazz = ev.erasure
    if (classOf[MqttGetCapabilitiesReq] isAssignableFrom clazz)
      MqttGetCapabilitiesReq.fromJsValue(json)
    else
      throw new IllegalArgumentException(s"Cannot create ${ev.toString()}")
  }
}
*/

abstract class MqttMessage(val messageUUID: Option[UUID]) {
  def asJson: JsObject
}

// TODO AKMO sections should also be Options/asOpt
object MqttGetCapabilitiesReq {
  def fromJsValue(json: JsValue): MqttGetCapabilitiesReq = {
    new MqttGetCapabilitiesReq(
      messageUUID = (json \ "messageUUID").asOpt[String].map(UUID.fromString(_)),
      sections = (json \ "sections").asOpt[List[String]]
    )
  }
}

class MqttGetCapabilitiesReq(
                              override val messageUUID: Option[UUID],
                              val sections: Option[List[String]]) extends MqttMessage(messageUUID) {
  override def asJson: JsObject = {
    //FIXME SREI make this look more functional
    var json = Json.obj()
    if (messageUUID.isDefined)
      json = json +("messageUUID", JsString(messageUUID.get.toString))
    if (sections.isDefined)
      json = json +("sections", JsArray(sections.getOrElse(List("serviceIdentification", "operations","tasks")).map(JsString(_))))
    return json
  }
}

object MqttGetCapabilitiesResp {
  def fromJsValue(json: JsValue): MqttGetCapabilitiesResp = {
    new MqttGetCapabilitiesResp(
      messageUUID = (json \ "messageUUID").asOpt[String].map(UUID.fromString(_)),
      serviceIdentification = (json \ "serviceIdentification").asOpt[String],
      operations = (json \ "operations").asOpt[List[String]],
      tasks = (json \ "tasks").asOpt[List[String]]
    )
  }
}

class MqttGetCapabilitiesResp(
                               override val messageUUID: Option[UUID],
                               val serviceIdentification: Option[String],
                               val operations: Option[List[String]],
                               val tasks: Option[List[String]]) extends MqttMessage(messageUUID) {
  override def asJson: JsObject = {
    //FIXME SREI make this look more functional
    var json = Json.obj()
    if (messageUUID.isDefined)
      json = json +("messageUUID", JsString(messageUUID.get.toString))
    if (serviceIdentification.isDefined)
      json = json +("serviceIdentification", JsString(serviceIdentification.get))
    if (operations.isDefined)
      json = json +("operations", JsArray(operations.get.map(JsString(_))))
    if (tasks.isDefined) {
      json = json +("tasks", JsArray(tasks.get.map(JsString(_))))
    }
    return json
  }
}

object MqttDescribeTaskReq {
  def fromJsValue(json: JsValue): MqttDescribeTaskReq = {
    new MqttDescribeTaskReq(
      messageUUID = (json \ "messageUUID").asOpt[String].map(UUID.fromString(_)),
      task = (json \ "task").as[String]
    )
  }
}

class MqttDescribeTaskReq(override val messageUUID: Option[UUID],
                          val task: String) extends MqttMessage(messageUUID) {
  override def asJson: JsObject = {
    var json = Json.obj()
    if (messageUUID.isDefined)
      json = json +("messageUUID", JsString(messageUUID.get.toString))
    json = json +("task", JsString(task))
    return json
  }
}


object MqttDescribeTaskResp {
  def fromJsValue(json: JsValue): MqttDescribeTaskResp = {
    new MqttDescribeTaskResp(messageUUID = (json \ "messageUUID").asOpt[String].map(UUID.fromString(_)),
      task = (json \ "task").as[String],
      description = (json \ "description").as[String],
      parameter = (json \ "parameter").as[List[String]])
  }
}

class MqttDescribeTaskResp(override val messageUUID: Option[UUID],
                           val task: String,
                           val description: String,
                           val parameter: List[String]) extends MqttMessage(messageUUID = messageUUID) {
  override def asJson: JsObject = {
    var json = Json.obj()
    if (messageUUID.isDefined)
      json = json +("messageUUID", JsString(messageUUID.get.toString))
    json = json +("task", JsString(task)) +
      ("description", JsString(description)) +
      ("parameter", JsArray(parameter.map(JsString(_))))
    return json
  }
}

// TODO SREI make parameter handling graceful asOpt to allow empty or missing paramters if task doesn't need params
object MqttSubmitTaskReq {
  def fromJsValue(json: JsValue): MqttSubmitTaskReq = {
    new MqttSubmitTaskReq(messageUUID = (json \ "messageUUID").asOpt[String].map(UUID.fromString(_)),
      task = (json \ "task").as[String],
      parameter = (json \ "parameter").as[Map[String, String]])
  }
}

class MqttSubmitTaskReq(override val messageUUID: Option[UUID],
                        val task: String,
                        val parameter: Map[String, String]
                         ) extends MqttMessage(messageUUID = messageUUID) {
  override def asJson: JsObject = {
    var json = Json.obj()
    if (messageUUID.isDefined)
      json = json +("messageUUID", JsString(messageUUID.get.toString))
    json = json +("task", JsString(task))
    json = json +("parameter", Json.toJson(parameter))
    return json
  }
}

object MqttSubmitTaskResp {
  def fromJsValue(json: JsValue): MqttSubmitTaskResp = {
    new MqttSubmitTaskResp(messageUUID = (json \ "messageUUID").asOpt[String].map(UUID.fromString(_)),
      task = (json \ "task").as[String],
      parameter = (json \ "parameter").as[Map[String, String]],
      status = (json \ "status").as[String]
    )
  }
}

class MqttSubmitTaskResp(override val messageUUID: Option[UUID],
                         val task: String,
                         val parameter: Map[String, String],
                         val status: String) extends MqttMessage(messageUUID = messageUUID) {
  override def asJson: JsObject = {
    var json = Json.obj()
    if (messageUUID.isDefined)
      json = json +("messageUUID", JsString(messageUUID.get.toString))
    json = json +("task", JsString(task))
    json = json +("parameter", Json.toJson(parameter))
    json = json +("status", JsString(status))
    return json
  }
}
