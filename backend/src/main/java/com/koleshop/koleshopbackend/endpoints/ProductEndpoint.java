package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.db.models.InventoryProduct;
import com.koleshop.koleshopbackend.db.models.InventoryProductVariety;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.db.models.RestCallResponse;
import com.koleshop.koleshopbackend.db.models.deprecated.Product;
import com.koleshop.koleshopbackend.services.ProductService;
import com.koleshop.koleshopbackend.services.SessionService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 12/05/15.
 */
@Api(name = "productEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = ""))
public class ProductEndpoint {

    private static final Logger logger = Logger.getLogger(ProductEndpoint.class.getName());

    @ApiMethod(name = "getProductsList")
    public List<Product> getProductList(@Named("shopId") int shopId,
                                        @Named("startIndex") int startIndex,
                                        @Named("count") int count) {
        ProductService productService = new ProductService();
        List<Product> productList = productService.getProduct(shopId, startIndex, count);
        return productList;
    }

    @ApiMethod(name = "saveProduct")
    public KoleResponse saveProduct(InventoryProduct product, @Named("userId") Long userId, @Named("sessionId") String sessionId, @Named("categoryId") Long categoryId) {
        KoleResponse koleResponse = new KoleResponse();

        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId)) {
                ProductService productService = new ProductService();
                InventoryProduct savedProduct = productService.saveProduct(product, categoryId, userId);
                if (savedProduct != null) {
                    //product is saved successfully
                    koleResponse.setSuccess(true);
                    koleResponse.setData(savedProduct);
                } else {
                    koleResponse.setStatus("product not saved");
                    koleResponse.setData(null);
                }
            } else {
                koleResponse.setStatus("invalid request");
                koleResponse.setData(null);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "product not saved for userId = " + userId + " and session id = " + sessionId);
            koleResponse.setStatus("product not saved");
            koleResponse.setData(null);
        }

        return koleResponse;
    }

    @ApiMethod(name = "getOutOfStockItems")
    public KoleResponse getOutOfStockItems(@Named("userId") Long userId, @Named("sessionId") String sessionId) {
        KoleResponse koleResponse = new KoleResponse();

        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId, Constants.USER_SESSION_TYPE_SELLER)) {
                ProductService productService = new ProductService();
                List<InventoryProductVariety> outOfStockItems = productService.getOutOfStockItems(userId);
                koleResponse.setSuccess(true);
                if (outOfStockItems != null && outOfStockItems.size()>0) {
                    //product is saved successfully
                    koleResponse.setData(outOfStockItems);
                } else {
                    koleResponse.setData("No out of stock items");
                }
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception in getOutOfStockItems for userId = " + userId + " and session id = " + sessionId);
            koleResponse.setStatus("Some problem in getting out of stock items");
            koleResponse.setData(null);
        }

        return koleResponse;
    }

}
