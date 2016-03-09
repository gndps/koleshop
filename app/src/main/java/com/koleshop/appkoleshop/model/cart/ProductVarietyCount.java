package com.koleshop.appkoleshop.model.cart;

import com.koleshop.appkoleshop.model.realm.ProductVariety;

import org.parceler.Parcel;

import io.realm.RealmObject;

/**
 * Created by Gundeep on 02/03/16.
 */

@Parcel(//implementations = {ProductVarietyCountRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {ProductVarietyCount.class})
public class ProductVarietyCount extends RealmObject {
    private String title;
    private ProductVariety productVariety;
    private int cartCount;

    public ProductVarietyCount() {
    }

    public ProductVarietyCount(String title, ProductVariety productVariety, int cartCount) {
        this.productVariety = productVariety;
        this.cartCount = cartCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ProductVariety getProductVariety() {
        return productVariety;
    }

    public void setProductVariety(ProductVariety productVariety) {
        this.productVariety = productVariety;
    }

    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }
}
