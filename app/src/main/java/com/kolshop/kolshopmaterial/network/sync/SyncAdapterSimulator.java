package com.kolshop.kolshopmaterial.network.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.RestCallResponse;
import com.kolshop.kolshopmaterial.model.ShopSettings;
import com.kolshop.kolshopmaterial.network.volley.GsonRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gundeepsingh on 14/09/14.
 */
public class SyncAdapterSimulator {

    private static Context mContext;

    public static void performSync(Context context) {
        mContext = context;
        String sessionType = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_SESSION_TYPE);
        String userId = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_ID);
        if (!sessionType.isEmpty() && !userId.isEmpty()) {
                syncSettings(sessionType);
        }

    }

    private static void syncSettings(String sessionType) {
        final String TAG;
        String prefsName;
        String shopOrBuyer;
        final SharedPreferences prefs;
        final HashMap<String, String> settingsHashMap = new HashMap<String, String>();

        if (sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_SELLER)) {
            TAG = "Shop_Settings_Sync";
            prefsName = "shop_settings";
            shopOrBuyer = "shop";
            prefs = mContext.getSharedPreferences(prefsName, mContext.MODE_PRIVATE);

            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                ShopSettings tempShopSettings = new Gson().fromJson(entry.getValue().toString(), ShopSettings.class);
                if (!tempShopSettings.isSyncedToServer() && isValidShopSetting(tempShopSettings)) {
                    settingsHashMap.put(tempShopSettings.getSettingName(), entry.getValue().toString());
                    Log.d(TAG, "syncing " + entry.getKey());

                }
            }
        } else {
            TAG = "Buyer_Settings_Sync";
            prefsName = "buyer_settings";
            shopOrBuyer = "buyer";
            prefs = mContext.getSharedPreferences(prefsName, mContext.MODE_PRIVATE);
        }

        Gson gson = new Gson();
        final String shopSettingsString = gson.toJson(settingsHashMap);
        Map<String, String> params = new HashMap<String, String>();
        params.put("settings", shopSettingsString);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        GsonRequest gsonRequest = new GsonRequest(Constants.BASE_URL + "ShopNet/api/" + shopOrBuyer + "/syncSettings", RestCallResponse.class, params, new Response.Listener<RestCallResponse>() {
            @Override
            public void onResponse(RestCallResponse restCallResponse) {
                if (restCallResponse.getStatus().equalsIgnoreCase("success")) {
                    HashMap<String, String> receivedShopSettingsHashMap = new Gson().fromJson(restCallResponse.getData(), HashMap.class);
                    Iterator it = receivedShopSettingsHashMap.entrySet().iterator();
                    SharedPreferences.Editor editor = prefs.edit();
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        editor.putString(pairs.getKey().toString() + "_settings", pairs.getValue().toString());
                    }
                    editor.commit();
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
        queue.add(gsonRequest);
    }

    public static boolean isValidShopSetting(ShopSettings shopSettings) {
        return shopSettings.getSettingName() != null && shopSettings.getSettingValue() != null && !shopSettings.getSettingName().equalsIgnoreCase("")
                && !shopSettings.getSettingValue().equalsIgnoreCase("");

    }

}
