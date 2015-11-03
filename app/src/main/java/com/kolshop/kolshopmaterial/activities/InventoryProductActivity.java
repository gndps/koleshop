package com.kolshop.kolshopmaterial.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ViewFlipper;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.adapters.InventoryCategoryViewPagerAdapter;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.kolshopmaterial.singletons.KolShopSingleton;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.List;

public class InventoryProductActivity extends AppCompatActivity {

    Long parentCategoryId;
    String categoryTitle;
    Context mContext;
    ViewFlipper viewFlipper;
    private BroadcastReceiver inventoryProductBroadcastReceiver;
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_product);
        mContext = this;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            parentCategoryId = extras.getLong("categoryId");
            categoryTitle = extras.getString("categoryTitle");
        }
        initializeBroadcastReceivers();
        setupActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //todo subscribe only if categories don't exist in cache
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(inventoryProductBroadcastReceiver, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS));
        lbm.registerReceiver(inventoryProductBroadcastReceiver, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED));
    }

    private void initializeBroadcastReceivers() {
        inventoryProductBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS)) {
                    loadCategories();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED)) {
                    failedLoadingCategories();
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(inventoryProductBroadcastReceiver);
    }

    private void setupActivity() {
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper_inventory_product_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar_inventory_products);
        toolbar.setTitle(categoryTitle);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout = (TabLayout) findViewById(R.id.tab_inventory_product);
        tabLayout.setVisibility(View.GONE);
        fetchCategories();
    }

    private void fetchCategories() {
        //todo check for cache here
        viewFlipper.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(0);
        if(viewPager!=null) {
            viewPager.setVisibility(View.GONE);
        }
        tabLayout.setVisibility(View.GONE);
        Intent serviceIntent = new Intent(mContext, CommonIntentService.class);
        serviceIntent.setAction(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES);
        serviceIntent.putExtra("categoryId", parentCategoryId);
        startService(serviceIntent);
    }

    private void loadCategories() {
        viewFlipper.setVisibility(View.GONE);
        viewPager = (ViewPager) findViewById(R.id.view_pager_inventory_product);
        viewPager.setVisibility(View.VISIBLE);
        setupViewPager(viewPager);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        InventoryCategoryViewPagerAdapter adapter = new InventoryCategoryViewPagerAdapter(getSupportFragmentManager());
        List<InventoryCategory> categories = KolShopSingleton.getSharedInstance().getInventorySubcategoriesForCategoryId(parentCategoryId);
        adapter.setInventoryCategories(categories);
        viewPager.setAdapter(adapter);
    }

    private void failedLoadingCategories() {
        viewFlipper.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(1);
        if(viewPager!=null) {
            viewPager.setVisibility(View.GONE);
        }
    }

    public void retry(View v) {
        fetchCategories();
    }
}
