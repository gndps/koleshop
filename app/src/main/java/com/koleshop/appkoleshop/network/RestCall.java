package com.koleshop.appkoleshop.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.model.RestCallResponse;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestCall extends AsyncTask<Map<String, String>, Void, String> {

    Context context;
    String callID;
    String url;
    String requestId;
    RestCallListener restCallListener;

    public RestCall(Context context, RestCallListener restCallListener, String url, String requestId) {
        this.context = context;
        this.restCallListener = restCallListener;
        this.url = url;
        this.requestId = requestId;
    }

    public RestCall(Context context, RestCallListener restCallListener, String url) {
        this.context = context;
        this.restCallListener = restCallListener;
        this.url = url;
        this.requestId = null;
    }

    @Override
    protected void onPreExecute() {

    }

    protected void onPostExecute(String result) {
        registrationResultFetched(result);
    }

    private void registrationResultFetched(String result) {
        if (!result.startsWith("error~")) {
            Gson gson = new Gson();
            RestCallResponse restCallResponse = gson.fromJson(result, RestCallResponse.class);
            if (restCallResponse.getStatus().equalsIgnoreCase("success")) {
                restCallListener.onRestCallSuccess(restCallResponse, requestId);
            } else {
                restCallListener.onRestCallFail(restCallResponse, requestId);
            }
        } else {
            RestCallResponse restCallResponse = new RestCallResponse();
            restCallResponse.setStatus("failure");
            restCallResponse.setReason(result.split("error~")[1]);
            restCallResponse.setData(null);
            restCallListener.onRestCallFail(restCallResponse, requestId);
        }
    }

    @Override
    protected String doInBackground(Map<String, String>... params) {
        // Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP Post
        String tempUrl = Constants.BASE_URL + url;
        HttpPost httpPost = new HttpPost(tempUrl);

        // Building post parameters
        // key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        for (Map.Entry<String, String> e : params[0].entrySet()) {
            nameValuePair.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }

        // Url Encoding the POST parameters
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        // Making HTTP Request
        try {
            HttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("Http Response:", responseString);
            return responseString;
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
            return "error~" + e.getMessage();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
            return "error~" + e.getMessage();

        }
    }

}
