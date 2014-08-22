package com.trifork.ibeacon.widgets;

import android.app.Fragment;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.trifork.ibeacon.R;

import org.altbeacon.beacon.BeaconManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LocationTrackerView extends RelativeLayout {

    @InjectView(R.id.room) View room;
    @InjectView(R.id.beacon1) View beacon1;
    @InjectView(R.id.beacon2) View beacon2;
    @InjectView(R.id.beacon3) View beacon3;

    private float pixelsPrMeter;
    private float heightM = 10;
    private float widthM = 15;

    private int _xDelta;
    public int _yDelta;

    public LocationTrackerView(Context context) {
        super(context);
        init();
    }

    public LocationTrackerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LocationTrackerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.view_locationtracker, this, true);
        ButterKnife.inject(this, v);

        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                calculatePixelsPrMeter();
                setRoomSize();
            }
        });

        beacon1.setOnTouchListener(new OnTouchListener() {

            int startX;
            int startY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        startX = X;
                        startY = Y;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:

                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin += X - startX;
                        layoutParams.topMargin += Y - startY;
                        startX = X;
                        startY = Y;
                        view.setLayoutParams(layoutParams);
                        break;
                }

                room.invalidate();
                return true;
            }
        });
    }

    private void setRoomSize() {
        int width = (int) (widthM * pixelsPrMeter);
        int height = (int) (heightM * pixelsPrMeter);
        room.getLayoutParams().width = width;
        room.getLayoutParams().height = height;
        invalidate();
    }

    private void calculatePixelsPrMeter() {
        float width = getWidth();
        float height = getHeight();

        if (height < width) {
            pixelsPrMeter = height/heightM;
        } else {
            pixelsPrMeter = width/widthM;
        }
        calculateDistance(0,0);
    }

    protected static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        double distance;
        if (ratio < 1.0) {
            distance =  Math.pow(ratio,10);
        }
        else {
            distance =  (0.42093)*Math.pow(ratio,6.9476) + 0.54992;
        }
        return distance;
    }



}
