package com.idea.todo.wrapper.alarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;

import com.idea.todo.constants.C;
import com.idea.todo.wrapper.Log;

import java.util.Calendar;

/**
 * The Alarms provider supplies info about AlarmInfo Clock settings
 */
public class Alarms implements C {

    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving AlarmInfo broadcasts
    // from the alarm manager.
    public static final String ALARM_ALERT_ACTION = "com.idea.todo.ALARM_ALERT";

    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    // This intent is sent from the notification when the user cancels the
    // snooze alert.
    public static final String CANCEL_SNOOZE = "cancel_snooze";

    // This string is used when passing an AlarmInfo object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm.idea";

    // This extra is the raw AlarmInfo object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    // This string is used to identify the alarm id passed to SetAlarmActivity from the
    // list of alarms.
    public static final String ALARM_ID = "alarm_id";

    static final  String PREF_SNOOZE_ID = "snooze_id";
    static final  String PREF_SNOOZE_TIME = "snooze_time";

    private final static String DM12 = "E h:mm aa";
    private final static String DM24 = "E k:mm";

    private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    public final static String M24 = "kk:mm";

    /**
     * Creates a new AlarmInfo.
     */
    public static Uri addAlarm(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put(AlarmInfo.Columns.HOUR, 8);
        return contentResolver.insert(AlarmInfo.Columns.CONTENT_URI, values);
    }

    /**
     * Removes an existing AlarmInfo.  If this alarm is snoozing, disables
     * snooze.  Sets next alert.
     */
    public static void deleteAlarm(
            Context context, int alarmId) {

        ContentResolver contentResolver = context.getContentResolver();
        /* If alarm is snoozing, lose it */
        disableSnoozeAlert(context, alarmId);

        Uri uri = ContentUris.withAppendedId(AlarmInfo.Columns.CONTENT_URI, alarmId);
        contentResolver.delete(uri, "", null);

        setNextAlertEnhanced(context);
    }

