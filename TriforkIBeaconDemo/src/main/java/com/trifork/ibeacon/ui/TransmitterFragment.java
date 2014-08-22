package com.trifork.ibeacon.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.RequestBeaconTransmit;
import com.trifork.ibeacon.eventbus.StopTransmitEvent;

import org.altbeacon.beacon.Beacon;

import java.util.UUID;

import butterknife.InjectView;
import butterknife.Optional;

/**
 * Transmitting only works on Nexus 5 with Android L preview.
 */
public class TransmitterFragment extends BaseFragment {

    @Optional @InjectView(R.id.uuidEditText) EditText uuidView;
    @Optional @InjectView(R.id.majorEditText) EditText majorView;
    @Optional @InjectView(R.id.minorEditText) EditText minorView;
    @Optional @InjectView(R.id.powerEditText) EditText powerView;
    @Optional @InjectView(R.id.transmitterPowerSpinner) Spinner transmitterPowerSpinner;
    @Optional @InjectView(R.id.frequencySpinner) Spinner frequencySpinner;
    @Optional @InjectView(R.id.enableSwitch) Switch enableSwitch;
    @Optional @InjectView(R.id.generateButton) Button generateUuidButton;

    private Beacon beacon;
    private int advertiseMode;
    private int txPowerLevel;

    public static TransmitterFragment newInstance() {
        Bundle args = new Bundle();
        TransmitterFragment fragment = new TransmitterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < 20 || !Build.MODEL.equalsIgnoreCase("Nexus 5")) {
            return inflater.inflate(R.layout.fragment_transmitter_sdkfailure, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_transmitter, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (uuidView == null) return; // Check for sdk failure notice

        // Setup listeners and spinners
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startTransmitting();
                } else {
                    stopTransmitting();
                }
            }
        });

        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.frequency_array, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);
        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch(position) {
//                    case 0: advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER; break;
//                    case 1: advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED; break;
//                    case 2: advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY; break;
                }
                stopTransmitting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        ArrayAdapter<CharSequence> transmitPowerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.power_array, android.R.layout.simple_spinner_item);
        transmitPowerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transmitterPowerSpinner.setAdapter(transmitPowerAdapter);
        transmitterPowerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch(position) {
//                    case 0: txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH; break;
//                    case 1: txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM; break;
//                    case 2: txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_LOW; break;
//                    case 3: txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW; break;
                }
                stopTransmitting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        generateUuidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uuidView.setText(UUID.randomUUID().toString().substring(0,36).toUpperCase());
                stopTransmitting();
            }
        });

        TextWatcher stopTransmitterOnChangeTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override public void afterTextChanged(Editable s) {
                stopTransmitting();
            }
        };

        majorView.addTextChangedListener(stopTransmitterOnChangeTextWatcher);
        minorView.addTextChangedListener(stopTransmitterOnChangeTextWatcher);
        powerView.addTextChangedListener(stopTransmitterOnChangeTextWatcher);
    }

    private void stopTransmitting() {
        bus.post(new StopTransmitEvent(beacon));
        enableSwitch.setChecked(false);
        beacon = null;
    }

    private void startTransmitting() {
        beacon = new Beacon.Builder()
                .setId1(uuidView.getText().toString())
                .setId2(majorView.getText().toString())
                .setId3(minorView.getText().toString())
                .setTxPower(Integer.parseInt(powerView.getText().toString()))
                .build();

        bus.post(new RequestBeaconTransmit(beacon, advertiseMode, txPowerLevel));
    }
}
