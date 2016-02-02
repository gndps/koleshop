/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.koleshop.koleshopbackend.endpoints;

import com.google.android.gcm.server.Message;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.db.models.MyBean;
import com.koleshop.koleshopbackend.gcm.GcmHelper;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = ""))
public class MyEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }

    @ApiMethod(name = "sendToSeller")
    public KoleResponse sendToSeller(@Named("type") String type, @Named("jsonString") String jsonString) {
        try {
            Message gcmMessage = new Message.Builder()
                    .collapseKey(Constants.GCM_DEMO_MESSAGE_COLLAPSE_KEY)
                    .addData("type", Constants.GCM_DEMO_MESSAGE)
                    .addData("messageType", type)
                    .addData("jsonData", jsonString)
                    .build();
            GcmHelper.notifyUser(7l, gcmMessage, 2);
            KoleResponse response = new KoleResponse();
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            return KoleResponse.failedResponse();
        }
    }

    public KoleResponse sendToBuyer(@Named("jsonString") String jsonString) {
        try {
            Message gcmMessage = new Message.Builder()
                    .collapseKey(Constants.GCM_DEMO_MESSAGE_COLLAPSE_KEY)
                    .addData("type", Constants.GCM_DEMO_MESSAGE)
                    .addData("jsonData", jsonString)
                    .build();
            GcmHelper.notifyUser(7l, gcmMessage, 2);
            KoleResponse response = new KoleResponse();
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            return KoleResponse.failedResponse();
        }
    }

}
