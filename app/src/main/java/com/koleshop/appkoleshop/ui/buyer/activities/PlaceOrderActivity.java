package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.services.BuyerIntentService;
import com.koleshop.appkoleshop.ui.buyer.fragments.AddressesFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.ChooseDeliveryTimeFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.ChooseDeliveryOptionFragment;
import com.koleshop.appkoleshop.ui.common.activities.VerifyPhoneNumberActivity;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PlaceOrderActivity extends AppCompatActivity implements ChooseDeliveryTimeFragment.BackPressedListener {

    private final static String EXTRA_CART = "com.koleshop.appkoleshop.ui.buyer.activities.EXTRA_CART";
    private final static String DELIVERY_OPTION_FRAGMENT_TAG = "delivery_fragment";
    private static final String DELIVERY_TIME_FRAGMENT_TAG = "delivery_time_fragment_tag";
    private static final String ADDRESS_FRAGMENT_TAG = "address_frag_tag";
    private static final String TAG = "PlaceOrderActivity";

    @BindString(R.string.title_place_order)
    String titlePlaceOrder;
    @Bind(R.id.app_bar_place_order)
    Toolbar toolbar;
    @Bind(R.id.scroll_view_activity_place_order)
    ScrollView scrollView;
    @Bind(R.id.pb_activity_place_order)
    ProgressBar progressBar;
    @Bind(R.id.button_place_order)
    Button buttonPlaceOrder;

    boolean backHandledByFragment = false;
    private ChooseDeliveryOptionFragment chooseDeliveryOptionFragment; //frag 1
    private ChooseDeliveryTimeFragment chooseDeliveryTimeFragment; //frag 2
    private AddressesFragment deliveryAddressesFragment; // frag 3
    private Cart cart;
    private Context mContext;
    private boolean homeDelivery;
    private boolean asapDelivery;
    private int hoursLater;
    private int minutesLater;
    private BroadcastReceiver mBroadcastReceiver;

    private int selectedButton;
    private int selectedTimeOptions;
    private String selectedTimeString;
    private Long selectedDeliveryTime;

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

        if (getIntent() != null && getIntent().getExtras() != null && savedInstanceState == null) {
            Parcelable parcelableCart = getIntent().getExtras().getParcelable(EXTRA_CART);
            cart = Parcels.unwrap(parcelableCart);
            if (cart != null) {
                toolbar.setSubtitle(cart.getSellerSettings().getAddress().getName());
            }
        } else if (savedInstanceState != null) {
            //restore cart
            Parcelable parcelableCart = savedInstanceState.getParcelable(EXTRA_CART);
            if (parcelableCart != null) {
                cart = Parcels.unwrap(parcelableCart);
            }
            if (cart != null) {
                toolbar.setSubtitle(cart.getSellerSettings().getAddress().getName());
            }
            selectedButton = savedInstanceState.getInt(ChooseDeliveryOptionFragment.KEY_SELECTED_BUTTON);
            selectedTimeOptions = savedInstanceState.getInt(ChooseDeliveryTimeFragment.DELIVERY_TIME_SELECTION);
            selectedTimeString = savedInstanceState.getString(ChooseDeliveryTimeFragment.DELIVERY_TIME_STRING);
            selectedDeliveryTime = savedInstanceState.getLong(ChooseDeliveryTimeFragment.DELIVERY_DATE);
            removeFragments();
        }

        initializeFragments();

        initializeBroadcastReceiver();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //save selected delivery option
        if(chooseDeliveryOptionFragment!=null) {
            int selectedButton = chooseDeliveryOptionFragment.getSelectedButton();
            outState.putInt(ChooseDeliveryOptionFragment.KEY_SELECTED_BUTTON, selectedButton);
        }

        //save selected time
        if(chooseDeliveryTimeFragment!=null) {
            int selectedTimeOptions = chooseDeliveryTimeFragment.getSelectedTimeOptions();
            outState.putInt(ChooseDeliveryTimeFragment.DELIVERY_TIME_SELECTION, selectedTimeOptions);
            String time = chooseDeliveryTimeFragment.getTime();
            outState.putString(ChooseDeliveryTimeFragment.DELIVERY_TIME_STRING, time);
            Date deliveryTime = chooseDeliveryTimeFragment.getDeliveryTime();
            if(deliveryTime!=null) {
                outState.putLong(ChooseDeliveryTimeFragment.DELIVERY_DATE, deliveryTime.getTime());
            }
        }

        //save cart
        if(cart!=null) {
            outState.putParcelable(EXTRA_CART, Parcels.wrap(cart));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_CREATED_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_CREATED_FAILED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
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

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_ORDER_CREATED_SUCCESS:
                        orderPlacedSuccessfully();
                        break;
                    case Constants.ACTION_ORDER_CREATED_FAILED:
                        setProcessing(false);
                        Snackbar.make(buttonPlaceOrder, "Couldn't create order", Snackbar.LENGTH_SHORT).show();
                        //show snackbar to say that some problem in creating order
                        break;
                }
            }
        };
    }

    private void initializeFragments() {
        //place order fragment initialize
        chooseDeliveryOptionFragment = new ChooseDeliveryOptionFragment();
        Bundle bundle = new Bundle();
        if(!cart.getSellerSettings().isHomeDelivery() || !KoleshopUtils.doesSellerDeliverToBuyerLocation(cart.getSellerSettings())) {
            bundle.putInt(ChooseDeliveryOptionFragment.KEY_SELECTED_BUTTON, ChooseDeliveryOptionFragment.PICK_UP_BUTTON);
            bundle.putBoolean(ChooseDeliveryOptionFragment.HOME_DELIVERY_BUTTON_DISABLED, true);
        } else {
            bundle.putInt(ChooseDeliveryOptionFragment.KEY_SELECTED_BUTTON, selectedButton);
        }
        chooseDeliveryOptionFragment.setArguments(bundle);

        //choose delivery fragment initialize
        chooseDeliveryTimeFragment = new ChooseDeliveryTimeFragment();
        Bundle bundleDeliveryTimeFragment = new Bundle();
        bundleDeliveryTimeFragment.putInt(ChooseDeliveryTimeFragment.DELIVERY_TIME_SELECTION, selectedTimeOptions);
        bundleDeliveryTimeFragment.putString(ChooseDeliveryTimeFragment.DELIVERY_TIME_STRING, selectedTimeString);
        if(selectedDeliveryTime!=null && selectedDeliveryTime>0l) {
            bundleDeliveryTimeFragment.putLong(ChooseDeliveryTimeFragment.DELIVERY_DATE, selectedDeliveryTime);
        }
        chooseDeliveryTimeFragment.setArguments(bundleDeliveryTimeFragment);

        //initialize address fragment
        deliveryAddressesFragment = AddressesFragment.newInstance(true, true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.view_activity_place_order_frag_1, chooseDeliveryOptionFragment, DELIVERY_OPTION_FRAGMENT_TAG)
                .add(R.id.view_activity_place_order_frag_2, chooseDeliveryTimeFragment, DELIVERY_TIME_FRAGMENT_TAG)
                .add(R.id.view_activity_place_order_frag_3, deliveryAddressesFragment, ADDRESS_FRAGMENT_TAG)
                .commit();
    }

    private void removeFragments() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DELIVERY_OPTION_FRAGMENT_TAG);
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(DELIVERY_TIME_FRAGMENT_TAG);
        Fragment fragment3 = getSupportFragmentManager().findFragmentByTag(ADDRESS_FRAGMENT_TAG);
        boolean commit = false;
        if (fragment != null) {
            commit = true;
            fragmentTransaction.remove(fragment);
        }
        if (fragment2 != null) {
            commit = true;
            fragmentTransaction.remove(fragment2);
        }
        if (fragment3 != null) {
            commit = true;
            fragmentTransaction.remove(fragment3);
        }
        if(commit) {
            fragmentTransaction.commit();
        }
        /*fragmentTransaction.add(R.id.view_activity_place_order_frag_1, chooseDeliveryOptionFragment, DELIVERY_OPTION_FRAGMENT_TAG)
                .add(R.id.view_activity_place_order_frag_2, chooseDeliveryTimeFragment, DELIVERY_TIME_FRAGMENT_TAG)
                .add(R.id.view_activity_place_order_frag_3, deliveryAddressesFragment, ADDRESS_FRAGMENT_TAG)*/

    }

    @OnClick(R.id.button_place_order)
    public void placeOrder() {
        if (validateOrder()) {
            //check if user is signed up
            Long userId = PreferenceUtils.getUserId(mContext);
            if (userId <= 0) {
                //start login activity
                Intent intent = new Intent(mContext, VerifyPhoneNumberActivity.class);
                intent.putExtra("finishOnVerify", true);
                startActivity(intent);
            } else {
                //start processing order and show order screen when order is placed
                BuyerAddress buyerAddress = deliveryAddressesFragment.getSelectedAddress();
                if (buyerAddress != null) {
                    //01. save buyer address to db
                    Address address = new Address();
                    address.setNickname(buyerAddress.getNickname());
                    address.setName(buyerAddress.getName());
                    address.setUserId(userId);
                    address.setAddress(buyerAddress.getAddress());
                    address.setAddressType(Constants.ADDRESS_TYPE_BUYER);
                    address.setPhoneNumber(buyerAddress.getPhoneNumber());
                    address.setCountryCode(Constants.DEFAULT_COUNTRY_CODE);
                    address.setGpsLong(buyerAddress.getGpsLong());
                    address.setGpsLat(buyerAddress.getGpsLat());

                    //02. create buyer settings
                    BuyerSettings buyerSettings = RealmUtils.getBuyerSettings();
                    if (buyerSettings == null) {
                        buyerSettings = new BuyerSettings();
                        buyerSettings.setUserId(userId);
                        buyerSettings.setName(address.getName());
                        //RealmUtils.saveBuyerSettings(buyerSettings);
                    } else if (TextUtils.isEmpty(buyerSettings.getName())) {
                        buyerSettings.setUserId(userId);
                        buyerSettings.setName(address.getName());
                        //RealmUtils.saveBuyerSettings(buyerSettings);
                    }

                    //03. create seller settings
                    SellerSettings sellerSettings = cart.getSellerSettings();


                    //04. create order
                    Order order = new Order();
                    order.setSellerSettings(sellerSettings);
                    order.setBuyerSettings(buyerSettings);
                    order.setAddress(address);
                    order.setStatus(OrderStatus.INCOMING);
                    List<OrderItem> orderItems = createOrderItems();
                    order.setOrderItems(orderItems);
                    float carryBagCharges = cart.getSellerSettings().getCarryBagCharges();
                    float totalCharges = KoleshopUtils.getItemsTotalPrice(cart.getProductVarietyCountList());
                    float deliveryCharges = 0f;
                    if (totalCharges < sellerSettings.getMinimumOrder()) {
                        deliveryCharges = sellerSettings.getDeliveryCharges();
                    }
                    float amountPayable = totalCharges + carryBagCharges + deliveryCharges;
                    order.setDeliveryCharges(deliveryCharges);
                    order.setCarryBagCharges(carryBagCharges);
                    order.setTotalAmount(totalCharges);
                    order.setAmountPayable(amountPayable);
                    order.setHomeDelivery(homeDelivery);
                    order.setAsap(asapDelivery);
                    order.setTotalAmount(deliveryCharges + carryBagCharges + totalCharges);
                    setProcessing(true);
                    BuyerIntentService.createNewOrder(mContext, order, hoursLater, minutesLater);
                }
            }
        }
    }

    private void setProcessing(boolean processing) {
        progressBar.setVisibility(processing ? View.VISIBLE : View.GONE);
        buttonPlaceOrder.setClickable(processing ? false : true);
    }

    private List<OrderItem> createOrderItems() {
        List<ProductVarietyCount> productVarietyCounts = cart.getProductVarietyCountList();
        List<OrderItem> orderItems = new ArrayList<>();
        for (ProductVarietyCount pvc : productVarietyCounts) {
            String title = pvc.getTitle();
            String brand = "";
            String name = "";
            try {
                brand = title.split("-")[0].trim();
                name = title.split("-")[1].trim();
            } catch (Exception e) {
                Log.e(TAG, "couldn't split title to name and brand", e);
            }
            OrderItem item = new OrderItem(pvc.getProductVariety().getId(), name, brand,
                    pvc.getProductVariety().getQuantity(), pvc.getProductVariety().getPrice(),
                    pvc.getProductVariety().getImageUrl(), pvc.getCartCount(), 0);
            orderItems.add(item);
        }
        return orderItems;
    }

    private boolean validateOrder() {
        if (validateDeliveryOption() && validateDeliveryTime() && validateDeliveryAddress()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean validateDeliveryOption() {
        int deliveryOption = chooseDeliveryOptionFragment.getSelectedButton();
        switch (deliveryOption) {
            case ChooseDeliveryOptionFragment.PICK_UP_BUTTON:
                homeDelivery = false;
                return true;
            case ChooseDeliveryOptionFragment.DELIVERY_BUTTON:
                homeDelivery = true;
                return true;
            default:
                scrollView.smoothScrollTo(0, 0);
                Snackbar.make(toolbar, "Please choose from pickup or home delivery", Snackbar.LENGTH_LONG).show();
                return false;
        }
    }

    private boolean validateDeliveryTime() {
        int selectedDeliveryTimeFlag = chooseDeliveryTimeFragment.getSelectedTimeOptions();
        switch (selectedDeliveryTimeFlag) {
            case ChooseDeliveryTimeFragment.ASAP_BUTTON_CLICKED:
                asapDelivery = true;
                return true;
            case ChooseDeliveryTimeFragment.CHOOSE_DELIVERY_BUTTON_CLICKED:
                asapDelivery = false;
                Date deliveryTime = chooseDeliveryTimeFragment.getDeliveryTime();
                hoursLater = CommonUtils.getHoursDifference(deliveryTime);
                minutesLater = CommonUtils.getMinutesDifference(deliveryTime);
                if (hoursLater < 0 || minutesLater < 0) {
                    scrollView.smoothScrollTo(0, scrollView.getBottom() / 3);
                    Snackbar.make(toolbar, "Please choose a delivery time in future", Snackbar.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            default:
                scrollView.smoothScrollTo(0, scrollView.getBottom() / 3);
                Snackbar.make(toolbar, "Please choose a delivery time", Snackbar.LENGTH_SHORT).show();
                return false;
        }
    }

    private boolean validateDeliveryAddress() {
        BuyerAddress selectedAddress = deliveryAddressesFragment.getSelectedAddress();
        if (selectedAddress == null) {
            return false;
        }
        Double gpsLong = selectedAddress.getGpsLong();
        Double gpsLat = selectedAddress.getGpsLat();
        if (gpsLat != null && gpsLong != null && gpsLat > 0 && gpsLong > 0) {
            if (TextUtils.isEmpty(selectedAddress.getName()) || TextUtils.isEmpty(selectedAddress.getAddress())
                    || (selectedAddress.getPhoneNumber() == null || selectedAddress.getPhoneNumber() < 10000)) {
                //address is not valid
                Snackbar.make(toolbar, "Please set a delivery address", Snackbar.LENGTH_SHORT).show();
                scrollView.smoothScrollTo(0, scrollView.getBottom());
                return false;
            } else {
                return true;
            }
        } else {
            Snackbar.make(toolbar, "Please select a valid delivery address", Snackbar.LENGTH_SHORT).show();
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

    private void orderPlacedSuccessfully() {
        setProcessing(false);
        Log.d(TAG, "clearing cart after successful order");
        CartUtils.clearCart(cart);
        //clear back stack and go to my orders display
        Intent intentMyOrders = new Intent(mContext, HomeActivity.class);
        intentMyOrders.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intentMyOrders.putExtra("openMyOrders", true);
        startActivity(intentMyOrders);
        finish();
    }

}
