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
import com.koleshop.api.sellerEndpoint.SellerEndpoint;
import com.koleshop.api.sellerEndpoint.model.KoleResponse;
import com.koleshop.api.sessionApi.SessionApi;
import com.koleshop.api.sessionApi.model.RestCallResponse;
import com.koleshop.api.yolo.inventoryEndpoint.InventoryEndpoint;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SellerIntentService extends IntentService {

    private static final String ACTION_SHOP_STATUS_TOGGLE = "com.koleshop.appkoleshop.services.action.shop_status_toggle";
    private static final String ACTION_GET_OUT_OF_STOCK_ITEMS = "com.koleshop.appkoleshop.services.action.get_out_of_stock_items";

    private static final String SHOP_STATUS = "com.koleshop.appkoleshop.services.extra.SHOP_STATUS";
    private static final String TAG = "SellerIntentService";

    public SellerIntentService() {
        super("SellerIntentService");
    }

    public static void startActionToggleStatus(Context context, boolean shopStatus) {
        Intent intent = new Intent(context, SellerIntentService.class);
        intent.setAction(ACTION_SHOP_STATUS_TOGGLE);
        intent.putExtra(SHOP_STATUS, shopStatus);
        context.startService(intent);
    }

    public static void getOutOfStockItems(Context context) {
        Intent intent = new Intent(context, SellerIntentService.class);
        intent.setAction(ACTION_GET_OUT_OF_STOCK_ITEMS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SHOP_STATUS_TOGGLE.equals(action)) {
                final boolean shopOpen = intent.getBooleanExtra(SHOP_STATUS, false);
                handleActionShopStatusToggle(shopOpen);
            } else if (ACTION_GET_OUT_OF_STOCK_ITEMS.equals(action)) {
                handleActionGetOutOfStockItems();
            }
        }
    }

    private void handleActionShopStatusToggle(boolean shopOpen) {
        SellerEndpoint sellerEndpoint = null;
        if (sellerEndpoint == null) {
            SellerEndpoint.Builder builder = new SellerEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // use 10.0.2.2 for localhost testing
                    .setRootUrl(Constants.SERVER_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            sellerEndpoint = builder.build();
        }


        try {
            Long userId = PreferenceUtils.getUserId(getApplicationContext());
            String sessionId = PreferenceUtils.getSessionId(getApplicationContext());
            KoleResponse response = sellerEndpoint.openCloseShop(userId, sessionId, shopOpen).execute();
            if (response != null && response.getStatus().equalsIgnoreCase("success")) {
                //shop status updated
                Log.d(TAG, "shop status update success");
                Intent intent = new Intent(Constants.ACTION_SHOP_STATUS_UPDATED_SUCCESS);
                intent.putExtra("isChecked", shopOpen);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                SellerSettings sellerSettings = RealmUtils.getSellerSettings(getApplicationContext());
                if(sellerSettings!=null) {
                    sellerSettings.setShopOpen(shopOpen);
                    RealmUtils.saveSellerSettings(sellerSettings);
                }
            } else {
                //shop status update failed
                Log.d(TAG, "shop status update failed");
                Intent intent = new Intent(Constants.ACTION_SHOP_STATUS_UPDATED_FAILED);
                intent.putExtra("isChecked", shopOpen);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception while updating shop status", e);
            Intent intent = new Intent(Constants.ACTION_SHOP_STATUS_UPDATED_FAILED);
            intent.putExtra("isChecked", shopOpen);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    public void handleActionGetOutOfStockItems() {
        InventoryEndpoint inventoryEndpoint = null;
        if (inventoryEndpoint == null) {
            InventoryEndpoint.Builder builder = new InventoryEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // use 10.0.2.2 for localhost testing
                    .setRootUrl(Constants.SERVER_URL)
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            inventoryEndpoint = builder.build();
        }


        try {
            Long userId = PreferenceUtils.getUserId(getApplicationContext());
            String sessionId = PreferenceUtils.getSessionId(getApplicationContext());
            com.koleshop.api.yolo.inventoryEndpoint.model.KoleResponse response = null;
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    response = inventoryEndpoint.getOutOfStockProductVarieties(sessionId, userId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
            if (response != null && response.getStatus().equalsIgnoreCase("success")) {

                //out of stock fetched
                Log.d(TAG, "out of stock items fetched");

                //if(response.getData() instanceof String && response.getData().equals("no products found")) {
                if(response.getSuccess() && response.getData() == null) {

                    //no items out of stock
                    Intent intent = new Intent(Constants.ACTION_NO_ITEMS_OUT_OF_STOCK);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } else {
                    //extract the out of stock items list
                    ArrayList<ArrayMap<String, Object>> list = (ArrayList<ArrayMap<String, Object>>) response.getData();
                    List<Product> products;

                    //1. parse the response
                    products = CloudEndpointDataExtractionUtil.getProductsList(list, userId, 0l, true);
                    Parcelable parcelableProducts = Parcels.wrap(products);
                    Intent intent = new Intent(Constants.ACTION_OUT_OF_STOCK_FETCH_SUCCESS);
                    intent.putExtra("products", parcelableProducts);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

            } else {
                //out of stock fetch failed
                Log.d(TAG, "out of stock fetch failed");
                Intent intent = new Intent(Constants.ACTION_OUT_OF_STOCK_FETCH_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.e(TAG, "exception while fetching out of stock list", e);
            Intent intent = new Intent(Constants.ACTION_OUT_OF_STOCK_FETCH_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }

}
