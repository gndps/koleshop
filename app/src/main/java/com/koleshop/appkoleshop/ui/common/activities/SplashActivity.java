package com.koleshop.appkoleshop.ui.common.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.services.RegistrationIntentService;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

public class SplashActivity extends AppCompatActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "SplashActivity";

    private Context mContext;
    String sessionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;

        if (checkPlayServices()) {
            thugLife();
            loadUserProfileIfLoggedIn();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            return;
        }

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * first method that run in InitialActivity
     */
    private void thugLife() {

        //1. delete the network request statuses
        new AsyncTask<Void, Void, Void>(
        ) {
            @Override
            protected Void doInBackground(Void... params) {
                PreferenceUtils.deleteNetworkRequestStatusPreferences(mContext);
                return null;
            }
        }.execute();

        //2. update the token on the server if needed
        boolean deviceIdSyncedToServer = PreferenceUtils.getPreferencesFlag(mContext, Constants.FLAG_DEVICE_ID_SYNCED_TO_SERVER);
        if (!deviceIdSyncedToServer) {
            Intent tokenRefreshIntent = new Intent(mContext, RegistrationIntentService.class);
            tokenRefreshIntent.setAction(RegistrationIntentService.REGISTRATION_INTENT_SERVICE_ACTION_UPDATE_TOKEN_ON_SERVER);
            startService(tokenRefreshIntent);
        }
    }

    /**
     * second method that run in InitialActivity
     */
    private void loadUserProfileIfLoggedIn() {

        String sessionType = PreferenceUtils.getPreferences(this, Constants.KEY_USER_SESSION_TYPE);
        if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_SELLER)) {
            //seller session
            //if logged in
            int currentRealmVersion = PreferenceUtils.getCurrentRealmVersion(mContext);
            if(currentRealmVersion<Constants.REALM_VERSION) {
                RealmUtils.resetRealm(mContext);
            }
            if (CommonUtils.getUserId(mContext) != null && CommonUtils.getUserId(mContext) > 0) {
                //if settings setup is finished then open home
                boolean settingsSetupFinished = PreferenceUtils.getPreferencesFlag(this, Constants.FLAG_SELLER_SETTINGS_SETUP_FINISHED);
                if (settingsSetupFinished) {
                    //go to home activity
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    //go to get started activity
                    Intent intent = new Intent(this, GetStartedActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
                goToNextScreen();
            }
        } else if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_BUYER)) {
            //buyer session
            boolean userLoggedIn = CommonUtils.getUserId(mContext) != null && CommonUtils.getUserId(mContext) > 0;
            int currentRealmVersion = PreferenceUtils.getCurrentRealmVersion(mContext);
            if(currentRealmVersion<Constants.REALM_VERSION) {
                RealmUtils.resetRealm(mContext);
            }
            BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
            boolean deliveryLocationSelected = buyerAddress != null;
            //if(userLoggedIn || deliveryLocationSelected) {
            if (deliveryLocationSelected) {
                Intent intent = new Intent(this, com.koleshop.appkoleshop.ui.buyer.activities.HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                //user need to select the delivery location
                goToNextScreen();
            }

        } else {
            //let the user choose the session type
            startInitialActivity();
        }

    }

    public void goToNextScreen() {
        String token = PreferenceUtils.getPreferences(mContext, Constants.KEY_GOOGLE_API_TOKEN);
        if (token != null && !token.isEmpty()) {
            if (sessionType == Constants.SESSION_TYPE_SELLER) {
                goToVerifyPhoneNumberScreen();
            } else {
                setLocationToStartShopping();
            }
        } else {
            if (checkPlayServices()) {
                //go to initial activity
                startInitialActivity();
            }
        }
    }

    private void startInitialActivity() {
        Intent initialActivityIntent = new Intent(mContext, InitialActivity.class);
        initialActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(initialActivityIntent);
    }

    private void setLocationToStartShopping() {
        Intent mapsActivityIntent = new Intent(mContext, MapsActivity.class);
        mapsActivityIntent.putExtra("twoButtonMode", false);
        mapsActivityIntent.putExtra("title", getString(R.string.title_set_delivery_location));
        mapsActivityIntent.putExtra("markerTitle", "Delivery location");
        mapsActivityIntent.putExtra("actionButtonTitle", getString(R.string.title_start_shopping));
        startActivity(mapsActivityIntent);
    }

    public void goToVerifyPhoneNumberScreen() {
        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        if (sessionType == Constants.SESSION_TYPE_BUYER) {
            intent.putExtra(Constants.KEY_SKIP_ALLOWED, true);
        }
        startActivity(intent);
    }

}
