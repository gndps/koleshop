package com.koleshop.koleshopbackend.gcm;

import java.util.List;

import com.google.gson.JsonObject;
import com.koleshop.koleshopbackend.services.UserService;
import com.koleshop.koleshopbackend.db.models.GcmContent;

public class GcmHelper {

    public static void broadcastToDeviceIds(List<String> deviceIdsList, JsonObject jsonObject) {
        GcmContent gcmContent = new GcmContent();
        gcmContent.setRegistration_ids(deviceIdsList);
        gcmContent.setData(jsonObject);
        GoogleCloudMessaging.sendMessage(gcmContent);
    }

    public static void notifyUser(Long userId, JsonObject jsonObject, String collapseKey) {

        List<String> deviceIds = UserService.getDeviceIdsForUserId(userId);

        /*JsonObject jsonobj = new JsonObject();
        jsonobj.addProperty("test", "fuck");
        jsonobj.addProperty("data", "gndp");*/

        GcmContent cloudMessage = new GcmContent()
                .data(jsonObject)
                .collapse_key(collapseKey)
                .registration_ids(deviceIds);

        GoogleCloudMessaging.sendMessage(cloudMessage);
    }

}
