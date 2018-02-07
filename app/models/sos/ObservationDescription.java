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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

public class ObservationDescription {

	protected String OM_Member;

	public String getOM_Member() {
		return OM_Member;
	}

	public void setOM_Member(String oM_Member) {
		this.OM_Member = oM_Member;
	}

	/**
	 * standard ObservationDescription, to be tested if still valid, is of type
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement,
	 * sampledFeatureURI http://sweet.nasa.jpl/2.2/Climate empty codeSpace for
	 * feature
	 * 
	 * @param phenTime
	 * @param offeringURI
	 * @param sensorURI
	 * @param phenomenonURI
	 * @param uomCode
	 * @param value
	 * @param featureURI
	 * @param position
	 *            (can be null)
	 */
	public ObservationDescription(Date phenTime, String offeringURI,
			String sensorURI, String phenomenonURI, String uomCode,
			Double value, String featureURI, Double[] position) {
		super();
		// create network sensorML without parent, without location, without
		// inputs/outputs
		this.OM_Member = createTemplatedMeasurementObservation(phenTime,
				offeringURI, sensorURI, phenomenonURI, uomCode, value,
				featureURI, position);
	}

	/**
	 * ObservationDescription, assumed to be of type MEASUREMENT or COUNT!!!!
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement
	 * 
	 * @param phenTime
	 * @param offeringURI
	 * @param sensorURI
	 * @param phenomenonURI
	 * @param uomCode
	 *            (ignored for COUNT)
	 * @param value
	 *            ( 8.0 Double will be cast to int)
	 * @param featureURI
	 * @param position
	 *            (can be null)
	 * @param obsType
	 *            can be MEASUREMENT or COUNT!!!!
	 * @param codeSpace
	 *            (can be null, but can be used for feature codespace, because
	 *            we don't have category
	 * @param sampledFeatureURI
	 *            (can be null)
	 */
	public ObservationDescription(Date phenTime, String offeringURI,
			String sensorURI, String phenomenonURI, String uomCode,
			Double value, String featureURI, Double[] position, String obsType,
			String codeSpace, String sampledFeatureURI) {
		super();
		// create network sensorML without parent, without location, without
		// inputs/outputs
		this.OM_Member = createNumberbasedObservation(phenTime, offeringURI,
				sensorURI, phenomenonURI, uomCode, value, featureURI, position,
				obsType, sampledFeatureURI, codeSpace);
	}

	/**
	 * ObservationDescription, assumed to be of type TEXT, TRUTH, CATEGORY !!!
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation
	 * 
	 * @param phenTime
	 * @param offeringURI
	 * @param sensorURI
	 * @param phenomenonURI
	 * @param value
	 *            (the respective string value for the obsType, or true/false
	 *            for TRUTH)
	 * @param featureURI
	 * @param position
	 *            (can be null)
	 * @param obsType
	 *            can be TEXT, TRUTH, CATEGORY !!!
	 * @param codeSpace
	 *            (can be null, but can be used for feature codespace)
	 * @param sampledFeatureURI
	 *            (can be null)
	 */
	public ObservationDescription(Date phenTime, String offeringURI,
			String sensorURI, String phenomenonURI, String value,
			String featureURI, Double[] position, String obsType,
			String codeSpace, String sampledFeatureURI) {
		super();
		// create network sensorML without parent, without location, without
		// inputs/outputs
		this.OM_Member = createTextbasedObservation(phenTime, offeringURI,
				sensorURI, phenomenonURI, value, featureURI, position, obsType,
				sampledFeatureURI, codeSpace);

	}

