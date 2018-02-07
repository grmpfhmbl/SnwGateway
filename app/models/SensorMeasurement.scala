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

import java.sql.Timestamp
import java.util.Date

import anorm.{ RowParser, SQL }
import anorm.SqlParser.{ double, long, str, date, bool }
import anorm.ResultSetParser
import anorm.SqlQuery
import anorm.~

import play.api.Play.current
import play.api.db.DB
import play.api.Logger
import utils.MyLogger

case class SensorMeasurement(
  idsensormeasurement: Long,
  meastime: Date,
  latitude: Double,
  longitude: Double,
  altitude: Double,
  rawvalue: Double,
  calcvalue: Double,
  sostransmitted: Boolean,
  soserrorcode: Long,
  sensornodes_idsensornode: Long,
  sensortypes_idsensortype: Long)

object SensorMeasurement extends Object with MyLogger {

  val sensorMeasurementQuery: SqlQuery = SQL("select * from sensormeasurements order by meastime desc")

  val SensorMeasurementRowParser: RowParser[SensorMeasurement] = {
    long("idsensormeasurement") ~
      date("meastime") ~
      double("latitude") ~
      double("longitude") ~
      double("altitude") ~
      double("rawvalue") ~
      double("calcvalue") ~
      bool("sostransmitted") ~
      long("soserrorcode") ~
      long("sensornodes_idsensornode") ~
      long("sensortypes_idsensortype") map {
        case idsensormeasurement ~ meastime ~ latitude ~ longitude ~ altitude ~ rawvalue ~ calcvalue ~ sostransmitted ~ soserrorcode ~ sensornodes_idsensornode ~ sensortypes_idsensortype =>
          SensorMeasurement(idsensormeasurement, meastime, latitude, longitude, altitude, rawvalue, calcvalue, sostransmitted, soserrorcode, sensornodes_idsensornode, sensortypes_idsensortype)
      }
  }

  val SensorMeasurementsParser: ResultSetParser[List[SensorMeasurement]] = {
    import scala.language.postfixOps
    SensorMeasurementRowParser *
  }

  def getAllWithParser: List[SensorMeasurement] = DB.withConnection {
    implicit connection =>
      sensorMeasurementQuery.as(SensorMeasurementsParser)
  }

  def get100AscWithParser: List[SensorMeasurement] = DB.withConnection {
    implicit connection =>
      val sensorMeasurementQuery100: SqlQuery = SQL("select * from sensormeasurements order by meastime desc LIMIT 100")
      sensorMeasurementQuery100.as(SensorMeasurementsParser)
  }

  def get100FailedAscWithParser: List[SensorMeasurement] = DB.withConnection {
    implicit connection =>
      val sensorMeasurementQuery100: SqlQuery = SQL("select * from sensormeasurements order by meastime desc LIMIT 100")
      sensorMeasurementQuery100.as(SensorMeasurementsParser)
  }

  def getAllNewSosForUpload: List[SensorMeasurement] = DB.withConnection {
    implicit connection =>
      {
        // default insert false, -1,
        val query = s"select * from sensormeasurements where sostransmitted=false and soserrorcode=-1 order by meastime asc"
        logger.debug(query)
        val getAllNewSosQuery: SqlQuery = SQL(query)
        getAllNewSosQuery.as(SensorMeasurementsParser)
      }
  }

  def getAllFailedSosForUpload: List[SensorMeasurement] = DB.withConnection {
    implicit connection =>
      // default insert false, -1,
      val query = s"select * from sensormeasurements where sostransmitted=false and soserrorcode>0 order by meastime asc"
      logger.debug(query)
      val getAllFailedSosUploadQuery: SqlQuery = SQL(query)
      getAllFailedSosUploadQuery.as(SensorMeasurementsParser)
  }

