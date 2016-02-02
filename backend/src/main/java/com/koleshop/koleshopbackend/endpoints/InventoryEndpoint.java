package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.db.models.InventoryCategory;
import com.koleshop.koleshopbackend.db.models.InventoryProduct;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.db.models.ProductVarietySelection;
import com.koleshop.koleshopbackend.services.InventoryService;
import com.koleshop.koleshopbackend.services.SessionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 16/10/15.
 */

@Api(name = "inventoryEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = "yolo"))
public class InventoryEndpoint {

    @ApiMethod(name = "getCategories", httpMethod = ApiMethod.HttpMethod.POST, path="categories")
    public KoleResponse getCategories(@Named("userId") Long userId, @Named("sessionId") String sessionId, @Named("myInventory") boolean myInventory) {

        KoleResponse response = new KoleResponse();
        List<InventoryCategory> list = null;
        try {
            if (true||SessionService.verifyUserAuthenticity(userId, sessionId)) {
                list = new InventoryService().getCategories(myInventory, userId);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (list != null) {
            response.setSuccess(true);
            response.setData(list);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "getSubcategories", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse getSubcategories(@Named("userId") Long userId, @Named("sessionId") String sessionId,
                                         @Named("categoryId") Long categoryId, @Named("myInventory") boolean myInventory) {

        KoleResponse response = new KoleResponse();
        List<InventoryCategory> list = null;
        try {
            if (true||SessionService.verifyUserAuthenticity(userId, sessionId)) {
                list = new InventoryService().getSubcategories(categoryId, userId, myInventory);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (list != null) {
            response.setSuccess(true);
            response.setData(list);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "getProductsForCategoryAndUser", httpMethod = ApiMethod.HttpMethod.POST, path = "user")
    public KoleResponse getProductsForCategoryAndUser(@Named("userId") Long userId, @Named("sessionId") String sessionId,
                                                      @Named("categoryId") Long categoryId, @Named("myInventory") boolean myInventory) {

        KoleResponse response = new KoleResponse();
        List<InventoryProduct> products = null;
        try {
            if (true||SessionService.verifyUserAuthenticity(userId, sessionId)) {
                products = new InventoryService().getProductsForCategory(categoryId, userId, myInventory);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (products != null) {
            response.setSuccess(true);
            response.setData(products);
        } else {
            response.setSuccess(false);
        }
        return response;
    }


    //this method is just for exposure of ListInventoryCategory
    @ApiMethod(name = "exposeInventoryCategory")
    public List<InventoryCategory> justatest() {
        List<InventoryCategory> list = new ArrayList<>();
        InventoryCategory cat = new InventoryCategory(1L, "myinventory", "cool things", "someurl", 1);
        list.add(cat);
        return list;
    }

    //this method is just for exposure of ListInventoryProduct
    @ApiMethod(name = "exposeInventoryProduct")
    public List<InventoryProduct> exposeInventoryCategory() {
        List<InventoryProduct> list = new ArrayList<>();
        InventoryProduct cat = new InventoryProduct();
        list.add(cat);
        return list;
    }

    @ApiMethod(name = "updateProductSelection", httpMethod = ApiMethod.HttpMethod.POST, path = "product")
    public KoleResponse updateProductSelection(@Named("userId") Long userId,  @Named("sessionId") String sessionId, ProductVarietySelection productVarietySelection) {

        //check userId and sessionId pair authenticity
        boolean authenticRequest = SessionService.verifyUserAuthenticity(userId, sessionId);
        if (!authenticRequest) {
            return null;
        }

        //process request
        KoleResponse response = new KoleResponse();
        boolean updated = false;
        try {
            updated = new InventoryService().updateProductSelection(userId, productVarietySelection);
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (updated) {
            response.setSuccess(true);
            response.setData("true");
        } else {
            response.setSuccess(false);
        }
        return response;
    }

}
