package com.koleshop.appkoleshop.common.util;

import android.util.Log;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.model.genericjson.GenericJsonListInventoryProduct;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.koleshop.appkoleshop.model.genericjson.GenericJsonListInventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;

import java.util.Date;
import java.util.List;

/**
 * Created by Gundeep on 25/11/15.
 */
public class KoleCacheUtil {

    public static final String TAG = "KoleCacheUtil";

    public static boolean cacheProductsList(List<InventoryProduct> products, Long categoryId, boolean updateDate) {
        GenericJsonListInventoryProduct genericProductsList = new GenericJsonListInventoryProduct();
        genericProductsList.setList(products);
        String key = Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
        try {
            byte[] serializedProducts = SerializationUtil.getSerializableFromGenericJson(genericProductsList);
            KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedProducts);
            if(updateDate) {
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "some problem in serializing products", e);
        }
        return false;
    }

    public static boolean cacheInventorySubcategories(List<InventoryCategory> cats, Long parentCategoryId, boolean updateDate) {
        GenericJsonListInventoryCategory genericSubcategories = new GenericJsonListInventoryCategory();
        genericSubcategories.setList(cats);
        try {
            //cache subcategories and broadcast success
            byte[] serializedSubcategories = SerializationUtil.getSerializableFromGenericJson(genericSubcategories);
            String key = Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
            KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedSubcategories);
            if(updateDate) {
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
            }
            return true;
        } catch (Exception e) {
            //problem in serializing
            Log.e(TAG, "some problem occurred in serializing subcategories", e);
        }
        return false;
    }

    public static boolean cacheInventoryCategories(List<InventoryCategory> cats, boolean updateDate, boolean myInventory) {
        GenericJsonListInventoryCategory genericCategories = new GenericJsonListInventoryCategory();
        genericCategories.setList(cats);
        try {
            String key;
            if(myInventory) {
                key = Constants.CACHE_MY_INVENTORY_CATEGORIES;
            } else {
                key = Constants.CACHE_INVENTORY_CATEGORIES;
            }
            byte[] serializedCategories = SerializationUtil.getSerializableFromGenericJson(genericCategories);
            KoleshopSingleton.getSharedInstance().getDualCacheByteArray().put(key, serializedCategories);
            if(updateDate) {
                KoleshopSingleton.getSharedInstance().getDualCacheDate().put(key, new Date());
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "some problem while serializing category", e);
        }
        return false;
    }

    public static List<InventoryCategory> getCachedInventoryCategories(boolean myInventory) {
        List<InventoryCategory> listOfInventoryCategories = null;
        String cacheKey;
        int cacheTimeToLive;
        if(myInventory) {
            cacheKey = Constants.CACHE_MY_INVENTORY_CATEGORIES;
            cacheTimeToLive = Constants.TIME_TO_LIVE_MY_INV_CAT;
        } else {
            cacheKey = Constants.CACHE_INVENTORY_CATEGORIES;
            cacheTimeToLive = Constants.TIME_TO_LIVE_INV_CAT;
        }
        byte[] cachedGenericJsonByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, cacheTimeToLive);
        if(cachedGenericJsonByteArray==null) {
            return null;
        }
        try {
            GenericJsonListInventoryCategory listCategory = SerializationUtil.getGenericJsonFromSerializable(cachedGenericJsonByteArray, GenericJsonListInventoryCategory.class);
            if(listCategory!=null) {
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
        if(myInventory) {
            cacheKey = Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
            cacheTimeToLive = Constants.TIME_TO_LIVE_MY_INV_SUBCAT;
        } else {
            cacheKey = Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
            cacheTimeToLive = Constants.TIME_TO_LIVE_INV_SUBCAT;
        }
        byte[] cachedSubcategoriesByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, cacheTimeToLive);
        if(cachedSubcategoriesByteArray!=null && cachedSubcategoriesByteArray.length>0) {
            try {
                GenericJsonListInventoryCategory genericJsonListInventoryCategory = SerializationUtil.getGenericJsonFromSerializable(cachedSubcategoriesByteArray, GenericJsonListInventoryCategory.class);
                List<InventoryCategory> subcategories = genericJsonListInventoryCategory.getList();
                if(subcategories!=null && subcategories.size()>0) {
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

}
