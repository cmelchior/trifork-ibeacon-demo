package com.trifork.estimote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.trifork.estimote.detectors.IBeaconDetector;

import javax.inject.Inject;

public class BootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BootBroadcastReceiver.class.getName();

    @Inject IBeaconDetector detector;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (detector != null) {
            detector.startMonitoring();
        }
    }
}
