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
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuyerIntentService extends IntentService {

    private static final String ACTION_GET_NEARBY_SHOPS = "com.koleshop.appkoleshop.services.action.get_nearby_shops";
    private static final String ACTION_CREATE_NEW_ORDER = "com.koleshop.appkoleshop.services.action.create_new_order";
    private static final String ACTION_GET_MY_ORDERS = "com.koleshop.appkoleshop.services.action.get_my_orders";
    private static final String ACTION_GET_SHOP = "com.koleshop.appkoleshop.services.action.get_shop";

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

    //extras for saveAddress and getShop
    private static final String EXTRA_DELIVERY_ADDRESS = "com.koleshop.appkoleshop.services.extra.delivery_address";
    private static final String EXTRA_SHOP_ID = "com.koleshop.appkoleshop.services.extra.shop_id";

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
        if (!order.isAsap()) {
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

    public static void getShop(Context context, Long sellerId) {
        Intent intent = new Intent(context, BuyerIntentService.class);
        intent.setAction(ACTION_GET_SHOP);
        intent.putExtra(EXTRA_SHOP_ID, sellerId);
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
                if (order != null && !order.isAsap()) {
                    deliveryHour = intent.getIntExtra(EXTRA_DELIVERY_HOUR, 0);
                    deliveryMinute = intent.getIntExtra(EXTRA_DELIVERY_MINUTE, 0);
                }
                createNewOrder(order, deliveryHour, deliveryMinute);
            } else if (ACTION_GET_MY_ORDERS.equals(action)) {
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 0);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                getMyOrders(limit, offset);
            } else if (ACTION_GET_SHOP.equals(action)) {
                final Long shopId = intent.getLongExtra(EXTRA_SHOP_ID, 0l);
                if (shopId > 0) {
                    //get shop from internet
                    getShop(shopId);
                } else {
                    return;
                }
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
        if (buyerAddress == null) {
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
            if (list != null && list.size() > 0) {
                for (ArrayMap<String, Object> map : list) {
                    if (map != null) {
                        SellerSettings sellerSettings = CloudEndpointDataExtractionUtil.getSellerSettings(map);
                        sellerSettingsList.add(sellerSettings);
                    }
                }
            }

            Intent intentNearbyShops = new Intent(Constants.ACTION_NEARBY_SHOPS_RECEIVE_SUCCESS);

            if (sellerSettingsList != null && sellerSettingsList.size() > 0) {
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

            if (result != null && result.getSuccess()) {
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

            if (result != null && result.getSuccess()) {
                Log.d(TAG, "fetched my orders");
                if (result.getData() instanceof String && ((String) result.getData()).startsWith("No")) {
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

    private void getShop(Long shopId) {
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

        com.koleshop.api.buyerEndpoint.model.KoleResponse result = null;
        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = endpoint.getShop(shopId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

            if (result != null && result.getSuccess()) {
                Log.d(TAG, "fetched shop");
                SellerSettings sellerSettings = CloudEndpointDataExtractionUtil.getSellerSettings((ArrayMap<String, Object>) result.getData());
                Intent shopFetchIntent = new Intent(Constants.ACTION_SHOP_FETCH_SUCCESS);
                shopFetchIntent.putExtra("sellerSettings", Parcels.wrap(sellerSettings));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(shopFetchIntent);
            } else {
                Log.d(TAG, "problem in fetching fav shop");
                Intent shopFetchFailedIntent = new Intent(Constants.ACTION_SHOP_FETCH_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(shopFetchFailedIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
            Intent intentOrdersListFetchFailed = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentOrdersListFetchFailed);
        }
    }

}
