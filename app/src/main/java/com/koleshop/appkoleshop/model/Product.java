package com.koleshop.appkoleshop.model;

import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public class Product implements Parcelable {

    public static final Creator<Product> CREATOR = new Creator<Product>() {

        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
    int id;
    String name;
    String imageUrl;
    float price;
    int measurementUnit;
    String varient;
    String shopId;
    int categoryId;
    int packingUnitId;
    float packingAmount;
    String desc;

    public Product(int id, String name, String imageUrl, float price,
                   int measurementUnit, String varient, String shopId, int categoryId,
                   int packingUnitId, float packingAmount, String desc) {
        super();
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.measurementUnit = measurementUnit;
        this.varient = varient;
        this.shopId = shopId;
        this.categoryId = categoryId;
        this.packingUnitId = packingUnitId;
        this.packingAmount = packingAmount;
        this.desc = desc;
    }

    public Product() {

    }

    /**
     * Retrieving Product data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     */
    private Product(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.imageUrl = in.readString();
        this.price = in.readFloat();
        this.measurementUnit = in.readInt();
        this.varient = in.readString();
        this.shopId = in.readString();
        this.categoryId = in.readInt();
        this.packingUnitId = in.readInt();
        this.packingAmount = in.readFloat();
        this.desc = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getVarient() {
        return varient;
    }

    public void setVarient(String varient) {
        this.varient = varient;
    }

    public int getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(int measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getPackingUnitId() {
        return packingUnitId;
    }

    public void setPackingUnitId(int packingUnitId) {
        this.packingUnitId = packingUnitId;
    }

    public float getPackingAmount() {
        return packingAmount;
    }

    public void setPackingAmount(float packingAmount) {
        this.packingAmount = packingAmount;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeFloat(price);
        dest.writeInt(measurementUnit);
        dest.writeString(varient);
        dest.writeString(shopId);
        dest.writeInt(packingUnitId);
        dest.writeFloat(packingAmount);
        dest.writeString(desc);

    }

    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + ", imageUrl="
                + imageUrl + ", price=" + price + ", measurementUnit="
                + measurementUnit + ", varient=" + varient + ", shopId="
                + shopId + ", categoryId=" + categoryId + ", packingUnitId="
                + packingUnitId + ", packingAmount=" + packingAmount + ", desc="
                + packingAmount + "]";
    }

}
