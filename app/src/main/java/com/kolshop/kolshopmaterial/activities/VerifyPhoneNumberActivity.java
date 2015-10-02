package com.kolshop.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.services.SessionIntentService;

import java.io.IOException;

public class VerifyPhoneNumberActivity extends AppCompatActivity {

    EditText editTextPhone;
    ProgressDialog dialog;
    GoogleCloudMessaging gcm;
    String regId;
    Long phone;
    Context mContext;
    String userType;
    TextView textViewTitle,textViewSubtitle;
    FrameLayout frameLayoutBottomButtons;
    String titleBackup;
    ProgressBar progressBar;
    TextInputLayout textInputLayout;
    private BroadcastReceiver verifyActivityBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        userType = bundle.getString(Constants.KEY_USER_TYPE);
        mContext = this;
        setContentView(R.layout.activity_verify_phone_number);
        editTextPhone = (EditText) findViewById(R.id.editTextPhoneVerify);
        textViewTitle = (TextView) findViewById(R.id.textViewTitleVerifyPhone);
        textViewSubtitle = (TextView) findViewById(R.id.textViewSubtitleVerifyPhone);
        frameLayoutBottomButtons = (FrameLayout) findViewById(R.id.frame_layout_bottom_buttons_verify_phone);
        progressBar = (ProgressBar) findViewById(R.id.progressBarVerifyPhone);
        textInputLayout = (TextInputLayout) findViewById(R.id.input_layout_phone);
        initializeBroadcastReceivers();
        addTextListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(verifyActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_SUCCESS));
        lbm.registerReceiver(verifyActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_FAILED));
    }

    private void initializeBroadcastReceivers() {
        verifyActivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_SUCCESS)) {
                    stopProcessing();
                    Intent intent2 = new Intent(mContext, VerifyOTPActivity.class);
                    intent2.putExtra("phone", phone);
                    intent2.putExtra("deviceId", regId);
                    intent2.putExtra("userType", userType);
                    startActivity(intent2);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_FAILED)) {
                    stopProcessing();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Some problem occurred while verifying phone")
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
        lbm.unregisterReceiver(verifyActivityBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_verify_phone_number, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void requestOtp(View v) {
        try {
            String phoneNumber = textInputLayout.getEditText().getText().toString();
            phone = Long.parseLong(phoneNumber);
        } catch (Exception e) {
            editTextPhone.setError("Please enter a valid phone number");
            return;
        }
        if(editTextPhone.getText().toString().length()<10) {
            editTextPhone.setError("Please enter a valid phone number");
        } else {
            showProcessing("Sending verification code...");
            if(isDeviceRegisteredWithGoogleServer()) {
                requestOneTimePasswordFromServer();
            } else {
                registerWithGoogleServers();
            }
        }
    }

    private void requestOneTimePasswordFromServer() {
        Intent intent = new Intent(this, SessionIntentService.class);
        intent.putExtra("phone", editTextPhone.getText().toString());
        PreferenceUtils.setPreferences(mContext, Constants.KEY_USER_PHONE, editTextPhone.getText().toString());
        intent.putExtra("userType", userType);
        if(regId==null || regId.isEmpty()) {
            regId = PreferenceUtils.getRegistrationId(mContext);
        }
        intent.putExtra("deviceId", regId);
        intent.setAction(Constants.ACTION_REQUEST_OTP);
        startService(intent);
    }

    private boolean isDeviceRegisteredWithGoogleServer() {
        return !PreferenceUtils.getRegistrationId(this).isEmpty();
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
                    requestOneTimePasswordFromServer();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!regId.isEmpty()) {
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
                if(s.toString().length()==10) {
                    //hide keyboard
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    view.clearFocus();
                }
            }
        });
    }
}
