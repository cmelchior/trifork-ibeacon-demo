package com.trifork.ibeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.trifork.ibeacon.detectors.BeaconScanner;

import javax.inject.Inject;

public class BootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BootBroadcastReceiver.class.getName();

    @Inject BeaconScanner detector;

    public BootBroadcastReceiver() {
        BaseApplication.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        detector.startMonitoring();
    }
}
