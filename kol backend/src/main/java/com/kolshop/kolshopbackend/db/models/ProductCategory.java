package com.kolshop.kolshopbackend.db.models;

/**
 * Created by Gundeep on 30/05/15.
 */
public class ProductCategory {

    int id;
    String name;
    String imageUrl;
    int parentProductCategoryId;

    public ProductCategory(int id, String name, String imageUrl, int parentProductCategoryId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentProductCategoryId = parentProductCategoryId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getParentProductCategoryId() {
        return parentProductCategoryId;
    }

    public void setParentProductCategoryId(int parentProductCategoryId) {
        this.parentProductCategoryId = parentProductCategoryId;
    }


}
