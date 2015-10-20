package com.kolshop.kolshopbackend.utils;

import com.kolshop.kolshopbackend.db.models.RestCallResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gundeep on 30/09/15.
 */
public class RestClient {

    private static final String USER_AGENT = "Mozilla/5.0";

    // HTTP GET request
    public static RestCallResponse sendGet(String url, HashMap<String, String> params) {

        if(url==null) return null;
        RestCallResponse restCallResponse = new RestCallResponse();
        url = prepareUrl(url, params);
        String resultString = "";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("User-Agent", USER_AGENT);
            HttpResponse response = client.execute(request);

            System.out.println("\nSending 'GET' request to URL : " + url);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                resultString = result.toString();
                restCallResponse.setStatus("success");
                restCallResponse.setReason(null);
                restCallResponse.setData(resultString);
            } else {
                restCallResponse.setStatus("failure");
                restCallResponse.setData(null);
                restCallResponse.setReason(response.getStatusLine().getReasonPhrase());
            }
        } catch (ClientProtocolException exception) {
            System.err.println("-- ClientProtocolException --");
            exception.printStackTrace();
            restCallResponse.setStatus("failure");
            restCallResponse.setData(null);
            restCallResponse.setReason(exception.getMessage());
        } catch (IOException exception) {
            System.err.println("-- Java io exception --");
            exception.printStackTrace();
            restCallResponse.setStatus("failure");
            restCallResponse.setData(null);
            restCallResponse.setReason(exception.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            restCallResponse.setStatus("failure");
            restCallResponse.setData(null);
            restCallResponse.setReason(e.getMessage());
        } finally {
            return restCallResponse;
        }
    }

    // HTTP POST request
    public static RestCallResponse sendPost(String url, List<NameValuePair> urlParameters) {

        String resultString = "";
        RestCallResponse restCallResponse = new RestCallResponse();
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        try {
            // add header
            post.setHeader("User-Agent", USER_AGENT);

            //urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + post.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                resultString = result.toString();
                restCallResponse.setStatus("success");
                restCallResponse.setData(resultString);
                restCallResponse.setReason(null);
            } else {
                restCallResponse.setStatus("failure");
                restCallResponse.setData(null);
                restCallResponse.setReason(response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            restCallResponse.setStatus("failure");
            restCallResponse.setData(null);
            restCallResponse.setReason(e.getMessage());
        }

        return restCallResponse;

    }

    private static String prepareUrl(String url, HashMap<String, String> hashMap) {
        Iterator it = hashMap.entrySet().iterator();
        List<NameValuePair> params = new LinkedList<>();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry)it.next();
            params.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));
            it.remove(); // avoids a ConcurrentModificationException
        }
        url = addParametersToUrl(url, params);
        return url;
    }

    private static String addParametersToUrl(String url, List<NameValuePair> params){
        if(!url.endsWith("?") && !url.contains("?")) {
            url += "?";
        } else {
            url += "&";
        }
        String paramString = URLEncodedUtils.format(params, "utf-8");
        url += paramString;
        return url;
    }

}
