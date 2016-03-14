package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.ui.seller.fragments.product.InventoryCategoryFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.RealmUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ShopActivity extends AppCompatActivity {

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
    FloatingActionButton favouriteShopButton;

    SellerSettings sellerSettings;
    InventoryCategoryFragment inventoryCategoryFragment;
    private AppBarLayout.OnOffsetChangedListener mListener;
    Context mContext;
    private static final String TAG = "ShopActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        ButterKnife.bind(this);
        Bundle receivedBundle = getIntent().getExtras();
        mContext = this;
        if (receivedBundle != null) {
            sellerSettings = Parcels.unwrap(receivedBundle.getParcelable("sellerSettings"));
        } else {
            finish();
        }

        setupCoordinatorLayout();
        setDeliveryTimingsAndOpenCloseStatusOnTextViews();
        addFragmentToActivity();
        favouriteShopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipAnimation(v);
            }

        });

    }

    private void flipAnimation(final View v) {
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

                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.notification_sound);
                mp.start();
                zoomInAnimation.setFillAfter(true);
                v.startAnimation(zoomInAnimation);
                //v.setBackground(AndroidCompatUtil.getDrawable(getApplicationContext(),R.drawable.ic_star_golden));
                favouriteShopButton.setImageResource(R.drawable.ic_star_golden);
                Snackbar.make(v,"Shop Marked as Favourite",Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shop_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_cart:
                //open the cart activity
                return true;
            case R.id.menu_item_search:
                Intent searchIntent = SearchActivity.newSingleSellerSearch(mContext, sellerSettings);
                startActivity(searchIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
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
                    .centerCrop()
                    .fit()
                    .placeholder(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), 96, true))
                    .into(circleImageViewAvatarShop);
        } else {
            circleImageViewAvatarShop.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), 96, true));
        }

        //03. Set header image view
        if (!TextUtils.isEmpty(sellerSettings.getHeaderImageUrl())) {
            Picasso.with(mContext)
                    .load(sellerSettings.getHeaderImageUrl())
                    .centerCrop()
                    .fit()
                    .into(headerImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            changeStatusBarAndAppbarColor();
                        }

                        @Override
                        public void onError() {

                        }
                    });


        }


    }

    private void setDeliveryTimingsAndOpenCloseStatusOnTextViews() {
        //String distanceText = "";
        String openOrClosed = "";

        float[] results = new float[3];
        BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
        Double userLat = buyerAddress.getGpsLat();
        Double userLong = buyerAddress.getGpsLong();
        Location.distanceBetween(userLat, userLong, sellerSettings.getAddress().getGpsLat(), sellerSettings.getAddress().getGpsLong(), results);
        float userDistanceFromShopInMeters = results[0];
        if (results != null && results.length > 0) {
            //distanceText = CommonUtils.getReadableDistanceFromMetres(userDistanceFromShopInMeters);
        }
        String deliveryPickupInfo;
        if (sellerSettings.isHomeDelivery()) {
            if ((sellerSettings.getMaximumDeliveryDistance() + Constants.DELIVERY_DISTANCE_APPROXIMATION_ERROR) >= userDistanceFromShopInMeters) {
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

}
