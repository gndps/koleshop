package com.koleshop.appkoleshop.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.google.gson.Gson;
import com.koleshop.api.commonEndpoint.CommonEndpoint;
import com.koleshop.api.commonEndpoint.model.KoleResponse;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.NetworkUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsIntentService extends IntentService {

    private static final String ACTION_SAVE_UPDATE_ADDRESS = "com.koleshop.appkoleshop.services.action.SAVE_UPDATE_ADDRESS";
    private static final String ACTION_SAVE_UPDATE_SELLER_SETTINGS = "com.koleshop.appkoleshop.services.action.SAVE_UPDATE_SELLER_SETTINGS";
    private static final String ACTION_GET_SELLER_SETTINGS = "com.koleshop.appkoleshop.services.action.GET_SELLER_SETTINGS";

    private static final String EXTRA_ADDRESS = "com.koleshop.appkoleshop.services.extra.address";
    private static final String EXTRA_SETTINGS = "com.koleshop.appkoleshop.services.extra.settings";

    private static final int RESULT_CODE_SETTINGS_FETCH_SUCCESS = 0x01;
    private static final int RESULT_CODE_SETTINGS_FETCH_FAILED = 0x02;
    private static final int RESULT_CODE_ADDRESS_SAVE_SUCCESS = 0x03;
    private static final int RESULT_CODE_ADDRESS_SAVE_FAILED = 0x04;
    private static final int RESULT_CODE_SETTINGS_SAVE_SUCCESS = 0x05;
    private static final int RESULT_CODE_SETTINGS_SAVE_FAILED = 0x06;


    private static final String TAG = "SettingsIntentService";

    public SettingsIntentService() {
        super("AddressIntentService");
    }

    public static void saveOrUpdateAddress(Context context, Address address, AddressIntentServiceListener listener) {
        Intent intent = new Intent(context, SettingsIntentService.class);
        intent.setAction(ACTION_SAVE_UPDATE_ADDRESS);
        Parcelable addressParcelable = Parcels.wrap(Address.class, address);
        intent.putExtra(EXTRA_ADDRESS, addressParcelable);
        context.startService(intent);
    }

    public static void saveOrUpdateSettings(Context context, SellerSettings settings, String requestId) {
        SettingsIntentServiceListener settingsIntentServiceListener = new SettingsIntentService.SettingsIntentServiceListener(new Handler());
        settingsIntentServiceListener.setReceiver((SettingsReceiver) context);
        Intent intent = new Intent(context, SettingsIntentService.class);
        intent.setAction(ACTION_SAVE_UPDATE_SELLER_SETTINGS);
        intent.putExtra("settingsIntentServiceListener", settingsIntentServiceListener);
        intent.putExtra("requestId", requestId);
        Parcelable settingsParcelable = Parcels.wrap(SellerSettings.class, settings);
        intent.putExtra(EXTRA_SETTINGS, settingsParcelable);
        context.startService(intent);
    }

    public static void requestSellerSettings(Context context, String requestId) {
        SettingsIntentServiceListener settingsIntentServiceListener = new SettingsIntentService.SettingsIntentServiceListener(new Handler());
        settingsIntentServiceListener.setReceiver((SettingsReceiver) context);
        Intent intent = new Intent(context, SettingsIntentService.class);
        intent.setAction(ACTION_GET_SELLER_SETTINGS);
        intent.putExtra("settingsIntentServiceListener", settingsIntentServiceListener);
        intent.putExtra("requestId", requestId);
        context.startService(intent);
    }

    public static void refreshSellerSettings(Context context) {
        Intent intent = new Intent(context, SettingsIntentService.class);
        intent.setAction(ACTION_GET_SELLER_SETTINGS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE_UPDATE_ADDRESS.equals(action)) {
                Parcelable addressParcelable = intent.getParcelableExtra(EXTRA_ADDRESS);
                Address address = Parcels.unwrap(addressParcelable);
                handleActionSaveAddress(address);
            } else if (ACTION_SAVE_UPDATE_SELLER_SETTINGS.equals(action)) {
                if (intent != null && intent.getParcelableExtra("settingsIntentServiceListener") != null) {
                    ResultReceiver listener = intent.getParcelableExtra("settingsIntentServiceListener");
                    Parcelable settingsParcelable = intent.getParcelableExtra(EXTRA_SETTINGS);
                    SellerSettings sellerSettings = Parcels.unwrap(settingsParcelable);
                    String requestId = intent.getStringExtra("requestId");
                    handleActionSaveSettings(sellerSettings, requestId, listener);
                }
            } else if (ACTION_GET_SELLER_SETTINGS.equals(action)) {
                if (intent != null && intent.getParcelableExtra("settingsIntentServiceListener") != null) {
                    ResultReceiver listener = intent.getParcelableExtra("settingsIntentServiceListener");
                    String requestId = intent.getStringExtra("requestId");
                    handleActionGetSettings(listener, requestId);
                } else {
                    handleActionGetSettings(null, null);
                }
            }
        }
    }

    private void handleActionSaveAddress(Address address) {

    }

    private void handleActionSaveSettings(SellerSettings sellerSettings, String uniqueRequestId, ResultReceiver listener) {

        //do network call to get settings
        CommonEndpoint commonEndpoint = null;
        CommonEndpoint.Builder builder = new CommonEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        commonEndpoint = builder.build();

        KoleResponse result = null;

        Context context = getApplicationContext();

        try {
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    Address address = sellerSettings.getAddress();
                    com.koleshop.api.commonEndpoint.model.SellerSettings backendSettings = new com.koleshop.api.commonEndpoint.model.SellerSettings();
                    com.koleshop.api.commonEndpoint.model.Address backendAddress = new com.koleshop.api.commonEndpoint.model.Address();
                    if (address != null) {
                        backendAddress.setId(address.getId());
                        backendAddress.setAddress(address.getAddress());
                        backendAddress.setAddressType(address.getAddressType());
                        backendAddress.setCountryCode(address.getCountryCode());
                        backendAddress.setGpsLat(address.getGpsLat());
                        backendAddress.setGpsLong(address.getGpsLong());
                        backendAddress.setName(address.getName());
                        backendAddress.setPhoneNumber(address.getPhoneNumber());
                        backendAddress.setNickname(address.getNickname());
                        backendAddress.setUserId(userId);
                    }
                    backendSettings.setId(sellerSettings.getId());
                    backendSettings.setUserId(userId);
                    backendSettings.setAddress(backendAddress);
                    backendSettings.setDeliveryCharges(sellerSettings.getDeliveryCharges());
                    backendSettings.setCarryBagCharges(sellerSettings.getCarryBagCharges());
                    backendSettings.setDeliveryEndTime(sellerSettings.getDeliveryEndTime());
                    backendSettings.setDeliveryStartTime(sellerSettings.getDeliveryStartTime());
                    backendSettings.setHomeDelivery(sellerSettings.isHomeDelivery());
                    backendSettings.setMaximumDeliveryDistance(sellerSettings.getMaximumDeliveryDistance());
                    backendSettings.setMinimumOrder(sellerSettings.getMinimumOrder());
                    backendSettings.setPickupFromShop(true);
                    backendSettings.setShopCloseTime(sellerSettings.getShopCloseTime());
                    backendSettings.setShopOpenTime(sellerSettings.getShopOpenTime());

                    result = commonEndpoint.updateSellerSettings(sessionId, backendSettings).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if (result != null && result.getSuccess()) {
            //saved the settings object...extract that shit

            //convert the result to string and save in preferences
            String settingsUpdatedString = (String) result.getData();
            if (settingsUpdatedString.equalsIgnoreCase("settings_updated")) {
                //settings updated successfully
                //update the network request status
                NetworkUtils.setRequestStatusSuccess(context, uniqueRequestId);
                RealmUtils.saveSellerSettings(sellerSettings);
                PreferenceUtils.setPreferencesFlag(this, Constants.FLAG_SELLER_SETTINGS_SETUP_FINISHED, true);
            }

            if (listener != null) {
                //call the success callback
                listener.send(RESULT_CODE_SETTINGS_SAVE_SUCCESS, null);
                Intent refreshSettingsBroadcastIntent = new Intent(Constants.ACTION_REFRESH_SELLER_SETTINGS);
                LocalBroadcastManager.getInstance(context).sendBroadcast(refreshSettingsBroadcastIntent);
            }
        } else {
            //saving failed
            //update the network request status
            NetworkUtils.setRequestStatusFailed(context, uniqueRequestId);

            if (listener != null) {
                //call the failure callback
                listener.send(RESULT_CODE_SETTINGS_SAVE_FAILED, null);
            }
        }

    }

    private void handleActionGetSettings(ResultReceiver listener, String uniqueRequestId) {
        //do network call to get settings
        CommonEndpoint commonEndpoint = null;
        CommonEndpoint.Builder builder = new CommonEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        commonEndpoint = builder.build();

        KoleResponse result = null;

        Context context = getApplicationContext();

        Long userId = PreferenceUtils.getUserId(context);
        String sessionId = PreferenceUtils.getSessionId(context);
        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = commonEndpoint.getSellerSettings(userId, sessionId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if (result != null && result.getSuccess()) {
            //fetched the settings object...extract that shit

            //extract product list from result
            ArrayMap<String, Object> map = (ArrayMap<String, Object>) result.getData();
            SellerSettings sellerSettings = CloudEndpointDataExtractionUtil.getSellerSettings(map);
            RealmUtils.saveSellerSettings(sellerSettings);

            //update the network request status
            NetworkUtils.setRequestStatusSuccess(context, uniqueRequestId);

            if (listener != null) {
                //call the success callback
                listener.send(RESULT_CODE_SETTINGS_FETCH_SUCCESS, null);
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_REFRESH_SELLER_SETTINGS));

        } else if (result != null && !result.getSuccess()) {
            NetworkUtils.setRequestStatusSuccess(context, uniqueRequestId);
            if (listener != null) {
                listener.send(RESULT_CODE_SETTINGS_FETCH_SUCCESS, null);
            }
        } else {
            //fetching failed
            //update the network request status
            NetworkUtils.setRequestStatusFailed(context, uniqueRequestId);

            if (listener != null) {
                //call the failure callback
                listener.send(RESULT_CODE_SETTINGS_FETCH_FAILED, null);
            }
        }
    }

    @SuppressLint("ParcelCreator")
    public static class AddressIntentServiceListener extends ResultReceiver {

        private Receiver mReceiver;

        public AddressIntentServiceListener(Handler handler) {
            super(handler);
        }

        public interface Receiver {
            void onAddressSaveSuccess(Address address);

            void onAddressSaveFailed();
        }

        public void setReceiver(Receiver receiver) {
            mReceiver = receiver;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (mReceiver != null) {
                switch (resultCode) {
                    case RESULT_CODE_ADDRESS_SAVE_SUCCESS:
                        Parcelable addressParcel = resultData.getParcelable("address");
                        Address address = Parcels.unwrap(addressParcel);
                        mReceiver.onAddressSaveSuccess(address);
                        break;
                    case RESULT_CODE_ADDRESS_SAVE_FAILED:
                        mReceiver.onAddressSaveFailed();
                        break;
                }
            }
        }
    }

    @SuppressLint("ParcelCreator")
    public static class SettingsIntentServiceListener extends ResultReceiver {

        SettingsReceiver mReceiver;

        public SettingsIntentServiceListener(Handler handler) {
            super(handler);
        }

        public void setReceiver(SettingsReceiver receiver) {
            mReceiver = receiver;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (mReceiver != null) {
                switch (resultCode) {
                    case RESULT_CODE_SETTINGS_SAVE_SUCCESS:
                        mReceiver.onSettingsSaveSuccess();
                        break;
                    case RESULT_CODE_SETTINGS_SAVE_FAILED:
                        mReceiver.onSettingsSaveFailed();
                        break;
                    case RESULT_CODE_SETTINGS_FETCH_SUCCESS:
                        mReceiver.onSettingsFetchSuccess();
                        break;
                    case RESULT_CODE_SETTINGS_FETCH_FAILED:
                        mReceiver.onSettingsFetchFailed();
                }
            }
        }
    }

    public interface SettingsReceiver {
        void onSettingsSaveSuccess();

        void onSettingsSaveFailed();

        void onSettingsFetchSuccess();

        void onSettingsFetchFailed();
    }
}
