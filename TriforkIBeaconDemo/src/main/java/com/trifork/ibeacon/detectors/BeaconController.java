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
import com.trifork.ibeacon.eventbus.RangeScanCompleteEvent;
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
    @Inject Dao dao;

    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final BeaconManager beaconManager;
    private boolean serviceReady = false;

    private RegionHistoryEntry currentRegion;
    private ServiceReadyCallback serviceReadyCallback;

    public BeaconController() {
        BaseApplication.inject(this);
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().set(0, new IBeaconParser()); // Replace AltBeacon parser with iBeacon parser
        dao.open();
    }

    public void startRanging(Region region) {
        assertServiceReady();

        if (beaconManager.getRangedRegions().contains(region)) return; // Ignore if already ranging that region
        for (Region r : beaconManager.getRangedRegions()) {
            stopRanging(r);
        }

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    postOnMainThread(new RangeScanCompleteEvent(Calendar.getInstance(), beacons));
                }
            }
        });

        try {
            if (region != null) {
                beaconManager.startRangingBeaconsInRegion(region);
                Log.i(TAG, "Ranging started");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start ranging", e);
        }
    }

    public void stopRanging(Region region) {
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            Log.d(TAG, "Ranging stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop ranging", e);
        }
    }

    public void startMonitoring(Region region) {
        assertServiceReady();

        // For demo purposes we only accept Regions that point to a specific beacon
        if (region.getId1() == null || region.getId2() == null || region.getId3() == null) {
            return;
        }

        if (beaconManager.getMonitoredRegions().contains(region)) return;
        for (Region r : beaconManager.getMonitoredRegions()) {
            stopMonitoring(r);
        }

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
            beaconManager.startMonitoringBeaconsInRegion(region);
            Log.i(TAG, "Monitoring started");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start ranging", e);
        }
    }

    public void stopMonitoring(Region region) {
        try {
            beaconManager.stopMonitoringBeaconsInRegion(region);
            Log.d(TAG, "Monitoring stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not stop monitoring", e);
        }
    }

    public void startFullScan() {
        assertServiceReady();

        if (beaconManager.getRangedRegions().contains(FULL_SCAN_REGION)) return;
        for (Region r : beaconManager.getRangedRegions()) {
            stopRanging(r);
        }

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> iBeacons, Region region) {
                postOnMainThread(new FullScanCompleteEvent(iBeacons));
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(FULL_SCAN_REGION);
            Log.i(TAG, "Full scan started");
        } catch (RemoteException e) {
            Log.e(TAG, "Could not start full scan", e);
        }
    }

    private void assertServiceReady() {
        if (!serviceReady) throw new RuntimeException("Service not started");
    }

    public void stopFullScan() {
        stopRanging(FULL_SCAN_REGION);
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

    public void setBackgroundMode(boolean inBackground) {
        assertServiceReady();
        beaconManager.setBackgroundMode(inBackground);
    }

    public interface ServiceReadyCallback {
        public void serviceReady();
    }
}
