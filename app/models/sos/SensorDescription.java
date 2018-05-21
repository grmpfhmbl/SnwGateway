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

package models.sos;

import java.net.MalformedURLException;
import java.net.URL;

public class SensorDescription {

    protected String sensorML;

    public static final String VOCAB_PREFIX_PROCEDURE = play.Play.application()
            .configuration().getString("sensorweb.vocab.prefix.procedure");
    public static final String VOCAB_PREFIX_OFFERING = play.Play.application()
            .configuration().getString("sensorweb.vocab.prefix.offering");
    public static final String VOCAB_PREFIX_FEATURE = play.Play.application()
            .configuration().getString("sensorweb.vocab.prefix.feature");
    public static final String VOCAB_PREFIX_PHENOMENON = play.Play
            .application().configuration()
            .getString("sensorweb.vocab.prefix.phenomenon");

    // returns the generated sensorML
    public String getSensorML() {
        return sensorML;
    }

    public void setSensorML(String sensorML) {
        this.sensorML = sensorML;
    }

    /**
     * minimum constructor for NETWORK sensorml per default
     * 
     * @param networkID
     */
    public SensorDescription(String networkID) {
        super();
        // create network sensorML without parent, without location, without
        // inputs/outputs
        this.sensorML = createNetworkSensorML(networkID);
    }

    /**
     * constructor for NETWORK sensorml per default with status phenomenon with
     * obsType (TRUTH, MEASUREMENT, COUNT, TEXT, CATEGORY)
     * 
     * @param networkID
     * @param obsProp
     * @param uomCode
     * @param obsType
     * @param codeSpace
     * @param network
     *            (not really used, only to make method signature unique)
     */
    public SensorDescription(String networkID, String obsProp, String uomCode,
            String obsType, String codeSpace, boolean network) {
        super();
        // boolean network

        // create network sensorML without parent, without location, without
        // inputs/outputs
        this.sensorML = createNetworkSensorMLwithStatusPhen(networkID, obsProp,
                uomCode, obsType, codeSpace);
    }

    /**
     * default constructor for predefined PLATFORM sensorml structure
     * 
     * @param platformID
     * @param networkID
     * @param observedProperty
     * @param uomCode
     * @param position
     */
    @Deprecated
    public SensorDescription(String platformID, String networkID,
            String observedProperty, String uomCode, Double[] position) {
        super();
        this.sensorML = createPlatformSensorML(platformID, networkID,
                observedProperty, uomCode, position);
    }

    /**
     * default constructor for predefined PLATFORM sensorml structure with
     * flexible phen with obsType (TRUTH, MEASUREMENT, COUNT, TEXT, CATEGORY)
     * 
     * @param platformID
     * @param networkID
     * @param observedProperty
     * @param uomCode
     * @param position
     * @param obsType
     * @param codeSpace
     */
    public SensorDescription(String platformID, String networkID,
            String observedProperty, String uomCode, Double[] position,
            String obsType, String codeSpace) {
        super();
        this.sensorML = createPlatformSensorML(platformID, networkID,
                observedProperty, uomCode, position, obsType, codeSpace);
    }

    /**
     * default constructor for predefined CHILDSENSOR sensorml structure
     * 
     * @param sensorID
     * @param platformID
     * @param networkID
     * @param observedProperty
     * @param uomCode
     */
    @Deprecated
    public SensorDescription(String sensorID, String platformID,
            String networkID, String observedProperty, String uomCode) {
        super();
        this.sensorML = createChildSensorML(sensorID, platformID, networkID,
                observedProperty, uomCode);
    }

    /**
     * default constructor for predefined CHILDSENSOR sensorml structure with
     * flexible phen with obsType (TRUTH, MEASURMENT, COUNT, TEXT, CATEGORY)
     * 
     * @param sensorID
     * @param platformID
     * @param networkID
     * @param observedProperty
     * @param uomCode
     * @param obsType
     * @param codeSpace
     */
    public SensorDescription(String sensorID, String platformID,
            String networkID, String observedProperty, String uomCode,
            String obsType, String codeSpace) {
        super();
        this.sensorML = createChildSensorML(sensorID, platformID, networkID,
                observedProperty, uomCode, obsType, codeSpace);
    }

