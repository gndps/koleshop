package com.kolshop.kolshopmaterial.helper;

import android.os.Parcel;

import com.kolshop.kolshopmaterial.model.android.AttributeValue;

import org.parceler.ParcelConverter;
import org.parceler.Parcels;

import io.realm.RealmList;

/**
 * Created by Gundeep on 01/10/15.
 */
public class AttributeValueParcelConverter implements ParcelConverter<RealmList<AttributeValue>> {
    @Override
    public void toParcel(RealmList<AttributeValue> input, Parcel parcel) {
        if(input == null) {
            parcel.writeInt(-1);
        } else {
            int size = input.size();
            parcel.writeInt(size);
            for (int i = 0; i < size; i++) {
                parcel.writeParcelable(Parcels.wrap(AttributeValue.class, input.get(i)), 0);
            }
        }
    }

    @Override
    public RealmList<AttributeValue> fromParcel(Parcel parcel) {
        int size = parcel.readInt();
        if(size<0) return null;
        RealmList<AttributeValue> listAttributeValue = new RealmList<>();
        for(int i=0;i<size;i++) {
            listAttributeValue.add((AttributeValue) Parcels.unwrap(parcel.readParcelable(AttributeValue.class.getClassLoader())));
        }
        return listAttributeValue;
    }
}
