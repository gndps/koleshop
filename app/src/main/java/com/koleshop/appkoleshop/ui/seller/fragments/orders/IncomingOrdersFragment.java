package com.koleshop.appkoleshop.ui.seller.fragments.orders;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.ui.seller.adapters.IncomingOrderAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IncomingOrdersFragment extends Fragment {
    @Bind(R.id.view_flipper_fragment_incoming_orders)
    ViewFlipper viewFlipper;
    @Bind(R.id.rv_fragment_incoming_orders)
    RecyclerView recyclerView;
    @Bind(R.id.button_retry_incoming_orders)
    Button buttonRetry;

    IncomingOrderAdapter adapter;
    Context mContext;

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
        initializeFragment();
        return view;
    }

    private void initializeFragment() {
        recyclerViewSetup();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewFlipper.setDisplayedChild(2);
            }
        }, 1000);
    }

    private void recyclerViewSetup() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new IncomingOrderAdapter(mContext);
        adapter.setOrdersList(getDummyOrderList());
        recyclerView.setAdapter(adapter);
    }

    private List<Order> getDummyOrderList() {
        List<Order> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setAsap(false);
            BuyerSettings buyerSettings = new BuyerSettings(null, null, "Gundeep Singh", null, null);
            order.setAddress(new Address(null, null, "Gundeep singh", "C-78 Sector 23\nNoida", Constants.ADDRESS_TYPE_BUYER, 8585945716l, 91, "gndp", 76.0d, 30.0d));
            order.setRequestedDeliveryTime(new Date());
            order.setBuyerSettings(buyerSettings);
            order.setHomeDelivery(true);
            order.setTotalAmount(220f);
            list.add(order);
        }
        return list;
     }
}
