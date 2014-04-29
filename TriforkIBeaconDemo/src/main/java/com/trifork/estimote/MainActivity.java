package com.trifork.estimote;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.squareup.otto.Subscribe;
import com.trifork.estimote.database.Dao;
import com.trifork.estimote.detectors.IBeaconDetector;
import com.trifork.estimote.eventbus.RequestBeaconScanEvent;
import com.trifork.estimote.eventbus.RequestFullScanEvent;
import com.trifork.estimote.ui.*;

import javax.inject.Inject;
import java.util.Locale;


public class MainActivity extends BaseActivity implements ActionBar.TabListener {

    @Inject Dao dao;
    @Inject IBeaconDetector detector;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private boolean isFullScanning = false;
    private boolean isBeaconScanning = false;

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
    public void onStop() {
        super.onStop();
        detector.stopRanging();
        detector.stopFullScan();
        isFullScanning = false;
        isBeaconScanning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dao.close();
    }

    @Subscribe
    public void fullScanRequested(RequestFullScanEvent event) {
        if (isFullScanning) return;
        detector.connect(new IBeaconDetector.ServiceReadyCallback() {
            @Override
            public void serviceReady() {
                detector.stopMonitoring();
                detector.stopRanging();
                detector.startFullScan();
                isFullScanning = true;
                isBeaconScanning = false;
            }
        });
    }

    @Subscribe
    public void beaconScanRequested(RequestBeaconScanEvent event) {
        if (isBeaconScanning) return;
        detector.connect(new IBeaconDetector.ServiceReadyCallback() {
            @Override
            public void serviceReady() {
                detector.stopFullScan();
                detector.startRanging();
                detector.startMonitoring();
                isFullScanning = false;
                isBeaconScanning = true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                default: throw new RuntimeException("Not supported: " + position);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0: return getString(R.string.title_scan).toUpperCase(l);
                case 1: return getString(R.string.title_beacondata).toUpperCase(l);
                case 2: return getString(R.string.title_log).toUpperCase(l);
            }
            return null;
        }
    }
}
