package com.idea.todo.wrapper.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.idea.todo.wrapper.Log;

public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Log.LOGV) Log.v("AlarmInitReceiver" + action);

        if (context.getContentResolver() == null) {
            Log.e("AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            Alarms.disableExpiredAlarms(context);
        }
        Alarms.setNextAlertEnhanced(context);
    }
}
