package com.kolshop.kolshopmaterial.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 01/12/15.
 */
public class Brand extends RealmObject {

    @PrimaryKey
    private Long id;

    private String name;

    public Brand() {
    }

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
