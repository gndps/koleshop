package com.koleshop.appkoleshop.util;

import android.content.Context;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.Prefs;

/**
 * Created by Gundeep on 19/12/15.
 */
public class NetworkUtils {

    //Request Status
    public static final String REQUEST_STATUS_PROCESSING = "request_processing" ;
    public static final String REQUEST_STATUS_SUCCESS = "request_success";
    public static final String REQUEST_STATUS_FAILED = "reqeust_failed";

    public static void setRequestStatus(Context context, String requestId, String status) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, status, Prefs.NETWORK_REQUEST_PREFS);
    }

    public static String getRequestStatus(Context context, String requestId) {
        return  PreferenceUtils.getPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, Prefs.NETWORK_REQUEST_PREFS);
    }

    public static void setRequestStatusProcessing(Context context, String requestId) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, REQUEST_STATUS_PROCESSING, Prefs.NETWORK_REQUEST_PREFS);
    }

    public static void setRequestStatusSuccess(Context context, String requestId) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, REQUEST_STATUS_SUCCESS, Prefs.NETWORK_REQUEST_PREFS);
    }

    public static void setRequestStatusFailed(Context context, String requestId) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, REQUEST_STATUS_FAILED, Prefs.NETWORK_REQUEST_PREFS);
    }

    public static void setRequestStatusComplete(Context context, String requestId) {
        PreferenceUtils.setPreferences(context, Constants.REQUEST_STATUS_PREFIX + requestId, null, Prefs.NETWORK_REQUEST_PREFS);
    }

}
