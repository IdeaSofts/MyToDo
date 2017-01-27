package com.idea.todo.frag.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.idea.todo.activity.alarm.SetAlarmActivity;
import com.idea.todo.constants.C;

import java.util.Calendar;

public class MyDatePickerDialog extends DialogFragment
        implements android.app.DatePickerDialog.OnDateSetListener, C {

    private long toDoTime;

    public static MyDatePickerDialog newInstance(long toDoTime) {
        MyDatePickerDialog f = new MyDatePickerDialog();
        f.setToDoTime(toDoTime);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        if (toDoTime != 0) {
            c.setTimeInMillis(toDoTime);
        }
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);

        return new android.app.DatePickerDialog(getActivity(), this, currentYear, currentMonth, currentDay);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        showDialogTimePicker(c);
    }

    private void showDialogTimePicker(Calendar c) {
        MyTimePickerDialog.newInstance(c).show(base().getSupportFragmentManager(), "MyTimePickerDialog");
    }

    private SetAlarmActivity base(){
        return (SetAlarmActivity) getActivity();
    }




    public void setToDoTime(long toDoTime) {
        this.toDoTime = toDoTime;
    }
}