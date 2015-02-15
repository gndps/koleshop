package com.gndps.kolshopmaterial.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gndps.kolshopmaterial.R;
import com.gndps.kolshopmaterial.common.GlobalData;
import com.gndps.kolshopmaterial.common.constant.Constants;
import com.gndps.kolshopmaterial.common.constant.Prefs;
import com.gndps.kolshopmaterial.model.Session;
import com.google.gson.Gson;

public class InitialActivity extends Activity {

    // Constants

    static Account mAccount;

    Button btnSignUp, btnLogIn;

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
        processIfUserLoggedIn();


        // Create the dummy account
        mAccount = CreateSyncAccount(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void processIfUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
        if (!prefs.getString("session", "").trim().equalsIgnoreCase("")) {
            Gson gson = new Gson();
            GlobalData globalData = GlobalData.getInstance();
            Session session = gson.fromJson(prefs.getString("session", ""), Session.class);
            if (!session.getSessionId().isEmpty()) {
                globalData.setSession(session);
                if (session.getSessionType() == Constants.SHOPKEEPER_SESSION || session.getSessionType() == Constants.BUYER_SESSION) {
                    if (session.getSessionType() == Constants.SHOPKEEPER_SESSION) {
                        Intent intent = new Intent(this, ShopSettingsActivity.class);
                        startActivity(intent);
                        //start db sync
                    } else if (session.getSessionType() == Constants.BUYER_SESSION) {
                        //go to buyer home screen
                    }
                } else {
                    Intent intent = new Intent(this, ChooseActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else if (!session.getUsername().isEmpty()) {
                //go to login screen
            }
        }
    }

    private void initializeButtonHandlers() {
        btnSignUp = (Button) findViewById(R.id.buttonLogin_Login);
        btnLogIn = (Button) findViewById(R.id.buttonBuyer);
    }

    public void signUp(final View view) {
        SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("session", null);
        editor.commit();
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void logIn(final View view) {
        SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("session", null);
        editor.commit();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
