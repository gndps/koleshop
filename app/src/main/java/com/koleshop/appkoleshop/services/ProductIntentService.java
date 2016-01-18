package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import com.google.api.client.util.ArrayMap;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.gson.Gson;
import com.koleshop.api.productEndpoint.ProductEndpoint;
import com.koleshop.api.productEndpoint.model.InventoryProduct;
import com.koleshop.api.productEndpoint.model.InventoryProductVariety;
import com.koleshop.api.productEndpoint.model.KoleResponse;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.KoleshopUtils;
import com.koleshop.appkoleshop.common.util.NetworkUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ProductIntentService extends IntentService {

    private static String ACTION_SAVE_PRODUCT = "save_the_product_dude";

    private String TAG = "ProductIntentService";

    public ProductIntentService() {
        super("ProductIntentService");
    }

    public static void saveProduct(Context context, EditProduct product, String requestTag) {
        Intent intent = new Intent(context, ProductIntentService.class);
        intent.setAction(ACTION_SAVE_PRODUCT);
        Parcelable wrappedProduct = Parcels.wrap(product);
        intent.putExtra("product", wrappedProduct);
        intent.putExtra("requestTag", requestTag);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE_PRODUCT.equals(action)) {
                Parcelable productParcel = intent.getParcelableExtra("product");
                String requestTag = intent.getStringExtra("requestTag");
                EditProduct product = Parcels.unwrap(productParcel);
                saveProduct(product, requestTag);
            }
        }
    }

    private void saveProduct(EditProduct product, String requestTag) {
        ProductEndpoint productEndpoint;
        ProductEndpoint.Builder builder = new ProductEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        Context context = getApplicationContext();
        productEndpoint = builder.build();
        KoleResponse result = null;

        NetworkUtils.setRequestStatusProcessing(context, requestTag);

        try {
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    //prepare data
                    InventoryProduct inventoryProduct = KoleshopUtils.getInventoryProductFromEditProduct(product);
                    //make product save request
                    result = productEndpoint.saveProduct(userId, sessionId, product.getCategoryId(), inventoryProduct).execute();
                    count = maxTries; //should retry only when there is an exception
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            Log.e(TAG, "product save failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());

            NetworkUtils.setRequestStatusFailed(context, requestTag);

            Intent intent = new Intent(Constants.ACTION_PRODUCT_SAVE_FAILED);
            intent.putExtra("requestTag", requestTag);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else {
            NetworkUtils.setRequestStatusSuccess(context, requestTag);

            //save result in shared prefs
            Gson gson = new Gson();
            String resultSavedProduct = gson.toJson(result);
            PreferenceUtils.setPreferences(context, "savedProduct", resultSavedProduct);

            /*ArrayMap<String, Object> map = (ArrayMap<String, Object>) result.getData();
            InventoryProduct savedProduct = (InventoryProduct) map.get("data");
            EditProduct receivedProduct = new EditProduct(savedProduct, product.getCategoryId());
            Parcelable parcelableProduct = Parcels.wrap(receivedProduct);*/

            Intent intent = new Intent(Constants.ACTION_PRODUCT_SAVE_SUCCESS);
            //intent.putExtra("savedProduct", parcelableProduct);
            intent.putExtra("requestTag", requestTag);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
