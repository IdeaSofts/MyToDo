/**
 * package-level logging flag
 */

package com.idea.todo.wrapper;

public class Log {
    public final static String LOGTAG = "AlarmClockLog";

//    public static final boolean LOGV = AlarmClock.DEBUG ? Config.LOGD : Config.LOGV;
    public static final boolean LOGV = true;

    public static void v(String logMe) {
        android.util.Log.e(LOGTAG, /* SystemClock.uptimeMillis() + " " + */ logMe);
    }

    public static void e(String logMe) {
        android.util.Log.e(LOGTAG, logMe);
    }

    public static void e(String logMe, Exception ex) {
        android.util.Log.e(LOGTAG, logMe, ex);
    }
}
