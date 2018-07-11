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

    private void setUsedSerialPort(String usedSerialPort) {
        this.usedSerialPort = usedSerialPort;
    }

    private void init() throws XBeeException {
        waspcom = new WaspComm();
        waspcom.open(this.usedSerialPort);
        dbActor = Akka.system().actorSelection("/user/" + ActorSupervisor.ActorName() +"/" + DbActor.ActorName());
    }

    private void uninit() {
        waspcom.close();
    }

    /**
     * process received dataframe
     *
     * @param df DataFrame to be procesed.
     * @throws XBeeException
     */
    private void process(DataFrame df) throws XBeeException {
        if (df == null) {
            logger.debug("No data frame received");
        } else {
            if (df instanceof SensorDataFrame) {
                logger.info("SensorDataFrame received");
                SensorDataFrame sensDataFrame = (SensorDataFrame) df;

                logger.info("Sender: " + sensDataFrame.getAddr64asString());

                Calendar calendar = Calendar.getInstance();
                java.util.Date now = calendar.getTime();
                Timestamp nowTimestamp = new java.sql.Timestamp(now.getTime());
                final Timestamp dataframeTimestamp = sensDataFrame.getTimestamp();
                long diff = Helper.getTimeTifference(nowTimestamp, dataframeTimestamp);
                logger.debug("Time difference dataframe<>now: " + String.valueOf(diff));

                if (diff > 60000) {
                    logger.info("Time difference between received data and current time is: " + diff);
                    logger.info("Sending UpdateTime packet to " + sensDataFrame.getAddr64asString());
                    int m[] = SendFrame.updateNodeTime(nowTimestamp);
                    waspcom.sendToWaspmote(sensDataFrame.getAddr64(), m);
                }

                // ---> Store all sensor data <---
                for (SensorData sd : sensDataFrame.getSensorData()) {
                    try {
                        logger.debug("SensorID: " + Integer.toHexString(sd.getSensorID()));
                        logger.debug(" - Data: " + (new DecimalFormat("0.00")).format(sd.getData()));
                        logger.debug(" - TimeStamp: " + dataframeTimestamp.toString());

                        // put into case class in DB actor?
                        String addr = sensDataFrame.getAddr64asString();
                        int sid = sd.getSensorID();
                        java.sql.Timestamp ts;

                        if (diff > 60000) {
                            logger.warn("Time difference too big, using 'now' as timestamp for database insert");
                            ts = nowTimestamp;
                        }
                        else {
                            ts = dataframeTimestamp;
                        }

                        Double raw = sd.getRawDataAsDouble();
                        Double data = sd.getData();

                        XBeeDataMessage xbMessage = new XBeeDataMessage(addr, sid, ts, raw, data);
                        dbActor.tell(xbMessage, null);
                    }
                    catch (Exception e) {
                        logger.error("Exception: ", e);
                    }
                }
            }
            else if (df instanceof TimeStampFrame) {
                // FIXME this is deprecated - delete code!
                logger.debug("TimeStampFrame received");
                TimeStampFrame timeStampFrame = (TimeStampFrame) df;

                logger.debug(timeStampFrame.getAddr64asString());
                Calendar nowa = Calendar.getInstance();
                logger.debug(Long.toString(nowa.getTimeInMillis()));

                ArrayList<java.sql.Timestamp> ArrayTs = timeStampFrame.GetTimeStamps();
                // ---> Store all timestamps <---
                for (java.sql.Timestamp actts : ArrayTs) {
                    logger.debug("Timestamp" + actts.toString());

                    String addr = timeStampFrame.getAddr64asString();
                    int sid = 9;
                    Double raw = 0.0;
                    Double data = 0.0;

                    XBeeDataMessage xbMessage = new XBeeDataMessage(addr, sid, actts, raw, data);
                    dbActor.tell(xbMessage, null);
                }
            } else if (df instanceof StatusDataFrame) {
                logger.info("StatusDataFrame received");
                StatusDataFrame statDataFrame = (StatusDataFrame) df;

                logger.info("Sender: " + statDataFrame.getAddr64asString());

                Calendar calendar = Calendar.getInstance();
                java.util.Date now = calendar.getTime();
                Timestamp nowTimestamp = new java.sql.Timestamp(now.getTime());
                final Timestamp dataframeTimestamp = statDataFrame.getTimestamp();
                long diff = Helper.getTimeTifference(nowTimestamp, dataframeTimestamp);
                logger.debug("Time difference dataframe<>now: " + String.valueOf(diff));


                // ---> Store all status infos <---
                for (StatusData sd : statDataFrame.getStatusData()) {
                    logger.debug("StatusData ("
                                         + Integer.toHexString(sd.getStatusId()) + ":"
                                         + Integer.toHexString(sd.getStatusValue()) + ")");

                    if (sd.getStatusId() == (int) 'W') {

                        String addr = statDataFrame.getAddr64asString();
                        int sid = 0x0101;
                        java.sql.Timestamp ts;

                        if (diff > 60000) {
                            logger.warn("Time difference too big, using 'now' as timestamp for database insert");
                            ts = nowTimestamp;
                        }
                        else {
                            ts = dataframeTimestamp;
                        }

                        Double raw = (double) sd.getStatusValue();
                        Double data = (double) sd.getStatusValue();

                        XBeeDataMessage xbMessage = new XBeeDataMessage(addr, sid, ts, raw, data);
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
        //FIXME open WaspComm with resources to save finally.
        //FIXME this has to go to the actor itself. No thread needed, just have a readFromXbee message and schedule this every x seconds.
        try {
            this.init();
            while (true) // --- Main infinite loop ---
            {
                try {
                    DataFrame df = waspcom.read(10);
                    process(df);
                    Thread.sleep(10000);
                } catch (DataFrameParseException|XBeeException ex) {
                    logger.error("Error while reading / processing XBee data: " + ex.getMessage(), ex);
                } catch (InterruptedException e) {
                    logger.warn("Thread sleep was interrupted.", e);
                } catch (Exception e) {
                    logger.error("(in while true) THIS SHOULD NEVER HAPPEN! MAKE NEW CATCH FOR THIS!!!", e);
                }
            }
        } catch (XBeeException ex) {
            logger.error("Exception while running Thread.");
        }
        catch (Exception e) {
            logger.error("(in run())THIS SHOULD NEVER HAPPEN! MAKE NEW CATCH FOR THIS!!!", e);
        } finally {
            if (this.waspcom != null) {
                this.uninit();
            }
        }
    }
}