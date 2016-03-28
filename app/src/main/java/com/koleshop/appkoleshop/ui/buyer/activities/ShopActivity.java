package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.helpers.MyMenuItemStuffListener;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.services.BuyerIntentService;
import com.koleshop.appkoleshop.ui.seller.fragments.product.InventoryCategoryFragment;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ShopActivity extends AppCompatActivity {

    private static int VIEW_FLIPPER_CHILD_NO_INTERNET = 0x00;
    private static int VIEW_FLIPPER_CHILD_SOME_PROBLEM = 0x01;
    private static int VIEW_FLIPPER_CHILD_LOADING = 0x02;
    private static int VIEW_FLIPPER_CHILD_SHOP = 0x03;

    @Bind(R.id.collapsing_toolbar_activity_shop)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.toolbar_activity_shop)
    Toolbar toolbar;
    @Bind(R.id.app_bar_layout_activity_shop)
    AppBarLayout appBarLayout;
    @Bind(R.id.shop_menu_container)
    LinearLayout linearLayoutContainer;
    @Bind(R.id.civ_avatar_activity_shop)
    CircleImageView circleImageViewAvatarShop;
    @Bind(R.id.image_view_activity_shop)
    ImageView headerImageView;
    @Bind(R.id.tv_shop_activity_open_or_closed)
    TextView textViewOpenClosed;
    @Bind(R.id.tv_shop_activity_delivery_timings)
    TextView textViewDeliveryTimings;
    @Bind(R.id.favourite_shop_button)
    ImageButton favoriteShopButton;//FloatingActionButton favoriteShopButton;
    @Bind(R.id.vf_shop_activity)
    ViewFlipper viewFlipper;

    SellerSettings sellerSettings;
    InventoryCategoryFragment inventoryCategoryFragment;
    Context mContext;
    private static final String TAG = "ShopActivity";
    private Menu menu;
    private TextView noOfItemsViewer = null;
    Long sellerId;
    BroadcastReceiver mBroadcastReceiver;
    boolean shopIsFavorite = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        ButterKnife.bind(this);
        Bundle receivedBundle = getIntent().getExtras();
        mContext = this;
        if (receivedBundle != null) {
            Parcelable parcelableSellerSettings = receivedBundle.getParcelable("sellerSettings");
            if (parcelableSellerSettings != null) {
                sellerSettings = Parcels.unwrap(parcelableSellerSettings);
            }
            if (sellerSettings != null) {
                sellerId = sellerSettings.getUserId();
            } else {
                sellerId = receivedBundle.getLong("sellerId", 0l);
            }
        } else {
            finish();
        }

        if (sellerSettings != null) {
            loadShop();
        } else {
            fetchShopFromInternet();
            initializeBroadcastReceiver();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SHOP_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SHOP_FETCH_FAILED));
        updateHotCount();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shop_activity, menu);
        this.menu = menu;
        MenuItem item1 = menu.findItem(R.id.items_in_cart);
        final View showItemsInCart = MenuItemCompat.getActionView(item1);
        noOfItemsViewer = (TextView) showItemsInCart.findViewById(R.id.no_of_items_in_cart);


        new MyMenuItemStuffListener(showItemsInCart, "Cart") {
            @Override
            public void onClick(View v) {
                Intent cartActivityIntent = new Intent(mContext, CartActivity.class);
                startActivity(cartActivityIntent);
            }
        };

        updateHotCount();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.items_in_cart:
                Intent cartOpenActivityIntent = new Intent(this, CartActivity.class);
                startActivity(cartOpenActivityIntent);
                //open the cart activity
                break;
            case R.id.menu_item_search:
                Intent searchIntent = SearchActivity.newSingleSellerSearch(mContext, sellerSettings);
                startActivity(searchIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.button_retry_vinc, R.id.button_retry_vspo})
    public void retry() {
        fetchShopFromInternet();
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_SHOP_FETCH_SUCCESS:
                        try {
                            sellerSettings = Parcels.unwrap(intent.getParcelableExtra("sellerSettings"));
                            sellerId = sellerSettings.getUserId();
                            loadShop();
                        } catch (Exception e) {
                            Log.e(TAG, "couldn't unwrap seller settings", e);
                        }
                        break;
                    case Constants.ACTION_SHOP_FETCH_FAILED:
                        //some problem in fetching shop...show view flipper accordingly
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOME_PROBLEM);
                        break;
                }
            }
        };
    }

    private void loadShop() {
        setupCoordinatorLayout();
        setDeliveryTimingsAndOpenCloseStatusOnTextViews();
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SHOP);
        addFragmentToActivity();
        setupFavoriteButton();
    }

    private void fetchShopFromInternet() {
        //show progress view flipper child
        if (CommonUtils.isConnectedToInternet(mContext)) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            BuyerIntentService.getShop(mContext, sellerId);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_INTERNET);
        }
    }

    private void setupFavoriteButton() {
        String favShopIdString = PreferenceUtils.getPreferences(mContext, Constants.KEY_FAVORITE_SHOP_ID);
        Long favShopId = 0l;
        if (favShopIdString != null && !favShopIdString.isEmpty()) {
            favShopId = Long.parseLong(favShopIdString);
        }
        if (favShopId > 0 && sellerId == favShopId) {
            shopIsFavorite = true;
            favoriteShopButton.setImageResource(R.drawable.ic_star_golden_24dp);
        } else {
            shopIsFavorite = false;
            favoriteShopButton.setImageResource(R.drawable.ic_star_white_24dp);
        }
        favoriteShopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShopAsFavorite();
                showFavouriteButtonAnimation(v);
            }

        });
    }

    private void showFavouriteButtonAnimation(final View v) {
        //final Animation an = new RotateAnimation(0, 360, v.getWidth()/2, v.getHeight()/2);
        Animation an = AnimationUtils.loadAnimation(this, R.anim.favourite_button_animation);
        an.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(an);

        final Animation zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.favourite_button_animation_2);

        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                MediaPlayer mp;

                if (!shopIsFavorite) {
                    favoriteShopButton.setImageResource(R.drawable.ic_star_golden_24dp);
                    shopIsFavorite = true;
                    Snackbar.make(v, "Shop Favorited", Snackbar.LENGTH_SHORT).show();
                    mp = MediaPlayer.create(getApplicationContext(), R.raw.notification_favorite);
                    mp.start();
                } else {
                    favoriteShopButton.setImageResource(R.drawable.ic_star_white_24dp);
                    shopIsFavorite = false;
                    Snackbar.make(v, "Shop Unfavorited", Snackbar.LENGTH_SHORT).show();
                }

                zoomInAnimation.setFillAfter(true);
                v.startAnimation(zoomInAnimation);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public void updateHotCount() {
        if(menu!=null) {
            if (noOfItemsViewer == null) {
                MenuItem menuItemCart = this.menu.findItem(R.id.items_in_cart);
                final View showItemsInCart = MenuItemCompat.getActionView(menuItemCart);
                noOfItemsViewer = (TextView) showItemsInCart.findViewById(R.id.no_of_items_in_cart);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (CartUtils.getCartsTotalCount() == 0) {
                        noOfItemsViewer.setVisibility(View.INVISIBLE);
                    } else {
                        noOfItemsViewer.setVisibility(View.VISIBLE);
                        noOfItemsViewer.setText(CartUtils.getCartsTotalCount() + "");
                    }
                }
            });
        }
    }

    private void setupCoordinatorLayout() {

        //01. Setup toolbar title and elevation
        collapsingToolbarLayout.setTitle(sellerSettings.getAddress().getName());
        //Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        //CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }

        /*mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.d(TAG, "voffset = " + verticalOffset);
                if(collapsingToolbarLayout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbarLayout)) {
                    textViewActivityShop.animate().alpha(0).setDuration(600);
                } else {
                    textViewActivityShop.animate().alpha(1).setDuration(600);
                }
            }
        };

        appBarLayout.addOnOffsetChangedListener(mListener);*/


        //02. Set image avatar for shop
        if (!TextUtils.isEmpty(sellerSettings.getImageUrl())) {
            Picasso.with(mContext)
                    .load(sellerSettings.getImageUrl())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .centerCrop()
                    .fit()
                    .placeholder(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), 96, true))
                    .into(circleImageViewAvatarShop, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(sellerSettings.getImageUrl())
                                    .centerCrop()
                                    .fit()
                                    .placeholder(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), 96, true))
                                    .error(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), 96, true))
                                    .into(circleImageViewAvatarShop);
                        }
                    });
        } else {
            circleImageViewAvatarShop.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), 96, true));
        }

        //03. Set header image view
        if (!TextUtils.isEmpty(sellerSettings.getHeaderImageUrl())) {
            Picasso.with(mContext)
                    .load(sellerSettings.getHeaderImageUrl())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .centerCrop()
                    .fit()
                    .into(headerImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            changeStatusBarAndAppbarColor();
                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(sellerSettings.getHeaderImageUrl())
                                    .centerCrop()
                                    .fit()
                                    .into(headerImageView);
                        }
                    });


        }


    }

    private void setDeliveryTimingsAndOpenCloseStatusOnTextViews() {
        //String distanceText = "";
        String openOrClosed = "";

        String deliveryPickupInfo;
        if (sellerSettings.isHomeDelivery()) {
            if (KoleshopUtils.doesSellerDeliverToBuyerLocation(sellerSettings)) {
                //home delivery is available to this location
                deliveryPickupInfo = KoleshopUtils.getDeliveryTimeStringFromOpenAndCloseTime(sellerSettings.getDeliveryStartTime(), sellerSettings.getDeliveryEndTime());
                if (KoleshopUtils.willSellerDeliverNow(sellerSettings.getDeliveryEndTime())) {
                    //OPEN
                    openOrClosed = "Open";
                } else {
                    openOrClosed = "Open (delivery time over)";
                }
            } else {
                //seller don't delivery at this location - ORANGE ICON
                deliveryPickupInfo = "No delivery to your location";
                openOrClosed = "Open";
            }
        } else {
            //only pickup available - ORANGE ICON
            deliveryPickupInfo = "Pickup Only";
            openOrClosed = "Open (pickup only)";
        }

        if (!sellerSettings.isShopOpen()) {
            //seller is offline - GREY ICON
            openOrClosed = "Closed";
        }

        textViewOpenClosed.setText(openOrClosed);
        textViewDeliveryTimings.setText(deliveryPickupInfo);

    }

    private void changeStatusBarAndAppbarColor() {
        Bitmap bitmap = ((BitmapDrawable) headerImageView.getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //work with the palette here
                if (palette != null && palette.getVibrantSwatch() != null) {
                    int paletteColor = palette.getVibrantSwatch().getRgb();
                    int rgb = paletteColor;
                    int red = (rgb >> 16) & 0x000000FF;
                    int green = (rgb >> 8) & 0x000000FF;
                    int blue = (rgb) & 0x000000FF;
                    @ColorInt int darkPaletteColor = Color.argb(215, red, green, blue);
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.setStatusBarColor(darkPaletteColor);
                    }
                    collapsingToolbarLayout.setBackgroundColor(paletteColor);
                    collapsingToolbarLayout.setCollapsedTitleTextColor(palette.getVibrantSwatch().getTitleTextColor());
                }
            }
        });
    }

    private void addFragmentToActivity() {
        inventoryCategoryFragment = new InventoryCategoryFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("myInventory", true);
        bundle.putBoolean("customerView", true);
        bundle.putParcelable("sellerSettings", Parcels.wrap(sellerSettings));
        inventoryCategoryFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.shop_menu_container, inventoryCategoryFragment, "categoriesFragment")
                .commit();
    }

    private void setShopAsFavorite() {
        if (!shopIsFavorite) {
            PreferenceUtils.setPreferences(mContext, Constants.KEY_FAVORITE_SHOP_ID, sellerSettings.getUserId() + "");
            PreferenceUtils.setPreferences(mContext, Constants.KEY_FAVORITE_SHOP_NAME, sellerSettings.getAddress().getName() + "");
        } else {
            PreferenceUtils.setPreferences(mContext, Constants.KEY_FAVORITE_SHOP_ID, "");
        }
    }

}
