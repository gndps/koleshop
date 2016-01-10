package com.koleshop.appkoleshop.model.realm;

import com.koleshop.appkoleshop.helper.AttributeValueParcelConverter;
import com.koleshop.appkoleshop.helper.VarietyAttributeParcelConverter;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 05/09/15.
 */
@Deprecated
@Parcel(value = Parcel.Serialization.BEAN, analyze = { ProductVariety.class })
public class ProductVariety extends RealmObject {

    @PrimaryKey
    private String id;
    private String productId;
    private String name;
    private int limitedStock;
    private boolean validVariety;
    private String imageUrl;
    private Date dateAdded;
    private Date dateModified;

    private RealmList<VarietyAttribute> listVarietyAttributes;
    private RealmList<AttributeValue> listAttributeValues;

    public ProductVariety() {
    }

    public ProductVariety(String id, String productId, String name, int limitedStock, boolean validVariety, String imageUrl, Date dateAdded, Date dateModified) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.limitedStock = limitedStock;
        this.validVariety = validVariety;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public boolean isValidVariety() {
        return validVariety;
    }

    public void setValidVariety(boolean validVariety) {
        this.validVariety = validVariety;
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

    @ParcelPropertyConverter(VarietyAttributeParcelConverter.class)
    public void setListVarietyAttributes(RealmList<VarietyAttribute> listVarietyAttributes) {
        this.listVarietyAttributes = listVarietyAttributes;
    }

    public RealmList<AttributeValue> getListAttributeValues() {
        return listAttributeValues;
    }

    @ParcelPropertyConverter(AttributeValueParcelConverter.class)
    public void setListAttributeValues(RealmList<AttributeValue> listAttributeValues) {
        this.listAttributeValues = listAttributeValues;
    }
}
