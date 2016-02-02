package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.koleshop.api.commonEndpoint.model.Brand;
import com.koleshop.api.myApi.MyApi;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DemoIntentService extends IntentService {

    private static String TAG = "DemoIntentService";

    public DemoIntentService() {
        super("DemoIntentService");
    }

    public static void sendToSeller(Context context, String type, String message) {
        Intent intent = new Intent(context, DemoIntentService.class);
        intent.setAction("sendToSeller");
        intent.putExtra("type", type);
        intent.putExtra("message", message);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if ("sendToSeller".equals(action)) {
                final String type = intent.getStringExtra("type");
                final String message = intent.getStringExtra("message");
                sendToSeller(type, message);
            }
        }
    }

    private void sendToSeller(String type, String message) {
        MyApi myApi = null;
        MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setApplicationName(Constants.APP_NAME)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        myApi = builder.build();

        com.koleshop.api.myApi.model.KoleResponse result = null;
        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = myApi.sendToSeller(type, message).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if (result == null) {
            Log.d(TAG, "product brands loading failed");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_BRANDS_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        } else {
            //broadcast result success
        }

    }
}
