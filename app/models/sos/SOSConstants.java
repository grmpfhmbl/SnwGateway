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

public class SOSConstants {

    public enum typeofObservation {
        MEASUREMENT, COUNT, TEXT, CATEGORY, TRUTH

    }

    public final static String MEASUREMENT_OBS_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
    public final static String COUNT_OBS_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation";
    public final static String TEXT_OBS_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation";
    public final static String CATEGORY_OBS_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation";
    public final static String TRUTH_OBS_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation";

    public final static String SAMPLINGPOINT_DEF = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";

    public enum EntityType {
        NETWORK, PLATFORM, SENSOR, INDEPENDENT
    }

    /**
     * procedures/sensors ... URI_PREFIX + /procedure + /network + /platform +
     * /sensor http://vocab.smart-project.info/sensorweb/procedure + /baseID1 +
     * /waspID01 + /ID55
     * 
     * offerings (off-proc 1:1) ... URI_PREFIX + /offering + /network +
     * /platform + /sensor http://vocab.smart-project.info/sensorweb/offering +
     * /baseID1 + /waspID01 + /ID55
     * 
     * features (only baseID) ... URI_PREFIX + /feature + /platform
     * http://vocab.smart-project.info/sensorweb/feature + /baseID1
     * 
     * obsProps phenomenon ... URI_PREFIX + /phenomenon + /obsProp
     * http://vocab.smart-project.info/sensorweb/phenomenon + /temperature
     */

    public final static String InsertSensorHeaders = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<swes:InsertSensor \n"
            + " xmlns:swes=\"http://www.opengis.net/swes/2.0\" \n"
            + " xmlns:sos=\"http://www.opengis.net/sos/2.0\" \n"
            + " xmlns:swe=\"http://www.opengis.net/swe/1.0.1\" \n"
            + " xmlns:sml=\"http://www.opengis.net/sensorML/1.0.1\" \n"
            + " xmlns:gml=\"http://www.opengis.net/gml\" \n"
            + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" \n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" service=\"SOS\" version=\"2.0.0\" \n"
            + " xsi:schemaLocation=\"http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sosInsertSensor.xsd "
            + " http://www.opengis.net/swes/2.0 http://schemas.opengis.net/swes/2.0/swes.xsd\">"
            + "<swes:procedureDescriptionFormat>http://www.opengis.net/sensorML/1.0.1</swes:procedureDescriptionFormat>"
            + "<swes:procedureDescription>";

    public final static String UpdateSensorHeaders = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<swes:UpdateSensorDescription \n"
            + " xmlns:swes=\"http://www.opengis.net/swes/2.0\" "
            + " xmlns:sos=\"http://www.opengis.net/sos/2.0\" "
            + " xmlns:swe=\"http://www.opengis.net/swe/1.0.1\" "
            + " xmlns:sml=\"http://www.opengis.net/sensorML/1.0.1\" "
            + " xmlns:gml=\"http://www.opengis.net/gml\" "
            + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" service=\"SOS\" version=\"2.0.0\" \n"
            + " xsi:schemaLocation=\"http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sosInsertSensor.xsd "
            + " http://www.opengis.net/swes/2.0 http://schemas.opengis.net/swes/2.0/swes.xsd\">";

    public final static String InsertObservationHeaders = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<sos:InsertObservation "
            + " xmlns:sos=\"http://www.opengis.net/sos/2.0\" "
            + " xmlns:swes=\"http://www.opengis.net/swes/2.0\" "
            + " xmlns:swe=\"http://www.opengis.net/swe/2.0\" "
            + " xmlns:sml=\"http://www.opengis.net/sensorML/1.0.1\" "
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\" "
            + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
            + " xmlns:om=\"http://www.opengis.net/om/2.0\" "
            + " xmlns:sams=\"http://www.opengis.net/samplingSpatial/2.0\" "
            + " xmlns:sf=\"http://www.opengis.net/sampling/2.0\" \n"
            + " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" \n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" service=\"SOS\" version=\"2.0.0\" \n"
            + " xsi:schemaLocation=\"http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd \n"
            + "  http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd\">";
}