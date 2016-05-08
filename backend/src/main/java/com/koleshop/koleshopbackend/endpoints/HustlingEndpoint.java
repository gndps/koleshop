package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.services.HustleService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 04/05/16.
 */
@Api(name = "hustlingEndpoint", version = "v1",
        namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = ""),
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ADMIN_APP_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class HustlingEndpoint {

    private static final Logger logger = Logger.getLogger(HustlingEndpoint.class.getName());

    @ApiMethod(name = "hustle.login")
    public KoleResponse login(@Named("username") String username, @Named("password") String password, User user) throws OAuthRequestException, IOException {
        boolean loggedIn = HustleService.isUserValid(username, password);
        if(loggedIn) {
            if(user!=null) {
                logger.log(Level.INFO, "user is not null");
                return KoleResponse.successResponse();
            } else {
                logger.log(Level.INFO, "user is null");
                return KoleResponse.failedResponse("invalid client");
            }
        } else {
            return KoleResponse.failedResponse("invalid login");
        }
    }

    @ApiMethod(name = "hustle.getRecentOrders")
    public KoleResponse getRecentOrders(@Named("username") String username, @Named("password") String password, User user) {
        boolean loggedIn = HustleService.isUserValid(username, password);
        if(loggedIn && user!=null) {
            return HustleService.getRecentOrders();
        } else {
            return KoleResponse.failedResponse("you gotta hustle your own way!");
        }
    }

}
