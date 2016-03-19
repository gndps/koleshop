package com.koleshop.appkoleshop.ui.seller.fragments.orders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.services.OrdersIntentService;
import com.koleshop.appkoleshop.ui.seller.adapters.IncomingOrderAdapter;
import com.koleshop.appkoleshop.ui.seller.adapters.OrderAdapter;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IncomingOrdersFragment extends Fragment {
    private static final String TAG = "IncomingOrdersFrag";
    @Bind(R.id.view_flipper_fragment_incoming_orders)
    ViewFlipper viewFlipper;
    @Bind(R.id.rv_fragment_incoming_orders)
    RecyclerView recyclerView;
    @Bind(R.id.tv_nothing_here_yet)
    TextView textViewNothingHereYet;
    @Bind(R.id.iv_nothing_here_yet)
    ImageView imageViewNothingHereYet;

    private final int VIEW_FLIPPER_CHILD_LOADING = 0x00;
    private final int VIEW_FLIPPER_CHILD_NO_INTERNET = 0x01;
    private final int VIEW_FLIPPER_CHILD_SOME_PROBLEM = 0x02;
    private final int VIEW_FLIPPER_CHILD_NO_ORDERS = 0x03;
    private final int VIEW_FLIPPER_CHILD_ORDERS_LIST = 0x04;

    private static final int ORDER_REQUEST_TYPE_INCOMING = 1;

    IncomingOrderAdapter adapter;
    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    List<Order> orders;

    public IncomingOrdersFragment() {
        // Required empty public constructor
    }

    public static IncomingOrdersFragment newInstance() {
        IncomingOrdersFragment fragment = new IncomingOrdersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incoming_orders, container, false);
        mContext = getContext();
        ButterKnife.bind(this, view);
        setupDefaultView();
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
                        if (bundle != null && orderRequestType == ORDER_REQUEST_TYPE_INCOMING) {
                            Parcelable ordersParcel = bundle.getParcelable("orders");
                            if (ordersParcel != null) {
                                orders = Parcels.unwrap(ordersParcel);
                                loadOrders();
                            }
                        }
                        break;
                    case Constants.ACTION_ORDERS_FETCH_FAILED:
                        if (orderRequestType == ORDER_REQUEST_TYPE_INCOMING) {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOME_PROBLEM);
                        }
                        break;
                    case Constants.ACTION_NO_ORDERS_FETCHED:
                        if (orderRequestType == ORDER_REQUEST_TYPE_INCOMING) {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_ORDERS);
                        }
                        break;
                    case Constants.ACTION_ORDER_UPDATE_SUCCESS:
                        Log.d(TAG, "order updated");
                        Long updatedOrderId = intent.getLongExtra("order_id", 0);
                        Log.d(TAG, "updated order id = " + updatedOrderId);
                        if (updatedOrderId != null && updatedOrderId > 0) {
                            int position = findPositionInOrdersList(updatedOrderId);
                            Log.d(TAG, "order position = " + position);
                            Log.d(TAG, "orders size = " + orders.size() + "...removing this order...");
                            orders.remove(position);
                            Log.d(TAG, "orders size = " + orders.size());
                            adapter.setOrdersList(orders);
                            adapter.orderRequestComplete(updatedOrderId);
                            adapter.notifyItemRemoved(position);
                            Intent refreshPendingOrders = new Intent(Constants.ACTION_REFRESH_PENDING_ORDERS);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(refreshPendingOrders);
                        }
                        break;
                    case Constants.ACTION_ORDER_UPDATE_FAILED:
                        Long notUpdatedOrderId = intent.getLongExtra("order_id", 0);
                        if (notUpdatedOrderId != null && notUpdatedOrderId > 0) {
                            int position = findPositionInOrdersList(notUpdatedOrderId);
                            adapter.orderRequestComplete(notUpdatedOrderId);
                            adapter.notifyItemChanged(position);
                            if(CommonUtils.isConnectedToInternet(mContext)) {
                                Snackbar.make(viewFlipper, "Some problem occurred", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(viewFlipper, "Please check connection", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            }
        }

        ;
    }

    private int findPositionInOrdersList(Long orderId) {
        Log.d(TAG, "finding position of " + orderId + " in orders list...");
        int pos = 0;
        for(Order order : orders) {
            Log.d(TAG, "order at pos " + pos + " = " + order.getId());
        }
        if (orders != null) {
            int position = 0;
            for (Order order : orders) {
                if(order.getId().equals(orderId)) {
                    return position;
                }
                position++;
            }
        }
        return 0;
    }

    private void setupDefaultView() {
        imageViewNothingHereYet.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_pinky_sleepy));
        textViewNothingHereYet.setText("No new orders");
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
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_UPDATE_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_UPDATE_FAILED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void loadOrders() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDERS_LIST);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new IncomingOrderAdapter(mContext);
        adapter.setOrdersList(orders);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        //todo dismiss all incoming order notifications
    }

    private void fetchOrdersFromInternet() {
        if (CommonUtils.isConnectedToInternet(mContext)) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            OrdersIntentService.getIncomingOrders(mContext);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_INTERNET);
        }
    }

}
