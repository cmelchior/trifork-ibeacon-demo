package com.trifork.ibeacon.eventbus;

import com.estimote.sdk.Beacon;
import com.radiusnetworks.ibeacon.IBeacon;
import com.trifork.ibeacon.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FullScanCompleteEvent implements OttoEvent {

    private final List<Beacon> beacons;

    public FullScanCompleteEvent(List<Beacon> beacons) {
        this.beacons = beacons;
    }

    public FullScanCompleteEvent(Collection<IBeacon> iBeacons) {
        beacons = new ArrayList<>();
        Iterator<IBeacon> it = iBeacons.iterator();
        while(it.hasNext()) {
            beacons.add(Utils.convertIBeacon(it.next()));
        }
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }
}
