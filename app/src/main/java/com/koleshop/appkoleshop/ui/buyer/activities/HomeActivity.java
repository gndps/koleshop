package com.koleshop.appkoleshop.ui.buyer.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.ui.buyer.fragments.AddressesFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.MyOrdersFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.NearbyShopsFragment;
import com.koleshop.appkoleshop.ui.common.activities.ChangePictureActivity;
import com.koleshop.appkoleshop.ui.common.activities.FeedbackActivity;
import com.koleshop.appkoleshop.ui.common.activities.LegalActivity;
import com.koleshop.appkoleshop.ui.common.activities.VerifyPhoneNumberActivity;
import com.koleshop.appkoleshop.ui.common.fragments.NotImplementedFragment;
import com.koleshop.appkoleshop.ui.common.interfaces.FragmentHomeActivityListener;
import com.koleshop.appkoleshop.ui.seller.fragments.DummyHomeFragment;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindString;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements FragmentHomeActivityListener, NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog dialog;
    private Context mContext;
    private DrawerLayout drawerLayout;
    private final String TAG = "HomeActivity";
    private final String FRAGMENT_HOME_TAG = "home";
    private final String FRAGMENT_NEARBY_SHOPS_TAG = "nearby_shops";
    private final String FRAGMENT_NOT_IMPL = "not_impl";
    private final String FRAGMENT_ADDRESSES_TAG = "addresses";
    private final String FRAGMENT_MY_ORDERS_TAG = "my_orders";
    private String lastFragmentTag;
    private boolean lastFragmentShowed, isLastFragmentSupportType;
    NavigationView navigationView;
    private Toolbar toolbar;
    private boolean backHandled;
    private BuyerSettings buyerSettings;

    String imageViewHeaderUrl;
    String imageViewAvatarUrl;
    CircleImageView imageViewAvatar;
    ImageView imageViewHeader;
    TextView textViewCustomerName;

    @BindString(R.string.navigation_drawer_nearby_shops)
    String titleNearbyShops;
    @BindString(R.string.navigation_drawer_inventory)
    String titleWareHouse;
    @BindString(R.string.navigation_drawer_home)
    String titleHome;
    @BindString(R.string.navigation_drawer_addresses)
    String titleAddresses;
    @BindString(R.string.navigation_drawer_my_orders)
    String titleMyOrders;
    private boolean firstTime;
    private boolean openMyOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_buyer);

        if (getIntent().getBooleanExtra("CLOSE_APPLICATION", false)) {
            finish();
        } else {
            firstTime = getIntent().getBooleanExtra("firstTime", false);
            openMyOrders = getIntent().getBooleanExtra("openMyOrders", false);
            ButterKnife.bind(this);
            mContext = this;
            setupToolbar();
            boolean showDefaultFragment = true;
            if (savedInstanceState != null && savedInstanceState.getBoolean("createdOnce")) {
                showDefaultFragment = false;
            }
            setupDrawerLayout(showDefaultFragment);
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
                //revealSearchBar(menuView, true);
                startActivity(SearchActivity.newMultiSellerSearch(mContext));
                return true;

            case R.id.menu_item_cart:
                if (item != null) {
                    displayView(item);
                }

        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (backHandled || lastFragmentTag.equalsIgnoreCase(FRAGMENT_HOME_TAG)) {
            super.onBackPressed();
        } else {
            showHome();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLoginLogoutStates();
        refreshBuyerNameAndImage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("createdOnce", true);
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

    private void setupDrawerLayout(boolean showDefaultFragment) {
        //initialize layout elements
        drawerLayout = (DrawerLayout) findViewById(com.koleshop.appkoleshop.R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        imageViewAvatar = (CircleImageView) headerView.findViewById(R.id.avatar_drawer);
        imageViewHeader = (ImageView) headerView.findViewById(R.id.image_view_header_drawer);
        textViewCustomerName = (TextView) headerView.findViewById(R.id.tv_drawer_header_title);

        imageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PreferenceUtils.isUserLoggedIn(mContext)) {
                    Snackbar.make(imageViewAvatar, R.string.login_to_set_picture, Snackbar.LENGTH_SHORT).show();
                } else {
                    Intent intentChangePicture = new Intent(mContext, ChangePictureActivity.class);
                    intentChangePicture.putExtra("buyerMode", true);
                    startActivity(intentChangePicture);
                }
            }
        });
        /*imageViewHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!PreferenceUtils.isUserLoggedIn(mContext))
                {
                    Toast.makeText(mContext,"Log in first",Toast.LENGTH_SHORT).show();
                }
                else {
                Intent intentChangePicture = new Intent(mContext, ChangePictureActivity.class);
                intentChangePicture.putExtra("isHeaderImage", true);
                startActivity(intentChangePicture);
            }
            }
        });*/

        if (firstTime) {
            showNearbyShops();
        } else if (showDefaultFragment) {
            if (!openMyOrders) {
                showHome();
            } else {
                showMyOrders();
            }
        }


    }

    private void refreshBuyerNameAndImage() {

        //set home screen title to shop name
        buyerSettings = RealmUtils.getBuyerSettings();
        boolean updateTitle = false;
        String drawerTitle = "";

        if (buyerSettings != null) {
            if (!TextUtils.isEmpty(buyerSettings.getName())) {
                drawerTitle = buyerSettings.getName();
                updateTitle = true;
            }
        } else {
            BuyerSettings newBuyerSettings = new BuyerSettings();
            if (PreferenceUtils.getUserId(mContext) > 0) {
                //newBuyerSettings.setId(PreferenceUtils.getUserId(mContext));
                newBuyerSettings.setUserId(PreferenceUtils.getUserId(mContext));
                RealmUtils.saveBuyerSettings(newBuyerSettings);
            }
        }

        //set nav bar header title
        if(updateTitle) {
            textViewCustomerName.setText(drawerTitle);
        }

        //set avatar image view
        boolean refreshAvatar = false;
        if (imageViewAvatarUrl == null && buyerSettings != null) {
            imageViewAvatarUrl = buyerSettings.getImageUrl();
            refreshAvatar = true;
        } else if (buyerSettings != null && !TextUtils.isEmpty(buyerSettings.getImageUrl())
                && !imageViewAvatarUrl.equalsIgnoreCase(buyerSettings.getImageUrl())) {
            //there is a new image url and avatar should be refreshed
            imageViewAvatarUrl = buyerSettings.getImageUrl();
            refreshAvatar = true;
        }

        if (refreshAvatar) {
            if (!TextUtils.isEmpty(imageViewAvatarUrl)) {
                imageViewAvatarUrl = KoleshopUtils.getThumbnailImageUrl(imageViewAvatarUrl);
                Picasso.with(mContext)
                        .load(imageViewAvatarUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .fit().centerCrop()
                        .into(imageViewAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                //Try again online if cache failed
                                Picasso.with(mContext)
                                        .load(imageViewAvatarUrl)
                                        .error(R.drawable.ic_user_profile)
                                        .into(imageViewAvatar, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Log.v("Picasso","Could not fetch image");
                                            }
                                        });
                            }
                        });
            } else {
                imageViewAvatar.setImageResource(R.drawable.ic_user_profile);
            }
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

    private void showMyOrders() {
        MenuItem item = navigationView.getMenu().findItem(R.id.drawer_my_orders);
        if (item != null) {
            displayView(item);
        }
        setElevation(8);
        setTitle(titleMyOrders);
    }

    /*private void revealSearchBar(View view, final boolean reveal) {

        int ANIMATION_DURATION = 350;

        if (reveal) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;

            // get the center for the clipping circle
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int cx = location[0] + 12;
            int cy = location[1] + 12;

            // get the final radius for the clipping circle
            int dx = Math.max(cx, width - cx);
            int dy = Math.max(cy, height - cy);
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
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //get search suggestions from internet and history
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            searchBar.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
                @Override
                public void onSearchAction(CharSequence charSequence) {
                    Intent intent = new Intent(mContext, MultiSellerSearchActivity.class);
                    intent.putExtra("searchQuery", searchBar.getText());
                    intent.putExtra("multiSellerSearch", true);
                    intent.putExtra("customerView", true);
                    startActivity(intent);
                }
            });
        } else {
            searchBar.setOnSearchFocusChangedListener(null);
            searchBar.setIcon(null);
            searchBar.setOnIconClickListener(null);
        }
    }*/

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

            case R.id.drawer_my_orders:
                //open my orders
                title = titleMyOrders;
                MyOrdersFragment myOrdersFragment = new MyOrdersFragment();
                setItemChecked = true;
                closeDrawers = true;
                replaceFragment(myOrdersFragment, FRAGMENT_MY_ORDERS_TAG);
                break;

            case R.id.drawer_addresses:
                //Cart
                title = titleAddresses;
                AddressesFragment addressesFragment = AddressesFragment.newInstance(false, false);
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
                intentLogin.putExtra("finishOnVerify", true);
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
            case R.id.drawer_feedback:
                closeDrawers = true;
                setItemChecked = false;
                Intent feedback = new Intent(mContext, FeedbackActivity.class);
                startActivity(feedback);
                break;
            case R.id.drawer_legal:
                closeDrawers = true;
                setItemChecked = false;
                Intent intentLegalWebPage = new Intent(mContext, LegalActivity.class);
                startActivity(intentLegalWebPage);
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

    //this method is called onClick on edit button in xml
    public void editNameOfUser(View view) {
        if (PreferenceUtils.isUserLoggedIn(mContext)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set name");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            FrameLayout container = new FrameLayout(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            int pixelsFor8dp = CommonUtils.getPixelsFromDp(mContext, 8);
            params.setMargins(2 * pixelsFor8dp, 2 * pixelsFor8dp, 2 * pixelsFor8dp, 2 * pixelsFor8dp);
            container.addView(input);
            input.setLayoutParams(params);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(textViewCustomerName.getText());
            builder.setView(container);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newName = input.getText().toString();
                    if(!TextUtils.isEmpty(newName)) {
                        if(buyerSettings!=null) {
                            buyerSettings.setName(newName);
                            buyerSettings.setUserId(PreferenceUtils.getUserId(mContext));
                            RealmUtils.saveBuyerSettings(buyerSettings);
                        } else {
                            buyerSettings = new BuyerSettings();
                            buyerSettings.setUserId(PreferenceUtils.getUserId(mContext));
                            buyerSettings.setName(newName);
                            RealmUtils.saveBuyerSettings(buyerSettings);
                        }
                        refreshBuyerNameAndImage();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            Snackbar.make(navigationView, "Only logged in users can change this", Snackbar.LENGTH_SHORT).show();
        }
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
