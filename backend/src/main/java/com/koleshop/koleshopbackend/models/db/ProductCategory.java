package com.koleshop.koleshopbackend.models.db;

/**
 * Created by Gundeep on 30/05/15.
 */
public class ProductCategory {

    Long id;
    String name;
    String imageUrl;
    Long parentProductCategoryId;

    public ProductCategory(Long id, String name, String imageUrl, Long parentProductCategoryId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentProductCategoryId = parentProductCategoryId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getParentProductCategoryId() {
        return parentProductCategoryId;
    }

    public void setParentProductCategoryId(Long parentProductCategoryId) {
        this.parentProductCategoryId = parentProductCategoryId;
    }


}
