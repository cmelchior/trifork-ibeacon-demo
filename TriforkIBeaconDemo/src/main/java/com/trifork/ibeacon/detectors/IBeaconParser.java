package com.trifork.ibeacon.detectors;

import org.altbeacon.beacon.BeaconParser;

/**
 * @author Christian Melchior
 *
 * See http://stackoverflow.com/questions/18906988/what-is-the-ibeacon-bluetooth-profile for futher
 * info.
 */
public class IBeaconParser extends BeaconParser {

    public IBeaconParser() {
        super();
        this.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    }
}
