package com.koleshop.koleshopbackend.db.models;

/**
 * Created by Gundeep on 02/11/15.
 */
public class InventoryProductVariety {

    Long id;
    String quantity;
    float price;
    String imageUrl;
    //String vegNonVeg;
    boolean valid; // valid
    int limitedStock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /*public String getVegNonVeg() {
        return vegNonVeg;
    }

    public void setVegNonVeg(String vegNonVeg) {
        this.vegNonVeg = vegNonVeg;
    }*/

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setLimitedStock(int limitedStock) {
        this.limitedStock = limitedStock;
    }

    public int getLimitedStock() {
        return limitedStock;
    }
}
