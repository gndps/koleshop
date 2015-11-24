package com.kolshop.kolshopmaterial.model;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by Gundeep on 24/11/15.
 */

@Parcel
public class ProductSelectionRequest {

    ArrayList<Long> productVarietyIds;
    String randomId;
    int positionOfUpdate;
    boolean willSelectOnSuccess;

    public ProductSelectionRequest() {
    }

    public ProductSelectionRequest(ArrayList<Long> productVarietyIds, String randomId, int positionOfUpdate, boolean willSelectOnSuccess) {
        this.productVarietyIds = productVarietyIds;
        this.randomId = randomId;
        this.positionOfUpdate = positionOfUpdate;
        this.willSelectOnSuccess = willSelectOnSuccess;
    }

    public ArrayList<Long> getProductVarietyIds() {
        return productVarietyIds;
    }

    public void setProductVarietyIds(ArrayList<Long> productVarietyIds) {
        this.productVarietyIds = productVarietyIds;
    }

    public String getRandomId() {
        return randomId;
    }

    public void setRandomId(String randomId) {
        this.randomId = randomId;
    }

    public int getPositionOfUpdate() {
        return positionOfUpdate;
    }

    public void setPositionOfUpdate(int positionOfUpdate) {
        this.positionOfUpdate = positionOfUpdate;
    }

    public boolean isWillSelectOnSuccess() {
        return willSelectOnSuccess;
    }

    public void setWillSelectOnSuccess(boolean willSelectOnSuccess) {
        this.willSelectOnSuccess = willSelectOnSuccess;
    }
}
