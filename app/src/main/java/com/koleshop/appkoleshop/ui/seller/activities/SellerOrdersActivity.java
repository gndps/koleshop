package com.koleshop.appkoleshop.ui.seller.activities;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.ui.seller.adapters.SellerOrderTabsAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SellerOrdersActivity extends AppCompatActivity {

    @BindString(R.string.navigation_drawer_orders)
    String titleOrders;
    @Bind(R.id.tab_seller_orders)
    TabLayout tabLayout;
    @Bind(R.id.view_pager_seller_orders)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_orders);
        initializeActivity();
    }

    private void initializeActivity() {
        ButterKnife.bind(this);
        setupToolbar();
        setupViewPagerAndTabLayout();
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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0.0f);
        }
    }

    private void setupViewPagerAndTabLayout() {
        //setupViewPager(viewPager, list);
        SellerOrderTabsAdapter adapter = new SellerOrderTabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
