package com.koleshop.koleshopbackend.models.db;

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
    boolean limitedStock;

    public InventoryProductVariety() {
    }

    public InventoryProductVariety(Long id, String quantity, float price, String imageUrl, boolean valid, boolean limitedStock) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.valid = valid;
        this.limitedStock = limitedStock;
    }

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

    public boolean isLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(boolean limitedStock) {
        this.limitedStock = limitedStock;
    }
}