	/**
	 * standard ObservationDescription, to be tested if still valid, is of type
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement,
	 * sampledFeatureURI http://sweet.nasa.jpl/2.2/Climate empty codeSpace for
	 * feature
	 * 
	 * @param phenTime
	 * @param offeringURI
	 * @param sensorURI
	 * @param phenomenonURI
	 * @param uomCode
	 * @param value
	 * @param featureURI
	 * @param position
	 * @return
	 */
	public String createTemplatedMeasurementObservation(Date phenTime,
			String offeringURI, String sensorURI, String phenomenonURI,
			String uomCode, Double value, String featureURI, Double[] position) {

		StringBuilder sosOMGenerator = new StringBuilder();
		sosOMGenerator.append("<sos:offering>" + offeringURI
				+ "</sos:offering>");

		// FIXME generate better?
		String sampledFeatureURI = "http://sweet.nasa.jpl/2.2/Climate";
		String ssfGmlID = "ssf_"
				+ DigestUtils.sha1Hex(featureURI).toUpperCase();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		String pointGmlID = "poi_"
				+ DigestUtils.sha1Hex(featureURI).toUpperCase();

		sosOMGenerator
				.append("<sos:observation>\n"
						+ "<om:OM_Observation gml:id=\"o1\">\n"
						+ "<om:type xlink:href=\"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"/>\n"
						+ "<om:phenomenonTime>\n"
						+ "<gml:TimeInstant gml:id=\"phenomenonTime\">\n"
						+ "<gml:timePosition>" + fmt.format(phenTime)
						+ "</gml:timePosition>\n" + "</gml:TimeInstant>\n"
						+ "</om:phenomenonTime>\n"
						+ "<om:resultTime xlink:href=\"#phenomenonTime\"/>\n"
						+ "<om:procedure xlink:href=\"" + sensorURI + "\"/>\n"
						+ "<om:observedProperty xlink:href=\"" + phenomenonURI
						+ "\"/>\n");

		// if position null, assume feature exists and just past URI
		if (position == null) {
			sosOMGenerator.append("	    <om:featureOfInterest xlink:href=\""
					+ featureURI + "\"/>\n");
		} else {
			sosOMGenerator
					.append("	    <om:featureOfInterest>\n"
							+ "<sams:SF_SpatialSamplingFeature gml:id=\""
							+ ssfGmlID
							+ "\">\n"
							+ "<gml:identifier codeSpace=\"\">"
							+ featureURI
							+ "</gml:identifier>\n"
							+ "<sf:type xlink:href=\"http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint\"/>\n"
							+ "<sf:sampledFeature xlink:href=\""
							+ sampledFeatureURI
							+ "\"/>\n"
							+ "<sams:shape>\n"
							+ "<gml:Point gml:id=\""
							+ pointGmlID
							+ "\">\n"
							+ "<gml:pos srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\">"
							+ position[0]
							+ " "
							+ position[1]
							+ "</gml:pos>\n"
							+ "</gml:Point>\n"
							+ "</sams:shape>\n"
							+ "</sams:SF_SpatialSamplingFeature>\n"
							+ "</om:featureOfInterest>\n");
		}
		sosOMGenerator.append("<om:result xsi:type=\"gml:MeasureType\" uom=\""
				+ uomCode + "\">" + value + "</om:result>\n"
				+ "</om:OM_Observation>\n" + "</sos:observation>\n");

		return sosOMGenerator.toString();
	}

	/**
	 * createNumberbasedObservation
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation
	 * 
	 * @param phenTime
	 * @param offeringURI
	 * @param sensorURI
	 * @param phenomenonURI
	 * @param uomCode
	 *            (ignored for COUNT)
	 * @param value
	 *            ( 8.0 Double will be cast to int)
	 * @param featureURI
	 * @param position
	 *            (can be null, will only use feature href then)
	 * @param obsType
	 *            can be MEASUREMENT or COUNT!!!!
	 * @param sampledFeatureURI
	 *            (can be null)
	 * @param foiCodeSpace
	 *            (can be null or be used for feature)
	 * @return
	 */
	private String createNumberbasedObservation(Date phenTime,
			String offeringURI, String sensorURI, String phenomenonURI,
			String uomCode, Double value, String featureURI, Double[] position,
			String obsType, String sampledFeatureURI, String foiCodeSpace) {

		StringBuilder sosOMGenerator = new StringBuilder();

		// begin
		sosOMGenerator.append("<sos:offering>" + offeringURI
				+ "</sos:offering>\n" + "<sos:observation>\n"
				+ "<om:OM_Observation gml:id=\"o1\">\n");

		// obsType
		if (obsType != null && obsType.equalsIgnoreCase("MEASUREMENT")) {

			sosOMGenerator.append("<om:type xlink:href=\""
					+ SOSConstants.MEASUREMENT_OBS_DEF + "\"/>\n");

		} else if (obsType != null && obsType.equalsIgnoreCase("COUNT")) {

			sosOMGenerator.append("<om:type xlink:href=\""
					+ SOSConstants.COUNT_OBS_DEF + "\"/>\n");
		}

		// result and phen times
		sosOMGenerator.append(createObservationTimes(phenTime));

		// proc href
		sosOMGenerator.append("<om:procedure xlink:href=\"" + sensorURI
				+ "\"/>\n");

		// here would strict spatial sampling filtering paramter go
		if (position != null && position.length >= 2) {
			sosOMGenerator.append(createSpatialSamplingParameter(position));
		}

		// obsprop
		sosOMGenerator.append("<om:observedProperty xlink:href=\""
				+ phenomenonURI + "\"/>\n");

		// spatialsampling feature
		sosOMGenerator.append(createSamplingFeature(featureURI, position,
				sampledFeatureURI, foiCodeSpace));

		// actual measurement respective count observation
		if (obsType != null && obsType.equalsIgnoreCase("MEASUREMENT")) {

			sosOMGenerator
					.append("<om:result xsi:type=\"gml:MeasureType\" uom=\""
							+ uomCode + "\">" + value + "</om:result>\n");

		} else if (obsType != null && obsType.equalsIgnoreCase("COUNT")) {

			sosOMGenerator.append("<om:result xsi:type=\"xs:integer\">"
					+ (int) value.doubleValue() + "</om:result>\n");
		}

		// footer
		sosOMGenerator
				.append("</om:OM_Observation>\n" + "</sos:observation>\n");

		return sosOMGenerator.toString();
	}

