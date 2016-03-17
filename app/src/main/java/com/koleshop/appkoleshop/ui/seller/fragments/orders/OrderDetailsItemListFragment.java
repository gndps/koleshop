package com.koleshop.appkoleshop.ui.seller.fragments.orders;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.ui.seller.adapters.OrderItemsListAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderDetailsItemListFragment extends Fragment {

    private static final String TAG = "SellerListFragment";
    @Bind(R.id.rv_fodil)
    RecyclerView recyclerView;
    @Bind(R.id.button_fodil_check_all)
    Button buttonCheckAll;
    @Bind(R.id.tv_bill_details_total)
    TextView textViewTotal;
    //@Bind(R.id.tv_bill_details_not_available)
    //TextView textViewNotAvailable;
    @Bind(R.id.tv_bill_details_delivery_charges)
    TextView textViewDeliveryCharges;
    @Bind(R.id.tv_bill_details_carry_bag_charges)
    TextView textViewCarryBagCharges;
    @Bind(R.id.tv_bill_details_amount_payable)
    TextView textViewAmountPayable;

    Context mContext;
    OrderItemsListAdapter adapter;
    private Order order;
    private BroadcastReceiver mBroadcastReceiver;
    boolean customerView;

    public OrderDetailsItemListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_details_item_list, container, false);
        mContext = getContext();
        ButterKnife.bind(this, view);
        recyclerViewSetup();
        initializeBroadcastReceiver();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_ITEM_COUNT_PLUS));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_ORDER_ITEM_COUNT_MINUS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_ORDER_ITEM_COUNT_PLUS:
                        int position = intent.getIntExtra("position", -1);
                        Log.d(TAG, "item plus broadcast received");
                        if (position > -1) {
                            Log.d(TAG, "position = " + position);
                            increaseAvailableCount(position);
                        }
                        break;
                    case Constants.ACTION_ORDER_ITEM_COUNT_MINUS:
                        int positionDecrement = intent.getIntExtra("position", -1);
                        if (positionDecrement > -1) {
                            decreaseAvailableCount(positionDecrement);
                        }
                        break;
                }
            }
        };
    }

    private void recyclerViewSetup() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new OrderItemsListAdapter(mContext);
        recyclerView.setAdapter(adapter);
    }

    public void setCustomerView(boolean customerView) {
        this.customerView = customerView;
    }

    public void setOrder(Order order) {
        this.order = order;
        adapter.setData(order, customerView);
        adapter.notifyDataSetChanged();
        refreshViews();
    }

    private void refreshViews() {
        if (order.getStatus() == OrderStatus.ACCEPTED && !customerView) {
            //show this button only when order is pending(accepted) in seller view
            buttonCheckAll.setVisibility(View.VISIBLE);
        } else {
            buttonCheckAll.setVisibility(View.GONE);
        }
        textViewCarryBagCharges.setText(CommonUtils.getPriceStringFromFloat(order.getCarryBagCharges(), true));
        textViewDeliveryCharges.setText(CommonUtils.getPriceStringFromFloat(order.getDeliveryCharges(), true));
        refreshAmountPayable();
    }

    private void refreshAmountPayable() {
        float amountPayable = 0f;
        boolean atLeastOneItemIsChecked = isAtLeastOneItemChecked();
        /*at least one item is always checked in complete order
        show amount payable using order count in these cases:incoming,accepted(buyer)
        show amount payable using available count in these cases:accepted(seller),complete
        */
        for (OrderItem orderItem : order.getOrderItems()) {
            if (atLeastOneItemIsChecked || (order.getStatus() == OrderStatus.ACCEPTED && !customerView)) {
                amountPayable += orderItem.getAvailableCount() * orderItem.getPricePerUnit();
            } else {
                amountPayable += orderItem.getOrderCount() * orderItem.getPricePerUnit();
            }
        }
        textViewTotal.setText(CommonUtils.getPriceStringFromFloat(amountPayable, true));
        amountPayable += order.getCarryBagCharges();
        amountPayable += order.getDeliveryCharges();
        textViewAmountPayable.setText(CommonUtils.getPriceStringFromFloat(amountPayable, true));
    }

    private void increaseAvailableCount(int position) {
        Log.d(TAG, "will increase variety count");
        OrderItem orderItem = order.getOrderItems().get(position);
        int availableCount = orderItem.getAvailableCount();
        Log.d(TAG, "old available count= " + availableCount);
        if (availableCount < orderItem.getOrderCount()) {
            orderItem.setAvailableCount(availableCount++);
        }
        Log.d(TAG, "new available count= " + availableCount);
        order.getOrderItems().get(position).setAvailableCount(availableCount);
        adapter.setData(order, customerView);
        refreshAmountPayable();
        adapter.notifyItemChanged(position);
    }

    private void decreaseAvailableCount(int position) {
        OrderItem orderItem = order.getOrderItems().get(position);
        int availableCount = orderItem.getAvailableCount();
        if (availableCount > 0) {
            orderItem.setAvailableCount(availableCount--);
        }
        order.getOrderItems().get(position).setAvailableCount(availableCount);
        adapter.setData(order, customerView);
        refreshAmountPayable();
        adapter.notifyItemChanged(position);
    }

    @OnClick(R.id.button_fodil_check_all)
    public void checkAll() {
        List<OrderItem> orderItemsList = order.getOrderItems();
        for (int i = 0; i < orderItemsList.size(); i++) {
            order.getOrderItems().get(i).setAvailableCount(order.getOrderItems().get(i).getOrderCount());
        }
        setOrder(order);
    }

    private boolean isAtLeastOneItemChecked() {
        boolean atLeastOneItemIsChecked = false;
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getAvailableCount() > 0) {
                atLeastOneItemIsChecked = true;
                break;
            }
        }
        return atLeastOneItemIsChecked;
    }


}
