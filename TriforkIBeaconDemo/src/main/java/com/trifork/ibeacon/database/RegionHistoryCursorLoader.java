package com.trifork.ibeacon.database;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import com.trifork.ibeacon.BaseApplication;
import com.trifork.ibeacon.util.PersistentState;

import javax.inject.Inject;

public class RegionHistoryCursorLoader extends CursorLoader {

    @Inject Dao dao;
    @Inject PersistentState persistentState;

    public RegionHistoryCursorLoader(Context context) {
        super(context);
        BaseApplication.inject(this);
    }

    @Override
    public Cursor loadInBackground() {
        return dao.getHistory(persistentState.getSelectedRegion());
    }
}
