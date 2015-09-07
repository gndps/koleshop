package com.kolshop.kolshopbackend.beans;

import java.util.List;

/**
 * Created by Gundeep on 12/05/15.
 */
public class Product {

    int id;
    String name;
    String description;
    String brand;
    int brandId;
    int userId;
    int productCategoryId;

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

    public int getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(int productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public List<ProductVariety> getProductVarieties() {
        return productVarieties;
    }

    public void setProductVarieties(List<ProductVariety> productVarieties) {
        this.productVarieties = productVarieties;
    }

}
