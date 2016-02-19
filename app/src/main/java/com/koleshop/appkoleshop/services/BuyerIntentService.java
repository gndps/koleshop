package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.google.gson.Gson;
import com.koleshop.api.buyerEndpoint.BuyerEndpoint;
import com.koleshop.api.buyerEndpoint.model.KoleResponse;
import com.koleshop.api.commonEndpoint.CommonEndpoint;
import com.koleshop.api.yolo.inventoryEndpoint.InventoryEndpoint;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.util.NetworkUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuyerIntentService extends IntentService {

    private static final String ACTION_GET_NEARBY_SHOPS = "com.koleshop.appkoleshop.services.action.get_nearby_shops";

//    @Named("customerId") Long customerId, @Named("sessionId") String sessionId,
//    @Named("gpsLong") Double gpsLong, @Named("gpsLat") Double gpsLat, @Named("homeDeliveryOnly") boolean homeDeliveryOnly,
//    @Named("openShopsOnly") boolean openShopsOnly, @Named("limit") int limit, @Named("offset") int offset

    private static final String EXTRA_HOME_DELIVERY_ONLY = "com.koleshop.appkoleshop.services.extra.home_delivery_only";
    private static final String EXTRA_OPEN_SHOPS_ONLY = "com.koleshop.appkoleshop.services.extra.open_shops_only";
    private static final String EXTRA_LIMIT = "com.koleshop.appkoleshop.services.extra.limit";
    private static final String EXTRA_OFFSET = "com.koleshop.appkoleshop.services.extra.offset";
    private static String TAG = "BuyerIntentService";

    public BuyerIntentService() {
        super("BuyerIntentService");
    }

    public static void getNearbyShops(Context context, boolean homeDeliveryOnly, boolean openShopsOnly, int limit, int offset) {
        Intent intent = new Intent(context, BuyerIntentService.class);
        intent.setAction(ACTION_GET_NEARBY_SHOPS);
        intent.putExtra(EXTRA_HOME_DELIVERY_ONLY, homeDeliveryOnly);
        intent.putExtra(EXTRA_OPEN_SHOPS_ONLY, openShopsOnly);
        intent.putExtra(EXTRA_LIMIT, limit);
        intent.putExtra(EXTRA_OFFSET, offset);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_NEARBY_SHOPS.equals(action)) {
                final boolean homeDeliveryOnly = intent.getBooleanExtra(EXTRA_HOME_DELIVERY_ONLY, false);
                final boolean openShopsOnly = intent.getBooleanExtra(EXTRA_OPEN_SHOPS_ONLY, false);
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 0);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                getNearbyShopsFromInternet(homeDeliveryOnly, openShopsOnly, limit, offset);
            }
        }
    }

    private void getNearbyShopsFromInternet(boolean homeDeliveryOnly, boolean openShopsOnly, int limit, int offset) {

        BuyerEndpoint endpoint = null;
        BuyerEndpoint.Builder builder = new BuyerEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setApplicationName(Constants.APP_NAME)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        endpoint = builder.build();

        Context context = getApplicationContext();

        Long userId = PreferenceUtils.getUserId(context);
        String sessionId = PreferenceUtils.getSessionId(context);
        Double gpsLong = PreferenceUtils.getGpsLong(context);
        Double gpsLat = PreferenceUtils.getGpsLat(context);


        KoleResponse result = null;
        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = endpoint.getNearbyShops(gpsLong, gpsLat, homeDeliveryOnly, openShopsOnly, limit, offset).execute();
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


            ArrayList<ArrayMap<String, Object>> list = (ArrayList<ArrayMap<String, Object>>) result.getData();
            List<SellerSettings> sellerSettingsList = new ArrayList<>();
            if (list != null && list.size()>0) {
                for (ArrayMap<String, Object> map : list) {
                    if (map != null) {
                        SellerSettings sellerSettings = new SellerSettings();
                        Address address = new Address();
                        sellerSettings.setImageUrl((String) map.get("imageUrl"));
                        sellerSettings.setHeaderImageUrl((String) map.get("headerImageUrl"));
                        sellerSettings.setId(Long.valueOf((String) map.get("id")));
                        sellerSettings.setPickupFromShop((Boolean) map.get("pickupFromShop"));
                        sellerSettings.setHomeDelivery((Boolean) map.get("homeDelivery"));
                        sellerSettings.setMinimumOrder(((BigDecimal) map.get("minimumOrder")).floatValue());
                        sellerSettings.setDeliveryCharges(((BigDecimal) map.get("deliveryCharges")).floatValue());
                        sellerSettings.setCarryBagCharges(((BigDecimal) map.get("carryBagCharges")).floatValue());
                        sellerSettings.setMaximumDeliveryDistance(Long.valueOf((String) map.get("maximumDeliveryDistance")));
                        sellerSettings.setDeliveryStartTime(((BigDecimal) map.get("deliveryStartTime")).intValue());
                        sellerSettings.setDeliveryEndTime(((BigDecimal) map.get("deliveryEndTime")).intValue());
                        sellerSettings.setShopOpenTime(((BigDecimal) map.get("shopOpenTime")).intValue());
                        sellerSettings.setShopCloseTime(((BigDecimal) map.get("shopCloseTime")).intValue());
                        sellerSettings.setShopOpen((Boolean) map.get("shopOpen"));
                        sellerSettings.setUserId(userId);
                        ArrayMap<String, Object> addressMap = (ArrayMap<String, Object>) map.get("address");
                        if (addressMap != null) {
                            address.setUserId(userId);
                            address.setId(Long.valueOf((String) addressMap.get("id")));
                            address.setAddress((String) addressMap.get("address"));
                            address.setPhoneNumber(Long.valueOf((String) addressMap.get("phoneNumber")));
                            address.setName((String) addressMap.get("name"));
                            address.setAddressType(((BigDecimal) addressMap.get("addressType")).intValue());
                            address.setCountryCode(((BigDecimal) addressMap.get("countryCode")).intValue());
                            address.setNickname((String) addressMap.get("nickname"));
                            address.setGpsLong(((BigDecimal) addressMap.get("gpsLong")).doubleValue());
                            address.setGpsLat(((BigDecimal) addressMap.get("gpsLat")).doubleValue());
                            sellerSettings.setAddress(address);
                        }
                        sellerSettingsList.add(sellerSettings);
                    }
                }
            }

            Intent intentNearbyShops = new Intent(Constants.ACTION_NEARBY_SHOPS_RECEIVE_SUCCESS);

            if(sellerSettingsList!=null && sellerSettingsList.size()>0) {
                //nearby shops received successfully - now parcel with the broadcast
                Parcelable parcelableListOfNearbyShops = Parcels.wrap(sellerSettingsList);
                intentNearbyShops.putExtra("nearbyShopsList", parcelableListOfNearbyShops);
            }

            intentNearbyShops.putExtra("offset", offset);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentNearbyShops);

        } else {
            //fetching failed
            //update the network request status
            Intent intentNearbyShops = new Intent(Constants.ACTION_NEARBY_SHOPS_RECEIVE_FAILED);
            intentNearbyShops.putExtra("offset", offset);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentNearbyShops);
        }
    }
}
