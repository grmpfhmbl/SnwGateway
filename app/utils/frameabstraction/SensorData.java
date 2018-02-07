/*
 * Copyright 2015 Smart Aquifer Characterisation (SAC) Programme (http://www.gns.cri
 * .nz/Home/Our-Science/Environment-and-Materials/Groundwater/Research-Programmes/SMART-Aquifer-Characterisation)
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
 * Imported from Gateway meshlium
 * <p>
 * only Christian knows what's happening really, we better reverse
 * engineer quickly and refactor and make it modern and reactive :-p
 * <p>
 * note as of 19.98.2014 AK
 * <p>
 * LICENSE designated ASL 2.0, extra packages used xbee api and rxtx to be checked
 */
package utils.frameabstraction;

/**
 * This seems equivalent of the SpaDataFrame  case class I used in the Actors
 * ak, 19.08.2014
 *
 */
public class SensorData {
    private long rawData;
    private Double data = null;
    private int sensorID;

//    public SensorData() { // empty
//    }

    public SensorData(int sensorID, long rawData) {
        this.sensorID = sensorID;
        this.rawData = rawData;
    }

    public int getSensorID() {
        return sensorID;
    }

    public long getRawData() {
        return rawData;
    }

    public Double getRawDataAsDouble() {
        return Double.valueOf((double) rawData);
    }


    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }


}
