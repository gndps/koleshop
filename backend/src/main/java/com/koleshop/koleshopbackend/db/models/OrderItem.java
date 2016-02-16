package com.koleshop.koleshopbackend.db.models;

/**
 * Created by Gundeep on 13/02/16.
 */
public class OrderItem {

    InventoryProductVariety variety;
    String name;
    String brand;
    float price;
    String quantity;
    int orderCount;
    int availableCount;

    public OrderItem() {
    }

    public OrderItem(InventoryProductVariety variety, String name, String brand, float price, String quantity, int orderCount, int availableCount) {
        this.name = name;
        this.brand = brand;
        this.variety = variety;
        this.price = price;
        this.quantity = quantity;
        this.orderCount = orderCount;
        this.availableCount = availableCount;
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

    public InventoryProductVariety getVariety() {
        return variety;
    }

    public void setVariety(InventoryProductVariety variety) {
        this.variety = variety;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
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
