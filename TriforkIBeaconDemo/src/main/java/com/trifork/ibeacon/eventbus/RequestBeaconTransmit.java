package com.trifork.ibeacon.eventbus;

import org.altbeacon.beacon.Beacon;

public class RequestBeaconTransmit implements OttoEvent {

    private Beacon beacon;
    private int advertiseMode;
    private int txPowerLevel;

    public RequestBeaconTransmit(Beacon beacon, int advertiseMode, int txPowerLevel) {
        this.beacon = beacon;
        this.advertiseMode = advertiseMode;
        this.txPowerLevel = txPowerLevel;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public int getAdvertiseMode() {
        return advertiseMode;
    }

    public int getTxPowerLevel() {
        return txPowerLevel;
    }
}
