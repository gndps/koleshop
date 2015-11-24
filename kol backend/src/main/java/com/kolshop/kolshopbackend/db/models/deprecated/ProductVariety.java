package com.kolshop.kolshopbackend.db.models.deprecated;

import java.util.Date;
import java.util.List;

/**
 * Created by Gundeep on 17/05/15.
 */

@Deprecated
public class ProductVariety {

    Long id;
    String name;
    int limitedStock;
    boolean valid;
    String imageUrl;
    Date dateAdded;
    Date dateModified;

    List<ProductVarietyAttribute> productVarietyAttributes;

    public ProductVariety(ProductInfoPackage productInfoPackage) {

        super();
        setId(productInfoPackage.getProductVarietyId());
        setName(productInfoPackage.getProductName());
        setLimitedStock(productInfoPackage.getLimitedStock());
        setValid(productInfoPackage.isValid());
        setImageUrl(productInfoPackage.getImageUrl());
        setDateAdded(productInfoPackage.getDateAdded());
        setDateModified(productInfoPackage.getDateModified());

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

    public int getLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(int limitedStock) {
        this.limitedStock = limitedStock;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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

    public List<ProductVarietyAttribute> getProductVarietyAttributes() {
        return productVarietyAttributes;
    }

    public void setProductVarietyAttributes(List<ProductVarietyAttribute> productVarietyAttributes) {
        this.productVarietyAttributes = productVarietyAttributes;
    }

}
