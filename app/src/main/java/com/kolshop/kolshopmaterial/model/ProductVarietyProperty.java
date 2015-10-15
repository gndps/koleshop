package com.kolshop.kolshopmaterial.model;

import com.kolshop.kolshopmaterial.model.realm.AttributeValue;
import com.kolshop.kolshopmaterial.model.realm.VarietyAttribute;

/**
 * Created by Gundeep on 27/09/15.
 */
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
