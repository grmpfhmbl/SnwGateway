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
package utils;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.Timestamp;

import actors.ActorSupervisor;
import actors.DbActor;
import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.wpan.RxResponse64;
import play.Logger;
import play.libs.Akka;
import utils.frameabstraction.*;
import actors.LogDataMessage;
import actors.XBeeDataMessage;
import akka.actor.ActorSelection;

import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;

public class WaspComm {

    XBee xb = null;
    ActorSelection dbActor = null;

    // private static org.apache.log4j.Logger logger =
    // org.apache.log4j.Logger.getLogger( WaspComm.class );
    final static Logger.ALogger logger = Logger.of("application." + WaspComm.class.getCanonicalName());

    /**
     * Opens serial port, connects to zigbee gateway with baudrate 38400
     * 
     * @param serialport
     *            e.g. "COM9"
     * @throws XBeeException
     */
    public void open(String serialport) throws XBeeException {
        xb = new XBee();
        xb.open(serialport, 38400);
        dbActor = Akka.system().actorSelection("/user/" + ActorSupervisor.ActorName() +"/" + DbActor.ActorName());
    }

    /**
     * Closes serialport
     */
    public void close() {
        xb.close();
    }

    /**
     * Sends data to addr64 with payload
     * 
     * @param addr64
     *            64bit network address
     * @param payload
     *            data
     * @throws XBeeException
     */
    public void SendToWasp(XBeeAddress64 addr64, int[] payload)
            throws XBeeException {
        ZNetTxRequest txRequest = new ZNetTxRequest(
                ZNetTxRequest.NO_RESPONSE_FRAME_ID, addr64,
                XBeeAddress16.ZNET_BROADCAST,
                ZNetTxRequest.DEFAULT_BROADCAST_RADIUS,
                ZNetTxRequest.Option.UNICAST, payload);

        logger.debug("zb request is " + txRequest.getXBeePacket().getPacket());
        logger.debug("sending tx " + txRequest);
        xb.sendAsynchronous(txRequest);
    }

    public DataFrame read(int timeout) throws DataFrameParseException, SQLException, ClassNotFoundException {
        XBeeResponse resp;
        DataFrame dataFrame = null;
        try {
            resp = xb.getResponse(timeout);
            int[] rawData = resp.getRawPacketBytes();
            logger.debug("Resp: " + resp.toString());

            if ((resp.getApiId() == ApiId.ZNET_RX_RESPONSE)
                    || (resp.getApiId() == ApiId.ZNET_EXPLICIT_RX_RESPONSE)
            ) {
                ZNetRxResponse rxResp = (ZNetRxResponse) resp;
                int[] rxData = rxResp.getData();
                dataFrame = parseFrame(rxResp);
                logger.debug("rxData : " + rxData.toString());

                if (dataFrame instanceof SensorDataFrame) {
                    logger.debug("SensorDataFrame received");
                    SensorDataFrame sensDataFrame = (SensorDataFrame)dataFrame;
                    saveRSSIToDatabase(sensDataFrame.getAddr64asString(),
                            sensDataFrame.getTimestamp());
                }
            }
            else if (resp.getApiId() == ApiId.RX_64_RESPONSE) {
                RxResponse64 rxResponse = (RxResponse64)resp;
                dataFrame = parseFrame(rxResponse);
            }
            else {
                logger.warn("Unknown response type: " + resp.getApiId());
            }

            return dataFrame;
        } catch (XBeeTimeoutException ex) {
            logger.debug("XBeeTimeoutException", ex);
            return null;
        } catch (XBeeException ex) {
            logger.error("XBeeException: " + ex.getMessage(), ex);
            LogDataMessage logMessage = new LogDataMessage("XBeeException",
                    WaspComm.class.getName() + " XBeeException" + ex);
            dbActor.tell(logMessage, null);
            return null;
        }
    }

