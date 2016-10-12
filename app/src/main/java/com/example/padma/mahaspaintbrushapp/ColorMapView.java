package com.example.padma.mahaspaintbrushapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.Math;

/**
 * A view that displays the available color map which users can use
 * to select a color of their choice for drawing.
 */

public class ColorMapView extends View {
    /**
     * A paint object to draw the color wheel. The color wheel
     * provides users a range of color to choose from.
     */
    private Paint mPaint;

    /**
     * A paint object to draw the chosen color as a circle.
     * This is used to indicate the chosen color.
     */
    private Paint mCenterPaint;

    /**
     * An array for holding the colors. Used in drawing circle.
     */
    private int[] mColors;

    private boolean mTrackingCenter;
    private boolean mHighlightCenter;

    private static final int CIRCLE_CENTER_X = 100;
    private static final int CENTER_Y = 100;
    private static final int CIRCLE_RADIUS = 32;

    /**
     * A listener interface that lets the implementor of the interface
     * to be notified when users choose a color.
     */
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener mListener;

    public ColorMapView(Context context) {
        super(context);
        init(context, Color.BLUE);
    }

    public ColorMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, Color.BLUE);
    }

    public ColorMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, Color.BLUE);
    }

    private void init(Context c, int color) {
        // RGB and CMY color code
        mColors = new int[] { 0xFFFF0000, /* Red */
                0xFFFF00FF, /* Magenta */
                0xFF0000FF, /* Blue */
                0xFF00FFFF, /* Cyan */
                0xFF00FF00, /* Green */
                0xFFFFFF00, /* Yellow */
                0xFFFF0000  /* Red */
        };
        Shader s = new SweepGradient(0, 0, mColors, null);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(32);

        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(color);
        mCenterPaint.setStrokeWidth(5);
        if (c instanceof OnColorChangedListener) {
            mListener = (OnColorChangedListener)c;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float r = CIRCLE_CENTER_X - mPaint.getStrokeWidth()*0.5f;

        canvas.translate(CIRCLE_CENTER_X, CIRCLE_CENTER_X);

        canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
        canvas.drawCircle(0, 0, CIRCLE_RADIUS, mCenterPaint);

        if (mTrackingCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);
            mCenterPaint.setAlpha(mHighlightCenter ? 0xFF : 0x80);
            canvas.drawCircle(0, 0, CIRCLE_RADIUS + mCenterPaint.getStrokeWidth(), mCenterPaint);
            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(CIRCLE_CENTER_X*2, CENTER_Y*2);
    }

    private int linearInterpolation(int v0, int v1, float t) {
        return v0 + java.lang.Math.round(t * (v1 - v0));
    }

    /**
     * Interpolates color table to identify a color corresponding to the given parameter.
     * The input parameter is in the range [0 .. 1) and is used as an index into the color array
     * to identify two colors which then via interpolation is used to identify the color at a
     * given location.
     *
     * @param unit
     * @return color value at a given location
     */

    private int interpolateColor(float unit) {
        if ( unit > 0 && unit < 1 ) {
            float interpolationParameter = unit * (mColors.length - 1);
            int index = (int) interpolationParameter;
            interpolationParameter -= index;

            int c0 = mColors[index];
            int c1 = mColors[index + 1];
            int alpha = linearInterpolation(Color.alpha(c0), Color.alpha(c1), interpolationParameter);
            int red = linearInterpolation(Color.red(c0), Color.red(c1), interpolationParameter);
            int green = linearInterpolation(Color.green(c0), Color.green(c1), interpolationParameter);
            int blue = linearInterpolation(Color.blue(c0), Color.blue(c1), interpolationParameter);

            return Color.argb(alpha, red, green, blue);
        } else {
            return mColors[unit <= 0 ? 0 : mColors.length-1];
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CIRCLE_CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CIRCLE_RADIUS;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                /**
                 * If the touch event is inside of the inner circle that displays the
                 * chosen color, don't change the color selection.
                 */
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    /**
                     * The touch event happens outside of the inner circle. So, change
                     * the color selection based on users location.
                     */
                    float theta = (float)java.lang.Math.atan2(y, x);
                    // Normalize theta into range: [0....1]
                    float unit = theta/(2*(float)Math.PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    mCenterPaint.setColor(interpolateColor(unit));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    mListener.colorChanged(mCenterPaint.getColor());
                    mTrackingCenter = false;
                    invalidate();
                }
                break;
        }
        return true;
    }
}