    public SensorDescription(String sensorID, String platformID,
            String networkID, String observedProperty, String uomCode,
            Double[] placement, String obsType, String codeSpace) {
        super();
        this.sensorML = createChildSensorML(sensorID, platformID, networkID,
                observedProperty, uomCode, placement, obsType, codeSpace);
    }

    /**
     * to create an INDEPENDENT sensor thing WITH location
     * 
     * @param independencyIndicator
     * @param independencyGroup
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @param position
     */
    @Deprecated
    public SensorDescription(Boolean independencyIndicator,
            String independencyGroup, String uniqueSensorID, String obsProp,
            String uomCode, Double[] position) {
        super();
        this.sensorML = createIndependentSensorMLwithLocation(
                independencyIndicator, independencyGroup, uniqueSensorID,
                obsProp, uomCode, position);
    }

    /**
     * to create an INDEPENDENT sensor thing WITH location and flexible phen
     * with obsType (TRUTH, MEASUREMENT, COUNT, TEXT, CATEGORY)
     * 
     * @param independencyIndicator
     * @param independencyGroup
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @param position
     * @param obsType
     * @param codeSpace
     */
    public SensorDescription(Boolean independencyIndicator,
            String independencyGroup, String uniqueSensorID, String obsProp,
            String uomCode, Double[] position, String obsType, String codeSpace) {
        super();
        this.sensorML = createIndependentSensorMLwithLocation(
                independencyIndicator, independencyGroup, uniqueSensorID,
                obsProp, uomCode, position, obsType, codeSpace);
    }

    /**
     * to create an INDEPENDENT sensor thing NO location
     * 
     * @param independencyIndicator
     * @param independencyGroup
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     */
    public SensorDescription(Boolean independencyIndicator,
            String independencyGroup, String uniqueSensorID, String obsProp,
            String uomCode) {
        super();
        this.sensorML = createIndependentSensorML(independencyIndicator,
                independencyGroup, uniqueSensorID, obsProp, uomCode);
    }

    /**
     * to create an INDEPENDENT sensor thing NO location but flecxible phen with
     * obsType (TRUTH, MEASURMENT, COUNT, TEXT, CATEGORY)
     * 
     * @param independencyIndicator
     * @param independencyGroup
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @param obsType
     * @param codeSpace
     */
    public SensorDescription(Boolean independencyIndicator,
            String independencyGroup, String uniqueSensorID, String obsProp,
            String uomCode, String obsType, String codeSpace) {
        super();
        this.sensorML = createIndependentSensorML(independencyIndicator,
                independencyGroup, uniqueSensorID, obsProp, uomCode, obsType,
                codeSpace);
    }

    /**
     * simplest sensor, only uniqueID
     * 
     * @param networkID
     * @return
     */
    private String createNetworkSensorML(String networkID) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + networkID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * still network sensor, only uniqueID/network name, but allows for
     * registering of e.g. a LOGGER phen with obsType (TRUTH, MEASUREMENT,
     * COUNT, TEXT, CATEGORY)
     * 
     * @param networkID
     * @param obsProp
     * @param uomCode
     * @param obsType
     * @param codeSpace
     * @return
     */
    private String createNetworkSensorMLwithStatusPhen(String networkID,
            String obsProp, String uomCode, String obsType, String codeSpace) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + networkID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        if ((obsProp != null) && !(obsProp.isEmpty())) {
            smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode,
                    obsType, codeSpace));
        }
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * predefined platform sensorml structure
     * 
     * @param platformID
     * @param networkID
     * @param obsProp
     * @param uomCode
     * @param position
     * @return
     */
    @Deprecated
    private String createPlatformSensorML(String platformID, String networkID,
            String obsProp, String uomCode, Double[] position) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/"
                + platformID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID + "/"
                + platformID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + platformID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
