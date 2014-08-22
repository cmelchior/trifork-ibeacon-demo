package com.trifork.ibeacon;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

public class BeaconController implements BeaconConsumer {

    private Context context;

    public BeaconController(Context context) {
        this.context = context;
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().set(0,
                new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setForegroundScanPeriod(1100);
        beaconManager.setForegroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(10000);
        beaconManager.setBackgroundBetweenScanPeriod(5*60*1000);
        beaconManager.setBackgroundMode(true);

        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        // BeaconService ready
    }

    @Override
    public Context getApplicationContext() {
        return context.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection connection) {
        context.unbindService(connection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection connection, int mode) {
        return context.bindService(intent, connection, mode);
    }
}
