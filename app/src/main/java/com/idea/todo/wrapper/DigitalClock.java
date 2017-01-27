package com.idea.todo.wrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.wrapper.alarm.Alarms;

import java.util.Calendar;

/**
 * Displays the time
 */
public class DigitalClock extends LinearLayout {

    private final static String M12 = "h:mm";

    private Calendar mCalendar;
    private String mFormat;
    private TextView mTimeDisplay;
    private AmPm mAmPm;
    private boolean mAnimate;
    private ContentObserver mFormatChangeObserver;
    private boolean mLive = true;
    private boolean mAttached;
    private Context mContext;
    /* called by system on minute ticks */


    public DigitalClock(Context context) {
        this(context, null);
        mContext = context;
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public DigitalClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mLive && intent.getAction().equals(
                            Intent.ACTION_TIMEZONE_CHANGED)) {
                    mCalendar = Calendar.getInstance();
                }
                updateTime();
            }
        };

    static class AmPm {
        private int mColorOn, mColorOff;

        private LinearLayout mAmPmLayout;
        private TextView mAm, mPm;

        AmPm(View parent) {
            mAmPmLayout = (LinearLayout) parent.findViewById(R.id.am_pm);
            mAm = (TextView)mAmPmLayout.findViewById(R.id.am);
            mPm = (TextView)mAmPmLayout.findViewById(R.id.pm);

            Resources r = parent.getResources();
            mColorOn = r.getColor(R.color.ampm_on);
            mColorOff = r.getColor(R.color.ampm_off);
        }

        void setShowAmPm(boolean show) {
            mAmPmLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        void setIsMorning(boolean isMorning) {
            mAm.setTextColor(isMorning ? mColorOn : mColorOff);
            mPm.setTextColor(isMorning ? mColorOff : mColorOn);
        }
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            setDateFormat();
            updateTime();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
        mAmPm = new AmPm(this);
        mCalendar = Calendar.getInstance();

        setDateFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (Log.LOGV) Log.v("onAttachedToWindow " + this);

        if (mAttached) return;
        mAttached = true;

        if (mAnimate) {
            setBackgroundResource(R.drawable.animate_circle);
            /* Start the animation (looped playback by default). */
            ((AnimationDrawable) getBackground()).start();
        }

        if (mLive) {
            /* monitor time ticks, time changed, timezone */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
        }

        /* monitor 12/24-hour display preference */
        mFormatChangeObserver = new FormatChangeObserver();
        mContext.getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached) return;
        mAttached = false;

        Drawable background = getBackground();
        if (background instanceof AnimationDrawable) {
            ((AnimationDrawable) background).stop();
        }

        if (mLive) {
            mContext.unregisterReceiver(mIntentReceiver);
        }
        mContext.getContentResolver().unregisterContentObserver(
                mFormatChangeObserver);
    }


    void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    private void updateTime() {
        if (mLive) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
        }

        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
        mTimeDisplay.setText(newTime);
        int amPm = mCalendar.get(Calendar.AM_PM);
        mAmPm.setIsMorning(amPm == Calendar.AM);

    }

    private void setDateFormat() {
        mFormat = Alarms.get24HourMode(mContext) ? Alarms.M24 : M12;
        mAmPm.setShowAmPm(mFormat.equals(M12));
    }

    public void setAnimate() {
        mAnimate = true;
    }

    void setLive(boolean live) {
        mLive = live;
    }
}
