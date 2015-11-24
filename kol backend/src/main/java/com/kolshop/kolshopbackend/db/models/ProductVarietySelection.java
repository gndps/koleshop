package com.kolshop.kolshopbackend.db.models;

import java.util.List;

/**
 * Created by Gundeep on 21/11/15.
 */
public class ProductVarietySelection {

    List<Long> selectProductIds;
    List<Long> deselectProductIds;

    public List<Long> getSelectProductIds() {
        return selectProductIds;
    }

    public void setSelectProductIds(List<Long> selectProductIds) {
        this.selectProductIds = selectProductIds;
    }

    public List<Long> getDeselectProductIds() {
        return deselectProductIds;
    }

    public void setDeselectProductIds(List<Long> deselectProductIds) {
        this.deselectProductIds = deselectProductIds;
    }
}
