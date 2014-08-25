package com.trifork.ibeacon.eventbus;

import org.altbeacon.beacon.Region;

public class StopScanEvent implements OttoEvent {

    private Region region;

    public StopScanEvent(Region region) {
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }
}
