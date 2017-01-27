package com.idea.todo.frag.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.idea.todo.activity.alarm.SetAlarmActivity;
import com.idea.todo.constants.C;
import com.idea.todo.frag.alarm.SetAlarmFrag;

import java.util.Calendar;

public class MyTimePickerDialog extends DialogFragment
        implements android.app.TimePickerDialog.OnTimeSetListener,
        C{
    private Calendar calendar;

    public static MyTimePickerDialog newInstance(Calendar calendar) {
        MyTimePickerDialog f = new MyTimePickerDialog();
        f.setCalendar(calendar);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return new android.app.TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        onTimeSetComplete();

    }

    private void onTimeSetComplete() {
        ((SetAlarmFrag)base()
                .getFragmentManager()
                .findFragmentByTag(TAG_FRAG_SET_ALARM))
                .onTimeSetComplete(calendar);
    }

    private SetAlarmActivity base(){
        return (SetAlarmActivity) getActivity();
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
