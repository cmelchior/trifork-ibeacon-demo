package com.trifork.ibeacon.util;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.radiusnetworks.ibeacon.IBeacon;

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

    public static Beacon convertIBeacon(IBeacon beacon) {
       return new Beacon(beacon.getProximityUuid(), "", beacon.getBluetoothAddress(), beacon.getMajor(), beacon.getMinor(), beacon.getTxPower(), beacon.getRssi());
    }

    public static com.radiusnetworks.ibeacon.Region convertRegion(Region region) {
        return new com.radiusnetworks.ibeacon.Region(region.getIdentifier(), region.getProximityUUID(),region.getMajor(), region.getMinor());
    }

    public static Region convertRegion(com.radiusnetworks.ibeacon.Region region) {
        return new Region(region.getUniqueId(), region.getProximityUuid(), region.getMajor(), region.getMinor());
    }
}
