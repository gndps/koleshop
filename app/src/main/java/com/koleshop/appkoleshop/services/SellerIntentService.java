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
import com.koleshop.api.sellerEndpoint.SellerEndpoint;
import com.koleshop.api.sellerEndpoint.model.KoleResponse;
import com.koleshop.api.sessionApi.SessionApi;
import com.koleshop.api.sessionApi.model.RestCallResponse;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.json.JSONObject;

import java.io.IOException;

public class SellerIntentService extends IntentService {

    private static final String ACTION_SHOP_STATUS_TOGGLE = "com.koleshop.appkoleshop.services.action.shop_status_toggle";

    private static final String SHOP_STATUS = "com.koleshop.appkoleshop.services.extra.SHOP_STATUS";
    private static final String TAG = "SellerIntentService";

    public SellerIntentService() {
        super("SellerIntentService");
    }

    public static void startActionToggleStatus(Context context, boolean shopStatus) {
        Intent intent = new Intent(context, SellerIntentService.class);
        intent.setAction(ACTION_SHOP_STATUS_TOGGLE);
        intent.putExtra(SHOP_STATUS, shopStatus);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SHOP_STATUS_TOGGLE.equals(action)) {
                final boolean shopOpen = intent.getBooleanExtra(SHOP_STATUS, false);
                handleActionShopStatusToggle(shopOpen);
            }
        }
    }

    private void handleActionShopStatusToggle(boolean shopOpen) {
        SellerEndpoint sellerEndpoint = null;
        if (sellerEndpoint == null) {
            SellerEndpoint.Builder builder = new SellerEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // use 10.0.2.2 for localhost testing
                    .setRootUrl(Constants.SERVER_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            sellerEndpoint = builder.build();
        }


        try {
            Long userId = PreferenceUtils.getUserId(getApplicationContext());
            String sessionId = PreferenceUtils.getSessionId(getApplicationContext());
            KoleResponse response = sellerEndpoint.openCloseShop(userId, sessionId, shopOpen).execute();
            if (response != null && response.getStatus().equalsIgnoreCase("success")) {
                //shop status updated
                Log.d(TAG, "shop status update success");
                Intent intent = new Intent(Constants.ACTION_SHOP_STATUS_UPDATED_SUCCESS);
                intent.putExtra("isChecked", shopOpen);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                //shop status update failed
                Log.d(TAG, "shop status update failed");
                Intent intent = new Intent(Constants.ACTION_SHOP_STATUS_UPDATED_FAILED);
                intent.putExtra("isChecked", shopOpen);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception while updating shop status", e);
            Intent intent = new Intent(Constants.ACTION_SHOP_STATUS_UPDATED_FAILED);
            intent.putExtra("isChecked", shopOpen);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

}
