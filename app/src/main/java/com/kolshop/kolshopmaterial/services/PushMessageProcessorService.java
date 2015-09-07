package com.kolshop.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.kolshop.kolshopmaterial.model.ShopSettings;
import com.google.gson.Gson;

public class PushMessageProcessorService extends IntentService {

    private static final String ACTION_PUSH_MESSAGE_RECIEVED = "com.kolshop.action.PUSH_MESSAGE_RECIEVED";

    public PushMessageProcessorService() {
        super("PushMessageProcessorService");
    }

    public static void processPushMessage(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PushMessageProcessorService.class);
        intent.setAction(ACTION_PUSH_MESSAGE_RECIEVED);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PUSH_MESSAGE_RECIEVED.equals(action)) {
                final String messageType = intent.getStringExtra("messageType");
                if (messageType.equalsIgnoreCase("shop_settings")) {
                    String settingsString = intent.getStringExtra("settings");
                    ShopSettings shopSettings = new Gson().fromJson(settingsString, ShopSettings.class);
                    handleReceivedSettings(shopSettings);
                }
                final String param2 = intent.getStringExtra("data2");
                processPushMessage(this, "", param2);
            }
        }
    }

    private void handleReceivedSettings(ShopSettings shopSettings) {

    }
}
