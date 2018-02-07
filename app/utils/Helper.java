/*
 * Copyright 2015 Smart Aquifer Characterisation (SAC) Programme (http://www.gns.cri
 * .nz/Home/Our-Science/Environment-and-Materials/Groundwater/Research-Programmes/SMART-Aquifer-Characterisation)
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
 * <p>
 * only Christian knows what happening relly, we better reverse
 * engineer quickly and refacor and make it modern and reactive :-p
 * <p>
 * note as of 19.98.2014 AK
 * <p>
 * LICENSE designated ASL 2.0, extra packages used xbee api and rxtx to be checked
 */
package utils;

import play.Logger;

import java.nio.ByteBuffer;

public class Helper {

    final static Logger.ALogger logger = Logger.of("utils.Helper");
    static final String HEXES = "0123456789ABCDEF";

    /**
     * Converts a HexString to a byte array
     * @param s String to be converted
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
               .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * Linear mapping
     * @param x the value which should be mapped
     * @param in_min input value minimum
     * @param in_max input value maximum
     * @param out_min output value minimum
     * @param out_max output value maximum
     * @return
     */
    public static long mapValues(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * Linear mapping
     * @param x the value which should be mapped
     * @param in_min input value minimum
     * @param in_max input value maximum
     * @param out_min output value minimum
     * @param out_max output value maximum
     * @return
     */
    public static double mapValues(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * Long value to byte array
     * @param l
     * @return
     */
    public static byte[] long2bytearray(long l) {
        byte[] b = new byte[8];
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(0, l);

        return buf.array();
    }

    /**
     * Converts IEEE754 32bit long representation to double
     * @param l Value to be converted
     * @return
     */
    public static double longieee2double(long l) {
        Float f = Float.intBitsToFloat((int) l);
        return (double) f;
    }

    //

    /**
     * Converts long to 8 bit unsigned short array
     * @param l value to be converted
     * @return
     */
    public static short[] long2unsignedShortArray(long l) {
        return (byteArray2shortArrayRemoveSign(long2bytearray(l)));
    }

    /**
     * Byte array to short array 
     * @param b byte array
     * @return short array
     */
    public static short[] byteArray2shortArrayRemoveSign(byte[] b) {
        short[] s = new short[b.length];

        for (int i = 0; i < b.length; i++) {
            s[i] = (short) (b[i] & 0xFF);
        }
        return s;
    }

    /**
     * Converts byte array to hex string
     * @param bytes Array to be converted
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] int2byte(int[] src) {
        int srcLength = src.length;
        byte[] dst = new byte[srcLength << 2];

        for (int i = 0; i < srcLength; i++) {
            int x = src[i];
            int j = i << 2;
            dst[j++] = (byte) ((x >>> 0) & 0xff);
            dst[j++] = (byte) ((x >>> 8) & 0xff);
            dst[j++] = (byte) ((x >>> 16) & 0xff);
            dst[j++] = (byte) ((x >>> 24) & 0xff);
        }
        return dst;
    }

    /**
     * Int array to byte array
     * Removes the upper bytes from the int field and returns just one byte portion
     * @param src Int array
     * @return Byte array
     */
    public static byte[] int2byteTrun(int[] src) {
        int srcLength = src.length;
        byte[] dst = new byte[srcLength];

        for (int i = 0; i < srcLength; i++) {
            dst[i] = (byte) src[i];
        }
        return dst;
    }

    /**
     * Byte array to int array
     * @param src Source byte array
     * @return int array
     */
    public static int[] byte2int(byte[] src) {
        int dstLength = src.length >>> 2;
        int[] dst = new int[dstLength];

        for (int i = 0; i < dstLength; i++) {
            int j = i << 2;
            int x = 0;
            x += (src[j++] & 0xff) << 0;
            x += (src[j++] & 0xff) << 8;
            x += (src[j++] & 0xff) << 16;
            x += (src[j++] & 0xff) << 24;
            dst[i] = x;
        }
        return dst;
    }

    /**
     * Calculates time difference between two timestamps
     * @param ts1 Timestamp 1
     * @param ts2 Timestamp 2
     * @return Difference in milliseconds
     */
    public static long getTimeTifference(java.sql.Timestamp ts1, java.sql.Timestamp ts2) {
        long a = ts1.getTime();
        long b = ts2.getTime();
        long diff = a - b;
        return (diff < 0) ? -diff : diff;
    }
}
