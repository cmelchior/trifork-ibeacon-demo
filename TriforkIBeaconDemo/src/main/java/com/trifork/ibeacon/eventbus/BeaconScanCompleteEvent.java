package com.trifork.ibeacon.eventbus;

import com.estimote.sdk.Beacon;
import com.trifork.ibeacon.util.Utils;

import java.util.Calendar;

public class BeaconScanCompleteEvent implements OttoEvent {

    private final org.altbeacon.beacon.Beacon beacon;
    private final Calendar timestamp;

    public BeaconScanCompleteEvent(Calendar timestamp, org.altbeacon.beacon.Beacon beacon) {
        this.timestamp = timestamp;
        this.beacon = beacon;
    }

    public BeaconScanCompleteEvent(Calendar timestamp, Beacon beacon) {
        this.timestamp = timestamp;
        this.beacon = Utils.convertIBeacon(beacon);
    }

    public org.altbeacon.beacon.Beacon getBeacon() {
        return beacon;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }
}
