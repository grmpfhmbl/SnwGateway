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

public class StatusDataFrame extends DataFrame{
    
    private ArrayList<StatusData> dataa;
    private java.sql.Timestamp ts;
    
    public StatusDataFrame(short packedId) 
    {
        super(packedId);
        dataa = new ArrayList<StatusData>();    
        ts = null;
            
    }

    public StatusDataFrame(short packedId, XBeeAddress64 addr64, java.sql.Timestamp ts) 
    {
        super(packedId, addr64);
        dataa = new ArrayList<StatusData>();
        this.ts = ts;
    }

    public ArrayList<StatusData> getStatusData()
    {
        return dataa;
    }
    
    public void addStatusData(StatusData sd)
    {
        dataa.add(sd);
    }
    public java.sql.Timestamp getTimestamp()
    {
        return this.ts;
    }
    
}
