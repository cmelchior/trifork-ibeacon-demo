package com.trifork.estimote;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import butterknife.ButterKnife;
import com.squareup.otto.Bus;
import com.trifork.estimote.util.PersistentState;

import javax.inject.Inject;

public class BaseFragment extends Fragment {

    protected @Inject Bus bus;
    protected @Inject PersistentState persistentState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.inject(this);
        bus.register(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        bus.unregister(this);
    }
}
