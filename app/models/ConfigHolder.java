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

package models;

import play.Logger;

@Deprecated
public class ConfigHolder {

    /**
     * SMART WaspMote and Sensors config
     * 
     * BEWARE not yet used in application, application has hardcoded values
     * still please open ticket and/or change :-D ~~~~~
     */

    public static String VOCAB_NETWORK_IDENTIFIER = "koppl";

    public static String VOCAB_PREFIX_PROCEDURE = "http://vocab.smart-project.info/sensorweb/procedure";
    public static String VOCAB_PREFIX_OFFERING = "http://vocab.smart-project.info/sensorweb/offering";
    public static String VOCAB_PREFIX_FEATURE = "http://vocab.smart-project.info/sensorweb/feature";
    public static String VOCAB_PREFIX_PHENOMENON = "http://vocab.smart-project.info/sensorweb/phenomenon";

    public static String XBEE_GATEWAY_COMPORT = "COM3";
    public static String XBEE_GATEWAY_NETWORKID = "1337";
    public static String XBEE_GATEWAY_ENABLED = "false";

    // public static String MOXA_BASE_IP = "192.168.178.100";
    public static String MOXA_BASE_IP = "127.0.0.1";
    public static String MOXA_SPA_PORT = "4001";
    public static String MOXA_SPA_ENABLED = "false";
    public static String MOXA_TAROM_PORT = "4002";
    public static String MOXA_TAROM_ENABLED = "false";

    public static String UPLINK_SOS_ENABLED = "false";
    public static String UPLINK_SOS_URL = "http://mondseewebgis.hermannklug.com/52n-sos-webapp/sos/kvp?service=SOS&request=GetCapabilities&AcceptVersions=2.0.0";
    public static String UPLINK_SOS_TIMEOUT = "30000";
    public static String UPLINK_SPS_URL = "";
    public static String UPLINK_SPS_ENABLED = "false";
    public static String UPLINK_SPS_TIMEOUT = "30000";
    public static String UPLINK_MQTT_ADDRESS = "tcp://91.118.87.135:1883";
    public static String UPLINK_MQTT_USERNAME = "gateway";
    public static String UPLINK_MQTT_PASSWORD = "gateway";
    public static String UPLINK_MQTT_ENABLED = "false";
    public static String UPLINK_SES_URL = "";
    public static String UPLINK_SES_ENABLED = "false";
    public static String UPLINK_SES_TIMEOUT = "30000";

    /**
     * @return the vOCAB_NETWORK_IDENTIFIER
     */
    public static String getVOCAB_NETWORK_IDENTIFIER() {
        return VOCAB_NETWORK_IDENTIFIER;
    }

    /**
     * @param vOCAB_NETWORK_IDENTIFIER
     *            the vOCAB_NETWORK_IDENTIFIER to set
     */
    public static void setVOCAB_NETWORK_IDENTIFIER(
            String vOCAB_NETWORK_IDENTIFIER) {
        VOCAB_NETWORK_IDENTIFIER = vOCAB_NETWORK_IDENTIFIER;
    }

    /**
     * @return the vOCAB_PREFIX_PROCEDURE
     */
    public static String getVOCAB_PREFIX_PROCEDURE() {
        return VOCAB_PREFIX_PROCEDURE;
    }

    /**
     * @param vOCAB_PREFIX_PROCEDURE
     *            the vOCAB_PREFIX_PROCEDURE to set
     */
    public static void setVOCAB_PREFIX_PROCEDURE(String vOCAB_PREFIX_PROCEDURE) {
        VOCAB_PREFIX_PROCEDURE = vOCAB_PREFIX_PROCEDURE;
    }

    /**
     * @return the vOCAB_PREFIX_OFFERING
     */
    public static String getVOCAB_PREFIX_OFFERING() {
        return VOCAB_PREFIX_OFFERING;
    }

    /**
     * @param vOCAB_PREFIX_OFFERING
     *            the vOCAB_PREFIX_OFFERING to set
     */
    public static void setVOCAB_PREFIX_OFFERING(String vOCAB_PREFIX_OFFERING) {
        VOCAB_PREFIX_OFFERING = vOCAB_PREFIX_OFFERING;
    }

    /**
     * @return the vOCAB_PREFIX_FEATURE
     */
    public static String getVOCAB_PREFIX_FEATURE() {
        return VOCAB_PREFIX_FEATURE;
    }

    /**
     * @param vOCAB_PREFIX_FEATURE
     *            the vOCAB_PREFIX_FEATURE to set
     */
    public static void setVOCAB_PREFIX_FEATURE(String vOCAB_PREFIX_FEATURE) {
        VOCAB_PREFIX_FEATURE = vOCAB_PREFIX_FEATURE;
    }