//        smlGenerator
//                .append(getSensorCapabilitiesParentProcedures(VOCAB_PREFIX_PROCEDURE
//                        + "/" + networkID));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getPositionSensorML(position));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * predefined platform sensorml structure, with flexible status logger
     * phenomenon and with obsType (TRUTH, MEASUREMENT, COUNT, TEXT, CATEGORY)
     * 
     * @param platformID
     * @param networkID
     * @param obsProp
     * @param uomCode
     * @param position
     * @param obsType
     * @param codeSpace
     * @return
     */
    private String createPlatformSensorML(String platformID, String networkID,
            String obsProp, String uomCode, Double[] position, String obsType,
            String codeSpace) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/"
                + platformID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID + "/"
                + platformID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + platformID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
//        smlGenerator
//                .append(getSensorCapabilitiesParentProcedures(VOCAB_PREFIX_PROCEDURE
//                        + "/" + networkID));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getPlatformPositionSensorML(position, platformID));
        if ((obsProp != null) && !(obsProp.isEmpty())) {
            smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode,
                    obsType, codeSpace));
        }
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * predefined childsensor sensorml structure
     * 
     * @param sensorID
     * @param platformID
     * @param networkID
     * @param obsProp
     * @param uomCode
     * @return
     */
    @Deprecated
    private String createChildSensorML(String sensorID, String platformID,
            String networkID, String obsProp, String uomCode) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/" + "/"
                + platformID + "/" + sensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID + "/"
                + "/" + platformID + "/" + sensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + platformID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");

        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
//        smlGenerator
//                .append(getSensorCapabilitiesParentProcedures(VOCAB_PREFIX_PROCEDURE
//                        + "/" + networkID + "/" + platformID));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * redefined childsensor sensorml structure with obsType (TRUTH, MEASURMENT,
     * COUNT, TEXT, CATEGORY) and codeSpace
     * 
     * @param sensorID
     * @param platformID
     * @param networkID
     * @param obsProp
     * @param uomCode
     * @param obsType
     * @param codeSpace
     * @return
     */
    private String createChildSensorML(String sensorID, String platformID,
            String networkID, String obsProp, String uomCode, String obsType,
            String codeSpace) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/"
                + platformID + "/" + sensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID + "/"
                + platformID + "/" + sensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + platformID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");

        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
