package com.kolshop.kolshopbackend.gcm;

import java.util.List;

import com.google.gson.JsonObject;
import com.kolshop.kolshopbackend.db.models.GcmContent;
import com.kolshop.kolshopbackend.services.ShopService;

public class GcmHelper {
	
	public static void broadcastToUser(String username, JsonObject jsonObject)
	{
		List<String> deviceIdsList = ShopService.getRegistrationIdsForUser(username);
		GcmContent gcmContent = new GcmContent();
		gcmContent.setRegistration_ids(deviceIdsList);
		gcmContent.setData(jsonObject);
		GoogleCloudMessaging.sendMessage(gcmContent);
	}
	
	public static void broadcastToDeviceIds(List<String> deviceIdsList, JsonObject jsonObject)
	{
		GcmContent gcmContent = new GcmContent();
		gcmContent.setRegistration_ids(deviceIdsList);
		gcmContent.setData(jsonObject);
		GoogleCloudMessaging.sendMessage(gcmContent);
	}

}
