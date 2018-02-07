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
import anorm.SqlParser.{ double, long, str, date, bool }
import anorm.ResultSetParser
import anorm.SqlQuery
import anorm.~

import play.api.Play.current
import play.api.db.DB
import play.api.Logger

case class SensorTextObservation(
  idsensortextobservation: Long,
  meastime: Date,
  latitude: Double,
  longitude: Double,
  altitude: Double,
  category: String,
  textvalue: String,
  sostransmitted: Boolean,
  soserrorcode: Long,
  sensornodes_idsensornode: Long,
  sensortypes_idsensortype: Long)

object SensorTextObservation {

  val sensorTextObservationQuery: SqlQuery = SQL("select * from sensortextobservations order by meastime desc")

  val SensorTextObservationRowParser: RowParser[SensorTextObservation] = {
    long("idsensortextobservation") ~
      date("meastime") ~
      double("latitude") ~
      double("longitude") ~
      double("altitude") ~
      str("category") ~
      str("textvalue") ~
      bool("sostransmitted") ~
      long("soserrorcode") ~
      long("sensornodes_idsensornode") ~
      long("sensortypes_idsensortype") map {
        case idsensortextobservation ~ meastime ~ latitude ~ longitude ~ altitude ~ category ~ textvalue ~ sostransmitted ~ soserrorcode ~ sensornodes_idsensornode ~ sensortypes_idsensortype =>
          SensorTextObservation(idsensortextobservation, meastime, latitude, longitude, altitude, category, textvalue, sostransmitted, soserrorcode, sensornodes_idsensornode, sensortypes_idsensortype)
      }
  }

  val SensorTextObservationsParser: ResultSetParser[List[SensorTextObservation]] = {
    import scala.language.postfixOps
    SensorTextObservationRowParser *
  }

  def getAllWithParser: List[SensorTextObservation] = DB.withConnection {
    implicit connection =>
      sensorTextObservationQuery.as(SensorTextObservationsParser)
  }

  def get1000AscWithParser: List[SensorTextObservation] = DB.withConnection {
    implicit connection =>
      val sensorTextObservationQuery1000: SqlQuery = SQL("select * from sensortextobservations order by meastime desc LIMIT 1000")
      sensorTextObservationQuery1000.as(SensorTextObservationsParser)
  }

  def getAllNewSosUploadWithParser: List[SensorTextObservation] = DB.withConnection {
    implicit connection =>
      // default insert false, -1,
      val query = s"select * from sensortextobservations where sostransmitted=false and soserrorcode=-1 order by meastime desc LIMIT 300"
      Logger.debug(query)
      val getAllNewSosUploadQuery: SqlQuery = SQL(query)
      getAllNewSosUploadQuery.as(SensorTextObservationsParser)
  }

  def getAllFailedSosUploadWithParser: List[SensorTextObservation] = DB.withConnection {
    implicit connection =>
      // default insert false, -1,
      val query = s"select * from sensortextobservations where sostransmitted=false and soserrorcode>0 order by meastime desc LIMIT 300"
      Logger.debug(query)
      val getAllFailedSosUploadQuery: SqlQuery = SQL(query)
      getAllFailedSosUploadQuery.as(SensorTextObservationsParser)
  }

  // getSelectForSosUpload
  def getSelectForSosUpload(newOrFailed: Boolean, maxNum: Long): List[SensorTextObservation] = DB.withConnection {
    implicit connection =>
      // default insert false, -1,
      val errorcode_query = if (newOrFailed) """sostransmitted=false and soserrorcode=-1""" else """sostransmitted=false and soserrorcode=>0"""
      val query = s"select * from sensortextobservations where $errorcode_query order by meastime desc LIMIT $maxNum"
      Logger.debug(query)
      val getAllFailedSosUploadQuery: SqlQuery = SQL(query)
      getAllFailedSosUploadQuery.as(SensorTextObservationsParser)
  }

  def getTextObservationCount: Long = DB.withConnection {
    implicit connection =>
      val firstrow = SQL("select count(*) as c from sensortextobservations").apply().head
      firstrow[Long]("c")
  }

  def insert(sensorTextObservation: SensorTextObservation): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensortextobservations
    	values ( {idsensortextobservation}, {meastime}, {latitude}, {longitude}, {altitude}, {category}, {textvalue},
          {sostransmitted}, {soserrorcode}, {sensornodes_idsensornode}, {sensortypes_idsensortype} )
        """).on(
        "idsensortextobservation" -> sensorTextObservation.idsensortextobservation,
        "meastime" -> sensorTextObservation.meastime,
        "latitude" -> sensorTextObservation.latitude,
        "longitude" -> sensorTextObservation.longitude,
        "altitude" -> sensorTextObservation.altitude,
        "category" -> sensorTextObservation.category,
        "textvalue" -> sensorTextObservation.textvalue,
        "sostransmitted" -> sensorTextObservation.sostransmitted,
        "soserrorcode" -> sensorTextObservation.soserrorcode,
        "sensornodes_idsensornode" -> sensorTextObservation.sensornodes_idsensornode,
        "sensortypes_idsensortype" -> sensorTextObservation.sensortypes_idsensortype)
        .executeUpdate()
      addedRows == 1
    }

  def insertNoID(sensorTextObservation: SensorTextObservation): Boolean =
    DB.withConnection { implicit connection =>
      val addedRows = SQL("""insert into sensortextobservations
          (meastime, latitude, longitude, altitude, category, textvalue,
          sostransmitted, soserrorcode, sensornodes_idsensornode, sensortypes_idsensortype)
    	values ( {meastime}, {latitude}, {longitude}, {altitude}, {category}, {textvalue},
          {sostransmitted}, {soserrorcode}, {sensornodes_idsensornode}, {sensortypes_idsensortype} )
        """).on(
        "meastime" -> sensorTextObservation.meastime,
        "latitude" -> sensorTextObservation.latitude,
        "longitude" -> sensorTextObservation.longitude,
        "altitude" -> sensorTextObservation.altitude,
        "category" -> sensorTextObservation.category,
        "textvalue" -> sensorTextObservation.textvalue,
        "sostransmitted" -> sensorTextObservation.sostransmitted,
        "soserrorcode" -> sensorTextObservation.soserrorcode,
        "sensornodes_idsensornode" -> sensorTextObservation.sensornodes_idsensornode,
        "sensortypes_idsensortype" -> sensorTextObservation.sensortypes_idsensortype)
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

}

