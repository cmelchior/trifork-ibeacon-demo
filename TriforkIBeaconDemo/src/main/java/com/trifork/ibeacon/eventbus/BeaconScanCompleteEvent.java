package com.trifork.ibeacon.eventbus;

import com.estimote.sdk.Beacon;
import com.radiusnetworks.ibeacon.IBeacon;
import com.trifork.ibeacon.util.Utils;

import java.util.Calendar;

public class BeaconScanCompleteEvent implements OttoEvent {

    private final Beacon beacon;
    private final Calendar timestamp;

    public BeaconScanCompleteEvent(Calendar timestamp, Beacon beacon) {
        this.timestamp = timestamp;
        this.beacon = beacon;
    }

    public BeaconScanCompleteEvent(Calendar timestamp, IBeacon beacon) {
        this.timestamp = timestamp;
        this.beacon = Utils.convertIBeacon(beacon);
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }
}
