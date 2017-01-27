package com.idea.todo.wrapper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.idea.todo.R;

/**
 * Created by sha on 24/01/17.
 */

public class NotificationWrapper {

    public static NotificationCompat.Builder createBuilder(
            Context context,
            String title,
            String content,
            Intent resultIntent,
            int alarmId) {
        /** Uri customSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/agheebo");*/
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_todo)
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(title)
                .setSound(defaultSound)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, alarmId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(content);
        mBuilder .setStyle(bigTextStyle);
        return mBuilder;
    }
}
