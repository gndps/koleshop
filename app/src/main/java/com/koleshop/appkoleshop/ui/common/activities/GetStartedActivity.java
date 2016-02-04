package com.koleshop.appkoleshop.ui.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;


public class GetStartedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.koleshop.appkoleshop.R.layout.activity_get_started);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.koleshop.appkoleshop.R.menu.get_started, menu);
        return true;
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

    /**
     * "Get Started" button pressed
     * @param view
     */
    public void getStarted(View view) {
        //TODO use different home screens for buyer and shopkeeper
        String sessionType = PreferenceUtils.getPreferences(this, Constants.KEY_USER_SESSION_TYPE);
        if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_SELLER)) {
            //seller session
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (!sessionType.isEmpty() && sessionType.equalsIgnoreCase(Constants.SESSION_TYPE_BUYER)) {
            //buyer session
            Intent intent = new Intent(this, com.koleshop.appkoleshop.ui.buyer.activities.HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
