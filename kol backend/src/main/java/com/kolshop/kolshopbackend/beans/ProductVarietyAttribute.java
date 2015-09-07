package com.kolshop.kolshopbackend.beans;

/**
 * Created by Gundeep on 17/05/15.
 */
public class ProductVarietyAttribute {

    int id;
    String name;
    int measuringUnitId;
    int attributeValueId;
    String value;

    public ProductVarietyAttribute(ProductInfoPackage productInfoPackage) {

        super();
        setId(productInfoPackage.getAttributeId());
        setName(productInfoPackage.getAttributeName());
        setMeasuringUnitId(productInfoPackage.getMeasuringUnitId());
        setAttributeValueId(productInfoPackage.getAttributeValueId());
        setValue(productInfoPackage.getAttributeValueDetail());

    }

    public int getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(int attributeValueId) {
        this.attributeValueId = attributeValueId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMeasuringUnitId() {
        return measuringUnitId;
    }

    public void setMeasuringUnitId(int measuringUnitId) {
        this.measuringUnitId = measuringUnitId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
