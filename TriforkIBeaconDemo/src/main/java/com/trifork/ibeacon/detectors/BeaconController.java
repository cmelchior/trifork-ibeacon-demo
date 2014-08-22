package com.trifork.ibeacon.detectors;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.trifork.ibeacon.BaseApplication;
import com.trifork.ibeacon.database.Dao;
import com.trifork.ibeacon.database.RegionHistoryEntry;
import com.trifork.ibeacon.eventbus.BeaconScanCompleteEvent;
import com.trifork.ibeacon.eventbus.FullScanCompleteEvent;
import com.trifork.ibeacon.eventbus.OttoEvent;
import com.trifork.ibeacon.util.PersistentState;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Calendar;
import java.util.Collection;

import javax.inject.Inject;

public class BeaconController implements BeaconConsumer {

    private static final String TAG = BeaconController.class.getName();
    private static final Region FULL_SCAN_REGION = new Region("All", null, null, null);

    @Inject Context context;
    @Inject Bus bus;
    @Inject PersistentState persistentState;
    @Inject Dao dao;

    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final BeaconManager beaconManager;
    private boolean rangingStarted = false;
    private boolean monitorStarted = false;
    private boolean fullScanStarted = false;
    private boolean serviceReady = false;

    private RegionHistoryEntry currentRegion;
    private ServiceReadyCallback serviceReadyCallback;

    public BeaconController() {
        BaseApplication.inject(this);
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().set(0, new IBeaconParser()); // Replace AltBeacon parser with iBeacon parser
    }

    public void startRanging() {
        assertServiceReady();
        if (fullScanStarted) throw new RuntimeException("Full scan in progress");
        if (rangingStarted) return;

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> iBeacons, Region region) {
                if (iBeacons.size() == 1) {
                    Beacon beacon = iBeacons.iterator().next();
                    Log.d(TAG, "Discovered beacon: " + beacon);
                    postOnMainThread(new BeaconScanCompleteEvent(Calendar.getInstance(), beacon));
                }
            }
        });

        try {
            Region selectedRegion = persistentState.getSelectedRegion();
            if (selectedRegion == null) return;
            beaconManager.startRangingBeaconsInRegion(persistentState.getSelectedRegion());
            rangingStarted = true;
            Log.i(TAG, "Ranging started");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start ranging", e);
        }
    }

    public void stopRanging() {
        if (!rangingStarted) return;
        try {
            beaconManager.stopRangingBeaconsInRegion(persistentState.getSelectedRegion());
            rangingStarted = false;
            Log.d(TAG, "Ranging stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop ranging", e);
        }
    }

    public void startMonitoring() {
        assertServiceReady();
        if (monitorStarted) return;

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(final Region region) {
                Log.d(TAG, "Entered: " + region);
                Toast.makeText(context, "Entered region " + region.getUniqueId(), Toast.LENGTH_SHORT).show();
                dao.execute(new Runnable() {
                    @Override
                    public void run() {
                        currentRegion = dao.enterRegion(region);
                    }
                });
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG, "Exit: " + region);
                Toast.makeText(context, "Exited region " + region.getUniqueId(), Toast.LENGTH_SHORT).show();
                dao.execute(new Runnable() {
                    @Override
                    public void run() {
                        dao.exitRegion(currentRegion);
                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        try {
            Region selectedRegion = persistentState.getSelectedRegion();
            if (selectedRegion == null) return;
            beaconManager.startMonitoringBeaconsInRegion(selectedRegion);
            monitorStarted = true;
            Log.i(TAG, "Monitoring started");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start ranging", e);
        }
    }

    public void stopMonitoring() {
        if (!monitorStarted) return;
        try {
            beaconManager.stopMonitoringBeaconsInRegion(persistentState.getSelectedRegion());
            monitorStarted = false;
            Log.d(TAG, "Monitoring stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop monitoring", e);
        }
    }

    public void startFullScan() {
        assertServiceReady();
        if (rangingStarted) throw new RuntimeException("Ranging scan in progress");
        if (fullScanStarted) return;

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> iBeacons, Region region) {
                postOnMainThread(new FullScanCompleteEvent(iBeacons));
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(FULL_SCAN_REGION);
            fullScanStarted = true;
            Log.i(TAG, "Full scan started");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start full scan", e);
        }
    }

    private void assertServiceReady() {
        if (!serviceReady) throw new RuntimeException("Service not started");
    }

    public void stopFullScan() {
        if (!fullScanStarted) return;
        try {
            beaconManager.stopRangingBeaconsInRegion(FULL_SCAN_REGION);
            fullScanStarted = false;
            Log.d(TAG, "Full scan stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop full scan", e);
        }
    }

    public void connect(ServiceReadyCallback callback) {
        serviceReadyCallback = callback;
        if (!serviceReady) {
            beaconManager.bind(this);
        } else {
            serviceReadyCallback.serviceReady();
        }
    }

    public void disconnect() {
        beaconManager.unbind(this);
    }

    public boolean isServiceReady() {
        return serviceReady;
    }

    @Override
    public void onBeaconServiceConnect() {
        serviceReady = true;
        if (serviceReadyCallback != null) {
            serviceReadyCallback.serviceReady();
        }
    }

    @Override
    public Context getApplicationContext() {
        return context;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return context.bindService(intent, serviceConnection, i);
    }

    private void postOnMainThread(final OttoEvent event) {
        mainThread.post( new Runnable() {
            @Override
            public void run() {
                bus.post(event);
            }
        });
    }

    public boolean isRangingSingleBeacon() {
        return rangingStarted;
    }

    public boolean isMonitoringRegion() {
        return monitorStarted;
    }

    public boolean isFullScanning() {
        return fullScanStarted;
    }

    public interface ServiceReadyCallback {
        public void serviceReady();
    }
}
