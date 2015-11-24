package com.kolshop.kolshopbackend.db.models.deprecated;

import java.util.List;

/**
 * Created by Gundeep on 12/05/15.
 */

@Deprecated
public class Product {

    Long id;
    String name;
    String description;
    String brand;
    Long brandId;
    Long userId;
    Long productCategoryId;

    List<ProductVariety> productVarieties;

    public Product(ProductInfoPackage productInfoPackage) {
        super();
        setId(productInfoPackage.getProductId());
        setName(productInfoPackage.getProductName());
        setDescription(productInfoPackage.getProductDescription());
        setBrand(productInfoPackage.getBrand());
        setBrandId(productInfoPackage.getBrandId());
        setUserId(productInfoPackage.getUserId());
        setProductCategoryId(productInfoPackage.getProductCategoryId());
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<ProductVariety> getProductVarieties() {
        return productVarieties;
    }

    public void setProductVarieties(List<ProductVariety> productVarieties) {
        this.productVarieties = productVarieties;
    }

}
