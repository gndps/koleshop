package com.kolshop.kolshopmaterial.helper;

import android.os.Parcel;

import com.kolshop.kolshopmaterial.model.realm.VarietyAttribute;

import org.parceler.ParcelConverter;
import org.parceler.Parcels;

import io.realm.RealmList;

/**
 * Created by Gundeep on 01/10/15.
 */
public class VarietyAttributeParcelConverter implements ParcelConverter<RealmList<VarietyAttribute>> {
    @Override
    public void toParcel(RealmList<VarietyAttribute> input, Parcel parcel) {
        if(input == null) {
            parcel.writeInt(-1);
        } else {
            int size = input.size();
            parcel.writeInt(size);
            for (int i = 0; i < size; i++) {
                parcel.writeParcelable(Parcels.wrap(VarietyAttribute.class, input.get(i)), 0);
            }
        }
    }

    @Override
    public RealmList<VarietyAttribute> fromParcel(Parcel parcel) {
        int size = parcel.readInt();
        if(size<0) return null;
        RealmList<VarietyAttribute> listVarietyAttribute = new RealmList<>();
        for(int i=0;i<size;i++) {
            listVarietyAttribute.add((VarietyAttribute) Parcels.unwrap(parcel.readParcelable(VarietyAttribute.class.getClassLoader())));
        }
        return listVarietyAttribute;
    }
}
