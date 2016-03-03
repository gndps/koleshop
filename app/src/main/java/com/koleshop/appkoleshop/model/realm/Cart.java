package com.koleshop.appkoleshop.model.realm;

import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 02/03/16.
 */
public class Cart extends RealmObject {

    @PrimaryKey
    private Long sellerId;
    private SellerSettings sellerSettings;
    private RealmList<ProductVarietyCount> productVarietyCountList;

    public Cart() {
    }

    public Cart(Long sellerId, SellerSettings sellerSettings, RealmList<ProductVarietyCount> productVarietyCountList) {
        this.sellerId = sellerId;
        this.sellerSettings = sellerSettings;
        this.productVarietyCountList = productVarietyCountList;
    }

    public Cart(SellerSettings sellerSettings) {
        this.sellerId = sellerSettings.getUserId();
        this.sellerSettings = sellerSettings;
        this.productVarietyCountList = new RealmList<>();
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public SellerSettings getSellerSettings() {
        return sellerSettings;
    }

    public void setSellerSettings(SellerSettings sellerSettings) {
        this.sellerSettings = sellerSettings;
    }

    public RealmList<ProductVarietyCount> getProductVarietyCountList() {
        return productVarietyCountList;
    }

    public void setProductVarietyCountList(RealmList<ProductVarietyCount> productVarietyCountList) {
        this.productVarietyCountList = productVarietyCountList;
    }
}
