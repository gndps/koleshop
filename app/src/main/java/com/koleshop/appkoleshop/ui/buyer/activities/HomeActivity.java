package com.koleshop.appkoleshop.ui.buyer.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.ui.buyer.fragments.AddressesFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.NearbyShopsFragment;
import com.koleshop.appkoleshop.ui.common.activities.VerifyPhoneNumberActivity;
import com.koleshop.appkoleshop.ui.common.fragments.NotImplementedFragment;
import com.koleshop.appkoleshop.ui.common.interfaces.FragmentHomeActivityListener;
import com.koleshop.appkoleshop.ui.seller.fragments.DummyHomeFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.mypopsy.widget.FloatingSearchView;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;

public class HomeActivity extends AppCompatActivity implements FragmentHomeActivityListener, NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog dialog;
    private Context mContext;
    private DrawerLayout drawerLayout;
    private final String TAG = "HomeActivity";
    private final String FRAGMENT_HOME_TAG = "home";
    private final String FRAGMENT_NEARBY_SHOPS_TAG = "nearby_shops";
    private final String FRAGMENT_NOT_IMPL = "not_impl";
    private final String FRAGMENT_ADDRESSES_TAG = "addresses";
    private String lastFragmentTag;
    private boolean lastFragmentShowed, isLastFragmentSupportType;
    NavigationView navigationView;
    private boolean backHandled;
    private SupportAnimator animator;
    private Toolbar toolbar;

    @BindString(R.string.navigation_drawer_nearby_shops)
    String titleNearbyShops;
    @BindString(R.string.navigation_drawer_inventory)
    String titleWareHouse;
    @BindString(R.string.navigation_drawer_home)
    String titleHome;
    @BindString(R.string.navigation_drawer_addresses)
    String titleAddresses;
    @Bind(R.id.floating_search_bar)
    FloatingSearchView searchBar;
    private boolean searchBarVisible;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_buyer);

        if (getIntent().getBooleanExtra("CLOSE_APPLICATION", false)) {
            finish();
        } else {
            firstTime = getIntent().getBooleanExtra("firstTime", false);
            ButterKnife.bind(this);
            mContext = this;
            setupToolbar();
            setupDrawerLayout();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayView(item);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(com.koleshop.appkoleshop.R.menu.menu_seller_home, menu);
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

            case R.id.menu_item_search:
                //open search overlay activity
                View menuView = findViewById(R.id.menu_item_search);
                revealSearchBar(menuView, true);
                return true;

        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (searchBarVisible) {
            revealSearchBar(null, false);
        } else {
            if (backHandled || lastFragmentTag.equalsIgnoreCase(FRAGMENT_HOME_TAG)) {
                super.onBackPressed();
            } else {
                showHome();
            }
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
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
        //initialize layout elements
        drawerLayout = (DrawerLayout) findViewById(com.koleshop.appkoleshop.R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        refreshLoginLogoutStates();
        if(firstTime) {
            showNearbyShops();
        } else {
            showHome();
        }
    }

    private void showHome() {
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_home);
        if (item != null) {
            displayView(item);
        }
        setElevation(8);
        setTitle(titleHome);
    }

    private void showNearbyShops() {
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_nearby_shops);
        if (item != null) {
            displayView(item);
        }
        setElevation(8);
        setTitle(titleNearbyShops);
    }

    private void revealSearchBar(View view, final boolean reveal) {

        int ANIMATION_DURATION = 350;

        if (reveal) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;

            // get the center for the clipping circle
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int cx = location[0] + 12;
            int cy = location[1] + 12;

            // get the final radius for the clipping circle
            int dx = Math.max(cx, width - cx);
            int dy = Math.max(cy, 56 - cy);
            float finalRadius = (float) Math.hypot(dx, dy);

            animator = ViewAnimationUtils.createCircularReveal(searchBar, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(ANIMATION_DURATION);
            animator.removeAllListeners();
            animator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                    searchBar.setVisibility(View.VISIBLE);
                    searchBar.setActivated(true);
                    configureSearchBar(true);
                }

                @Override
                public void onAnimationEnd() {
                }

                @Override
                public void onAnimationCancel() {

                }

                @Override
                public void onAnimationRepeat() {

                }
            });
        } else {
            animator = animator.reverse();
            animator.removeAllListeners();
            animator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                    configureSearchBar(false);
                }

                @Override
                public void onAnimationEnd() {
                    searchBar.setActivated(false);
                    searchBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel() {

                }

                @Override
                public void onAnimationRepeat() {

                }
            });
        }
        animator.start();
        searchBarVisible = reveal;

    }

    private void configureSearchBar(boolean config) {
        if(config) {
            searchBar.setIcon(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_arrow_back_grey600_24dp));
            searchBar.setOnSearchFocusChangedListener(new FloatingSearchView.OnSearchFocusChangedListener() {
                @Override
                public void onFocusChanged(boolean b) {
                    revealSearchBar(null, false);
                }
            });
            searchBar.setOnIconClickListener(new FloatingSearchView.OnIconClickListener() {
                @Override
                public void onNavigationClick() {
                    onBackPressed();
                }
            });
        } else {
            searchBar.setOnSearchFocusChangedListener(null);
            searchBar.setIcon(null);
            searchBar.setOnIconClickListener(null);
        }
    }

    private void refreshLoginLogoutStates() {
        //setup login and logout buttons visibility
        boolean loggedIn = PreferenceUtils.isUserLoggedIn(mContext);
        if (loggedIn) {
            navigationView.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_login).setVisible(false);
            navigationView.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_logout).setVisible(true);
        } else {
            navigationView.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_login).setVisible(true);
            navigationView.getMenu().findItem(com.koleshop.appkoleshop.R.id.drawer_logout).setVisible(false);
        }
    }

    public void displayView(MenuItem item) {

        if (item == null) return;

        int viewId = item.getItemId();

        String title = getString(R.string.app_name);
        boolean closeDrawers;
        boolean setItemChecked;

        switch (viewId) {

            case R.id.drawer_home:
                //dummy home fragment
                DummyHomeFragment dummyHomeFragment = new DummyHomeFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("buyerMode", true);
                dummyHomeFragment.setArguments(bundle);
                title = titleHome;
                setItemChecked = true;
                closeDrawers = true;
                replaceFragment(dummyHomeFragment, FRAGMENT_HOME_TAG);
                break;

            case R.id.drawer_nearby_shops:
                //Products
                title = titleNearbyShops;
                NearbyShopsFragment nearbyShopsFragment = new NearbyShopsFragment();
                setItemChecked = true;
                closeDrawers = true;
                replaceFragment(nearbyShopsFragment, FRAGMENT_NEARBY_SHOPS_TAG);
                break;

            case R.id.drawer_cart:
                //Cart
                Intent intentCart = new Intent(mContext, CartActivity.class);
                startActivity(intentCart);
                setItemChecked = true;
                closeDrawers = true;
                break;

            case R.id.drawer_addresses:
                //Cart
                title = titleAddresses;
                AddressesFragment addressesFragment = AddressesFragment.newInstance(true, 1l);
                setItemChecked = true;
                closeDrawers = true;
                replaceFragment(addressesFragment, FRAGMENT_ADDRESSES_TAG);
                break;


            /*case R.id.drawer_inventory:
                //Drawer inventory
                title = titleWareHouse;
                fragment = new InventoryCategoryFragment();
                setItemChecked = true;
                closeDrawers = true;
                replaceFragment = true;
                selectedFragment = FRAGMENT_WAREHOUSE;
                break;*/

            case R.id.drawer_settings:
                //Settings
                setItemChecked = true;
                closeDrawers = true;
                NotImplementedFragment notImplementedFragment = new NotImplementedFragment();
                replaceFragment(notImplementedFragment, FRAGMENT_NOT_IMPL);
                //Intent intent3 = new Intent(mContext, BuyerSettingsActivity.class);
                //startActivity(intent3);
                break;

            case R.id.drawer_login:
                //Log In  or Log out
                closeDrawers = true;
                setItemChecked = false;
                Intent intentLogin = new Intent(mContext, VerifyPhoneNumberActivity.class);
                startActivity(intentLogin);
                break;

            case R.id.drawer_logout:
                closeDrawers = true;
                setItemChecked = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure ?")
                        .setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PreferenceUtils.setPreferences(mContext, Constants.KEY_USER_ID, "");
                                PreferenceUtils.setPreferences(mContext, Constants.KEY_SESSION_ID, "");
                                refreshLoginLogoutStates();
                            }
                        })
                        .setNegativeButton("CANCEL", null);
                builder.create().show();
                break;
            default:
                closeDrawers = true;
                setItemChecked = true;
                NotImplementedFragment notImplementedFragment2 = new NotImplementedFragment();
                replaceFragment(notImplementedFragment2, FRAGMENT_NOT_IMPL);
                break;
        }

        //set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        if (closeDrawers) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        item.setChecked(setItemChecked);

    }

    private void replaceFragment(Fragment fragment, String fragmentTag) {
        if (lastFragmentShowed && isLastFragmentSupportType) {
            android.support.v4.app.Fragment fr_v4 = getSupportFragmentManager().findFragmentByTag(lastFragmentTag);
            if (fr_v4 != null) {
                getSupportFragmentManager().beginTransaction().remove(fr_v4).commit();
            }
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, fragmentTag)
                .commit();
        lastFragmentTag = fragmentTag;
        lastFragmentShowed = true;
        isLastFragmentSupportType = false;
    }

    private void replaceFragment(android.support.v4.app.Fragment fragment, String fragmentTag) {
        if (lastFragmentShowed && !isLastFragmentSupportType) {
            Fragment fr = getFragmentManager().findFragmentByTag(lastFragmentTag);
            if (fr != null) {
                getFragmentManager().beginTransaction().remove(fr).commit();
            }
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, fragmentTag)
                .commit();
        lastFragmentTag = fragmentTag;
        lastFragmentShowed = true;
        isLastFragmentSupportType = true;
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

    /*private void showInternetConnectionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Device is not connected to internet.")
                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create().show();
    }*/

    private void closeTheApplication() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("CLOSE_APPLICATION", true);
        startActivity(intent);
    }

    @Override
    public void setElevation(int elevation) {
        try {
            getSupportActionBar().setElevation(elevation);
        } catch (Exception e) {
            Log.d(TAG, "elevation not set", e);
        }
    }

    @Override
    public void setTitle(String title) {
        try {
            getSupportActionBar().setTitle(title);
        } catch (Exception e) {
            Log.d(TAG, "title not set", e);
        }
    }

    @Override
    public void setBackButtonHandledByFragment(boolean backHandled) {
        this.backHandled = backHandled;
    }

}
