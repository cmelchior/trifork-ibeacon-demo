package com.trifork.ibeacon.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.eventbus.FullScanCompleteEvent;
import com.trifork.ibeacon.eventbus.NewBeaconSelectedEvent;
import com.trifork.ibeacon.eventbus.RequestFullScanEvent;
import com.trifork.ibeacon.util.PersistentState;
import com.trifork.ibeacon.util.Utils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import javax.inject.Inject;

public class ScanFragment extends BaseFragment {

    @Inject PersistentState persistentState;
    @InjectView(R.id.listview) ListView listView;

    private ArrayAdapter<Beacon> adapter;
    private Region selectedBeacon;

    public static Fragment newInstance() {
        Bundle args = new Bundle();
        ScanFragment fragment = new ScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new BeaconAdapter(getActivity());

        TextView emptyView = new TextView(getActivity());
        emptyView.setText("Empty");

        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Region oldBeacon = selectedBeacon;
                persistentState.setSelectedRegion(adapter.getItem(position));
                selectedBeacon = persistentState.getSelectedRegion();
                adapter.notifyDataSetChanged();
                if (oldBeacon == null || !oldBeacon.equals(selectedBeacon)) {
                    bus.post(new NewBeaconSelectedEvent());
                }
            }
        });
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
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.add("Loading");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setActionView(R.layout.actionbar_progress);
        item.expandActionView();
    }

    private void startScan() {
        selectedBeacon = persistentState.getSelectedRegion();
        bus.post(new RequestFullScanEvent());
    }

    @Subscribe
    public void scanComplete(FullScanCompleteEvent event) {
        adapter.clear();
        adapter.addAll(event.getBeacons());
    }

    class BeaconAdapter extends ArrayAdapter<Beacon> {

        LayoutInflater inflater;

        public BeaconAdapter(Context context) {
            super(context, R.layout.row_beaconentry);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_beaconentry, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            Beacon entry = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (Utils.isSameBeacon(entry, selectedBeacon)) {
                holder.beaconIcon.setImageResource(R.drawable.beacon_gray_checked);
            } else {
                holder.beaconIcon.setImageResource(R.drawable.selector_beacon_icon);
            }
            holder.mac.setText(entry.getBluetoothAddress());
            holder.uuid.setText(entry.getId1().toString());
            holder.major.setText(String.format(getString(R.string.label_major), entry.getId2().toString()));
            holder.minor.setText(String.format(getString(R.string.label_minor), entry.getId3().toString()));
            holder.txPower.setText(String.format(getString(R.string.label_txpower), entry.getTxPower()));
            holder.rssi.setText(String.format(getString(R.string.label_rssi), entry.getRssi()));
            return convertView;
        }

        class ViewHolder {
            @InjectView(R.id.beacon_icon) ImageView beaconIcon;
            @InjectView(R.id.mac) TextView mac;
            @InjectView(R.id.uuid) TextView uuid;
            @InjectView(R.id.major) TextView major;
            @InjectView(R.id.minor) TextView minor;
            @InjectView(R.id.txpower) TextView txPower;
            @InjectView(R.id.rssi) TextView rssi;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
