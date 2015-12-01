package com.kolshop.kolshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.kolshop.kolshopbackend.db.models.Brand;
import com.kolshop.kolshopbackend.db.models.ParentProductCategory;
import com.kolshop.kolshopbackend.db.models.ProductCategory;
import com.kolshop.kolshopbackend.db.models.deprecated.ProductVarietyAttributeMeasuringUnit;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.services.InventoryService;
import com.kolshop.kolshopbackend.services.ProductService;
import com.kolshop.kolshopbackend.services.SessionService;

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
    public List<ProductCategory> getAllProductCategories(@Named("userId") Long userId, @Named("sessionId") String sessionId) {
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId)) {
                return new ProductService().getAllProductCategories();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @ApiMethod(name = "getAllBrands")
    public List<Brand> getAllBrands(@Named("userId") Long userId, @Named("sessionId") String sessionId) {
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId)) {
                return new ProductService().getAllBrands();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    @Deprecated
    @ApiMethod(name = "getMeasuringUnits")
    public List<ProductVarietyAttributeMeasuringUnit> getMeasuringUnits(@Named("userId") Long userId, @Named("sessionId") String sessionId) {
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId)) {
                return new ProductService().getMeasuringUnits();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

}
