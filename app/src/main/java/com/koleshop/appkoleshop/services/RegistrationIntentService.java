/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.koleshop.api.sessionApi.SessionApi;
import com.koleshop.api.sessionApi.model.KoleResponse;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.Prefs;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    public static final String REGISTRATION_INTENT_SERVICE_ACTION_REGISTER = "register_device";
    public static final String REGISTRATION_INTENT_SERVICE_ACTION_UPDATE_TOKEN_ON_SERVER = "update_token";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction().equalsIgnoreCase(REGISTRATION_INTENT_SERVICE_ACTION_REGISTER)) {
            Log.d(TAG, "will register device with google servers");
            registerDeviceWithGoogle(intent, true); //broadcast when device registered
        } else if (intent.getAction().equalsIgnoreCase(REGISTRATION_INTENT_SERVICE_ACTION_UPDATE_TOKEN_ON_SERVER)) {
            Log.d(TAG, "updating google token on server");
            boolean latestTokenAvailable = PreferenceUtils.getPreferencesFlag(this, Constants.FLAG_LATEST_TOKEN_AVAILABLE);

            if(!latestTokenAvailable) {
                latestTokenAvailable = registerDeviceWithGoogle(intent, false); //don't broadcast device registered
                if (latestTokenAvailable) {
                    updateDeviceIdOnServer();
                } // else retry again on app start
            }
            else {
                updateDeviceIdOnServer();
            }

        }
    }

    private boolean registerDeviceWithGoogle(Intent intent, boolean broadcastRegistrationComplete) {
        PreferenceUtils.setPreferencesFlag(this, Constants.FLAG_DEVICE_ID_SYNCED_TO_SERVER, false);
        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);

            PreferenceUtils.storeRegistrationId(this, token);

            PreferenceUtils.setPreferencesFlag(this, Constants.FLAG_LATEST_TOKEN_AVAILABLE, true);

            //Implement this method to send any registration to your app's servers.
            //sendRegistrationToServer(token);

            // Subscribe to topic channels
            //subscribeTopics(token);

            if (broadcastRegistrationComplete) {
                Intent registrationComplete = new Intent(Constants.ACTION_GCM_REGISTRATION_COMPLETE);
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            if (broadcastRegistrationComplete) {
                Intent registrationComplete = new Intent(Constants.ACTION_GCM_REGISTRATION_FAILED);
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
                return true;
            } else {
                return false;
            }
        }
    }

    private void updateDeviceIdOnServer() {
        SharedPreferences prefs = getSharedPreferences(Prefs.KOLE_PREFS, Context.MODE_PRIVATE);
        String oldRegistrationId = prefs.getString(Constants.KEY_REG_ID_OLD, "");
        String newRegistrationId = prefs.getString(Constants.KEY_REG_ID, "");

        if(oldRegistrationId.isEmpty()) {
            oldRegistrationId = newRegistrationId;
        }

        if (!newRegistrationId.isEmpty()) {
            //call the google cloud endpoints api to update the registration token
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

                boolean thisIsARetry = false;
                boolean keepRetrying = true;
                int delayTime = 2;
                int maxDelayTime = 4;
                while(keepRetrying && delayTime <= maxDelayTime) {

                    if(thisIsARetry) {
                        //sleep for delayTime seconds
                        try {
                            Thread.sleep(1000 * delayTime);
                            delayTime *= 2;
                        } catch (InterruptedException e) {
                            Log.e(TAG, "thread sleep exception", e);
                            delayTime *= 2;
                            continue;
                        }
                    } else {
                        thisIsARetry = true;
                    }

                    String sessionId = PreferenceUtils.getSessionId(this);
                    Long userId = PreferenceUtils.getUserId(this);
                    if(!TextUtils.isEmpty(sessionId) && userId>0) {
                        try {
                            KoleResponse result = sessionApi.updateDeviceUser(sessionId, userId, oldRegistrationId, newRegistrationId).execute();
                            if (result != null && result.getSuccess()) {
                                //device id updated
                                PreferenceUtils.setPreferencesFlag(this, Constants.FLAG_DEVICE_ID_SYNCED_TO_SERVER, true);
                                keepRetrying = false;
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "problem while updating device id", e);
                        }
                    }
                }
            }
        }
    }

    /*
    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    /*private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    /*private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }*/
    // [END subscribe_topics]

}