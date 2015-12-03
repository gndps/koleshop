package com.koleshop.appkoleshop.helper;

import android.support.annotation.Nullable;

import com.koleshop.appkoleshop.model.realm.VarietyAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 05/10/15.
 */
public class VarietyAttributePool {

    List<VarietyAttribute> varietyAttributes;
    static VarietyAttributePool mInstance;

    public static VarietyAttributePool getInstance() {
        if(mInstance==null) {
            mInstance = new VarietyAttributePool();
        }
        return mInstance;
    }

    public void reset()
    {
        mInstance = new VarietyAttributePool();
    }

    public VarietyAttribute addVarietyAttribute(VarietyAttribute varietyAttribute) {
        if(varietyAttributes==null) {
            varietyAttributes = new ArrayList<>();
        }
        varietyAttributes.add(varietyAttribute);
        return varietyAttribute;
    }

    public List<VarietyAttribute> getVarietyAttributes() {
        return varietyAttributes;
    }

    public void setVarietyAttributes(List<VarietyAttribute> varietyAttributes) {
        this.varietyAttributes = varietyAttributes;
    }

    @Nullable
    public VarietyAttribute getSimilarVarietyAttribute(VarietyAttribute varietyAttribute) {
        if(varietyAttributes!=null) {
            for(VarietyAttribute va :varietyAttributes) {
                if (va.getName().equalsIgnoreCase(varietyAttribute.getName()) && va.getMeasuringUnitId() == varietyAttribute.getMeasuringUnitId()) {
                    return va;
                }
            }
        }
        return null;
    }
}
