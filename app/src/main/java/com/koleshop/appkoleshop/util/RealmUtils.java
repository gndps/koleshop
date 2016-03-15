package com.koleshop.appkoleshop.util;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.model.realm.ProductCategory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 25/07/15.
 */
public class RealmUtils {

    private static BuyerSettings buyerSettings;

    public static Long getParentCategoryIdForCategoryId(Long categoryId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class)
                .equalTo("id", categoryId);
        ProductCategory categoryTemp = query.findFirst();
        ProductCategory category = null;
        if (categoryTemp != null) {
            category = realm.copyFromRealm(categoryTemp);
        }
        if (category != null && category.getParentCategoryId() > 0l) {
            realm.close();
            return category.getParentCategoryId();
        } else {
            realm.close();
            return null;
        }
    }

    public static BuyerAddress getDefaultUserAddress() {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<BuyerAddress> query = realm.where(BuyerAddress.class)
                .equalTo("defaultAddress", true)
                .equalTo("validAddress", true);
        BuyerAddress realmAddress = query.findFirst();
        BuyerAddress address = null;
        if (realmAddress != null) {
            address = realm.copyFromRealm(realmAddress);
        }
        realm.close();
        return address;
    }

    public static void createBuyerAddress(Double gpsLong, Double gpsLat, boolean defaultAddress) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        BuyerAddress buyerAddress = realm.createObject(BuyerAddress.class);
        buyerAddress.setGpsLong(gpsLong);
        buyerAddress.setGpsLat(gpsLat);
        buyerAddress.setValidAddress(true);
        buyerAddress.setUpdatedDate(new Date());
        buyerAddress.setCountryCode(Constants.DEFAULT_COUNTRY_CODE);
        buyerAddress.setDefaultAddress(defaultAddress);
        buyerAddress.setAddressType(Constants.ADDRESS_TYPE_BUYER);
        realm.commitTransaction();
        realm.close();
    }

    public static void updateBuyerAddress(BuyerAddress buyerAddress) {

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<BuyerAddress> query = realm.where(BuyerAddress.class)
                .equalTo("gpsLong", buyerAddress.getGpsLong())
                .equalTo("gpsLat", buyerAddress.getGpsLat());
        BuyerAddress address = query.findFirst();
        realm.beginTransaction();
        address.setName(buyerAddress.getName());
        if (!TextUtils.isEmpty(buyerAddress.getNickname())) {
            address.setNickname(buyerAddress.getNickname());
        }
        if (buyerAddress.getUserId() != null && buyerAddress.getUserId() > 0l) {
            address.setUserId(buyerAddress.getUserId());
        }
        if (buyerAddress.getId() != null && buyerAddress.getId() > 0l) {
            address.setId(buyerAddress.getId());
        }
        if (buyerAddress.getPhoneNumber() != null && buyerAddress.getPhoneNumber() > 0l) {
            address.setPhoneNumber(buyerAddress.getPhoneNumber());
        }
        if (!TextUtils.isEmpty(buyerAddress.getAddress())) {
            address.setAddress(buyerAddress.getAddress());
        }
        address.setDefaultAddress(buyerAddress.isDefaultAddress());
        realm.commitTransaction();
        realm.close();
    }

    public static void setAddressAsSelected(BuyerAddress selectedBuyerAddress) {
        Realm realm = Realm.getDefaultInstance();

        //deselect all
        RealmQuery<BuyerAddress> query = realm.where(BuyerAddress.class);
        RealmResults<BuyerAddress> addresses = query.findAll();
        boolean clearAllCarts = false;
        realm.beginTransaction();
        for (int i = 0; i < addresses.size(); i++) {
            BuyerAddress buyerAddress = addresses.get(i);
            if (buyerAddress != null) {
                if (buyerAddress.getGpsLat().equals(selectedBuyerAddress.getGpsLat()) && buyerAddress.getGpsLong().equals(selectedBuyerAddress.getGpsLong())) {
                    if (!buyerAddress.isDefaultAddress()) {
                        buyerAddress.setDefaultAddress(true);
                        clearAllCarts = true;
                    }
                } else {
                    buyerAddress.setDefaultAddress(false);
                }
            }
        }
        realm.commitTransaction();
        if (clearAllCarts) {
            CartUtils.clearAllCarts();
        }
        realm.close();
    }

    public static void deleteBuyerAddress(BuyerAddress buyerAddress) {

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<BuyerAddress> query = realm.where(BuyerAddress.class)
                .equalTo("gpsLong", buyerAddress.getGpsLong())
                .equalTo("gpsLat", buyerAddress.getGpsLat());
        BuyerAddress address = query.findFirst();
        boolean clearCart = false;
        if (buyerAddress.isDefaultAddress()) {
            clearCart = true;
        }
        realm.beginTransaction();
        if (address.getId() != null && address.getId() > 0) {
            address.setValidAddress(false);
            address.setUpdatedDate(buyerAddress.getUpdatedDate());
        } else {
            address.removeFromRealm();
        }
        realm.commitTransaction();
        realm.close();
        if (clearCart) {
            CartUtils.clearAllCarts();
        }

    }

    public static List<BuyerAddress> getBuyerAddresses() {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<BuyerAddress> query = realm.where(BuyerAddress.class)
                .equalTo("validAddress", true);
        RealmResults<BuyerAddress> addresses = query.findAll();
        List<BuyerAddress> buyerAddresses = new ArrayList<>();
        for (BuyerAddress buyerAddress : addresses) {
            if (buyerAddress != null) {
                BuyerAddress addressWithoutRealm = realm.copyFromRealm(buyerAddress);
                buyerAddresses.add(addressWithoutRealm);
            }
        }
        realm.close();
        return buyerAddresses;
    }

    public static void resetRealm(Context mContext) {
        String TAG = "RealmUtils";
        try {
            Realm.deleteRealm(new RealmConfiguration.Builder(mContext).name("default.realm").build());
        } catch (Exception e) {
            Log.e(TAG, "Could not close or delete realm", e);
        }
    }

    public static BuyerSettings getBuyerSettings() {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<BuyerSettings> realmQuery = realm.where(BuyerSettings.class);
        BuyerSettings settingsRealm = realmQuery.findFirst();
        if (settingsRealm != null) {
            BuyerSettings buyerSettings = realm.copyFromRealm(settingsRealm);
            return buyerSettings;
        }
        return null;
    }

    public static void saveSellerSettings(SellerSettings sellerSettings) {
        if (sellerSettings != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(sellerSettings);
            realm.commitTransaction();
            realm.close();
        }
    }

    public static SellerSettings getSellerSettings(Context context) {
        Long userId = PreferenceUtils.getUserId(context);
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<SellerSettings> realmQuery = realm.where(SellerSettings.class)
                .equalTo("userId", userId);
        SellerSettings sellerSettingsRealm = realmQuery.findFirst();
        SellerSettings sellerSettings;
        if (sellerSettingsRealm != null) {
            sellerSettings = realm.copyFromRealm(sellerSettingsRealm);
        } else {
            sellerSettings = null;
        }
        realm.close();
        return sellerSettings;
    }

    public static void clearSellerSettings(Context context) {
        Realm realm = Realm.getDefaultInstance();
        Long userId = PreferenceUtils.getUserId(context);
        RealmQuery<SellerSettings> realmQuery = realm.where(SellerSettings.class)
                .equalTo("userId", userId);
        SellerSettings sellerSettingsRealm = realmQuery.findFirst();
        sellerSettingsRealm.removeFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public static void saveBuyerSettings(BuyerSettings buyerSettings) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(buyerSettings);
        realm.commitTransaction();
        realm.close();
    }
}
