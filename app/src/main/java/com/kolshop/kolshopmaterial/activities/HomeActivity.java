package com.kolshop.kolshopmaterial.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.common.util.RealmUtils;
import com.kolshop.kolshopmaterial.fragments.product.ProductListFragment;
import com.kolshop.kolshopmaterial.services.CommonIntentService;

import java.util.Arrays;

import io.realm.Realm;

public class HomeActivity extends ActionBarActivity {

    private BroadcastReceiver homeActivityBroadcastReceiver;
    private ProgressDialog dialog;
    private boolean loadedProductCategories,loadedMeasuringUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (getIntent().getBooleanExtra("CLOSE_APPLICATION", false))
        {
            finish();
        }
        else {
            initializeBroadcastReceivers();
            loadInitialData();
            if (savedInstanceState == null) {
                //set default fragment
                setFragment(getNavigationDrawerArray()[0]);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_NAVIGATION_ITEM_SELECTED));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_MEASURING_UNITS_LOAD_SUCCESS));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeBroadcastReceivers() {
        homeActivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_NAVIGATION_ITEM_SELECTED)) {
                    String fragmentName = intent.getStringExtra("NavigationItem");
                    setFragment(fragmentName);
                }
                else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS)) {
                    loadedProductCategories = true;
                    if(loadedMeasuringUnits)
                    {
                        initialDataLoadingComplete();
                    }
                }
                else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED)) {
                    dialog.dismiss();
                    loadedProductCategories = false;
                    if(!CommonUtils.isConnectedToInternet(context))
                    {
                        showInternetConnectionPopup();
                    }
                    else
                    {
                        showRetryLoadingPopup();
                    }
                }
                else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_MEASURING_UNITS_LOAD_SUCCESS)) {
                    loadedMeasuringUnits = true;
                    if(loadedProductCategories)
                    {
                        initialDataLoadingComplete();
                    }
                }
                else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED)) {
                    dialog.dismiss();
                    loadedMeasuringUnits = false;
                    if(!CommonUtils.isConnectedToInternet(context))
                    {
                        showInternetConnectionPopup();
                    }
                    else
                    {
                        showRetryLoadingPopup();
                    }
                }
            }
        };
    }

    private void setFragment(String fragmentName)
    {
        String[] navigationDrawerItems = getResources().getStringArray(R.array.navigation_drawer_array);
        int navigationDrawerItemIndex = Arrays.asList(navigationDrawerItems).indexOf(fragmentName);
        switch (navigationDrawerItemIndex){
            case 0:
                //Products
                ProductListFragment productListFragment = new ProductListFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, productListFragment).commit();
                break;
            case 1:
                //Log out
                PreferenceUtils.saveSession(this, "");
                Intent intent = new Intent(this, InitialActivity.class);
                startActivity(intent);
                break;
            case 2:
                //Add/Edit Products
                Intent intent2 = new Intent(this, AddEditProductActivity.class);
                startActivity(intent2);
                break;
            case 3:
                //Settings
                Intent intent3 = new Intent(this, ShopSettingsActivity.class);
                startActivity(intent3);
                break;
        }
    }

    private String[] getNavigationDrawerArray()
    {
        return getResources().getStringArray(R.array.navigation_drawer_array);
    }

    private void loadInitialData()
    {

        Realm.deleteRealmFile(this);

        loadedProductCategories = Boolean.valueOf(RealmUtils.getRealmPrefs(getApplicationContext(), Constants.FLAG_PRODUCT_CATEGORIES_LOADED));
        loadedMeasuringUnits = Boolean.valueOf(RealmUtils.getRealmPrefs(getApplicationContext(), Constants.FLAG_MEASURING_UNITS_LOADED));

        if(!loadedProductCategories || !loadedMeasuringUnits) {
            dialog = ProgressDialog.show(this, "Loading first time data", "Please wait...", true);
        }

        if(!loadedProductCategories) {
            loadProductCategories();
        }

        if(!loadedMeasuringUnits) {
            loadMeasuringUnits();
        }
    }

    private void loadProductCategories()
    {
            Intent commonIntentService = new Intent(this, CommonIntentService.class);
            commonIntentService.setAction(CommonIntentService.ACTION_LOAD_PRODUCT_CATEGORIES);
            startService(commonIntentService);
    }

    private void loadMeasuringUnits()
    {
            Intent commonIntentService = new Intent(this, CommonIntentService.class);
            commonIntentService.setAction(CommonIntentService.ACTION_LOAD_MEASURING_UNITS);
            startService(commonIntentService);
    }

    private void initialDataLoadingComplete()
    {
        dialog.dismiss();
        //TODO show tutorial if first time use
    }

    private void showInternetConnectionPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No internet connection. Try Again?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadInitialData();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeTheApplication();
                    }
                });
        builder.create().show();
    }

    private void showRetryLoadingPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Some problem occurred while loading. Try Again?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadInitialData();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeTheApplication();
                    }
                });
        builder.create().show();
    }

    private void closeTheApplication()
    {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("CLOSE_APPLICATION", true);
        startActivity(intent);
    }

}
