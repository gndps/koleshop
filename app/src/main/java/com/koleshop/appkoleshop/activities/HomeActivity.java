package com.koleshop.appkoleshop.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.fragments.DummyHomeFragment;
import com.koleshop.appkoleshop.fragments.productedit.ProductVarietyEditFragment;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.koleshop.appkoleshop.fragments.product.InventoryCategoryFragment;

import butterknife.BindString;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;

public class HomeActivity extends AppCompatActivity {

    private BroadcastReceiver homeActivityBroadcastReceiver;
    private ProgressDialog dialog;
    private boolean loadedProductCategories, loadedBrands;
    private Context mContext;
    private DrawerLayout drawerLayout;
    View content;
    private String TAG = "HomeActivity";
    private int FRAGMENT_HOME = 0x00;
    private int FRAGMENT_MY_SHOP = 0x01;
    private int FRAGMENT_WAREHOUSE = 0x02;
    private int selectedFragment;
    private String titleOnBackPressed;

    @BindString(R.string.navigation_drawer_products) String titleMyShop;
    @BindString(R.string.navigation_drawer_inventory) String titleWareHouse;
    @BindString(R.string.navigation_drawer_home) String titleHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.koleshop.appkoleshop.R.layout.activity_home);
        ButterKnife.bind(this);
        content = findViewById(com.koleshop.appkoleshop.R.id.home_coordinatorlayout);
        mContext = this;
        setupToolbar();
        setupDrawerLayout();
        addInitialFragment();
        if (getIntent().getBooleanExtra("CLOSE_APPLICATION", false)) {
            finish();
        } else {
            initializeBroadcastReceivers();
            loadInitialData();
        }
        String registrationId = PreferenceUtils.getRegistrationId(mContext);
        Log.d(TAG, "registrationId = " + registrationId);

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        Log.d(TAG, "width = " + dpWidth + "dp");
        Log.d(TAG, "height = " + dpHeight + "dp");

        //Toast.makeText(mContext, "width = " + dpWidth + "dp, height = " + dpHeight + "dp", Toast.LENGTH_SHORT).show();
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(com.koleshop.appkoleshop.R.id.app_bar);
        toolbar.setTitle(titleHome);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(com.koleshop.appkoleshop.R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);

        }
    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(com.koleshop.appkoleshop.R.id.drawer_layout);
        final NavigationView view = (NavigationView) findViewById(com.koleshop.appkoleshop.R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.drawer_home:
                        getSupportActionBar().setTitle(titleHome);
                        DummyHomeFragment dummyHomeFragment = new DummyHomeFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, dummyHomeFragment).commit();
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        selectedFragment = FRAGMENT_HOME;
                        return true;
                    case R.id.drawer_products:
                        //Products
                        getSupportActionBar().setTitle(titleMyShop);
                        InventoryCategoryFragment myInventoryCategoryFragment = new InventoryCategoryFragment();
                        Bundle bundleMyInventory = new Bundle();
                        bundleMyInventory.putBoolean("myInventory", true);
                        myInventoryCategoryFragment.setArguments(bundleMyInventory);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, myInventoryCategoryFragment, "frag_my_shop").commit();
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        selectedFragment = FRAGMENT_MY_SHOP;
                        return true;
                    case R.id.drawer_inventory:
                        //Drawer inventory
                        getSupportActionBar().setTitle(titleWareHouse);
                        InventoryCategoryFragment inventoryCategoryFragment = new InventoryCategoryFragment();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, inventoryCategoryFragment, "frag_ware_house").commit();
                        //Snackbar.make(content, menuItem.getTitle() + " opened", Snackbar.LENGTH_LONG).show();
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        selectedFragment = FRAGMENT_WAREHOUSE;
                        return true;
                    /*case R.id.drawer_add_edit:
                        //Add/Edit Products
                        //getSupportActionBar().setTitle("Inventory categories");
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        //todo do some stuff
                        Intent addEditProductIntent = new Intent(mContext, AddEditProductActivity.class);
                        startActivity(addEditProductIntent);
                        return true;*/
                    case R.id.drawer_settings:
                        //Settings
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        Intent intent3 = new Intent(mContext, ShopSettingsActivity.class);
                        startActivity(intent3);
                        return true;
                    case R.id.drawer_login:
                        //Log In  or Log out
                        drawerLayout.closeDrawers();
                        Intent intentLogin = new Intent(mContext, VerifyPhoneNumberActivity.class);
                        startActivity(intentLogin);
                        return true;
                    case R.id.drawer_logout:
                        drawerLayout.closeDrawers();
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Are you sure ?")
                                .setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        PreferenceUtils.setPreferences(mContext, Constants.KEY_USER_ID, "");
                                        PreferenceUtils.setPreferences(mContext, Constants.KEY_SESSION_ID, "");
                                        view.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_login).setVisible(true);
                                        view.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_logout).setVisible(false);
                                    }
                                })
                                .setNegativeButton("CANCEL", null);
                        builder.create().show();
                        return false;
                    /*case R.id.drawer_test:
                        getSupportActionBar().setTitle("Testing cardview shit");
                        ProductVarietyEditFragment fragment = new ProductVarietyEditFragment();
                        Bundle bundl = new Bundle();
                        bundl.putLong("categoryId", 120l);
                        fragment.setArguments(bundl);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment).commit();
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        return true;*/
                }

                return true;
            }
        });

        //setup login and logout buttons visibility
        boolean loggedIn = PreferenceUtils.isUserLoggedIn(mContext);

        if (loggedIn) {
            view.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_login).setVisible(false);
            view.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_logout).setVisible(true);
        } else {
            view.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_login).setVisible(true);
            view.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_logout).setVisible(false);
        }
    }

    private void addInitialFragment() {
        DummyHomeFragment dummyHomeFragment = new DummyHomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(com.koleshop.appkoleshop.R.id.fragment_container, dummyHomeFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED));
        //lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_MEASURING_UNITS_LOAD_SUCCESS));
        //lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_MEASURING_UNITS_LOAD_FAILED));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_BRANDS_LOAD_SUCCESS));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_BRANDS_LOAD_FAILED));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_SWITCH_TO_WAREHOUSE));
        lbm.registerReceiver(homeActivityBroadcastReceiver, new IntentFilter(Constants.ACTION_SWITCH_BACK_TO_MY_SHOP));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(homeActivityBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.koleshop.appkoleshop.R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == com.koleshop.appkoleshop.R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(selectedFragment == FRAGMENT_WAREHOUSE) {
            InventoryCategoryFragment inventoryCategoryFragment = (InventoryCategoryFragment) getFragmentManager().findFragmentByTag("frag_my_shop");
            if(inventoryCategoryFragment.isBackAllowed()) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    if (titleOnBackPressed != null && !titleOnBackPressed.isEmpty()) {
                        getSupportActionBar().setTitle(titleMyShop);
                        titleOnBackPressed = "";
                    }
                } else {
                    super.onBackPressed();
                }
            }
        } else if(selectedFragment == FRAGMENT_MY_SHOP) {
            InventoryCategoryFragment myInventoryCategoryFragment = (InventoryCategoryFragment) getFragmentManager().findFragmentByTag("frag_my_shop");
            if(myInventoryCategoryFragment.isBackAllowed()) {
                super.onBackPressed();
            }
        }
    }

    private void initializeBroadcastReceivers() {
        homeActivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS)) {
                    loadedProductCategories = true;
                    if (loadedBrands) {
                        initialDataLoadingComplete();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_CATEGORIES_LOAD_FAILED)) {
                    dialog.dismiss();
                    loadedProductCategories = false;
                    if (!CommonUtils.isConnectedToInternet(context)) {
                        showInternetConnectionPopup();
                    } else {
                        showRetryLoadingPopup();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_BRANDS_LOAD_SUCCESS)) {
                    loadedBrands = true;
                    if (loadedProductCategories) {
                        initialDataLoadingComplete();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_BRANDS_LOAD_FAILED)) {
                    dialog.dismiss();
                    loadedBrands = false;
                    if (!CommonUtils.isConnectedToInternet(context)) {
                        showInternetConnectionPopup();
                    } else {
                        showRetryLoadingPopup();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_SWITCH_TO_WAREHOUSE)) {

                    InventoryCategoryFragment inventoryCategoryFragment = new InventoryCategoryFragment();
                    Bundle bundleMyInventory = new Bundle();
                    bundleMyInventory.putBoolean("switchedFromMyShop", true);
                    inventoryCategoryFragment.setArguments(bundleMyInventory);

                    getSupportActionBar().setTitle(titleWareHouse);
                    titleOnBackPressed = titleMyShop;
                    selectedFragment = FRAGMENT_WAREHOUSE;

                    android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out);
                    ft.replace(R.id.fragment_container, inventoryCategoryFragment, "frag_ware_house");
                    ft.addToBackStack(null);
                    ft.commit();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_SWITCH_BACK_TO_MY_SHOP)) {
                    onBackPressed();
                }
            }
        };
    }

    /*private void setFragment(String fragmentName)
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
                //Log In  or Log out
                boolean loggedIn = false;
                if(!PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_ID).isEmpty()) {
                    loggedIn = true;
                }

                if(loggedIn) {
                    //clear user_id
                    PreferenceUtils.setPreferences(this, Constants.KEY_USER_ID, "");
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    Intent broadcastIntent = new Intent(Constants.ACTION_LOG_OUT);
                    broadcastManager.sendBroadcast(broadcastIntent);
                } else {
                    Intent intent = new Intent(mContext, VerifyPhoneNumberActivity.class);
                    startActivity(intent);
                }


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
            case 4:
                ProductMasterListFragment productMasterListFragment = new ProductMasterListFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, productMasterListFragment).commit();
                break;
        }
    }*/

    private String[] getNavigationDrawerArray() {
        return getResources().getStringArray(com.koleshop.appkoleshop.R.array.navigation_drawer_array);
    }

    private void loadInitialData() {
        if (Constants.RESET_REALM) {
            deleteRealmPreferences();
        }

        loadedProductCategories = PreferenceUtils.getPreferencesFlag(mContext, Constants.FLAG_PRODUCT_CATEGORIES_LOADED);
        loadedBrands = PreferenceUtils.getPreferencesFlag(mContext, Constants.FLAG_BRANDS_LOADED);
        //loadedBrands = true;

        if (!loadedProductCategories || !loadedBrands) {
            //todo add some fun fact in loading screen
            dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        }

        if (!loadedProductCategories) {
            loadProductCategories();
        }

        if (!loadedBrands) {
            loadBrands();
        }
    }

    private void deleteRealmPreferences() {
        //CommonUtils.closeRealm(mContext);
        PreferenceUtils.setPreferencesFlag(mContext, Constants.FLAG_PRODUCT_CATEGORIES_LOADED, false);
        PreferenceUtils.setPreferencesFlag(mContext, Constants.FLAG_BRANDS_LOADED, false);
        try {
            Realm.getDefaultInstance().close();
        } catch (Exception e) {
            //dont give a damn
        }
        try {
            Realm r = Realm.getDefaultInstance();
            r.close();
            if (r != null) {
                Realm.deleteRealm(new RealmConfiguration.Builder(mContext).name("default.realm").build());
            }
        } catch (RealmException e) {
            Log.e(TAG, "realm exception", e);
        } finally {
            Realm.deleteRealm(new RealmConfiguration.Builder(mContext).name("default.realm").build());
        }
    }

    private void loadProductCategories() {
        Intent commonIntentService = new Intent(this, CommonIntentService.class);
        commonIntentService.setAction(CommonIntentService.ACTION_LOAD_PRODUCT_CATEGORIES);
        startService(commonIntentService);
    }

    private void loadBrands() {
        Intent commonIntentService = new Intent(this, CommonIntentService.class);
        commonIntentService.setAction(CommonIntentService.ACTION_LOAD_BRANDS);
        startService(commonIntentService);
    }

    private void initialDataLoadingComplete() {
        dialog.dismiss();
        //TODO show tutorial if first time use
    }

    private void showInternetConnectionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Device is not connected to internet. Try Again?")
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

    private void showRetryLoadingPopup() {
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

    private void closeTheApplication() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("CLOSE_APPLICATION", true);
        startActivity(intent);
    }

}
