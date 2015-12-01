package com.kolshop.kolshopbackend.db.models;

/**
 * Created by Gundeep on 01/12/15.
 */
public class Brand {

    Long id;
    String name;

    public Brand(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
