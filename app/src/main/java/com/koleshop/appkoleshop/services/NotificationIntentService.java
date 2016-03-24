package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.model.OrderLite;
import com.koleshop.appkoleshop.util.PreferenceUtils;

public class NotificationIntentService extends IntentService {

    private static final String ACTION_NOTIFY_USER = "com.koleshop.appkoleshop.services.action.NOTIFY_USER";
    private static final String TAG = "NotiIntentService";

    public NotificationIntentService() {
        super("NotificationIntentService");
    }


    public static void startActionNotify(Context context, Bundle orderBundle) {
        Intent intent = new Intent(context, NotificationIntentService.class);
        intent.setAction(ACTION_NOTIFY_USER);
        intent.putExtras(orderBundle);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOTIFY_USER.equals(action)) {
                int orderStatus = intent.getIntExtra("status", 0);
                Long orderId = intent.getLongExtra("orderId", 0);
                String name = intent.getStringExtra("name");
                float orderAmount = intent.getFloatExtra("amount", 0f);
                String imageUrl = intent.getStringExtra("imageUrl");
                notifyUser(orderStatus, orderId, name, orderAmount, imageUrl);
            }
        }
    }

    private void notifyUser(int orderStatus, Long orderId, String name, float orderAmount, String imageUrl) {
        Log.d(TAG, "orderstatus=" + orderStatus + ", orderId=" + orderId + ", name=" + name + ", orderAmount=" + orderAmount
                + ", imageUrl=" + imageUrl);

        Log.d(TAG, "settings preferences flag");
        final Context context = getApplicationContext();
        PreferenceUtils.setPreferencesFlag(context, Constants.KEY_ORDERS_NEED_REFRESHING, true);
        boolean valueOfJustSetFlag = PreferenceUtils.getPreferencesFlag(context, Constants.KEY_ORDERS_NEED_REFRESHING);
        Log.d(TAG, "value of just set flag = " + valueOfJustSetFlag);
        //show notification based on status and session_type
        if (PreferenceUtils.isSessionTypeSeller(context)) {
            Log.d(TAG, "this user is a seller");
            switch (orderStatus) {
                case OrderStatus.INCOMING:
                case OrderStatus.CANCELLED:
                case OrderStatus.NOT_DELIVERED:
                    OrderLite orderLite = new OrderLite(orderId, name, orderAmount, orderStatus, imageUrl);
                    Log.d(TAG, "orderlite = " + orderLite.toString() + "\nadding order to notification objects");
                    KoleshopNotificationUtils.addOrderToNotifications(orderLite);
                    Log.d(TAG, "starting async task");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Log.d(TAG, "");
                            KoleshopNotificationUtils.notifySeller(context);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            Log.d(TAG, "async task was complete");
                            super.onPostExecute(aVoid);
                        }
                    }.execute(null, null, null);
                    break;
                default:
                    break;
            }
        } else if (PreferenceUtils.isSessionTypeBuyer(context)) {

            Log.d(TAG, "this user is a buyer");
            switch (orderStatus) {
                case OrderStatus.REJECTED:
                case OrderStatus.CANCELLED:
                case OrderStatus.OUT_FOR_DELIVERY:
                case OrderStatus.READY_FOR_PICKUP:
                    OrderLite orderLite = new OrderLite(orderId, name, orderAmount, orderStatus, imageUrl);
                    Log.d(TAG, "orderlite = " + orderLite.toString() + "\nadding order to notification objects");
                    KoleshopNotificationUtils.addOrderToNotifications(orderLite);
                    Log.d(TAG, "starting async task");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Log.d(TAG, "");
                            KoleshopNotificationUtils.notifyBuyer(context);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            Log.d(TAG, "async task was complete");
                            super.onPostExecute(aVoid);
                        }
                    }.execute(null, null, null);
                    break;
                default:
                    break;
            }
        } else {
            Log.d(TAG, "ignoring the update");
            //ignore this update
        }
    }

}
