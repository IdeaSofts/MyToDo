package com.idea.todo.wrapper.alarm;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.idea.todo.R;
import com.idea.todo.wrapper.Log;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public final class AlarmInfo implements Parcelable {
    public int        id;
    public boolean    enabled;
    public int        hour;
    public int        minutes;
    public DaysOfWeek daysOfWeek;
    public long       time;
    public boolean    vibrate;
    public String     label;
    public Uri        alert;
    public boolean    silent;

    public AlarmInfo(Cursor c) {
        id = c.getInt(Columns.ALARM_ID_INDEX);
        enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.ALARM_HOUR_INDEX);
        minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
        daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
        time = c.getLong(Columns.ALARM_TIME_INDEX);
        vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
        label = c.getString(Columns.ALARM_MESSAGE_INDEX);
        String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
        if (Alarms.ALARM_ALERT_SILENT.equals(alertString)) {
            if (Log.LOGV) {
                Log.v("AlarmInfo is marked as silent");
            }
            silent = true;
        } else {
            if (alertString != null && alertString.length() != 0) {
                alert = Uri.parse(alertString);
            }

            // If the database alert is null or it failed to parse, use the
            // default alert.
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM);
            }
        }
    }


    //////////////////////////////
    // Column definitions
    //////////////////////////////

    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://com.idea.todo/alarm");

        /**
         * We'll save the date (timeInMillis) to enable
         * alarm in any day, not just in the current or the next day.
         */
        public static final String DATE = "date";

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>Type: INTEGER</P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>Type: INTEGER</P>
         */
        public static final String MINUTES = "minutes";

        /**
         * Days of week coded as integer
         * <P>Type: INTEGER</P>
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * AlarmInfo time in UTC milliseconds from the epoch.
         * <P>Type: INTEGER</P>
         */
        public static final String ALARM_TIME = "alarmtime";

        /**
         * True if alarm is active
         * <P>Type: BOOLEAN</P>
         */
        public static final String ENABLED = "enabled";

        /**
         * True if alarm should vibrate
         * <P>Type: BOOLEAN</P>
         */
        public static final String VIBRATE = "vibrate";

        /**
         * Message to show when alarm triggers
         * Note: not currently used
         * <P>Type: STRING</P>
         */
        public static final String MESSAGE = "message";

        /**
         * Audio alert to play when alarm triggers
         * <P>Type: STRING</P>
         */
        public static final String ALERT = "alert";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                HOUR + ", " + MINUTES + " ASC";

        // Used when filtering enabled alarms.
        public static final String WHERE_ENABLED = ENABLED + "=1";

        static final String[] ALARM_QUERY_COLUMNS = {
                _ID,
                HOUR,
                MINUTES,
                DAYS_OF_WEEK,
                ALARM_TIME,
                ENABLED,
                VIBRATE,
                MESSAGE,
                ALERT
        };

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        public static final int ALARM_ID_INDEX = 0;
        public static final int ALARM_HOUR_INDEX = 1;
        public static final int ALARM_MINUTES_INDEX = 2;
        public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
        public static final int ALARM_TIME_INDEX = 4;
        public static final int ALARM_ENABLED_INDEX = 5;
        public static final int ALARM_VIBRATE_INDEX = 6;
        public static final int ALARM_MESSAGE_INDEX = 7;
        public static final int ALARM_ALERT_INDEX = 8;
    }
    //////////////////////////////
    // End column definitions
    //////////////////////////////

    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     */
    public static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
        };

        // Bitmask of all repeating days
        private int mDays;

        DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
                return showNever ?
                        context.getText(R.string.never).toString() : "";
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0) ret.append(
                            context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        /**
         * returns number of days from today until next alarm
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
    }

    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Creator<AlarmInfo> CREATOR
            = new Creator<AlarmInfo>() {
        public AlarmInfo createFromParcel(Parcel p) {
            return new AlarmInfo(p);
        }

        public AlarmInfo[] newArray(int size) {
            return new AlarmInfo[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(time);
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeParcelable(alert, flags);
        p.writeInt(silent ? 1 : 0);
    }

    public AlarmInfo(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        vibrate = p.readInt() == 1;
        label = p.readString();
        alert = p.readParcelable(null);
        silent = p.readInt() == 1;
    }

    //////////////////////////////
    // end Parcelable apis
    //////////////////////////////

    /**
     * Getters
     */
    public String getLabelOrDefault(Context context) {
        if (label == null || label.length() == 0) {
            return context.getString(R.string.default_label);
        }
        return label;
    }
}