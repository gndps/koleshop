package com.koleshop.appkoleshop.model.realm;

import java.util.Date;

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
    private String desc;
    private int sortOrder;
    private long parentCategoryId;
    private boolean addedToMyShop;
    private Date myShopUpdateDate;
    private Date warehouseUpdateDate;

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

    public boolean isAddedToMyShop() {
        return addedToMyShop;
    }

    public void setAddedToMyShop(boolean addedToMyShop) {
        this.addedToMyShop = addedToMyShop;
    }

    public Date getMyShopUpdateDate() {
        return myShopUpdateDate;
    }

    public void setMyShopUpdateDate(Date myShopUpdateDate) {
        this.myShopUpdateDate = myShopUpdateDate;
    }

    public Date getWarehouseUpdateDate() {
        return warehouseUpdateDate;
    }

    public void setWarehouseUpdateDate(Date warehouseUpdateDate) {
        this.warehouseUpdateDate = warehouseUpdateDate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
