package com.koleshop.appkoleshop.common.util;

import android.content.Context;

import com.koleshop.appkoleshop.common.constant.Constants;

/**
 * Created by Gundeep on 19/12/15.
 */
public class NetworkUtils {

    public static void setRequestStatus(Context context, String requestId, String status) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, status);
    }

    public static String getRequestStatus(Context context, String requestId) {
        return  PreferenceUtils.getPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId);
    }

    public static void setRequestStatusComplete(Context context, String requestId) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, null);
    }

}
