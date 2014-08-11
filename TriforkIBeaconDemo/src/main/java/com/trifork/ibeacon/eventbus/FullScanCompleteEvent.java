package com.trifork.ibeacon.eventbus;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FullScanCompleteEvent implements OttoEvent {

    private final List<Beacon> beacons;

    public FullScanCompleteEvent(Collection<Beacon> beacons) {
        this.beacons = new ArrayList(beacons);
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }
}
