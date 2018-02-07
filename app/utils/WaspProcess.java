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

package utils;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import actors.ActorSupervisor;
import actors.DbActor;
import play.Logger;
import play.libs.Akka;
import utils.frameabstraction.*;
import actors.LogDataMessage;
import actors.XBeeDataMessage;
import akka.actor.ActorSelection;

import com.rapplogic.xbee.api.XBeeException;

public class WaspProcess implements Runnable {
    final static Logger.ALogger logger = Logger.of("application." + WaspProcess.class.getCanonicalName());

    private static WaspProcess instance = null;
    private String usedSerialPort = null;
    WaspComm waspcom = null;
    ActorSelection dbActor = null;

    boolean frameProc = false;

    public WaspProcess(String usedSerialPort) {
        this.setUsedSerialPort(usedSerialPort);
        logger.debug("WaspProcess using " + usedSerialPort);
    }

    // public WaspProcess() throws ClassNotFoundException {
    // waspcom = new WaspComm();
    // logger.debug("WaspProcess started");
    // }
    //
    // // Singleton
    // public static WaspProcess getinstance() throws ClassNotFoundException {
    // if (instance == null)
    // instance = new WaspProcess();
    // return instance;
    // }

    public void setUsedSerialPort(String usedSerialPort) {
        this.usedSerialPort = usedSerialPort;
    }

    public void init() throws XBeeException {
        // sensdb.open();
        waspcom = new WaspComm();
        waspcom.open(this.usedSerialPort);
        dbActor = Akka.system().actorSelection("/user/" + ActorSupervisor.ActorName() +"/" + DbActor.ActorName());
    }

    public void uninit() {
        waspcom.close();
    }

