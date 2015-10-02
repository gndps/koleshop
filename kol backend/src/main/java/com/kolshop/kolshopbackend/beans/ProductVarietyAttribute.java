package com.kolshop.kolshopbackend.beans;

/**
 * Created by Gundeep on 17/05/15.
 */
public class ProductVarietyAttribute {

    Long id;
    String name;
    int measuringUnitId;
    Long attributeValueId;
    String value;

    public ProductVarietyAttribute(ProductInfoPackage productInfoPackage) {

        super();
        setId(productInfoPackage.getAttributeId());
        setName(productInfoPackage.getAttributeName());
        setMeasuringUnitId(productInfoPackage.getMeasuringUnitId());
        setAttributeValueId(productInfoPackage.getAttributeValueId());
        setValue(productInfoPackage.getAttributeValueDetail());

    }

    public Long getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(Long attributeValueId) {
        this.attributeValueId = attributeValueId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
