package com.koleshop.appkoleshop.model;

import org.parceler.Parcel;

/**
 * Created by Gundeep on 23/01/16.
 */

@Parcel
public class OrderItem {


    private Long productVarietyId;
    private String name;
    private String brand;
    private String quantity;
    private float pricePerUnit;
    private String imageUrl;
    private int orderCount;
    private int availableCount;

    public OrderItem() {

    }

    public OrderItem(Long productVarietyId, String name, String brand, String quantity, float pricePerUnit, String imageUrl, int orderCount, int availableCount) {
        this.productVarietyId = productVarietyId;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.imageUrl = imageUrl;
        this.orderCount = orderCount;
        this.availableCount = availableCount;
    }

    public Long getProductVarietyId() {
        return productVarietyId;
    }

    public void setProductVarietyId(Long productVarietyId) {
        this.productVarietyId = productVarietyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public float getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(float pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }
}
