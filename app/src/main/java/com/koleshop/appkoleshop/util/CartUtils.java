package com.koleshop.appkoleshop.util;

import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.singletons.CartsSingleton;

import java.util.List;

import io.realm.RealmList;

/**
 * Created by Gundeep on 02/03/16.
 */
public class CartUtils {

    public static void increaseCount(ProductVariety productVariety, SellerSettings sellerSettings) {
        Cart cart = CartsSingleton.getSharedInstance().getCart(sellerSettings);
        RealmList<ProductVarietyCount> list = cart.getProductVarietyCountList();
        boolean countIncreased = false;
        for(ProductVarietyCount provarcount : list) {
            ProductVariety currentProductVariety = provarcount.getProductVariety();
            if(currentProductVariety!=null && currentProductVariety.getId().equals(productVariety.getId())) {
                provarcount.setCartCount(provarcount.getCartCount()+1);
                countIncreased = true;
                break;
            }
        }
        if(!countIncreased) {
            ProductVarietyCount productVarietyCount = new ProductVarietyCount(productVariety, 1);
            list.add(productVarietyCount);
            cart.setProductVarietyCountList(list);
        }
    }

    public static void decreaseCount(ProductVariety productVariety, SellerSettings sellerSettings) {
        Cart cart = CartsSingleton.getSharedInstance().getCart(sellerSettings);
        List<ProductVarietyCount> list = cart.getProductVarietyCountList();
        boolean countDecreased = false;
        for(ProductVarietyCount provarcount : list) {
            ProductVariety currentProductVariety = provarcount.getProductVariety();
            if(currentProductVariety!=null && currentProductVariety.getId().equals(productVariety.getId())) {
                if(provarcount.getCartCount() <= 1) {
                    list.remove(provarcount);
                } else {
                    provarcount.setCartCount(provarcount.getCartCount()-1);
                }
                countDecreased = true;
                break;
            }
        }
    }

    public static int getCountOfVariety(ProductVariety productVariety, SellerSettings sellerSettings) {
        Cart cart = CartsSingleton.getSharedInstance().getCart(sellerSettings);
        List<ProductVarietyCount> list = cart.getProductVarietyCountList();
        int count = 0;
        for(ProductVarietyCount provarcount : list) {
            ProductVariety currentProductVariety = provarcount.getProductVariety();
            if(currentProductVariety!=null && currentProductVariety.getId().equals(productVariety.getId())) {
                if(provarcount.getCartCount() >= 1) {
                    count = provarcount.getCartCount();
                }
                break;
            }
        }
        return count;
    }

}
