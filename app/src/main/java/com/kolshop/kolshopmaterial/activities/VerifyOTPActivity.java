package com.kolshop.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.GlobalData;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.Session;
import com.kolshop.kolshopmaterial.services.SessionIntentService;

import static com.kolshop.kolshopmaterial.R.id.textViewTitleVerifyPhone;

public class VerifyOTPActivity extends AppCompatActivity {

    EditText editTextCode;
    Button buttonResend;
    String phone;
    String userType;
    String deviceId;
    ProgressDialog dialog;
    private BroadcastReceiver verifyOtpBroadcastReceiver;
    Context mContext;
    ProgressBar progressBar;
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
        Bundle bundle = getIntent().getExtras();
        phone = bundle.getInt("phone") + "";
        userType = bundle.getString("userType");
        deviceId = bundle.getString("deviceId");
        editTextCode = (EditText) findViewById(R.id.editTextCodeVerify);
        buttonResend = (Button) findViewById(R.id.buttonResend);
        textViewTitle = (TextView) findViewById(R.id.textViewTitleOtp);
        textViewSubtitle = (TextView) findViewById(R.id.textViewSubtitleOtp);
        frameLayoutBottomButtons = (FrameLayout) findViewById(R.id.frame_layout_bottom_buttons_verify_otp);
        progressBar = (ProgressBar) findViewById(R.id.progressBarOtp);
        initializeBroadcastReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_OTP_RECEIVED));
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_SUCCESS));
        lbm.registerReceiver(verifyOtpBroadcastReceiver, new IntentFilter(Constants.ACTION_REQUEST_OTP_FAILED));
        if(userType==null || userType.isEmpty()) {
            userType = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_TYPE);
        }
        if(deviceId == null || deviceId.isEmpty()) {
            deviceId = PreferenceUtils.getRegistrationId(mContext);
        }
        if(phone ==null || phone.isEmpty()) {
            phone = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_PHONE);
        }
    }

    private void initializeBroadcastReceivers() {
        verifyOtpBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_OTP_RECEIVED)) {
                    String code = intent.getExtras().getString("code");
                    editTextCode.setText(code);
                    verifyOtp(null);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_REQUEST_OTP_SUCCESS)) {
                    stopProcessing();
                    Intent intent2 = new Intent(mContext, VerifyOTPActivity.class);
                    intent2.putExtra("phone", phone);
                    String regId = PreferenceUtils.getRegistrationId(mContext);
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
        lbm.unregisterReceiver(verifyOtpBroadcastReceiver);
    }

    public void resendCode(View v) {
        showProcessing("Sending verification code...");
        requestOneTimePasswordFromServer();
    }

    private void requestOneTimePasswordFromServer() {
        Intent intent = new Intent(this, SessionIntentService.class);
        intent.putExtra("phone", phone);
        intent.putExtra("userType", userType);
        intent.putExtra("deviceId", deviceId);
        intent.setAction(Constants.ACTION_REQUEST_OTP);
        startService(intent);
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
        titleBackup = textViewTitle.getText().toString();
        textViewTitle.setText(processingMessage);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopProcessing() {
        frameLayoutBottomButtons.setVisibility(View.VISIBLE);
        textViewSubtitle.setVisibility(View.VISIBLE);
        buttonResend.setVisibility(View.VISIBLE);
        textViewTitle.setText(titleBackup);
        progressBar.setVisibility(View.GONE);
    }
}
