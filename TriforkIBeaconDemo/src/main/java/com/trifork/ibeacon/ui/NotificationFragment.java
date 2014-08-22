package com.trifork.ibeacon.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.BeaconScanCompleteEvent;
import com.trifork.ibeacon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;

public class NotificationFragment extends BaseFragment {

    @InjectView(R.id.immediateText) EditText immediateText;
    @InjectView(R.id.immediateSwitch) Switch immediateSwitch;
    @InjectView(R.id.nearText) EditText nearText;
    @InjectView(R.id.nearSwitch) Switch nearSwitch;
    @InjectView(R.id.farText) EditText farText;
    @InjectView(R.id.farSwitch) Switch farSwitch;

    private List<Utils.Proximity> notifications = new ArrayList<>();
    private Utils.Proximity lastProximity = Utils.Proximity.UNKNOWN;

    public static NotificationFragment newInstance() {
        Bundle args = new Bundle();
        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        immediateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                toggleNotification(Utils.Proximity.IMMEDIATE, checked);
            }
        });

        nearSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                toggleNotification(Utils.Proximity.NEAR, checked);
            }
        });

        farSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                toggleNotification(Utils.Proximity.FAR, checked);
            }
        });
    }

    private void toggleNotification(Utils.Proximity proximity, boolean checked) {
        if (checked) {
            notifications.add(proximity);
        } else {
            notifications.remove(proximity);
            if (notifications.isEmpty()) {
                lastProximity = Utils.Proximity.UNKNOWN;
            }
        }
    }

    @Subscribe
    public void beaconFound(BeaconScanCompleteEvent event) {
        Utils.Proximity proximity = Utils.proximityFromDistance(event.getBeacon().getDistance());
        if (proximity != lastProximity) {
            if (notifications.contains(proximity)) {
                showNotification(proximity);
                lastProximity = proximity;
            }
        }
    }

    private void showNotification(Utils.Proximity proximity) {
        Notification notification = new Notification.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getNotificationTitle(proximity))
                .setContentText(getNotificationText(proximity))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(proximity.hashCode(), notification);
    }

    private CharSequence getNotificationText(Utils.Proximity proximity) {
        switch(proximity) {
            case IMMEDIATE: return immediateText.getText().toString();
            case NEAR: return nearText.getText().toString();
            case FAR: return farText.getText().toString();
            default: return "";
        }
    }

    private CharSequence getNotificationTitle(Utils.Proximity proximity) {
        switch (proximity) {
            case UNKNOWN: return "Beacon status: Unknown";
            case IMMEDIATE: return "Beacon status: Immediate";
            case NEAR: return "Beacon status: Near";
            case FAR: return "Beacon status: Far";
            default: return "";
        }
    }

}
