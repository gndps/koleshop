package com.kolshop.kolshopbackend.db.models.deprecated;

/**
 * Created by Gundeep on 04/07/15.
 */

@Deprecated
public class ProductVarietyAttributeMeasuringUnit {

    int id;
    String unitType;
    String unit;
    boolean isBaseUnit;
    float conversionRate;
    String unitFullName;

    public ProductVarietyAttributeMeasuringUnit(int id, String unitType, String unit, boolean isBaseUnit, float conversionRate, String unitFullName) {
        this.id = id;
        this.unitType = unitType;
        this.unit = unit;
        this.isBaseUnit = isBaseUnit;
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
        return isBaseUnit;
    }

    public void setIsBaseUnit(boolean isBaseUnit) {
        this.isBaseUnit = isBaseUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
