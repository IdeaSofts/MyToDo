package com.idea.todo.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.idea.todo.R;
import com.idea.todo.constants.C;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateWrapper implements C{

    public static String dateSummary(Context context, long date) {
        long diff = daysToGo(date);
//        String formattedDate = DateFormat.getDateFormat(context).format(date);
        String formattedDate = getDateFormat(date);
        if (diff > 1) {
            return context.getString(R.string.itemDaysToGo, formattedDate, diff);
        }

        else if (diff == 1) {
            return context.getString(R.string.itemDaysTomorrow, formattedDate);
        }

        else if (diff == 0) {
            return context.getString(R.string.itemDaysToday, formattedDate);
        }

        else if (diff == -1) {
            return context.getString(R.string.itemDaysYesterday, formattedDate);
        }

        else {
            return context.getString(R.string.itemDaysOverdue, formattedDate, String.valueOf(Math.abs(diff)));
        }
    }

    public static long daysToGo(long date) {
//        Calendar now = setDateOnly(Calendar.getInstance());
        Calendar now = Calendar.getInstance();
        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(date);
        return (setDateOnly(due).getTimeInMillis() - now.getTimeInMillis()) / (24 * 60 * 60 * 1000);
    }

    public static Calendar setDateOnly(Calendar cal) {
        Calendar date = Calendar.getInstance();
        date.clear();
        date.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        date.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        date.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        return date;
    }

    public static int getDateColor(long date, Context context) {
        long diff = daysToGo(date);

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (diff == 0)
            return mSharedPreferences.getInt(DATE_CURRENT_COLOR, Color.parseColor("#292f6b"));

        if (diff < 0)
            return mSharedPreferences.getInt(DATE_PAST_COLOR, Color.parseColor("#cc1933"));

        return mSharedPreferences.getInt(PREFS_DATE_FUTURE_COLOR, Color.parseColor("#8907a9"));
    }

    public static String getDateFormat(long date) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa", Locale.US).format(date);
    }

    public static String getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa", Locale.US).format(new Date());
    }
}
