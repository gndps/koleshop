package com.koleshop.appkoleshop.services;

/**
 * Created by Gundeep on 19/01/16.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.Prefs;
import com.koleshop.appkoleshop.util.PreferenceUtils;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    //gcm keys
    public static final String GCM_NOTI_USER_INVENTORY_CREATED = "gcm_noti_user_inventory_created";
    public static final String GCM_NOTI_DELETE_OLD_SETTINGS_CACHE = "gcm_noti_delete_old_settings_cache";

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
            //not in use
        } else {
            // normal downstream message.
            handleGcmMessage(data);
        }

    }

    private void handleGcmMessage(Bundle data) {
        String type = data.getString("type");
        Context mContext = getApplicationContext();
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
                    Log.d(TAG, "user settings updated message received");
                    //delete the old seller settings
                    String millisString = data.getString("millis", "");
                    Long updatedSettingsMillis = 0l;
                    if (!millisString.isEmpty()) {
                        try {
                            updatedSettingsMillis = Long.parseLong(millisString);
                        } catch (Exception e) {
                            Log.e(TAG, "parse exception", e);
                        }
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences(Prefs.KOLE_PREFS, MODE_PRIVATE);
                    Long savedSettingsMillis = sharedPreferences.getLong(Constants.KEY_SELLER_SETTINGS_MILLIS, 0);
                    if (updatedSettingsMillis > savedSettingsMillis) {
                        Log.d(TAG, "will update user settings");
                        PreferenceUtils.setPreferences(mContext, Constants.KEY_SELLER_SETTINGS, "");
                        Intent intent = new Intent(Constants.ACTION_RELOAD_SETTINGS);
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
                        localBroadcastManager.sendBroadcast(intent);
                    } else {
                        Log.d(TAG, "no need to update user settings");
                    }
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