package com.koleshop.appkoleshop.model.realm;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 26/01/16.
 */

@Parcel(//implementations = {SellerSettingsRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {ProductVariety.class})
public class ProductVariety extends RealmObject {

    @PrimaryKey
    private Long id;
    private String quantity; //50 gm
    private float price; //Rs. 100
    private String imageUrl;
    private boolean varietyValid;
    private boolean limitedStock;

    public ProductVariety() {
    }

    public ProductVariety(Long id, String quantity, float price, String imageUrl, boolean varietyValid, boolean limitedStock) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.varietyValid = varietyValid;
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

    public boolean isVarietyValid() {
        return varietyValid;
    }

    public void setVarietyValid(boolean varietyValid) {
        this.varietyValid = varietyValid;
    }

    public boolean isLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(boolean limitedStock) {
        this.limitedStock = limitedStock;
    }

}
