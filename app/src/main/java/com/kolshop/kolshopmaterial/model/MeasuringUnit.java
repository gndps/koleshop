package com.kolshop.kolshopmaterial.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 10/07/15.
 */
public class MeasuringUnit extends RealmObject {


    @PrimaryKey
    private int id;
    private String unitDimensions;
    private String unit;
    private boolean baseUnit;
    private float conversionRate;
    private String unitFullName;

    public MeasuringUnit(){
    }

    public MeasuringUnit(int id, String unitDimensions, String unit, boolean baseUnit, float conversionRate, String unitFullName) {
        this.id = id;
        this.unitDimensions = unitDimensions;
        this.unit = unit;
        this.baseUnit = baseUnit;
        this.conversionRate = conversionRate;
        this.unitFullName = unitFullName;
    }


    public String getUnitFullName() {
        return unitFullName;
    }

    public void setUnitFullName(String unitFullName) {
        this.unitFullName = unitFullName;
    }

    public float getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(float conversionRate) {
        this.conversionRate = conversionRate;
    }

    public boolean isBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(boolean baseUnit) {
        this.baseUnit = baseUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitDimensions() {
        return unitDimensions;
    }

    public void setUnitDimensions(String unitDimensions) {
        this.unitDimensions = unitDimensions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
