package com.trifork.ibeacon.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;
import com.trifork.ibeacon.database.Dao;
import com.trifork.ibeacon.database.RegionHistoryCursorLoader;
import com.trifork.ibeacon.database.RegionHistoryEntry;
import com.trifork.ibeacon.eventbus.RequestBeaconScanEvent;
import com.trifork.ibeacon.eventbus.RequestFullScanEvent;
import com.trifork.ibeacon.eventbus.StopScanEvent;

import javax.inject.Inject;
import java.text.SimpleDateFormat;

public class RegionLogFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject Dao dao;
    @InjectView(R.id.listview) ListView listView;

    private final int ACTION_CLEARLOG = 1;
    private final int LOADER_ID = 0x01;
    private RegionHistoryAdapter adapter;

    public static RegionLogFragment newInstance() {
        Bundle args = new Bundle();
        RegionLogFragment fragment = new RegionLogFragment();
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
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new RegionHistoryAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            startScan();
        }
    }

    private void startScan() {
        bus.post(new RequestBeaconScanEvent(persistentState.getSelectedRegion()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, ACTION_CLEARLOG, 0, R.string.action_clearlog);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == ACTION_CLEARLOG) {
            dao.execute(new Runnable() {
                @Override
                public void run() {
                    dao.clearLog(persistentState.getSelectedRegion());
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new RegionHistoryCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor c = adapter.swapCursor(data);
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (adapter != null) {
            Cursor c = adapter.swapCursor(null);
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    class RegionHistoryAdapter extends CursorAdapter {

        private final LayoutInflater inflater;
        private SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");

        public RegionHistoryAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = inflater.inflate(R.layout.row_regionhistory, parent, false);
            v.setTag(new ViewHolder(v));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            RegionHistoryEntry entry = dao.getEntryFromCursor(cursor);
            holder.name.setText(entry.getName());
            holder.enter.setText(getDateTimeString(entry.getEnter()));
            holder.exit.setText(getDateTimeString(entry.getExit()));
        }

        private String getDateTimeString(long timestamp) {
            if (timestamp > 0) {
                return formatter.format(timestamp);
            } else {
                return getResources().getString(R.string.empty_placeholder);
            }
        }

        class ViewHolder {
            @InjectView(R.id.name) TextView name;
            @InjectView(R.id.enter) TextView enter;
            @InjectView(R.id.exit) TextView exit;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }

        @Override
        protected void onContentChanged() {
            getLoaderManager().restartLoader(LOADER_ID, null, RegionLogFragment.this);
        }
    }
}
