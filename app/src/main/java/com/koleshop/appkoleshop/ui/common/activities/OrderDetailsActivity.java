package com.koleshop.appkoleshop.ui.common.activities;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;

import com.klinker.android.sliding.SlidingActivity;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.SellerItemListFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.SellerOrderDetailsFragment;

import org.parceler.Parcels;

public class OrderDetailsActivity extends SlidingActivity {

    Context mContext;
    Order order;

    SellerItemListFragment sellerItemListFragment;
    SellerOrderDetailsFragment sellerOrderDetailsFragment;

    @Override
    public void init(Bundle savedInstanceState) {
        mContext = this;
        setTitle("Order Details");
        setPrimaryColors(
                ContextCompat.getColor(mContext, R.color.primary),
                ContextCompat.getColor(mContext, R.color.primary_dark)
        );
        setContent(R.layout.activity_order_details);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Parcelable parcelableOrder = getIntent().getExtras().getParcelable("order");
            this.order = Parcels.unwrap(parcelableOrder);
        }
        findFragments();
        loadOrderContent();
    }

    private void loadOrderContent() {
        if(sellerOrderDetailsFragment!=null) {
            sellerOrderDetailsFragment.setOrder(order);
        }
        if(sellerItemListFragment!=null) {
            sellerItemListFragment.setOrder(order);
        }
    }

    private void findFragments() {
        sellerOrderDetailsFragment = (SellerOrderDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_aod_seller_details);
        sellerItemListFragment = (SellerItemListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_aod_seller_items_list);
    }

    private void updateOrder(Order order) {
        this.order = order;
        loadOrderContent();
    }
}
