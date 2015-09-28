package com.kolshop.kolshopmaterial.singletons;

/**
 * Created by Gundeep on 22/03/15.
 */
public class KolShopSingleton {

    public static KolShopSingleton sharedInstance = null;
    public int defaultPriceMeasuringUnitId;

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

}