//        smlGenerator
//                .append(getSensorCapabilitiesParentProcedures(VOCAB_PREFIX_PROCEDURE
//                        + "/" + networkID + "/" + platformID));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode, obsType,
                codeSpace));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    private String createChildSensorML(String sensorID, String platformID,
            String networkID, String obsProp, String uomCode,
            Double[] placement, String obsType, String codeSpace) {

        StringBuilder smlGenerator = new StringBuilder();

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + networkID + "/"
                + platformID + "/" + sensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + networkID + "/"
                + platformID + "/" + sensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + platformID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");

        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
//        smlGenerator
//                .append(getSensorCapabilitiesParentProcedures(VOCAB_PREFIX_PROCEDURE
//                        + "/" + networkID + "/" + platformID));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getPlatformPositionSensorML(placement, platformID));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode, obsType,
                codeSpace));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * an independent group through indicator and location support
     * 
     * @param independencyIndicator
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @param position
     * @return
     */
    @Deprecated
    private String createIndependentSensorMLwithLocation(
            Boolean independencyIndicator, String independencyGroup,
            String uniqueSensorID, String obsProp, String uomCode,
            Double[] position) {

        StringBuilder smlGenerator = new StringBuilder();

        // must be intended otherwise empty sensorml
        if (!independencyIndicator)
            return "";

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + independencyGroup
                + "/" + uniqueSensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + independencyGroup
                + "/" + uniqueSensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + independencyGroup
                + "/" + uniqueSensorID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getPositionSensorML(position));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * an independent group through indicator and location support
     * 
     * @param independencyIndicator
     * @param independencyGroup
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @param position
     * @param obsType
     * @param codeSpace
     * @return
     */
    private String createIndependentSensorMLwithLocation(
            Boolean independencyIndicator, String independencyGroup,
            String uniqueSensorID, String obsProp, String uomCode,
            Double[] position, String obsType, String codeSpace) {

        StringBuilder smlGenerator = new StringBuilder();

        // FIXME, you made up that crap :-p
        // must be intended otherwise empty sensorml
        if (!independencyIndicator)
            return "";

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + independencyGroup
                + "/" + uniqueSensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/"
                + independencyGroup + "/" + uniqueSensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + independencyGroup
                + "/" + uniqueSensorID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getPositionSensorML(position));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode, obsType,
                codeSpace));
        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * no location, and an independent group through indicator
     * 
     * @param independencyIndicator
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @return
     */
    @Deprecated
    private String createIndependentSensorML(Boolean independencyIndicator,
            String independencyGroup, String uniqueSensorID, String obsProp,
            String uomCode) {

        StringBuilder smlGenerator = new StringBuilder();

        // must be intended otherwise empty sensorml
        if (!independencyIndicator)
            return "";

        String sensorURI = VOCAB_PREFIX_PROCEDURE + "/" + independencyGroup
                + "/" + uniqueSensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + independencyGroup
                + "/" + uniqueSensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + independencyGroup
                + "/" + uniqueSensorID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode));

        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * no location, and an independent group through indicator
     * 
     * @param independencyIndicator
     * @param independencyGroup
     * @param uniqueSensorID
     * @param obsProp
     * @param uomCode
     * @param obsType
     * @param codeSpace
     * @return
     */
    private String createIndependentSensorML(Boolean independencyIndicator,
            String independencyGroup, String uniqueSensorID, String obsProp,
            String uomCode, String obsType, String codeSpace) {

        StringBuilder smlGenerator = new StringBuilder();

        // must be intended otherwise empty sensorml
        if (!independencyIndicator)
            return "";

        String sensorURI = VOCAB_PREFIX_FEATURE + "/" + independencyGroup + "/"
                + uniqueSensorID;
        String offeringURI = VOCAB_PREFIX_OFFERING + "/" + independencyGroup
                + "/" + uniqueSensorID;
        String featureURI = VOCAB_PREFIX_FEATURE + "/" + independencyGroup
                + "/" + uniqueSensorID;

        smlGenerator.append("<sml:SensorML version=\"1.0.1\">" + "<sml:member>"
                + "<sml:System>");
        // here comes obly identification, uniqueID and offering
        smlGenerator.append(getIdentificationSensorML(sensorURI, offeringURI));
        smlGenerator.append(getSensorCapabilitiesOffering(sensorURI,
                offeringURI));
        smlGenerator
                .append(getgetSensorCapabilitiesFeatureOfInterestSensorML(featureURI));
        smlGenerator.append(getInputsOutputsSensorML(obsProp, uomCode, obsType,
                codeSpace));

        smlGenerator.append("</sml:System>" + "</sml:member>"
                + "</sml:SensorML>");
        return smlGenerator.toString();
    }

    /**
     * identification xml blocks
     * 
     * @param sensorURI
     * @param offeringURI
     * @return
     */
    private String getIdentificationSensorML(String sensorURI,
            String offeringURI) {

        StringBuilder identificationGenerator = new StringBuilder();

        String longName = sensorURI;
        String shortName = sensorURI;

        try {
            URL sUri = new URL(sensorURI);

            if (sUri.getPath() != null) {
                longName = sUri.getPath();
                String[] tempString = longName.split("/");

                if (tempString.length >= 1) {

                    shortName = tempString[tempString.length - 1];
                }
            }

        } catch (MalformedURLException e) {
            // nothing
            System.out.println("not a URL" + sensorURI);
        }

        identificationGenerator
                .append("<sml:identification>\n"
                        + "<sml:IdentifierList>\n"
                        + "<sml:identifier name=\"uniqueID\">\n"
                        + "<sml:Term definition=\"urn:ogc:def:identifier:OGC:uniqueID\">\n"
                        + "<sml:value>"
                        + sensorURI
                        + "</sml:value>\n"
                        + "</sml:Term>\n"
                        + "</sml:identifier>\n"
                        + "<sml:identifier name=\"longName\">\n"
                        + "<sml:Term definition=\"urn:ogc:def:identifier:OGC:1.0:longName\">\n"
                        + "<sml:value>"
                        + longName
                        + "</sml:value>\n"
                        + "</sml:Term>\n"
                        + "</sml:identifier>\n"
                        + "<sml:identifier name=\"shortName\">\n"
                        + "<sml:Term definition=\"urn:ogc:def:identifier:OGC:1.0:shortName\">\n"
                        + "<sml:value>" + shortName + "</sml:value>\n"
                        + "</sml:Term>\n" + "</sml:identifier>\n"
                        + "</sml:IdentifierList>\n" + "</sml:identification>\n");
        return identificationGenerator.toString();
    }

    private String getSensorCapabilitiesOffering(String sensorURI,
            String offeringURI) {

        StringBuilder gen = new StringBuilder();

        gen.append("<sml:capabilities name=\"offerings\">\n"
                + "     <!-- Special capabilities used to specify offerings. -->\n"
                + "     <!-- Parsed and removed during InsertSensor/UpdateSensorDescription, added during DescribeSensor. -->\n"
                + "     <!-- Offering is generated if not specified. -->\n"
                + "     <swe:SimpleDataRecord>\n"
                + "         <!-- Field name is used for the offering's name -->\n"
                + "         <swe:field name=\"Offering for sensor "
                + sensorURI
                + "\">\n"
                + "             <swe:Text definition=\"urn:ogc:def:identifier:OGC:offeringID\">\n"
                + "                 <swe:value>" + offeringURI
                + "</swe:value>\n" + "             </swe:Text>\n"
                + "         </swe:field>\n" + "     </swe:SimpleDataRecord>\n"
                + "</sml:capabilities>\n");

        return gen.toString();
    }

    private String getSensorCapabilitiesParentProcedures(String sensorURI) {

        StringBuilder gen = new StringBuilder();

        gen.append("<sml:capabilities name=\"parentProcedures\">\n"
                + "<!-- Special capabilities used to specify parent procedures. -->\n"
                + "<!-- Parsed and removed during InsertSensor/UpdateSensorDescription, added during DescribeSensor. -->\n"
                + "<swe:SimpleDataRecord>\n"
                + "    <swe:field name=\"parentProcedure\">\n"
                + "        <swe:Text>\n" + "            <swe:value>"
                + sensorURI + "</swe:value>\n" + "        </swe:Text>\n"
                + "    </swe:field>\n" + "</swe:SimpleDataRecord>\n"
                + "</sml:capabilities>\n");

        return gen.toString();
    }

    /**
     * featureOfInterest reference xml building blocks, not really used yet
     * 
     * @param featureURI
     * @return
     */
    private String getgetSensorCapabilitiesFeatureOfInterestSensorML(
            String featureURI) {

        StringBuilder featureOfInterestGenerator = new StringBuilder();
        // FIXME actually it needs to be checked if this foi is available
        featureOfInterestGenerator
                .append("<sml:capabilities name=\"featureOfInterest\">\n"
                        + "<swe:SimpleDataRecord>\n"
                        + "<swe:field name=\"FeatureOfInterestID\">\n"
                        + "<swe:Text definition=\"FeatureOfInterest identifier\">\n"
                        + "<swe:value>" + featureURI + "</swe:value>\n"
                        + "</swe:Text>\n" + "</swe:field>\n"
                        + "</swe:SimpleDataRecord>\n" + "</sml:capabilities>\n");
        return featureOfInterestGenerator.toString();
    }

    /**
     * position xml block
     * 
     * @param coords
     * @return
     */
    private String getPositionSensorML(Double[] coords) {

        StringBuilder locationGenerator = new StringBuilder();

        locationGenerator
                .append("<sml:position name=\"sensorPosition\">\n"
                        + "<swe:Position referenceFrame=\"urn:ogc:def:crs:EPSG::4326\">\n"
                        + "<swe:location>\n"
                        + "<swe:Vector gml:id=\"SENSOR_LOCATION\">\n"
                        + "<swe:coordinate name=\"easting\">\n"
                        + "<swe:Quantity axisID=\"x\">\n"
                        + "<swe:uom code=\"degree\"/>\n" + "<swe:value>"
                        + coords[0]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "<swe:coordinate name=\"northing\">\n"
                        + "<swe:Quantity axisID=\"y\">\n"
                        + "<swe:uom code=\"degree\"/>\n"
                        + "<swe:value>"
                        + coords[1]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "<swe:coordinate name=\"altitude\">\n"
                        + "<swe:Quantity axisID=\"z\">\n"
                        + "<swe:uom code=\"m\"/>\n"
                        + "<swe:value>"
                        + coords[2]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "</swe:Vector>\n"
                        + "</swe:location>\n"
                        + "</swe:Position>\n"
                        + "</sml:position>\n");
        // TODO better coords management
        return locationGenerator.toString();
    }

    /**
     * position xml block
     * 
     * @param coords
     * @return
     */
    private String getPlatformPositionSensorML(Double[] coords,
            String platformID) {

        StringBuilder locationGenerator = new StringBuilder();

        locationGenerator
                .append("<sml:position name=\"sensorPosition\">\n"
                        + "<swe:Position referenceFrame=\"urn:ogc:def:crs:EPSG::4326\">\n"
                        + "<swe:location>\n"
                        + "<swe:Vector gml:id=\"STATION_LOCATION_"
                        + platformID.toUpperCase()
                        + "\">\n"
                        + "<swe:coordinate name=\"easting\">\n"
                        + "<swe:Quantity axisID=\"x\">\n"
                        + "<swe:uom code=\"degree\"/>\n"
                        + "<swe:value>"
                        + coords[0]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "<swe:coordinate name=\"northing\">\n"
                        + "<swe:Quantity axisID=\"y\">\n"
                        + "<swe:uom code=\"degree\"/>\n"
                        + "<swe:value>"
                        + coords[1]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "<swe:coordinate name=\"altitude\">\n"
                        + "<swe:Quantity axisID=\"z\">\n"
                        + "<swe:uom code=\"m\"/>\n"
                        + "<swe:value>"
                        + coords[2]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "</swe:Vector>\n"
                        + "</swe:location>\n"
                        + "</swe:Position>\n"
                        + "</sml:position>\n");
        // TODO better coords management
        return locationGenerator.toString();
    }

    private String getChildSensorPositionSensorML(Double[] coords,
            String childsensorID) {

        StringBuilder locationGenerator = new StringBuilder();

        locationGenerator
                .append("<sml:position name=\"sensorPosition\">\n"
                        + "<swe:Position referenceFrame=\"urn:ogc:def:crs:EPSG::4326\">\n"
                        + "<swe:location>\n"
                        + "<swe:Vector gml:id=\"PLACEMENT_"
                        + childsensorID.toUpperCase()
                        + "\">\n"
                        + "<swe:coordinate name=\"easting\">\n"
                        + "<swe:Quantity axisID=\"x\">\n"
                        + "<swe:uom code=\"degree\"/>\n"
                        + "<swe:value>"
                        + coords[0]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "<swe:coordinate name=\"northing\">\n"
                        + "<swe:Quantity axisID=\"y\">\n"
                        + "<swe:uom code=\"degree\"/>\n"
                        + "<swe:value>"
                        + coords[1]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "<swe:coordinate name=\"altitude\">\n"
                        + "<swe:Quantity axisID=\"z\">\n"
                        + "<swe:uom code=\"m\"/>\n"
                        + "<swe:value>"
                        + coords[2]
                        + "</swe:value>\n"
                        + "</swe:Quantity>\n"
                        + "</swe:coordinate>\n"
                        + "</swe:Vector>\n"
                        + "</swe:location>\n"
                        + "</swe:Position>\n"
                        + "</sml:position>\n");
        // TODO better coords management
        return locationGenerator.toString();
    }

    /**
     * input output list for procedure xml block
     * 
     * @param observedProperty
     * @param uomCode
     * @return
     */
    @Deprecated
    private String getInputsOutputsSensorML(String observedProperty,
            String uomCode) {

        StringBuilder inputsOutputsGenerator = new StringBuilder();
        String phenomenonURI = VOCAB_PREFIX_PHENOMENON + "/" + observedProperty;

        inputsOutputsGenerator.append("<sml:inputs>\n" + "<sml:InputList>\n"
                + "<sml:input name=\""
                + observedProperty
                + "\">\n"
                + "<swe:ObservableProperty definition=\""
                + phenomenonURI
                + "\"/>\n"
                + "</sml:input>\n"
                + "</sml:InputList>\n"
                + "</sml:inputs>\n"
                + "<sml:outputs>\n"
                + "<sml:OutputList>\n"
                + "<sml:output name=\""
                + observedProperty
                + "\">\n"
                + "<swe:Quantity definition=\""
                + phenomenonURI
                + "\">\n"
                + "<swe:uom code=\""
                + uomCode
                + "\"/>\n"
                + "</swe:Quantity>\n"
                + "</sml:output>\n"
                + "</sml:OutputList>\n"
                + "</sml:outputs>\n");
        return inputsOutputsGenerator.toString();
    }

    /**
     * input output list for procedure xml block
     * 
     * @param observedProperty
     *            (plain term, NOT a URI)
     * @param uomCode
     *            (only for MEASUREMENT)
     * @param typeofObservation
     *            MEASUREMENT, COUNT, TEXT, CATEGORY, TRUTH
     * @param codeSpace
     *            (only for CATEGORY)
     * @return
     */
    private String getInputsOutputsSensorML(String observedProperty,
            String uomCode, String typeofObservation, String codeSpace) {

        StringBuilder inputsOutputsGenerator = new StringBuilder();
        String phenomenonURI = VOCAB_PREFIX_PHENOMENON + "/" + observedProperty;

        inputsOutputsGenerator.append("<sml:inputs>\n" + "<sml:InputList>\n"
                + "<sml:input name=\"" + observedProperty + "\">\n"
                + "<swe:ObservableProperty definition=\"" + phenomenonURI
                + "\"/>\n" + "</sml:input>\n" + "</sml:InputList>\n"
                + "</sml:inputs>\n" + "<sml:outputs>\n" + "<sml:OutputList>\n"
                + "<sml:output name=\"" + observedProperty + "\">\n");

        // ObservationType MEASUREMENT, COUNT, TEXT, CATEGORY, TRUTH
        if (typeofObservation.equalsIgnoreCase("MEASUREMENT")) {

            inputsOutputsGenerator.append("<swe:Quantity definition=\""
                    + phenomenonURI + "\">\n" + "<swe:uom code=\"" + uomCode
                    + "\"/>\n" + "</swe:Quantity>\n");

        } else if (typeofObservation.equalsIgnoreCase("COUNT")) {

            inputsOutputsGenerator.append("<swe:Count definition=\""
                    + phenomenonURI + "\"/>\n");

        } else if (typeofObservation.equalsIgnoreCase("TEXT")) {

            inputsOutputsGenerator.append("<swe:Text definition=\""
                    + phenomenonURI + "\"/>\n");

        } else if (typeofObservation.equalsIgnoreCase("CATEGORY")) {

            inputsOutputsGenerator.append("<swe:Category definition=\""
                    + phenomenonURI + "\">\n" + "<swe:codeSpace xlink:href=\""
                    + codeSpace + "\"/>\n" + "</swe:Category>\n");

        } else if (typeofObservation.equalsIgnoreCase("TRUTH")) {

            inputsOutputsGenerator.append("<swe:Boolean definition=\""
                    + phenomenonURI + "\"/>\n");

        }

        inputsOutputsGenerator.append("</sml:output>\n" + "</sml:OutputList>\n"
                + "</sml:outputs>\n");
        return inputsOutputsGenerator.toString();
    }

}
