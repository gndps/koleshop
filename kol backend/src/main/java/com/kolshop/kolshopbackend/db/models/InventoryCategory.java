package com.kolshop.kolshopbackend.db.models;

import com.google.api.server.spi.config.ApiClass;

/**
 * Created by Gundeep on 16/10/15.
 */

public class InventoryCategory {

    String name;
    String desc;
    String imageUrl;
    String countString;

    public InventoryCategory(String name, String desc, String imageUrl, String countString) {
        this.name = name;
        this.desc = desc;
        this.imageUrl = imageUrl;
        this.countString = countString;
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

    public String getCountString() {
        return countString;
    }

    public void setCountString(String countString) {
        this.countString = countString;
    }
}
