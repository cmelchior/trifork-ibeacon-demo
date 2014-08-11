package com.trifork.ibeacon.eventbus;

import org.altbeacon.beacon.Beacon;

import java.util.Calendar;

public class BeaconScanCompleteEvent implements OttoEvent {

    private final Beacon beacon;
    private final Calendar timestamp;

    public BeaconScanCompleteEvent(Calendar timestamp, Beacon beacon) {
        this.timestamp = timestamp;
        this.beacon = beacon;
    }

    public org.altbeacon.beacon.Beacon getBeacon() {
        return beacon;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }
}