  // getSelectForSosUpload
  def getSelectForSosUpload(fetchFailedObservations: Boolean, maxNum: Long): List[SensorMeasurement] = DB.withConnection {
    implicit connection =>
      // default insert false, -1,
      val errorcode_query = if (!fetchFailedObservations) """sostransmitted=false and soserrorcode=-1"""else """sostransmitted=false and soserrorcode>0"""
      //FIXME technically this could lead to SQL injection!
      val query = s"select * from sensormeasurements where $errorcode_query order by meastime desc LIMIT $maxNum"
      logger.info(query)
      val getAllFailedSosUploadQuery: SqlQuery = SQL(query)
      getAllFailedSosUploadQuery.as(SensorMeasurementsParser)
  }

  def getMeasurementCount: Long = DB.withConnection {
    implicit connection =>
      val firstrow = SQL("select count(*) as c from sensormeasurements").apply().head
      firstrow[Long]("c")
  }

  def insert(sensorMeasurement: SensorMeasurement): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensormeasurements
    	values ( {idsensormeasurement}, {meastime}, {latitude}, {longitude}, {altitude}, {rawvalue}, {calcvalue},
          {sostransmitted}, {soserrorcode}, {sensornodes_idsensornode}, {sensortypes_idsensortype} )
        """).on(
        "idsensormeasurement" -> sensorMeasurement.idsensormeasurement,
        "meastime" -> sensorMeasurement.meastime,
        "latitude" -> sensorMeasurement.latitude,
        "longitude" -> sensorMeasurement.longitude,
        "altitude" -> sensorMeasurement.altitude,
        "rawvalue" -> sensorMeasurement.rawvalue,
        "calcvalue" -> sensorMeasurement.calcvalue,
        "sostransmitted" -> sensorMeasurement.sostransmitted,
        "soserrorcode" -> sensorMeasurement.soserrorcode,
        "sensornodes_idsensornode" -> sensorMeasurement.sensornodes_idsensornode,
        "sensortypes_idsensortype" -> sensorMeasurement.sensortypes_idsensortype)
        .executeUpdate()
      addedRows == 1
    }

  def insertNoID(sensorMeasurement: SensorMeasurement): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensormeasurements
          (meastime, latitude, longitude, altitude, rawvalue, calcvalue,
          sostransmitted, soserrorcode, sensornodes_idsensornode, sensortypes_idsensortype)
    	values ( {meastime}, {latitude}, {longitude}, {altitude}, {rawvalue}, {calcvalue},
          {sostransmitted}, {soserrorcode}, {sensornodes_idsensornode}, {sensortypes_idsensortype} )
        """).on(
        "meastime" -> sensorMeasurement.meastime,
        "latitude" -> sensorMeasurement.latitude,
        "longitude" -> sensorMeasurement.longitude,
        "altitude" -> sensorMeasurement.altitude,
        "rawvalue" -> sensorMeasurement.rawvalue,
        "calcvalue" -> sensorMeasurement.calcvalue,
        "sostransmitted" -> sensorMeasurement.sostransmitted,
        "soserrorcode" -> sensorMeasurement.soserrorcode,
        "sensornodes_idsensornode" -> sensorMeasurement.sensornodes_idsensornode,
        "sensortypes_idsensortype" -> sensorMeasurement.sensortypes_idsensortype)
        .executeUpdate()
      addedRows == 1
    }

  def updateSosState(idsensormeasurement: Long, sostransmitted: Boolean, soserrorcode: Long): Boolean =
    DB.withConnection { implicit connection =>
      val updatedRows = SQL("""update sensormeasurements
    set sostransmitted = {sostransmitted},
    soserrorcode = {soserrorcode}
    where idsensormeasurement = {idsensormeasurement}
    """).on(
        "idsensormeasurement" -> idsensormeasurement,
        "sostransmitted" -> sostransmitted,
        "soserrorcode" -> soserrorcode)
        .executeUpdate()
      updatedRows == 1
    }

  def existsTimestamp(nodeId: Int, sensorId: Int, ts: Timestamp): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """select 1 from SensorMeasurements
          where
            sensornodes_idsensornode = {nodeId} and
            sensortypes_idsensortype = {sensorId} and
            meastime = {ts}
        """).on("nodeId" -> nodeId,
                "sensorId" -> sensorId,
                "ts" -> ts)
        .executeQuery().resultSet.next()
    }
  }
}