    private void saveRSSIToDatabase(String extendedAddress, Timestamp ts)
            throws XBeeException, ClassNotFoundException, SQLException {
        AtCommand at = new AtCommand("DB");
        this.xb.sendAsynchronous(at);
        XBeeResponse atResponse = this.xb.getResponse();
        if (atResponse.getApiId() == ApiId.AT_RESPONSE) {
            int sid = 45; // this is the sensor ID for RSSI!
            final AtCommandResponse atCommandResponse = (AtCommandResponse)atResponse;
            Double data = (double) -1*atCommandResponse.getValue()[0];

            XBeeDataMessage xbMessage = new XBeeDataMessage(extendedAddress, sid, ts, data, data);
            dbActor.tell(xbMessage, null);
            logger.info("RSSI of last response is " + data);
        } else {
            logger.info("expected RSSI, but received " + atResponse.toString());
        }
    }

    /**
     * parse dataframe from ZNetRxResponse (XBee ZB)
     * @param rxresp
     * @return
     * @throws Exception
     */
    private DataFrame parseFrame(ZNetRxResponse rxresp) throws DataFrameParseException {
        XBeeAddress64 addr64 = rxresp.getRemoteAddress64();
        int[] rxData = rxresp.getData();
        DataFrame fr = null;

        if (rxData.length < 6) {
            throw new DataFrameParseException("Message too Short");
        }

        int packetID = rxData[0];

        logger.debug("packetID " + Integer.toHexString(packetID) + "  " + packetID);

        switch (packetID) {
        case 0x55: // SensorData
            if (rxData.length < 12) { // 9 Bytes Header + minimum 1Byte Data
                throw new DataFrameParseException("Message too Short");
            }
            //TODO SR this must be done by the data frame itself and not by WaspComm
            fr = extractSensorData(rxData, addr64);
            calculateSensorData((SensorDataFrame) fr);

            break;
        case 0x56: // Timestamp
            if (rxData.length < 7) // 1 Byte Header + min 1 timestamp (6bytes)
                throw new DataFrameParseException("Message too Short");
            //TODO SR this must be done by the data frame itself and not by WaspComm
            fr = extractTimeStamps(rxData, addr64);

            break;
        case 0x57: // Status Frame
            if (rxData.length < 2) // 6 Byte Header + min 1 status with 1 byte
                throw new DataFrameParseException("Message too Short");
            //TODO SR this must be done by the data frame itself and not by WaspComm
            fr = extractStatusData(rxData, addr64);
            break;
        default:
            throw new DataFrameParseException("Unknown packet ID: " + packetID);
        }
        return fr;
    }

    /**
     * parse dataframe from RxResponse64 (XBee 802)
     * @param rxresp
     * @return
     * @throws Exception
     */
    private DataFrame parseFrame(RxResponse64 rxresp) throws DataFrameParseException {
        XBeeAddress64 addr64 = rxresp.getRemoteAddress();
        int[] rxData = rxresp.getData();

        DataFrame fr = null;

        //check if first 3 bytes are ASCII "<=>" ==> WaspFrameAscii
        if (rxData[0] == 0x3C && rxData[1] == 0x3D && rxData[2] == 0x3E) {
            logger.debug("Found WaspFrameAscii. FrameType: " + rxData[4]);
            WaspFrameAscii wfr = new WaspFrameAscii((short)rxData[4], addr64);
            wfr.setData(rxData);
            logger.debug("Packet: " + wfr.getDataAsString());
            fr = wfr;
        }
        else {
            logger.warn("Unknown DataFrame type.");
            throw new DataFrameParseException("Unknown DataFrame type");
        }
        return fr;
    }

    public static TimeStampFrame extractTimeStamps(int[] data,
                                                   XBeeAddress64 addr64) {
        TimeStampFrame tsf = new TimeStampFrame((short) 0x56, addr64);
        short numts = (short) data[6];
        int counter = 7;
        int year;
        int month;
        int date;
        int hour;
        int minute;
        int second;

        for (int i = 0; i < numts; i++) {

            year = data[counter++];
            month = data[counter++];
            date = data[counter++];
            hour = data[counter++];
            minute = data[counter++];
            second = data[counter++];

            String tsstring = String.format("%04d-%02d-%02d %02d:%02d:%02d.0",
                    year + 2000, month, date, hour, minute, second);
            java.sql.Timestamp tss = Timestamp.valueOf(tsstring);

            tsf.addTimeStamp(tss);

        }
        return tsf;
    }

