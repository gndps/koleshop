package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.ImageUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.model.ProductSelectionRequest;
import com.koleshop.appkoleshop.common.util.KoleCacheUtil;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.api.commonEndpoint.CommonEndpoint;
import com.koleshop.api.commonEndpoint.model.Brand;
import com.koleshop.api.commonEndpoint.model.BrandCollection;
import com.koleshop.api.commonEndpoint.model.ProductCategoryCollection;
import com.koleshop.api.yolo.inventoryEndpoint.InventoryEndpoint;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.koleshop.api.yolo.inventoryEndpoint.model.KoleResponse;
import com.koleshop.api.yolo.inventoryEndpoint.model.ProductVarietySelection;

import org.parceler.Parcels;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class CommonIntentService extends IntentService {

    public static final String ACTION_LOAD_PRODUCT_CATEGORIES = "action_load_product_categories";
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

            } else if (ACTION_LOAD_BRANDS.equals(action) && !PreferenceUtils.getPreferencesFlag(getApplicationContext(), Constants.FLAG_BRANDS_LOADED)) {
                loadBrands();

                //inventory categories
            } else if (Constants.ACTION_FETCH_INVENTORY_CATEGORIES.equals(action)) {
                boolean myInventory = intent.getBooleanExtra("myInventory", false);
                fetchInventoryCategories(myInventory);

                //inventory subcategories
            } else if (Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES.equals(action)) {
                Long categoryId = intent.getLongExtra("categoryId", 0L);
                boolean myInventory = intent.getBooleanExtra("myInventory", false);
                if (categoryId > 0L) {
                    fetchInventorySubcategories(categoryId, myInventory);
                } else {
                    //broadcast failure
                    Intent intent2 = new Intent(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                }

                //inventory products
            } else if (Constants.ACTION_FETCH_INVENTORY_PRODUCTS.equals(action)) {
                Long categoryId = intent.getLongExtra("categoryId", 0L);
                boolean myInventory = intent.getBooleanExtra("myInventory", false);
                if (categoryId > 0L) {
                    fetchInventoryProductsForCategoryAndUser(categoryId, myInventory);
                } else {
                    //broadcast failure
                    Intent intent2 = new Intent(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                }

                //inventory product selection
            } else if (Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION.equals(action)) {
                ProductSelectionRequest request = Parcels.unwrap(intent.getParcelableExtra("request"));
                updateInventoryProductSelection(request);

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
            realm.copyToRealm(realmBrands);
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

    public void fetchInventoryCategories(boolean myInventory) {
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

        KoleResponse result = null;

        Context context = getApplicationContext();
        Long userId = PreferenceUtils.getUserId(context);
        String sessionId = PreferenceUtils.getSessionId(context);

        //if result fails, then retry 3 times before showing error
        int count = 0;
        int maxTries = 3;
        while (count < maxTries) {
            try {
                result = inventoryEndpoint.getCategories(myInventory, sessionId, userId).execute();
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
            ArrayList<ArrayMap<String, String>> list = (ArrayList<ArrayMap<String, String>>) result.getData();
            List<InventoryCategory> cats = new ArrayList<>();
            if (list != null) {
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
            }

            //cache response and send success broadcast
            if (cats != null && cats.size() > 0) {
                Log.d(TAG, "inventory cateogires fetched");
                boolean cachedCategories = KoleCacheUtil.cacheInventoryCategories(cats, true, myInventory);
                if (cachedCategories) {
                    Intent intent = new Intent(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } else {
                    //broadcast failure
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

    public void fetchInventorySubcategories(Long categoryId, boolean myInventory) {
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

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = inventoryEndpoint.getSubcategories(userId, sessionId, categoryId, myInventory).execute();
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

        ArrayList<ArrayMap<String, String>> list = (ArrayList<ArrayMap<String, String>>) result.getData();
        List<InventoryCategory> cats = new ArrayList<>();
        if (list != null) {
            for (ArrayMap<String, String> map : list) {
                if (map != null) {
                    InventoryCategory cat = new InventoryCategory();
                    cat.setId(Long.valueOf(map.get("id")));
                    cat.setName(map.get("name"));
                    cats.add(cat);
                }
            }
        }

        //cache categories and broadcast result success/failure
        if (cats != null && cats.size() > 0) {
            Log.d(TAG, "inventory subcateogires fetched");
            boolean categoriesCached = KoleCacheUtil.cacheInventorySubcategories(cats, categoryId, true);
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

    public void fetchInventoryProductsForCategoryAndUser(Long categoryId, boolean myInventory) {
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

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = inventoryEndpoint.getProductsForCategoryAndUser(categoryId, myInventory, sessionId, userId).execute();
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
        List<InventoryProduct> products = new ArrayList<>();
        if (list != null) {
            for (ArrayMap<String, Object> map : list) {
                if (map != null) {
                    InventoryProduct prod = new InventoryProduct();
                    prod.setId(Long.valueOf((String) map.get("id")));
                    prod.setName((String) map.get("name"));
                    //prod.setDescription((String) map.get("description"));
                    prod.setBrand((String) map.get("brand"));
                    //prod.setAdditionalInfo((String) map.get("additionalInfo"));
                    //prod.setAdditionalInfo((String) map.get("specialDescription"));
                    //prod.setPrivateToUser(Boolean.valueOf((Boolean) map.get("privateToUser")));
                    //prod.setSelectedByUser(Boolean.valueOf((Boolean) map.get("selectedByUser")));
                    ArrayList<ArrayMap<String, Object>> varieties = (ArrayList<ArrayMap<String, Object>>) map.get("varieties");
                    List<InventoryProductVariety> inventoryProductVarieties = new ArrayList<>();
                    for (ArrayMap<String, Object> variety : varieties) {
                        InventoryProductVariety invProVar = new InventoryProductVariety();
                        invProVar.setId(Long.valueOf((String) variety.get("id")));
                        invProVar.setQuantity((String) variety.get("quantity"));
                        invProVar.setPrice(((BigDecimal) variety.get("price")).floatValue());
                        invProVar.setImageUrl((String) variety.get("imageUrl"));
                        //invProVar.setVegNonVeg((String) variety.get("vegNonVeg"));
                        invProVar.setSelected((Boolean) variety.get("selected"));
                        invProVar.setLimitedStock(((BigDecimal) variety.get("limitedStock")).intValue());
                        inventoryProductVarieties.add(invProVar);
                    }
                    prod.setVarieties(inventoryProductVarieties);
                    products.add(prod);
                }
            }
        }

        //cache response and broadcast success
        if (products != null && products.size() > 0) {
            Log.d(TAG, "products fetch SUCCESS for category id " + categoryId + ".");
            String key = Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
            boolean productsCached = KoleCacheUtil.cacheProductsList(products, categoryId, true);
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

    private void uploadImageToCloudStorage(String filepath, String filename, String tag) {
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


        com.koleshop.api.commonEndpoint.model.KoleResponse result = null;
        Context context = getApplicationContext();

        try {
            Long userId = PreferenceUtils.getUserId(context);
            String sessionId = PreferenceUtils.getSessionId(context);
            byte[] imageByteArray = ImageUtils.getImageByteArrayForUpload(filepath);
            ImageUploadRequest imageUploadRequest = new ImageUploadRequest();
            if(!filename.startsWith("IMG_") || !filename.endsWith("_" + userId + ".jpg") || !filename.endsWith("_" + userId + ".png")) {
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
            PreferenceUtils.setPreferences(context, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, "uploading");

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
            PreferenceUtils.setPreferences(context, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, "upload_failed");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //image upload success
            Intent intent = new Intent(Constants.ACTION_UPLOAD_IMAGE_SUCCESS);
            intent.putExtra("tag", tag);
            intent.putExtra("filename", filename);
            //the following prefs will be deleted if this broadcast is received by the product_edit_activity
            PreferenceUtils.setPreferences(context, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, "upload_success");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

}
