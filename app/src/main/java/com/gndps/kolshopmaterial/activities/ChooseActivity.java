package com.gndps.kolshopmaterial.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gndps.kolshopmaterial.R;
import com.gndps.kolshopmaterial.common.constant.Constants;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.model.RestCallResponse;
import com.gndps.kolshopmaterial.model.Session;
import com.gndps.kolshopmaterial.services.RestCallService;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ChooseActivity extends Activity {

    private static final String REQUEST_CHOOSE_SESSION_TYPE = "choose_session_type";
    private static final String TAG = "LoginActivity";
    private BroadcastReceiver chooseSessionTypeBroadcastReciever;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        initializeBroadcastReceivers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.choose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
        if (!prefs.getString("session", "").trim().equalsIgnoreCase("")) {
            Gson gson = new Gson();
            Session session = gson.fromJson(prefs.getString("session", ""), Session.class);
            if (session.getSessionType() == Constants.SHOPKEEPER_SESSION || session.getSessionType() == Constants.BUYER_SESSION) {
                Intent intent = new Intent(this, GetStartedActivity.class);
                startActivity(intent);
            }
        }
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(chooseSessionTypeBroadcastReciever, new IntentFilter(REQUEST_CHOOSE_SESSION_TYPE));
    }

    private void initializeBroadcastReceivers() {
        chooseSessionTypeBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(REQUEST_CHOOSE_SESSION_TYPE)) {
                    RestCallResponse restCallResponse = new Gson().fromJson(intent.getStringExtra("result"), RestCallResponse.class);
                    if (restCallResponse.getStatus().equalsIgnoreCase("success")) {
                        onRestCallSuccess(restCallResponse, intent.getAction());
                    } else {
                        onRestCallFail(restCallResponse, intent.getAction());
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(chooseSessionTypeBroadcastReciever);
    }

    public void shopkeeper(final View view) {
        dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        chooseSessionType(Constants.SHOPKEEPER_SESSION);
        //Intent intent = new Intent(this, ShopkeeperHome.class);
        //startActivity(intent);
    }

    public void buyer(final View view) {
        dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        chooseSessionType(Constants.BUYER_SESSION);
        //Intent intent = new Intent(this, BuyerHome.class);
        //startActivity(intent);
    }

    private void chooseSessionType(int sessionType) {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        Session session = new Gson().fromJson(prefs.getString("session", ""), Session.class);
        String relativeUrl = "ShopNet/api/session/chooseSessionType";
        Map<String, String> map = new HashMap<String, String>();
        map.put("sessionId", session.getSessionId());
        map.put("sessionType", sessionType + "");
        Intent restCallServiceIntent = new Intent(this, RestCallService.class);
        restCallServiceIntent.putExtra("url", relativeUrl);
        restCallServiceIntent.putExtra("map", new Gson().toJson(map));
        restCallServiceIntent.setAction(REQUEST_CHOOSE_SESSION_TYPE);
        startService(restCallServiceIntent);
    }

    private void onRestCallSuccess(RestCallResponse restCallResponse, String action) {
        dialog.dismiss();
        Intent intent = new Intent(this, GetStartedActivity.class);
        startActivity(intent);
    }

    private void onRestCallFail(RestCallResponse restCallResponse, String action) {
        dialog.dismiss();
        Log.i(TAG, ((RestCallResponse) restCallResponse).getReason());
        new AlertDialog.Builder(this)
                .setTitle("Loading failed")
                .setMessage("Please try again")
                .setPositiveButton("Ok", null)
                .show();
    }
}
