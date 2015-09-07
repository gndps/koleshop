package com.kolshop.kolshopmaterial.singletons;

/**
 * Created by Gundeep on 22/03/15.
 */
public class ProductPackageEditSingleton {

    public static ProductPackageEditSingleton sharedInstance = null;
    public String[] propertyArray;
    public int numberOfProperties;

    protected ProductPackageEditSingleton() {

    }

    public static ProductPackageEditSingleton getSharedInstance() {
        if(sharedInstance == null)
        {
            sharedInstance = new ProductPackageEditSingleton();
        }
        return sharedInstance;
    }

    public String[] getPropertyArray() {
        return propertyArray;
    }

    public void setPropertyArray(String[] propertyArray) {
        this.propertyArray = propertyArray;
    }

    public int getNumberOfProperties() {
        return numberOfProperties;
    }

    public void setNumberOfProperties(int numberOfProperties) {
        this.numberOfProperties = numberOfProperties;
    }

}
