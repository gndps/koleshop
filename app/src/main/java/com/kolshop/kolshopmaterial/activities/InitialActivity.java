package com.kolshop.kolshopmaterial.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.GlobalData;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.constant.Prefs;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.model.Session;
import com.google.gson.Gson;

public class InitialActivity extends ActionBarActivity {

    // Constants

    static Account mAccount;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "InitialActivity";
    int userType;

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
            loadUserProfileIfLoggedIn();
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void loadUserProfileIfLoggedIn() {

        GlobalData globalData = GlobalData.getInstance();
        Session session = PreferenceUtils.getSession(this);
        //if session active, then go to home activity
        if (session != null && !session.getSessionId().isEmpty()) {
            globalData.setSession(session);
            if (session.getSessionType() == Constants.SHOPKEEPER_SESSION) {
                //IF SHOPKEEPER SESSION ACTIVE
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);//start db sync
            } else if (session.getSessionType() == Constants.BUYER_SESSION) {
                //IF BUYER SESSION ACTIVE
            } else {
                //IF SESSION TYPE NOT AVAILABLE
                Intent intent = new Intent(this, ChooseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        //else let the user choose the session type
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
        userType = Constants.USER_TYPE_BUYER;
        PreferenceUtils.setPreferences(this, Constants.KEY_USER_TYPE, String.valueOf(userType));
        if(checkPlayServices())
        {
            goToVerifyPhoneNumberScreen();
        }
    }

    public void goSell(View v) {
        userType = Constants.USER_TYPE_SELLER;
        PreferenceUtils.setPreferences(this, Constants.KEY_USER_TYPE, String.valueOf(userType));
        if(checkPlayServices())
        {
            goToVerifyPhoneNumberScreen();
        }
    }

    public void goToVerifyPhoneNumberScreen() {
        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        intent.putExtra(Constants.KEY_USER_TYPE, userType+"");
        startActivity(intent);
    }
}
