package com.koleshop.appkoleshop.ui.common.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.model.realm.EssentialInfo;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import java.util.Date;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedbackActivity extends AppCompatActivity {

    private final static int VIEW_FLIPPER_CHILD_FEEDBACK_FORM = 0x00;
    private final static int VIEW_FLIPPER_CHILD_THANK_YOU = 0x01;
    private static final String TAG = "FeedbackActivity";

    @Bind(R.id.pb_status_feedback)
    DilatingDotsProgressBar progressBar;
    @BindString(R.string.send_feedback)
    String stringSendFeedback;

    @Bind(R.id.header_text_view)
    TextView headerTextView;
    @Bind(R.id.feedback_text)
    EditText feedbackText;
    @Bind(R.id.button_send_feedback)
    Button buttonSendFeedback;
    @Bind(R.id.button_call_us)
    Button buttonCallUs;
    @Bind(R.id.textview_thankyou_feedback)
    TextView textViewFeedbackThankyou;
    @Bind(R.id.vf_activity_feedback)
    ViewFlipper viewFlipper;
    @Bind(R.id.iv_we_love_you)
    ImageView imageViewWeLoveYou;

    Context context;
    BroadcastReceiver mBroadcastReceiver;
    Animation.AnimationListener listener;
    Long callUsPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        context = this;
        initializeBroadcastReceiver();
        setupToolbar();
        setupFeedbackButton();
        setupCallUsButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SAVE_FEEDBACK_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SAVE_FEEDBACK_FAILED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_SAVE_FEEDBACK_SUCCESS:
                        showProgressBar(false);
                        //play a pleasant sound
                        //show a valuable life quote
                        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/dancing-script.ttf");
                        textViewFeedbackThankyou.setTypeface(type);
                        startRotationAnimation();
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_THANK_YOU);
                        break;
                    case Constants.ACTION_SAVE_FEEDBACK_FAILED:
                        showProgressBar(false);
                        break;
                }
            }
        };
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }
    }

    private void setupFeedbackButton() {
        buttonSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                sendFeedback();
            }
        });
    }

    private void setupCallUsButton() {
        EssentialInfo essentialInfo = RealmUtils.getEssentialInfo();
        if(essentialInfo!=null) {
            callUsPhone = essentialInfo.getCallUsPhone();
            if(callUsPhone!=null &&  callUsPhone > 0) {
                //all good
                buttonCallUs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        String phoneCallString = "tel:" + callUsPhone;
                        callIntent.setData(Uri.parse(phoneCallString));
                        startActivity(callIntent);

                    }
                });
            } else {
                //hide call us button
                buttonCallUs.setVisibility(View.GONE);
            }
        }
    }

    private void sendFeedback() {
        String message = feedbackText.getText().toString();
        if (TextUtils.isEmpty(message) || message.length() < 4) {
            Snackbar.make(headerTextView, "Feedback too short to submit", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String deviceModel = android.os.Build.MODEL;
        String deviceManufacturer = Build.MANUFACTURER;
        String OS = Build.VERSION.RELEASE + " API: " + Build.VERSION.SDK_INT;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        String dpHeight = displayMetrics.heightPixels / displayMetrics.density + "";
        String dpWidth = displayMetrics.widthPixels / displayMetrics.density + "";
        String screenSize = getScreenSize();
        String deviceTime = new Date().getTime() + "";
        String sessionType = PreferenceUtils.getPreferences(this, Constants.KEY_USER_SESSION_TYPE);
        String gpsLong, gpsLat;
        try {
            BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
            gpsLong = String.valueOf(buyerAddress.getGpsLong());
            gpsLat = String.valueOf(buyerAddress.getGpsLat());
        } catch (Exception e) {
            gpsLong = "unknown";
            gpsLat = "unknown";
        }
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String isWifiConnected = "Nope";

        if (mWifi.isConnected()) {
            isWifiConnected = "Yes";
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        String userId = String.valueOf(PreferenceUtils.getUserId(this));
        String sessionId = PreferenceUtils.getSessionId(this);

        //get app version info
        PackageInfo pInfo = null;
        String version = "unknown";
        String verCode = "unknown";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            verCode = pInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "some problem in finding app version", e);
        }



        //show progress bar
        showProgressBar(true);
        CommonIntentService.saveFeedback(this, message, deviceModel, deviceManufacturer, OS, dpHeight, dpWidth, screenSize, deviceTime, sessionType, gpsLat, gpsLong, carrierName, isWifiConnected, userId, sessionId, version, verCode);

    }

    private void showProgressBar(boolean show) {
        if (show) {
            buttonSendFeedback.setText("Sending");
            progressBar.setVisibility(View.VISIBLE);
            progressBar.show();
        } else {
            buttonSendFeedback.setText(stringSendFeedback);
            progressBar.setVisibility(View.GONE);
            progressBar.hide();
        }
    }

    private void startRotationAnimation() {
        listener = new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                System.out.println("End Animation!");
                //load_animations();
            }
        };
        new AnimationUtils();
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_infinite);
        rotation.setAnimationListener(listener);
        imageViewWeLoveYou.startAnimation(rotation);
    }

    private String getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);
        double screenInches = Math.sqrt(x + y);
        return screenInches + "";
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @OnClick(R.id.button_feedback_done)
    public void done() {
        finish();
    }

}
