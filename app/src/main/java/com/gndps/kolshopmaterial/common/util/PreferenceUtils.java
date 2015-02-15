package com.gndps.kolshopmaterial.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.gndps.kolshopmaterial.common.GlobalData;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.model.Session;

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
