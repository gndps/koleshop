package com.koleshop.appkoleshop.model.realm;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 26/01/16.
 */
public class Product extends RealmObject {

    @PrimaryKey
    private Long id;
    private String brand;
    private String name;
    private RealmList<ProductVariety> varieties;
    private Long categoryId;
    private Date updateDateMyShop;
    private Date updateDateWareHouse;

    public Product() {
    }

    public Product(Long id, String brand, String name, RealmList<ProductVariety> varieties, Long categoryId, Date updateDateMyShop, Date updateDateWareHouse) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.varieties = varieties;
        this.categoryId = categoryId;
        this.updateDateMyShop = updateDateMyShop;
        this.updateDateWareHouse = updateDateWareHouse;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
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

    public RealmList<ProductVariety> getVarieties() {
        return varieties;
    }

    public void setVarieties(RealmList<ProductVariety> varieties) {
        this.varieties = varieties;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setUpdateDateMyShop(Date updateDateMyShop) {
        this.updateDateMyShop = updateDateMyShop;
    }

    public Date getUpdateDateMyShop() {
        return updateDateMyShop;
    }

    public Date getUpdateDateWareHouse() {
        return updateDateWareHouse;
    }

    public void setUpdateDateWareHouse(Date updateDateWareHouse) {
        this.updateDateWareHouse = updateDateWareHouse;
    }
}
