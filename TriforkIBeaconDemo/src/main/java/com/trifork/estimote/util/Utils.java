package com.trifork.estimote.util;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;

import java.text.DecimalFormat;

public class Utils {

    private static DecimalFormat rangeFormatter = new DecimalFormat("#,##0.0");

    public static String formatRange(double rangeInMeters) {
        return rangeFormatter.format(rangeInMeters);
    }

    public static boolean isSameBeacon(Beacon entry, Region selectedBeacon) {
        if (entry == null || selectedBeacon == null) return false;
        return (entry.getProximityUUID().equals(selectedBeacon.getProximityUUID())
                && entry.getMajor() == selectedBeacon.getMajor()
                && entry.getMinor() == selectedBeacon.getMinor());
    }
}
