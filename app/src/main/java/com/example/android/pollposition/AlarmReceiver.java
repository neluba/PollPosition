package com.example.android.pollposition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by oliver on 28.01.2018.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.resetBeaconList();
        StartAlarm.cancelReminder(context);
    }
}
