package com.kolshop.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gndps.kolshopserver.sessionApi.SessionApi;
import com.gndps.kolshopserver.sessionApi.model.RestCallResponse;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.constant.Prefs;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.services.SessionIntentService;

import java.io.IOException;

public class SignUpActivity extends ActionBarActivity {

    private static final String TAG = "SignUpActivity";
    EditText editTextUsername, editTextEmail, editTextPassword;
    ProgressDialog dialog;
    ProgressBar progressBarUsernameAvailable;
    ImageView imageViewUsernameAvailable;
    Context mContext;
    Button buttonSignUp;
    private BroadcastReceiver signUpActivityBroadcastReceiver;
    private boolean usernameAvailable, checkingUsernameAvailability;
    private Toolbar toolbar;
    GoogleCloudMessaging gcm;
    String regId;
    String uniqueRequestId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mContext = this;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initializeBroadcastReceivers();
        initializeUIElements();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_up, menu);
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
        /*SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
        if (!prefs.getString("session", "").trim().equalsIgnoreCase("")) {
            Gson gson = new Gson();
            Session session = gson.fromJson(prefs.getString("session", ""), Session.class);
            if (!session.getSessionId().isEmpty()) {
                Intent intent = new Intent(this, InitialActivity.class);
                startActivity(intent);
            }
        }*/
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(signUpActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_SIGN_UP_COMPLETE));
        checkSignUpStatus(false);
    }

    private void initializeBroadcastReceivers() {
        signUpActivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_SIGN_UP_COMPLETE)) {
                    //user sign up request complete with kol shop servers
                    dialog.dismiss();
                    checkSignUpStatus(true);
                }

            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(signUpActivityBroadcastReceiver);
    }

    public void signUp(View view) {
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
                    checkSignUpStatus(true);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if(!regId.isEmpty())
                registerWithKolShop();
            }
        }.execute(null, null, null);

    }

    private void registerWithKolShop() {
        //request to server with registrationId, username, password, email
        String registrationId = PreferenceUtils.getRegistrationId(mContext);
        String username = editTextUsername.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        PreferenceUtils.setPreferences(mContext, Constants.KEY_SIGN_UP_STATUS, null);

        Intent sessionIntentServiceIntent = new Intent(mContext, SessionIntentService.class);
        //sessionIntentServiceIntent = CommonUtils.createExplicitFromImplicitIntent(this, sessionIntentServiceIntent);
        sessionIntentServiceIntent.putExtra("username", username);
        sessionIntentServiceIntent.putExtra("email", email);
        sessionIntentServiceIntent.putExtra("password", password);
        sessionIntentServiceIntent.putExtra("registrationId", registrationId);
        sessionIntentServiceIntent.setAction(Constants.ACTION_SIGN_UP);
        PreferenceUtils.clearUserSettings(this);
        startService(sessionIntentServiceIntent);

    }

    private void initializeUIElements() {
        editTextUsername = (EditText) findViewById(R.id.editTextUsername_Login);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword_Login);
        progressBarUsernameAvailable = (ProgressBar) findViewById(R.id.progressBarUsername);
        progressBarUsernameAvailable.setVisibility(View.GONE);
        imageViewUsernameAvailable = (ImageView) findViewById(R.id.imageViewUsernameAvailable);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);

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
        new CheckUsernameTaskAsync().execute(editTextUsername.getText().toString());
        //requestId = CommonUtils.randomString(6);
        /*RestCall restCall = new RestCall(mContext, this, RestUrl.URL_IS_USERNAME_AVAILABLE, requestId);
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", editTextUsername.getText().toString());
        restCall.execute(map);*/
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
        return !PreferenceUtils.getRegistrationId(this).isEmpty();
    }

    /*@Override
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
    }*/

    public void checkSignUpStatus(boolean showPopupError) {
        String signUpStatus = PreferenceUtils.getPreferences(this, Constants.KEY_SIGN_UP_STATUS);
        if (signUpStatus != null && signUpStatus.equalsIgnoreCase("success")) {
            Intent intent = new Intent(this, ChooseActivity.class);
            startActivity(intent);
        } else if(showPopupError) {
            new AlertDialog.Builder(mContext)
                    .setTitle("Problem in signing up, please try again")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    class CheckUsernameTaskAsync extends AsyncTask<String, Void, RestCallResponse> {

        private SessionApi sessionApi = null;

        @Override
        protected RestCallResponse doInBackground(String... params) {
            if (sessionApi == null) {  // Only do this once
                SessionApi.Builder builder = new SessionApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // use 10.0.2.2 for localhost testing
                        .setRootUrl(Constants.SERVER_URL)
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });

                sessionApi = builder.build();
            }

            try {
                uniqueRequestId = CommonUtils.randomString(6);
                RestCallResponse restCallResponse = sessionApi.checkUsername(params[0], uniqueRequestId).execute();
                return restCallResponse;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(RestCallResponse result) {

            if(result!=null && result.getData()!=null && !result.getData().endsWith(uniqueRequestId))
            {
                return;
            }

            checkingUsernameAvailability = false;
            progressBarUsernameAvailable.setVisibility(View.GONE);
            imageViewUsernameAvailable.setVisibility(View.GONE);

            if (result != null) {

                if (result.getStatus().equalsIgnoreCase("success")) {
                    if (result.getData().startsWith("yes")) {
                        imageViewUsernameAvailable.setVisibility(View.VISIBLE);
                        usernameAvailable = true;
                        imageViewUsernameAvailable.setImageResource(R.drawable.check);
                        editTextUsername.setError(null);
                    } else {
                        usernameAvailable = false;
                        editTextUsername.setError("Username already taken");
                    }
                } else {
                    Log.i(TAG, result.getReason());
                    new AlertDialog.Builder(mContext)
                            .setTitle("Problem while connecting, please try again")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle("Problem while connecting, please try again")
                        .setPositiveButton("Ok", null)
                        .show();
            }
        }
    }
}
