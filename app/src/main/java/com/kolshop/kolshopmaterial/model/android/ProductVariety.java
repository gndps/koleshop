package com.kolshop.kolshopmaterial.model.android;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 05/09/15.
 */
@Parcel(value = Parcel.Serialization.BEAN, analyze = { ProductVariety.class })
public class ProductVariety extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private int limitedStock;
    private boolean valid;
    private String imageUrl;
    private Date dateAdded;
    private Date dateModified;

    private RealmList<VarietyAttribute> listVarietyAttributes;
    private RealmList<AttributeValue> listAttributeValues;

    public ProductVariety() {
    }

    public ProductVariety(String id, String name, int limitedStock, boolean valid, String imageUrl, Date dateAdded, Date dateModified) {
        this.id = id;
        this.name = name;
        this.limitedStock = limitedStock;
        this.valid = valid;
        this.imageUrl = imageUrl;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
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

    public int getLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(int limitedStock) {
        this.limitedStock = limitedStock;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public RealmList<VarietyAttribute> getListVarietyAttributes() {
        return listVarietyAttributes;
    }

    public void setListVarietyAttributes(RealmList<VarietyAttribute> listVarietyAttributes) {
        this.listVarietyAttributes = listVarietyAttributes;
    }

    public RealmList<AttributeValue> getListAttributeValues() {
        return listAttributeValues;
    }

    public void setListAttributeValues(RealmList<AttributeValue> listAttributeValues) {
        this.listAttributeValues = listAttributeValues;
    }
}
