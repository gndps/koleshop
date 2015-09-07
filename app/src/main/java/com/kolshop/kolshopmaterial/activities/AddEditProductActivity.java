package com.kolshop.kolshopmaterial.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.fragments.product.ProductBasicInfoShopkeeper;
import com.kolshop.kolshopmaterial.fragments.product.ProductVarietyDetailsFragment;
import com.kolshop.kolshopmaterial.model.Product;
import com.kolshop.kolshopmaterial.singletons.ProductPackageEditSingleton;

import java.util.ArrayList;
import java.util.List;

public class AddEditProductActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private ProductBasicInfoShopkeeper productBasicInfoShopkeeper;
    private ProductPackageEditSingleton currentProductPackage;
    private BroadcastReceiver mMessageReceiver;
    private int numberOfVarieties,deleteIndex;
    private LinearLayout varietyLinearLayoutContainer;
    private List<ProductVarietyDetailsFragment> productVarietyDetailsFragmentList;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        setupViews();
        numberOfVarieties = 0;
        initializeBroadcastReceivers();
        initializeOtherStuff();
    }

    private void initializeBroadcastReceivers()
    {
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.ACTION_ADD_VARIETY)) {
                    numberOfVarieties = intent.getIntExtra("numberOfVarieties", 1);
                    if(numberOfVarieties<99) {
                        addVariety();
                    }
                    else
                    {
                        Toast.makeText(context, "Maximum number of varieties created already", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(intent.getAction().equals(Constants.ACTION_DELETE_VARIETY)) {
                    deleteIndex = intent.getIntExtra("varietyIndex", 99);
                    if(deleteIndex<=99)
                    {
                        deleteVariety();
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.ACTION_ADD_VARIETY));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_edit_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setupViews()
    {
        toolbar = (Toolbar) findViewById(R.id.add_edit_product_app_bar);
        productBasicInfoShopkeeper = (ProductBasicInfoShopkeeper) getSupportFragmentManager().findFragmentById(R.id.activity_add_edit_product_fragment_basic_product_info);
        varietyLinearLayoutContainer = (LinearLayout) productBasicInfoShopkeeper.getView().findViewById(R.id.linear_layout_product_varieties_details_container);
        //productBasicInfoShopkeeper.setProduct(selected product or null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializeOtherStuff()
    {
        /*if(product!=null){
            numberOfVarieties = product.getVarieties.size();
        }
        else {*/

            productVarietyDetailsFragmentList = new ArrayList<ProductVarietyDetailsFragment>();
            numberOfVarieties = 1;
            ProductVarietyDetailsFragment productVarietyDetailsFragment = new ProductVarietyDetailsFragment();
            productVarietyDetailsFragment.setIndex(0);
            productVarietyDetailsFragment.setNumberOfVarieties(numberOfVarieties);
            productVarietyDetailsFragmentList.add(productVarietyDetailsFragment);
            getFragmentManager().beginTransaction().add(R.id.linear_layout_product_varieties_details_container, productVarietyDetailsFragment, ""+1).commit();

        //}
    }

    /*public void initializeObjects()
    {
        currentProductPackage = ProductPackageEditSingleton.getSharedInstance();
        currentProductPackage.setPropertyArray(new String[]{"notes", "needs to be tasted", "cool", "shit"});
        currentProductPackage.setNumberOfProperties(3);
    }*/

    private void addVariety()
    {
        //add a new variety fragment at the end
        ProductVarietyDetailsFragment productVarietyDetailsFragment = new ProductVarietyDetailsFragment();
        productVarietyDetailsFragment.setIndex(numberOfVarieties-1);
        productVarietyDetailsFragment.setNumberOfVarieties(numberOfVarieties);
        productVarietyDetailsFragmentList.add(productVarietyDetailsFragment);
        getFragmentManager().beginTransaction().add(R.id.linear_layout_product_varieties_details_container, productVarietyDetailsFragment, "" + numberOfVarieties).commit();
        Toast.makeText(this, "Empty Variety Added", Toast.LENGTH_SHORT).show();
    }

    private void deleteVariety()
    {

    }
}
