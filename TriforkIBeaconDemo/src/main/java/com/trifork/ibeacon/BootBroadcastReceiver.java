package com.trifork.ibeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

public class BootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BootBroadcastReceiver.class.getName();

    @Inject com.trifork.ibeacon.detectors.BeaconController detector;

    public BootBroadcastReceiver() {
        BaseApplication.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        detector.startMonitoring();
    }
}
