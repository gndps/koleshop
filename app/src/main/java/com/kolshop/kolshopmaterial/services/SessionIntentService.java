package com.kolshop.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.server.sessionApi.SessionApi;
import com.kolshop.server.sessionApi.model.RestCallResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SessionIntentService extends IntentService {

    private static String TAG = "SessionIntentService";

    public SessionIntentService() {
        super("SessionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_SIGN_UP.equals(action)) {
                final String username = intent.getStringExtra("username");
                final String password = intent.getStringExtra("password");
                final String email = intent.getStringExtra("email");
                final String registrationId = intent.getStringExtra("registrationId");
                Map<String, String> map = new HashMap<>();
                map.put("username", username);
                map.put("password", password);
                map.put("email", email);
                map.put("registrationId", registrationId);
                signUpTask(map);
            } else if (Constants.ACTION_CHOOSE_SESSION_TYPE.equals(action)) {
                final String sessionId = intent.getStringExtra("sessionId");
                final String sessionType = intent.getStringExtra("sessionType");
                Map<String, String> map = new HashMap<>();
                map.put("sessionId", sessionId);
                map.put("sessionType", sessionType);
                chooseSessionTypeTaskAsync(map);
            } else if (Constants.ACTION_LOGIN.equals(action)) {
                Map<String, String> map = new HashMap<>();
                map.put("username", intent.getStringExtra("username"));
                map.put("password", intent.getStringExtra("password"));
                loginTask(map);
            } else if(Constants.ACTION_REQUEST_OTP.equals(action)) {
                Long phone = Long.parseLong(intent.getStringExtra("phone"));
                int sessionType = Integer.parseInt(intent.getStringExtra("userType"));
                String deviceId = intent.getStringExtra("deviceId");
                requestOneTimePassword(phone, sessionType, deviceId);
            } else if(Constants.ACTION_VERIFY_OTP.equals(action)) {
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
            if(restCallResponse !=null && restCallResponse.getStatus().equalsIgnoreCase("success")) {
                Log.d("SessionIntentService", "OTP request success");
                Intent intent = new Intent(Constants.ACTION_REQUEST_OTP_SUCCESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if(restCallResponse !=null && restCallResponse.getStatus().equalsIgnoreCase("failure")) {
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
            if(restCallResponse !=null && restCallResponse.getStatus().equalsIgnoreCase("success")) {
                Log.d("SessionIntentService", "Phone verified success");
                Intent intent = new Intent(Constants.ACTION_VERIFY_OTP_SUCCESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if(restCallResponse !=null && restCallResponse.getStatus().equalsIgnoreCase("failure")) {
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

    private void signUpTask(Map<String, String> map) {

        /*SessionApi sessionApi = null;
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

        String username = map.get("username");
        String password = map.get("password");
        String email = map.get("email");
        String registrationId = map.get("registrationId");


        try {
            RestCallResponse restCallResponse = sessionApi.register(username, password, email, registrationId, Integer.parseInt(Constants.DEVICE_TYPE)).execute();
            //1. save sign up result in shared prefs
            String signUpStatus;
            if (restCallResponse != null && !restCallResponse.getStatus().equalsIgnoreCase("failure")) {
                if (restCallResponse.getReason() != null && restCallResponse.getReason().equalsIgnoreCase("Could not create session")) {
                    signUpStatus = "fail";
                } else {
                    signUpStatus = "success";
                    PreferenceUtils.saveSession(getApplicationContext(), restCallResponse.getData());
                }
            } else {
                signUpStatus = "fail";
            }
            PreferenceUtils.setPreferences(getApplicationContext(), Constants.KEY_SIGN_UP_STATUS, signUpStatus);

            //2. broadcast sign up result
            Log.d("SessionIntentService", "Sign up result fetched");
            Intent intent = new Intent(Constants.ACTION_SIGN_UP_COMPLETE);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }*/
    }

    private void chooseSessionTypeTaskAsync(Map<String, String> map) {
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

        String sessionId = map.get("sessionId");
        String sessionType = map.get("sessionType");


        try {
            RestCallResponse restCallResponse = sessionApi.chooseSessionType(sessionId, Integer.parseInt(sessionType)).execute();
            //1. Save session to persistant storage if success
            if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("success")) {
                PreferenceUtils.saveSession(getApplicationContext(), restCallResponse.getData());
            } else {
                //session choosing failed
                Log.d("SessionIntentService", "Choose session type request failed");
                Intent intent = new Intent(Constants.ACTION_CHOOSE_SESSION_TYPE_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }

            //2. broadcast choose type result
            Log.d("SessionIntentService", "Choose session type result fetched");
            Intent intent = new Intent(Constants.ACTION_CHOOSE_SESSION_TYPE_COMPLETE);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

    private void loginTask(Map<String, String> map) {

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

        String username = map.get("username");
        String password = map.get("password");
        String registrationId = PreferenceUtils.getRegistrationId(getApplicationContext());


        try {
            //todo check this
            RestCallResponse restCallResponse = null;//sessionApi.login(username, password, registrationId, Integer.parseInt(Constants.DEVICE_TYPE)).execute();
            //1. Save session to persistant storage if success
            if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("success"))          //success
            {
                PreferenceUtils.saveSession(getApplicationContext(), restCallResponse.getData());
            } else if (restCallResponse != null && restCallResponse.getStatus().equalsIgnoreCase("failure")      //fail return;
                    && restCallResponse.getReason().equalsIgnoreCase("Invalid Username or Password")) {
                //invalid username passowrd while logging in
                Log.d("SessionIntentService", "Invalid username and password combination");
                Intent intent = new Intent(Constants.ACTION_LOGIN_INVALID_CREDENTIALS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            } else {                                                                                        //error return;
                //some problem occured while logging in
                Log.d("SessionIntentService", "some problem occured while logging in");
                Intent intent = new Intent(Constants.ACTION_LOGIN_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }

            //2. broadcast login success result
            Log.d("SessionIntentService", "user logged in successfully");
            Intent intent = new Intent(Constants.ACTION_LOGIN_SUCCESS);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

    }

}
