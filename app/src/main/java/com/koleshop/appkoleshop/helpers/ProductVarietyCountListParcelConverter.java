package com.koleshop.appkoleshop.helpers;

import android.os.Parcel;

import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;

import org.parceler.Parcels;

/**
 * Created by Gundeep on 07/03/16.
 */
public class ProductVarietyCountListParcelConverter extends RealmListParcelConverter<ProductVarietyCount> {

    @Override
    public void itemToParcel(ProductVarietyCount input, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(input), 0);
    }

    @Override
    public ProductVarietyCount itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(ProductVarietyCount.class.getClassLoader()));
    }
}
