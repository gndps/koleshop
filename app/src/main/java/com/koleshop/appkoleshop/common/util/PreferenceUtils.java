package com.koleshop.appkoleshop.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.constant.Prefs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gundeepsingh on 17/10/14.
 */
public class PreferenceUtils {

    public static String TAG = "PreferencesUtil";

    public static void setPreferencesFlag(Context context, String flagKey, boolean flagValue) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.FLAGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(flagKey, flagValue);
        editor.commit();
    }

    public static void setPreferences(Context context, HashMap<String, String> map) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.KOL_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String key = (String) pair.getKey();
            String value = (String) pair.getValue();
            if(key!=null && value!=null && !key.trim().equalsIgnoreCase(""))
            {
                editor.putString(key, value);
            }
        }
        editor.apply();
    }

    public static void setPreferences(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.KOL_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if(value == null)
        {
            value = "";
        }
            if(key!=null && !key.trim().equalsIgnoreCase(""))
            {
                editor.putString(key, value);
            }
        editor.apply();
    }

    public static String getPreferences(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.KOL_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static boolean getPreferencesFlag(Context context, String flagKey) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.FLAGS, Context.MODE_PRIVATE);
        return prefs.getBoolean(flagKey, false);
    }

    public static void clearUserSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.FLAGS, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        prefs = context.getSharedPreferences(Prefs.SHOP_SETTINGS, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        prefs = context.getSharedPreferences(Prefs.BUYER_SETTINGS, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        prefs = context.getSharedPreferences(Prefs.USER_INFO, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        //Realm.deleteRealmFile(context);
    }

    public static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = context.getSharedPreferences(Prefs.KOL_PREFS, Context.MODE_PRIVATE);
        int appVersion = CommonUtils.getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_REG_ID, regId);
        editor.putInt(Constants.KEY_APP_VERSION, appVersion);
        editor.apply();
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(Prefs.KOL_PREFS, Context.MODE_PRIVATE);
        String registrationId = prefs.getString(Constants.KEY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.KEY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = CommonUtils.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

}
