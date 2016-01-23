package com.koleshop.appkoleshop.util;

import android.util.Log;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.genericjson.GenericJsonListInventoryProduct;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.koleshop.appkoleshop.model.genericjson.GenericJsonListInventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Gundeep on 25/11/15.
 */
public class KoleCacheUtil {

    public static final String TAG = "KoleCacheUtil";
    //todo update all these cached on async task

    public static boolean cacheProductsList(List<InventoryProduct> products, Long categoryId, boolean updateDate, boolean myInventory) {
        GenericJsonListInventoryProduct genericProductsList = new GenericJsonListInventoryProduct();
        genericProductsList.setList(products);
        String key = myInventory ? Constants.CACHE_MY_INVENTORY_PRODUCTS + categoryId : Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
        try {
            byte[] serializedProducts = SerializationUtil.getSerializableFromGenericJson(genericProductsList);
            KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedProducts);
            if (updateDate) {
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "some problem in serializing products", e);
        }
        return false;
    }

    public static boolean invalidateProductsCache(Long categoryId, boolean myInventory) {
        String key = myInventory ? Constants.CACHE_MY_INVENTORY_PRODUCTS + categoryId : Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
        try {
            //KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, null);
            KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, getDateMinusDays(1));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "some problem in invalidating products cache", e);
        }
        return false;
    }

    public static boolean cacheInventorySubcategories(List<InventoryCategory> cats, Long parentCategoryId, boolean updateDate, boolean myInventory) {
        GenericJsonListInventoryCategory genericSubcategories = new GenericJsonListInventoryCategory();
        genericSubcategories.setList(cats);
        try {
            //cache subcategories and broadcast success
            byte[] serializedSubcategories = SerializationUtil.getSerializableFromGenericJson(genericSubcategories);
            String key = myInventory ? Constants.CACHE_MY_INVENTORY_SUBCATEGORIES + parentCategoryId : Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
            KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedSubcategories);
            if (updateDate) {
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
            }
            return true;
        } catch (Exception e) {
            //problem in serializing
            Log.e(TAG, "some problem occurred in serializing subcategories", e);
        }
        return false;
    }

    public static void invalidateInventorySubcategories(Long parentCategoryId, boolean myInventory) {
        String key = myInventory ? Constants.CACHE_MY_INVENTORY_SUBCATEGORIES + parentCategoryId : Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
        KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, getDateMinusDays(1));
    }

    public static boolean cacheInventoryCategories(List<InventoryCategory> cats, boolean updateDate, boolean myInventory) {
        GenericJsonListInventoryCategory genericCategories = new GenericJsonListInventoryCategory();
        genericCategories.setList(cats);
        try {
            String key = myInventory ? Constants.CACHE_MY_INVENTORY_CATEGORIES : Constants.CACHE_INVENTORY_CATEGORIES;
            byte[] serializedCategories = SerializationUtil.getSerializableFromGenericJson(genericCategories);
            KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedCategories);
            if (updateDate) {
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "some problem while serializing category", e);
        }
        return false;
    }

    public static void invalidateInventoryCategories(boolean myInventory) {
        String key = myInventory ? Constants.CACHE_MY_INVENTORY_CATEGORIES : Constants.CACHE_INVENTORY_CATEGORIES;
        KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, getDateMinusDays(1));
    }

    public static List<InventoryCategory> getCachedInventoryCategories(boolean myInventory) {
        List<InventoryCategory> listOfInventoryCategories = null;
        String cacheKey;
        int cacheTimeToLive;
        if (myInventory) {
            cacheKey = Constants.CACHE_MY_INVENTORY_CATEGORIES;
            cacheTimeToLive = Constants.TIME_TO_LIVE_MY_INV_CAT;
        } else {
            cacheKey = Constants.CACHE_INVENTORY_CATEGORIES;
            cacheTimeToLive = Constants.TIME_TO_LIVE_INV_CAT;
        }
        byte[] cachedGenericJsonByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, cacheTimeToLive);
        if (cachedGenericJsonByteArray == null) {
            return null;
        }
        try {
            GenericJsonListInventoryCategory listCategory = SerializationUtil.getGenericJsonFromSerializable(cachedGenericJsonByteArray, GenericJsonListInventoryCategory.class);
            if (listCategory != null) {
                listOfInventoryCategories = listCategory.getList();
            }
        } catch (Exception e) {
            Log.e(TAG, "some problem while deserlzng", e);
            return null;
        }
        return listOfInventoryCategories;
    }

    public static List<InventoryCategory> getCachedSubcategories(boolean myInventory, long parentCategoryId) {
        String cacheKey;
        int cacheTimeToLive;
        if (myInventory) {
            cacheKey = Constants.CACHE_MY_INVENTORY_SUBCATEGORIES + parentCategoryId;
            cacheTimeToLive = Constants.TIME_TO_LIVE_MY_INV_SUBCAT;
        } else {
            cacheKey = Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
            cacheTimeToLive = Constants.TIME_TO_LIVE_INV_SUBCAT;
        }
        byte[] cachedSubcategoriesByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, cacheTimeToLive);
        if (cachedSubcategoriesByteArray != null && cachedSubcategoriesByteArray.length > 0) {
            try {
                GenericJsonListInventoryCategory genericJsonListInventoryCategory = SerializationUtil.getGenericJsonFromSerializable(cachedSubcategoriesByteArray, GenericJsonListInventoryCategory.class);
                List<InventoryCategory> subcategories = genericJsonListInventoryCategory.getList();
                if (subcategories != null && subcategories.size() > 0) {
                    return subcategories;
                } else {
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "some problem occurred in deserializing subcategories", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static List<InventoryProduct> getCachedProducts(boolean myInventory, Long categoryId) {
        String cacheKey = myInventory ? Constants.CACHE_MY_INVENTORY_PRODUCTS + categoryId : Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
        int cacheTimeToLive = myInventory ? Constants.TIME_TO_LIVE_MY_INV_PRODUCT : Constants.TIME_TO_LIVE_INV_PRODUCT;
        byte[] productByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, cacheTimeToLive);
        if (productByteArray != null && productByteArray.length > 0) {
            try {
                GenericJsonListInventoryProduct genericProducts = SerializationUtil.getGenericJsonFromSerializable(productByteArray, GenericJsonListInventoryProduct.class);
                if (genericProducts != null) {
                    List<InventoryProduct> products = genericProducts.getList();
                    return products;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void addNewProductToCache(EditProduct product) {
        InventoryProduct inventoryProduct = KoleshopUtils.getInventoryProductFromEditProduct2(product);

        //cache my inventory (my shop)
        List<InventoryProduct> list = getCachedProducts(true, product.getCategoryId());
        if(list!=null) {
            list.add(inventoryProduct);
            cacheProductsList(list, product.getCategoryId(), true, true);
        }

        //cache ware house
        List<InventoryProduct> wareHouseList = getCachedProducts(false, product.getCategoryId());
        if(wareHouseList!=null) {
            wareHouseList.add(inventoryProduct);
            cacheProductsList(wareHouseList, product.getCategoryId(), true, false);
        } else {

        }
    }

    public static void updateProductInCache(EditProduct product) {
        InventoryProduct inventoryProduct = KoleshopUtils.getInventoryProductFromEditProduct2(product);
        List<InventoryProduct> list = getCachedProducts(true, product.getCategoryId());
        if(list!=null) {
            for (InventoryProduct pro : list) {
                if (pro.getId().longValue() == inventoryProduct.getId().longValue()) {
                    pro.setName(inventoryProduct.getName());
                    pro.setBrand(inventoryProduct.getBrand());
                    pro.setVarieties(inventoryProduct.getVarieties());
                    break;
                }
            }
            cacheProductsList(list, product.getCategoryId(), true, true);
        }
    }

    private static Date getDateMinusDays(int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -1*days);
        Date dt = c.getTime();
        return dt;
    }

}
