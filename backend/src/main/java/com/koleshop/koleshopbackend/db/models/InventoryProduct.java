package com.koleshop.koleshopbackend.db.models;

import java.util.List;

/**
 * Created by Gundeep on 02/11/15.
 */
public class InventoryProduct {

    Long id;
    String name;
    //String description;
    String brand;
    Long categoryId;
    //String additionalInfo;
    //String specialDescription;
    //boolean privateToUser;
    //boolean selectedByUser;
    List<InventoryProductVariety> varieties;

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

    /*public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }*/

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    /*public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getSpecialDescription() {
        return specialDescription;
    }

    public void setSpecialDescription(String specialDescription) {
        this.specialDescription = specialDescription;
    }

    public boolean isPrivateToUser() {
        return privateToUser;
    }

    public void setPrivateToUser(boolean privateToUser) {
        this.privateToUser = privateToUser;
    }

    public boolean isSelectedByUser() {
        return selectedByUser;
    }

    public void setSelectedByUser(boolean selectedByUser) {
        this.selectedByUser = selectedByUser;
    }*/

    public List<InventoryProductVariety> getVarieties() {
        return varieties;
    }

    public void setVarieties(List<InventoryProductVariety> varieties) {
        this.varieties = varieties;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
