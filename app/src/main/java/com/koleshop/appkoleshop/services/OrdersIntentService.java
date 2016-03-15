package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.koleshop.api.orderEndpoint.OrderEndpoint;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrdersIntentService extends IntentService {

    private static final String ACTION_GET_PENDING_ORDERS = "com.koleshop.appkoleshop.services.action.get_pending_orders";
    private static final String ACTION_GET_INCOMING_ORDERS = "com.koleshop.appkoleshop.services.action.get_incoming_orders";
    private static final String ACTION_GET_COMPLETE_ORDERS = "com.koleshop.appkoleshop.services.action.get_complete_orders";
    private static final String ACTION_UPDATE_ORDER = "com.koleshop.appkoleshop.services.action.update_order";

    private static final String EXTRA_LIMIT = "com.koleshop.appkoleshop.services.extra.limit";
    private static final String EXTRA_OFFSET = "com.koleshop.appkoleshop.services.extra.offset";
    private static final String EXTRA_ORDER_REQUEST_TYPE = "com.koleshop.appkoleshop.services.extra.order_request_type";

    private static final int ORDER_REQUEST_TYPE_PENDING = 0;
    private static final int ORDER_REQUEST_TYPE_INCOMING = 1;
    private static final int ORDER_REQUEST_TYPE_COMPLETE = 2;
    private static final String TAG = "OrdersIntentService";

    public OrdersIntentService() {
        super("OrdersIntentService");
    }

    public static void getPendingOrders(Context context, int limit, int offset) {
        Intent intent = new Intent(context, OrdersIntentService.class);
        intent.setAction(ACTION_GET_PENDING_ORDERS);
        intent.putExtra(EXTRA_LIMIT, limit);
        intent.putExtra(EXTRA_OFFSET, offset);
        intent.putExtra(EXTRA_ORDER_REQUEST_TYPE, ORDER_REQUEST_TYPE_PENDING);
        context.startService(intent);
    }

    public static void getIncomingOrders(Context context) {
        Intent intent = new Intent(context, OrdersIntentService.class);
        intent.setAction(ACTION_GET_PENDING_ORDERS);
        intent.putExtra(EXTRA_ORDER_REQUEST_TYPE, ORDER_REQUEST_TYPE_INCOMING);
        context.startService(intent);
    }

    public static void getCompleteOrders(Context context, int limit, int offset) {
        Intent intent = new Intent(context, OrdersIntentService.class);
        intent.setAction(ACTION_GET_PENDING_ORDERS);
        intent.putExtra(EXTRA_LIMIT, limit);
        intent.putExtra(EXTRA_OFFSET, offset);
        intent.putExtra(EXTRA_ORDER_REQUEST_TYPE, ORDER_REQUEST_TYPE_COMPLETE);
        context.startService(intent);
    }

    public static void updateOrder(Context context, Order acceptOrder) {
        Intent intent = new Intent(context, OrdersIntentService.class);
        intent.setAction(ACTION_UPDATE_ORDER);
        Parcelable parcelableOrder = Parcels.wrap(acceptOrder);
        intent.putExtra("order", parcelableOrder);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_PENDING_ORDERS.equals(action)) {
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 0);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                final int orderRequestType = intent.getIntExtra(EXTRA_ORDER_REQUEST_TYPE, -1);
                fetchOrders(orderRequestType, limit, offset);
            } else if (ACTION_GET_COMPLETE_ORDERS.equals(action)) {
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 0);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                final int orderRequestType = intent.getIntExtra(EXTRA_ORDER_REQUEST_TYPE, -1);
                fetchOrders(orderRequestType, limit, offset);
            } else if (ACTION_GET_INCOMING_ORDERS.equals(action)) {
                final int orderRequestType = intent.getIntExtra(EXTRA_ORDER_REQUEST_TYPE, -1);
                fetchOrders(orderRequestType, 0, 0);
            } else if (ACTION_UPDATE_ORDER.equals(action)) {
                Parcelable parcelableOrder = intent.getParcelableExtra("order");
                Order updateOrder = Parcels.unwrap(parcelableOrder);
                updateOrderRightNow(updateOrder);
            }
        }
    }

    private void fetchOrders(int orderRequestType, int limit, int offset) {
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
                    switch (orderRequestType) {
                        case ORDER_REQUEST_TYPE_INCOMING:
                            result = endpoint.getIncomingOrders(sessionId, userId).execute();
                            break;
                        case ORDER_REQUEST_TYPE_PENDING:
                            result = endpoint.getPendingOrders(limit, offset, true, sessionId, userId).execute();
                            break;
                        case ORDER_REQUEST_TYPE_COMPLETE:
                            result = endpoint.getCompleteOrders(limit, offset, true, sessionId, userId).execute();
                            break;
                    }
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

            if (result != null && result.getSuccess()) {
                Log.d(TAG, "fetched orders - request type = " + orderRequestType);
                if (result.getData() instanceof String) {
                    Intent noOrdersFetchedIntent = new Intent(Constants.ACTION_NO_ORDERS_FETCHED);
                    noOrdersFetchedIntent.putExtra("order_request_type", orderRequestType);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(noOrdersFetchedIntent);
                } else {
                    ArrayList<ArrayMap<String, Object>> myOrdersJsonList = (ArrayList<ArrayMap<String, Object>>) result.getData();
                    List<Order> myOrdersList = CloudEndpointDataExtractionUtil.getOrdersListFromJsonResult(myOrdersJsonList);
                    Intent ordersListFetchedIntent = new Intent(Constants.ACTION_ORDERS_FETCH_SUCCESS);
                    ordersListFetchedIntent.putExtra("orders", Parcels.wrap(myOrdersList));
                    ordersListFetchedIntent.putExtra("order_request_type", orderRequestType);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(ordersListFetchedIntent);
                }
            } else {
                Log.d(TAG, "problem in fetching my orders");
                Intent intentOrdersListFetchFailed = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
                intentOrdersListFetchFailed.putExtra("order_request_type", orderRequestType);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentOrdersListFetchFailed);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
            Intent intentOrdersListFetchFailed = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
            intentOrdersListFetchFailed.putExtra("order_request_type", orderRequestType);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentOrdersListFetchFailed);
        }
    }

    private void updateOrderRightNow(Order order) {
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

        String sessionId = PreferenceUtils.getSessionId(context);

        com.koleshop.api.orderEndpoint.model.KoleResponse result = null;
        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = endpoint.updateOrder(sessionId, KoleshopUtils.getEndpointOrder(order)).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

            if (result != null && result.getSuccess()) {
                Log.d(TAG, "updated order with id = " + order.getId());
                if (result.getData() instanceof String && result.getData().equals("order updated")) {
                    Intent orderUpdatedIntent = new Intent(Constants.ACTION_ORDER_UPDATE_SUCCESS);
                    orderUpdatedIntent.putExtra("order_id", order.getId());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(orderUpdatedIntent);
                } else {
                    Intent orderNotUpdatedIntent = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
                    orderNotUpdatedIntent.putExtra("order_id", order.getId());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(orderNotUpdatedIntent);
                }
            } else {
                Log.d(TAG, "problem in updating order with id = " + order.getId());
                Intent orderNotUpdatedIntent = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(orderNotUpdatedIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
            Intent orderNotUpdatedIntent = new Intent(Constants.ACTION_ORDERS_FETCH_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(orderNotUpdatedIntent);
        }
    }

}
