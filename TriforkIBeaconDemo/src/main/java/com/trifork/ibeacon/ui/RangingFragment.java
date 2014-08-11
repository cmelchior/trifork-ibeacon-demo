package com.trifork.ibeacon.ui;


import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.BeaconScanCompleteEvent;
import com.trifork.ibeacon.eventbus.RequestBeaconScanEvent;
import com.trifork.ibeacon.util.Utils;

import org.altbeacon.beacon.Beacon;

public class RangingFragment extends BaseFragment {

    private static final int MIN_PAUSE_MS = 125; // Minimum pause between beeps at minimum distance
    private static final int MAX_PAUSE_MS = 2000; // Pause between beeps at maximum distance

    @InjectView(R.id.range) TextView rangeView;
    @InjectView(R.id.proximity) TextView proximityView;

    private Handler handler = new Handler();
    private ToneGenerator toneG;
    private int pause = MAX_PAUSE_MS;
    private Runnable toneRunner = new Runnable() {
        @Override
        public void run() {
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 125);
            handler.postDelayed(this, pause);
        }
    };

    public static RangingFragment newInstance() {
        Bundle args = new Bundle();
        RangingFragment fragment = new RangingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detector, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rangeView.setText(R.string.empty_placeholder);
        proximityView.setText(Utils.Proximity.UNKNOWN.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            startScan();
        }
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && isVisibleToUser) {
            startScan();
        } else {
            stopSound();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(toneRunner);
    }

    @Subscribe
    public void onBeaconDataReceived(BeaconScanCompleteEvent event) {
        Beacon beacon = event.getBeacon();
        double distanceM = beacon.getDistance();
        Utils.Proximity proximity = Utils.proximityFromDistance(beacon.getDistance());

        rangeView.setText(com.trifork.ibeacon.util.Utils.formatRange(distanceM) + "m");
        proximityView.setText(proximity.toString());

        // Calculate new pause based on current RSSI
        // Convert to positive numbers to not make my head hurt.
        float txPower = -event.getBeacon().getTxPower();
        float rssi = -event.getBeacon().getRssi();
        if (rssi <= txPower) {
            pause = MIN_PAUSE_MS;
        } else if (rssi >= 100) {
            pause = MAX_PAUSE_MS;
        } else {
            float factor = ((rssi - txPower)/(100 - txPower));
            pause = (int) (MIN_PAUSE_MS + (MAX_PAUSE_MS - MIN_PAUSE_MS) * factor);
        }
    }

    private void startScan() {
        bus.post(new RequestBeaconScanEvent());
        if (persistentState.getSelectedRegion() != null) {
            handler.post(toneRunner);
        }
    }

    private void stopSound() {
        handler.removeCallbacks(toneRunner);
    }
}
