package com.trifork.ibeacon.util;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;

public class Utils {

    private static DecimalFormat rangeFormatter = new DecimalFormat("#,##0.0");

    public static String formatRange(double rangeInMeters) {
        return rangeFormatter.format(rangeInMeters);
    }

    public static boolean isSameBeacon(Beacon entry, Region selectedBeacon) {
        if (entry == null || selectedBeacon == null) return false;
        return (entry.getId1().equals(selectedBeacon.getId1())
                && entry.getId2().equals(selectedBeacon.getId2())
                && entry.getId3().equals(selectedBeacon.getId3()));
    }

    public static Proximity proximityFromDistance(double accuracy) {
        if (accuracy < 0.0D) {
            return Proximity.UNKNOWN;
        }
        if (accuracy < 0.5D) {
            return Proximity.IMMEDIATE;
        }
        if (accuracy <= 3.0D) {
            return Proximity.NEAR;
        }
        return Proximity.FAR;
    }

    private static double computeAccuracy(Beacon beacon) {
//            if (beacon.getRssi() == 0) {
//                return -1.0D;
//            }
//
//            double ratio = beacon.getRssi() / beacon.getTxPower();
//            double rssiCorrection = 0.96D + Math.pow(Math.abs(beacon.getRssi()), 3.0D) % 10.0D / 150.0D;
//
//            if (ratio <= 1.0D) {
//                return Math.pow(ratio, 9.98D) * rssiCorrection;
//            }
//            return (0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;

        return 0;
    }


    public static enum Proximity {
        UNKNOWN,
        IMMEDIATE,
        NEAR,
        FAR;
    }
}
