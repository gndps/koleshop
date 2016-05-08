/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.gcm.GcmHelper;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.models.db.MyBean;
import com.koleshop.koleshopbackend.models.gcm.Message;
import com.koleshop.koleshopbackend.utils.PropertiesCache;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
                    .priority(Message.Priority.HIGH)
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
                    .priority(Message.Priority.HIGH)
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

    @ApiMethod(name = "testAdminNotification")
    public KoleResponse testAdminNotification(@Named("jsonString") String jsonString) {
        try {
            try {
                // Prepare JSON containing the GCM message content. What to send and where to send.
                JSONObject jGcmData = new JSONObject();
                JSONObject jData = new JSONObject();
                jData.put("message", jsonString);
                // Where to send GCM message.
                jGcmData.put("to", "/topics/admin_messages");
                // What to send in GCM message.
                jGcmData.put("data", jData);

                // Create connection to send GCM Message request.
                URL url = new URL("https://android.googleapis.com/gcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "key=" + PropertiesCache.getProp("GCM_API_KEY"));
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Send GCM message content.
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(jGcmData.toString().getBytes());

                // Read GCM response.
                InputStream inputStream = conn.getInputStream();
                String resp = IOUtils.toString(inputStream);
                System.out.println(resp);
                System.out.println("Check your device/emulator for notification or logcat for " +
                        "confirmation of the receipt of the GCM message.");
            } catch (IOException e) {
                System.out.println("Unable to send GCM message.");
                System.out.println("Please ensure that API_KEY has been replaced by the server " +
                        "API key, and that the device's registration token is correct (if specified).");
                e.printStackTrace();
            }
            KoleResponse response = new KoleResponse();
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            return KoleResponse.failedResponse();
        }
    }

}
