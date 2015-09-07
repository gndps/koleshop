package com.kolshop.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gndps.kolshopserver.sessionApi.SessionApi;
import com.gndps.kolshopserver.sessionApi.model.RestCallResponse;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;

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
                new SignUpTaskAsync().execute(map);
            }
            else if(Constants.ACTION_CHOOSE_SESSION_TYPE.equals(action))
            {
                final String sessionId = intent.getStringExtra("sessionId");
                final String sessionType = intent.getStringExtra("sessionType");
                Map<String, String> map = new HashMap<>();
                map.put("sessionId", sessionId);
                map.put("sessionType", sessionType);
                new ChooseSessionTypeTaskAsync().execute(map);
            }
            else if(Constants.ACTION_LOGIN.equals(action))
            {
                Map<String, String> map = new HashMap<>();
                map.put("username", intent.getStringExtra("username"));
                map.put("password", intent.getStringExtra("password"));
                new LoginTaskAsync().execute(map);
            }
        }
    }

    class SignUpTaskAsync extends AsyncTask<Map<String, String>, Void, RestCallResponse> {

        private SessionApi sessionApi = null;

        @Override
        protected RestCallResponse doInBackground(Map<String, String>... params) {

            if(sessionApi == null) {
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

            Map<String, String> map = params[0];
            String username = map.get("username");
            String password = map.get("password");
            String email = map.get("email");
            String registrationId = map.get("registrationId");


            try {
                RestCallResponse restCallResponse = sessionApi.register(username, password, email, registrationId, Integer.parseInt(Constants.DEVICE_TYPE)).execute();
                return restCallResponse;
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(RestCallResponse result) {

            //1. save sign up result in shared prefs
            String signUpStatus;
            if(result!=null && !result.getStatus().equalsIgnoreCase("failure"))
            {
                if (result.getReason() != null && result.getReason().equalsIgnoreCase("Could not create session")) {
                    signUpStatus = "fail";
                } else {
                    signUpStatus = "success";
                    PreferenceUtils.saveSession(getApplicationContext(), result.getData());
                }
            }
            else
            {
                signUpStatus = "fail";
            }
            PreferenceUtils.setPreferences(getApplicationContext(), Constants.KEY_SIGN_UP_STATUS, signUpStatus);

            //2. broadcast sign up result
            Log.d("SessionIntentService", "Sign up result fetched");
            Intent intent = new Intent(Constants.ACTION_SIGN_UP_COMPLETE);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        }
    }

    class ChooseSessionTypeTaskAsync extends AsyncTask<Map<String, String>, Void, RestCallResponse> {

        private SessionApi sessionApi = null;

        @Override
        protected RestCallResponse doInBackground(Map<String, String>... params) {
            if(sessionApi == null) {
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

            Map<String, String> map = params[0];
            String sessionId = map.get("sessionId");
            String sessionType = map.get("sessionType");


            try {
                RestCallResponse restCallResponse = sessionApi.chooseSessionType(sessionId, Integer.parseInt(sessionType)).execute();
                return restCallResponse;
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(RestCallResponse restCallResponse) {
            //1. Save session to persistant storage if success
            if(restCallResponse!=null && restCallResponse.getStatus().equalsIgnoreCase("success"))
            {
                PreferenceUtils.saveSession(getApplicationContext(), restCallResponse.getData());
            }
            else
            {
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
        }
    }

    class LoginTaskAsync extends AsyncTask<Map<String, String>, Void, RestCallResponse> {

        private SessionApi sessionApi = null;

        @Override
        protected RestCallResponse doInBackground(Map<String, String>... params) {
            if(sessionApi == null) {
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

            Map<String, String> map = params[0];
            String username = map.get("username");
            String password = map.get("password");
            String registrationId = PreferenceUtils.getRegistrationId(getApplicationContext());


            try {
                RestCallResponse restCallResponse = sessionApi.login(username, password, registrationId, Integer.parseInt(Constants.DEVICE_TYPE)).execute();
                return restCallResponse;
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(RestCallResponse restCallResponse) {
            //1. Save session to persistant storage if success
            if(restCallResponse!=null && restCallResponse.getStatus().equalsIgnoreCase("success"))
            {
                PreferenceUtils.saveSession(getApplicationContext(), restCallResponse.getData());
            }
            else if(restCallResponse!=null && restCallResponse.getStatus().equalsIgnoreCase("failure")
                    && restCallResponse.getReason().equalsIgnoreCase("Invalid Username or Password"))
            {
                //invalid username passowrd while logging in
                Log.d("SessionIntentService", "Invalid username and password combination");
                Intent intent = new Intent(Constants.ACTION_LOGIN_INVALID_CREDENTIALS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }
            else
            {
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
        }
    }

}
