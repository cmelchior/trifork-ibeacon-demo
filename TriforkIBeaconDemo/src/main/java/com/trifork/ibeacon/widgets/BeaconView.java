package com.trifork.ibeacon.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BeaconView extends View {

    private View parentView;
    private int backgroundColor = Color.BLACK;
    private float pixelsPrMeter;
    private float xPositionMeters;
    private float yPositionMeters;
    private OnPositionChangedListener listener;
    private float distance;

    public BeaconView(Context context) {
        super(context);
        init();
    }

    public BeaconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BeaconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        parentView = (View) getParent();
        createBackgroundDrawable();
        configureOnTouchListener();
    }

    private void configureOnTouchListener() {
        setOnTouchListener(new OnTouchListener() {

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
                        int diffX = X - startX;
                        int diffY = Y - startY;
                        setRoomCoordinate(xPositionMeters + diffX/pixelsPrMeter, yPositionMeters + diffY/pixelsPrMeter);
                        startX = X;
                        startY = Y;
                        break;
                }

                return true;
            }
        });
    }

    private void createBackgroundDrawable() {
        ShapeDrawable background = new ShapeDrawable(new OvalShape());
        background.getPaint().setColor(backgroundColor);
        background.getPaint().setStyle(Paint.Style.FILL);
        setBackground(background);
    }

    public void updatePixelsPrMeter(float pixelsPrMeter) {
        this.pixelsPrMeter = pixelsPrMeter;
        requestLayout();
    }

    public void setRoomCoordinate(float xInMeters, float yInMeters) {
        this.xPositionMeters = xInMeters;
        this.yPositionMeters = yInMeters;
        notifyPositionChanged();
        invalidatePosition();
    }

    private void notifyPositionChanged() {
        if (listener != null) {
            listener.positionChanged(xPositionMeters, yPositionMeters);
        }
    }

    private void invalidatePosition() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.leftMargin = (int) (xPositionMeters * pixelsPrMeter);
        layoutParams.topMargin = (int) (yPositionMeters * pixelsPrMeter);
        setLayoutParams(layoutParams);
    }

    public void setBeaconColor(int color) {
        this.backgroundColor = color;
        createBackgroundDrawable();
        invalidate();
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.listener = listener;
    }

    public void updateDistance(float distance) {
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public interface OnPositionChangedListener {
        public void positionChanged(float xMeters, float yMeters);
    }
}
