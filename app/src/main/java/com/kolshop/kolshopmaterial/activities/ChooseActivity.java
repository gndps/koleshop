package com.kolshop.kolshopmaterial.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.Session;
import com.kolshop.kolshopmaterial.services.SessionIntentService;

public class ChooseActivity extends Activity {

    private BroadcastReceiver chooseSessionTypeBroadcastReceiver;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kolshop.kolshopmaterial.R.layout.activity_choose);
        initializeBroadcastReceivers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.kolshop.kolshopmaterial.R.menu.choose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == com.kolshop.kolshopmaterial.R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if session type is already selected, then go to get started activity
        Session session = PreferenceUtils.getSession(this);
        if (session!=null && (session.getSessionType() == Constants.SHOPKEEPER_SESSION || session.getSessionType() == Constants.BUYER_SESSION)) {
            Intent intent = new Intent(this, GetStartedActivity.class);
            startActivity(intent);
        }
        //else register broadcast receivers
        else {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.registerReceiver(chooseSessionTypeBroadcastReceiver, new IntentFilter(Constants.ACTION_CHOOSE_SESSION_TYPE_COMPLETE));
            lbm.registerReceiver(chooseSessionTypeBroadcastReceiver, new IntentFilter(Constants.ACTION_CHOOSE_SESSION_TYPE_FAILED));
        }
    }

    private void initializeBroadcastReceivers() {
        chooseSessionTypeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_CHOOSE_SESSION_TYPE_COMPLETE)) {
                    //select session type success -> go to get started activity
                    dialog.dismiss();
                    Intent intent2 = new Intent(getApplicationContext(), GetStartedActivity.class);
                    startActivity(intent2);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_CHOOSE_SESSION_TYPE_FAILED)) {
                    //select session type failure -> show failed popup
                    dialog.dismiss();
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Loading failed")
                            .setMessage("Please try again")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            }

        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(chooseSessionTypeBroadcastReceiver);
    }

    public void shopkeeper(final View view) {
        dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        chooseSessionType(Constants.SHOPKEEPER_SESSION);
    }

    public void buyer(final View view) {
        dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        chooseSessionType(Constants.BUYER_SESSION);
    }

    private void chooseSessionType(int sessionType) {

        /*SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        Session session = new Gson().fromJson(prefs.getString("session", ""), Session.class);
        String relativeUrl = "ShopNet/api/session/chooseSessionType";
        Map<String, String> map = new HashMap<String, String>();
        map.put("sessionId", session.getSessionId());
        map.put("sessionType", sessionType + "");
        SessionIntentService sessionIntentService = new Intent(this, SessionIntentService.class);
        restCallServiceIntent.putExtra("url", relativeUrl);
        restCallServiceIntent.putExtra("map", new Gson().toJson(map));
        restCallServiceIntent.setAction(REQUEST_CHOOSE_SESSION_TYPE);
        startService(restCallServiceIntent);*/

        Intent sessionIntentServiceIntent = new Intent(this, SessionIntentService.class);
        PreferenceUtils.getSession(this).getSessionId();
        sessionIntentServiceIntent.putExtra("sessionId", PreferenceUtils.getSession(this).getSessionId());
        sessionIntentServiceIntent.putExtra("sessionType", sessionType + "");
        sessionIntentServiceIntent.setAction(Constants.ACTION_CHOOSE_SESSION_TYPE);
        startService(sessionIntentServiceIntent);

    }
}
