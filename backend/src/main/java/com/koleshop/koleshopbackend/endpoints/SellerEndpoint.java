package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.db.models.SellerSettings;
import com.koleshop.koleshopbackend.services.SellerService;
import com.koleshop.koleshopbackend.services.SessionService;

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

}
