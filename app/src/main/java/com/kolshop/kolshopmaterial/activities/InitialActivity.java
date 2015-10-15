package com.kolshop.kolshopmaterial.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;

public class InitialActivity extends ActionBarActivity {

    // Constants
    static private boolean TEST_MODE = false;
    static private Class TEST_CLASS = SelectSellerCategoryActivity.class;

    static Account mAccount;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "InitialActivity";
    String sessionType;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                Constants.ACCOUNT, Constants.ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }

    public static Account getmAccount() {
        return mAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_initial);

        if (checkPlayServices()) {
            // Create the dummy account
            mAccount = CreateSyncAccount(this);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPlayServices())
        {
            if(!TEST_MODE) {
                loadUserProfileIfLoggedIn();
            } else {
                Intent intent = new Intent(getApplicationContext(), TEST_CLASS);
                startActivity(intent);
            }
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void loadUserProfileIfLoggedIn() {

        String sessionType = PreferenceUtils.getPreferences(this, Constants.KEY_USER_SESSION_TYPE);
        if(!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_SELLER)) {
            //seller session
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if(!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_BUYER)) {
            //buyer session
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            //let the user choose the session type
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

    public void goBuy(View v) {
        sessionType = Constants.SESSION_TYPE_BUYER;
        next();
    }

    public void goSell(View v) {
        sessionType = Constants.SESSION_TYPE_SELLER;
        next();
    }

    private void next() {
        PreferenceUtils.setPreferences(this, Constants.KEY_USER_SESSION_TYPE, sessionType);
        if(checkPlayServices())
        {
            goToVerifyPhoneNumberScreen();
        }
    }

    public void goToVerifyPhoneNumberScreen() {
        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        intent.putExtra(Constants.KEY_SKIP_ALLOWED, true);
        startActivity(intent);
    }
}
