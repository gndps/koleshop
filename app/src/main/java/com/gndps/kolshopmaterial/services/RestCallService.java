package com.gndps.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gndps.kolshopmaterial.common.GlobalData;
import com.gndps.kolshopmaterial.model.RestCallResponse;
import com.gndps.kolshopmaterial.model.Session;
import com.gndps.kolshopmaterial.network.RestCall;
import com.gndps.kolshopmaterial.network.RestCallListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class RestCallService extends IntentService implements RestCallListener {

    private static final String REQUEST_SIGN_UP = "signup";
    private static final String REQUEST_LOG_IN = "login";
    private static final String REQUEST_CHOOSE_SESSION_TYPE = "choose_session_type";
    private static final String TAG = "REST_CALL_SERVICE";

    public RestCallService() {
        super("RestCallService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String relativeUrl = intent.getStringExtra("url");
            Map<String, String> map = new Gson().fromJson((String) intent.getSerializableExtra("map"), HashMap.class);

            //make rest call with url, action(requestId), map
            RestCall restCall = new RestCall(this, this, relativeUrl, action);
            restCall.execute(map);
        }
    }

    @Override
    public void onRestCallSuccess(Object result, Object requestId) {

        //broadcast result
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent();
        RestCallResponse restCallResponse = (RestCallResponse) result;
        intent.putExtra("result", new Gson().toJson(restCallResponse));
        intent.setAction((String) requestId);
        lbm.sendBroadcast(intent);

        //save result(if needed) so that result can later be used if activity didn't receive broadcast
        if (requestId.toString().equalsIgnoreCase(REQUEST_SIGN_UP) || requestId.toString().equalsIgnoreCase(REQUEST_LOG_IN)
                || requestId.toString().equalsIgnoreCase(REQUEST_CHOOSE_SESSION_TYPE)) {
            //save session
            SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("session", ((RestCallResponse) result).getData());
            editor.commit();
            try {
                GlobalData.getInstance().setSession(new Gson().fromJson(((RestCallResponse) result).getData(), Session.class));
            } catch (Exception e) {
                Log.e(TAG, "session not set in global data\nreason:" + e.getMessage());
            }
        }
    }

    @Override
    public void onRestCallFail(Object result, Object requestId) {

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent();
        RestCallResponse restCallResponse = (RestCallResponse) result;
        intent.putExtra("result", new Gson().toJson(restCallResponse));
        intent.setAction((String) requestId);
        lbm.sendBroadcast(intent);
    }
}
