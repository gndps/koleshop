package com.koleshop.appkoleshop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    //gcm keys
    public static final String GCM_NOTI_USER_INVENTORY_CREATED = "gcm_noti_user_inventory_created";
    public static final String GCM_NOTI_DELETE_OLD_SETTINGS_CACHE = "gcm_noti_delete_old_settings_cache";

    private static String TAG = "GCM_BROADCAST_RECEIVER";

    @Override
    public void onReceive(Context mContext, Intent intent) {
        try {
            String action = intent.getAction();
            /*if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {//control will never come here
                //Device Registered with google messaging servers
                String registrationId = intent.getStringExtra("registration_id");
                Log.i("uo", registrationId);
                String error = intent.getStringExtra("error");
                String unregistered = intent.getStringExtra("unregistered");
                Intent mServiceIntent = new Intent(mContext, PushMessageProcessorService.class);
                mServiceIntent.putExtra("registration_id", registrationId);
                mServiceIntent.setAction("com.koleshop.action.DEVICE_REGISTERED");
                mContext.startService(mServiceIntent);
            } else */if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
                handleReceivedGcmMessage(mContext, intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleReceivedGcmMessage(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        switch (type) {
            case GCM_NOTI_USER_INVENTORY_CREATED:
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                Intent localBroadcastIntent = new Intent();
                localBroadcastIntent.setAction(Constants.ACTION_GCM_BROADCAST_INVENTORY_CREATED);
                lbm.sendBroadcast(localBroadcastIntent);
                break;
            case GCM_NOTI_DELETE_OLD_SETTINGS_CACHE:
                //delete the old seller settings
                Long updatedSettingsMillis = intent.getLongExtra("millis", 0);
                String savedSettingsMillisString = PreferenceUtils.getPreferences(context, Constants.KEY_SELLER_SETTINGS_MILLIS);
                if(savedSettingsMillisString!=null && !savedSettingsMillisString.isEmpty()) {
                    try {
                        Long savedSettingsMillis = Long.parseLong(savedSettingsMillisString);
                        if(updatedSettingsMillis>savedSettingsMillis) {
                            PreferenceUtils.setPreferences(context, Constants.KEY_SELLER_SETTINGS, "");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "problem in parsing the saved settings millis");
                    }
                }
        }

        /*String test = intent.getStringExtra("data");
        Log.d(TAG, "testdata = " + test);
        Toast.makeText(context, test, Toast.LENGTH_SHORT).show();
        MessageType messageType = MessageType.getMessageType(type);
        switch (messageType) {
            case SHOP_SETTINGS:
                SharedPreferences prefs = context.getSharedPreferences(Prefs.SHOP_SETTINGS, context.MODE_PRIVATE);
                ShopSettings shopSettings = new Gson().fromJson(intent.getStringExtra("settings"), ShopSettings.class);
                String shopSettingKey = shopSettings.getSettingName() + "_settings";
                ShopSettings savedShopSettings = null;
                try {
                    savedShopSettings = new Gson().fromJson(prefs.getString(shopSettingKey, ""), ShopSettings.class);
                } catch (Exception e) {
                    Log.i(TAG, "Settings " + shopSettings.getSettingName() + " doesn't exist");
                }
                if (savedShopSettings == null || !(savedShopSettings.getUpdateTime().after(shopSettings.getUpdateTime()))) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(shopSettingKey, new Gson().toJson(shopSettings));
                    //editor.putString(shopSettings.getSettingName(), null);
                    SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor defaultPrefsEditor = defaultPrefs.edit();
                    defaultPrefsEditor.putString(shopSettings.getSettingName(), shopSettings.getSettingValue());
                    defaultPrefsEditor.commit();
                    editor.commit();
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    Intent localBroadcastIntent = new Intent();
                    localBroadcastIntent.setAction(Actions.ACTION_SETTINGS_UPDATED);
                    localBroadcastIntent.putExtra(KolStringExtras.PREFERENCE_NAME, shopSettings.getSettingName());
                    lbm.sendBroadcast(localBroadcastIntent);
                }
                break;
        }*/
    }

}
