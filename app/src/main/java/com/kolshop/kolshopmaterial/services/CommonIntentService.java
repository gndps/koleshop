package com.kolshop.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.common.util.RealmUtils;
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.ProductCategory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import kolshopbackend.kolshop.com.commonEndpoint.CommonEndpoint;
import kolshopbackend.kolshop.com.commonEndpoint.model.ProductCategoryCollection;
import kolshopbackend.kolshop.com.commonEndpoint.model.ProductVarietyAttributeMeasuringUnitCollection;

public class CommonIntentService extends IntentService {

    public static final String ACTION_LOAD_PRODUCT_CATEGORIES = "action_load_product_categories";
    public static final String ACTION_LOAD_MEASURING_UNITS = "action_load_measuring_units";
    public static final String TAG = "CommonIntentService";

    public CommonIntentService() {
        super("CommonIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_PRODUCT_CATEGORIES.equals(action) && !Boolean.valueOf(RealmUtils.getRealmPrefs(getApplicationContext(), Constants.FLAG_PRODUCT_CATEGORIES_LOADED))) {
                loadProductCategories();
            }
            else if (ACTION_LOAD_MEASURING_UNITS.equals(action) && !Boolean.valueOf(RealmUtils.getRealmPrefs(getApplicationContext(), Constants.FLAG_MEASURING_UNITS_LOADED))) {
                loadMeasuringUnits();
            }
        }
    }

    private void loadProductCategories() {
        new LoadProductCategoriesTaskAsync().execute(new HashMap<String, String>());
    }
    private void loadMeasuringUnits() {
        new LoadMeasuringUnitsTaskAsync().execute(new HashMap<String, String>());
    }

    class LoadProductCategoriesTaskAsync extends AsyncTask<Map<String, String>, Void, ProductCategoryCollection> {

        private CommonEndpoint commonEndpoint = null;

        @Override
        protected ProductCategoryCollection doInBackground(Map<String, String>... params) {

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

            try {
                ProductCategoryCollection productCategoryCollection = commonEndpoint.getAllProductCategories().execute();
                return productCategoryCollection;
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ProductCategoryCollection result) {

            if(result==null)
            {
                Log.d(TAG, "product categories loading failed");
                Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }

            List<kolshopbackend.kolshop.com.commonEndpoint.model.ProductCategory> productCategories = result.getItems();
            List<ProductCategory> proCats = new ArrayList<>();

            for(kolshopbackend.kolshop.com.commonEndpoint.model.ProductCategory pc : productCategories)
            {
                ProductCategory productCategory = new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentProductCategoryId());
                proCats.add(productCategory);
            }

            if(productCategories!=null && productCategories.size()>0) {
                Realm realm = CommonUtils.getRealmInstance(getApplicationContext());
                realm.beginTransaction();
                realm.copyToRealm(proCats);
                realm.commitTransaction();
                Log.d("SessionIntentService", "product categories fetched");
                Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                RealmUtils.saveRealmPrefs(getApplicationContext(), Constants.FLAG_PRODUCT_CATEGORIES_LOADED, "true");
            }
            else {
                Log.d(TAG, "product categories loading failed 2");
                Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        }
    }

    class LoadMeasuringUnitsTaskAsync extends AsyncTask<Map<String, String>, Void, ProductVarietyAttributeMeasuringUnitCollection> {

        private CommonEndpoint commonEndpoint = null;

        @Override
        protected ProductVarietyAttributeMeasuringUnitCollection doInBackground(Map<String, String>... params) {

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

            try {
                ProductVarietyAttributeMeasuringUnitCollection measuringUnitCollection = commonEndpoint.getMeasuringUnits().execute();
                return measuringUnitCollection;
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ProductVarietyAttributeMeasuringUnitCollection result) {

            if(result==null)
            {
                Log.d(TAG, "measuring units loading failed");
                Intent intent = new Intent(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }

            List<kolshopbackend.kolshop.com.commonEndpoint.model.ProductVarietyAttributeMeasuringUnit> measuringUnits = result.getItems();
            List<MeasuringUnit> mUnits = new ArrayList<>();

            for(kolshopbackend.kolshop.com.commonEndpoint.model.ProductVarietyAttributeMeasuringUnit currentMu : measuringUnits)
            {
                MeasuringUnit mu = new MeasuringUnit(currentMu.getId(), currentMu.getUnitType(), currentMu.getUnit(), currentMu.getBaseUnit(), currentMu.getConversionRate(), currentMu.getUnitFullName());
                mUnits.add(mu);
            }

            if(mUnits!=null && mUnits.size()>0) {
                Realm realm = CommonUtils.getRealmInstance(getApplicationContext());
                realm.beginTransaction();
                realm.copyToRealm(mUnits);
                realm.commitTransaction();
                Log.d("SessionIntentService", "measuring units fetched");
                Intent intent = new Intent(Constants.ACTION_MEASURING_UNITS_LOAD_SUCCESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                RealmUtils.saveRealmPrefs(getApplicationContext(), Constants.FLAG_MEASURING_UNITS_LOADED, "true");
            }
            else {
                Log.d(TAG, "measuring units failed 2");
                Intent intent = new Intent(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        }
    }

}
