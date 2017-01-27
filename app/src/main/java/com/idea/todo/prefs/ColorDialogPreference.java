package com.idea.todo.prefs;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.idea.todo.constants.C;

public class ColorDialogPreference extends DialogPreference implements C{
    private int mDefaultColor;
    private int mSelectedColor;
    private TextView mSummaryText;

    private class ColorPickerView extends View {
        private float centerX;
        private float centerY;
        private Paint mCenterPaint;
        private Rect mClipBounds;
        private final int[] mColors;
        private RectF mDrawRect;
        private Paint mOuterPaint;

        ColorPickerView(Context context, OnColorChangedListener listener) {
            super(context);
            mColors = new int[]{
                    -65536,
                    -65281,
                    -16776961,
                    -1,
                    -16777216,
                    -16711681,
                    -16711936,
                    DATE_CURRENT_COLOR_DEFAULT,
                    -65536};

            mOuterPaint = new Paint(1);
            mOuterPaint.setShader(new SweepGradient(0.0f, 0.0f, mColors, null));
            mOuterPaint.setStyle(Style.STROKE);
            mCenterPaint = new Paint(1);
            mCenterPaint.setColor(getPersistedInt(mDefaultColor));
            mClipBounds = new Rect();
            mDrawRect = new RectF();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.getClipBounds(mClipBounds);
            float height = (float) (mClipBounds.bottom - mClipBounds.top);
            centerX = ((float) mClipBounds.left) + (((float) (mClipBounds.right - mClipBounds.left)) / 2.0f);
            centerY = ((float) mClipBounds.top) + (height / 2.0f);
            float radius = centerX < centerY ? centerX : centerY;
            mOuterPaint.setStrokeWidth(radius / 3.0f);
            mCenterPaint.setStrokeWidth(radius / 3.0f);
            radius -= mOuterPaint.getStrokeWidth();
            mDrawRect.set(-radius, -radius, radius, radius);
            canvas.translate(centerX, centerY);
            canvas.drawOval(mDrawRect, mOuterPaint);
            canvas.drawCircle(0.0f, 0.0f, radius / 3.0f, mCenterPaint);
        }

        private int ave(int s, int d, float p) {
            return Math.round(((float) (d - s)) * p) + s;
        }

        private int interpColor(int[] colors, float unit) {
            if (unit <= 0.0f) {
                return colors[0];
            }
            if (unit >= 1.0f) {
                return colors[colors.length - 1];
            }
            float p = unit * ((float) (colors.length - 1));
            int i = (int) p;
            p -= (float) i;
            int c0 = colors[i];
            int c1 = colors[i + 1];
            return Color.argb(ave(Color.alpha(c0), Color.alpha(c1), p), ave(Color.red(c0), Color.red(c1), p), ave(Color.green(c0), Color.green(c1), p), ave(Color.blue(c0), Color.blue(c1), p));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - centerX;
            float y = event.getY() - centerY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :
                case MotionEvent.ACTION_MOVE :
                    float unit = (float) (((double) ((float) Math.atan2((double) y, (double) x))) / 6.283185307179586d);
                    if (unit < 0.0f) {
                        unit += 1.0f;
                    }
                    mCenterPaint.setColor(interpColor(mColors, unit));
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP :
                    mSelectedColor = mCenterPaint.getColor();
                    break;
            }
            return true;
        }
    }

    public interface OnColorChangedListener {
        void colorChanged(int i);
    }

    public ColorDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        OnColorChangedListener mColorChangedListener = new OnColorChangedListener() {
            @Override
            public void colorChanged(int c) {
                mSelectedColor = c;
            }
        };
        builder.setView(new ColorPickerView(getContext(), mColorChangedListener));
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mSelectedColor != 0) {
            persistInt(mSelectedColor);
        }
        super.onDialogClosed(positiveResult);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
//        mSummaryText = (TextView) view.findViewById();
        updateSummaryColor();
    }

    public void setDefaultColor(int color) {
        mDefaultColor = color;
    }

    public void updateSummaryColor() {
        if (mSummaryText != null) {
            mSummaryText.setTextColor(getPersistedInt(mDefaultColor));
        }
    }
}