    /**
     * @return the vOCAB_PREFIX_PHENOMENON
     */
    public static String getVOCAB_PREFIX_PHENOMENON() {
        return VOCAB_PREFIX_PHENOMENON;
    }

    /**
     * @param vOCAB_PREFIX_PHENOMENON
     *            the vOCAB_PREFIX_PHENOMENON to set
     */
    public static void setVOCAB_PREFIX_PHENOMENON(String vOCAB_PREFIX_PHENOMENON) {
        VOCAB_PREFIX_PHENOMENON = vOCAB_PREFIX_PHENOMENON;
    }

    /**
     * @return the xBEE_GATEWAY_COMPORT
     */
    public static String getXBEE_GATEWAY_COMPORT() {
        return XBEE_GATEWAY_COMPORT;
    }

    /**
     * @param xBEE_GATEWAY_COMPORT
     *            the xBEE_GATEWAY_COMPORT to set
     */
    public static void setXBEE_GATEWAY_COMPORT(String xBEE_GATEWAY_COMPORT) {
        XBEE_GATEWAY_COMPORT = xBEE_GATEWAY_COMPORT;
    }

    /**
     * @return the xBEE_GATEWAY_NETWORKID
     */
    public static String getXBEE_GATEWAY_NETWORKID() {
        return XBEE_GATEWAY_NETWORKID;
    }

    /**
     * @param xBEE_GATEWAY_NETWORKID
     *            the xBEE_GATEWAY_NETWORKID to set
     */
    public static void setXBEE_GATEWAY_NETWORKID(String xBEE_GATEWAY_NETWORKID) {
        XBEE_GATEWAY_NETWORKID = xBEE_GATEWAY_NETWORKID;
    }

    /**
     * @return the xBEE_GATEWAY_ENABLED
     */
    public static String getXBEE_GATEWAY_ENABLED() {
        return XBEE_GATEWAY_ENABLED;
    }

    /**
     * @param xBEE_GATEWAY_ENABLED
     *            the xBEE_GATEWAY_ENABLED to set
     */
    public static void setXBEE_GATEWAY_ENABLED(String xBEE_GATEWAY_ENABLED) {
        XBEE_GATEWAY_ENABLED = xBEE_GATEWAY_ENABLED;
    }

    /**
     * @return the mOXA_BASE_IP
     */
    public static String getMOXA_BASE_IP() {
        return MOXA_BASE_IP;
    }

    /**
     * @param mOXA_BASE_IP
     *            the mOXA_BASE_IP to set
     */
    public static void setMOXA_BASE_IP(String mOXA_BASE_IP) {
        MOXA_BASE_IP = mOXA_BASE_IP;
    }

    /**
     * @return the mOXA_SPA_PORT
     */
    public static String getMOXA_SPA_PORT() {
        return MOXA_SPA_PORT;
    }

    /**
     * @param mOXA_SPA_PORT
     *            the mOXA_SPA_PORT to set
     */
    public static void setMOXA_SPA_PORT(String mOXA_SPA_PORT) {
        MOXA_SPA_PORT = mOXA_SPA_PORT;
    }

    /**
     * @return the mOXA_SPA_ENABLED
     */
    public static String getMOXA_SPA_ENABLED() {
        return MOXA_SPA_ENABLED;
    }

    /**
     * @param mOXA_SPA_ENABLED
     *            the mOXA_SPA_ENABLED to set
     */
    public static void setMOXA_SPA_ENABLED(String mOXA_SPA_ENABLED) {
        MOXA_SPA_ENABLED = mOXA_SPA_ENABLED;
    }

    /**
     * @return the mOXA_TAROM_PORT
     */
    public static String getMOXA_TAROM_PORT() {
        return MOXA_TAROM_PORT;
    }

    /**
     * @param mOXA_TAROM_PORT
     *            the mOXA_TAROM_PORT to set
     */
    public static void setMOXA_TAROM_PORT(String mOXA_TAROM_PORT) {
        MOXA_TAROM_PORT = mOXA_TAROM_PORT;
    }

    /**
     * @return the mOXA_TAROM_ENABLED
     */
    public static String getMOXA_TAROM_ENABLED() {
        return MOXA_TAROM_ENABLED;
    }

    /**
     * @param mOXA_TAROM_ENABLED
     *            the mOXA_TAROM_ENABLED to set
     */
    public static void setMOXA_TAROM_ENABLED(String mOXA_TAROM_ENABLED) {
        MOXA_TAROM_ENABLED = mOXA_TAROM_ENABLED;
    }

    /**
     * @return the uPLINK_SOS_ENABLED
     */
    public static String getUPLINK_SOS_ENABLED() {
        return UPLINK_SOS_ENABLED;
    }

    /**
     * @param uPLINK_SOS_ENABLED
     *            the uPLINK_SOS_ENABLED to set
     */
    public static void setUPLINK_SOS_ENABLED(String uPLINK_SOS_ENABLED) {
        UPLINK_SOS_ENABLED = uPLINK_SOS_ENABLED;
    }

