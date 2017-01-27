package com.idea.todo.frag.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.idea.todo.R;
import com.idea.todo.activity.alarm.SetAlarmActivity;
import com.idea.todo.constants.C;
import com.idea.todo.frag.dialog.MyDatePickerDialog;
import com.idea.todo.listener.OnDateSetComplete;
import com.idea.todo.listener.OnTimeSetComplete;
import com.idea.todo.wrapper.Log;
import com.idea.todo.wrapper.ToastMaster;
import com.idea.todo.wrapper.alarm.AlarmInfo;
import com.idea.todo.wrapper.alarm.AlarmPreference;
import com.idea.todo.wrapper.alarm.Alarms;

import java.util.Calendar;


/**
 * Created by sha on 24/01/17.
 */

public class SetAlarmFrag extends PreferenceFragment implements
        OnTimeSetComplete,
        OnDateSetComplete,
        C {

    private EditTextPreference mLabel;
    private Preference mTimePref;
    private AlarmPreference mAlarmPref;
    private CheckBoxPreference mVibratePref;
    private MenuItem mDeleteAlarmItem;
    private int mId;
    private boolean mEnabled;
    private long toDoAlarmTime;
    private Calendar calendar;

    /**
     * Set an alarm.  Requires an Alarms.ALARM_ID to be passed in as an
     * extra. FIXME: Pass an AlarmInfo object like every other Activity.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.alarm_prefs);
        initPrefsViews();
        getIntentData();
        updateTime();
    }

    private void initPrefsViews() {
        mLabel = (EditTextPreference) findPreference("label");
        mLabel.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference p, Object newValue) {
                        // Set the summary based on the new label.
                        p.setSummary((String) newValue);
                        return true;
                    }
                });
        mTimePref = findPreference("time");
        mAlarmPref = (AlarmPreference) findPreference("alarm");
        mVibratePref = (CheckBoxPreference) findPreference("vibrate");
    }

    private void getIntentData() {
        Intent i = base().getIntent();
        mId = i.getIntExtra(Alarms.ALARM_ID, -1);
        if (Log.LOGV) {
            Log.v("In SetAlarmFrag, alarmInfo id = " + mId);
        }

        /* load alarmInfo details from database */
        AlarmInfo alarmInfo = Alarms.getAlarm(base().getContentResolver(), mId);
        // Bad alarmInfo, bail to avoid a NPE.
        if (alarmInfo == null) {
            setResult(Activity.RESULT_CANCELED, 0);

            return;
        }
        mEnabled = alarmInfo.enabled;
        mLabel.setText(alarmInfo.label);
        mLabel.setSummary(alarmInfo.label);
        toDoAlarmTime = alarmInfo.time;

        if (toDoAlarmTime != 0){
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(toDoAlarmTime);
        }

        mVibratePref.setChecked(alarmInfo.vibrate);
        // Give the alert uri to the preference.
        mAlarmPref.setAlert(alarmInfo.alert);
    }

    private void setResult(int resultCode, long date) {
        Intent returnIntent = new Intent();
        switch (resultCode){
             case Activity.RESULT_OK :
                 returnIntent.putExtra(INTENT_KEY_TODO_DATE, date);
                break;
        }

        base().setResult(resultCode, returnIntent);
        base().finish();
    }

    private void updateTime() {
        if (calendar == null) return;
        if (Log.LOGV) {
            Log.v("updateTime " + mId);
        }
        mTimePref.setSummary(Alarms.formatTime(
                getActivity(),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.prefs_cutom_layout_alarm, container, false);
        // Attach actions to each button.
        Button b = (Button) v.findViewById(R.id.alarm_save);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Enable the alarm when clicking "Done"
                mEnabled = true;
                saveAlarmEnhanced();
            }
        });
        b = (Button) v.findViewById(R.id.alarm_cancel);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED, 0);
            }
        });
        return v;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTimePref) {
            showDialogDatePicker();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showDialogDatePicker() {
        MyDatePickerDialog.newInstance(toDoAlarmTime).show(base().getSupportFragmentManager(), "MyDatePickerDialog");
    }

    /**
     * SRC : MyTimePickerDialog
     * Call after the user set date and time
     * @param calendar
     */
    @Override
    public void onTimeSetComplete(Calendar calendar) {
        /**
         * set calendar to null to update to
         * remove any previous picking of date
         */
        this.calendar = null;
        if (isTimeValid(calendar)) {
            this.calendar = calendar;
            updateTime();
        }
    }

    private boolean isTimeValid(Calendar calendar) {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        if (calendar.getTime().before(now.getTime()) || calendar.getTimeInMillis() == now.getTimeInMillis()) {
            base().displayToast(R.string.invalid_time);
            return false;
        }
        return true;
    }

    @Override
    public void onDateSetComplete(Calendar calendar) {

    }

    private void saveAlarmEnhanced() {
        if (calendar == null){
            base().displayToast(R.string.must_pick_time);
            return;
        }

        final String alert = mAlarmPref.getAlertString();
         Alarms.setAlarmEnhanced(
                getActivity(),
                mId,
                mEnabled,
                mVibratePref.isChecked(),
                mLabel.getText(),
                alert,
                calendar);

        long time = calendar.getTimeInMillis();
        if (mEnabled) {
            popAlarmSetToast(getActivity(), time);
        }
        com.idea.todo.wrapper.Log.e("time = " + time);

        setResult(Activity.RESULT_OK , time);
    }

    private static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    /**
     * format "AlarmInfo set for 2 days 7 hours and 53 minutes from
     * now"
     */
    static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                        context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                        context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                        context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                (dispHour ? 2 : 0) |
                (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mDeleteAlarmItem = menu.add(0, 0, 0, R.string.delete_alarm);
        mDeleteAlarmItem.setIcon(android.R.drawable.ic_menu_delete);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mDeleteAlarmItem) {
            Alarms.deleteAlarm(getActivity(), mId);
            setResult(Activity.RESULT_CANCELED, 0);
            return true;
        }
        return false;
    }

    private SetAlarmActivity base() {
        return (SetAlarmActivity) getActivity();
    }

    /**
     * Called From base
     */
    public void onBackPressed() {
        saveAlarmEnhanced();
    }
}