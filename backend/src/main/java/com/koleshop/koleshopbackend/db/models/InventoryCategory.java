package com.koleshop.koleshopbackend.db.models;

/**
 * Created by Gundeep on 16/10/15.
 */

public class InventoryCategory {

    Long id;
    String name;
    String desc;
    String imageUrl;

    public InventoryCategory(Long id, String name, String desc, String imageUrl) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.imageUrl = imageUrl;
    }

    public InventoryCategory() {

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
