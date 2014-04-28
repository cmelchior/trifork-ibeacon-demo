package com.trifork.estimote.eventbus;

import com.estimote.sdk.Beacon;

import java.util.List;

public class FullScanCompleteEvent implements OttoEvent {

    private final List<Beacon> beacons;

    public FullScanCompleteEvent(List<Beacon> beacons) {
        this.beacons = beacons;
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }
}