	/**
	 * createTextbasedObservation
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation
	 * http
	 * ://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation
	 * http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation
	 * 
	 * @param phenTime
	 * @param offeringURI
	 * @param sensorURI
	 * @param phenomenonURI
	 * @param uomCode
	 * @param value
	 * @param featureURI
	 * @param position
	 *            (can be null, will only use feature href then)
	 * @param obsType
	 *            can be TEXT, TRUTH, CATEGORY !!!
	 * @param sampledFeatureURI
	 *            (can be null)
	 * @param foiCodeSpace
	 *            (can be null or be used for feature)
	 * @return
	 */
	private String createTextbasedObservation(Date phenTime,
			String offeringURI, String sensorURI, String phenomenonURI,
			String value, String featureURI, Double[] position, String obsType,
			String sampledFeatureURI, String foiCodeSpace) {

		StringBuilder sosOMGenerator = new StringBuilder();

		// begin
		sosOMGenerator.append("<sos:offering>" + offeringURI
				+ "</sos:offering>\n" + "<sos:observation>\n"
				+ "<om:OM_Observation gml:id=\"o1\">\n");

		// obsType
		if (obsType != null && obsType.equalsIgnoreCase("TEXT")) {

			sosOMGenerator.append("<om:type xlink:href=\""
					+ SOSConstants.TEXT_OBS_DEF + "\"/>\n");

		} else if (obsType != null && obsType.equalsIgnoreCase("TRUTH")) {

			sosOMGenerator.append("<om:type xlink:href=\""
					+ SOSConstants.TRUTH_OBS_DEF + "\"/>\n");

		} else if (obsType != null && obsType.equalsIgnoreCase("CATEGORY")) {

			sosOMGenerator.append("<om:type xlink:href=\""
					+ SOSConstants.CATEGORY_OBS_DEF + "\"/>\n");
		}

		// result and phen times
		sosOMGenerator.append(createObservationTimes(phenTime));

		// proc href
		sosOMGenerator.append("<om:procedure xlink:href=\"" + sensorURI
				+ "\"/>\n");

		// here would strict spatial sampling filtering paramter go
		if (position != null && position.length >= 2) {
			sosOMGenerator.append(createSpatialSamplingParameter(position));
		}

		// obsprop
		sosOMGenerator.append("<om:observedProperty xlink:href=\""
				+ phenomenonURI + "\"/>\n");

		// spatialsampling feature
		sosOMGenerator.append(createSamplingFeature(featureURI, position,
				sampledFeatureURI, foiCodeSpace));

		// actual measurement respective count observation
		if (obsType != null && obsType.equalsIgnoreCase("TEXT")) {

			sosOMGenerator.append("<om:result xsi:type=\"xs:string\">" + value
					+ "</om:result>\n");

		} else if (obsType != null && obsType.equalsIgnoreCase("TRUTH")) {

			if (value.equalsIgnoreCase("true")) {
				sosOMGenerator
						.append("<om:result xsi:type=\"xs:boolean\">true</om:result>\n");
			} else {
				sosOMGenerator
						.append("<om:result xsi:type=\"xs:boolean\">false</om:result>\n");
			}

		} else if (obsType != null && obsType.equalsIgnoreCase("CATEGORY")) {

			sosOMGenerator
					.append("<om:result xsi:type=\"gml:ReferenceType\" xlink:href=\""
							+ value + "\"/>\n");
		}

		// footer
		sosOMGenerator
				.append("</om:OM_Observation>\n" + "</sos:observation>\n");

		return sosOMGenerator.toString();
	}