    /**
     * @return the uPLINK_SOS_URL
     */
    public static String getUPLINK_SOS_URL() {
        return UPLINK_SOS_URL;
    }

    /**
     * @param uPLINK_SOS_URL
     *            the uPLINK_SOS_URL to set
     */
    public static void setUPLINK_SOS_URL(String uPLINK_SOS_URL) {
        UPLINK_SOS_URL = uPLINK_SOS_URL;
    }

    /**
     * @return the uPLINK_SOS_TIMEOUT
     */
    public static String getUPLINK_SOS_TIMEOUT() {
        return UPLINK_SOS_TIMEOUT;
    }

    /**
     * @param uPLINK_SOS_TIMEOUT
     *            the uPLINK_SOS_TIMEOUT to set
     */
    public static void setUPLINK_SOS_TIMEOUT(String uPLINK_SOS_TIMEOUT) {
        UPLINK_SOS_TIMEOUT = uPLINK_SOS_TIMEOUT;
    }

    /**
     * @return the uPLINK_SPS_URL
     */
    public static String getUPLINK_SPS_URL() {
        return UPLINK_SPS_URL;
    }

    /**
     * @param uPLINK_SPS_URL
     *            the uPLINK_SPS_URL to set
     */
    public static void setUPLINK_SPS_URL(String uPLINK_SPS_URL) {
        UPLINK_SPS_URL = uPLINK_SPS_URL;
    }

    /**
     * @return the uPLINK_SPS_ENABLED
     */
    public static String getUPLINK_SPS_ENABLED() {
        return UPLINK_SPS_ENABLED;
    }

    /**
     * @param uPLINK_SPS_ENABLED
     *            the uPLINK_SPS_ENABLED to set
     */
    public static void setUPLINK_SPS_ENABLED(String uPLINK_SPS_ENABLED) {
        UPLINK_SPS_ENABLED = uPLINK_SPS_ENABLED;
    }

    /**
     * @return the uPLINK_SPS_TIMEOUT
     */
    public static String getUPLINK_SPS_TIMEOUT() {
        return UPLINK_SPS_TIMEOUT;
    }

    /**
     * @param uPLINK_SPS_TIMEOUT
     *            the uPLINK_SPS_TIMEOUT to set
     */
    public static void setUPLINK_SPS_TIMEOUT(String uPLINK_SPS_TIMEOUT) {
        UPLINK_SPS_TIMEOUT = uPLINK_SPS_TIMEOUT;
    }

    /**
     * @return the uPLINK_MQTT_ADDRESS
     */
    public static String getUPLINK_MQTT_ADDRESS() {
        return UPLINK_MQTT_ADDRESS;
    }

    /**
     * @param uPLINK_MQTT_ADDRESS
     *            the uPLINK_MQTT_ADDRESS to set
     */
    public static void setUPLINK_MQTT_ADDRESS(String uPLINK_MQTT_ADDRESS) {
        UPLINK_MQTT_ADDRESS = uPLINK_MQTT_ADDRESS;
    }

    /**
     * @return the uPLINK_MQTT_USERNAME
     */
    public static String getUPLINK_MQTT_USERNAME() {
        return UPLINK_MQTT_USERNAME;
    }

    /**
     * @param uPLINK_MQTT_USERNAME
     *            the uPLINK_MQTT_USERNAME to set
     */
    public static void setUPLINK_MQTT_USERNAME(String uPLINK_MQTT_USERNAME) {
        UPLINK_MQTT_USERNAME = uPLINK_MQTT_USERNAME;
    }

    /**
     * @return the uPLINK_MQTT_PASSWORD
     */
    public static String getUPLINK_MQTT_PASSWORD() {
        return UPLINK_MQTT_PASSWORD;
    }

    /**
     * @param uPLINK_MQTT_PASSWORD
     *            the uPLINK_MQTT_PASSWORD to set
     */
    public static void setUPLINK_MQTT_PASSWORD(String uPLINK_MQTT_PASSWORD) {
        UPLINK_MQTT_PASSWORD = uPLINK_MQTT_PASSWORD;
    }

    /**
     * @return the uPLINK_MQTT_ENABLED
     */
    public static String getUPLINK_MQTT_ENABLED() {
        return UPLINK_MQTT_ENABLED;
    }

    /**
     * @param uPLINK_MQTT_ENABLED
     *            the uPLINK_MQTT_ENABLED to set
     */
    public static void setUPLINK_MQTT_ENABLED(String uPLINK_MQTT_ENABLED) {
        UPLINK_MQTT_ENABLED = uPLINK_MQTT_ENABLED;
    }

    /**
     * @return the uPLINK_SES_URL
     */
    public static String getUPLINK_SES_URL() {
        return UPLINK_SES_URL;
    }

