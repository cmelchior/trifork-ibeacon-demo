package com.trifork.ibeacon;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.squareup.otto.Subscribe;
import com.trifork.ibeacon.database.Dao;
import com.trifork.ibeacon.detectors.BeaconController;
import com.trifork.ibeacon.eventbus.RequestBeaconScanEvent;
import com.trifork.ibeacon.eventbus.RequestFullScanEvent;
import com.trifork.ibeacon.eventbus.StopFullScanEvent;
import com.trifork.ibeacon.eventbus.StopScanEvent;
import com.trifork.ibeacon.ui.*;

import javax.inject.Inject;
import java.util.Locale;


public class MainActivity extends BaseActivity implements ActionBar.TabListener {

    @Inject Dao dao;
    @Inject BeaconController controller;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dao.open();

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.post(new RequestFullScanEvent()); // Needed here because fragments in a pager adapter have horrible lifecycle callbacks
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dao.close();
    }

    @Subscribe
    public void fullScanRequested(RequestFullScanEvent event) {
        controller.connect(new BeaconController.ServiceReadyCallback() {
            @Override
            public void serviceReady() {
                controller.startFullScan();
            }
        });
    }

    @Subscribe
    public void beaconScanRequested(final RequestBeaconScanEvent event) {
        if (event.getRegion() == null) return;
        controller.connect(new BeaconController.ServiceReadyCallback() {
            @Override
            public void serviceReady() {
                controller.startRanging(event.getRegion());
                controller.startMonitoring(event.getRegion());
            }
        });
    }

    @Subscribe
    public void stopScanRequested(final StopScanEvent event) {
        controller.connect(new BeaconController.ServiceReadyCallback() {
            @Override
            public void serviceReady() {
                controller.stopRanging(event.getRegion());
                controller.stopMonitoring(event.getRegion());
            }
        });

    }

    @Subscribe
    public void stopFullScanRequested(final StopFullScanEvent event) {
        controller.connect(new BeaconController.ServiceReadyCallback() {
            @Override
            public void serviceReady() {
                controller.stopFullScan();
            }
        });
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return ScanFragment.newInstance();
                case 1: return BeaconDataFragment.newInstance();
                case 2: return RegionLogFragment.newInstance();
                case 3: return NotificationFragment.newInstance();
                case 4: return LocationFragment.newInstance();
                default: throw new RuntimeException("Not supported: " + position);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0: return getString(R.string.title_scan).toUpperCase(l);
                case 1: return getString(R.string.title_beacondata).toUpperCase(l);
                case 2: return getString(R.string.title_log).toUpperCase(l);
                case 3: return getString(R.string.title_notification).toUpperCase();
                case 4: return getString(R.string.title_location).toUpperCase();
            }
            return null;
        }
    }
}
