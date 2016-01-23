package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.api.sessionApi.SessionApi;
import com.koleshop.api.sessionApi.model.RestCallResponse;

import org.json.JSONObject;

import java.io.IOException;


public class SessionIntentService extends IntentService {

    private static String TAG = "SessionIntentService";

    public SessionIntentService() {
        super("SessionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_REQUEST_OTP.equals(action)) {
                Long phone = Long.parseLong(intent.getStringExtra("phone"));
                int sessionType = Integer.parseInt(intent.getStringExtra("sessionType"));
                String deviceId = intent.getStringExtra("deviceId");
                requestOneTimePassword(phone, sessionType, deviceId);
            } else if (Constants.ACTION_VERIFY_OTP.equals(action)) {
                Long phone = intent.getLongExtra("phone", 0);
                int code = intent.getIntExtra("code", 0);
                verifyOneTimePassword(phone, code);
            }
        }
    }

    private void requestOneTimePassword(Long phone, int sessionType, String deviceId) {
        SessionApi sessionApi = null;
        if (sessionApi == null) {
            SessionApi.Builder builder = new SessionApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // use 10.0.2.2 for localhost testing
                    .setRootUrl(Constants.SERVER_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            sessionApi = builder.build();
        }


        try {
            RestCallResponse restCallResponse = sessionApi.requestCode(phone, deviceId, Constants.DEVICE_TYPE, sessionType).execute();
            if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("success")) {
                Log.d("SessionIntentService", "OTP request success");
                Intent intent = new Intent(Constants.ACTION_REQUEST_OTP_SUCCESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("failure")) {
                Log.d("SessionIntentService", "OTP request failed");
                Intent intent = new Intent(Constants.ACTION_REQUEST_OTP_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                Log.d("SessionIntentService", "OTP network request failed or some other problem");
                Intent intent = new Intent(Constants.ACTION_REQUEST_OTP_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

    private void verifyOneTimePassword(Long phone, int otp) {
        SessionApi sessionApi = null;
        if (sessionApi == null) {
            SessionApi.Builder builder = new SessionApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // use 10.0.2.2 for localhost testing
                    .setRootUrl(Constants.SERVER_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            sessionApi = builder.build();
        }


        try {
            RestCallResponse restCallResponse = sessionApi.verifyCode(phone, otp).execute();
            if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("success")) {
                JSONObject result = new JSONObject(restCallResponse.getData());
                if (result != null && result.has("userId") && result.has("sessionId")) {
                    PreferenceUtils.setPreferences(getApplicationContext(), Constants.KEY_USER_ID, result.getString("userId"));
                    PreferenceUtils.setPreferences(getApplicationContext(), Constants.KEY_SESSION_ID, result.getString("sessionId"));
                    Log.d("SessionIntentService", "Phone verified success");
                    Intent intent = new Intent(Constants.ACTION_VERIFY_OTP_SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } else {
                    Log.d("SessionIntentService", "userId not present in the result");
                    Intent intent = new Intent(Constants.ACTION_VERIFY_OTP_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            } else if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("failure")) {
                Log.d("SessionIntentService", "Phone number verification failed");
                Intent intent = new Intent(Constants.ACTION_VERIFY_OTP_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                Log.d("SessionIntentService", "Phone number verify network request failed or some other problem");
                Intent intent = new Intent(Constants.ACTION_VERIFY_OTP_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

}
