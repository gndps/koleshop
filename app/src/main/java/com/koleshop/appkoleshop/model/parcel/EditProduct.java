package com.koleshop.appkoleshop.model.parcel;

import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 07/12/15.
 */

@Parcel
public class EditProduct {

    Long id;
    String name;
    String brand;
    List<EditProductVar> editProductVars;
    long categoryId;
    boolean isModified;

    public EditProduct() {
        this.id = 0l;
        this.name = "";
        this.brand = "";
    }

    public EditProduct(Long id, String name, String brand, List<EditProductVar> editProductVars) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.editProductVars = editProductVars;
    }

    public EditProduct(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.brand = product.getBrand();
        List<EditProductVar> vars = new ArrayList<>();
        for(ProductVariety var : product.getVarieties()) {
            EditProductVar editProductVar = new EditProductVar(var);
            vars.add(editProductVar);
        }
        this.editProductVars = vars;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<EditProductVar> getEditProductVars() {
        return editProductVars;
    }

    public void setEditProductVars(List<EditProductVar> editProductVars) {
        this.editProductVars = editProductVars;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setIsModified(boolean isModified) {
        this.isModified = isModified;
    }
}
