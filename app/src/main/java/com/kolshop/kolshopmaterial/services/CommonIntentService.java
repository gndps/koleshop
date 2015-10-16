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
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.ProductCategory;
import com.kolshop.kolshopmaterial.singletons.KolShopSingleton;
import com.kolshop.server.commonEndpoint.CommonEndpoint;
import com.kolshop.server.commonEndpoint.model.ProductCategoryCollection;
import com.kolshop.server.commonEndpoint.model.ProductVarietyAttributeMeasuringUnit;
import com.kolshop.server.commonEndpoint.model.ProductVarietyAttributeMeasuringUnitCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class CommonIntentService extends IntentService {

    public static final String ACTION_LOAD_PRODUCT_CATEGORIES = "action_load_product_categories";
    public static final String ACTION_LOAD_MEASURING_UNITS = "action_load_measuring_units";
    public static final String ACTION_LOAD_BRANDS = "action_load_brands";
    public static final String TAG = "CommonIntentService";
    Realm realm;

    public CommonIntentService() {
        super("CommonIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        realm = Realm.getInstance(getApplicationContext());
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_PRODUCT_CATEGORIES.equals(action) && !PreferenceUtils.getPreferencesFlag(getApplicationContext(), Constants.FLAG_PRODUCT_CATEGORIES_LOADED)) {
                loadProductCategories();
            } else if (ACTION_LOAD_MEASURING_UNITS.equals(action) && !PreferenceUtils.getPreferencesFlag(getApplicationContext(), Constants.FLAG_MEASURING_UNITS_LOADED)) {
                loadMeasuringUnits();
            } else if (ACTION_LOAD_BRANDS.equals(action) && !PreferenceUtils.getPreferencesFlag(getApplicationContext(), Constants.FLAG_BRANDS_LOADED)) {
                loadBrands();
            }
        }
        realm.close();
    }

    private void loadBrands() {
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

        /*try {
            ProductCategoryCollection productCategoryCollection = commonEndpoint.getAllProductCategories().execute();
            return productCategoryCollection;
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
            return null;
        }*/
    }

    private void loadProductCategories() {
        CommonEndpoint commonEndpoint = null;
        if (commonEndpoint == null) {
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
        }

        ProductCategoryCollection result = null;

        try {
            result = commonEndpoint.getAllProductCategories().execute();
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if (result == null) {
            Log.d(TAG, "product categories loading failed");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        List<com.kolshop.server.commonEndpoint.model.ProductCategory> productCategories = result.getItems();
        List<ProductCategory> proCats = new ArrayList<>();

        for (com.kolshop.server.commonEndpoint.model.ProductCategory pc : productCategories) {
            ProductCategory productCategory = new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentProductCategoryId());
            proCats.add(productCategory);
        }

        if (productCategories != null && productCategories.size() > 0) {

            realm.beginTransaction();
            realm.copyToRealm(proCats);
            realm.commitTransaction();
            Log.d("SessionIntentService", "product categories fetched");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            PreferenceUtils.setPreferencesFlag(getApplicationContext(), Constants.FLAG_PRODUCT_CATEGORIES_LOADED, true);
        } else {
            Log.d(TAG, "product categories loading failed 2");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void loadMeasuringUnits() {
        CommonEndpoint commonEndpoint = null;
        if (commonEndpoint == null) {
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
        }

        ProductVarietyAttributeMeasuringUnitCollection result = null;

        try {
            result = commonEndpoint.getMeasuringUnits().execute();
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null) {
            Log.d(TAG, "measuring units loading failed");
            Intent intent = new Intent(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        List<ProductVarietyAttributeMeasuringUnit> measuringUnits = result.getItems();
        List<MeasuringUnit> mUnits = new ArrayList<>();

        for (ProductVarietyAttributeMeasuringUnit currentMu : measuringUnits) {
            MeasuringUnit mu = new MeasuringUnit(currentMu.getId(), currentMu.getUnitType(), currentMu.getUnit(), currentMu.getBaseUnit(), currentMu.getConversionRate(), currentMu.getUnitFullName());
            if (mu.getUnitDimensions().equalsIgnoreCase("price")) {
                KolShopSingleton.getSharedInstance().setDefaultPriceMeasuringUnitId(mu.getId());
            }
            mUnits.add(mu);
        }

        if (mUnits != null && mUnits.size() > 0) {
            realm.beginTransaction();
            realm.copyToRealm(mUnits);
            realm.commitTransaction();
            Log.d("SessionIntentService", "measuring units fetched");
            Intent intent = new Intent(Constants.ACTION_MEASURING_UNITS_LOAD_SUCCESS);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            PreferenceUtils.setPreferencesFlag(getApplicationContext(), Constants.FLAG_MEASURING_UNITS_LOADED, true);
        } else {
            Log.d(TAG, "measuring units failed 2");
            Intent intent = new Intent(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
