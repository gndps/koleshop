package com.kolshop.kolshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.kolshop.kolshopbackend.db.models.ParentProductCategory;
import com.kolshop.kolshopbackend.db.models.ProductCategory;
import com.kolshop.kolshopbackend.db.models.deprecated.ProductVarietyAttributeMeasuringUnit;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.services.ProductService;

import java.util.List;

/**
 * Created by Gundeep on 30/05/15.
 */

@Api(name = "commonEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "server.kolshop.com", ownerName = "kolshopserver", packagePath = ""))
public class CommonEndpoint {

    @ApiMethod(name = "getProductCategories")
    public List<ParentProductCategory> getProductCategories() {
        return new ProductService().getProductCategories();
    }

    @ApiMethod(name = "storeImage")
    public RestCallResponse storeImage() {
        return null;
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
