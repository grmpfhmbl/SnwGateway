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

case class SensorType(
  idsensortype: Long,
  sensid: Long,
  sensorname: String,
  placement: Double,
  phenomenon: String,
  unit: String,
  description: String)

object SensorType {

  val sensorTypeQuery: SqlQuery = SQL("select * from sensortypes order by sensorname asc")

  val sensorTypeRowParser: RowParser[SensorType] = {
    long("idsensortype") ~
      long("sensid") ~
      str("sensorname") ~
      double("placement") ~
      str("phenomenon") ~
      str("unit") ~
      str("description") map {
        case idsensortype ~ sensid ~ sensorname ~ placement ~ phenomenon ~ unit ~ description =>
          SensorType(idsensortype, sensid, sensorname, placement, phenomenon, unit, description)
      }
  }

  val sensorTypesParser: ResultSetParser[List[SensorType]] = {
    import scala.language.postfixOps
    sensorTypeRowParser *
  }

  def getAllWithParser: List[SensorType] = DB.withConnection {
    implicit connection =>
      sensorTypeQuery.as(sensorTypesParser)
  }

  def getSensorTypeByName(sensorname: String): List[SensorType] = DB.withConnection {
    implicit connection =>
      {
        val query = s"select * from sensortypes where sensorname='$sensorname' order by sensorname asc"
        val sensorTypeQueryByName: SqlQuery = SQL(query)
        sensorTypeQueryByName.as(sensorTypesParser)
      }

  }

  def getSensorTypeBySensID(sensid: Long): List[SensorType] = DB.withConnection {
    implicit connection =>
      {
        val query = s"select * from sensortypes where sensid='$sensid' order by sensorname asc"
        val sensorTypeQueryBySensID: SqlQuery = SQL(query)
        sensorTypeQueryBySensID.as(sensorTypesParser)
      }

  }

  def getSensorTypeByID(id: Long): SensorType = DB.withConnection {
    implicit connection =>
      {
        val query = s"select * from sensortypes where idsensortype=$id"
        val sensorTypeQueryByID: SqlQuery = SQL(query)
        sensorTypeQueryByID.as(sensorTypesParser).head
      }

  }

  def delete(sensorType: SensorType): Boolean =
    DB.withConnection { implicit connection =>
      val updatedRows = SQL("delete from sensortypes where idsensortype = {idsensortype}").
        on("idsensortype" -> sensorType.idsensortype).executeUpdate()
      updatedRows == 0
    }

  def insert(sensorType: SensorType): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensortypes
    	values ( {idsensortype}, {sensid}, {sensorname}, {placement}, {phenomenon}, {unit}, {description} )
        """).on(
        "idsensortype" -> sensorType.idsensortype,
        "sensid" -> sensorType.sensid,
        "sensorname" -> sensorType.sensorname,
        "placement" -> sensorType.placement,
        "phenomenon" -> sensorType.phenomenon,
        "unit" -> sensorType.unit,
        "description" -> sensorType.description)
        .executeUpdate()
      addedRows == 1
    }

  def insertNoID(sensorType: SensorType): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensortypes
                ( sensid, sensorname, placement, phenomenon, unit, description )
      values ( {sensid}, {sensorname}, {placement}, {phenomenon}, {unit}, {description} )
        """).on(
        "sensid" -> sensorType.sensid,
        "sensorname" -> sensorType.sensorname,
        "placement" -> sensorType.placement,
        "phenomenon" -> sensorType.phenomenon,
        "unit" -> sensorType.unit,
        "description" -> sensorType.description)
        .executeUpdate()
      addedRows == 1
    }

  def update(sensorType: SensorType): Boolean =
    DB.withConnection { implicit connection =>
      val updatedRows = SQL("""update sensortypes
		set idsensortype = {idsensortype},
		sensid = {sensid},
		sensorname = {sensorname},
        placement = {placement},
		phenomenon = {phenomenon},
		unit = {unit},
        description = {description}
		where idsensortype = {idsensortype}
		""").on(
        "idsensortype" -> sensorType.idsensortype,
        "sensid" -> sensorType.sensid,
        "sensorname" -> sensorType.sensorname,
        "placement" -> sensorType.placement,
        "phenomenon" -> sensorType.phenomenon,
        "unit" -> sensorType.unit,
        "description" -> sensorType.description)
        .executeUpdate()
      updatedRows == 1
    }
}

