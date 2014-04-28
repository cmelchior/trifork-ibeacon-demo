package com.trifork.estimote.eventbus;

import com.estimote.sdk.Beacon;

import java.util.Calendar;

public class BeaconScanCompleteEvent implements OttoEvent {

    private final Beacon beacon;
    private final Calendar timestamp;

    public BeaconScanCompleteEvent(Calendar timestamp, Beacon beacon) {
        this.timestamp = timestamp;
        this.beacon = beacon;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }
}
