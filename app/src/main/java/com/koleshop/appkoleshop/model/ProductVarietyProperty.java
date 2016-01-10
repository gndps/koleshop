package com.koleshop.appkoleshop.model;

import com.koleshop.appkoleshop.model.realm.AttributeValue;
import com.koleshop.appkoleshop.model.realm.VarietyAttribute;

/**
 * Created by Gundeep on 27/09/15.
 */
@Deprecated
public class ProductVarietyProperty {

    VarietyAttribute varietyAttribute;
    AttributeValue attributeValue;

    public VarietyAttribute getVarietyAttribute() {
        return varietyAttribute;
    }

    public void setVarietyAttribute(VarietyAttribute varietyAttribute) {
        this.varietyAttribute = varietyAttribute;
    }

    public AttributeValue getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(AttributeValue attributeValue) {
        this.attributeValue = attributeValue;
    }

}
