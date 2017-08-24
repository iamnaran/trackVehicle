package com.ved.veddriver.Notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ved.veddriver.Activity.DriverActivity;

import static android.content.ContentValues.TAG;


/**
 * Created by NaRan on 8/17/17 at 11:23.
 */

public class NotificationUtils {

    public static void scheduleNotification(Context context, String title, String body, long delay) {

        int notification_id = 1;

        Notification notification = generateNotification(context, title, body);

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notification_id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        long futureMilli = SystemClock.elapsedRealtime() + delay;

        Log.e(TAG, "scheduleNotification: "+futureMilli);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureMilli, pendingIntent);

    }


    public static Notification generateNotification(Context context, String title, String body) {

        Intent intent = new Intent(context, DriverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return new NotificationCompat.Builder(context)
                .setContentTitle(title).setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }


}