    /**
     * @param uPLINK_SES_URL
     *            the uPLINK_SES_URL to set
     */
    public static void setUPLINK_SES_URL(String uPLINK_SES_URL) {
        UPLINK_SES_URL = uPLINK_SES_URL;
    }

    /**
     * @return the uPLINK_SES_ENABLED
     */
    public static String getUPLINK_SES_ENABLED() {
        return UPLINK_SES_ENABLED;
    }

    /**
     * @param uPLINK_SES_ENABLED
     *            the uPLINK_SES_ENABLED to set
     */
    public static void setUPLINK_SES_ENABLED(String uPLINK_SES_ENABLED) {
        UPLINK_SES_ENABLED = uPLINK_SES_ENABLED;
    }

    /**
     * @return the uPLINK_SES_TIMEOUT
     */
    public static String getUPLINK_SES_TIMEOUT() {
        return UPLINK_SES_TIMEOUT;
    }

    /**
     * @param uPLINK_SES_TIMEOUT
     *            the uPLINK_SES_TIMEOUT to set
     */
    public static void setUPLINK_SES_TIMEOUT(String uPLINK_SES_TIMEOUT) {
        UPLINK_SES_TIMEOUT = uPLINK_SES_TIMEOUT;
    }

    public static void logRunningConfigHolder() {

        Logger.info("sensorweb.vocab.network.identifier = "
                + ConfigHolder.VOCAB_NETWORK_IDENTIFIER);
        Logger.info("sensorweb.vocab.prefix.procedure = "
                + ConfigHolder.VOCAB_PREFIX_PROCEDURE);
        Logger.info("sensorweb.vocab.prefix.offering = "
                + ConfigHolder.VOCAB_PREFIX_OFFERING);
        Logger.info("sensorweb.vocab.prefix.feature = "
                + ConfigHolder.VOCAB_PREFIX_FEATURE);
        Logger.info("sensorweb.vocab.prefix.phenomenon = "
                + ConfigHolder.VOCAB_PREFIX_PHENOMENON);

        Logger.info("sensorweb.xbee.gateway.comport = "
                + ConfigHolder.XBEE_GATEWAY_COMPORT);
        Logger.info("sensorweb.xbee.gateway.networkid = "
                + ConfigHolder.XBEE_GATEWAY_NETWORKID);
        Logger.info("sensorweb.xbee.gateway.enabled = "
                + ConfigHolder.XBEE_GATEWAY_ENABLED);

        Logger.info("sensorweb.moxa.base.ip = " + ConfigHolder.MOXA_BASE_IP);
        Logger.info("sensorweb.moxa.spa.port = " + ConfigHolder.MOXA_SPA_PORT);
        Logger.info("sensorweb.moxa.spa.enabled = "
                + ConfigHolder.MOXA_SPA_ENABLED);
        Logger.info("sensorweb.moxa.tarom.port = "
                + ConfigHolder.MOXA_TAROM_PORT);
        Logger.info("sensorweb.moxa.tarom.enabled = "
                + ConfigHolder.MOXA_TAROM_ENABLED);

        Logger.info("sensorweb.uplink.sos.enabled = "
                + ConfigHolder.UPLINK_SOS_ENABLED);
        Logger.info("sensorweb.uplink.sos.url = " + ConfigHolder.UPLINK_SOS_URL);
        Logger.info("sensorweb.uplink.sos.timeout = "
                + ConfigHolder.UPLINK_SOS_TIMEOUT);

        Logger.info("sensorweb.uplink.sps.url = " + ConfigHolder.UPLINK_SPS_URL);
        Logger.info("sensorweb.uplink.sps.enabled = "
                + ConfigHolder.UPLINK_SPS_ENABLED);
        Logger.info("sensorweb.uplink.sps.timeout = "
                + ConfigHolder.UPLINK_SPS_TIMEOUT);

        Logger.info("sensorweb.uplink.mqtt.address = "
                + ConfigHolder.UPLINK_MQTT_ADDRESS);
        Logger.info("sensorweb.uplink.mqtt.username = "
                + ConfigHolder.UPLINK_MQTT_USERNAME);
        Logger.info("sensorweb.uplink.mqtt.password = "
                + ConfigHolder.UPLINK_MQTT_PASSWORD);
        Logger.info("sensorweb.uplink.mqtt.enabled = "
                + ConfigHolder.UPLINK_MQTT_ENABLED);

        Logger.info("sensorweb.uplink.ses.url = " + ConfigHolder.UPLINK_SES_URL);
        Logger.info("sensorweb.uplink.ses.enabled = "
                + ConfigHolder.UPLINK_SES_ENABLED);
        Logger.info("sensorweb.uplink.ses.timeout = "
                + ConfigHolder.UPLINK_SES_TIMEOUT);
    }
}
