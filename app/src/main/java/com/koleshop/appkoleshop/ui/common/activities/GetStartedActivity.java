package com.koleshop.appkoleshop.ui.common.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.services.SettingsIntentService;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.ui.seller.activities.SellerSettingsActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import butterknife.Bind;
import butterknife.ButterKnife;


public class GetStartedActivity extends AppCompatActivity implements SettingsIntentService.SettingsReceiver {

    @Bind(R.id.vf_ags)
    ViewFlipper viewFlipper;
    @Bind(R.id.buttonGetStarted)
    Button buttonGetStarted;
    @Bind(R.id.tv_ags_message)
    TextView textViewMessage;
    private boolean firstTimeUser;
    private boolean settingsReceivedFromInternet;
    private String title = "Koleshop";
    SellerSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.koleshop.appkoleshop.R.layout.activity_get_started);
        ButterKnife.bind(this);
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSettingsFromInternet();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == com.koleshop.appkoleshop.R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar_ags);
        toolbar.setTitle(title);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setIcon(R.drawable.action_bar_logo);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setElevation(8.0f);
        }
    }

    private void getSettingsFromInternet() {
        //show progress bar
        viewFlipper.setDisplayedChild(1);
        String requestId = CommonUtils.randomString(6);
        SettingsIntentService.requestSellerSettings(this, requestId);
    }

    public void getStarted(View view) {

        //TODO use different home screens for buyer and shopkeeper
        String sessionType = PreferenceUtils.getPreferences(this, Constants.KEY_USER_SESSION_TYPE);
        if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_SELLER)) {
            //seller session
            if(!firstTimeUser) {
                PreferenceUtils.setPreferencesFlag(this, Constants.FLAG_SELLER_SETTINGS_SETUP_FINISHED, true);
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                PreferenceUtils.setPreferencesFlag(this, Constants.FLAG_SELLER_SETTINGS_SETUP_FINISHED, false);
                Intent intentSettings = new Intent(this, SellerSettingsActivity.class);
                intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentSettings.putExtra("setupMode", true);
                startActivity(intentSettings);
            }
        } else if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_BUYER)) {
            //buyer session
            Intent intent = new Intent(this, com.koleshop.appkoleshop.ui.buyer.activities.HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onSettingsSaveSuccess() {
        //not called from this activity
    }

    @Override
    public void onSettingsSaveFailed() {
        //not called from this activity
    }

    @Override
    public void onSettingsFetchSuccess() {
        settings = KoleshopUtils.getSettingsFromCache(this);
        if(settings!=null && settings.getAddress()!=null && !TextUtils.isEmpty(settings.getAddress().getName())) {
            //this is not a first time user
            firstTimeUser = false;
        } else {
            firstTimeUser = true;
        }
        showAppropriateMessage();
    }

    @Override
    public void onSettingsFetchFailed() {
        viewFlipper.setDisplayedChild(2);
    }

    private void showAppropriateMessage() {
        viewFlipper.setDisplayedChild(0);
        if(!firstTimeUser) {
            textViewMessage.setText("Welcome back, " + settings.getAddress().getName() + "!!");
            buttonGetStarted.setText("MY SHOP");
        }
    }
}
