package com.example.android.pollposition;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

/**
 * Created by oliver on 28.01.2018.
 */

public class StartAlarm {

    public static void startReminder(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int timer = context.getResources().getInteger(R.integer.beacon_list_timer);
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timer);
        manager.set(AlarmManager.RTC, endTime, pendingIntent);
    }

    public static void cancelReminder(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            manager.cancel(pendingIntent);
        } catch (NullPointerException e) {
            // Alarm got already canceled
        }
    }
}
