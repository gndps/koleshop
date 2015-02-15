package com.gndps.kolshopmaterial.activities;

import android.app.Activity;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gndps.kolshopmaterial.R;
import com.gndps.kolshopmaterial.common.constant.Constants;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.common.constant.RestUrl;
import com.gndps.kolshopmaterial.common.util.CommonUtils;
import com.gndps.kolshopmaterial.common.util.PreferenceUtils;
import com.gndps.kolshopmaterial.model.RestCallResponse;
import com.gndps.kolshopmaterial.model.Session;
import com.gndps.kolshopmaterial.network.RestCall;
import com.gndps.kolshopmaterial.network.RestCallListener;
import com.gndps.kolshopmaterial.services.RestCallService;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends Activity implements RestCallListener {

    private static final String ACTION_PROCESS_DEVICE_REGISTRATION = "com.kolshop.action.PROCESS_DEVICE_REGISTRATION_GOOGLE";
    private static final String REQUEST_SIGN_UP = "signup";
    private static final String TAG = "SignUpActivity";
    EditText editTextUsername, editTextEmail, editTextPassword;
    ProgressDialog dialog;
    ProgressBar progressBarUsernameAvailable;
    ImageView imageViewUsernameAvailable;
    Context mContext;
    SignUpActivity thisActivity;
    String requestId;
    private BroadcastReceiver signUpActivityBroadcastReciever;
    private boolean usernameAvailable, checkingUsernameAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mContext = this;
        thisActivity = this;
        initializeBroadcastReceivers();
        initializeUIElements();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
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
            if (!session.getSessionId().isEmpty()) {
                Intent intent = new Intent(this, InitialActivity.class);
                startActivity(intent);
            }
        }
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(signUpActivityBroadcastReciever, new IntentFilter(ACTION_PROCESS_DEVICE_REGISTRATION));
        lbm.registerReceiver(signUpActivityBroadcastReciever, new IntentFilter(REQUEST_SIGN_UP));
    }

    private void initializeBroadcastReceivers() {
        signUpActivityBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(ACTION_PROCESS_DEVICE_REGISTRATION)) {
                    registerWithKolShop();
                } else if (intent.getAction().equalsIgnoreCase(REQUEST_SIGN_UP)) {
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
        lbm.unregisterReceiver(signUpActivityBroadcastReciever);
    }

    public void signUp(final View view) {
        if (validateForm()) {
            if (usernameAvailable) {
                dialog = ProgressDialog.show(this, "Signing Up", "Please wait...", true);
                if (!isDeviceRegisteredWithGoogleServer()) {
                    registerWithGoogleServers();
                } else {
                    registerWithKolShop();
                }
            } else {
                if (checkingUsernameAvailability) {
                    Toast toast = Toast.makeText(this, "Checking username availability", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    checkUsernameAvailability();
                }
            }
        }
    }

    private void registerWithGoogleServers() {

        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", Constants.GOOGLE_PROJECT_ID);
        startService(registrationIntent);

    }

    private void registerWithKolShop() {
        //request to server with registrationId, username, password, email
        String registrationId;
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        registrationId = prefs.getString("registration_id", "");
        String relativeUrl = "ShopNet/api/session/register";
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", editTextUsername.getText().toString());
        map.put("email", editTextEmail.getText().toString());
        map.put("password", editTextPassword.getText().toString());
        map.put("registrationId", registrationId);
        map.put("deviceType", Constants.DEVICE_TYPE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("session", null);
        editor.commit();

        Intent restCallServiceIntent = new Intent(mContext, RestCallService.class);
        restCallServiceIntent.putExtra("url", relativeUrl);
        restCallServiceIntent.putExtra("map", new Gson().toJson(map));
        restCallServiceIntent.setAction(REQUEST_SIGN_UP);
        PreferenceUtils.clearUserSettings(this);
        PreferenceUtils.setPreferencesFlag(this, Prefs.PrefFlags.NEVER_SYNCED, true);
        mContext.startService(restCallServiceIntent);

    }

    private void initializeUIElements() {
        editTextUsername = (EditText) findViewById(R.id.editTextUsername_Login);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword_Login);
        progressBarUsernameAvailable = (ProgressBar) findViewById(R.id.progressBarUsername);
        progressBarUsernameAvailable.setVisibility(View.GONE);
        imageViewUsernameAvailable = (ImageView) findViewById(R.id.imageViewUsernameAvailable);

        editTextUsername.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {

            }

            @Override
            public void afterTextChanged(Editable text) {
                progressBarUsernameAvailable.setVisibility(View.GONE);
                imageViewUsernameAvailable.setVisibility(View.GONE);
                editTextUsername.setError(null);
                if (text.length() < 4) {
                    editTextUsername.setError("Minimum 4 Characters");
                } else {
                    checkUsernameAvailability();
                }
            }
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {

            }

            @Override
            public void afterTextChanged(Editable text) {
                if (text.length() < 8) {
                    editTextPassword.setError("Minimum 8 Characters");
                } else {
                    editTextPassword.setError(null);
                }
            }
        });
    }

    private void checkUsernameAvailability() {
        progressBarUsernameAvailable.setVisibility(View.VISIBLE);
        checkingUsernameAvailability = true;
        editTextUsername.setError(null);
        requestId = CommonUtils.randomString(6);
        RestCall restCall = new RestCall(mContext, thisActivity, RestUrl.URL_IS_USERNAME_AVAILABLE, requestId);
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", editTextUsername.getText().toString());
        restCall.execute(map);
    }

    private boolean validateForm() {
        if (editTextUsername.getText().length() < 4) {
            editTextUsername.setError("Minimum 4 Characters");
            return false;
        }

        if (!CommonUtils.validateEmail(editTextEmail.getText().toString())) {
            editTextEmail.setError("Invalid Email");
            return false;
        }

        if (editTextPassword.getText().length() < 8) {
            editTextPassword.setError("Minimum 8 Characters");
        }

        return true;

    }

    private boolean isDeviceRegisteredWithGoogleServer() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        return (!prefs.getString("registration_id", "").trim().equalsIgnoreCase(""));
    }

    @Override
    public void onRestCallSuccess(Object result, Object requestId) {

        if (requestId.toString().equalsIgnoreCase(REQUEST_SIGN_UP)) {

            dialog.dismiss();
            if (((RestCallResponse) result).getReason() != null && ((RestCallResponse) result).getReason().equalsIgnoreCase("Could not create session")) {
                Intent intent = new Intent(this, InitialActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ChooseActivity.class);
                startActivity(intent);
            }

        } else if (requestId.toString().equalsIgnoreCase(this.requestId)) {
            checkingUsernameAvailability = false;
            progressBarUsernameAvailable.setVisibility(View.GONE);
            imageViewUsernameAvailable.setVisibility(View.GONE);
            if (((RestCallResponse) result).getData().equalsIgnoreCase("yes")) {
                imageViewUsernameAvailable.setVisibility(View.VISIBLE);
                usernameAvailable = true;
                imageViewUsernameAvailable.setImageResource(R.drawable.check);
                editTextUsername.setError(null);
            } else {
                usernameAvailable = false;
                editTextUsername.setError("Username already taken");
            }
        }

    }

    @Override
    public void onRestCallFail(Object result, Object requestId) {
        if (requestId.toString().equalsIgnoreCase(REQUEST_SIGN_UP)) {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Problem in Signing Up")
                    .setMessage(((RestCallResponse) result).getReason())
                    .setPositiveButton("Ok", null)
                    .show();
        } else if (requestId.toString().equalsIgnoreCase(this.requestId)) {
            checkingUsernameAvailability = false;
            progressBarUsernameAvailable.setVisibility(View.GONE);
            imageViewUsernameAvailable.setVisibility(View.GONE);
            Log.i(TAG, ((RestCallResponse) result).getReason());
            new AlertDialog.Builder(mContext)
                    .setTitle("Problem in Checking Username Availability")
                    .setMessage("Please try again")
                    .setPositiveButton("Ok", null)
                    .show();
        }
    }
}
