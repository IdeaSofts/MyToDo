package com.idea.todo.wrapper.alarm;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import com.idea.todo.R;
import com.idea.todo.activity.alarm.alert.AlarmAlertActivity;
import com.idea.todo.activity.alarm.alert.AlarmAlertFullScreenActivity;
import com.idea.todo.activity.alarm.SetAlarmActivity;
import com.idea.todo.wrapper.Log;
import com.idea.todo.wrapper.NotificationWrapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Glue class: connects AlarmAlertActivity IntentReceiver to AlarmAlertActivity
 * activity.  Passes through AlarmInfo ID.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("onReceive");

        if (handleKillAndSnoozeCancel(context, intent)) return;

        AlarmInfo alarmInfo = getAlarmObject(intent);

        if (alarmInfo == null) {
            Log.v("AlarmReceiver failed to parse the alarmInfo from the intent");
            return;
        }

        // Intentionally verbose: always log the alarmInfo time to provide useful
        // information in bug reports.
        debug(alarmInfo);

        // Maintain a cpu wake lock until the AlarmAlertActivity and AlarmKlaxon can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        closeDialogs(context);

        /* launch UI, explicitly stating that this is not due to user action
         * so that the current app's notification management is not disturbed */
        startAlarmAlertActivity(context, alarmInfo);

        playRingTone(context);

        handleSnooze(context, alarmInfo);

        displayNotification(context, alarmInfo);

    }

    private boolean handleKillAndSnoozeCancel(Context context, Intent intent) {
        if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
            // The alarm has been killed, update the notification
            updateNotification(context, (AlarmInfo)
                            intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
            return true;
        } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            return true;
        }
        return false;
    }

    private AlarmInfo getAlarmObject(Intent intent) {
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // AlarmInfo object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        try{
            final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
            if (data != null) {
                Parcel in = Parcel.obtain();
                in.unmarshall(data, 0, data.length);
                in.setDataPosition(0);
                return AlarmInfo.CREATOR.createFromParcel(in);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private void debug(AlarmInfo alarmInfo) {
        long now = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS aaa", Locale.US);
        Log.v("AlarmReceiver.onReceive() id " + alarmInfo.id + " setFor "
                + format.format(new Date(alarmInfo.time)));

        if (now > alarmInfo.time + STALE_WINDOW * 1000) {
            if (Log.LOGV) {
                Log.v("AlarmReceiver ignoring stale alarmInfo");
            }
        }
    }

    private void closeDialogs(Context context) {
         /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);
    }

    private void playRingTone(Context context) {

        Uri currentRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                context.getApplicationContext(),
                RingtoneManager.TYPE_ALARM);

        Ringtone ringtone = RingtoneManager.getRingtone(context, currentRingtoneUri);
        ringtone.play();
    }

    private void handleSnooze(Context context, AlarmInfo alarmInfo) {
        // Disable the snooze alert if this alarmInfo is the snooze.
        Alarms.disableSnoozeAlert(context, alarmInfo.id);
        // Disable this alarmInfo if it does not repeat.
        if (!alarmInfo.daysOfWeek.isRepeatSet()) {
            Alarms.enableAlarm(context, alarmInfo.id, false);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.setNextAlertEnhanced(context);
        }
    }

    private void startAlarmAlertActivity(Context context, AlarmInfo alarmInfo) {
        // Decide which activity to start based on the state of the keyguard.
        Class c = AlarmAlertActivity.class;
        KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            // Use the full screen activity for security.
            c = AlarmAlertFullScreenActivity.class;
        }

        Intent alarmAlert = new Intent(context, c);
        alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarmInfo);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(alarmAlert);
    }

    private void displayNotification(Context context, AlarmInfo alarmInfo) {
        Intent playAlarm = new Intent(context, AlarmReceiver.class);
        playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarmInfo);
        context.startService(playAlarm);

        String label = alarmInfo.getLabelOrDefault(context);
        Intent notify = new Intent(context, AlarmAlertActivity.class);
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarmInfo);

        NotificationCompat.Builder mBuilder = NotificationWrapper.createBuilder(
                context,
                label,
                context.getString(R.string.alarm_notify_text),
                notify,
                alarmInfo.id);
        Notification notification = mBuilder.build();

        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(alarmInfo.id, notification);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(Context context, AlarmInfo alarmInfo, int timeout) {
        NotificationManager nm = getNotificationManager(context);

        // If the alarmInfo is null, just cancel the notification.
        if (alarmInfo == null) {
            if (Log.LOGV) {
                Log.v("Cannot update notification for killer callback");
            }
            return;
        }

        String label = alarmInfo.getLabelOrDefault(context);
//      Launch SetAlarmActivity when clicked.
        Intent viewAlarm = new Intent(context, SetAlarmActivity.class);
        viewAlarm.putExtra(Alarms.ALARM_ID, alarmInfo.id);
        NotificationCompat.Builder mBuilder = NotificationWrapper.createBuilder(
                context,
                label,
                context.getString(R.string.alarm_alert_alert_silenced, timeout),
                viewAlarm,
                alarmInfo.id);
        Notification n = mBuilder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.cancel(alarmInfo.id);
        nm.notify(alarmInfo.id, n);
    }
}
