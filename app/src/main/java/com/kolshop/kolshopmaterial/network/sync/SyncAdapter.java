package com.kolshop.kolshopmaterial.network.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.constant.Prefs;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.RestCallResponse;
import com.kolshop.kolshopmaterial.model.ShopSettings;
import com.kolshop.kolshopmaterial.network.volley.GsonRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gundeepsingh on 30/08/14.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    public static boolean isValidShopSetting(ShopSettings shopSettings) {
        return shopSettings.getSettingName() != null && shopSettings.getSettingValue() != null && !shopSettings.getSettingName().equalsIgnoreCase("")
                && !shopSettings.getSettingValue().equalsIgnoreCase("");

    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        //get products with updated_time > synced_time
        //get products with synced_time = null
        //connect to server, send data 20 products at a time
        String sessionType = PreferenceUtils.getPreferences(getContext(), Constants.KEY_USER_SESSION_TYPE);
        String userId = PreferenceUtils.getPreferences(getContext(), Constants.KEY_USER_ID);
        if (!sessionType.isEmpty() && !userId.isEmpty()) {
            syncSettings(sessionType);
        }

    }

    private void syncSettings(String sessionType) {
        final String TAG;
        String prefsName;
        String shopOrBuyer;
        final SharedPreferences prefs;
        final HashMap<String, String> settingsHashMap = new HashMap<String, String>();

        if (sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_SELLER)) {
            TAG = "Shop_Settings_Sync";
            shopOrBuyer = "shop";
            prefs = getContext().getSharedPreferences(Prefs.SHOP_SETTINGS, getContext().MODE_PRIVATE);

            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                try {
                    String shopSettingsString = entry.getValue().toString();
                    ShopSettings tempShopSettings = new Gson().fromJson(shopSettingsString, ShopSettings.class);
                    if (!tempShopSettings.isSyncedToServer() && isValidShopSetting(tempShopSettings)) {
                        settingsHashMap.put(tempShopSettings.getSettingName(), entry.getValue().toString());
                        Log.d(TAG, "syncing " + entry.getKey());

                    }
                } catch (Exception e) {
                    Log.getStackTraceString(e);
                }
            }
        } else {
            TAG = "Buyer_Settings_Sync";
            shopOrBuyer = "buyer";
            prefs = getContext().getSharedPreferences(Prefs.BUYER_SETTINGS, getContext().MODE_PRIVATE);
        }

        Gson gson = new Gson();
        final String shopSettingsString = gson.toJson(settingsHashMap);
        Map<String, String> params = new HashMap<String, String>();
        params.put("settings", shopSettingsString);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        GsonRequest gsonRequest = new GsonRequest(Constants.BASE_URL + "ShopNet/api/" + shopOrBuyer + "/syncSettings", RestCallResponse.class, params, new Response.Listener<RestCallResponse>() {
            @Override
            public void onResponse(RestCallResponse restCallResponse) {
                if (restCallResponse.getStatus().equalsIgnoreCase("success")) {
                    Log.i(TAG, restCallResponse.getData());
                } else if (restCallResponse.getStatus().equalsIgnoreCase("failure")) {
                    Log.e(TAG, restCallResponse.getReason());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                VolleyLog.d(TAG, "Error: " + volleyError.getMessage());
            }
        });
        //VolleyUtil.getInstance().addToRequestQueue(gsonRequest);
        queue.add(gsonRequest);
    }

}
