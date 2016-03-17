package com.koleshop.appkoleshop.ui.seller.fragments.orders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.services.OrdersIntentService;
import com.koleshop.appkoleshop.ui.seller.adapters.OrderAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CompleteOrdersFragment extends Fragment {

    @Bind(R.id.view_flipper_fragment_complete_orders)
    ViewFlipper viewFlipper;
    @Bind(R.id.rv_fragment_complete_orders)
    RecyclerView recyclerView;
    @Bind(R.id.pb_load_more_fragment_complete_orders)
    ProgressBar progressBarLoadMore;
    @Bind(R.id.tv_nothing_here_yet)
    TextView textViewNothingHereYet;
    @Bind(R.id.iv_nothing_here_yet)
    ImageView imageViewNothingHereYet;

    private final int VIEW_FLIPPER_CHILD_LOADING = 0x00;
    private final int VIEW_FLIPPER_CHILD_NO_INTERNET = 0x01;
    private final int VIEW_FLIPPER_CHILD_SOME_PROBLEM = 0x02;
    private final int VIEW_FLIPPER_CHILD_NO_ORDERS = 0x03;
    private final int VIEW_FLIPPER_CHILD_ORDERS_LIST = 0x04;

    private static final int ORDER_REQUEST_TYPE_COMPLETE = 2;

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

    public CompleteOrdersFragment() {
        // Required empty public constructor
    }

    public static CompleteOrdersFragment newInstance() {
        CompleteOrdersFragment fragment = new CompleteOrdersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete_orders, container, false);
        mContext = getContext();
        ButterKnife.bind(this, view);
        initializeBroadcastReceiver();
        fetchOrdersFromInternet();
        return view;
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int orderRequestType = -1;
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    orderRequestType = intent.getIntExtra("order_request_type", -1);
                }
                switch (action) {
                    case Constants.ACTION_ORDERS_FETCH_SUCCESS:
                        if (bundle != null && orderRequestType == ORDER_REQUEST_TYPE_COMPLETE) {
                            Parcelable ordersParcel = bundle.getParcelable("orders");
                            if (ordersParcel != null) {
                                if (current_page == 0) {
                                    orders = Parcels.unwrap(ordersParcel);
                                    loadOrders();
                                } else {
                                    List<Order> moreOrders = Parcels.unwrap(ordersParcel);
                                    orders.addAll(moreOrders);
                                    loadMoreOrders();
                                }
                            }
                        }
                        break;
                    case Constants.ACTION_ORDERS_FETCH_FAILED:
                        if(orderRequestType == ORDER_REQUEST_TYPE_COMPLETE) {
                            if (orders == null || orders.isEmpty()) {
                                viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOME_PROBLEM);
                            } else {
                                progressBarLoadMore.setVisibility(View.GONE);
                                if (CommonUtils.isConnectedToInternet(mContext)) {
                                    Snackbar.make(viewFlipper, "Can't load more", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(viewFlipper, "Please check connection", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                        break;
                    case Constants.ACTION_NO_ORDERS_FETCHED:
                        if(orderRequestType == ORDER_REQUEST_TYPE_COMPLETE) {
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
                        }
                        break;
                }
            }
        };
    }

    @OnClick(R.id.button_retry_vinc)
    public void reloadOrders() {
        fetchOrdersFromInternet();
    }

    @OnClick(R.id.button_retry_vspo)
    public void reloadOrders2() {
        fetchOrdersFromInternet();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDERS_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDERS_FETCH_FAILED));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NO_ORDERS_FETCHED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void fetchOrdersFromInternet() {
        if (CommonUtils.isConnectedToInternet(mContext)) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            OrdersIntentService.getCompleteOrders(mContext, ITEMS_PER_PAGE, 0);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_INTERNET);
        }
    }

    private void fetchMoreOrdersFromInternet() {
        progressBarLoadMore.setVisibility(View.VISIBLE);
        if (CommonUtils.isConnectedToInternet(mContext)) {
            if(orders.size()>=ITEMS_PER_PAGE) {
                OrdersIntentService.getCompleteOrders(mContext, ITEMS_PER_PAGE, orders.size());
            }
        } else {
            Snackbar.make(viewFlipper, "Please check connection", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void loadOrders() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDERS_LIST);
        adapter = new OrderAdapter(mContext, true, false);
        adapter.setOrdersList(orders, false);
        mLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setupScrollListenerOnRv();
    }

    private void loadMoreOrders() {
        progressBarLoadMore.setVisibility(View.GONE);
        adapter.setOrdersList(orders, false);
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
