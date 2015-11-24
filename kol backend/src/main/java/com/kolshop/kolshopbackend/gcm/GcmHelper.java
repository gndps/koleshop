package com.kolshop.kolshopbackend.gcm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.db.models.GcmContent;
import com.kolshop.kolshopbackend.services.ShopService;
import com.kolshop.kolshopbackend.services.UserService;

public class GcmHelper {

    public static void broadcastToUser(String username, JsonObject jsonObject) {
        List<String> deviceIdsList = ShopService.getRegistrationIdsForUser(username);
        GcmContent gcmContent = new GcmContent();
        gcmContent.setRegistration_ids(deviceIdsList);
        gcmContent.setData(jsonObject);
        GoogleCloudMessaging.sendMessage(gcmContent);
    }

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
