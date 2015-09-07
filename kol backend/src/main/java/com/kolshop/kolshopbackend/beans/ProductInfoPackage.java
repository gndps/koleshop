package com.kolshop.kolshopbackend.beans;

import java.util.Date;

/**
 * Created by Gundeep on 14/05/15.
 */
public class ProductInfoPackage {

    int productId;
    String productName;
    String productDescription;
    String brand;
    int brandId;
    int userId;
    int productCategoryId;

    int productVarietyId;
    String productVarietyName;
    int limitedStock;
    boolean isValid;
    String imageUrl;

    Date dateAdded;
    Date dateModified;
    int attributeId;
    String attributeName;
    int measuringUnitId;
    String attributeValueDetail;
    int attributeValueId;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public int getLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(int limitedStock) {
        this.limitedStock = limitedStock;
    }

    public int getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(int productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getMeasuringUnitId() {
        return measuringUnitId;
    }

    public void setMeasuringUnitId(int measuringUnitId) {
        this.measuringUnitId = measuringUnitId;
    }

    public String getAttributeValueDetail() {
        return attributeValueDetail;
    }

    public void setAttributeValueDetail(String attributeValueDetail) {
        this.attributeValueDetail = attributeValueDetail;
    }

    public int getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(int attributeValueId) {
        this.attributeValueId = attributeValueId;
    }

    public int getProductVarietyId() {
        return productVarietyId;
    }

    public void setProductVarietyId(int productVarietyId) {
        this.productVarietyId = productVarietyId;
    }

    public String getProductVarietyName() {
        return productVarietyName;
    }

    public void setProductVarietyName(String productVarietyName) {
        this.productVarietyName = productVarietyName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
