package com.idea.todo.prefs;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.idea.todo.R;

public class MinMaxDialogPreference extends DialogPreference {
    private int mMaxLines;
    private int mMinLines;

    public MinMaxDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        builder.setView(new MinMaxView(getContext()));
        super.onPrepareDialogBuilder(builder);
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt((mMaxLines * 10) + mMinLines);
        }
        super.onDialogClosed(positiveResult);
    }

    private class MinMaxView extends LinearLayout {
        private Context mContext;
        private SeekBar mMaxLinesSeekBar;
        private TextView mMaxLinesText;
        private SeekBar mMinLinesSeekBar;
        private TextView mMinLinesText;

        public MinMaxView(Context context) {
            super(context);
            ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.prefs_minmax, this);
            mMinLinesText = (TextView) findViewById(R.id.minLinesText);
            mMaxLinesText = (TextView) findViewById(R.id.maxLinesText);
            mMinLinesSeekBar = (SeekBar) findViewById(R.id.minLinesSeekBar);
            mMaxLinesSeekBar = (SeekBar) findViewById(R.id.maxLinesSeekBar);
            int val = getPersistedInt(31);
            mMinLines = val % 10;
            mMaxLines = val / 10;
            mMinLinesSeekBar.setProgress(mMinLines);
            mMaxLinesSeekBar.setProgress(mMaxLines);
            mContext = context;
            updateText();
            mMinLinesSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mMinLines = progress;
                    if (mMinLines < 1) {
                        mMinLines = 1;
                    }
                    if (mMinLines > mMaxLines) {
                        mMinLines = mMaxLines;
                    }
                    seekBar.setProgress(mMinLines);
                    updateText();
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            mMaxLinesSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mMaxLines = progress;
                    if (mMaxLines < 1) {
                        mMaxLines = 1;
                    }
                    if (mMaxLines < mMinLines) {
                        mMaxLines = mMinLines;
                    }
                    seekBar.setProgress(mMaxLines);
                    updateText();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        private void updateText() {
            if (mMinLines == 1) {
                mMinLinesText.setText(mContext.getString(R.string.prefsListItemMinLinesSingle,
                        String.valueOf(mMinLines)));
            } else {
                mMinLinesText.setText(mContext.getString(R.string.prefsListItemMinLinesPlural,
                        String.valueOf(mMinLines)));
            }
            if (mMaxLines == 1) {
                mMaxLinesText.setText(mContext.getString(R.string.prefsListItemMaxLinesSingle,
                        String.valueOf(mMaxLines)));
                return;
            }
            mMaxLinesText.setText(mContext.getString(R.string.prefsListItemMaxLinesPlural,
                    String.valueOf(mMaxLines)));
        }
    }


}
