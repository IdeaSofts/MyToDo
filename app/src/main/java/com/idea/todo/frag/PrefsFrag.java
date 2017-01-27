package com.idea.todo.frag;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

import com.idea.todo.R;
import com.idea.todo.constants.C;
import com.idea.todo.db.Database;
import com.idea.todo.prefs.AutoMoveDialogPreference;
import com.idea.todo.prefs.ColorDialogPreference;
import com.idea.todo.prefs.MinMaxDialogPreference;
import com.idea.todo.wrapper.file.FileProcessor;

/**
 * Created by sha on 24/01/17.
 */

public class PrefsFrag extends PreferenceFragment implements
        C,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private AutoMoveDialogPreference mAutoMove;
    private ColorDialogPreference mDateCurrentColor;
    private ColorDialogPreference mDateFutureColor;
    private ColorDialogPreference mDatePastColor;
    private MinMaxDialogPreference mListItemSize;
    private CheckBoxPreference mListSortOrder;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        mListSortOrder = (CheckBoxPreference) findPreference(PREFS_LIST_SORT_ORDER);
        mListItemSize = (MinMaxDialogPreference) findPreference(PREFS_LIST_ITEM_SIZE);
        mDateCurrentColor = (ColorDialogPreference) findPreference(DATE_CURRENT_COLOR);
        mDateFutureColor = (ColorDialogPreference) findPreference(PREFS_DATE_FUTURE_COLOR);
        mDatePastColor = (ColorDialogPreference) findPreference(DATE_PAST_COLOR);
        mAutoMove = (AutoMoveDialogPreference) findPreference(AUTO_MOVE);
        mDateCurrentColor.setDefaultColor(DATE_CURRENT_COLOR_DEFAULT);
        mDateFutureColor.setDefaultColor(DATE_FUTURE_COLOR_DEFAULT);
        mDatePastColor.setDefaultColor(DATE_PAST_COLOR_DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListSortOrder();
        updateListItemSize();
        updateAutoMove();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(PREFS_LIST_SORT_ORDER)) {
            updateListSortOrder();
        }
        else if (key.equals(PREFS_LIST_ITEM_SIZE)) {
            updateListItemSize();
        }
        else if (key.equals(DATE_CURRENT_COLOR)) {
            mDateCurrentColor.updateSummaryColor();
        }
        else if (key.equals(PREFS_DATE_FUTURE_COLOR)) {
            mDateFutureColor.updateSummaryColor();
        }
        else if (key.equals(DATE_PAST_COLOR)) {
            mDatePastColor.updateSummaryColor();
        }
        else if (key.equals(AUTO_MOVE)) {
            updateAutoMove();
        }
    }

    private void updateListSortOrder() {
        if (mSharedPreferences.getBoolean(PREFS_LIST_SORT_ORDER, LIST_SORT_ORDER_DEFAULT)) {
            mListSortOrder.setSummary(R.string.prefsListSortOrderReminderDate);
        } else {
            mListSortOrder.setSummary(R.string.prefsListSortOrderCreatedDate);
        }
    }

    private void updateListItemSize() {
        int val = mSharedPreferences.getInt(PREFS_LIST_ITEM_SIZE, LIST_ITEM_SIZE_DEFAULT);
        int min = val % 10;
        int max = val / 10;
        if (min == 1 && max == 1) {
            mListItemSize.setSummary(getString(R.string.prefsListItemSizeSummarySingle));
        } else if (min == max) {
            mListItemSize.setSummary(getString(R.string.prefsListItemSizeSummaryPlural, new Object[]{Integer.valueOf(min)}));
        } else {
            mListItemSize.setSummary(getString(R.string.prefsListItemSizeSummaryMultiple, new Object[]{Integer.valueOf(min), Integer.valueOf(max)}));
        }
    }

    private void updateAutoMove() {
        switch (mSharedPreferences.getInt(AUTO_MOVE, AUTO_MOVE_DEFAULT)) {
            case AUTO_MOVE_DEFAULT /*-1*/:
                mAutoMove.setSummary(getString(R.string.prefsAutoMoveDisabled));
            case FileProcessor.MSG_PROCESS_STARTED /*0*/:
                mAutoMove.setSummary(getString(R.string.prefsAutoMoveToday));
            case Database.DB_VERSION /*1*/:
                mAutoMove.setSummary(getString(R.string.prefsAutoMoveSingle));
            default:
                /**
                 * Commented by me
                 */
//                mAutoMove.setSummary(getString(R.string.prefsAutoMovePlural, new Object[]{Integer.valueOf(val)}));
        }
    }
}