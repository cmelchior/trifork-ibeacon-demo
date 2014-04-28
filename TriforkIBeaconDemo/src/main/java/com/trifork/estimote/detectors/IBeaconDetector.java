package com.trifork.estimote.detectors;

public interface IBeaconDetector {
    // Lifecycle methods
    public void onStop();
    void onDestroy();

    void startRanging();
    void stopRanging();

    void startMonitoring();
    void stopMonitoring();

    void startFullScan();
    void stopFullScan();
}
