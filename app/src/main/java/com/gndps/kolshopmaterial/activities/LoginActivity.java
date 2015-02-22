package com.gndps.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gndps.kolshopmaterial.R;
import com.gndps.kolshopmaterial.common.constant.Constants;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.common.util.PreferenceUtils;
import com.gndps.kolshopmaterial.model.RestCallResponse;
import com.gndps.kolshopmaterial.model.Session;
import com.gndps.kolshopmaterial.services.RestCallService;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends ActionBarActivity {

    private static final String ACTION_PROCESS_DEVICE_REGISTRATION = "com.kolshop.action.PROCESS_DEVICE_REGISTRATION_GOOGLE";
    private static final String REQUEST_LOG_IN = "login";
    private static final String TAG = "LoginActivity";
    EditText editTextUsername_Login, editTextPassword_Login;
    TextView textViewSignUp;
    Context mContext;
    private BroadcastReceiver loginActivityBroadcastReciever;
    private ProgressDialog dialog;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        //toolbar = (Toolbar)findViewById(R.id.app_bar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        initializeLoginScreen();
        initializeBroadcastReceivers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
        //if logged in, go to initial activity -> home screen
        String sessionString = prefs.getString("session", "");
        if (!sessionString.trim().equalsIgnoreCase("")) {
            Gson gson = new Gson();
            Session session = gson.fromJson(sessionString, Session.class);
            if (session != null && !session.getSessionId().isEmpty()) {
                //from initial activity it will go to home screen
                Intent intent = new Intent(this, InitialActivity.class);
                startActivity(intent);
            }
        }
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(loginActivityBroadcastReciever, new IntentFilter(ACTION_PROCESS_DEVICE_REGISTRATION));
        lbm.registerReceiver(loginActivityBroadcastReciever, new IntentFilter(REQUEST_LOG_IN));
    }

    private void initializeBroadcastReceivers() {
        loginActivityBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(ACTION_PROCESS_DEVICE_REGISTRATION)) {
                    kolShopLogin();
                } else if (intent.getAction().equalsIgnoreCase(REQUEST_LOG_IN)) {
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
        lbm.unregisterReceiver(loginActivityBroadcastReciever);
    }

    private void initializeLoginScreen() {

        editTextUsername_Login = (EditText) findViewById(R.id.editTextUsername_Login);
        editTextPassword_Login = (EditText) findViewById(R.id.editTextPassword_Login);
        textViewSignUp = (TextView) findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUp(v);
            }
        });

    }

    public void logIn(View view) {
        if (editTextUsername_Login.getText().toString().length() < 4) {
            editTextUsername_Login.setError("Minimum 4 characters");
        } else if (editTextPassword_Login.getText().toString().length() < 8) {
            editTextPassword_Login.setError("Minimum 8 characters");
        } else if (!isDeviceRegisteredWithGoogleServer()) {
            dialog = ProgressDialog.show(this, "Logging In", "Please wait...", true);
            registerWithGoogleServers();
        } else {
            dialog = ProgressDialog.show(this, "Logging In", "Please wait...", true);
            kolShopLogin();
        }
    }

    public void onSignUp(View view)
    {
        Intent intent = new Intent(mContext, SignUpActivity.class);
        startActivity(intent);
    }

    private void registerWithGoogleServers() {

        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", Constants.GOOGLE_PROJECT_ID);
        startService(registrationIntent);

    }

    private void kolShopLogin() {
        //request to server with registrationId, username, password, email
        String registrationId;
        SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
        registrationId = prefs.getString("registration_id", "");
        String relativeUrl = "ShopNet/api/session/login";
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", editTextUsername_Login.getText().toString());
        map.put("password", editTextPassword_Login.getText().toString());
        map.put("registrationId", registrationId);
        map.put("deviceType", Constants.DEVICE_TYPE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("session", null);
        editor.commit();

        Intent restCallServiceIntent = new Intent(this, RestCallService.class);
        restCallServiceIntent.putExtra("url", relativeUrl);
        restCallServiceIntent.putExtra("map", new Gson().toJson(map));
        restCallServiceIntent.setAction(REQUEST_LOG_IN);
        PreferenceUtils.clearUserSettings(this);
        PreferenceUtils.setPreferencesFlag(this, Prefs.PrefFlags.NEVER_SYNCED, true);
        startService(restCallServiceIntent);
    }

    private boolean isDeviceRegisteredWithGoogleServer() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        return (!prefs.getString("registration_id", "").trim().equalsIgnoreCase(""));
    }

    private void onRestCallSuccess(RestCallResponse restCallResponse, String action) {
        if (action.toString().equalsIgnoreCase(REQUEST_LOG_IN)) {

            dialog.dismiss();
            if (((RestCallResponse) restCallResponse).getReason() != null && ((RestCallResponse) restCallResponse).getReason().equalsIgnoreCase("Could not create session")) {
                Intent intent = new Intent(this, InitialActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ChooseActivity.class);
                startActivity(intent);
            }

        }
    }

    private void onRestCallFail(RestCallResponse restCallResponse, String action) {
        if (action.toString().equalsIgnoreCase(REQUEST_LOG_IN)) {
            dialog.dismiss();
            String reason = ((RestCallResponse) restCallResponse).getReason();
            Log.i(TAG, reason);
            if (reason.equalsIgnoreCase("Invalid Username or Password")) {
                new AlertDialog.Builder(this)
                        .setTitle("Could not Log In")
                        .setMessage("Invalid Username or Password")
                        .setPositiveButton("Ok", null)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Could not Log In")
                        .setMessage("Please try again")
                        .setPositiveButton("Ok", null)
                        .show();
            }
        }
    }

}
