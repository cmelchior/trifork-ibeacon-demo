package com.trifork.ibeacon.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.InjectView;
import com.echo.holographlibrary.*;
import com.estimote.sdk.Utils;
import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.BeaconScanCompleteEvent;
import com.trifork.ibeacon.eventbus.NewBeaconSelectedEvent;
import com.trifork.ibeacon.eventbus.RequestBeaconScanEvent;
import com.trifork.ibeacon.util.CircularBuffer;

import org.altbeacon.beacon.Beacon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import static com.trifork.ibeacon.R.*;

public class BeaconDataFragment extends BaseFragment {

    @InjectView(id.value_uuid) TextView uuidView;
    @InjectView(id.value_major) TextView majorView;
    @InjectView(id.value_minor) TextView minorView;
    @InjectView(id.value_txpower) TextView txPowerView;
    @InjectView(id.value_lastreading) TextView lastReadingView;
    @InjectView(id.value_timestamp) TextView timestampView;
    @InjectView(id.graph_rssi) LineGraph rssiGraphView;
    @InjectView(id.graph_distance) BarGraph distanceGraphView;

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.S");
    private CircularBuffer<Integer> readings = new CircularBuffer<>(100);
    private CircularBuffer<Utils.Proximity> distances = new CircularBuffer<>(100);

    public static BeaconDataFragment newInstance() {
        Bundle args = new Bundle();
        BeaconDataFragment fragment = new BeaconDataFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_ibeacondata, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resetViews();
    }

    private void resetViews() {
        uuidView.setText(string.empty_placeholder);
        majorView.setText(string.empty_placeholder);
        minorView.setText(string.empty_placeholder);
        txPowerView.setText(string.empty_placeholder);
        lastReadingView.setText(string.empty_placeholder);
        timestampView.setText(string.empty_placeholder);
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
        if (isVisibleToUser && isResumed()) {
            startScan();
        }
    }

    private void startScan() {
        bus.post(new RequestBeaconScanEvent());
    }


    @Subscribe
    public void beaconFound(BeaconScanCompleteEvent event) {
        Beacon beacon = event.getBeacon();
        Calendar time = event.getTimestamp();
        readings.add(beacon.getRssi());
        distances.add(Utils.computeProximity(beacon));

        // Update beacon info and reading
        if (uuidView == null) return;
        uuidView.setText(beacon.getProximityUUID());
        majorView.setText(Integer.toString(beacon.getMajor()));
        minorView.setText(Integer.toString(beacon.getMinor()));
        txPowerView.setText(String.format("%s dBm", beacon.getMeasuredPower()));
        lastReadingView.setText(String.format("%s dBm (%s m)", beacon.getRssi(), com.trifork.ibeacon.util.Utils.formatRange(Utils.computeAccuracy(beacon))));
        timestampView.setText(timeFormatter.format(time.getTime()));

        updateRSSIGraph();
        updateDistanceGraph();
    }

    @Subscribe
    public void onNewBeaconSelected(NewBeaconSelectedEvent event) {
        readings.clear();
        distances.clear();
    }

    private void updateRSSIGraph() {
        rssiGraphView.removeAllLines();

        Line l = new Line();
        l.setShowingPoints(false);
        l.setUsingDips(false);
        l.setColor(getResources().getColor(color.yellow));
        Iterator<Integer> iterator = readings.iterator();
        int i  = 0;
        while(iterator.hasNext()) {
            int reading = iterator.next();
            LinePoint p = new LinePoint(i, reading * - 1);
            l.addPoint(p);
            i++;
        }

        rssiGraphView.setGraphBackgroundColor(getResources().getColor(R.color.graph_background));
        rssiGraphView.addLine(l);
        rssiGraphView.setRangeY(0, 100);
        rssiGraphView.setRangeX(0, 100);
        rssiGraphView.setClickable(false);
        rssiGraphView.setLineToFill(0);
    }

    private void updateDistanceGraph() {
        ArrayList<Bar> points = new ArrayList<>();
        Iterator<Utils.Proximity> iterator = distances.iterator();
        for (int i = 0; i < 100; i++) {
            Bar b = new Bar();
            int value;
            int color;
            Utils.Proximity distance = (iterator.hasNext()) ? iterator.next() : Utils.Proximity.UNKNOWN;
            switch(distance) {
                case IMMEDIATE:
                    value = 1;
                    color = getResources().getColor(R.color.green);
                    break;
                case NEAR:
                    value = 2;
                    color = getResources().getColor(R.color.yellow);
                    break;
                case FAR:
                    value = 3;
                    color = getResources().getColor(R.color.red);
                    break;
                default:
                    value = 0;
                    color = Color.TRANSPARENT;
                    break;
            }
            b.setValue(value);
            b.setColor(color);
            b.setName("");
            points.add(b);
        }

        distanceGraphView.setMaxValue(3);
        distanceGraphView.setClickable(false);
        distanceGraphView.setBars(points);
        distanceGraphView.setShowBarText(false);
    }
}
