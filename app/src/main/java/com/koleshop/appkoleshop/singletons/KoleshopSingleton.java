package com.koleshop.appkoleshop.singletons;

import android.util.Log;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;

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
    private boolean reloadSubcategories;
    private List<Long> reloadProductsCategoryIds;
    private boolean reloadProducts;

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

    public boolean isReloadSubcategories() {
        return reloadSubcategories;
    }

    public void setReloadSubcategories(boolean reloadSubcategories) {
        this.reloadSubcategories = reloadSubcategories;
    }

    public List<Long> getReloadProductsCategoryIds() {
        return reloadProductsCategoryIds;
    }

    public void setReloadProductsCategoryIds(List<Long> reloadProductsCategoryIds) {
        this.reloadProductsCategoryIds = reloadProductsCategoryIds;
    }

    public boolean isReloadProducts() {
        return reloadProducts;
    }

    public void setReloadProducts(boolean reloadProducts) {
        this.reloadProducts = reloadProducts;
    }
}
