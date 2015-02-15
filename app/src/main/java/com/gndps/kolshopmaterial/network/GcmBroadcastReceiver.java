package com.gndps.kolshopmaterial.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gndps.kolshopmaterial.common.constant.Actions;
import com.gndps.kolshopmaterial.common.constant.KolStringExtras;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.common.enums.MessageType;
import com.gndps.kolshopmaterial.model.ShopSettings;
import com.gndps.kolshopmaterial.services.PushMessageProcessorService;
import com.google.gson.Gson;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "GCM_BROADCAST_RECEIVER";

    @Override
    public void onReceive(Context mContext, Intent intent) {
        try {
            String action = intent.getAction();
            if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
                //Device Registered with google messaging servers
                String registrationId = intent.getStringExtra("registration_id");
                Log.i("uo", registrationId);
                String error = intent.getStringExtra("error");
                String unregistered = intent.getStringExtra("unregistered");
                Intent mServiceIntent = new Intent(mContext, PushMessageProcessorService.class);
                mServiceIntent.putExtra("registration_id", registrationId);
                mServiceIntent.setAction("com.kolshop.action.DEVICE_REGISTERED");
                mContext.startService(mServiceIntent);
            } else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
                handleReceivedGcmMessage(mContext, intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleReceivedGcmMessage(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
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
        }
    }

}
