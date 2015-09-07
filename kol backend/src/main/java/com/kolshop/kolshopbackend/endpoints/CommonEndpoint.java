package com.kolshop.kolshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.kolshop.kolshopbackend.beans.ParentProductCategory;
import com.kolshop.kolshopbackend.beans.ProductCategory;
import com.kolshop.kolshopbackend.beans.ProductVarietyAttributeMeasuringUnit;
import com.kolshop.kolshopbackend.db.models.MyBean;
import com.kolshop.kolshopbackend.services.ProductService;

import java.util.List;

import javax.inject.Named;

/**
 * Created by Gundeep on 30/05/15.
 */

@Api(name = "commonEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "com.kolshop.kolshopbackend", ownerName = "com.kolshop.kolshopbackend", packagePath = ""))
public class CommonEndpoint {

    @ApiMethod(name = "getProductCategories")
    public List<ParentProductCategory> getProductCategories() {
        return new ProductService().getProductCategories();
    }

    @ApiMethod(name = "getAllProductCategories")
    public List<ProductCategory> getAllProductCategories() {
        return new ProductService().getAllProductCategories();
    }

    @ApiMethod(name = "getMeasuringUnits")
    public List<ProductVarietyAttributeMeasuringUnit> getMeasuringUnits() {
        return new ProductService().getMeasuringUnits();
    }

}
