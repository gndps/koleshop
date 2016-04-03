package com.koleshop.appkoleshop.model.realm;

import com.koleshop.appkoleshop.helpers.ProductVarietyCountListParcelConverter;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;

import org.parceler.Parcel;
import org.parceler.ParcelProperty;
import org.parceler.ParcelPropertyConverter;
import org.parceler.Parcels;
import org.parceler.converter.CollectionParcelConverter;

import io.realm.CartRealmProxy;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 02/03/16.
 */
@Parcel(implementations = {CartRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Cart.class})
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

    @ParcelProperty("productVarietyCountList")
    @ParcelPropertyConverter(ProductVarietyCountListParcelConverter.class)
    public void setProductVarietyCountList(RealmList<ProductVarietyCount> productVarietyCountList) {
        this.productVarietyCountList = productVarietyCountList;
    }

}
