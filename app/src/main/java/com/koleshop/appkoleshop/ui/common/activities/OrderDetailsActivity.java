package com.koleshop.appkoleshop.ui.common.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.klinker.android.sliding.SlidingActivity;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.services.OrdersIntentService;
import com.koleshop.appkoleshop.ui.seller.fragments.DeliveryTimeRemainingDialogFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.OrderDetailsFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.OrderDetailsItemListFragment;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OrderDetailsActivity extends SlidingActivity implements DeliveryTimeRemainingDialogFragment.DeliveryTimeDialogFragmentListener {

    private static final String TAG = "OrderDetailsActivity";
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
    private Menu mOptionsMenu;

    @Override
    public void init(Bundle savedInstanceState) {
        mContext = this;
        setTitle("Order Details");
        setPrimaryColors(
                ContextCompat.getColor(mContext, R.color.cool_red),
                ContextCompat.getColor(mContext, R.color.cool_red_dark)
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
        KoleshopNotificationUtils.removeThisOrderFromNotificationOrders(orderId);
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
                        Log.d(TAG, "order update notification received");
                        int refreshedOrderId = intent.getIntExtra("orderId", 0);
                        Log.d(TAG, "order id = " + refreshedOrderId + ", current order id = " + orderId);
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
        IntentFilter orderUpdateIntentFilter = new IntentFilter(Constants.ACTION_ORDER_UPDATE_NOTIFICATION);
        orderUpdateIntentFilter.setPriority(1);
        mContext.registerReceiver(mBroadcastReceiver, orderUpdateIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        mContext.unregisterReceiver(mBroadcastReceiver);
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
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_order_details_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_cancel:
                order.setStatus(OrderStatus.CANCELLED);
                updateOrderInCloud(order);
                return true;
            case R.id.menu_item_call:
                if (customerView) {
                    //call seller
                    if (order != null && order.getSellerSettings() != null) {
                        Long phoneNumber = order.getSellerSettings().getAddress().getPhoneNumber();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Phone number is not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //call buyer
                    if (order != null && order.getBuyerSettings() != null) {
                        Long phoneNumber = order.getAddress().getPhoneNumber();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    }
                }
                return true;
            case R.id.menu_item_not_delivered:
                order.setStatus(OrderStatus.NOT_DELIVERED);
                updateOrderInCloud(order);
                return true;
            case R.id.menu_item_not_picked_up:
                order.setStatus(OrderStatus.NOT_DELIVERED);
                updateOrderInCloud(order);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deliveryTimeRemainingSelected(int minutes) {
        if (minutes > 0) {
            order.setMinutesToDelivery(minutes);
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            orderDetailsFragment.showProgressBar(true);
            updateOrderInCloud(order);
        } else {
            Snackbar.make(viewFlipper, "Please select estimated delivery time", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void updateOrderInCloud(Order order) {
        this.order = order;
        if (order.getStatus() == OrderStatus.READY_FOR_PICKUP || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            float deliveryCharges = order.getDeliveryCharges();
            float carryBagCharges = order.getCarryBagCharges();
            float availableItemsCharges = 0;
            for (OrderItem orderItem : order.getOrderItems()) {
                availableItemsCharges += orderItem.getAvailableCount() * orderItem.getPricePerUnit();
            }
            order.setAmountPayable(availableItemsCharges + deliveryCharges + carryBagCharges);
        }
        haveChangesLocally = true;
        OrdersIntentService.updateOrder(mContext, order);
    }

    public void refreshOrderFromInternet() {
        if (orderId > 0) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_PROGRESS_BAR);
            OrdersIntentService.getOrderForId(mContext, orderId);
        }
    }

    private void showNotPickedUpOrNotDeliveredMenu(boolean show) {
        MenuItem item = mOptionsMenu.findItem(R.id.menu_item_not_delivered);
        if (item != null) {
            item.setVisible(show && order.isHomeDelivery());
        }
        MenuItem item2 = mOptionsMenu.findItem(R.id.menu_item_not_picked_up);
        if (item2 != null) {
            item2.setVisible(show && !order.isHomeDelivery());
        }
    }

    private void showCancelButton(boolean show) {
        MenuItem item = mOptionsMenu.findItem(R.id.menu_item_cancel);
        if (item != null) {
            item.setVisible(show);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (order == null) {
            return true;
        }
        int orderStatus = order.getStatus();
        switch (orderStatus) {
            case OrderStatus.INCOMING:
                //show only cancel button in customer view
                if (customerView) {
                    showNotPickedUpOrNotDeliveredMenu(false);
                    showCancelButton(true);
                } else {
                    showNotPickedUpOrNotDeliveredMenu(false);
                    showCancelButton(false);
                }
                break;
            case OrderStatus.ACCEPTED:
                //show cancel button for customer and seller both
                showNotPickedUpOrNotDeliveredMenu(false);
                showCancelButton(true);
                break;
            case OrderStatus.REJECTED:
            case OrderStatus.MISSED:
            case OrderStatus.CANCELLED:
                //don't show cancel and not picked up/delivered
                showNotPickedUpOrNotDeliveredMenu(false);
                showCancelButton(false);
                break;
            case OrderStatus.OUT_FOR_DELIVERY:
            case OrderStatus.DELIVERED:
                showNotPickedUpOrNotDeliveredMenu(true);
                showCancelButton(false);
                break;
            case OrderStatus.READY_FOR_PICKUP:
                if (customerView) {
                    showNotPickedUpOrNotDeliveredMenu(false);
                    showCancelButton(false);
                } else {
                    showNotPickedUpOrNotDeliveredMenu(true);
                    showCancelButton(false);
                }
                break;
            case OrderStatus.NOT_DELIVERED:
                showNotPickedUpOrNotDeliveredMenu(false);
                showCancelButton(false);
                break;
        }
        invalidateOptionsMenu();
        return true;
    }
}
