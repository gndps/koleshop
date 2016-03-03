package com.koleshop.appkoleshop.singletons;

import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Cart;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 28/01/16.
 */
public class CartsSingleton {

    public static CartsSingleton sharedInstance = null;

    private List<Cart> carts;

    public static CartsSingleton getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new CartsSingleton();
        }
        return sharedInstance;
    }

    public List<Cart> getCartsFromRealm() {
        Realm realm = Realm.getDefaultInstance();
        List<Cart> carts = new ArrayList<>();
        RealmResults<Cart> realmCarts = realm.where(Cart.class).findAll();
        for(Cart cart : realmCarts) {
            carts.add(realm.copyFromRealm(cart));
        }
        realm.close();
        return carts;
    }

    public Cart getCart(SellerSettings sellerSettings) {

        Cart sellerCart = null;

        loadCarts_fromRealm_toSingleton_ifNeeded();

        for(Cart singleCart : carts) {
            if(singleCart!=null && singleCart.getSellerSettings()!=null && singleCart.getSellerSettings().getUserId().equals(sellerSettings.getUserId())) {
                sellerCart = singleCart;
                break;
            }
        }

        if(sellerCart == null) {
            sellerCart = new Cart(sellerSettings);
            carts.add(sellerCart);
        }

        return sellerCart;
    }

    public void loadCarts_fromRealm_toSingleton_ifNeeded() {
        if(carts==null) {
            carts = getCartsFromRealm();
            if(carts==null) {
                carts = new ArrayList<>();
            }
        }
    }

}
