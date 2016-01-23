package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.services.SessionService;
import com.koleshop.koleshopbackend.db.models.RestCallResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 19/04/15.
 */
@Api(name = "sessionApi",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "api.koleshop.com",
                ownerName = "koleshopserver",
                packagePath = ""))
public class SessionEndPoint {

    private static final Logger logger = Logger.getLogger(ProductEndpoint.class.getName());

    @ApiMethod(name = "requestCode")
    public RestCallResponse requestCode(@Named("phone") Long phone, @Named("registrationId") String registrationId, @Named("deviceType") int deviceType, @Named("sessionType") int sessionType) {
        SessionService sessionService = new SessionService();
        return sessionService.requestOneTimePassword(phone, registrationId, deviceType, sessionType);
    }

    @ApiMethod(name = "verifyCode")
    public RestCallResponse verifyCode(@Named("phone") Long phone, @Named("code") int code) {
        SessionService sessionService = new SessionService();
        return sessionService.verifyOneTimePassword(phone, code);
    }

    @Deprecated
    @ApiMethod(name = "checkUsername")
    public RestCallResponse checkUsername(@Named("username") String username, @Named("uniqueRequestId") String uniqueRequestId) {
        return new SessionService().isUsernameAvailable(username, uniqueRequestId);
    }

    @Deprecated
    @ApiMethod(name = "register")
    public RestCallResponse register(@Named("username") String username,
                                     @Named("password") String password,
                                     @Named("email") String email,
                                     @Named("registrationId") String registrationId,
                                     @Named("deviceId") int deviceId) {
        return new SessionService().register(username, password, registrationId, email, deviceId);
    }

    @ApiMethod(name = "chooseSessionType")
    public RestCallResponse chooseSessionType(@Named("sessionId") String sessionId,
                                              @Named("sessionType") int sessionType) {
        return new SessionService().chooseSessionType(sessionId, sessionType);
    }

    @ApiMethod(name = "updateDeviceUser")
    public KoleResponse updateDeviceUser(@Named("sessionId") String sessionId,
                                             @Named("userId") Long userId, @Named("oldDeviceId") String oldDeviceId, @Named("newDeviceId") String newDeviceId) {
        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId)) {
                KoleResponse koleResponse = new KoleResponse();
                if(new SessionService().updateDeviceUser(userId, oldDeviceId, newDeviceId)) {
                    koleResponse.setSuccess(true);
                    koleResponse.setData(null);
                } else {
                    koleResponse.setSuccess(false);
                }
                return koleResponse;
            } else {
                return KoleResponse.failedResponse();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "device id not updated for userid = " + userId + " and session id = " + sessionId);
            return KoleResponse.failedResponse();
        }

    }

}
