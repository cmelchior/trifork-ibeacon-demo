package com.trifork.ibeacon.ui;

import android.graphics.PointF;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.RangeScanCompleteEvent;
import com.trifork.ibeacon.eventbus.RequestBeaconScanEvent;
import com.trifork.ibeacon.widgets.LocationTrackerView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.Locale;

import butterknife.InjectView;

public class LocationFragment extends BaseFragment {

    @InjectView(R.id.room) LocationTrackerView roomView;
    @InjectView(R.id.width) EditText widthView;
    @InjectView(R.id.height) EditText heightView;
    @InjectView(R.id.beacon1data) TextView beacon1DataView;
    @InjectView(R.id.beacon2data) TextView beacon2DataView;
    @InjectView(R.id.beacon3data) TextView beacon3DataView;
    @InjectView(R.id.beacon1Coords) TextView beacon1CoordsView;
    @InjectView(R.id.beacon2Coords) TextView beacon2CoordsView;
    @InjectView(R.id.beacon3Coords) TextView beacon3CoordsView;

    private DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
    private PointF beacon1Coordinates = new PointF(0,0);
    private PointF beacon2Coordinates = new PointF(0,0);
    private PointF beacon3Coordinates = new PointF(0,0);

    private Region region = new Region("Triangulation", Identifier.parse("94041D10-2BB5-11E4-8C21-0800200C9A66"), Identifier.fromInt(1), null);

    public static LocationFragment newInstance() {
        Bundle args = new Bundle();
        LocationFragment fragment = new LocationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indoorlocation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        widthView.setText(Float.toString(roomView.getRoomWidth()));
        heightView.setText(Float.toString(roomView.getRoomHeight()));

        widthView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    roomView.setRoomWidth(Float.parseFloat(s.toString()));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });

        heightView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    roomView.setRoomHeight(Float.parseFloat(s.toString()));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });

        roomView.setOnBeaconMovedListener(new LocationTrackerView.OnBeaconMovedListener() {
            @Override
            public void onBeaconMoved(int id, float xMeters, float yMeters) {
                switch (id) {
                    case 1:
                        beacon1CoordsView.setText(String.format("(%s, %s)", formatter.format(xMeters), formatter.format(yMeters)));
                        break;

                    case 2:
                        beacon2CoordsView.setText(String.format("(%s, %s)", formatter.format(xMeters), formatter.format(yMeters)));
                        break;

                    case 3:
                        beacon3CoordsView.setText(String.format("(%s, %s)", formatter.format(xMeters), formatter.format(yMeters)));
                        break;

                    default:
                        // Ignore
                }

                roomView.invalidate();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            startScan();
        }
    }

    private void startScan() {
        bus.post(new RequestBeaconScanEvent(region));
    }

    @Subscribe
    public void beaconsDetected(RangeScanCompleteEvent event) {
         for (Beacon b : event.getBeacons()) {
             float distance = (float) b.getDistance();
             switch(b.getId3().toInt()) {
                 case 1:
                     roomView.updateBeaconDistance(0, distance);
                     beacon1DataView.setText(distance + " m.");
                     break;
                 case 2:
                     roomView.updateBeaconDistance(1, distance);
                     beacon2DataView.setText(distance + " m.");
                     break;
                 case 3:
                     roomView.updateBeaconDistance(2, distance);
                     beacon3DataView.setText(distance + " m.");
                 default:
                     // Ignore
             }

             roomView.invalidate();
         }

        trilateratePosition();
    }

    private void trilateratePosition() {

    }

}
