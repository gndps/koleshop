package com.kolshop.kolshopmaterial.model.android;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 05/09/15.
 */
public class ProductVarietyAttribute extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private int measuringUnitId;

    public ProductVarietyAttribute()
    {
    }

    public ProductVarietyAttribute(String id , String name, int measuringUnitId) {

        super();
        setId(id);
        setName(name);
        setMeasuringUnitId(measuringUnitId);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMeasuringUnitId() {
        return measuringUnitId;
    }

    public void setMeasuringUnitId(int measuringUnitId) {
        this.measuringUnitId = measuringUnitId;
    }

}
