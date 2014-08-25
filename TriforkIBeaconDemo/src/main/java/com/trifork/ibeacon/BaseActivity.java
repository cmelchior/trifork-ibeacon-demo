package com.trifork.ibeacon;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class BaseActivity extends FragmentActivity {

    protected @Inject Bus bus;

    public BaseActivity() {
        super();
        BaseApplication.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }
}
