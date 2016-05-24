package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.models.db.Order;
import com.koleshop.koleshopbackend.services.OrderService;
import com.koleshop.koleshopbackend.services.SessionService;

import java.util.List;

/**
 * Created by Gundeep on 13/02/16.
 */

@Api(name = "orderEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver"))
public class OrderEndpoint {

    @ApiMethod(name = "createNewOrder", httpMethod = ApiMethod.HttpMethod.POST, path = "create")
    public KoleResponse createNewOrder(@Named("sessionId") String sessionId, Order order, @Named("hours") int hours, @Named("minutes") int minutes) {

        KoleResponse response = new KoleResponse();
        try {
            if (SessionService.verifyUserAuthenticity(order.getBuyerSettings().getUserId(), sessionId, Constants.USER_SESSION_TYPE_BUYER)) {
                order = new OrderService().createNewOrder(order, minutes, hours);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (order != null && order.getId() != null && order.getId() > 0) {
            response.setSuccess(true);
            response.setData(order);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "updateOrder", httpMethod = ApiMethod.HttpMethod.POST, path = "update")
    public KoleResponse updateOrder(@Named("sessionId") String sessionId, Order order) {

        KoleResponse response = new KoleResponse();
        try {
            if (SessionService.verifyUserAuthenticity(order.getBuyerSettings().getUserId(), sessionId)) {
                order = new OrderService().updateOrder(order, true);
            } else if (SessionService.verifyUserAuthenticity(order.getSellerSettings().getUserId(), sessionId)) {
                order = new OrderService().updateOrder(order, false);
            } else if (SessionService.verifyHustleUserAuthenticity(order.getSellerSettings().getUserId(), sessionId)) {
                order = new OrderService().updateOrder(order, false);
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (order != null && order.getId() != null && order.getId() > 0) {
            response.setSuccess(true);
            response.setData("order updated"); //this string ("order updated") is matched to check if the order has been updated;
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "getIncomingOrders", httpMethod = ApiMethod.HttpMethod.POST, path = "incoming")
    public KoleResponse getIncomingOrders(@Named("userId") Long userId, @Named("sessionId") String sessionId) {

        KoleResponse response = new KoleResponse();
        List<Order> orders;
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId, Constants.USER_SESSION_TYPE_SELLER)) {
                orders = new OrderService().getIncomingOrders(userId);
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(e.getLocalizedMessage());
            return response;
        }
        if (orders != null && orders.size() > 0) {
            response.setSuccess(true);
            response.setData(orders);
        } else {
            response.setSuccess(true);
            response.setData("No Incoming Orders");
        }
        return response;
    }

    @ApiMethod(name = "getPendingOrders", httpMethod = ApiMethod.HttpMethod.POST, path = "pending")
    public KoleResponse getPendingOrders(@Named("userId") Long userId, @Named("sessionId") String sessionId,
                                         @Named("pagination") boolean pagination, @Named("limit") int limit, @Named("offset") int offset) {

        KoleResponse response = new KoleResponse();
        List<Order> orders;
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId, Constants.USER_SESSION_TYPE_SELLER)) {
                orders = new OrderService().getPendingOrders(userId, pagination, limit, offset);
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(e.getLocalizedMessage());
            return response;
        }
        if (orders != null && orders.size() > 0) {
            response.setSuccess(true);
            response.setData(orders);
        } else {
            response.setSuccess(true);
            response.setData("No Pending Orders");
        }
        return response;
    }

    @ApiMethod(name = "getCompleteOrders", httpMethod = ApiMethod.HttpMethod.POST, path = "complete")
    public KoleResponse getCompleteOrders(@Named("userId") Long userId, @Named("sessionId") String sessionId,
                                          @Named("pagination") boolean pagination, @Named("limit") int limit, @Named("offset") int offset) {

        KoleResponse response = new KoleResponse();
        List<Order> orders;
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId, Constants.USER_SESSION_TYPE_SELLER)) {
                orders = new OrderService().getCompleteOrders(userId, pagination, limit, offset);
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(e.getLocalizedMessage());
            return response;
        }
        if (orders != null && orders.size() > 0) {
            response.setSuccess(true);
            response.setData(orders);
        } else {
            response.setSuccess(true);
            response.setData("No Pending Orders");
        }
        return response;
    }

    @ApiMethod(name = "getMyOrders", httpMethod = ApiMethod.HttpMethod.POST, path = "customer")
    public KoleResponse getMyOrders(@Named("userId") Long userId, @Named("sessionId") String sessionId,
                                    @Named("pagination") boolean pagination, @Named("limit") int limit, @Named("offset") int offset) {

        KoleResponse response = new KoleResponse();
        List<Order> orders;
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId, Constants.USER_SESSION_TYPE_BUYER)) {
                orders = new OrderService().getMyOrders(userId, pagination, limit, offset);
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(e.getLocalizedMessage());
            return response;
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

    @ApiMethod(name = "getOrderForId", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse getOrderForId(@Named("userId") Long userId, @Named("sessionId") String sessionId, @Named("orderId") Long orderId) {

        KoleResponse response = new KoleResponse();
        Order order;
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId)) {
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
