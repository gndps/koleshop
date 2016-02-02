package com.koleshop.appkoleshop.singletons;

import com.koleshop.appkoleshop.model.demo.Cart;
import com.koleshop.appkoleshop.model.demo.SellerInfo;

/**
 * Created by Gundeep on 28/01/16.
 */
public class DemoSingleton {

    public static DemoSingleton sharedInstance = null;

    private Cart cart;

    public static DemoSingleton getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new DemoSingleton();
        }
        return sharedInstance;
    }

    public Cart getCart() {
        if(cart == null) {
            cart = new Cart();
        }
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
