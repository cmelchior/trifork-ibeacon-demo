package com.trifork.ibeacon.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.trifork.ibeacon.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LocationTrackerView extends RelativeLayout {

    private int BEACON_DISTANCE_ALPHA = 50;

    @InjectView(R.id.room) ViewGroup room;
    @InjectView(R.id.beacon1) BeaconView beacon1;
    @InjectView(R.id.beacon2) BeaconView beacon2;
    @InjectView(R.id.beacon3) BeaconView beacon3;

    private float maxWidthPixels;
    private float maxHeightPixels;
    private float pixelsPrMeter;
    private float heightMeters = 10;
    private float widthMeters = 15;

    private Paint beacon1Paint = new Paint();
    private Paint beacon2Paint = new Paint();
    private Paint beacon3Paint = new Paint();

    private int RED = Color.parseColor("#e00032");
    private int GREEN = Color.parseColor("#12c700");
    private int BLUE = Color.parseColor("#4d69ff");

    private OnBeaconMovedListener listener;

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

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                invalidateRoomSize();
            }
        });

        beacon1Paint.setStyle(Paint.Style.FILL);
        beacon1Paint.setAntiAlias(true);
        beacon1Paint.setColor(RED);
        beacon1Paint.setAlpha(BEACON_DISTANCE_ALPHA);

        beacon2Paint.setStyle(Paint.Style.FILL);
        beacon2Paint.setAntiAlias(true);
        beacon2Paint.setColor(GREEN);
        beacon2Paint.setAlpha(BEACON_DISTANCE_ALPHA);

        beacon3Paint.setStyle(Paint.Style.FILL);
        beacon3Paint.setAntiAlias(true);
        beacon3Paint.setColor(BLUE);
        beacon3Paint.setAlpha(BEACON_DISTANCE_ALPHA);

        beacon1.setBeaconColor(RED);
        beacon1.setRoomCoordinate(1, 1);
        beacon1.setOnPositionChangedListener(new BeaconView.OnPositionChangedListener() {
            @Override
            public void positionChanged(float xMeters, float yMeters) {
                notifyBeaconMoved(1, xMeters, yMeters);
            }
        });
        beacon2.setBeaconColor(GREEN);
        beacon2.setRoomCoordinate(2, 2);
        beacon2.setOnPositionChangedListener(new BeaconView.OnPositionChangedListener() {
            @Override
            public void positionChanged(float xMeters, float yMeters) {
                notifyBeaconMoved(2, xMeters, yMeters);
            }
        });
        beacon3.setBeaconColor(BLUE);
        beacon3.setRoomCoordinate(3,3);
        beacon3.setOnPositionChangedListener(new BeaconView.OnPositionChangedListener() {
            @Override
            public void positionChanged(float xMeters, float yMeters) {
                notifyBeaconMoved(3, xMeters, yMeters);
            }
        });

        setMaxRoomSizeInPixels();
    }

    private void notifyBeaconMoved(int beaconNo, float xMeters, float yMeters) {
        if (listener != null) {
            listener.onBeaconMoved(beaconNo, xMeters, yMeters);
        }
    }

    private void setMaxRoomSizeInPixels() {
        maxHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        maxWidthPixels = size.x;
    }

    public void setRoomSize(float widthMeters, float heightMeters) {
        this.widthMeters = widthMeters;
        this.heightMeters = heightMeters;
        invalidateRoomSize();
    }

    public void setRoomWidth(float widthMeters) {
        this.widthMeters = widthMeters;
        invalidateRoomSize();
    }

    public void setRoomHeight(float heightMeters) {
        this.heightMeters = heightMeters;
        invalidateRoomSize();
    }

    public float getRoomWidth() {
        return widthMeters;
    }

    public float getRoomHeight() {
        return heightMeters;
    }

    private void invalidateRoomSize() {
        calculatePixelsPrMeter();

        // Reset beacon positions
        beacon1.setRoomCoordinate(0, 0);
        beacon2.setRoomCoordinate(0, 0);
        beacon3.setRoomCoordinate(0, 0);

        // Change room
        int width = (int) (widthMeters * pixelsPrMeter);
        int height = (int) (heightMeters * pixelsPrMeter);
        room.getLayoutParams().width = width;
        room.getLayoutParams().height = height;
        invalidate();
    }

    private void calculatePixelsPrMeter() {
        float screenRatio = maxWidthPixels/maxHeightPixels;
        float newRatio = widthMeters/heightMeters;

        if (newRatio < screenRatio) {
            pixelsPrMeter = maxHeightPixels/heightMeters;
        } else {
            pixelsPrMeter = maxWidthPixels/widthMeters;
        }

        beacon1.updatePixelsPrMeter(pixelsPrMeter);
        beacon2.updatePixelsPrMeter(pixelsPrMeter);
        beacon3.updatePixelsPrMeter(pixelsPrMeter);
    }

    public void updateBeaconDistance(int beaconNo, float distance) {
        ((BeaconView) ((ViewGroup)room.getChildAt(0)).getChildAt(beaconNo)).updateDistance(distance);
        invalidate();
    }

    public void setOnBeaconMovedListener(OnBeaconMovedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x1 = beacon1.getX() + beacon1.getWidth() / 2;
        float y1 = beacon1.getY() + beacon1.getHeight() / 2;
        float d1 = beacon1.getDistance() * pixelsPrMeter;

        float x2 = beacon2.getX() + beacon2.getWidth() / 2;
        float y2 = beacon2.getY() + beacon2.getHeight() / 2;
        float d2 = beacon2.getDistance() * pixelsPrMeter;

        float x3 = beacon3.getX() + beacon3.getWidth() / 2;
        float y3 = beacon3.getY() + beacon3.getHeight() / 2;
        float d3 = beacon3.getDistance() * pixelsPrMeter;

        canvas.drawCircle(x1, y1, d1, beacon1Paint);
        canvas.drawCircle(x2, y2, d2, beacon2Paint);
        canvas.drawCircle(x3, y3, d3, beacon3Paint);
    }

    public interface OnBeaconMovedListener {
        public void onBeaconMoved(int id, float xMeters, float yMeters);
    }
}
