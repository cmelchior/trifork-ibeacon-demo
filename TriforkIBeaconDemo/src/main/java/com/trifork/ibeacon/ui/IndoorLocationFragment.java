package com.trifork.ibeacon.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trifork.ibeacon.BaseFragment;
import com.trifork.ibeacon.R;

public class IndoorLocationFragment extends BaseFragment {

    public static IndoorLocationFragment newInstance() {
        Bundle args = new Bundle();
        IndoorLocationFragment fragment = new IndoorLocationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_indoorlocation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
