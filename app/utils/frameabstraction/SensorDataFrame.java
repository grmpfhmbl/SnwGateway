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
 * Imported from Gateway meshlium
 * 
 * only Christian knows what happening relly, we better reverse 
 * engineer quickly and refacor and make it modern and reactive :-p
 * 
 * note as of 19.98.2014 AK
 * 
 * LICENSE designated ASL 2.0, extra packages used xbee api and rxtx to be checked
 */
package utils.frameabstraction;

import java.util.ArrayList;

import com.rapplogic.xbee.api.XBeeAddress64;


public class SensorDataFrame extends DataFrame {

    private ArrayList<SensorData> data;
    private java.sql.Timestamp ts;
    
    public SensorDataFrame(short packedId) 
    {
        super(packedId);
        data = new ArrayList<SensorData>();
        ts = null;
    }

    public SensorDataFrame(short packedId, XBeeAddress64 addr64, java.sql.Timestamp ts) {
        super(packedId, addr64);
        data = new ArrayList<SensorData>();
        this.ts = ts;
    }
    
    public void addSensorData(SensorData sd)
    {
        data.add(sd);
    }

    public ArrayList<SensorData> getSensorData()
    {
        return data;
    }
    public java.sql.Timestamp getTimestamp()
    {
        return this.ts;
    }
}


