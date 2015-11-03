package com.kolshop.kolshopmaterial.singletons;

import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gundeep on 22/03/15.
 */
public class KolShopSingleton {

    public static KolShopSingleton sharedInstance = null;
    public int defaultPriceMeasuringUnitId;
    public String googleAccessToken;
    public int numberOfVarieties;
    public List<InventoryCategory> inventoryCategories;
    public boolean inventoryCategoriesRequestComplete;
    public Map<Long, List<InventoryCategory>> inventorySubcategories;
    public Map<Long, Boolean> inventorySubcatRequestComplete;
    public Map<Long, List<InventoryProduct>> inventoryProducts;
    public Map<Long, Boolean> inventoryProductRequestComplete;

    protected KolShopSingleton() {

    }

    public static KolShopSingleton getSharedInstance() {
        if(sharedInstance == null)
        {
            sharedInstance = new KolShopSingleton();
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

    public boolean isInventoryCategoriesRequestComplete() {
        return inventoryCategoriesRequestComplete;
    }

    public void setInventoryCategoriesRequestComplete(boolean inventoryCategoriesRequestComplete) {
        this.inventoryCategoriesRequestComplete = inventoryCategoriesRequestComplete;
    }

    public void setInventorySubcategoriesForCategoryId(List<InventoryCategory> subcategories, Long categoryId) {
        if(inventorySubcategories==null) {
            inventorySubcategories = new HashMap<>();
        }
        inventorySubcategories.put(categoryId, subcategories);
    }

    public List<InventoryCategory> getInventorySubcategoriesForCategoryId(Long categoryId) {
        if(inventorySubcategories==null) {
            return null;
        } else {
            return inventorySubcategories.get(categoryId);
        }
    }

    public void setInventorySubcatRequestComplete(boolean complete, Long categoryId) {
        if(inventorySubcatRequestComplete==null) {
            inventorySubcatRequestComplete = new HashMap<>();
        }
        inventorySubcatRequestComplete.put(categoryId, complete);
    }

    public boolean getInventorySubcatRequestComplete(Long categoryId) {
        if(inventorySubcatRequestComplete==null) {
            return false;
        } else {
            return inventorySubcatRequestComplete.get(categoryId);
        }
    }

    public void setInventoryProductsForCategoryId(List<InventoryProduct> products, Long categoryId) {
        if(inventoryProducts==null) {
            inventoryProducts = new HashMap<>();
        }
        inventoryProducts.put(categoryId, products);
    }

    public List<InventoryProduct> getInventoryProductsForCategoryId(Long categoryId) {
        if(inventoryProducts==null) {
            return null;
        } else {
            return inventoryProducts.get(categoryId);
        }
    }

    public void setInventoryProductRequestComplete(boolean complete, Long categoryId) {
        if(inventoryProductRequestComplete==null) {
            inventoryProductRequestComplete = new HashMap<>();
        }
        inventoryProductRequestComplete.put(categoryId, complete);
    }

    public boolean getInventoryProductRequestComplete(Long categoryId) {
        if(inventoryProductRequestComplete==null) {
            return false;
        } else {
            return inventoryProductRequestComplete.get(categoryId);
        }
    }
}
