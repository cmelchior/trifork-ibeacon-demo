package com.trifork.ibeacon.eventbus;

import com.trifork.ibeacon.util.Utils;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FullScanCompleteEvent implements OttoEvent {

    private final List<Beacon> beacons;

    public FullScanCompleteEvent(Collection<Beacon> beacons) {
        this.beacons = new ArrayList(beacons);
    }

    public FullScanCompleteEvent(List<com.estimote.sdk.Beacon> iBeacons) {
        beacons = new ArrayList<Beacon>();
        Iterator<com.estimote.sdk.Beacon> it = iBeacons.iterator();
        while(it.hasNext()) {
            beacons.add(Utils.convertIBeacon(it.next()));
        }
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }
}
