package com.koleshop.koleshopbackend.db.models;

import java.util.List;

/**
 * Created by Gundeep on 30/05/15.
 */

public class ParentProductCategory {


    ProductCategory parentProductCategory;
    List<ProductCategory> childrenProductCategories;

    public ParentProductCategory()
    {
        super();
    }

    public ParentProductCategory(ProductCategory parentProductCategory) {
        this.parentProductCategory = parentProductCategory;
    }

    public ProductCategory getParentProductCategory() {
        return parentProductCategory;
    }

    public void setParentProductCategory(ProductCategory parentProductCategory) {
        this.parentProductCategory = parentProductCategory;
    }

    public List<ProductCategory> getChildrenProductCategories() {
        return childrenProductCategories;
    }

    public void setChildrenProductCategories(List<ProductCategory> childrenProductCategories) {
        this.childrenProductCategories = childrenProductCategories;
    }


}
