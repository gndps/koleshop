package com.koleshop.appkoleshop.ui.common.activities;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.klinker.android.sliding.SlidingActivity;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.ui.buyer.activities.CartActivity;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.SellerItemListFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.SellerOrderDetailsFragment;
import com.koleshop.appkoleshop.util.CartUtils;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_details_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View menuItemView = findViewById(R.id.hidden_menu);
        PopupMenu popup = new PopupMenu(this,menuItemView);
        popup.getMenuInflater().inflate(R.menu.popup_menu_order_details_activity, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
             /*   switch (item.getItemId())
                {
                    case R.id.cancel:
                        order.setStatus(OrderStatus.CANCELLED);
                        loadOrderContent();
                        break;
                    case R.id.call:
                        order.setStatus(OrderStatus.INCOMING);
                        loadOrderContent();
                        break;
                    case R.id.not_delivered:
                        order.setStatus(OrderStatus.NOT_DELIVERED);
                        loadOrderContent();
                        break;
                }*/
                Toast.makeText(mContext,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        popup.show();
        return super.onOptionsItemSelected(item);
    }
}
