package com.kolshop.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.common.util.SerializationUtil;
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.ProductCategory;
import com.kolshop.kolshopmaterial.model.genericjson.GenericJsonListInventoryCategory;
import com.kolshop.kolshopmaterial.model.genericjson.GenericJsonListInventoryProduct;
import com.kolshop.kolshopmaterial.singletons.KoleshopSingleton;
import com.kolshop.server.commonEndpoint.CommonEndpoint;
import com.kolshop.server.commonEndpoint.model.ProductCategoryCollection;
import com.kolshop.server.commonEndpoint.model.ProductVarietyAttributeMeasuringUnit;
import com.kolshop.server.commonEndpoint.model.ProductVarietyAttributeMeasuringUnitCollection;
import com.kolshop.server.yolo.inventoryEndpoint.InventoryEndpoint;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.kolshop.server.yolo.inventoryEndpoint.model.KolResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
            } else if (Constants.ACTION_FETCH_INVENTORY_CATEGORIES.equals(action)) {
                fetchInventoryCategories();
            } else if (Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES.equals(action)) {
                Long categoryId = intent.getLongExtra("categoryId", 0L);
                if (categoryId > 0L) {
                    fetchInventorySubcategories(categoryId);
                } else {
                    //broadcast failure
                    Intent intent2 = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                }
            } else if (Constants.ACTION_FETCH_INVENTORY_PRODUCTS.equals(action)) {
                Long categoryId = intent.getLongExtra("categoryId", 0L);
                if (categoryId > 0L) {
                    fetchInventoryProductsForCategoryAndUser(categoryId);
                } else {
                    //broadcast failure
                    Intent intent2 = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                }
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
                KoleshopSingleton.getSharedInstance().setDefaultPriceMeasuringUnitId(mu.getId());
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

    public void fetchInventoryCategories() {
        InventoryEndpoint inventoryEndpoint = null;
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

        KolResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = Long.parseLong(PreferenceUtils.getPreferences(context, Constants.KEY_USER_ID));
            String sessionId = PreferenceUtils.getPreferences(context, Constants.KEY_SESSION_ID);
            result = inventoryEndpoint.getCategories(userId, sessionId).execute();
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            Log.e(TAG, "inventory category loading failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());

            Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        } else {
            ArrayList<ArrayMap<String, String>> list = (ArrayList<ArrayMap<String, String>>) result.getData();
            List<InventoryCategory> cats = new ArrayList<>();
            for (ArrayMap<String, String> map : list) {
                if (map != null) {
                    InventoryCategory cat = new InventoryCategory();
                    cat.setId(Long.valueOf(map.get("id")));
                    cat.setName(map.get("name"));
                    cat.setDesc(map.get("desc"));
                    cat.setImageUrl(map.get("imageUrl"));
                    cats.add(cat);
                }
            }

            if (cats != null && cats.size() > 0) {
                Log.d(TAG, "inventory cateogires fetched");
                GenericJsonListInventoryCategory genericCategories = new GenericJsonListInventoryCategory();
                genericCategories.setList(cats);
                try {
                    //cache response and send success broadcast
                    byte[] serializedCategories = SerializationUtil.getSerializableFromGenericJson(genericCategories);
                    KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(Constants.CACHE_INVENTORY_CATEGORIES, serializedCategories);
                    KoleshopSingleton.getSharedInstance().getDualCacheDate().put(Constants.CACHE_INVENTORY_CATEGORIES, new Date());
                    Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (Exception e) {
                    //broadcast failure
                    Log.e(TAG, "some problem while serializing category", e);
                    Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            } else {
                Log.d(TAG, "inventory cateogires fetch failed");
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }

    public void fetchInventorySubcategories(Long categoryId) {
        InventoryEndpoint inventoryEndpoint = null;
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

        KolResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = Long.parseLong(PreferenceUtils.getPreferences(context, Constants.KEY_USER_ID));
            String sessionId = PreferenceUtils.getPreferences(context, Constants.KEY_SESSION_ID);
            result = inventoryEndpoint.getSubcategories(userId, sessionId, categoryId).execute();
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            Log.e(TAG, "inventory subcategories loading failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED);
            intent.putExtra("catId", categoryId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        ArrayList<ArrayMap<String, String>> list = (ArrayList<ArrayMap<String, String>>) result.getData();
        List<InventoryCategory> cats = new ArrayList<>();
        for (ArrayMap<String, String> map : list) {
            if (map != null) {
                InventoryCategory cat = new InventoryCategory();
                cat.setId(Long.valueOf(map.get("id")));
                cat.setName(map.get("name"));
                cats.add(cat);
            }
        }

        if (cats != null && cats.size() > 0) {
            Log.d(TAG, "inventory subcateogires fetched");
            GenericJsonListInventoryCategory genericSubcategories = new GenericJsonListInventoryCategory();
            genericSubcategories.setList(cats);
            try {
                //cache subcategories and broadcast success
                byte[] serializedSubcategories = SerializationUtil.getSerializableFromGenericJson(genericSubcategories);
                String key = Constants.CACHE_INVENTORY_SUBCATEGORIES + categoryId;
                KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedSubcategories);
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS);
                intent.putExtra("catId", categoryId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } catch (Exception e) {
                //broadcast failure
                Log.e(TAG, "some problem occurred in serializing subcategories", e);
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED);
                intent.putExtra("catId", categoryId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        } else {
            Log.d(TAG, "inventory subcateogires fetch failed");
            Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED);
            intent.putExtra("catId", categoryId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }

    public void fetchInventoryProductsForCategoryAndUser(Long categoryId) {
        InventoryEndpoint inventoryEndpoint = null;
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

        KolResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = Long.parseLong(PreferenceUtils.getPreferences(context, Constants.KEY_USER_ID));
            String sessionId = PreferenceUtils.getPreferences(context, Constants.KEY_SESSION_ID);
            result = inventoryEndpoint.getProductsForCategoryAndUser(categoryId, sessionId, userId).execute();
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            Log.e(TAG, "products for category id " + categoryId + "loading failed");

            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());

            Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED);
            intent.putExtra("catId", categoryId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        ArrayList<ArrayMap<String, Object>> list = (ArrayList<ArrayMap<String, Object>>) result.getData();
        List<InventoryProduct> products = new ArrayList<>();
        if (list != null) {
            for (ArrayMap<String, Object> map : list) {
                if (map != null) {
                    InventoryProduct prod = new InventoryProduct();
                    prod.setId(Long.valueOf((String) map.get("id")));
                    prod.setName((String) map.get("name"));
                    prod.setDescription((String) map.get("description"));
                    prod.setBrand((String) map.get("brand"));
                    prod.setAdditionalInfo((String) map.get("additionalInfo"));
                    prod.setAdditionalInfo((String) map.get("specialDescription"));
                    prod.setPrivateToUser(Boolean.valueOf((Boolean) map.get("privateToUser")));
                    prod.setSelectedByUser(Boolean.valueOf((Boolean) map.get("selectedByUser")));
                    ArrayList<ArrayMap<String, Object>> varieties = (ArrayList<ArrayMap<String, Object>>) map.get("varieties");
                    List<InventoryProductVariety> inventoryProductVarieties = new ArrayList<>();
                    for (ArrayMap<String, Object> variety : varieties) {
                        InventoryProductVariety invProVar = new InventoryProductVariety();
                        invProVar.setId(Long.valueOf((String) variety.get("id")));
                        invProVar.setQuantity((String) variety.get("quantity"));
                        invProVar.setPrice(((BigDecimal) variety.get("price")).floatValue());
                        invProVar.setImageUrl((String) variety.get("imageUrl"));
                        invProVar.setVegNonVeg((String) variety.get("vegNonVeg"));
                        invProVar.setSelected((Boolean) variety.get("selected"));
                        inventoryProductVarieties.add(invProVar);
                    }
                    prod.setVarieties(inventoryProductVarieties);
                    products.add(prod);
                }
            }
        }

        if (products != null && products.size() > 0) {
            Log.d(TAG, "products fetch SUCCESS for category id " + categoryId + ".");
            GenericJsonListInventoryProduct genericProductsList = new GenericJsonListInventoryProduct();
            genericProductsList.setList(products);
            String key = Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
            try {
                byte[] serializedProducts = SerializationUtil.getSerializableFromGenericJson(genericProductsList);
                KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedProducts);
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_SUCCESS);
                intent.putExtra("catId", categoryId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } catch (Exception e) {
                Log.e(TAG, "some problem in serializing products", e);
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED);
                intent.putExtra("catId", categoryId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        } else {
            Log.d(TAG, "no products exist for category id " + categoryId + ".");
            Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED);
            intent.putExtra("catId", categoryId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }

}
