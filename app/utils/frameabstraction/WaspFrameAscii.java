package utils.frameabstraction;

import com.rapplogic.xbee.api.XBeeAddress64;
import play.Logger;

import java.sql.Timestamp;
import java.util.*;

/**
 * Abstraction for WaspFrame provided by Libelium (see their API - WaspFrame)
 * Data looks something like that
 * <pre>
 *     <=>.#496267057C1054C5#NodeSONY#29#SOILTC:31.53#SOIL2:49.70#SOIL3:50.04#
 * </pre>
 */
public class WaspFrameAscii extends DataFrame {
    public static final Map<String, Integer> SENSOR_IDS;
    public static final int SENS_AGR_VANE_E = 0;
    public static final int SENS_AGR_VANE_ENE = 1;
    public static final int SENS_AGR_VANE_ESE = 15;
    public static final int SENS_AGR_VANE_N = 4;
    public static final int SENS_AGR_VANE_NE = 2;
    public static final int SENS_AGR_VANE_NNE = 3;
    public static final int SENS_AGR_VANE_NNW = 5;
    public static final int SENS_AGR_VANE_NW = 6;
    public static final int SENS_AGR_VANE_S = 12;
    public static final int SENS_AGR_VANE_SE = 14;
    public static final int SENS_AGR_VANE_SSE = 13;
    public static final int SENS_AGR_VANE_SSW = 11;
    public static final int SENS_AGR_VANE_SW = 10;
    public static final int SENS_AGR_VANE_W = 8;
    public static final int SENS_AGR_VANE_WNW = 7;
    public static final int SENS_AGR_VANE_WSW = 9;

    private final static Logger.ALogger logger = Logger.of("application." + WaspFrameAscii.class.getCanonicalName());

    static {
        //TODO SR this must be read from the database
        Map<String, Integer> aMap = new HashMap<>();
        aMap.put("BAT", 257);
        aMap.put("RSSI", 45);
        aMap.put("TC", 166);
        aMap.put("HUM", 167);
        aMap.put("PRES", 168);
        aMap.put("PAR", 12);
        aMap.put("ANE", 164);
        aMap.put("WV", 163);
        aMap.put("SOILTC", 169);
        aMap.put("SOIL2", 7);
        aMap.put("SOIL3", 8);
        aMap.put("PLV1", 165);
        SENSOR_IDS = Collections.unmodifiableMap(aMap);
    }

    /**
     * raw bytes from data frame
     */
    private byte[] data;
    private Timestamp timestamp;

    /**
     * parsed sensor data
     */
    private List<SensorData> sensorData = new ArrayList<>();

    public WaspFrameAscii(short packedId, XBeeAddress64 addr64) {
        super(packedId, addr64);
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        this.timestamp = new java.sql.Timestamp(now.getTime());
    }

    public List<SensorData> getSensorData() {
        return this.sensorData;
    }

    /**
     * this parses set data array (which should be an ASCII string) into sensor data array
     */
    private void parseSensorData() {
        sensorData.clear();

        String[] groups = this.getDataAsString().split("#");
        for (int i = 4; i < groups.length; i++) {
            logger.debug("Found Sensor Data: " + groups[i]);
            String[] splitIdValue = groups[i].split(":");
            // TODO SR this RAW data was basically a complete bullshit idea... so we set that to -1 here.
            final Double rawValue = Double.valueOf(splitIdValue[1]);
            SensorData sd = new SensorData(SENSOR_IDS.get(splitIdValue[0]), rawValue.longValue());
            sd.setData(Double.valueOf(splitIdValue[1]));
            switch (splitIdValue[0]) {
                case "WV":
                    sd.setData(decodeWindVane(rawValue.intValue()));
                    break;
                case "SOIL2":
                case "SOIL3":
                    sd.setData(decodeWatermark(rawValue));
                    break;
                default:
                    sd.setData(rawValue);
            }
            sensorData.add(sd);
        }
    }

    /**
     * Convert Watermark Frequency to Soil Water Pressure in cbar.
     * more info on this magic see http://www.libelium.com/downloads/documentation/agriculture_sensor_board_3.0.pdf
     *
     * @param freq
     * @return
     */
    private double decodeWatermark(double freq) {
        return (52273.6 - 6.83636 * freq) / (freq - 47.619);
    }

    private double decodeWindVane(int vane) {
        double returnvalue;
        switch (vane) {
            case SENS_AGR_VANE_N:
                returnvalue = 0;
                break;
            case SENS_AGR_VANE_NNE:
                returnvalue = 22;
                break;
            case SENS_AGR_VANE_NE:
                returnvalue = 45;
                break;
            case SENS_AGR_VANE_ENE:
                returnvalue = 67;
                break;
            case SENS_AGR_VANE_E:
                returnvalue = 90;
                break;
            case SENS_AGR_VANE_ESE:
                returnvalue = 112;
                break;
            case SENS_AGR_VANE_SE:
                returnvalue = 135;
                break;
            case SENS_AGR_VANE_SSE:
                returnvalue = 157;
                break;
            case SENS_AGR_VANE_S:
                returnvalue = 180;
                break;
            case SENS_AGR_VANE_SSW:
                returnvalue = 202;
                break;
            case SENS_AGR_VANE_SW:
                returnvalue = 225;
                break;
            case SENS_AGR_VANE_WSW:
                returnvalue = 247;
                break;
            case SENS_AGR_VANE_W:
                returnvalue = 270;
                break;
            case SENS_AGR_VANE_WNW:
                returnvalue = 292;
                break;
            case SENS_AGR_VANE_NW:
                returnvalue = 315;
                break;
            case SENS_AGR_VANE_NNW:
                returnvalue = 337;
                break;
            default:
                returnvalue = -1;
        }
        return returnvalue;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.parseSensorData();
    }

    public void setData(int[] data) {
        // is there a better way to convert int[] to byte[] when all ints are <255??
        this.data = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            this.data[i] = (byte) data[i];
        }
        this.parseSensorData();
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public String getDataAsString() {
        return new String(this.data);
    }
}
