package com.kolshop.kolshopmaterial.singletons;

import android.util.Log;

import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCache;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCacheBuilder;
import com.vincentbrison.openlibraries.android.dualcache.lib.SizeOf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gundeep on 22/03/15.
 */
public class KoleshopSingleton {

    public static KoleshopSingleton sharedInstance = null;
    public int defaultPriceMeasuringUnitId;
    public String googleAccessToken;
    public int numberOfVarieties;
    public List<InventoryCategory> inventoryCategories;
    public Map<Long, List<InventoryCategory>> inventorySubcategories;
    public Map<Long, Boolean> inventorySubcatRequestComplete;
    public Map<Long, List<InventoryProduct>> inventoryProducts;
    public Map<Long, Boolean> inventoryProductRequestComplete;
    private DualCache<String> dualCache;
    private DualCache<byte[]> dualCacheByteArray;
    private DualCache<Date> dualCacheDate;

    protected KoleshopSingleton() {

    }

    public static KoleshopSingleton getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new KoleshopSingleton();
        }
        return sharedInstance;
    }

    public int getDefaultPriceMeasuringUnitId() {
        return defaultPriceMeasuringUnitId;
    }

    public void setDefaultPriceMeasuringUnitId(int defaultPriceMeasuringUnitId) {
        this.defaultPriceMeasuringUnitId = defaultPriceMeasuringUnitId;
    }

    public int getNumberOfVarieties() {
        return numberOfVarieties;
    }

    public void setNumberOfVarieties(int numberOfVarieties) {
        this.numberOfVarieties = numberOfVarieties;
    }

    public void increaseNumberOfVarieties() {
        this.numberOfVarieties++;
    }

    public void decreaseNumberOfVarieties() {
        this.numberOfVarieties--;
    }

    public List<InventoryCategory> getInventoryCategories() {
        return inventoryCategories;
    }

    public void setInventoryCategories(List<InventoryCategory> inventoryCategories) {
        this.inventoryCategories = inventoryCategories;
    }

    public void setInventorySubcategoriesForCategoryId(List<InventoryCategory> subcategories, Long categoryId) {
        if (inventorySubcategories == null) {
            inventorySubcategories = new HashMap<>();
        }
        inventorySubcategories.put(categoryId, subcategories);
    }

    public List<InventoryCategory> getInventorySubcategoriesForCategoryId(Long categoryId) {
        if (inventorySubcategories == null) {
            return null;
        } else {
            return inventorySubcategories.get(categoryId);
        }
    }

    public void setInventorySubcatRequestComplete(boolean complete, Long categoryId) {
        if (inventorySubcatRequestComplete == null) {
            inventorySubcatRequestComplete = new HashMap<>();
        }
        inventorySubcatRequestComplete.put(categoryId, complete);
    }

    public boolean getInventorySubcatRequestComplete(Long categoryId) {
        if (inventorySubcatRequestComplete == null) {
            return false;
        } else {
            return inventorySubcatRequestComplete.get(categoryId);
        }
    }

    public void setInventoryProductsForCategoryId(List<InventoryProduct> products, Long categoryId) {
        if (inventoryProducts == null) {
            inventoryProducts = new HashMap<>();
        }
        inventoryProducts.put(categoryId, products);
    }

    public List<InventoryProduct> getInventoryProductsForCategoryId(Long categoryId) {
        if (inventoryProducts == null) {
            return null;
        } else {
            return inventoryProducts.get(categoryId);
        }
    }

    public void setInventoryProductRequestComplete(boolean complete, Long categoryId) {
        if (inventoryProductRequestComplete == null) {
            inventoryProductRequestComplete = new HashMap<>();
        }
        inventoryProductRequestComplete.put(categoryId, complete);
    }

    public boolean getInventoryProductRequestComplete(Long categoryId) {
        if (inventoryProductRequestComplete == null) {
            return false;
        } else {
            return inventoryProductRequestComplete.get(categoryId);
        }
    }

    public DualCache<String> getDualCacheString() {
        if (dualCache == null) {
            dualCache = new DualCacheBuilder<String>(Constants.CACHE_ID, Constants.APP_CACHE_VERSION, String.class)
                    .useReferenceInRam(Constants.RAM_CACHE_SIZE, new SizeOf<String>() {
                        @Override
                        public int sizeOf(String object) {
                            byte[] b = new byte[0];
                            try {
                                b = object.getBytes("UTF-8");
                                return b.length;
                            } catch (UnsupportedEncodingException e) {
                                String archType = System.getProperty("os.arch");
                                if(archType.contains("64")) {
                                    return 36 + object.length() * 2;
                                } else {
                                    return 36 + object.length();
                                }
                            }
                        }
                    })
                    .useDefaultSerializerInDisk(Constants.DISK_CACHE_SIZE, true);
        }
        return dualCache;
    }

    public DualCache<byte[]> getDualCacheByteArray() {
        if (dualCacheByteArray == null) {
            dualCacheByteArray = new DualCacheBuilder<byte[]>(Constants.CACHE_ID, Constants.APP_CACHE_VERSION, byte[].class)
                    .useReferenceInRam(Constants.RAM_CACHE_SIZE, new SizeOf<byte[]>() {
                        @Override
                        public int sizeOf(byte[] object) {
                            return object.length;
                        }
                    })
                    .useDefaultSerializerInDisk(Constants.DISK_CACHE_SIZE, true);
        }
        return dualCacheByteArray;
    }

    public DualCache<Date> getDualCacheDate() {
        if (dualCache == null) {
            dualCacheDate = new DualCacheBuilder<Date>(Constants.CACHE_ID_DATE, Constants.APP_CACHE_VERSION, Date.class)
                    .useReferenceInRam(Constants.RAM_CACHE_SIZE_DATE, new SizeOf<Date>() {
                        @Override
                        public int sizeOf(Date date) {
                            byte[] b = new byte[0];
                            try {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(baos);
                                oos.writeObject(date);
                                oos.flush();
                                byte[] buf = baos.toByteArray();
                                return buf.length;
                            } catch (IOException e) {
                                Log.e("KolSingleton", "error in calculating date size for caching", e);
                            }
                            return sizeOf(date);
                        }
                    })
                    .useDefaultSerializerInDisk(Constants.DISK_CACHE_SIZE_DATE, true);
        }
        return dualCacheDate;
    }

    public String getCachedGsonString(String key, int cacheExpireTimeInMinutes) {
        Date cachingDate = getDualCacheDate().get(key);
        if(cachingDate!=null) {
            long expirationTime = TimeUnit.MILLISECONDS.convert(cacheExpireTimeInMinutes, TimeUnit.MINUTES);
            long timeElapsedAfterCaching = new Date().getTime() - cachingDate.getTime();
            if (timeElapsedAfterCaching >= expirationTime) {
                //the cached data has expired
                return null;
            } else {
                String cachedGsonString = getDualCacheString().get(key);
                return cachedGsonString;
            }
        } else {
            //result for this key was never cached or is cleared
            return null;
        }
    }

    public byte[] getCachedGenericJsonByteArray(String key, int cacheExpireTimeInMinutes) {
        Date cachingDate = getDualCacheDate().get(key);
        if(cachingDate!=null) {
            long expirationTime = TimeUnit.MILLISECONDS.convert(cacheExpireTimeInMinutes, TimeUnit.MINUTES);
            long timeElapsedAfterCaching = new Date().getTime() - cachingDate.getTime();
            if (timeElapsedAfterCaching >= expirationTime) {
                //the cached data has expired
                return null;
            } else {
                byte[] cachedGenericJsonByteArray = getDualCacheByteArray().get(key);
                return cachedGenericJsonByteArray;
            }
        } else {
            //result for this key was never cached or is cleared
            return null;
        }
    }
}
