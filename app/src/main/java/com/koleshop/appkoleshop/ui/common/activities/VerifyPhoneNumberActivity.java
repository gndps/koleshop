package com.koleshop.appkoleshop.ui.common.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.services.RegistrationIntentService;
import com.koleshop.appkoleshop.services.SessionIntentService;

import java.io.IOException;

public class VerifyPhoneNumberActivity extends AppCompatActivity {

    EditText editTextPhone;
    GoogleCloudMessaging gcm;
    String regId;
    String phone;
    Context mContext;
    String sessionType;
    TextView textViewTitle, textViewSubtitle;
    FrameLayout frameLayoutBottomButtons;
    String titleBackup;
    ProgressBar progressBar;
    TextInputLayout textInputLayout;
    Button buttonBack, buttonNextSkip;
    boolean skipAllowed;
    private BroadcastReceiver verifyActivityBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(com.koleshop.appkoleshop.R.layout.activity_verify_phone_number);

        editTextPhone = (EditText) findViewById(com.koleshop.appkoleshop.R.id.editTextPhoneVerify);
        textViewTitle = (TextView) findViewById(com.koleshop.appkoleshop.R.id.textViewTitleVerifyPhone);
        textViewSubtitle = (TextView) findViewById(com.koleshop.appkoleshop.R.id.textViewSubtitleVerifyPhone);
        frameLayoutBottomButtons = (FrameLayout) findViewById(com.koleshop.appkoleshop.R.id.frame_layout_bottom_buttons_verify_phone);
        progressBar = (ProgressBar) findViewById(com.koleshop.appkoleshop.R.id.progressBarVerifyPhone);
        textInputLayout = (TextInputLayout) findViewById(com.koleshop.appkoleshop.R.id.input_layout_phone);
        buttonBack = (Button) findViewById(com.koleshop.appkoleshop.R.id.buttonBack);
        buttonNextSkip = (Button) findViewById(com.koleshop.appkoleshop.R.id.buttonNextSkip);

        initializeBroadcastReceivers();
        addTextListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(verifyActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_SUCCESS));
        lbm.registerReceiver(verifyActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_FAILED));
        lbm.registerReceiver(verifyActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_GCM_REGISTRATION_COMPLETE));
        lbm.registerReceiver(verifyActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_GCM_REGISTRATION_FAILED));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            skipAllowed = bundle.getBoolean(Constants.KEY_SKIP_ALLOWED);
        }
        sessionType = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_SESSION_TYPE);
        if (skipAllowed) {
            buttonNextSkip.setText("SKIP");
        }
    }

    private void initializeBroadcastReceivers() {
        verifyActivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_SUCCESS)) {
                    stopProcessing();
                    PreferenceUtils.setPreferencesFlag(context, Constants.FLAG_DEVICE_ID_SYNCED_TO_SERVER, true);
                    Intent intent2 = new Intent(mContext, VerifyOTPActivity.class);
                    startActivity(intent2);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_FAILED)) {
                    stopProcessing();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Some problem occurred while verifying phone")
                            .setMessage("Please try again")
                            .setPositiveButton("Ok", null)
                            .show();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_GCM_REGISTRATION_COMPLETE)) {
                    regId = PreferenceUtils.getRegistrationId(mContext);
                    requestOneTimePasswordFromServer();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_GCM_REGISTRATION_FAILED)) {
                    stopProcessing();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Some problem occurred while verifying phone")
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
        lbm.unregisterReceiver(verifyActivityBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.koleshop.appkoleshop.R.menu.menu_verify_phone_number, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.koleshop.appkoleshop.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goBack(View v) {
        finish();
    }

    public void requestOtp(View v) {
        if (skipAllowed && editTextPhone.getText().toString().isEmpty()) {
            skipLogin();
            return;
        }
        try {
            phone = textInputLayout.getEditText().getText().toString();
            Long.parseLong(phone);
        } catch (Exception e) {
            editTextPhone.setError("Please enter a valid phone number");
            return;
        }
        if (editTextPhone.getText().toString().length() < 10) {
            editTextPhone.setError("Please enter a valid phone number");
        } else {
            showProcessing("Sending verification code...");
            if (isDeviceRegisteredWithGoogleServer()) {
                requestOneTimePasswordFromServer();
            } else {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                intent.setAction(RegistrationIntentService.REGISTRATION_INTENT_SERVICE_ACTION_REGISTER);
                startService(intent);
                //registerWithGoogleServers();
            }
        }
    }

    private void skipLogin() {
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void requestOneTimePasswordFromServer() {
        Intent intent = new Intent(this, SessionIntentService.class);
        PreferenceUtils.setPreferences(mContext, Constants.KEY_USER_PHONE_NUMBER, phone);
        intent.putExtra("phone", phone);
        intent.putExtra("sessionType", sessionType);
        if (regId == null || regId.isEmpty()) {
            regId = PreferenceUtils.getRegistrationId(mContext);
        }
        intent.putExtra("deviceId", regId);
        intent.setAction(Constants.ACTION_REQUEST_OTP);
        startService(intent);
    }

    private boolean isDeviceRegisteredWithGoogleServer() {
        return !PreferenceUtils.getRegistrationId(this).isEmpty();
    }

    @Deprecated
    private void registerWithGoogleServers() {
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

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
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (regId != null && !regId.isEmpty()) {
                    requestOneTimePasswordFromServer();
                } else {
                    stopProcessing();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Some problem occurred while verifying phone")
                            .setMessage("Please try again...")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            }
        }.execute(null, null, null);

    }

    private void showProcessing(String processingMessage) {
        frameLayoutBottomButtons.setVisibility(View.GONE);
        textViewSubtitle.setVisibility(View.GONE);
        titleBackup = textViewTitle.getText().toString();
        textViewTitle.setText(processingMessage);
        textInputLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopProcessing() {
        frameLayoutBottomButtons.setVisibility(View.VISIBLE);
        textViewSubtitle.setVisibility(View.VISIBLE);
        textViewTitle.setText(titleBackup);
        textInputLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void addTextListener() {
        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0 && skipAllowed) {
                    buttonNextSkip.setText("SKIP");
                }
                if (skipAllowed && s.toString().length() > 0 && buttonNextSkip.getText().toString().equalsIgnoreCase("SKIP")) {
                    buttonNextSkip.setText("NEXT");
                }
                /*if (s.toString().length() == 10) {
                    //hide keyboard
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    view.clearFocus();
                }*/
            }
        });
    }
}
