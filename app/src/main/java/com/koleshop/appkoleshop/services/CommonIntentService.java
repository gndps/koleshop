package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.koleshop.api.commonEndpoint.model.ImageUploadRequest;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.ImageUtils;
import com.koleshop.appkoleshop.util.NetworkUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.model.ProductSelectionRequest;
import com.koleshop.appkoleshop.util.KoleCacheUtil;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.api.commonEndpoint.CommonEndpoint;
import com.koleshop.api.commonEndpoint.model.Brand;
import com.koleshop.api.commonEndpoint.model.BrandCollection;
import com.koleshop.api.commonEndpoint.model.ProductCategoryCollection;
import com.koleshop.api.yolo.inventoryEndpoint.InventoryEndpoint;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.KoleResponse;
import com.koleshop.api.yolo.inventoryEndpoint.model.ProductVarietySelection;
import com.koleshop.appkoleshop.util.ProductUtil;

import org.parceler.Parcels;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class CommonIntentService extends IntentService {

    public static final String ACTION_LOAD_PRODUCT_CATEGORIES = "action_load_product_categories";
    public static final String ACTION_LOAD_BRANDS = "action_load_brands";
    public static final String ACTION_SAVE_FEEDBACK = "action_save_feedback";
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

            } else if (ACTION_LOAD_BRANDS.equals(action) && !PreferenceUtils.getPreferencesFlag(getApplicationContext(), Constants.FLAG_BRANDS_LOADED)) {
                loadBrands();

                //inventory categories
            } else if (Constants.ACTION_FETCH_INVENTORY_CATEGORIES.equals(action)) {
                boolean myInventory = intent.getBooleanExtra("myInventory", false);
                boolean customerView = intent.getBooleanExtra("customerView", false);
                Long sellerId = intent.getLongExtra("sellerId", 0);
                fetchInventoryCategories(myInventory, customerView, sellerId);

                //inventory subcategories
            } else if (Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES.equals(action)) {
                Long categoryId = intent.getLongExtra("categoryId", 0L);
                boolean myInventory = intent.getBooleanExtra("myInventory", false);
                boolean customerView = intent.getBooleanExtra("customerView", false);
                Long sellerId = intent.getLongExtra("sellerId", 0);
                if (categoryId > 0L) {
                    fetchInventorySubcategories(categoryId, myInventory, customerView, sellerId);
                } else {
                    //broadcast failure
                    Intent intent2 = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                }

                //inventory products
            } else if (Constants.ACTION_FETCH_INVENTORY_PRODUCTS.equals(action)) {
                Long categoryId = intent.getLongExtra("categoryId", 0L);
                boolean customerView = intent.getBooleanExtra("customerView", false);
                Long sellerId = intent.getLongExtra("sellerId", 0);
                boolean myInventory = intent.getBooleanExtra("myInventory", false);
                if (categoryId > 0L) {
                    Log.d(TAG, "category id > 0....fetching products from internet");
                    fetchInventoryProductsForCategoryAndUser(categoryId, myInventory, customerView, sellerId);
                } else {
                    //broadcast failure
                    Log.d(TAG, "category id = 0....fetching products from internet");
                    Intent intent2 = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                }

                //inventory product selection
            } else if (Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION.equals(action)) {
                ProductSelectionRequest request = Parcels.unwrap(intent.getParcelableExtra("request"));
                boolean stockMode = intent.getBooleanExtra("stockMode", false);
                //stock mode is true only when a "back in stock" button is clicked in out of stock fragment
                if (stockMode) {
                    backInStock(request);
                } else {
                    updateInventoryProductSelection(request);
                }

                //upload image
            } else if (Constants.ACTION_UPLOAD_IMAGE.equals(action)) {
                String filename = intent.getStringExtra("filename");
                String filepath = intent.getStringExtra("filepath");
                String tag = intent.getStringExtra("tag");
                if (filepath == null || filepath.isEmpty() || filename == null || filename.isEmpty() || tag == null || tag.isEmpty()) {
                    Intent failedIntent = new Intent(Constants.ACTION_UPLOAD_IMAGE_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(failedIntent);
                } else {
                    uploadImageToCloudStorage(filepath, filename, tag);
                }

                //upload profile image
            } else if (Constants.ACTION_UPLOAD_PROFILE_IMAGE.equals(action)) {
                String filename = intent.getStringExtra("filename");
                String filepath = intent.getStringExtra("filepath");
                String tag = intent.getStringExtra("tag");
                boolean isHeaderImage = intent.getBooleanExtra("isHeaderImage", false);
                boolean userIsSeller = intent.getBooleanExtra("userIsSeller", false);
                if (filepath == null || filepath.isEmpty() || filename == null || filename.isEmpty() || tag == null || tag.isEmpty()) {
                    Intent failedIntent = new Intent(Constants.ACTION_UPLOAD_IMAGE_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(failedIntent);
                } else {
                    setUserProfilePicture(filepath, filename, tag, userIsSeller, isHeaderImage);
                }
            } else if (ACTION_SAVE_FEEDBACK.equals(action)) {
                String message = intent.getStringExtra("message");
                String deviceModel = intent.getStringExtra("deviceModel");
                String deviceManufacturer = intent.getStringExtra("deviceManufacturer");
                String osVersion = intent.getStringExtra("osVersion");
                String heightDp = intent.getStringExtra("heightDp");
                String widthDp = intent.getStringExtra("widthDp");
                String screenSize = intent.getStringExtra("screenSize");
                String deviceTime = intent.getStringExtra("deviceTime");
                String sessionType = intent.getStringExtra("sessionType");
                String gpsLat = intent.getStringExtra("gpsLat");
                String gpsLong = intent.getStringExtra("gpsLong");
                String networkName = intent.getStringExtra("networkName");
                String isWifiConnected = intent.getStringExtra("isWifiConnected");
                String userId = intent.getStringExtra("userId");
                String sessionId = intent.getStringExtra("sessionId");
                saveFeedback(message, deviceModel, deviceManufacturer, osVersion, heightDp, widthDp, screenSize, deviceTime, sessionType, gpsLat, gpsLong, networkName, isWifiConnected, userId, sessionId);
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
                .setApplicationName(Constants.APP_NAME)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        commonEndpoint = builder.build();

        BrandCollection result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = commonEndpoint.getAllBrands(userId, sessionId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if (result == null) {
            Log.d(TAG, "product brands loading failed");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_BRANDS_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        List<Brand> brandsList = result.getItems();
        List<com.koleshop.appkoleshop.model.realm.Brand> realmBrands = new ArrayList<>();

        for (com.koleshop.api.commonEndpoint.model.Brand b : brandsList) {
            com.koleshop.appkoleshop.model.realm.Brand brand = new com.koleshop.appkoleshop.model.realm.Brand(b.getId(), b.getName());
            realmBrands.add(brand);
        }

        if (realmBrands != null && realmBrands.size() > 0) {

            realm.beginTransaction();
            realm.copyToRealmOrUpdate(realmBrands);
            realm.commitTransaction();
            Log.d("SessionIntentService", "product brands fetched");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_BRANDS_LOAD_SUCCESS);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            PreferenceUtils.setPreferencesFlag(getApplicationContext(), Constants.FLAG_BRANDS_LOADED, true);
        } else {
            Log.d(TAG, "product brands loading failed 2");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_BRANDS_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void loadProductCategories() {
        CommonEndpoint commonEndpoint = null;
        if (commonEndpoint == null) {
            CommonEndpoint.Builder builder = new CommonEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

            commonEndpoint = builder.build();
        }

        ProductCategoryCollection result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = commonEndpoint.getAllProductCategories(userId, sessionId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if (result == null) {
            Log.d(TAG, "product categories loading failed");
            Intent intent = new Intent(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        List<com.koleshop.api.commonEndpoint.model.ProductCategory> productCategories = result.getItems();
        List<ProductCategory> proCats = new ArrayList<>();

        for (com.koleshop.api.commonEndpoint.model.ProductCategory pc : productCategories) {
            ProductCategory productCategory = new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentProductCategoryId(), 0l);
            proCats.add(productCategory);
        }

        if (productCategories != null && productCategories.size() > 0) {
            try {
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(proCats);
                realm.commitTransaction();
            } catch (Exception e) {
                Log.e(TAG, "exception while copying product categories into realm", e);
            }
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

    public void fetchInventoryCategories(boolean myInventory, boolean customerView, Long sellerId) {
        InventoryEndpoint inventoryEndpoint = null;
        InventoryEndpoint.Builder builder = new InventoryEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        inventoryEndpoint = builder.build();

        KoleResponse result = null;

        Context context = getApplicationContext();
        Long userId = PreferenceUtils.getUserId(context);
        String sessionId = PreferenceUtils.getSessionId(context);
        if (sessionId.isEmpty()) {
            sessionId = " "; //not empty
        }

        //if result fails, then retry 3 times before showing error
        int count = 0;
        int maxTries = 3;
        while (count < maxTries) {
            try {
                result = inventoryEndpoint.getCategories(customerView, myInventory, sellerId, sessionId, userId).execute();
                count = maxTries;
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
                count++;
            }
        }
        if (result == null || !result.getSuccess()) {
            Log.e(TAG, "inventory category loading failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());

            Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED);
            if (result != null && result.getStatus().equalsIgnoreCase(Constants.STATUS_KOLE_RESPONSE_CREATING_INVENTORY)) {
                intent.putExtra("status", Constants.STATUS_KOLE_RESPONSE_CREATING_INVENTORY);
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        } else {
            ArrayList<ArrayMap<String, Object>> list = (ArrayList<ArrayMap<String, Object>>) result.getData();
            List<ProductCategory> cats = new ArrayList<>();
            if (list != null) {
                for (ArrayMap<String, Object> map : list) {
                    if (map != null) {
                        ProductCategory cat = new ProductCategory();
                        cat.setId(Long.valueOf((String) map.get("id")));
                        cat.setSellerId(sellerId);
                        cat.setName((String) map.get("name"));
                        cat.setDesc((String) map.get("desc"));
                        cat.setImageUrl((String) map.get("imageUrl"));
                        cat.setSortOrder(((BigDecimal) map.get("sortOrder")).intValue());
                        cat.setAddedToMyShop(myInventory);
                        cat.setParentCategoryId(0l);
                        if (myInventory) {
                            cat.setMyShopUpdateDate(new Date());
                        } else {
                            cat.setWarehouseUpdateDate(new Date());
                        }
                        cats.add(cat);
                    }
                }
            }

            //cache response and send success broadcast
            if (cats != null && cats.size() > 0) {
                Log.d(TAG, "inventory cateogires fetched");
                boolean cachedCategories = KoleCacheUtil.cacheCategoriesInRealm(cats);
                if (cachedCategories) {
                    Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } else {
                    //broadcast failure
                    Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            } else if (cats != null) {
                Log.d(TAG, "fetch inventory categories empty");
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_EMPTY);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                Log.d(TAG, "inventory categories fetch failed");
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }

    public void fetchInventorySubcategories(Long categoryId, boolean myInventory, boolean customerView, Long sellerId) {
        InventoryEndpoint inventoryEndpoint = null;
        InventoryEndpoint.Builder builder = new InventoryEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        inventoryEndpoint = builder.build();

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            if (sessionId.isEmpty()) {
                sessionId = "something";
            }
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = inventoryEndpoint.getSubcategories(userId, sessionId, categoryId, myInventory, customerView, sellerId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
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

        ArrayList<ArrayMap<String, Object>> list = (ArrayList<ArrayMap<String, Object>>) result.getData();
        List<ProductCategory> cats = new ArrayList<>();
        if (list != null) {
            for (ArrayMap<String, Object> map : list) {
                if (map != null) {
                    ProductCategory cat = new ProductCategory();
                    cat.setId(Long.valueOf((String) map.get("id")));
                    cat.setSellerId(sellerId);
                    cat.setName((String) map.get("name"));
                    cat.setSortOrder(((BigDecimal) map.get("sortOrder")).intValue());
                    cat.setAddedToMyShop(myInventory);
                    if (myInventory) {
                        cat.setMyShopUpdateDate(new Date());
                    } else {
                        cat.setWarehouseUpdateDate(new Date());
                    }
                    cat.setParentCategoryId(categoryId);
                    cats.add(cat);
                }
            }
        }

        //cache categories and broadcast result success/failure
        if (cats != null && cats.size() > 0) {
            Log.d(TAG, "inventory subcateogires fetched");
            boolean categoriesCached = KoleCacheUtil.cacheCategoriesInRealm(cats);
            if (categoriesCached) {
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS);
                intent.putExtra("catId", categoryId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                //broadcast failure
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

    public void fetchInventoryProductsForCategoryAndUser(Long categoryId, boolean myInventory, boolean customerView, Long sellerId) {
        InventoryEndpoint inventoryEndpoint = null;
        InventoryEndpoint.Builder builder = new InventoryEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        inventoryEndpoint = builder.build();

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = inventoryEndpoint.getProductsForCategoryAndUser(categoryId, customerView, myInventory, sellerId, sessionId, userId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
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


        //extract product list from result
        ArrayList<ArrayMap<String, Object>> list = (ArrayList<ArrayMap<String, Object>>) result.getData();
        List<Product> products;

        //1. parse the response
        products = CloudEndpointDataExtractionUtil.getProductsList(list, sellerId, categoryId, myInventory);

        //2. Cache the parsed response and broadcast success
        if (products != null && products.size() > 0) {
            Log.d(TAG, "products fetch SUCCESS for category id " + categoryId + ".");
            boolean productsCached;
            //complementary date doesn't exist because the products are loaded from internet
            productsCached = KoleCacheUtil.cacheProductsListInRealm(products, false, false);
            if (productsCached) {
                Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_SUCCESS);
                intent.putExtra("catId", categoryId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                Log.e(TAG, "some problem in serializing products");
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

    private void updateInventoryProductSelection(ProductSelectionRequest productSelectionRequest) {
        InventoryEndpoint inventoryEndpoint = null;
        InventoryEndpoint.Builder builder = new InventoryEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        inventoryEndpoint = builder.build();

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            ProductVarietySelection productVarietySelection = new ProductVarietySelection();
            if (productSelectionRequest.isWillSelectOnSuccess()) {
                productVarietySelection.setSelectProductIds(productSelectionRequest.getProductVarietyIds());
            } else {
                productVarietySelection.setDeselectProductIds(productSelectionRequest.getProductVarietyIds());
            }

            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = inventoryEndpoint.updateProductSelection(sessionId, userId, productVarietySelection).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            //result is null - request failed
            Log.e(TAG, "product variety updating failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE);
            Parcelable wrapped = Parcels.wrap(productSelectionRequest);
            intent.putExtra("request", wrapped);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //result is cool - request success
            Intent intent = new Intent(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS);
            Parcelable wrapped = Parcels.wrap(productSelectionRequest);
            intent.putExtra("request", wrapped);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void backInStock(ProductSelectionRequest productSelectionRequest) {
        InventoryEndpoint inventoryEndpoint = null;
        InventoryEndpoint.Builder builder = new InventoryEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        inventoryEndpoint = builder.build();

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);

            Long varietyId = productSelectionRequest.getProductVarietyIds().get(0);

            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = inventoryEndpoint.backInStock(sessionId, userId, varietyId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            //result is null - request failed
            Log.e(TAG, "product variety back in stock failed");
            Intent intent = new Intent(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE);
            Parcelable wrapped = Parcels.wrap(productSelectionRequest);
            intent.putExtra("request", wrapped);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //result is cool - request success
            Intent intent = new Intent(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS);
            Parcelable wrapped = Parcels.wrap(productSelectionRequest);
            intent.putExtra("request", wrapped);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void uploadImageToCloudStorage(String filepath, String filename, String tag) {
        CommonEndpoint commonEndpoint = null;
        CommonEndpoint.Builder builder = new CommonEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        commonEndpoint = builder.build();


        com.koleshop.api.commonEndpoint.model.KoleResponse result = null;
        Context context = getApplicationContext();

        try {
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            byte[] imageByteArray = ImageUtils.getImageByteArrayForUpload(filepath);
            ImageUploadRequest imageUploadRequest = new ImageUploadRequest();
            if (!filename.startsWith("IMG_") || !filename.endsWith("_" + userId + ".jpg") || !filename.endsWith("_" + userId + ".png")) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String random6 = CommonUtils.randomString(6);
                filename = "IMG" + "_" + timeStamp + "_" + random6 + "_" + userId + ".jpg";
            }
            imageUploadRequest.setFileName(filename);
            if (filename.toLowerCase().endsWith(".png")) {
                imageUploadRequest.setMimeType("image/png");
            } else {
                imageUploadRequest.setMimeType("image/jpeg");
            }
            String imageString = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
            imageUploadRequest.setImageData(imageString);

            //following prefs are set because of a case...when product_variety_1 image is uploading...but ProductActivity
            //has stopped listening to broadcasts because the user has opened camera to capture product_variety_2 image
            NetworkUtils.setRequestStatusProcessing(context, tag);

            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = commonEndpoint.uploadImage(userId, sessionId, imageUploadRequest).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            //result is null - upload failed
            Log.e(TAG, "image uploading failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_UPLOAD_IMAGE_FAILED);
            intent.putExtra("tag", tag);
            intent.putExtra("filename", filename);
            //the following prefs will be deleted if this broadcast is received by the product_edit_activity
            NetworkUtils.setRequestStatusFailed(context, tag);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //image upload success
            Intent intent = new Intent(Constants.ACTION_UPLOAD_IMAGE_SUCCESS);
            intent.putExtra("tag", tag);
            intent.putExtra("filename", filename);
            //the following prefs will be deleted if this broadcast is received by the product_edit_activity
            NetworkUtils.setRequestStatusSuccess(context, tag);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void setUserProfilePicture(String filepath, String filename, String tag, boolean userIsSeller, boolean isHeaderImage) {
        CommonEndpoint commonEndpoint = null;
        CommonEndpoint.Builder builder = new CommonEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        commonEndpoint = builder.build();


        com.koleshop.api.commonEndpoint.model.KoleResponse result = null;
        Context context = getApplicationContext();

        try {
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            byte[] imageByteArray = ImageUtils.getImageByteArrayForUpload(filepath);
            ImageUploadRequest imageUploadRequest = new ImageUploadRequest();
            if (!filename.startsWith("IMG_") || !filename.endsWith("_" + userId + ".jpg") || !filename.endsWith("_" + userId + ".png")) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String random6 = CommonUtils.randomString(6);
                filename = "IMG" + "_" + timeStamp + "_" + random6 + "_" + userId + ".jpg";
            }
            imageUploadRequest.setFileName(filename);
            if (filename.toLowerCase().endsWith(".png")) {
                imageUploadRequest.setMimeType("image/png");
            } else {
                imageUploadRequest.setMimeType("image/jpeg");
            }
            String imageString = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
            imageUploadRequest.setImageData(imageString);

            //following prefs are set because of a case...when image is uploading...but calling activity
            //has stopped listening to broadcasts because of an incoming call
            NetworkUtils.setRequestStatusProcessing(context, tag);

            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = commonEndpoint.setUserProfileImage(userId, sessionId, userIsSeller, isHeaderImage, imageUploadRequest).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            //result is null - upload failed
            Log.e(TAG, "image uploading failed");
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_UPLOAD_IMAGE_FAILED);
            intent.putExtra("tag", tag);
            intent.putExtra("filename", filename);
            //the following prefs will be deleted if this broadcast is received by the product_edit_activity
            NetworkUtils.setRequestStatusFailed(context, tag);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //image upload success
            Intent intent = new Intent(Constants.ACTION_UPLOAD_IMAGE_SUCCESS);
            intent.putExtra("tag", tag);
            intent.putExtra("filename", filename);
            //the following prefs will be deleted if this broadcast is received by the product_edit_activity
            NetworkUtils.setRequestStatusSuccess(context, tag);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void saveFeedback(String message, String deviceModel, String deviceManufacturer, String osVersion,
                              String heightDp, String widthDp, String screenSize, String deviceTime, String sessionType,
                              String gpsLat, String gpsLong, String networkName, String isWifiConnected, String userId, String sessionId) {
        CommonEndpoint commonEndpoint = null;
        CommonEndpoint.Builder builder = new CommonEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
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

        commonEndpoint = builder.build();


        com.koleshop.api.commonEndpoint.model.KoleResponse result = null;
        Context context = getApplicationContext();

        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = commonEndpoint.saveFeedback(message, deviceModel, deviceManufacturer, osVersion, heightDp, widthDp, screenSize, deviceTime, sessionType, gpsLong, gpsLat, networkName, isWifiConnected, userId, sessionId).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        if (result == null || !result.getSuccess()) {
            //save feedback failed
            Log.e(TAG, "save feedback failed");
            Intent intent = new Intent(Constants.ACTION_SAVE_FEEDBACK_FAILED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //success
            Intent intent = new Intent(Constants.ACTION_SAVE_FEEDBACK_SUCCESS);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    public static void saveFeedback(Context context, String message, String deviceModel, String deviceManufacturer, String osVersion,
                                    String heightDp, String widthDp, String screenSize, String deviceTime, String sessionType,
                                    String gpsLat, String gpsLong, String networkName, String isWifiConnected, String userId, String sessionId) {
        Intent intent = new Intent(context, CommonIntentService.class);
        intent.setAction(ACTION_SAVE_FEEDBACK);
        intent.putExtra("message", message!=null?message:"");
        intent.putExtra("deviceModel", deviceModel!=null?deviceModel:"");
        intent.putExtra("deviceManufacturer", deviceManufacturer!=null?deviceManufacturer:"");
        intent.putExtra("osVersion", osVersion!=null?osVersion:"");
        intent.putExtra("heightDp", heightDp!=null?heightDp:"");
        intent.putExtra("widthDp", widthDp!=null?widthDp:"");
        intent.putExtra("screenSize", screenSize!=null?screenSize:"");
        intent.putExtra("deviceTime", deviceTime!=null?deviceTime:"");
        intent.putExtra("sessionType", sessionType!=null?sessionType:"");
        intent.putExtra("gpsLat", gpsLat!=null?gpsLat:"");
        intent.putExtra("gpsLong", gpsLong!=null?gpsLong:"");
        intent.putExtra("networkName", networkName!=null?networkName:"");
        intent.putExtra("isWifiConnected", isWifiConnected!=null?isWifiConnected:"");
        intent.putExtra("userId", userId!=null?userId:"");
        intent.putExtra("sessionId", sessionId!=null?sessionId:"");
        context.startService(intent);
    }

}
