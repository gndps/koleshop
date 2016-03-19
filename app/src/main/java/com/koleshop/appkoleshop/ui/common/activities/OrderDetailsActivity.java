package com.koleshop.appkoleshop.ui.common.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ViewFlipper;

import com.klinker.android.sliding.SlidingActivity;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.services.OrdersIntentService;
import com.koleshop.appkoleshop.ui.seller.fragments.DeliveryTimeRemainingDialogFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.OrderDetailsItemListFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.OrderDetailsFragment;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OrderDetailsActivity extends SlidingActivity implements DeliveryTimeRemainingDialogFragment.DeliveryTimeDialogFragmentListener {

    private static int VIEW_FLIPPER_CHILD_FRAGMENTS = 0X00;
    private static int VIEW_FLIPPER_CHILD_PROGRESS_BAR = 0X01;
    private static int VIEW_FLIPPER_CHILD_SOMETHING_WRONG = 0X02;

    @Bind(R.id.vf_activity_order_details)
    ViewFlipper viewFlipper;

    Context mContext;
    Order order;
    boolean haveChangesLocally;
    boolean customerView;
    Long orderId;

    OrderDetailsItemListFragment orderDetailsItemListFragment;
    OrderDetailsFragment orderDetailsFragment;
    BroadcastReceiver mBroadcastReceiver;

    @Override
    public void init(Bundle savedInstanceState) {
        mContext = this;
        setTitle("Order Details");
        setPrimaryColors(
                ContextCompat.getColor(mContext, R.color.primary),
                ContextCompat.getColor(mContext, R.color.primary_dark)
        );
        setContent(R.layout.activity_order_details);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Parcelable parcelableOrder = getIntent().getExtras().getParcelable("order");
            this.order = Parcels.unwrap(parcelableOrder);
            if (order == null) {
                orderId = getIntent().getLongExtra("orderId", 0l);
            } else {
                orderId = order.getId();
            }
            customerView = getIntent().getBooleanExtra("customerView", false);
        } else if (savedInstanceState != null) {
            try {
                order = Parcels.unwrap(savedInstanceState.getParcelable("order"));
            } catch (Exception e) {

            }
            haveChangesLocally = savedInstanceState.getBoolean("haveChangesLocally");
            orderId = savedInstanceState.getLong("orderId");
            customerView = savedInstanceState.getBoolean("customerView", false);
        }
        findFragments();
        if (order != null) {
            loadOrderContent();
        } else {
            refreshOrderFromInternet();
        }
        initializeBroadcastReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (order != null) {
            outState.putParcelable("order", Parcels.wrap(order));
        }
        outState.putBoolean("haveChangesLocally", haveChangesLocally);
        outState.putLong("orderId", orderId);
        outState.putBoolean("customerView", customerView);
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_ORDER_UPDATE_SUCCESS:
                        if (haveChangesLocally) {
                            haveChangesLocally = false;
                            updateOrderInUi(order);
                        } else {
                            refreshOrderFromInternet();
                        }
                        break;
                    case Constants.ACTION_ORDER_UPDATE_FAILED:
                        Snackbar.make(viewFlipper, "Please try again", Snackbar.LENGTH_SHORT).show();
                        break;
                    case Constants.ACTION_SINGLE_ORDER_FETCH_SUCCESS:
                        if (intent != null && intent.getExtras() != null) {
                            Parcelable parcelableOrder = intent.getExtras().getParcelable("order");
                            Order order = Parcels.unwrap(parcelableOrder);
                            updateOrderInUi(order);
                        }
                        break;
                    case Constants.ACTION_SINGLE_ORDER_FETCH_FAILED:
                        //something went wrong
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOMETHING_WRONG);
                        break;
                    case Constants.ACTION_ORDER_UPDATE_NOTIFICATION:
                        int refreshedOrderId = intent.getIntExtra("orderId", 0);
                        if (refreshedOrderId > 0 && refreshedOrderId == order.getId()) {
                            order = null;
                            refreshOrderFromInternet();
                            PreferenceUtils.setPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING, true);
                        }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_UPDATE_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_UPDATE_FAILED));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SINGLE_ORDER_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SINGLE_ORDER_FETCH_FAILED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void loadOrderContent() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_FRAGMENTS);
        if (orderDetailsFragment != null) {
            orderDetailsFragment.setCustomerView(customerView);
            orderDetailsFragment.setOrder(order);
        }
        if (orderDetailsItemListFragment != null) {
            orderDetailsItemListFragment.setCustomerView(customerView);
            orderDetailsItemListFragment.setOrder(order);
        }
    }

    private void findFragments() {
        orderDetailsFragment = (OrderDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_aod_seller_details);
        orderDetailsItemListFragment = (OrderDetailsItemListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_aod_seller_items_list);
    }

    public void updateOrderInUi(Order order) {
        this.order = order;
        loadOrderContent();
    }

    @Override
    public void deliveryTimeRemainingSelected(int minutes) {
        order.setMinutesToDelivery(minutes);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderDetailsFragment.showProgressBar(true);
        updateOrderInCloud(order);
    }

    public void updateOrderInCloud(Order order) {
        this.order = order;
        if(order.getStatus() == OrderStatus.READY_FOR_PICKUP || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            float deliveryCharges = order.getDeliveryCharges();
            float carryBagCharges = order.getCarryBagCharges();
            float availableItemsCharges = 0;
            for(OrderItem orderItem : order.getOrderItems()) {
                availableItemsCharges += orderItem.getAvailableCount() * orderItem.getPricePerUnit();
            }
            order.setAmountPayable(availableItemsCharges + deliveryCharges + carryBagCharges);
        }
        haveChangesLocally = true;
        OrdersIntentService.updateOrder(mContext, order);
    }

    public void refreshOrderFromInternet() {
        if (order != null) {
            Long orderId = order.getId();
            if (orderId > 0) {
                viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_PROGRESS_BAR);
                OrdersIntentService.getOrderForId(mContext, orderId);
            }
        }
    }

}
