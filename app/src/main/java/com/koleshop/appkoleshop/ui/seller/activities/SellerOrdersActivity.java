package com.koleshop.appkoleshop.ui.seller.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.ui.seller.adapters.SellerOrderTabsAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SellerOrdersActivity extends AppCompatActivity {

    private static final String TAG = "SellerOrdersActivity";
    @BindString(R.string.navigation_drawer_orders)
    String titleOrders;
    @Bind(R.id.tab_seller_orders)
    TabLayout tabLayout;
    @Bind(R.id.view_pager_seller_orders)
    ViewPager viewPager;

    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    SellerOrderTabsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_seller_orders);
        initializeActivity();
        initializeBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_ORDER_UPDATE_NOTIFICATION);
        intentFilter.setPriority(100);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
        if(PreferenceUtils.getPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING)) {
            adapter.notifyDataSetChanged();
            PreferenceUtils.setPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING, false);
            KoleshopNotificationUtils.dismissAllNotifications(mContext);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeActivity() {
        ButterKnife.bind(this);
        setupToolbar();
        setupViewPagerAndTabLayout();
        if(PreferenceUtils.getPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING)) {
            PreferenceUtils.setPreferencesFlag(mContext, Constants.KEY_ORDERS_NEED_REFRESHING, false);
            KoleshopNotificationUtils.dismissAllNotifications(mContext);
        }
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION_ORDER_UPDATE_NOTIFICATION:
                        Long orderId = intent.getLongExtra("orderId", 0);
                        Log.d(TAG, "order update notification received");
                        Log.d(TAG, "order id = " + orderId);
                        if(orderId>0) {
                            int orderStatus = intent.getIntExtra("status", 0);
                            //order status accept and reject are handled inside incoming order fragment broadcast receiver
                            if(orderStatus>0 && orderStatus!= OrderStatus.ACCEPTED && orderStatus!=OrderStatus.REJECTED) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                        //abortBroadcast();
                        break;
                }
            }
        };
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(titleOrders);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        TextView toolbarTextView = CommonUtils.getActionBarTextView(toolbar);
        if (toolbarTextView != null) {
            toolbarTextView.setTypeface(typeface);
        }
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(titleOrders);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0.0f);
        }
    }

    private void setupViewPagerAndTabLayout() {
        //setupViewPager(viewPager, list);
        adapter = new SellerOrderTabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
    }
}
