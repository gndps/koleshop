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
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.ui.seller.adapters.OrderAdapter;
import com.koleshop.appkoleshop.ui.seller.adapters.OrderItemsListAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SellerItemListFragment extends Fragment {

    @Bind(R.id.rv_fsil)
    RecyclerView recyclerView;
    @Bind(R.id.button_fsil_check_all)
    Button buttonCheckAll;
    @Bind(R.id.tv_bill_details_total)
    TextView textViewTotal;
    @Bind(R.id.tv_bill_details_not_available)
    TextView textViewNotAvailable;
    @Bind(R.id.tv_bill_details_delivery_charges)
    TextView textViewDeliveryCharges;
    @Bind(R.id.tv_bill_details_carry_bag_charges)
    TextView textViewCarryBagCharges;
    @Bind(R.id.tv_bill_details_amount_payable)
    TextView textViewAmountPayable;

    Context mContext;
    OrderItemsListAdapter adapter;

    public SellerItemListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_item_list, container, false);
        mContext = getContext();
        ButterKnife.bind(this, view);
        initializeFragment();
        return view;
    }

    public void initializeFragment() {
        recyclerViewSetup();
    }

    private void recyclerViewSetup() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new OrderItemsListAdapter(mContext);
        adapter.setItemsList();
        recyclerView.setAdapter(adapter);
    }

}
