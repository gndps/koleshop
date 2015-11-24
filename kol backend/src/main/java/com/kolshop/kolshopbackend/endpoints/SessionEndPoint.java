package com.kolshop.kolshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.gson.Gson;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.db.models.ResultBean;
import com.kolshop.kolshopbackend.services.SessionService;

/**
 * Created by Gundeep on 19/04/15.
 */
@Api(name = "sessionApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = "server.kolshop.com",
    ownerName = "kolshopserver",
    packagePath = ""))
public class SessionEndPoint {

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

}
