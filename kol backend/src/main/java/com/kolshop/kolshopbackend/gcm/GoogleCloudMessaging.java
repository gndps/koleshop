package com.kolshop.kolshopbackend.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kolshop.kolshopbackend.common.Constants;
import com.kolshop.kolshopbackend.db.models.GcmContent;
import com.kolshop.kolshopbackend.db.models.GcmResponse;

public class GoogleCloudMessaging {

	public static void sendMessage(GcmContent gcmContent) {

		String url = "https://android.googleapis.com/gcm/send";
		String gcmContentJson = new Gson().toJson(gcmContent);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpPost request = new HttpPost(url);
			Header header = new BasicHeader("Authorization", "key=" + Constants.GCM_API_KEY);
			request.addHeader(header);
			StringEntity params = new StringEntity(gcmContentJson);
			params.setContentType("application/json");
			request.setEntity(params);
			HttpResponse result = httpClient.execute(request);
			String json = EntityUtils.toString(result.getEntity(), "UTF-8");

			com.google.gson.Gson gson = new com.google.gson.Gson();
			GcmResponse response = null;
			try {
				response = gson.fromJson(json, GcmResponse.class);
			} catch (Exception e) {
				System.out.println("GCM response not parsed. Response:\n"
						+ response);
			}
			System.out.println(json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		JsonObject jsonobj = new JsonObject();
		JsonArray jsonArray = new JsonArray();
		List<String> regIds = new ArrayList<String>();
		regIds.add("APA91bE4TUU0GbyerIpbcLB7jyJ7V74at5GcyOLVUK55J9N-vAAf2lIGCpnU04-ufjDzxchWPo66OFerKJbgeUTF7fzi4Y8vHPH6wAQFWx_sRvexQQPl9HMuRQ3_6LvXWYor9Kka7r2UcW1ppoXzEGjGnWdoMuQ0Ig");
		jsonobj.addProperty("test", "fuck");
		jsonobj.addProperty("data", "gndp");
		GcmContent gcm = new GcmContent().data(jsonobj)
				.collapse_key("testcollasekey").registration_ids(regIds);
		sendMessage(gcm);
	}

}