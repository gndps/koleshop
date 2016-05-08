package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.models.db.InventoryProduct;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.models.db.SellerSettings;
import com.koleshop.koleshopbackend.services.SellerService;
import com.koleshop.koleshopbackend.services.SessionService;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 17/02/16.
 */


@Api(name = "sellerEndpoint",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = ""))
public class SellerEndpoint {

    private static final Logger logger = Logger.getLogger(SellerSettings.class.getName());

    @ApiMethod(name = "openCloseShop", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse openCloseShop(@Named("sellerId") Long sellerId, @Named("sessionId") String sessionId,
                                       @Named("open") boolean open) {

        KoleResponse response = new KoleResponse();
        boolean shopStatusToggleComplete = false;
        try {
            if (SessionService.verifyUserAuthenticity(sellerId, sessionId, Constants.USER_SESSION_TYPE_SELLER)) {
                shopStatusToggleComplete = new SellerService().openCloseShop(sellerId, open);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (shopStatusToggleComplete) {
            response.setSuccess(true);
            response.setData(shopStatusToggleComplete);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "searchProducts", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse searchProducts(@Named("sellerId") Long sellerId, @Named("sessionId") String sessionId,
                                      @Named("searchQuery") String searchQuery, @Named("myInventory") boolean myInventory,
                                       @Named("limit") int limit, @Named("offset") int offset) {

        KoleResponse response = new KoleResponse();
        List<InventoryProduct> searchResultProducts = null;
        try {
            if (SessionService.verifyUserAuthenticity(sellerId, sessionId, Constants.USER_SESSION_TYPE_SELLER)) {
                searchResultProducts = new SellerService().searchProducts(sellerId, myInventory, searchQuery, limit, offset);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (searchResultProducts!=null) {
            response.setSuccess(true);
            response.setData(searchResultProducts);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

}
