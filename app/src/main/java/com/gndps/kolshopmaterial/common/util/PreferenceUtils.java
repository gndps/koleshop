package com.gndps.kolshopmaterial.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.gndps.kolshopmaterial.common.GlobalData;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.model.Session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gundeepsingh on 17/10/14.
 */
public class PreferenceUtils {

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
            if(key!=null && value!=null && !key.trim().equalsIgnoreCase(""))
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

    public static String getLoggedInUsername(Context context) {
        Session session = GlobalData.getInstance().getSession();
        if (session != null && !session.getUsername().trim().isEmpty()) {
            return session.getUsername();
        } else return "";
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
    }

}
