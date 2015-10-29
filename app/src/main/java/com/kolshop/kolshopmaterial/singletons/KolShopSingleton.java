package com.kolshop.kolshopmaterial.singletons;

import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.List;

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
}
