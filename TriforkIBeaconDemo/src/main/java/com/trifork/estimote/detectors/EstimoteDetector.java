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

    @Inject Context context;
    @Inject Bus bus;
    @Inject Dao dao;
    @Inject PersistentState persistentState;

    private static final String GIMBAL_PROXIMITY_UUID = "8D81F247-15BB-4919-A3A1-C42BEF04F5FB";
    private static final String ESTIMOTE_PROXIMITY_UUID = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
    private static final Region FULL_SCAN_REGION = new Region("All", null, null, null);
//    private static final Region ALL_ESTIMOTE_BEACONS = new Region("EstimoteBeacon", ESTIMOTE_PROXIMITY_UUID, 3818, 7176);
//    private static final Region ALL_ESTIMOTE_BEACONS = new Region("GimbalToken", GIMBAL_PROXIMITY_UUID, 10, 20);

    // Beacon manager has very few methods for quering service state, so we need to handle all that by ourselves.
    private BeaconManager beaconManager;
    private boolean serviceReady = false;
    private boolean requestedRangingStart = false;
    private boolean rangingStarted = false;
    private boolean requestedMonitorStart = false;
    private boolean monitorStarted = false;
    private boolean requestedFullScan = false;
    private boolean fullScanStarted = false;
    private RegionHistoryEntry insideRegion = null;

    public EstimoteDetector() {
        BaseApplication.inject(this);
        this.beaconManager = new BeaconManager(context);
    }

    @Override
    public void startRanging() {
        if (requestedFullScan || fullScanStarted) throw new RuntimeException("Full scan in progress");
        if (!rangingStarted) {
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                    if (beacons.size() == 1) {
                        Log.d(TAG, "Discovered beacon: " + beacons.get(0));
                        bus.post(new BeaconScanCompleteEvent(Calendar.getInstance(), beacons.get(0)));
                    }
                }
            });

            requestedRangingStart = true;
            startBeaconService();
        }
    }

    @Override
    public void stopRanging() {
        if (!requestedRangingStart && !rangingStarted) return;

        try {
            beaconManager.stopRanging(persistentState.getSelectedBeacon());
            requestedRangingStart = false;
            rangingStarted = false;
            Log.d(TAG, "Ranging stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop ranging", e);
        }
    }

    @Override
    public void startMonitoring() {
        if (!monitorStarted) {
            beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
                @Override
                public void onEnteredRegion(final Region region, List<Beacon> beacons) {
                    Log.d(TAG, "Entered: " + region);
                    Toast.makeText(context, "Entered region " + region.getIdentifier(), Toast.LENGTH_SHORT).show();
                    dao.execute(new Runnable() {
                        @Override
                        public void run() {
                            insideRegion = dao.enterRegion(region);
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
                            dao.exitRegion(insideRegion);
                        }
                    });
                }
            });

            requestedMonitorStart = true;
            startBeaconService();
        }
    }

    @Override
    public void stopMonitoring() {
        if (!requestedMonitorStart && !monitorStarted) return;

        try {
            beaconManager.stopMonitoring(persistentState.getSelectedBeacon());
            requestedMonitorStart = false;
            monitorStarted = false;
            Log.d(TAG, "Monitoring stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop monitoring", e);
        }
    }

    @Override
    public void startFullScan() {
        if (requestedRangingStart || rangingStarted) throw new RuntimeException("Ranging scan in progress");
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                    Log.d(TAG, "Discovered beacons: " + beacons.size());
                    bus.post(new FullScanCompleteEvent(beacons));
                }
            }
        );

        requestedFullScan = true;
        startBeaconService();
    }

    @Override
    public void stopFullScan() {
        if (fullScanStarted) {
            try {
                beaconManager.stopRanging(FULL_SCAN_REGION);
                requestedFullScan = false;
                fullScanStarted = false;
                Log.d(TAG, "Full scan stopped");
            } catch (RemoteException e) {
                Log.e(TAG, "Could not stop full scan", e);
            }
        }
    }

    private void startBeaconService() {
        if (!serviceReady) {
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    serviceReady();
                }
            });
        } else {
            serviceReady();
        }
    }

    @Override
    public void onStop() {
        stopRanging();
        stopMonitoring();
        stopFullScan();
    }

    @Override
    public void onDestroy() {
        beaconManager.disconnect();
    }

    // Service is ready. Start requested services.
    private void serviceReady() {
        serviceReady = true;

        if (requestedRangingStart) {
            requestedRangingStart = false;
            try {
                Region selectedRegion = persistentState.getSelectedBeacon();
                if (selectedRegion == null) return;
                beaconManager.startRanging(selectedRegion);
                rangingStarted = true;
                Log.i(TAG, "Ranging started");
            } catch (RemoteException e) {
                Log.e(TAG, "Could not start ranging", e);
            }
        }

        if (requestedMonitorStart) {
            requestedMonitorStart = false;
            try {
                Region selectedRegion = persistentState.getSelectedBeacon();
                if (selectedRegion == null) return;
                beaconManager.startMonitoring(selectedRegion);
                monitorStarted = true;
                Log.i(TAG, "Monitoring started");

            } catch (RemoteException e) {
                Log.e(TAG, "Could not start monitoring", e);
            }
        }

        if (requestedFullScan) {
            requestedFullScan = false;
            try {
                beaconManager.startRanging(FULL_SCAN_REGION);
                fullScanStarted = true;
                Log.i(TAG, "Full scan started");

            } catch (RemoteException e) {
                Log.e(TAG, "Could not start full scan", e);
            }
        }
    }
}
