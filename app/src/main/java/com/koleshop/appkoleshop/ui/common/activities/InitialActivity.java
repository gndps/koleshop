package com.koleshop.appkoleshop.ui.common.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.ui.seller.activities.SelectSellerCategoryActivity;
import com.koleshop.appkoleshop.services.RegistrationIntentService;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InitialActivity extends AppCompatActivity {

    // Constants
    static private boolean TEST_MODE = false;
    static private Class TEST_CLASS = SelectSellerCategoryActivity.class;

    static Account mAccount;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "InitialActivity";
    String sessionType;

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1002;
    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()
    Context mContext;

    @Bind(R.id.btn_sell_initial) ImageButton btnSell;
    @Bind(R.id.btn_buy_initial) ImageButton btnBuy;
    @Bind(R.id.pb_initial_activity) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_initial);
        ButterKnife.bind(this);

        if (checkPlayServices()) {
            if (!TEST_MODE) {
                thugLife();
                loadUserProfileIfLoggedIn();
            } else {
                Intent intent = new Intent(getApplicationContext(), TEST_CLASS);
                startActivity(intent);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            return;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
        if(!deviceIdSyncedToServer) {
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
            if(CommonUtils.getUserId(mContext)!=null && CommonUtils.getUserId(mContext)>0) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                goToNextScreen();
            }
        } else if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_BUYER)) {
            //buyer session
            Intent intent = new Intent(this, com.koleshop.appkoleshop.ui.buyer.activities.HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        goToNextScreen();
    }

    private void setProgressing(boolean progressing) {
        progressBar.setVisibility(progressing?View.VISIBLE:View.GONE);
        btnBuy.setEnabled(!progressing);
        btnSell.setEnabled(!progressing);
    }

    public void goToNextScreen() {
        String token = PreferenceUtils.getPreferences(mContext, Constants.KEY_GOOGLE_API_TOKEN);
        if(token!=null && !token.isEmpty()) {
            goToVerifyPhoneNumberScreen();
        } else {
            if (checkPlayServices()) {
                pickUserAccount();
                setProgressing(true);
            }
        }
    }

    public void goToVerifyPhoneNumberScreen() {
        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        if(sessionType == Constants.SESSION_TYPE_BUYER) {
            intent.putExtra(Constants.KEY_SKIP_ALLOWED, true);
        }
        startActivity(intent);
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressing(false);
                    }
                });
                Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            getUsername();
        }
    }

    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (CommonUtils.isConnectedToInternet(this)) {
                new GetUsernameTask(InitialActivity.this, mEmail).execute();
            } else {
                Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void internetConnectionCheck() {
        if (!CommonUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
        }
    }

    public class GetUsernameTask extends AsyncTask<Void, Void, Void> {
        Activity mActivity;
        String mScope;
        String mEmail;

        GetUsernameTask(Activity activity, String name) {
            this.mActivity = activity;
            this.mScope = Constants.GOOGLE_API_SCOPE;
            this.mEmail = name;
        }

        /**
         * Executes the asynchronous job. This runs when you call execute()
         * on the AsyncTask instance.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String token = fetchToken();
                if (token != null) {
                    // **Insert the good stuff here.**
                    // Use the token to access the user's Google data.
                    //todo encrypt for saving
                    PreferenceUtils.setPreferences(mContext, Constants.KEY_GOOGLE_API_TOKEN, token);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setProgressing(false);
                        }
                    });

                    goToVerifyPhoneNumberScreen();
                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressing(false);
                    }
                });
                internetConnectionCheck();
            }
            return null;
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present
                // so we need to show the user some UI in the activity to recover.
                ((InitialActivity) mActivity).handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                Toast.makeText(mContext, "Some problem occurred. Please restart the app.", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            InitialActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }
}
