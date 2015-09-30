package com.kolshop.kolshopmaterial.helper;

import android.os.Parcel;

import com.kolshop.kolshopmaterial.model.android.ProductVariety;

import org.parceler.ParcelConverter;
import org.parceler.Parcels;

import io.realm.RealmList;

/**
 * Created by Gundeep on 30/09/15.
 */
public class ProductVarietyParcelConverter implements ParcelConverter<RealmList<ProductVariety>> {
    @Override
    public void toParcel(RealmList<ProductVariety> input, Parcel parcel) {
        if(input == null) {
            parcel.writeInt(-1);
        } else {
            int size = input.size();
            parcel.writeInt(size);
            for (int i = 0; i < size; i++) {
                parcel.writeParcelable(Parcels.wrap(ProductVariety.class, input.get(i)), 0);
            }
        }
    }

    @Override
    public RealmList<ProductVariety> fromParcel(Parcel parcel) {
        int size = parcel.readInt();
        if(size<0) return null;
        RealmList<ProductVariety> listProductVariety = new RealmList<>();
        for(int i=0;i<size;i++) {
            listProductVariety.add((ProductVariety) Parcels.unwrap(parcel.readParcelable(ProductVariety.class.getClassLoader())));
        }
        return listProductVariety;
    }
}
