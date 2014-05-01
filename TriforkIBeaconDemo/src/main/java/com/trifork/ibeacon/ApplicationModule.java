package com.trifork.ibeacon;

import android.content.Context;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.trifork.ibeacon.database.Dao;
import com.trifork.ibeacon.database.Database;
import com.trifork.ibeacon.database.RegionHistoryCursorLoader;
import com.trifork.ibeacon.detectors.EstimoteDetector;
import com.trifork.ibeacon.detectors.IBeaconDetector;
import com.trifork.ibeacon.detectors.RadiusNetworkDetector;
import com.trifork.ibeacon.ui.*;
import com.trifork.ibeacon.util.PersistentState;
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
            RadiusNetworkDetector.class,
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
        return new Bus(ThreadEnforcer.MAIN);
    }

    @Provides
    @Singleton
    Dao providesDao(Context context) {
        return new Dao(new Database(context), context);
    }

    @Provides
    IBeaconDetector providesIBeaconDetector() {
        return new RadiusNetworkDetector();
    }

    @Provides
    @Singleton
    PersistentState providesPersistentState(Context context) {
        return new PersistentState(context);
    }
}
