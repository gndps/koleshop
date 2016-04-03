package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.services.BuyerIntentService;
import com.koleshop.appkoleshop.ui.seller.adapters.OrderAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MyOrdersFragment extends Fragment {

    private static final String TAG = "MyOrdersFragment";
    @Bind(R.id.rv_fragment_my_orders)
    RecyclerView recyclerView;
    @Bind(R.id.view_flipper_fragment_my_orders)
    ViewFlipper viewFlipper;
    @Bind(R.id.pb_load_more_fragment_my_orders)
    SmoothProgressBar progressBarLoadMore;

    private final int VIEW_FLIPPER_CHILD_LOADING = 0x00;
    private final int VIEW_FLIPPER_CHILD_NO_INTERNET = 0x01;
    private final int VIEW_FLIPPER_CHILD_SOME_PROBLEM = 0x02;
    private final int VIEW_FLIPPER_CHILD_NO_ORDERS = 0x03;
    private final int VIEW_FLIPPER_CHILD_ORDERS_LIST = 0x04;

    private final int ORDERS_LOAD_COUNT = 20;

    Context mContext;
    private OrderAdapter adapter;
    BroadcastReceiver mBroadcastReceiver;
    List<Order> orders;
    private LinearLayoutManager mLinearLayoutManager;
    boolean noMoreOrdersToLoad;
    private int visibleItemCount;
    private int totalItemCount;
    private int firstVisibleItem;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    private int current_page;
    private static final int ITEMS_PER_PAGE = 20;

    public MyOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_orders, container, false);
        ButterKnife.bind(this, view);
        initializeBroadcastReceiver();
        fetchOrdersFromInternet();
        if (PreferenceUtils.getPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING)) {
            PreferenceUtils.setPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING, false);
            KoleshopNotificationUtils.dismissAllNotifications(mContext);
        }
        return view;
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_ORDERS_FETCH_SUCCESS:
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            Parcelable ordersParcel = bundle.getParcelable("orders");
                            if (ordersParcel != null) {
                                Log.d(TAG, "my orders parcel is NOT null");
                                if (current_page == 0) {
                                    orders = Parcels.unwrap(ordersParcel);
                                    loadOrders();
                                } else {
                                    List<Order> moreOrders = Parcels.unwrap(ordersParcel);
                                    orders.addAll(moreOrders);
                                    loadMoreOrders();
                                }
                            } else {
                                Log.d(TAG, "my orders parcel is null");
                            }
                        }
                        break;
                    case Constants.ACTION_ORDERS_FETCH_FAILED:
                        if(!PreferenceUtils.isUserLoggedIn(mContext)) {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_ORDERS);
                        } else if (orders == null || orders.isEmpty()) {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOME_PROBLEM);
                        } else {
                            progressBarLoadMore.setVisibility(View.GONE);
                            if (CommonUtils.isConnectedToInternet(mContext)) {
                                Snackbar.make(viewFlipper, "Can't load more", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(viewFlipper, "Please check connection", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case Constants.ACTION_NO_ORDERS_FETCHED:
                        if (orders == null || orders.isEmpty()) {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_ORDERS);
                        } else {
                            if (CommonUtils.isConnectedToInternet(mContext)) {
                                Snackbar.make(viewFlipper, "No more orders to load", Snackbar.LENGTH_SHORT).show();
                                progressBarLoadMore.setVisibility(View.GONE);
                                noMoreOrdersToLoad = true;
                            } else {
                                Snackbar.make(viewFlipper, "Please check connection", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case Constants.ACTION_ORDER_UPDATE_NOTIFICATION:
                        Log.d(TAG, "order update notification in my orders fragment");
                        Long orderId = intent.getLongExtra("orderId", 0);
                        Log.d(TAG, "order id = " + orderId);
                        if (orderId > 0) {
                            int newOrderStatus = intent.getIntExtra("status", 0);
                            if (newOrderStatus == OrderStatus.ACCEPTED) {
                                orderAccepted(orderId);
                            } else if (newOrderStatus == OrderStatus.REJECTED) {
                                orderRejected(orderId);
                            } else {
                                fetchOrdersFromInternet();
                            }
                        }
                        abortBroadcast();
                        break;

                }
            }
        };
    }

    private void orderAccepted(Long acceptedOrderId) {
        int orderPosition = getOrderPositionWithId(acceptedOrderId);
        if (orderPosition > -1) {
            orders.get(orderPosition).setStatus(OrderStatus.ACCEPTED);
        }
        adapter.setOrdersList(orders, true);
        adapter.notifyItemChanged(orderPosition);
    }

    private void orderRejected(Long rejectedOrderId) {
        int orderPosition = getOrderPositionWithId(rejectedOrderId);
        if (orderPosition > -1) {
            orders.get(orderPosition).setStatus(OrderStatus.REJECTED);
        }
        adapter.setOrdersList(orders, true);
        adapter.notifyItemChanged(orderPosition);
    }

    private int getOrderPositionWithId(Long orderId) {
        int orderPosition = -1;
        if (orders != null && orders.size() > 0) {
            int position = 0;
            for (Order order : orders) {
                if (order.getId().equals(orderId)) {
                    orderPosition = position;
                    break;
                }
                position++;
            }
        }
        return orderPosition;
    }

    @OnClick(R.id.button_retry_vinc)
    public void reloadMyOrders() {
        fetchOrdersFromInternet();
    }

    @OnClick(R.id.button_retry_vspo)
    public void reloadMyOrders2() {
        fetchOrdersFromInternet();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDERS_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDERS_FETCH_FAILED));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NO_ORDERS_FETCHED));
        IntentFilter orderUpdateIntentFilter = new IntentFilter(Constants.ACTION_ORDER_UPDATE_NOTIFICATION);
        orderUpdateIntentFilter.setPriority(100);
        mContext.registerReceiver(mBroadcastReceiver, orderUpdateIntentFilter);
        if (PreferenceUtils.getPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING)) {
            fetchOrdersFromInternet();
            PreferenceUtils.setPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private void fetchOrdersFromInternet() {
        if (CommonUtils.isConnectedToInternet(mContext)) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            BuyerIntentService.getMyOrders(mContext, ORDERS_LOAD_COUNT, 0);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_INTERNET);
        }
    }

    private void fetchMoreOrdersFromInternet() {
        progressBarLoadMore.setVisibility(View.VISIBLE);
        if (CommonUtils.isConnectedToInternet(mContext)) {
            if (orders.size() >= ORDERS_LOAD_COUNT) {
                BuyerIntentService.getMyOrders(mContext, ORDERS_LOAD_COUNT, orders.size());
            }
        } else {
            Snackbar.make(viewFlipper, "Please check connection", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void loadOrders() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDERS_LIST);
        adapter = new OrderAdapter(mContext, true, true);
        adapter.setOrdersList(orders, true);
        mLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(orders.size() == ORDERS_LOAD_COUNT) {
            setupScrollListenerOnRv();
        }
    }

    private void loadMoreOrders() {
        progressBarLoadMore.setVisibility(View.GONE);
        adapter.setOrdersList(orders, true);
        adapter.notifyDataSetChanged();
    }

    private void setupScrollListenerOnRv() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    //02. Load more results
                    visibleItemCount = recyclerView.getChildCount();
                    totalItemCount = mLinearLayoutManager.getItemCount();
                    firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached

                        // Do something
                        current_page++;
                        loading = true;
                        fetchMoreOrdersFromInternet();

                    }

                }
            });
        } else {
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    visibleItemCount = recyclerView.getChildCount();
                    totalItemCount = mLinearLayoutManager.getItemCount();
                    firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached

                        // Do something
                        current_page++;
                        loading = true;
                        fetchMoreOrdersFromInternet();

                    }
                }
            });
        }
    }

}
