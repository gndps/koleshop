package com.kolshop.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.GlobalData;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.Session;
import com.kolshop.kolshopmaterial.services.SessionIntentService;

import java.io.IOException;

public class LoginActivity extends ActionBarActivity {

    EditText editTextUsername_Login, editTextPassword_Login;
    TextView textViewSignUp;
    Context mContext;
    private BroadcastReceiver loginActivityBroadcastReceiver;
    private ProgressDialog dialog;
    private Toolbar toolbar;
    GoogleCloudMessaging gcm;
    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initializeLoginScreen();
        initializeBroadcastReceivers();
        PreferenceUtils.setPreferences(this, Constants.KEY_SIGN_UP_STATUS, "");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //1. if session exists, then load session and start home activity
        Session session = PreferenceUtils.getSession(this);
        if (session != null && !session.getSessionId().isEmpty()) {
            GlobalData globalData = GlobalData.getInstance();
            globalData.setSession(session);
            if (session.getSessionType() == Constants.SHOPKEEPER_SESSION) {
                //IF SHOPKEEPER SESSION ACTIVE
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else if (session.getSessionType() == Constants.BUYER_SESSION) {
                //IF BUYER SESSION ACTIVE
            } else {
                //IF SESSION TYPE NOT AVAILABLE
                Intent intent = new Intent(this, ChooseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        //2. Register broadcast receivers for all expected actions
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(loginActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
        lbm.registerReceiver(loginActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_LOGIN_INVALID_CREDENTIALS));
        lbm.registerReceiver(loginActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_LOGIN_FAILED));
    }

    private void initializeBroadcastReceivers() {
        loginActivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_LOGIN_SUCCESS)) {
                    dialog.dismiss();
                    Intent intent2 = new Intent(mContext, ChooseActivity.class);
                    startActivity(intent2);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_LOGIN_INVALID_CREDENTIALS)) {
                    dialog.dismiss();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Please check username or password")
                            .setMessage("Invalid username/password combination")
                            .setPositiveButton("Ok", null)
                            .show();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_LOGIN_FAILED)) {
                    dialog.dismiss();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Some problem occurred while logging in")
                            .setMessage("Please try again...")
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
        lbm.unregisterReceiver(loginActivityBroadcastReceiver);
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

    public void onSignUp(View view) {
        Intent intent = new Intent(mContext, SignUpActivity.class);
        startActivity(intent);
    }

    private void registerWithGoogleServers() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regId = gcm.register(Constants.GOOGLE_PROJECT_ID);
                    msg = "Device registered, registration ID=" + regId;

                    // Persist the regID - no need to register again.
                    PreferenceUtils.storeRegistrationId(mContext, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    kolShopLogin();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!regId.isEmpty())
                    kolShopLogin();
            }
        }.execute(null, null, null);

    }

    private void kolShopLogin() {
        //0. Get username, password
        String username = editTextUsername_Login.getText().toString();
        String password = editTextPassword_Login.getText().toString();

        //1. Set session to null
        PreferenceUtils.saveSession(this, "");

        //2. Request login to server using IntentService
        Intent sessionIntentServiceIntent = new Intent(this, SessionIntentService.class);
        sessionIntentServiceIntent.putExtra("username", username);
        sessionIntentServiceIntent.putExtra("password", password);
        sessionIntentServiceIntent.setAction(Constants.ACTION_LOGIN);
        PreferenceUtils.clearUserSettings(this);
        startService(sessionIntentServiceIntent);
    }

    private boolean isDeviceRegisteredWithGoogleServer() {
        return !PreferenceUtils.getRegistrationId(this).isEmpty();
    }

    /*private void onRestCallSuccess(RestCallResponse restCallResponse, String action) {
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
    }*/

}
