package com.idea.todo.wrapper.alarm;

import android.content.Context;
import android.os.PowerManager;

import com.idea.todo.wrapper.Log;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlertActivity activity
 */
class AlarmAlertWakeLock {

    private static PowerManager.WakeLock sCpuWakeLock;

    static void acquireCpuWakeLock(Context context) {
        Log.v("Acquiring cpu wake lock");
        if (sCpuWakeLock != null) {
            return;
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sCpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, Log.LOGTAG);
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        Log.v("Releasing cpu wake lock");
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
