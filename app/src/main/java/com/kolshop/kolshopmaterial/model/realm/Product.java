package com.kolshop.kolshopmaterial.model.realm;

import com.kolshop.kolshopmaterial.helper.ProductVarietyParcelConverter;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 05/09/15.
 */
@Parcel(value = Parcel.Serialization.BEAN, analyze = { Product.class })
public class Product extends RealmObject{

    @PrimaryKey
    private String id;
    private String name;
    private String description;
    private String brand;
    private int brandId;
    private int userId;
    private int productCategoryId;
    private RealmList<ProductVariety> listProductVariety;

    public Product(String id,String name, String description, String brand, int brandId, int userId, int productCategoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.brandId = brandId;
        this.userId = userId;
        this.productCategoryId = productCategoryId;
    }

    public Product()
    {

    }

    public int getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(int productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmList<ProductVariety> getListProductVariety() {
        return listProductVariety;
    }

    @ParcelPropertyConverter(ProductVarietyParcelConverter.class)
    public void setListProductVariety(RealmList<ProductVariety> listProductVariety) {
        this.listProductVariety = listProductVariety;
    }


}
