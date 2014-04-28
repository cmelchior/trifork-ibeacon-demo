package com.trifork.estimote;

import android.content.Context;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.trifork.estimote.database.Dao;
import com.trifork.estimote.database.Database;
import com.trifork.estimote.database.RegionHistoryCursorLoader;
import com.trifork.estimote.detectors.EstimoteDetector;
import com.trifork.estimote.detectors.IBeaconDetector;
import com.trifork.estimote.ui.*;
import com.trifork.estimote.util.PersistentState;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

//This annotation must list all activities that wish to inject
@Module(injects = {
            BaseActivity.class,
            MainActivity.class,
            AboutActivity.class,
            BaseFragment.class,
            ScanFragment.class,
            BeaconDataFragment.class,
            RangingFragment.class,
            RegionLogFragment.class,
            EstimoteDetector.class,
            RegionHistoryCursorLoader.class
        },
        complete = true,
        library = false)
public class ApplicationModule {

    private final Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus(ThreadEnforcer.ANY);
    }

    @Provides
    @Singleton
    Dao providesDao(Context context) {
        return new Dao(new Database(context), context);
    }

    @Provides
    IBeaconDetector providesIBeaconDetector() {
        return new EstimoteDetector();
    }

    @Provides
    @Singleton
    PersistentState providesPersistentState(Context context) {
        return new PersistentState(context);
    }
}
