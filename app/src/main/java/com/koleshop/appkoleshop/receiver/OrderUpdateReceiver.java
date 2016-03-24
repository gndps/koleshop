package com.koleshop.appkoleshop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.model.OrderLite;
import com.koleshop.appkoleshop.services.NotificationIntentService;
import com.koleshop.appkoleshop.util.PreferenceUtils;

public class OrderUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "OrderUpdateReceiver";

    public OrderUpdateReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        //notification will reach here only when it is not used by the dynamic ui receiver
        Log.d(TAG, "order update received");
        String action = intent.getAction();
        if (action.equalsIgnoreCase(Constants.ACTION_ORDER_UPDATE_NOTIFICATION)) {
            Bundle intentExtras = intent.getExtras();
            NotificationIntentService.startActionNotify(context, intentExtras);
        } else {
            Log.wtf(TAG, "this notification is not even handled");
        }
    }
}
