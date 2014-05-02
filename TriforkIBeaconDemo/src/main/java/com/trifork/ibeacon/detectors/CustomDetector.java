package com.trifork.ibeacon.detectors;

import android.content.Context;

import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Bus;
import com.trifork.ibeacon.BaseApplication;
import com.trifork.ibeacon.eventbus.FullScanCompleteEvent;

import java.util.List;

import javax.inject.Inject;

/**
 * Custom IBeacon detector.
 * Will only do full scans right now.
 */
public class CustomDetector implements IBeaconDetector {

    @Inject Context context;
    @Inject Bus bus;

    private final BeaconManager beaconManager;

    public CustomDetector() {
        BaseApplication.inject(this);
        beaconManager = new BeaconManager(context);
    }


    @Override
    public void connect(ServiceReadyCallback callback) {
        if (callback != null) {
            callback.serviceReady();
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isServiceReady() {
        return true;
    }

    @Override
    public void startRanging() {
    }

    @Override
    public void stopRanging() {

    }

    @Override
    public void startMonitoring() {

    }

    @Override
    public void stopMonitoring() {

    }

    @Override
    public void startFullScan() {
        beaconManager.setRangingCallback(new BeaconManager.ScanCompletedCallback() {
            @Override
            public void beaconsDiscovered(List<IBeacon> beacons) {
                bus.post(new FullScanCompleteEvent(beacons));
            }
        });

        beaconManager.startFullscan();
    }

    @Override
    public void stopFullScan() {

    }
}