	/**
	 * createObservationTimes, take one time instant for result and ohentime
	 * 
	 * @param phenTime
	 * @return
	 */
	private String createObservationTimes(Date phenTime) {

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");

		return ("<om:phenomenonTime>\n"
				+ "<gml:TimeInstant gml:id=\"phenomenonTime\">\n"
				+ "<gml:timePosition>" + fmt.format(phenTime)
				+ "</gml:timePosition>\n" + "</gml:TimeInstant>\n"
				+ "</om:phenomenonTime>\n"
				+ "<om:resultTime xlink:href=\"#phenomenonTime\"/>\n");
	}

	/**
	 * createSamplingFeature, tries to crate sf_samplingfeature flexibly as
	 * possible
	 * 
	 * @param featureURI
	 *            (will be needed in any case)
	 * @param position
	 *            (can be null, but if not must have x/y values)
	 * @param sampledFeatureURI
	 *            (can be null)
	 * @param foiCodeSpace
	 *            (can be null)
	 * @return
	 */
	private String createSamplingFeature(String featureURI, Double[] position,
			String sampledFeatureURI, String foiCodeSpace) {

		StringBuilder sosOMGenerator = new StringBuilder();

		// generate better?
		String ssfGmlID = "ssf_"
				+ DigestUtils.sha1Hex(featureURI).toUpperCase();
		String pointGmlID = "poi_"
				+ DigestUtils.sha1Hex(featureURI).toUpperCase();

		String codeSpace = "";
		if (foiCodeSpace != null) {
			codeSpace = foiCodeSpace;
		}

		if (position == null) {
			sosOMGenerator.append("	    <om:featureOfInterest xlink:href=\""
					+ featureURI + "\"/>\n");
		} else {
			sosOMGenerator.append("	    <om:featureOfInterest>\n"
					+ "<sams:SF_SpatialSamplingFeature gml:id=\"" + ssfGmlID
					+ "\">\n" + "<gml:identifier codeSpace=\"" + codeSpace
					+ "\">" + featureURI + "</gml:identifier>\n"
					+ "<sf:type xlink:href=\"" + SOSConstants.SAMPLINGPOINT_DEF
					+ "\"/>\n");

			if ((sampledFeatureURI != null) && !(sampledFeatureURI.isEmpty())) {
				sosOMGenerator.append("<sf:sampledFeature xlink:href=\""
						+ sampledFeatureURI + "\"/>\n");
			}

			sosOMGenerator
					.append("<sams:shape>\n"
							+ "<gml:Point gml:id=\""
							+ pointGmlID
							+ "\">\n"
							+ "<gml:pos srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\">"
							+ position[0] + " " + position[1] + "</gml:pos>\n"
							+ "</gml:Point>\n" + "</sams:shape>\n"
							+ "</sams:SF_SpatialSamplingFeature>\n"
							+ "</om:featureOfInterest>\n");
		}

		return sosOMGenerator.toString();
	}

	/**
	 * createSpatialSamplingParameter, new, I wonder if this in now the
	 * alternative to querying against SF_feature geometry, we can used it
	 * instead of always encoding the SF_foi because I think the 52n sos
	 * complained that the same feature already exists
	 * 
	 * @param position
	 * @return
	 */
	private String createSpatialSamplingParameter(Double[] position) {

		StringBuilder sosOMGenerator = new StringBuilder();
		String hex = "SamplingPoint_" + position[0] + " " + position[1];

		String pointGmlID = "SamplingPoint_"
				+ DigestUtils.sha1Hex(hex).toUpperCase();

		sosOMGenerator
				.append("<om:parameter>\n"
						+ "<om:NamedValue>\n"
						+ "    <om:name xlink:href=\"http://www.opengis.net/def/param-name/OGC-OM/2.0/samplingGeometry\"/>\n"
						+ "    <om:value xsi:type=\"gml:GeometryPropertyType\">\n"
						+ "        <gml:Point gml:id=\""
						+ pointGmlID
						+ "\">\n"
						+ "            <gml:pos srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\">"
						+ position[0] + " " + position[1] + "</gml:pos>\n"
						+ "        </gml:Point>\n" + "    </om:value>\n"
						+ "	</om:NamedValue>\n" + "</om:parameter>\n");

		return sosOMGenerator.toString();
	}
}
