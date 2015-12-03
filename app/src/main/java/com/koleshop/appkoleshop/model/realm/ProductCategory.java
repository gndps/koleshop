package com.koleshop.appkoleshop.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 04/07/15.
 */
public class ProductCategory extends RealmObject {

    @PrimaryKey
    private long id;

    private String name;
    private String imageUrl;
    private long parentCategoryId;

    public ProductCategory() {
    }

    public ProductCategory(long id, String name, String imageUrl, long parentCategoryId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentCategoryId = parentCategoryId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setParentCategoryId(long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

}
