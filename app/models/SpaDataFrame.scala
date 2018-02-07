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

/**
 * SPA packet parsing partially adapted from Gateway meshlium
 * 
 * only Christian knows what happening relly, we better reverse 
 * engineer quickly and refacor and make it modern and reactive :-p
 * 
 * note as of 19.98.2014 AK
 * 
 * LICENSE designated ASL 2.0, extra packages used xbee api and rxtx to be checked
 */
package models

case class SpaDataFrame(
  data: Double,
  rawdata: Double,
  sensorid: Int)

object SpaDataFrame {

  // constants for SPA Hex parse, explicit Int for readabilty
  val Sensor1IceContID: Int = 0x012C
  val Sensor1WaterContID: Int = 0x012D
  val Sensor1SnowDensID: Int = 0x012E
  val Sensor1SweID: Int = 0x012F

  val Sensor2IceContID: Int = 0x0130
  val Sensor2WaterContID: Int = 0x0131
  val Sensor2SnowDensID: Int = 0x0132
  val Sensor2SweID: Int = 0x0133

  val SnowDepthID: Int = 0x013C
  val ChipTempID: Int = 0x013E

  // dises if d > 10 oder eben nicht, x=d, y=e
  def getd10(str: String) = {
    val d = str.toDouble
    val e = if (d > 0) d / 10 else d
    (d, e)
  }

  def parseSpaMessage(line: String) : List[SpaDataFrame] = {

    val msg = line.trim()

    msg match {
      
      case str if str.startsWith("I00") => {

        //        Value = msg.substring(5, 9);       //Sensor 1 Ice Content
        //                d = Double.valueOf(Value);
        //                if (d>0)    e = d/10;
        //                else        e = d;
        //                data.add(new SpaData(Sensor1IceContID, e,d));
        val (x1, y1) = getd10(str.substring(5, 9))
        val sens1ice = SpaDataFrame(y1, x1, Sensor1IceContID)
        
        //                Value = msg.substring(9, 13);
        //                d = Double.valueOf(Value);
        //                if (d>0)    e = d/10;
        //                else        e = d;
        //                data.add(new SpaData(Sensor1WaterContID, e,d));
        val (x2, y2) = getd10(str.substring(9, 13))
        val sens1wat = SpaDataFrame(y2, x2, Sensor1WaterContID)
        
        //                Value = msg.substring(13, 17);
        //                d = Double.valueOf(Value);
        //                data.add(new SpaData(Sensor1SnowDensID, d,d));
        val d1 = msg.substring(13, 17).toDouble
        val sens1snd = SpaDataFrame(d1, d1, Sensor1WaterContID)

        //                Value = msg.substring(17, 21);
        //                d = Double.valueOf(Value);
        //                data.add(new SpaData(Sensor1SweID, d,d));
        val d2 = msg.substring(17, 21).toDouble
        val sens1swe = SpaDataFrame(d2, d2, Sensor1SweID)

        // ok, and now?
        val retlist = List(sens1ice, sens1wat, sens1snd, sens1swe)
        retlist
      }
      case str if str.startsWith("I01") => {

		//        Value = msg.substring(5, 9);       //Sensor 2 Ice Content
		//                d = Double.valueOf(Value);
		//                if (d>0)    e = d/10;
		//                else        e = d;
		//                data.add(new SpaData(Sensor2IceContID, e,d));
        val (x1, y1) = getd10(str.substring(5, 9))
        val sens2ice = SpaDataFrame(y1, x1, Sensor2IceContID)
        
		//                Value = msg.substring(9, 13);
		//                d = Double.valueOf(Value);
		//                if (d>0)    e = d/10;
		//                else        e = d;
		//                data.add(new SpaData(Sensor2WaterContID, e,d));
        val (x2, y2) = getd10(str.substring(9, 13))
        val sens2wat = SpaDataFrame(y2, x2, Sensor2WaterContID)
        
		//                Value = msg.substring(13, 17);
		//                d = Double.valueOf(Value);
		//                data.add(new SpaData(Sensor2SnowDensID, d,d));
        val d1 = msg.substring(13, 17).toDouble
        val sens2snd = SpaDataFrame(d1, d1, Sensor2SnowDensID)
        
		//                Value = msg.substring(17, 21);
		//                d = Double.valueOf(Value);
		//                data.add(new SpaData(Sensor2SweID, d,d));
        val d2 = msg.substring(17, 21).toDouble
        val sens2swe = SpaDataFrame(d2, d2, Sensor2SweID)
        
        // ok, and now?
        val retlist = List(sens2ice, sens2wat, sens2snd, sens2swe)
        retlist
      }
      case str if str.startsWith("I04") => {
        
		//        Value = msg.substring(5, 9);
		//                d = Double.valueOf(Value);
		//                data.add(new SpaData(SnowDepthID, d,d));
        val d1 = msg.substring(5, 9).toDouble
        val sens3snd = SpaDataFrame(d1, d1, SnowDepthID)
        
		//                //Value = msg.substring(9, 13);
		//                Value = msg.substring(13, 17);
		//                d = Double.valueOf(Value);
		//                if (d>0)    e = d/10;
		//                else        e = d;
		//                data.add(new SpaData(ChipTempID, e,d));
        val (x1, y1) = getd10(str.substring(13, 17))
        val sens3cht = SpaDataFrame(y1, x1, ChipTempID)

        // ok, and now?
        val retlist = List(sens3snd, sens3cht)
        retlist
      }
      case _ => {
        // Logger.debug("no match, returning empty list?")
        val retlist = List()
        retlist
      }
    }

  }
}