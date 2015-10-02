package com.kolshop.kolshopbackend.beans;

import java.util.Date;

/**
 * Created by Gundeep on 14/05/15.
 */
public class ProductInfoPackage {

    Long productId;
    String productName;
    String productDescription;
    String brand;
    Long brandId;
    Long userId;
    Long productCategoryId;

    Long productVarietyId;
    String productVarietyName;
    int limitedStock;
    boolean isValid;
    String imageUrl;

    Date dateAdded;
    Date dateModified;
    Long attributeId;
    String attributeName;
    int measuringUnitId;
    String attributeValueDetail;
    Long attributeValueId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
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

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(Long productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public Long getProductVarietyId() {
        return productVarietyId;
    }

    public void setProductVarietyId(Long productVarietyId) {
        this.productVarietyId = productVarietyId;
    }

    public String getProductVarietyName() {
        return productVarietyName;
    }

    public void setProductVarietyName(String productVarietyName) {
        this.productVarietyName = productVarietyName;
    }

    public int getLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(int limitedStock) {
        this.limitedStock = limitedStock;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
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

    public Long getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(Long attributeValueId) {
        this.attributeValueId = attributeValueId;
    }
}
