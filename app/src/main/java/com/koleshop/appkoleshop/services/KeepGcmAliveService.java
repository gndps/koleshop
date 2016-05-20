package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;


public class KeepGcmAliveService extends IntentService {

    public static final String ACTION_KEEP_GCM_ALIVE = "com.koleshop.appkoleshop.services.action.ACTION_KEEP_GCM_ALIVE";

    public KeepGcmAliveService() {
        super("KeepGcmAliveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_KEEP_GCM_ALIVE.equals(action)) {
                keepGcmAlive();
            }
        }
    }

    private void keepGcmAlive() {
        Context context = getApplicationContext();
        new GcmKeepAlive(context).broadcastIntents();
    }

    public class GcmKeepAlive  {

        protected Context mContext;
        protected Intent gTalkHeartBeatIntent;
        protected Intent mcsHeartBeatIntent;

        public GcmKeepAlive(Context context) {
            mContext = context;
            gTalkHeartBeatIntent = new Intent(
                    "com.google.android.intent.action.GTALK_HEARTBEAT");
            mcsHeartBeatIntent = new Intent(
                    "com.google.android.intent.action.MCS_HEARTBEAT");
        }

        public void broadcastIntents() {
            System.out.println("sending heart beat to keep gcm alive");
            mContext.sendBroadcast(gTalkHeartBeatIntent);
            mContext.sendBroadcast(mcsHeartBeatIntent);
        }

    }

}
