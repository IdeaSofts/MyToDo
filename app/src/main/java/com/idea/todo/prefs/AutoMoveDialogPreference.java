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
import com.idea.todo.constants.C;

public class AutoMoveDialogPreference extends DialogPreference implements
        C {
    private int mAutoMove;

    public AutoMoveDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        builder.setView(new AutoMoveView(getContext()));
        super.onPrepareDialogBuilder(builder);
    }

    private class AutoMoveView extends LinearLayout {
        private SeekBar mAutoMoveSeekBar;
        private TextView mAutoMoveText;
        private Context mContext;

        public AutoMoveView(Context context) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.prefs_automove, this);

            mAutoMoveText = (TextView) findViewById(R.id.autoMoveText);
            mAutoMoveSeekBar = (SeekBar) findViewById(R.id.autoMoveSeekBar);
            mContext = context;
            mAutoMove = getPersistedInt(-1);
            mAutoMoveSeekBar.setProgress(mAutoMove + 1);
            updateText();
            mAutoMoveSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mAutoMove = progress - 1;
                    updateText();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        private void updateText() {
            switch (mAutoMove) {
                case AUTO_MOVE_DEFAULT :
                    mAutoMoveText.setText(mContext.getString(R.string.prefsAutoMoveDisabled));
                    break;

                case 0  :
                    mAutoMoveText.setText(mContext.getString(R.string.prefsAutoMoveToday));
                    break;

                case 1 :
                    mAutoMoveText.setText(mContext.getString(R.string.prefsAutoMoveSingle));
                    break;

                default:
                    mAutoMoveText.setText(mContext.getString(R.string.prefsAutoMovePlural,
                            String.valueOf(mAutoMove)));
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(mAutoMove);
        }
        super.onDialogClosed(positiveResult);
    }



}