    public StatusDataFrame extractStatusData(int[] data,
                                                    XBeeAddress64 addr64) throws DataFrameParseException {
        final byte head = 6;
        int counter = head + 6; // Message Head + 6byte time

        // "2011-10-02 18:48:05.123456";
        // String tsstring = String.valueOf(data[6]+2000) + "-" + data[7] + "-"
        // + data[8] + " " + data[9] + ":" + data[10] + ":" + data[11] +".0";
        String tsstring = String.format("%04d-%02d-%02d %02d:%02d:%02d.0",
                (int) data[6] + 2000, data[7], data[8], data[9], data[10],
                data[11]);
        java.sql.Timestamp tss = Timestamp.valueOf(tsstring);

        StatusDataFrame sdf = new StatusDataFrame((short) 0x57, addr64, tss);
        if (data.length < head + 2)
            throw new DataFrameParseException("Message too Short");

        do {
            if (data[counter] == 0x57) // Battery Info 'W'
            {
                StatusData sd = new StatusData((short) 0x57, data[++counter]);
                sdf.addStatusData(sd);
                counter++;
            } else if (data[counter] == 0x45) // Error Status 'E'
            {
                StatusData sd = new StatusData((short) 0x45,
                        data[++counter] << 8 | data[++counter]);
                sdf.addStatusData(sd);
                counter++;
            } else {
                // other message
                break;
            }
        } while (counter != data.length);

        return sdf;

    }

    public static SensorDataFrame extractSensorData(int[] data,
                                                    XBeeAddress64 addr64) throws DataFrameParseException {
        final byte head = 12;
        int counter = head + 3;

        short count32 = (short) data[12];
        short count16 = (short) data[13];
        short count8 = (short) data[14];

        // "2011-10-02 18:48:05.123456";
        // String tsstring = String.valueOf(data[6]+2000) + "-" + data[7] + "-"
        // + data[8] + " " + data[9] + ":" + data[10] + ":" + data[11] +".0";
        String tsstring = String.format("%04d-%02d-%02d %02d:%02d:%02d.0",
                (int) data[6] + 2000, data[7], data[8], data[9], data[10],
                data[11]);
        java.sql.Timestamp tss = Timestamp.valueOf(tsstring);

        SensorDataFrame sf = new SensorDataFrame((short) 0x55, addr64, tss);

        int datacount = count32 * 6 + count16 * 4 + count8 * 3;

        if (data.length < head + datacount) {
            throw new DataFrameParseException("Message too Short");
        }

        if (count32 > 0) {
            for (int i = 0; i < count32; i++) {
                int sensid = data[counter++] << 8 | data[counter++];
                long sensdat = ((long) data[counter++]) << 24
                        | ((long) data[counter++]) << 16
                        | ((long) data[counter++]) << 8
                        | ((long) data[counter++]);
                SensorData sd = new SensorData(sensid, sensdat);
                sf.addSensorData(sd);
            }
        }

        if (count16 > 0) {
            for (int i = 0; i < count16; i++) {

                int sensid = data[counter++] << 8 | data[counter++];
                long sensdat = ((long) data[counter++]) << 8
                        | ((long) data[counter++]);
                SensorData sd = new SensorData(sensid, sensdat);
                sf.addSensorData(sd);
            }
        }

        if (count8 > 0) {
            for (int i = 0; i < count8; i++) {

                int sensid = data[counter++] << 8 | data[counter++];
                long sensdat = ((long) data[counter++]);
                SensorData sd = new SensorData(sensid, sensdat);
                sf.addSensorData(sd);
            }
        }
        return sf;

    }

