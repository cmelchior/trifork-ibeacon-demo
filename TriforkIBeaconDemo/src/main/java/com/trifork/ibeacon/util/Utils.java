package com.trifork.ibeacon.util;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;

import org.altbeacon.beacon.Identifier;

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

    public static org.altbeacon.beacon.Beacon convertIBeacon(Beacon beacon) {
       return new org.altbeacon.beacon.Beacon.Builder()
               .setId1(beacon.getProximityUUID())
               .setId2(Integer.toString(beacon.getMajor()))
               .setId3(Integer.toString(beacon.getMinor()))
               .setBluetoothAddress(beacon.getMacAddress())
               .setTxPower(beacon.getMeasuredPower())
               .setRssi(beacon.getRssi())
               .build();
    }

    public static org.altbeacon.beacon.Region convertRegion(Region region) {
        return new org.altbeacon.beacon.Region(
                region.getIdentifier(),
                Identifier.parse(region.getProximityUUID()),
                Identifier.fromInt(region.getMajor()),
                Identifier.fromInt(region.getMinor()));
    }

    public static Region convertRegion(org.altbeacon.beacon.Region region) {
        return new Region(region.getUniqueId(),
                region.getId1().toString(),
                region.getId2().toInt(),
                region.getId3().toInt());
    }
}
