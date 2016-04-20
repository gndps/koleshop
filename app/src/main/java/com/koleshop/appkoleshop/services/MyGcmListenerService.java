package com.koleshop.appkoleshop.services;

/**
 * Created by Gundeep on 19/01/16.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.ui.seller.activities.SellerOrdersActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    //gcm keys
    public static final String GCM_NOTI_USER_INVENTORY_CREATED = "gcm_noti_user_inventory_created";
    public static final String GCM_NOTI_DELETE_OLD_SETTINGS_CACHE = "gcm_noti_delete_old_settings_cache";
    public static final String GCM_NOTI_ORDER_UPDATED = "gcm_noti_order_updated";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        //Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
            // not in use
        } else {
            // normal downstream message.
            if(CommonUtils.isUserLoggedIn(this)) {
                handleGcmMessage(data);
            }
        }

    }

    private void handleGcmMessage(Bundle data) {
        String type = data.getString("type");
        Log.d(TAG, "<<<<gcm message received>>>>");
        Context mContext = getApplicationContext();
        String sessionType = "";
        if (type != null && !type.isEmpty()) {
            switch (type) {
                case GCM_NOTI_USER_INVENTORY_CREATED:
                    Log.d(TAG, "User inventory created message received");
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
                    Intent localBroadcastIntent = new Intent();
                    localBroadcastIntent.setAction(Constants.ACTION_GCM_BROADCAST_INVENTORY_CREATED);
                    lbm.sendBroadcast(localBroadcastIntent);
                    break;
                case GCM_NOTI_DELETE_OLD_SETTINGS_CACHE:
                    //this module is deactivated for now
                    /*Log.d(TAG, "user settings updated message received");
                    Log.d(TAG, "clearing seller settings so that it get updated on opening next time");
                    SettingsIntentService.refreshSellerSettings(this);
                    RealmUtils.clearSellerSettings(mContext);
                    Intent intent = new Intent(Constants.ACTION_RELOAD_SETTINGS);
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
                    localBroadcastManager.sendBroadcast(intent);*/
                    break;
                case GCM_NOTI_ORDER_UPDATED:
                    Log.d(TAG, "gcm order updated received");
                    try {
                        int orderStatus = Integer.parseInt(data.getString("status"));
                        Log.d(TAG, "order status = " + orderStatus);
                        Long orderId = Long.valueOf(data.getString("orderId"));
                        String name = data.getString("name");
                        float amount = Float.parseFloat(data.getString("amount"));
                        String imageUrl = data.getString("imageUrl");
                        Intent orderUpdatedIntent = new Intent(Constants.ACTION_ORDER_UPDATE_NOTIFICATION);
                        orderUpdatedIntent.putExtra("status", orderStatus);
                        orderUpdatedIntent.putExtra("orderId", orderId);
                        orderUpdatedIntent.putExtra("name", name);
                        orderUpdatedIntent.putExtra("amount", amount);
                        orderUpdatedIntent.putExtra("imageUrl", imageUrl);
                        Log.d(TAG, "broadcasting order update");
                        sendOrderedBroadcast(orderUpdatedIntent, null);
                    } catch (Exception e) {
                        Log.e(TAG, "problem in order updated notification", e);
                    }
                    break;
                default:
                    break;
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Message not handled by Koleshop", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showIncomingNotification(Long orderId, String buyerName, String amountPayable) {
        Context ctx = getApplicationContext();
        Intent notificationIntent = new Intent(ctx, SellerOrdersActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);


        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx);

        /*
        //add reject button
        Intent reject = new Intent(MainActivity.this, NotificationReceiver.class);
        reject.setAction("Reject");
        PendingIntent pendingReject = PendingIntent.getBroadcast(this, 12345, reject, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.addAction(android.R.drawable.ic_delete, "Reject", pendingReject);

        //add accept button
        Intent accept = new Intent(MainActivity.this, DatePickActivity.class);
        accept.setAction("Accept");
        PendingIntent pendingAccept = PendingIntent.getActivity(this, 12345, accept, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.addAction(android.R.drawable.ic_menu_call, "Accept", pendingAccept);
        */

        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ic_new_order_48dp);

        notification.setContentTitle(buyerName)
                .setContentText("New Order Received")
                .setSmallIcon(R.drawable.ic_koleshop_logo_24dp)
                .setContentIntent(pIntent)
                //.setLargeIcon(notificationInfo.getImage())
                .setLargeIcon(icon)
                .setSound(soundUri)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setFullScreenIntent(pIntent, true)
                .setContentInfo(amountPayable);

        notificationManager.notify(11, notification.build());
    }



    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    /*private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification , notificationBuilder.build());
    }*/
}