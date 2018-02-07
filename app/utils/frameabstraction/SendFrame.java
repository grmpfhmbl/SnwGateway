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


public class SendFrame {
    private static int FRAGMENT_NR = 0x01;
    private static int FIRST_FRAG_INDI = (int)'#';
    private static int SOURCE_TYPE_ID64_MAC = 1;       //64-bit MAC Address ID
    private static int SOURCE_TYPE_ID16_NW = 0;        //16-bit Network Address ID
    
    private static int HEAD_SIZE = 4;
    
    /**
     * Generate updateNodeTime SendFrame
     * @param ts Actual timestamp
     * @return 
     */
    public static int[] updateNodeTime(java.sql.Timestamp ts)
    {
        int[] resp = new int[HEAD_SIZE+2+7];
     
        String tim = ts.toString();     //format:  yyyy-mm-dd hh:mm:ss.fffffffff
        resp[0] = 0x60;     //Set Node Time ID
        resp[1] = FRAGMENT_NR;
        resp[2] = FIRST_FRAG_INDI;
        resp[3] = SOURCE_TYPE_ID16_NW;
        resp[4] = 0x00;                 //Stands for Gateway but is unused
        resp[5] = 0x00;                 //Stands for Gateway but is unused
       
//        String test = tim.substring(2, 4);
//        int ab = Integer.valueOf(test);
        
        resp[6] = Integer.valueOf(tim.substring(2, 4));     //year
        resp[7] = Integer.valueOf(tim.substring(5, 7));     //month
        resp[8] = Integer.valueOf(tim.substring(8, 10));     //date
        resp[9] =  ts.getDay();   //day of week
        resp[10] = Integer.valueOf(tim.substring(11, 13));    //hours
        resp[11] = Integer.valueOf(tim.substring(14, 16));    //minutes
        resp[12] = Integer.valueOf(tim.substring(17, 19));    //seconds
        
        return resp;
    }
}
