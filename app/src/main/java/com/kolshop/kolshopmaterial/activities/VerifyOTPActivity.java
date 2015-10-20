package com.kolshop.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.services.SessionIntentService;

public class VerifyOTPActivity extends AppCompatActivity {

    EditText editTextCode;
    Button buttonResend;
    String phone, sessionType, deviceId;
    private BroadcastReceiver verifyOtpBroadcastReceiver;
    Context mContext;
    ProgressBar progressBar;
    TextInputLayout textInputLayout;
    String titleBackup;
    FrameLayout frameLayoutBottomButtons;
    TextView textViewTitle, textViewSubtitle;
    static String TAG = "VerifyOtpActivity";

    //todo add sms receivers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        mContext = this;

        editTextCode = (EditText) findViewById(R.id.editTextCodeVerify);
        buttonResend = (Button) findViewById(R.id.buttonResend);
        textViewTitle = (TextView) findViewById(R.id.textViewTitleOtp);
        textViewSubtitle = (TextView) findViewById(R.id.textViewSubtitleOtp);
        frameLayoutBottomButtons = (FrameLayout) findViewById(R.id.frame_layout_bottom_buttons_verify_otp);
        progressBar = (ProgressBar) findViewById(R.id.progressBarOtp);
        textInputLayout = (TextInputLayout) findViewById(R.id.input_layout_verification_code);

        initializeBroadcastReceivers();
        addTextListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_OTP_RECEIVED));
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_SUCCESS));
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_FAILED));
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_VERIFY_OTP_SUCCESS));
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_VERIFY_OTP_FAILED));

        phone = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_PHONE_NUMBER);
        sessionType = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_SESSION_TYPE);
        deviceId = PreferenceUtils.getRegistrationId(mContext);
    }

    private void initializeBroadcastReceivers() {
        verifyOtpBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_OTP_RECEIVED)) {
                    String code = intent.getStringExtra("code");
                    editTextCode.setText(code);
                    verifyOtp(null);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_SUCCESS)) {
                    stopProcessing();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_FAILED)) {
                    stopProcessing();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Some problem occurred while verifying phone")
                            .setMessage("Please try again")
                            .setPositiveButton("Ok", null)
                            .show();
                } else if(intent.getAction().equalsIgnoreCase(Constants.ACTION_VERIFY_OTP_SUCCESS)) {
                    stopProcessing();
                    Intent intent1 = new Intent(mContext, GetStartedActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                } else if(intent.getAction().equalsIgnoreCase(Constants.ACTION_VERIFY_OTP_FAILED)) {
                    stopProcessing();
                    new AlertDialog.Builder(mContext)
                            .setTitle("Code not matched")
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
        lbm.unregisterReceiver(verifyOtpBroadcastReceiver);
    }

    public void resendCode(View v) {
        showProcessing("Sending verification code...");
        requestOneTimePasswordFromServer();
    }

    private void requestOneTimePasswordFromServer() {
        Intent intent = new Intent(this, SessionIntentService.class);
        intent.putExtra("phone", phone);
        intent.putExtra("sessionType", sessionType);
        intent.putExtra("deviceId", deviceId);
        intent.setAction(Constants.ACTION_REQUEST_OTP);
        startService(intent);
    }

    public void goBack(View v) {
        finish();
    }

    public void verifyOtp(View v) {
        if (editTextCode.getText().toString().length() != 4) {
            showInvalidCodeDialog();
        } else {
            Intent intent = new Intent(this, SessionIntentService.class);
            Long phoneLong;
            int codeInt;
            try {
                phoneLong = Long.parseLong(phone);
                codeInt = Integer.parseInt(editTextCode.getText().toString());
            } catch (Exception e) {
                Log.d(TAG, "phone or code not parsed to integer - pre code verify");
                showInvalidCodeDialog();
                return;
            }
            showProcessing("Verifying the code...");
            intent.putExtra("phone", phoneLong);
            intent.putExtra("code", codeInt);
            intent.setAction(Constants.ACTION_VERIFY_OTP);
            startService(intent);

        }
    }

    private void showInvalidCodeDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle("Invalid code")
                .setPositiveButton("Ok", null)
                .show();
    }

    private void showProcessing(String processingMessage) {
        frameLayoutBottomButtons.setVisibility(View.GONE);
        textViewSubtitle.setVisibility(View.GONE);
        buttonResend.setVisibility(View.GONE);
        textInputLayout.setVisibility(View.GONE);
        titleBackup = textViewTitle.getText().toString();
        textViewTitle.setText(processingMessage);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopProcessing() {
        frameLayoutBottomButtons.setVisibility(View.VISIBLE);
        textViewSubtitle.setVisibility(View.VISIBLE);
        buttonResend.setVisibility(View.VISIBLE);
        textInputLayout.setVisibility(View.VISIBLE);
        textViewTitle.setText(titleBackup);
        progressBar.setVisibility(View.GONE);
    }

    private void addTextListener() {
        editTextCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 4) {
                    //hide keyboard
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    view.clearFocus();
                }
            }
        });
    }
}
