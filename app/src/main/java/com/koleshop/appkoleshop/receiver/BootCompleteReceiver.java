package com.koleshop.appkoleshop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.koleshop.appkoleshop.util.PreferenceUtils;

public class BootCompleteReceiver extends BroadcastReceiver {

    KoleshopAlarmReceiver alarm = new KoleshopAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            if(PreferenceUtils.isSessionTypeSeller(context)) {
                alarm.setAlarm(context);
            }
        }
    }
}
