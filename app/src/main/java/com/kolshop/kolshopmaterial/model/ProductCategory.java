package com.kolshop.kolshopmaterial.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 04/07/15.
 */
public class ProductCategory extends RealmObject {

    @PrimaryKey
    private int id;

    private String name;
    private String imageUrl;
    private int parentCategoryId;

    public ProductCategory() {
    }

    public ProductCategory(int id, String name, String imageUrl, int parentCategoryId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentCategoryId = parentCategoryId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getParentCategoryId() {
        return parentCategoryId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setParentCategoryId(int parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

}
