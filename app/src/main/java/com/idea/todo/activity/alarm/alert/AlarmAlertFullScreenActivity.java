package com.idea.todo.activity.alarm.alert;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.idea.todo.R;
import com.idea.todo.activity.BaseActivity;
import com.idea.todo.wrapper.DigitalClock;
import com.idea.todo.wrapper.Log;
import com.idea.todo.wrapper.NotificationWrapper;
import com.idea.todo.wrapper.alarm.AlarmInfo;
import com.idea.todo.wrapper.alarm.AlarmReceiver;
import com.idea.todo.wrapper.alarm.Alarms;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * AlarmInfo Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class AlarmAlertFullScreenActivity extends BaseActivity implements
        View.OnClickListener{

    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "10";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";
    protected AlarmInfo mAlarmInfo;
    private int mVolumeBehavior;
    @BindView(R.id.btnSnooze)
    Button btnSnooze;

    @BindView(R.id.btnDismiss)
    Button btnDismiss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(inflater.inflate(R.layout.activity_alarm_alert, null));
        ButterKnife.bind(this);
        initDigitalClock(inflater);
        init();
        setTitle();
        keepScreenOn();
    }

    private void keepScreenOn() {
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // Turn on the screen unless we are being launched from the AlarmAlertActivity
        // subclass.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void initDigitalClock(LayoutInflater inflater) {
        SharedPreferences settings = getSharedPreferences(ALARM_PREFERENCES, 0);
        int face = settings.getInt(PREF_CLOCK_FACE, 0);
        if (face < 0 || face >= CLOCKS.length) {
            face = 0;
        }
        ViewGroup clockView = (ViewGroup) findViewById(R.id.clockView);
        inflater.inflate(CLOCKS[face], clockView);
        View clockLayout = findViewById(R.id.clock);
        if (clockLayout instanceof DigitalClock) {
            ((DigitalClock) clockLayout).setAnimate();
        }
    }

    private void init() {
        btnSnooze.requestFocus();
        btnSnooze.setOnClickListener(this);
        btnDismiss.setOnClickListener(this);

        final String vol =
                PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(KEY_VOLUME_BEHAVIOR, DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        mAlarmInfo = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        // Register to get the alarm killed intent.
        registerReceiver(mReceiver, new IntentFilter(Alarms.ALARM_KILLED));

    }

    private void setTitle() {
        String label = mAlarmInfo.getLabelOrDefault(this);
        TextView title = (TextView) findViewById(R.id.alertTitle);
        title.setText(label);
    }

    @Override
    public void onClick(View view) {
        if (view == btnSnooze){
            snooze();
        }
        else if (view == btnDismiss){
            dismiss(false);
        }
    }

    // Attempt to btnSnooze this alert.
    private void snooze() {
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(this, mAlarmInfo.id, snoozeTime);

        // Get the display time for the btnSnooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarmInfo.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.

        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_ID, mAlarmInfo.id);

        NotificationCompat.Builder mBuilder = NotificationWrapper.createBuilder(
                this,
                label,
                getString(R.string.alarm_notify_text),
                cancelSnooze,
                mAlarmInfo.id);
        Notification notification = mBuilder.build();

        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(mAlarmInfo.id, notification);
//        *****************

        String displayTime = getString(R.string.alarm_alert_snooze_set, snoozeMinutes);
        // Intentionally log the btnSnooze time for debugging.
        Log.v(displayTime);

        // Display the btnSnooze minutes in a toast.
        Toast.makeText(this, displayTime, Toast.LENGTH_LONG).show();
        stopService(new Intent(this, AlarmReceiver.class));
        finish();
    }

    // Dismiss the alarm.
    private void dismiss(boolean killed) {
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarmInfo.id);
            Intent i = new Intent(this, AlarmReceiver.class);
            i.setAction(Alarms.ALARM_KILLED);
            stopService(i);
        }
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Log.LOGV) Log.v("AlarmAlertActivity.OnNewIntent()");

        mAlarmInfo = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            // Don't hang around.
            finish();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.LOGV) Log.v("AlarmAlertActivity.onDestroy()");
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlertActivity
        // so that the dialog is dismissed.
        return;
    }

    // Receives the ALARM_KILLED action from the AlarmKlaxon.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AlarmInfo alarmInfo = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            if (alarmInfo != null && mAlarmInfo.id == alarmInfo.id) {
                dismiss(true);
            }
        }
    };
}
