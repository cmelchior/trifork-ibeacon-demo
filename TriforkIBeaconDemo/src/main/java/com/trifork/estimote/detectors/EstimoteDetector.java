package com.trifork.estimote.detectors;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.squareup.otto.Bus;
import com.trifork.estimote.BaseApplication;
import com.trifork.estimote.database.Dao;
import com.trifork.estimote.database.RegionHistoryEntry;
import com.trifork.estimote.eventbus.BeaconScanCompleteEvent;
import com.trifork.estimote.eventbus.FullScanCompleteEvent;
import com.trifork.estimote.util.PersistentState;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.List;

public class EstimoteDetector implements IBeaconDetector {

    private static final String TAG =  EstimoteDetector.class.getName();
    private static final Region FULL_SCAN_REGION = new Region("All", null, null, null);

    @Inject Context context;
    @Inject Bus bus;
    @Inject Dao dao;
    @Inject PersistentState persistentState;

    // Beacon manager has very few methods for quering service state, so we need to handle all that by ourselves.
    private BeaconManager beaconManager;
    private boolean serviceReady = false;
    private boolean rangingStarted = false;
    private boolean monitorStarted = false;
    private boolean fullScanStarted = false;

    private RegionHistoryEntry currentRegion = null;

    public EstimoteDetector() {
        BaseApplication.inject(this);
        this.beaconManager = new BeaconManager(context);
    }

    @Override
    public void startRanging() {
        if (!serviceReady) throw new RuntimeException("Service isn't started");
        if (fullScanStarted) throw new RuntimeException("Full scan in progress");
        if (rangingStarted) return;

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                if (beacons.size() == 1) {
                    Log.d(TAG, "Discovered beacon: " + beacons.get(0));
                    bus.post(new BeaconScanCompleteEvent(Calendar.getInstance(), beacons.get(0)));
                }
            }
        });

        try {
            Region selectedRegion = persistentState.getSelectedRegion();
            if (selectedRegion == null) return;
            beaconManager.startRanging(selectedRegion);
            rangingStarted = true;
            Log.i(TAG, "Ranging started");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start ranging", e);
        }
    }

    @Override
    public void stopRanging() {
        if (!rangingStarted) return;
        try {
            beaconManager.stopRanging(persistentState.getSelectedRegion());
            rangingStarted = false;
            Log.d(TAG, "Ranging stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop ranging", e);
        }
    }

    @Override
    public void startMonitoring() {
        if (!serviceReady) throw new RuntimeException("Service isn't started");
        if (monitorStarted) return;

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(final Region region, List<Beacon> beacons) {
                Log.d(TAG, "Entered: " + region);
                Toast.makeText(context, "Entered region " + region.getIdentifier(), Toast.LENGTH_SHORT).show();
                dao.execute(new Runnable() {
                    @Override
                    public void run() {
                        currentRegion = dao.enterRegion(region);
                    }
                });
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "Exit: " + region);
                Toast.makeText(context, "Exited region " + region.getIdentifier(), Toast.LENGTH_SHORT).show();
                dao.execute(new Runnable() {
                    @Override
                    public void run() {
                        dao.exitRegion(currentRegion);
                    }
                });
            }
        });

        try {
            Region selectedRegion = persistentState.getSelectedRegion();
            if (selectedRegion == null) return;
            beaconManager.startMonitoring(selectedRegion);
            monitorStarted = true;
            Log.i(TAG, "Monitoring started");

        } catch (RemoteException e) {
            Log.e(TAG, "Could not start monitoring", e);
        }
    }

    @Override
    public void stopMonitoring() {
        if (!monitorStarted) return;
        try {
            beaconManager.stopMonitoring(persistentState.getSelectedRegion());
            monitorStarted = false;
            Log.d(TAG, "Monitoring stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop monitoring", e);
        }
    }

    @Override
    public void startFullScan() {
        if (rangingStarted) throw new RuntimeException("Ranging scan in progress");
        if (fullScanStarted) return;

        beaconManager.setRangingListener(
                new BeaconManager.RangingListener() {
                    @Override
                    public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                        Log.d(TAG, "Discovered beacons: " + beacons.size());
                        bus.post(new FullScanCompleteEvent(beacons));
                    }
                }
        );

        try {
            beaconManager.startRanging(FULL_SCAN_REGION);
            fullScanStarted = true;
            Log.i(TAG, "Full scan started");

        } catch (RemoteException e) {
            Log.e(TAG, "Could not start full scan", e);
        }
    }

    @Override
    public void stopFullScan() {
        if (!fullScanStarted) return;
        try {
            beaconManager.stopRanging(FULL_SCAN_REGION);
            fullScanStarted = false;
            Log.d(TAG, "Full scan stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop full scan", e);
        }
    }

    @Override
    public void connect(final ServiceReadyCallback callback) {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                serviceReady = true;
                callback.serviceReady();
            }
        });
    }

    @Override
    public void disconnect() {
        beaconManager.disconnect();
    }

    @Override
    public boolean isServiceReady() {
        return serviceReady;
    }
}
