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
import android.util.Log;
import android.view.View;
import android.widget.ViewFlipper;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.adapters.InventoryCategoryViewPagerAdapter;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.SerializationUtil;
import com.kolshop.kolshopmaterial.model.genericjson.GenericJsonListInventoryCategory;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.kolshopmaterial.singletons.KoleshopSingleton;
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
    private static final String TAG = "InventoryPrductActity";

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
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(inventoryProductBroadcastReceiver, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS));
        lbm.registerReceiver(inventoryProductBroadcastReceiver, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED));
    }

    private void initializeBroadcastReceivers() {
        inventoryProductBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS)) {
                    loadCategories(null);
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
        List<InventoryCategory> list = getCachedSubcategories();
        if(list!=null && list.size()>0) {
            loadCategories(list);
        } else {
            requestCategoriesFromInternet();
        }
    }

    private List<InventoryCategory> getCachedSubcategories() {
        String cacheKey = Constants.CACHE_INVENTORY_SUBCATEGORIES + parentCategoryId;
        byte[] cachedSubcategoriesByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, Constants.TIME_TO_LIVE_INV_SUBCAT);
        if(cachedSubcategoriesByteArray!=null && cachedSubcategoriesByteArray.length>0) {
            try {
                GenericJsonListInventoryCategory genericJsonListInventoryCategory = SerializationUtil.getGenericJsonFromSerializable(cachedSubcategoriesByteArray, GenericJsonListInventoryCategory.class);
                List<InventoryCategory> subcategories = genericJsonListInventoryCategory.getList();
                if(subcategories!=null && subcategories.size()>0) {
                    return subcategories;
                } else {
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "some problem occurred in deserializing subcategories", e);
                return null;
            }
        } else {
            return null;
        }
    }

    private void requestCategoriesFromInternet() {
        viewFlipper.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(0);
        if (viewPager != null) {
            viewPager.setVisibility(View.GONE);
        }
        tabLayout.setVisibility(View.GONE);
        Intent serviceIntent = new Intent(mContext, CommonIntentService.class);
        serviceIntent.setAction(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES);
        serviceIntent.putExtra("categoryId", parentCategoryId);
        startService(serviceIntent);
    }

    private void loadCategories(List<InventoryCategory> list) {
        viewFlipper.setVisibility(View.GONE);
        viewPager = (ViewPager) findViewById(R.id.view_pager_inventory_product);
        viewPager.setVisibility(View.VISIBLE);
        setupViewPager(viewPager, list);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager, List<InventoryCategory> subcategories) {
        InventoryCategoryViewPagerAdapter adapter = new InventoryCategoryViewPagerAdapter(getSupportFragmentManager());
        List<InventoryCategory> categories;
        if(subcategories!=null && subcategories.size()>0){
            categories = subcategories;
        } else {
            categories = getCachedSubcategories();
        }
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