    public void calculateSensorData(SensorDataFrame sdf) {
        int sensid;
        long rawdata;
        for (SensorData sd : sdf.getSensorData()) {
            sensid = sd.getSensorID();

            // Sensors with float values from agriculture board
            if (sensid == 0x0001 || sensid == 0x0002 || sensid == 0x0003
                    || sensid == 0x0004 || sensid == 0x0005 || sensid == 0x000A
                    || sensid == 0x000B || sensid == 0x000C) {
                rawdata = sd.getRawData();
                double d = Helper.longieee2double(rawdata);
                sd.setData(d);
            }
            // Sensors with 16bit values (watermark) from agriculture board
            else if (sensid == 0x0006 || sensid == 0x0007 || sensid == 0x00008) {
                rawdata = sd.getRawData();
                double d = Helper.longieee2double(rawdata);
                sd.setData(d);
            }
            //Waterlevel
            else if (sensid == 0x001C || sensid == 0x001D) {
                // Waterlevel 4 to 20mA
                // Output is measured at 160E
                // 4ma is 0,64V (ADC 198) 20mA is 3,2 (ADC 993)
                //FIXME SREI je nach Sonde müssten das 4mA = 0m und 20mA = 10m sein. sprich (d-4)*10/16 = Meter?
                rawdata = sd.getRawData();
                double d = Helper.mapValues((double) rawdata, 198.0, 993.0,
                                            4.0, 20.0);
                sd.setData(d);
            }
            //temperature sensors hydro board (custom board)
            else if ((sensid >= 0x0020) && (sensid <= 0x027)) {
                rawdata = sd.getRawData();
                double d = Helper.longieee2double(rawdata);
                sd.setData(d);
            }
            // Battery voltage mainstation 12v
            //TODO SREI do we still need this?! Where the heck did that come from anyways?
            else if (sensid == 0x002C) {
                rawdata = sd.getRawData();
                double d = (double) rawdata;
                sd.setData(d);
            }
            // Soil Moisture 10HS
            else if ((sensid >= 0x0028) && (sensid <= 0x002A)) {
                rawdata = sd.getRawData();
                double d = (double) rawdata;
                d = d*(3.3/1024)*1000;
                double  vwc = Math.pow(d, 3)*2.97*Math.pow(10,-9) - Math.pow(d,2)*(7.37) *Math.pow(10,-6)+ d*6.69* Math.pow(10,-3)-1.92;
                sd.setData(vwc * 100);
            }
            // Davis RainCollector 2
            else if (sensid == 0x0009) {
                rawdata = sd.getRawData();
                sd.setData((double)rawdata * 0.2);
            }
            //Gas board HTL
            else if (sensid >= 0x000D && sensid <= 0x0013) {
                rawdata = sd.getRawData();
                double d = Helper.longieee2double(rawdata);
                sd.setData(d);
            }
            else if (sensid == 0x0058) {
                rawdata = sd.getRawData();
                double d = Helper.longieee2double(rawdata);
                sd.setData(d);
            }
            //SMART Water
            else if (sensid >= 0x00A0 && sensid <= 0x00A2) {
                rawdata = sd.getRawData();
                double d = Helper.longieee2double(rawdata);
                sd.setData(d);
            }

            // else if (sensid == 0x0021)
            // {
            // rawdata = sd.getRawData();
            // short[] s = Helper.long2unsignedShortArray(rawdata);
            // cdata = (double)s[6];
            // cdata += ((double)s[7]) / 100;
            // if (s[5] > 0)
            // cdata *= -1;
            // //double sensdat = Math.round(cdata*100) / 100.00;
            // sd.setData(Double.valueOf(cdata));
            // }
            // else if (sensid == 0x0022)
            // {
            // rawdata = sd.getRawData();
            // short[] s = Helper.long2unsignedShortArray(rawdata);
            // cdata = (double)s[6];
            // cdata += ((double)s[7]) / 100;
            // if (s[5] > 0)
            // cdata *= -1;
            // //double sensdat = Math.round(cdata*100) / 100.00;
            // sd.setData(Double.valueOf(cdata));
            // }
            // else if (sensid == 0x0023)
            // {
            // rawdata = sd.getRawData();
            // short[] s = Helper.long2unsignedShortArray(rawdata);
            // cdata = (double)s[6];
            // cdata += ((double)s[7]) / 100;
            // if (s[5] > 0)
            // cdata *= -1;
            // //double sensdat = Math.round(cdata*100) / 100.00;
            // sd.setData(Double.valueOf(cdata));
            // }
            else {
                logger.error(WaspComm.class.getName() + " Sensor ID "+ sensid + " invalid.");
                ActorSelection logDbActor = Akka.system().actorSelection(
                        "/user/"+ DbActor.ActorName());
                LogDataMessage logMessage = new LogDataMessage("XBeeException",
                        WaspComm.class.getName() + " Sensor ID "+ sensid + " invalid.");
                logDbActor.tell(logMessage, null);
            }
        }
    }

}
