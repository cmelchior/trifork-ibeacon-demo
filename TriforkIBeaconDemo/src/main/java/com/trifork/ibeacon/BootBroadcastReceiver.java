package com.trifork.ibeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.trifork.ibeacon.detectors.BeaconController;
import com.trifork.ibeacon.util.PersistentState;

import org.altbeacon.beacon.Region;

import javax.inject.Inject;

public class BootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BootBroadcastReceiver.class.getName();

    @Inject PersistentState persistentState;
    @Inject BeaconController controller;

    public BootBroadcastReceiver() {
        BaseApplication.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Region region = persistentState.getSelectedRegion();
        if (region != null) {
            controller.connect(new BeaconController.ServiceReadyCallback() {
                @Override
                public void serviceReady() {
                    controller.startMonitoring(region);
                }
            });
        }
    }
}
