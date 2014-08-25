package com.trifork.ibeacon.eventbus;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class RangeScanCompleteEvent implements OttoEvent {

    private final List<Beacon> beacons;
    private final Calendar timestamp;

    public RangeScanCompleteEvent(Calendar timestamp, Collection<Beacon> beacons) {
        this.timestamp = timestamp;
        this.beacons = new ArrayList<>(beacons);
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }
}