    // Private method to get a more limited set of alarms from the database.
    private static Cursor getEnabledAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(AlarmInfo.Columns.CONTENT_URI,
                AlarmInfo.Columns.ALARM_QUERY_COLUMNS, AlarmInfo.Columns.WHERE_ENABLED,
                null, null);
    }

    /**
     * Return an AlarmInfo object representing the alarm id in the database.
     * Returns null if no alarm exists.
     */
    public static AlarmInfo getAlarm(ContentResolver contentResolver, int alarmId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(AlarmInfo.Columns.CONTENT_URI,alarmId),
                AlarmInfo.Columns.ALARM_QUERY_COLUMNS,
                null,
                null,
                null);
        AlarmInfo alarmInfo = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarmInfo = new AlarmInfo(cursor);
            }
            cursor.close();
        }
        return alarmInfo;
    }

    /**
     * A convenience method to enable or disable an alarm.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     */

    public static void enableAlarm(final Context context, final int id, boolean enabled) {
        enableAlarmInternalEnhanced(context, getAlarm(context.getContentResolver(), id), enabled);
        setNextAlertEnhanced(context);
    }

    /**
     * Disables non-repeating alarms that have passed.  Called at
     * boot.
     */
    public static void disableExpiredAlarms(final Context context) {
        Cursor cur = getEnabledAlarmsCursor(context.getContentResolver());
        long now = System.currentTimeMillis();

        if (cur.moveToFirst()) {
            do {
                AlarmInfo alarmInfo = new AlarmInfo(cur);
                // A time of 0 means this alarmInfo repeats. If the time is
                // non-zero, check if the time is before now.
                if (alarmInfo.time < now) {
                    if (Log.LOGV) {
                        Log.v("** DISABLE " + alarmInfo.id + " now " + now +" set "
                                + alarmInfo.time);
                    }
                    enableAlarmInternalEnhanced(context, alarmInfo, false);
                }
            } while (cur.moveToNext());
        }
        cur.close();
    }

    public static void saveSnoozeAlert(final Context context, final int id, final long time) {
        SharedPreferences prefs = context.getSharedPreferences( ALARM_PREFERENCES, 0);
        if (id == -1) {
            clearSnoozePreference(context, prefs);
        } else {
            SharedPreferences.Editor ed = prefs.edit();
            ed.putInt(PREF_SNOOZE_ID, id);
            ed.putLong(PREF_SNOOZE_TIME, time);
            ed.apply();
        }
        // Set the next alert after updating the snooze.
        setNextAlertEnhanced(context);
    }

    /**
     * Disable the snooze alert if the given id matches the snooze id.
     */
    static void disableSnoozeAlert(final Context context, final int id) {
        SharedPreferences prefs = context.getSharedPreferences(
                ALARM_PREFERENCES, 0);
        int snoozeId = prefs.getInt(PREF_SNOOZE_ID, -1);
        if (snoozeId == -1) {
            // No snooze set, do nothing.
            return;
        } else if (snoozeId == id) {
            // This is the same id so clear the shared prefs.
            clearSnoozePreference(context, prefs);
        }
    }

    /**
     * Tells the StatusBar whether the alarm is enabled or disabled
     */
    private static void setStatusBarIcon(Context context, boolean enabled) {
        final String ACTION_ALARM_CHANGED = "android.intent.action.ALARM_CHANGED";
        Intent alarmChanged = new Intent(ACTION_ALARM_CHANGED);
        alarmChanged.putExtra("alarmSet", enabled);
        context.sendBroadcast(alarmChanged);
    }

    /**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     * @param hour Always in 24 hour 0-23
     * @param minute 0-59
     */
    public static Calendar calculateAlarm(int hour, int minute) {
        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    public static String formatTime(final Context context, int hour, int minute) {
        Calendar c = calculateAlarm(hour, minute);
        return formatTime(context, c);
    }

    /* used by SetAlarmFrag */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * Shows day and time -- used for lock screen
     */
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    public static boolean get24HourMode(final Context context) {
        return DateFormat.is24HourFormat(context);
    }

//    ******************************************************************************************

    /**
     * A convenience method to set an alarm in the Alarms
     * content provider.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     * @param vibrate        corresponds to the VIBRATE column
     * @param message        corresponds to the MESSAGE column
     * @param alert          corresponds to the ALERT column
     * @return Time when the alarm will fire.
     */
    public static void setAlarmEnhanced(
            Context context,
            int id,
            boolean enabled,
            boolean vibrate,
            String message,
            String alert,
            Calendar calendar) {

        ContentValues values = new ContentValues(8);
        ContentResolver resolver = context.getContentResolver();

        values.put(AlarmInfo.Columns.ENABLED, enabled ? 1 : 0);
        values.put(AlarmInfo.Columns.ALARM_TIME, calendar.getTimeInMillis());
        values.put(AlarmInfo.Columns.VIBRATE, vibrate);
        values.put(AlarmInfo.Columns.MESSAGE, message);
        values.put(AlarmInfo.Columns.ALERT, alert);
        resolver.update(ContentUris.withAppendedId(AlarmInfo.Columns.CONTENT_URI, id), values, null, null);

        if (enabled) {
            // If this alarm fires before the next snooze, clear the snooze to
            // enable this alarm.
            SharedPreferences prefs = context.getSharedPreferences( ALARM_PREFERENCES, 0);
            long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
            if (calendar.getTimeInMillis() < snoozeTime) {
                clearSnoozePreference(context, prefs);
            }
        }
        setNextAlertEnhanced(context);
    }

    // Helper to remove the snooze preference. Do not use clear because that
    // will erase the clock preferences. Also clear the snooze notification in
    // the window shade.
    private static void clearSnoozePreference(final Context context, final SharedPreferences prefs) {
        final int alarmId = prefs.getInt(PREF_SNOOZE_ID, -1);
        if (alarmId != -1) {
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(alarmId);
        }

        final SharedPreferences.Editor ed = prefs.edit();
        ed.remove(PREF_SNOOZE_ID);
        ed.remove(PREF_SNOOZE_TIME);
        ed.apply();
    }

    /**
     * Called at system startup, on time/timezone change, and whenever
     * the user changes alarm settings.  Activates snooze if set,
     * otherwise loads all alarms, activates next alert.
     */
    public static void setNextAlertEnhanced(final Context context) {
        AlarmInfo alarmInfo = calculateNextAlertEnhanced(context);
        if (alarmInfo != null) {
            enableAlert(context, alarmInfo, alarmInfo.time);
        } else {
            disableAlert(context);
        }
    }

    public static AlarmInfo calculateNextAlertEnhanced(final Context context) {
        AlarmInfo alarmInfo = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        Cursor cursor = getEnabledAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    AlarmInfo a = new AlarmInfo(cursor);

                    if (a.time < now) {
                        // Expired alarmInfo, disable it and move along.
                        enableAlarmInternalEnhanced(context, a, false);
                        continue;
                    }

                    if (a.time < minTime) {
                        minTime = a.time;
                        alarmInfo = a;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return alarmInfo;
    }

    private static void enableAlarmInternalEnhanced(final Context context, final AlarmInfo alarmInfo, boolean enabled) {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(AlarmInfo.Columns.ENABLED, enabled ? 1 : 0);
        resolver.update(ContentUris.withAppendedId(
                AlarmInfo.Columns.CONTENT_URI, alarmInfo.id), values, null, null);
    }

    /**
     * Sets alert in AlarmManger and StatusBar.  This is what will
     * actually launch the alert when the alarmInfo triggers.
     *
     * @param alarmInfo AlarmInfo.
     * @param atTimeInMillis milliseconds since epoch
     */
    private static void enableAlert(Context context, final AlarmInfo alarmInfo,
                                    final long atTimeInMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Log.LOGV) {
            Log.v("** setAlert id " + alarmInfo.id + " atTime " + atTimeInMillis);
        }

        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the AlarmInfo class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the AlarmInfo
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        alarmInfo.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

        setStatusBarIcon(context, true);

        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(atTimeInMillis));
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
    }

    /**
     * Disables alert in AlarmManger and StatusBar.
     */
    static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, new Intent(ALARM_ALERT_ACTION),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
        setStatusBarIcon(context, false);
        saveNextAlarm(context, "");
    }

    /**
     * Save time of the next alarm, as a formatted string, into the system
     * settings so those who care can make use of it.
     */
    static void saveNextAlarm(final Context context, String timeString) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED,
                timeString);
    }



}
