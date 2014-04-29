package com.trifork.estimote.detectors;

public interface IBeaconDetector {

    /**
     * Connects/Starts the IBeaconDetector service.
     * All other calls will fail if the service isn't connected first.
     */
    void connect(ServiceReadyCallback callback);

    /**
     * Stops the IBeaconDetector service
     */
    void disconnect();

    /**
     * @return Returns true if the service is already running, false otherwise.
     */
    boolean isServiceReady();

    void startRanging();
    void stopRanging();

    void startMonitoring();
    void stopMonitoring();

    void startFullScan();
    void stopFullScan();

    public interface ServiceReadyCallback {
        public void serviceReady();
    }
}
