package com.gndps.kolshopmaterial.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.model.Session;
import com.google.gson.Gson;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gundeepsingh on 17/08/14.
 */

public class CommonUtils {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    static Random rnd = new Random();

    public static boolean validateEmail(final String hex) {

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(hex);
        return matcher.matches();
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static Session getUserSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Prefs.USER_INFO, context.MODE_PRIVATE);
        if (!prefs.getString("session", "").equalsIgnoreCase("")) {
            Session session = new Gson().fromJson(prefs.getString("session", ""), Session.class);
            return session;
        } else return null;
    }

}
