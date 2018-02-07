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

package models

import java.util.Date

import anorm.{ RowParser, SQL }
import anorm.SqlParser.{ double, long, str }
import anorm.ResultSetParser
import anorm.SqlQuery
import anorm.~
import play.api.Play.current
import play.api.db.DB
import utils.MyLogger

case class SensorNode(
  idsensornode: Long,
  extendedaddress: String,
  name: String,
  description: String,
  latitude: Double,
  longitude: Double,
  altitude: Double)

object SensorNode extends Object with MyLogger {

  val sensorNodeQuery: SqlQuery = SQL("select * from sensornodes order by name asc")

  val SensorNodeRowParser: RowParser[SensorNode] = {
    long("idsensornode") ~
      str("extendedaddress") ~
      str("name") ~
      str("description") ~
      double("latitude") ~
      double("longitude") ~
      double("altitude") map {
        case idsensornode ~ extendedaddress ~ name ~ description ~ latitude ~ longitude ~ altitude =>
          SensorNode(idsensornode, extendedaddress, name, description, latitude, longitude, altitude)
      }
  }

  val SensorNodesParser: ResultSetParser[List[SensorNode]] = {
    import scala.language.postfixOps
    SensorNodeRowParser *
  }

  def getAllWithParser: List[SensorNode] = DB.withConnection {
    implicit connection =>
      val test = sensorNodeQuery.as(SensorNodesParser)
      test
  }

  def getSensorNodeByExtendedAddress(extendedAddress: String): List[SensorNode] = DB.withConnection {
    implicit connection =>
      {
        val query = s"select * from sensornodes where extendedaddress='$extendedAddress' order by name asc"
        logger.debug(query)
        val sensorNodeQueryByExtendedAddress: SqlQuery = SQL(query)
        sensorNodeQueryByExtendedAddress.as(SensorNodesParser)
      }

  }

  def getSensorNodeByName(name: String): List[SensorNode] = DB.withConnection {
    implicit connection =>
      {
        val query = s"select * from sensornodes where name='$name' order by name asc"
        val sensorNodeQueryByName: SqlQuery = SQL(query)
        logger.debug(sensorNodeQueryByName.toString())
        sensorNodeQueryByName.as(SensorNodesParser)
      }

  }

  def getSensorNodeByID(id: Long): SensorNode = DB.withConnection {
    implicit connection =>
      {
        val query = s"select * from sensornodes where idsensornode=$id"
        val sensorNodeQueryByName: SqlQuery = SQL(query)
        sensorNodeQueryByName.as(SensorNodesParser).head
      }

  }

  def delete(sensorNode: SensorNode): Boolean =
    DB.withConnection { implicit connection =>
      val updatedRows = SQL("delete from sensornodes where idsensornode = {idsensornode}").
        on("idsensornode" -> sensorNode.idsensornode).executeUpdate()
      updatedRows == 0
    }

  def insert(sensorNode: SensorNode): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensornodes
    	values ( {idsensornode}, {extendedaddress}, {name}, {description}, {latitude}, {longitude}, {altitude} )
        """).on(
        "idsensornode" -> sensorNode.idsensornode,
        "extendedaddress" -> sensorNode.extendedaddress,
        "name" -> sensorNode.name,
        "description" -> sensorNode.description,
        "latitude" -> sensorNode.latitude,
        "longitude" -> sensorNode.longitude,
        "altitude" -> sensorNode.altitude)
        .executeUpdate()
      addedRows == 1
    }

  def insertNoID(sensorNode: SensorNode): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensornodes
               ( extendedaddress , name , description , latitude , longitude ,altitude ) 
      values ( {extendedaddress}, {name}, {description}, {latitude}, {longitude}, {altitude} )
        """).on(
        "extendedaddress" -> sensorNode.extendedaddress,
        "name" -> sensorNode.name,
        "description" -> sensorNode.description,
        "latitude" -> sensorNode.latitude,
        "longitude" -> sensorNode.longitude,
        "altitude" -> sensorNode.altitude)
        .executeUpdate()
      addedRows == 1
    }

  def update(sensorNode: SensorNode): Boolean =
    DB.withConnection { implicit connection =>
      val updatedRows = SQL("""update sensornodes
		set idsensornode = {idsensornode},
		extendedaddress = {extendedaddress},
		name = {name},
        description = {description},
		latitude = {latitude},
		longitude = {longitude},
        altitude = {altitude}
		where idsensornode = {idsensornode}
		""").on(
        "idsensornode" -> sensorNode.idsensornode,
        "extendedaddress" -> sensorNode.extendedaddress,
        "name" -> sensorNode.name,
        "description" -> sensorNode.description,
        "latitude" -> sensorNode.latitude,
        "longitude" -> sensorNode.longitude,
        "altitude" -> sensorNode.altitude)
        .executeUpdate()
      updatedRows == 1
    }
}

