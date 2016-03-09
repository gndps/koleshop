package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ScrollView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.services.BuyerIntentService;
import com.koleshop.appkoleshop.ui.buyer.fragments.AddressesFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.ChooseDeliveryTimeFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.ChooseDeliveryOptionFragment;
import com.koleshop.appkoleshop.ui.common.activities.VerifyPhoneNumberActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PlaceOrderActivity extends AppCompatActivity implements ChooseDeliveryTimeFragment.BackPressedListener {

    private final static String EXTRA_CART = "com.koleshop.appkoleshop.ui.buyer.activities.EXTRA_CART";
    private final static int NONE_IS_SELECTED = 0;
    private final static String IMAGE_SELECTION_KEY = "selectedButton";
    private final static String DELIVERY_OPTION_FRAGMENT_TAG = "delivery_fragment";
    private static final String DELIVERY_TIME_FRAGMENT_TAG = "delivery_time_fragment_tag";
    private static final String ADDRESS_FRAGMENT_TAG = "address_frag_tag";
    private static final int NONE_SELECTED_DELIVERY_TIME_FREGMENT = 5;
    private static final String DELIVERY_OPTIONS_SELECTIONS_KEY = "11";
    private static final String DELIVERY_TIME_SELECTION_KEY = "12";
    private static final String TIME_KEY = "key";
    private static final String FRAGMENTS_ALREADY_ADDED = "FragmentsAlreadyAdded";

    @BindString(R.string.title_place_order)
    String titlePlaceOrder;
    @Bind(R.id.app_bar_place_order)
    Toolbar toolbar;
    @Bind(R.id.scroll_view_activity_place_order)
    ScrollView scrollView;

    boolean backHandledByFragment = false;
    private ChooseDeliveryOptionFragment chooseDeliveryOptionFragment; //frag 1
    private ChooseDeliveryTimeFragment chooseDeliveryTimeFragment; //frag 2
    private AddressesFragment deliveryAddressesFragment; // frag 3
    private Cart cart;
    private Context mContext;
    private int deliveryOrPickup;
    private boolean asapDelivery;
    private int selectedHour;
    private int selectedMinute;

    public static void startActivityNow(Context context, Cart cart) {
        Intent intent = new Intent(context, PlaceOrderActivity.class);
        intent.putExtra(EXTRA_CART, Parcels.wrap(cart));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        mContext = this;
        ButterKnife.bind(this);
        setupToolbar();

        //place order fragment initialize
        chooseDeliveryOptionFragment = new ChooseDeliveryOptionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(IMAGE_SELECTION_KEY, NONE_IS_SELECTED);
        chooseDeliveryOptionFragment.setArguments(bundle);

        //choose delivery fragment initialize
        chooseDeliveryTimeFragment = new ChooseDeliveryTimeFragment();
        Bundle bundleDeliveryTimeFragment = new Bundle();
        bundleDeliveryTimeFragment.putInt(IMAGE_SELECTION_KEY, NONE_SELECTED_DELIVERY_TIME_FREGMENT);
        chooseDeliveryTimeFragment.setArguments(bundleDeliveryTimeFragment);

        //initialize address fragment
        deliveryAddressesFragment = AddressesFragment.newInstance(true, true);


        //RESTORE DATA
        if (savedInstanceState != null) {

            //restore cart
            Parcelable parcelableCart = savedInstanceState.getParcelable(EXTRA_CART);
            if (parcelableCart != null) {
                cart = Parcels.unwrap(parcelableCart);
            }

            //restore fragment state
            int deliveryOptionsSelection = savedInstanceState.getInt(DELIVERY_OPTIONS_SELECTIONS_KEY);
            bundle.putInt(DELIVERY_OPTIONS_SELECTIONS_KEY, deliveryOptionsSelection);

            int selectedButtoninDeliveryFragment = savedInstanceState.getInt(DELIVERY_TIME_SELECTION_KEY);
            bundleDeliveryTimeFragment.putInt(DELIVERY_TIME_SELECTION_KEY, selectedButtoninDeliveryFragment);
            String time = savedInstanceState.getString(TIME_KEY);
            bundleDeliveryTimeFragment.putString(TIME_KEY, time);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (savedInstanceState.getBoolean(FRAGMENTS_ALREADY_ADDED)) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(DELIVERY_OPTION_FRAGMENT_TAG);
                Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(DELIVERY_TIME_FRAGMENT_TAG);
                Fragment fragment3 = getSupportFragmentManager().findFragmentByTag(ADDRESS_FRAGMENT_TAG);
                boolean commitFt = false;
                if(fragment!=null) {
                    fragmentTransaction.remove(fragment);
                    commitFt = true;
                }
                if(fragment2!=null) {
                    fragmentTransaction.remove(fragment2);
                    commitFt = true;
                }
                if(fragment3!=null) {
                    fragmentTransaction.remove(fragment3);
                    commitFt = true;
                }

                /*if(commitFt) {
                    fragmentTransaction.commit();
                }*/

                //fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.view_activity_place_order_frag_1, chooseDeliveryOptionFragment, DELIVERY_OPTION_FRAGMENT_TAG)
                .add(R.id.view_activity_place_order_frag_2, chooseDeliveryTimeFragment, DELIVERY_TIME_FRAGMENT_TAG)
                .add(R.id.view_activity_place_order_frag_3, deliveryAddressesFragment, ADDRESS_FRAGMENT_TAG)
                .commit();
                //getSupportFragmentManager().executePendingTransactions();
            }

        } else {
            if (getIntent() != null && getIntent().getExtras() != null) {
                Parcelable parcelableCart = getIntent().getExtras().getParcelable(EXTRA_CART);
                cart = Parcels.unwrap(parcelableCart);
                if (cart != null) {
                    toolbar.setSubtitle(cart.getSellerSettings().getAddress().getName());
                }
            }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.view_activity_place_order_frag_1, chooseDeliveryOptionFragment, DELIVERY_OPTION_FRAGMENT_TAG)
            .add(R.id.view_activity_place_order_frag_2, chooseDeliveryTimeFragment, DELIVERY_TIME_FRAGMENT_TAG)
            .add(R.id.view_activity_place_order_frag_3, deliveryAddressesFragment, ADDRESS_FRAGMENT_TAG)
            .commit();
            //getSupportFragmentManager().executePendingTransactions();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ChooseDeliveryOptionFragment chooseDeliveryOptionFragment = (ChooseDeliveryOptionFragment) getSupportFragmentManager().findFragmentByTag(DELIVERY_OPTION_FRAGMENT_TAG);

        //1st
        int flag = chooseDeliveryOptionFragment.getSelectedButton();
        outState.putInt(DELIVERY_OPTIONS_SELECTIONS_KEY, flag);
        outState.putBoolean(FRAGMENTS_ALREADY_ADDED, true);

        //2nd
        int flagDeliveryTimeFragment = chooseDeliveryTimeFragment.getFlag();
        outState.putInt(DELIVERY_TIME_SELECTION_KEY, flagDeliveryTimeFragment);
        outState.putBoolean("Delivery_Fragment_already_added", true);
        String time = chooseDeliveryTimeFragment.getTime();
        outState.putString(TIME_KEY, time);
        outState.putParcelable(EXTRA_CART, Parcels.wrap(cart));

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void setBackPressedHandledByFragment(boolean isBackPressedHandled) {
        backHandledByFragment = isBackPressedHandled;
    }

    @Override
    public void onBackPressed() {
        if (backHandledByFragment) {
            backHandledByFragment = false;
            chooseDeliveryTimeFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                super.onBackPressed();
                return true;

        }
        return false;
    }

    @OnClick(R.id.button_place_order)
    public void placeOrder() {
        //check if user is signed up
        Long userId = PreferenceUtils.getUserId(mContext);
        if (userId <= 0) {
            //start login activity
            Intent intent = new Intent(mContext, VerifyPhoneNumberActivity.class);
            intent.putExtra("finishOnVerify", true);
            startActivity(intent);
        } else {
            //start processing order and show order screen when order is placed
            if(validateOrder()) {
                //OrderIntentService.placeOrder();
                Snackbar.make(toolbar, "wow...this order looks crispy", Snackbar.LENGTH_LONG).show();
                BuyerIntentService.createOrder();
            }
        }
    }

    private boolean validateOrder() {
        if(validateDeliveryOption() && validateDeliveryTime() && validateDeliveryAddress()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean validateDeliveryOption() {
        int deliveryOption = chooseDeliveryOptionFragment.getSelectedButton();
        switch (deliveryOption) {
            case ChooseDeliveryOptionFragment.FLAG_PICK_UP_BUTTON_SELECTED:
                deliveryOrPickup = Constants.ORDER_OPTION_PICKUP;
                return true;
            case ChooseDeliveryOptionFragment.FLAG_DELIVERY_BUTTON_SELECTED:
                deliveryOrPickup = Constants.ORDER_OPTION_DELIVERY;
                return true;
            default:
                scrollView.smoothScrollTo(0, 0);
                Snackbar.make(toolbar, "Please choose from pickup or home delivery", Snackbar.LENGTH_LONG).show();
                return false;
        }
    }

    private boolean validateDeliveryTime() {
        int selectedDeliveryTimeFlag = chooseDeliveryTimeFragment.getFlag();
        switch (selectedDeliveryTimeFlag) {
            case ChooseDeliveryTimeFragment.ASAP_BUTTON_CLICKED:
                asapDelivery = true;
                return true;
            case ChooseDeliveryTimeFragment.CHOOSE_DELIVERY_BUTTON_CLICKED:
                asapDelivery = false;
                selectedHour = chooseDeliveryTimeFragment.getDeliveryTimeHours();
                selectedMinute = chooseDeliveryTimeFragment.getDeliveryTimeMinutes();
                return true;
            default:
                scrollView.smoothScrollTo(0, scrollView.getBottom() / 3);
                Snackbar.make(toolbar, "Please choose a delivery time", Snackbar.LENGTH_LONG).show();
                return false;
        }
    }

    private boolean validateDeliveryAddress() {
        BuyerAddress selectedAddress = deliveryAddressesFragment.getSelectedAddress();
        Double gpsLong = selectedAddress.getGpsLong();
        Double gpsLat = selectedAddress.getGpsLat();
        if(gpsLat!=null && gpsLong!=null && gpsLat>0 && gpsLong>0) {
            if(TextUtils.isEmpty(selectedAddress.getName()) || TextUtils.isEmpty(selectedAddress.getAddress())
                    || (selectedAddress.getPhoneNumber()==null || selectedAddress.getPhoneNumber()<10000)) {
                //address is not valid
                Snackbar.make(toolbar, "Please set a delivery address", Snackbar.LENGTH_LONG).show();
                scrollView.smoothScrollTo(0, scrollView.getBottom());
                return false;
            } else {
                return true;
            }
        } else {
            Snackbar.make(toolbar, "Please select a valid delivery address", Snackbar.LENGTH_LONG).show();
            scrollView.scrollTo(0, scrollView.getBottom());
            return false;
        }
    }

    private void setupToolbar() {
        toolbar.setTitle(titlePlaceOrder);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);

        }
    }

}
