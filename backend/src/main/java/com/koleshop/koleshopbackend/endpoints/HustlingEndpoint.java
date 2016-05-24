package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.models.db.Order;
import com.koleshop.koleshopbackend.services.HustleService;
import com.koleshop.koleshopbackend.services.OrderService;
import com.koleshop.koleshopbackend.services.SessionService;

import java.io.IOException;
import java.util.List;
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
    private static final boolean DISABLE_USER_AUTH = true;

    @ApiMethod(name = "hustle.login")
    public KoleResponse login(@Named("username") String username, @Named("password") String password, User user) throws OAuthRequestException, IOException {
        boolean loggedIn = HustleService.isUserValid(username, password);
        if(loggedIn) {
            if(user!=null || DISABLE_USER_AUTH) {
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
    public KoleResponse getRecentOrders(@Named("userId") Long userId, @Named("sessionId") String sessionId,
                                        @Named("pagination") boolean pagination, @Named("limit") int limit, @Named("offset") int offset) {
        //boolean loggedIn = HustleService.isUserValid(username, password);
        KoleResponse response = new KoleResponse();
        List<Order> orders;
        try {
            if (SessionService.verifyHustleUserAuthenticity(userId, sessionId)) {
                orders = new OrderService().getRecentOrders(userId, pagination, limit, offset);
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            return KoleResponse.failedResponse("you gotta hustle your own way!");
        }

        if (orders != null && orders.size() > 0) {
            response.setSuccess(true);
            response.setData(orders);
        } else {
            response.setSuccess(true);
            response.setData("No Orders available");
        }
        return response;
    }

    @ApiMethod(name = "hustle.activateShopWithPhoneNumber")
    public KoleResponse activateShopWithPhoneNumber(@Named("phoneNumber") Long phoneNumber, User user) {
        if(user!=null || DISABLE_USER_AUTH) {
            boolean activated = false;
            //activated = new HustleService().activateShopWithPhoneNumber(phoneNumber);
            if(activated) {
                return KoleResponse.successResponse();
            } else {
                return KoleResponse.failedResponse("invalid user");
            }
        } else {
            return KoleResponse.failedResponse("you gotta hustle your own way!");
        }
    }

    @ApiMethod(name = "getOrderForId", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse getOrderForId(@Named("userId") Long userId, @Named("sessionId") String sessionId, @Named("orderId") Long orderId) {

        KoleResponse response = new KoleResponse();
        Order order;
        try {
            if (SessionService.verifyHustleUserAuthenticity(userId, sessionId)) {
                order = new OrderService().getOrderForId(orderId, userId);
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(e.getLocalizedMessage());
            return response;
        }
        if (order != null && order.getId() > 0) {
            response.setSuccess(true);
            response.setData(order);
        } else {
            response.setSuccess(true);
            response.setData("No such order exist");
        }
        return response;
    }

}
