package com.kolshop.kolshopmaterial.model.android;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 05/09/15.
 */
public class ProductVarietyAttributeValue extends RealmObject {

    @PrimaryKey
    private String id;
    private int productVarietyId;
    private int productVarietyAttributeId;
    private String value;

    public ProductVarietyAttributeValue()
    {

    }

    public ProductVarietyAttributeValue(String id, int productVarietyId, int productVarietyAttributeId, String value) {
        this.id = id;
        this.productVarietyId = productVarietyId;
        this.productVarietyAttributeId = productVarietyAttributeId;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getProductVarietyId() {
        return productVarietyId;
    }

    public void setProductVarietyId(int productVarietyId) {
        this.productVarietyId = productVarietyId;
    }

    public int getProductVarietyAttributeId() {
        return productVarietyAttributeId;
    }

    public void setProductVarietyAttributeId(int productVarietyAttributeId) {
        this.productVarietyAttributeId = productVarietyAttributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
