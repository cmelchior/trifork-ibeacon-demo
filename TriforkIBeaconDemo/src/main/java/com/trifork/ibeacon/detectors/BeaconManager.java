package com.trifork.ibeacon.detectors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.trifork.ibeacon.BuildConfig;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Custom BeaconManager to see how LE Scans happen
 */
public class BeaconManager {

    private static final long SCAN_PERIOD = 5000;
    private static final char[] HEX_ARRAY = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter adapter;
    private boolean scanning = false;
    private Handler handler = new Handler();

    private ScanCompletedCallback rangingScanCallback;
    private HashSet<Beacon> discoveredBeacons = new HashSet<>();
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Beacon result = getBeaconsFromScanRecord(device, rssi, scanRecord);
            if (result != null) {
                discoveredBeacons.add(result);
            }
        }
    };

    public BeaconManager(Context context) {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    adapter.stopLeScan(leScanCallback);
                    ArrayList<Beacon> result = new ArrayList<>();
                    result.addAll(discoveredBeacons);
                    if (rangingScanCallback != null) {
                        rangingScanCallback.beaconsDiscovered(result);
                    }
                }
            }, SCAN_PERIOD);

            scanning = true;
            discoveredBeacons.clear();
            adapter.startLeScan(leScanCallback);
        } else {
            scanning = false;
            adapter.stopLeScan(leScanCallback);
        }
    }

    public void setRangingCallback(ScanCompletedCallback callback) {
        this.rangingScanCallback = callback;
    }

    public void startFullscan() {
        scanLeDevice(true);
    }

    public interface ScanCompletedCallback {
        public void beaconsDiscovered(List<Beacon> beacons);
    }

    // Copy from RadiusNetwork SDK
    private Beacon getBeaconsFromScanRecord(BluetoothDevice device, int rssi, byte[] scanData) {
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((scanData[(startByte + 2)] & 0xFF) == 2) && ((scanData[(startByte + 3)] & 0xFF) == 21)) {
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (!patternFound) {
            if (BuildConfig.DEBUG) Log.d("IBeacon", new StringBuilder().append("This is not an iBeacon advertisment (no 0215 seen in bytes 4-7).  The bytes I see are: ").append(bytesToHex(scanData)).toString());
            return null;
        }

        int major = ((scanData[(startByte + 20)] & 0xFF) * 256 + (scanData[(startByte + 21)] & 0xFF));
        int minor = ((scanData[(startByte + 22)] & 0xFF) * 256 + (scanData[(startByte + 23)] & 0xFF));
        int txPower = scanData[(startByte + 24)];

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
        String hexString = bytesToHex(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0, 8));
        sb.append("-");
        sb.append(hexString.substring(8, 12));
        sb.append("-");
        sb.append(hexString.substring(12, 16));
        sb.append("-");
        sb.append(hexString.substring(16, 20));
        sb.append("-");
        sb.append(hexString.substring(20, 32));
        String proximityUuid = sb.toString();

        return new Beacon.Builder()
                .setId1(proximityUuid)
                .setId2(Integer.toString(major))
                .setId3(Integer.toString(minor))
                .build();
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[(j * 2)] = HEX_ARRAY[(v >>> 4)];
            hexChars[(j * 2 + 1)] = HEX_ARRAY[(v & 0xF)];
        }
        return new String(hexChars);
    }
}
