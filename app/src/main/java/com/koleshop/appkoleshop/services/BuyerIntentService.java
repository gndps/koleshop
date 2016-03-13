package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.koleshop.api.buyerEndpoint.BuyerEndpoint;
import com.koleshop.api.buyerEndpoint.model.KoleResponse;
import com.koleshop.api.orderEndpoint.OrderEndpoint;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BuyerIntentService extends IntentService {

    private static final String ACTION_GET_NEARBY_SHOPS = "com.koleshop.appkoleshop.services.action.get_nearby_shops";
    private static final String ACTION_CREATE_NEW_ORDER = "com.koleshop.appkoleshop.services.action.create_new_order";
    private static final String ACTION_GET_MY_ORDERS = "com.koleshop.appkoleshop.services.action.get_my_orders";

//    @Named("customerId") Long customerId, @Named("sessionId") String sessionId,
//    @Named("gpsLong") Double gpsLong, @Named("gpsLat") Double gpsLat, @Named("homeDeliveryOnly") boolean homeDeliveryOnly,
//    @Named("openShopsOnly") boolean openShopsOnly, @Named("limit") int limit, @Named("offset") int offset

    //extras for getNearbyShops
    private static final String EXTRA_HOME_DELIVERY_ONLY = "com.koleshop.appkoleshop.services.extra.home_delivery_only";
    private static final String EXTRA_OPEN_SHOPS_ONLY = "com.koleshop.appkoleshop.services.extra.open_shops_only";
    private static final String EXTRA_LIMIT = "com.koleshop.appkoleshop.services.extra.limit";
    private static final String EXTRA_OFFSET = "com.koleshop.appkoleshop.services.extra.offset";

    //extras for createNewOrder
    private static final String EXTRA_ORDER = "com.koleshop.appkoleshop.services.extra.order";
    private static final String EXTRA_DELIVERY_HOUR = "com.koleshop.appkoleshop.services.extra.delivery_hour";
    private static final String EXTRA_DELIVERY_MINUTE = "com.koleshop.appkoleshop.services.extra.delivery_minute";

    //extras for saveAddress
    private static final String EXTRA_DELIVERY_ADDRESS = "com.koleshop.appkoleshop.services.extra.delivery_address";

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

    public static void createNewOrder(Context context, Order order, int deliveryHour, int deliveryMinute) {
        Intent intent = new Intent(context, BuyerIntentService.class);
        intent.setAction(ACTION_CREATE_NEW_ORDER);
        intent.putExtra(EXTRA_ORDER, Parcels.wrap(order));
        if(!order.isAsap()) {
            intent.putExtra(EXTRA_DELIVERY_HOUR, deliveryHour);
            intent.putExtra(EXTRA_DELIVERY_MINUTE, deliveryMinute);
        }
        context.startService(intent);
    }

    public static void getMyOrders(Context context, int limit, int offset) {
        Intent intent = new Intent(context, BuyerIntentService.class);
        intent.setAction(ACTION_GET_MY_ORDERS);
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
            } else if (ACTION_CREATE_NEW_ORDER.equals(action)) {
                Parcelable parcelableOrder = intent.getParcelableExtra(EXTRA_ORDER);
                Order order = Parcels.unwrap(parcelableOrder);
                Parcelable parcelableDeliveryAddress = intent.getParcelableExtra(EXTRA_DELIVERY_ADDRESS);
                int deliveryHour = 0;
                int deliveryMinute = 0;
                if(order!=null && !order.isAsap()) {
                    deliveryHour = intent.getIntExtra(EXTRA_DELIVERY_HOUR, 0);
                    deliveryMinute = intent.getIntExtra(EXTRA_DELIVERY_MINUTE, 0);
                }
                createNewOrder(order, deliveryHour, deliveryMinute);
            } else if (ACTION_GET_MY_ORDERS.equals(action)) {
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 0);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                getMyOrders(limit, offset);
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

        //Long userId = PreferenceUtils.getUserId(context);
        //String sessionId = PreferenceUtils.getSessionId(context);
        BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
        if(buyerAddress==null) {
            Intent noAddressIntent = new Intent(Constants.ACTION_NO_ADDRESS_SELECTED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(noAddressIntent);
            return;
        }
        Double gpsLong = buyerAddress.getGpsLong();
        Double gpsLat = buyerAddress.getGpsLat();


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
                        sellerSettings.setUserId(Long.valueOf((String) map.get("userId")));
                        ArrayMap<String, Object> addressMap = (ArrayMap<String, Object>) map.get("address");
                        if (addressMap != null) {
                            address.setUserId(Long.valueOf((String) map.get("userId")));
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
                Parcelable parcelableBuyerAddress = Parcels.wrap(buyerAddress);
                intentNearbyShops.putExtra("nearbyShopsList", parcelableListOfNearbyShops);
                intentNearbyShops.putExtra("buyerAddress", parcelableBuyerAddress);
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

    private void createNewOrder(Order order, int deliveryHour, int deliveryMinute) {
        OrderEndpoint endpoint = null;
        OrderEndpoint.Builder builder = new OrderEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        //Long userId = PreferenceUtils.getUserId(context);
        String sessionId = PreferenceUtils.getSessionId(context);

        com.koleshop.api.orderEndpoint.model.KoleResponse result = null;
        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    com.koleshop.api.orderEndpoint.model.Order endpointOrder = KoleshopUtils.getEndpointOrder(order);
                    result = endpoint.createNewOrder(deliveryHour, deliveryMinute, sessionId, endpointOrder).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

            if(result!=null && result.getSuccess()) {
                Log.d(TAG, "created order success");
                ArrayMap<String, Object> resultArrayMap = (ArrayMap<String, Object>) result.getData();
                Log.d(TAG, resultArrayMap.toString());
                Order createdOrder = CloudEndpointDataExtractionUtil.getOrderFromJsonResult(resultArrayMap);
                Intent orderCreatedIntent = new Intent(Constants.ACTION_ORDER_CREATED_SUCCESS);
                orderCreatedIntent.putExtra("orderParcelable", Parcels.wrap(createdOrder));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(orderCreatedIntent);
            } else {
                Log.d(TAG, "problem in creating order");
                Intent orderNotCreatedIntent = new Intent(Constants.ACTION_ORDER_CREATED_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(orderNotCreatedIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

    }

    private void getMyOrders(int limit, int offset) {
        OrderEndpoint endpoint = null;
        OrderEndpoint.Builder builder = new OrderEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        com.koleshop.api.orderEndpoint.model.KoleResponse result = null;
        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = endpoint.getMyOrders(limit, offset, true, sessionId, userId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

            if(result!=null && result.getSuccess()) {
                Log.d(TAG, "fetched my orders");
                if(result.getData() instanceof String) {
                    Intent noOrdersFetchedIntent = new Intent(Constants.ACTION_NO_ORDERS_FETCHED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(noOrdersFetchedIntent);
                } else {
                    ArrayList<ArrayMap<String, Object>> myOrdersJsonList = (ArrayList<ArrayMap<String, Object>>) result.getData();
                    List<Order> myOrdersList = CloudEndpointDataExtractionUtil.getOrdersListFromJsonResult(myOrdersJsonList);
                    Intent ordersListFetchedIntent = new Intent(Constants.ACTION_ORDERS_FETCH_SUCCESS);
                    ordersListFetchedIntent.putExtra("orders", Parcels.wrap(myOrdersList));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(ordersListFetchedIntent);
                }
            } else {
                Log.d(TAG, "problem in fetching my orders");
                Intent intentOrdersListFetchFailed = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentOrdersListFetchFailed);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
            Intent intentOrdersListFetchFailed = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentOrdersListFetchFailed);
        }
    }
}