    public void Process() throws Exception {
        DataFrame df = null;

        try {
            // read Frame
            df = waspcom.read(10000);
        } catch (Exception ex) {
            logger.error(WaspProcess.class.getName() + " thrown then reading data from XBEE: " + ex.getMessage(), ex);
        }

        if (df == null) {
            logger.debug("No data frame received"); // No data frame received
        } else {
            if (df instanceof SensorDataFrame) {
                logger.info("SensorDataFrame received");
                SensorDataFrame sensDataFrame = (SensorDataFrame) df;

                logger.info(sensDataFrame.getAddr64asString());

                Calendar calendar = Calendar.getInstance();
                java.util.Date now = calendar.getTime();
                Timestamp currentTS = new java.sql.Timestamp(now.getTime());
                long diff = Helper.getTimeTifference(currentTS,
                                                     sensDataFrame.getTimestamp());

                logger.debug("Time difference of " + String.valueOf(diff));

                if (diff > 60000) {
                    int m[] = SendFrame.updateNodeTime(currentTS);
                    logger.debug("UpdateTime");
                    waspcom.SendToWasp(sensDataFrame.getAddr64(), m);
                }

                // ---> Store all sensor data <---
                for (SensorData sd : sensDataFrame.getSensorData()) {
                    try {
                        logger.debug("SensorID: "
                                + Integer.toHexString(sd.getSensorID()));
                        logger.debug(" - Data: "
                                + (new DecimalFormat("0.00")).format(sd.getData()));
                        logger.debug(" - TimeStamp: "
                                + sensDataFrame.getTimestamp().toString());
                        // FIXME here be persistence in DB or messagepassing to DB
                        // actor
                        // logger.info("XBee WaspProcess here be persistence in DB or messagepassing to DB actor");

                        // put into case class in DB actor?
                        String addr = sensDataFrame.getAddr64asString();
                        int sid = sd.getSensorID();
                        java.sql.Timestamp ts = sensDataFrame.getTimestamp();
                        Double raw = sd.getRawDataAsDouble();
                        Double data = sd.getData();

                        XBeeDataMessage xbMessage = new XBeeDataMessage(addr, sid,
                                ts, raw, data);
                        dbActor.tell(xbMessage, null);
                    }
                    catch (Exception e) {
                        logger.error("Exception: ", e);
                    }
                }
            }
            else if (df instanceof TimeStampFrame) {
                logger.debug("TimeStampFrame received");
                TimeStampFrame timStampFrame = (TimeStampFrame) df;

                logger.debug(timStampFrame.getAddr64asString());
                Calendar nowa = Calendar.getInstance();
                logger.debug(Long.toString(nowa.getTimeInMillis()));

                ArrayList<java.sql.Timestamp> ArrayTs = timStampFrame
                        .GetTimeStamps();
                // ---> Store all timestamps <---
                for (java.sql.Timestamp actts : ArrayTs) {
                    logger.debug("Timestamp" + actts.toString());
                    // FIXME here be persistence in DB or messagepassing to DB
                    // actor
                    // logger.info("XBee WaspProcess here be persistence in DB or messagepassing to DB actor");

                    String addr = timStampFrame.getAddr64asString();
                    int sid = 9;
                    Double raw = 0.0;
                    Double data = 0.0;

                    XBeeDataMessage xbMessage = new XBeeDataMessage(addr, sid,
                            actts, raw, data);
                    dbActor.tell(xbMessage, null);

                }
            } else if (df instanceof StatusDataFrame) {
                logger.info("StatusDataFrame received");
                StatusDataFrame statDataFrame = (StatusDataFrame) df;

                logger.info(statDataFrame.getAddr64asString());
                Calendar nowa = Calendar.getInstance();
                logger.debug("ts: " + Long.toString(nowa.getTimeInMillis()));

                // ---> Store all status infos <---
                for (StatusData sd : statDataFrame.GetStatusData()) {
                    logger.debug("StatusData ("
                                         + Integer.toHexString(sd.getStatusId()) + ":"
                                         + Integer.toHexString(sd.getStatusValue()) + ")");

                    LogDataMessage logMessage = new LogDataMessage(
                            "XBee StatusDataFrame", "StatusData "
                            + Integer.toHexString(sd.getStatusId())
                            + ":"
                            + Integer.toHexString(sd.getStatusValue()));
                    dbActor.tell(logMessage, null);

                    if (sd.getStatusId() == (int) 'W') {

                        String addr = statDataFrame.getAddr64asString();
                        int sid = 0x0101;
                        Timestamp ts = statDataFrame.GetTimestamp();
                        Double raw = (double) sd.getStatusValue();
                        Double data = (double) sd.getStatusValue();

                        XBeeDataMessage xbMessage = new XBeeDataMessage(addr,
                                                                        sid, ts, raw, data
                        );
                        dbActor.tell(xbMessage, null);

                    }
                    else if (sd.getStatusId() == (int) 'E') {
                        logger.warn("Status - unused");
                    }
                    else {
                        logger.error("Status not defined");
                    }

                }
            } else if (df instanceof WaspFrameAscii) {
                WaspFrameAscii waspFrameAscii = (WaspFrameAscii) df;
                logger.info("WaspFrameAscii received from " +waspFrameAscii.getAddr64asString());
                logger.info("Sender: " + waspFrameAscii.getAddr64asString());

                // ---> Store all sensor data <---
                for (SensorData sd : waspFrameAscii.getSensorData()) {
                    try {
                        logger.debug("SensorID: " + Integer.toHexString(sd.getSensorID()));
                        logger.debug(" - Data: " + (new DecimalFormat("0.00")).format(sd.getData()));
                        logger.debug(" - TimeStamp: " + waspFrameAscii.getTimestamp().toString());

                        XBeeDataMessage xbMessage = new XBeeDataMessage(waspFrameAscii.getAddr64asString(),
                                                                        sd.getSensorID(),
                                                                        waspFrameAscii.getTimestamp(),
                                                                        sd.getRawDataAsDouble(),
                                                                        sd.getData()
                        );
                        dbActor.tell(xbMessage, null);
                    }
                    catch (Exception e) {
                        logger.error("Exception: " + e.getMessage(), e);
                    }
                }
            } else {
                logger.error(WaspProcess.class.getName() + "Unknown Frame");
            }
        }
    }

    @Override
    public void run() {
        logger.debug("WaspProcess Thread started");
        try {
            this.init();
            while (true) // --- Main infinite loop ---
            {
                Process();
            }
        } catch (XBeeException ex) {
            ex.printStackTrace();
            // logger.error(WaspProcess.class.getName() + ": " + ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            // logger.error(WaspProcess.class.getName() + ": " + ex);
        } finally {
            if (this.waspcom != null) {
                this.uninit();
            }
        }

    }

}