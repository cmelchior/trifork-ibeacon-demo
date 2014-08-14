package com.trifork.ibeacon.eventbus;

import org.altbeacon.beacon.Beacon;

public class StopTransmitEvent implements OttoEvent {

    private Beacon beacon;

    public StopTransmitEvent(Beacon beacon) {
        this.beacon = beacon;
    }

    public Beacon getBeacon() {
        return beacon;
    }
}
