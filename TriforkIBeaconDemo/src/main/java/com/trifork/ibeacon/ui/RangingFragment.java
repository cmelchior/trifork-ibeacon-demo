package com.trifork.ibeacon.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.InjectView;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.BeaconScanCompleteEvent;
import com.trifork.ibeacon.eventbus.RequestBeaconScanEvent;

public class RangingFragment extends BaseFragment {

    @InjectView(R.id.container) View container;
    @InjectView(R.id.range) TextView rangeView;
    @InjectView(R.id.proximity) TextView proximityView;

    public static RangingFragment newInstance() {
        Bundle args = new Bundle();
        RangingFragment fragment = new RangingFragment();
        fragment.setArguments(args);
        return fragment;
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            bus.post(new RequestBeaconScanEvent());
        }
    }

    @Subscribe
    public void onBeaconDataReceived(BeaconScanCompleteEvent event) {
        Beacon beacon = event.getBeacon();
        double distanceM = Utils.computeAccuracy(beacon);
        Utils.Proximity proximity = Utils.computeProximity(beacon);

        rangeView.setText(com.trifork.ibeacon.util.Utils.formatRange(distanceM) + "m");
        proximityView.setText(proximity.toString());

        // Transition between colors based on distances
        // < 0.5 -> green
        // 5 > x > 0.5 -> yellow
        // > 5 -> red
    }
}
