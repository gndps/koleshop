package com.kolshop.kolshopmaterial.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.fragments.product.ProductBasicInfoShopkeeper;
import com.kolshop.kolshopmaterial.fragments.product.ProductVarietyDetailsFragment;
import com.kolshop.kolshopmaterial.model.android.Product;
import com.kolshop.kolshopmaterial.model.android.ProductVariety;

import org.parceler.Parcels;

import java.util.ArrayList;

import io.realm.RealmList;

public class AddEditProductActivity extends ActionBarActivity {

    private static String TAG = "Kolshop_AddEditActivity";

    private Toolbar toolbar;
    private ProductBasicInfoShopkeeper productBasicInfoShopkeeper;
    private BroadcastReceiver mMessageReceiver;
    private int numberOfVarieties;
    private String deleteTag;
    private LinearLayout varietyLinearLayoutContainer;
    private Product product;

    //POJO Fields
    private String id;
    private String name;
    private String brand;
    private int brandId;
    private String description;
    private int categoryId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        setupViews();
        numberOfVarieties = 0;
        initializeBroadcastReceivers();
        initializeUI();
    }

    private void initializeBroadcastReceivers() {
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_ADD_VARIETY)) {
                    numberOfVarieties = intent.getIntExtra("numberOfVarieties", 1);
                    if (numberOfVarieties < 99) {
                        addNewVariety();
                    } else {
                        Toast.makeText(context, "Maximum number of varieties created already", Toast.LENGTH_SHORT).show();
                    }
                } else if (intent.getAction().equals(Constants.ACTION_DELETE_VARIETY)) {
                    deleteTag = intent.getStringExtra("fragmentTag");
                    if (deleteTag!=null && !deleteTag.isEmpty()) {
                        deleteVariety(deleteTag);
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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

    public void setupViews() {
        toolbar = (Toolbar) findViewById(R.id.add_edit_product_app_bar);
        productBasicInfoShopkeeper = (ProductBasicInfoShopkeeper) getSupportFragmentManager().findFragmentById(R.id.activity_add_edit_product_fragment_basic_product_info);
        varietyLinearLayoutContainer = (LinearLayout) productBasicInfoShopkeeper.getView().findViewById(R.id.linear_layout_product_varieties_details_container);
        //productBasicInfoShopkeeper.setProduct(selected product or null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializeUI() {

        //extract data from bundle
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        Parcelable productParcel = bundle.getParcelable("product");
        Product product = Parcels.unwrap(productParcel);
        if (product != null) {

            //prepare data
            this.product = product;
            id = product.getId();
            name = product.getName();
            brand = product.getBrand();
            brandId = product.getBrandId();
            description = product.getDescription();
            categoryId = product.getProductCategoryId();
            //todo userId will be used for saving product
            RealmList<ProductVariety> productVarieties = product.getListProductVariety();
            Bundle basicInfoBundle = new Bundle();
            basicInfoBundle.putString("name", name);
            basicInfoBundle.putString("brand", brand);
            basicInfoBundle.putInt("brandId", brandId);
            basicInfoBundle.putString("description", description);
            basicInfoBundle.putInt("categoryId", categoryId);
            basicInfoBundle.putInt("numberOfVarieties", productVarieties.size());
            productBasicInfoShopkeeper.setArguments(basicInfoBundle);

            //initialize User Interface
            for (int index = 0; index < productVarieties.size(); index++) {
                addProductVarietyToUserInterface(productVarieties.get(index), index);
            }
        } else {

            //prepare empty data
            this.product = new Product();
            product.setId("random" + CommonUtils.randomString(8));
            product.setName("");
            product.setBrand("");
            product.setBrandId(0);
            product.setDescription("");
            product.setProductCategoryId(0);
            //todo product.setUserId();

            //initialize User Interface
            addNewVariety();
        }
    }

    private void addProductVarietyToUserInterface(ProductVariety productVariety, int index) {
        ProductVarietyDetailsFragment productVarietyDetailsFragment = new ProductVarietyDetailsFragment();
        Bundle args = new Bundle();
        args.putString("id", productVariety.getId());
        args.putString("name", productVariety.getName());
        args.putInt("stock", productVariety.getLimitedStock());
        args.putInt("sortOrder", index);
        args.putString("imageUrl", productVariety.getImageUrl());
        args.putLong("dateAdded", productVariety.getDateAdded().getTime());
        args.putLong("dateModified", productVariety.getDateModified().getTime());
        Parcelable parcelableListVarietyAttributes = Parcels.wrap(productVariety.getListVarietyAttributes());
        Parcelable parcelableListAttributeValues = Parcels.wrap(productVariety.getListAttributeValues());
        args.putParcelable("listAttributeValue", parcelableListAttributeValues);
        args.putParcelable("listVarietyAttribute", parcelableListVarietyAttributes);
        productVarietyDetailsFragment.setArguments(args);
        String fragmentTag = CommonUtils.randomString(8);
        getSupportFragmentManager().beginTransaction().add(R.id.linear_layout_product_varieties_details_container, productVarietyDetailsFragment, fragmentTag).commit();
    }

    private void addNewVariety() {
        //add a new variety fragment at the end....properties will be initialized from within ProductVariety
        String fragmentTag = CommonUtils.randomString(8);
        ProductVarietyDetailsFragment productVarietyDetailsFragment = new ProductVarietyDetailsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.linear_layout_product_varieties_details_container, productVarietyDetailsFragment, fragmentTag).commit();
        Log.d(TAG, "added a new variety with tag = " + fragmentTag);
    }

    private void deleteVariety(String framentTag) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(framentTag);
        getSupportFragmentManager().beginTransaction().remove(frag).commit();
        Log.d(TAG, "deleted variety with tag = " + framentTag);
    }

    private Product getProduct()
    {
        Product product = new Product();
        //todo handle this shit
        return null;
        //product.setUserId();
        //product.setProductCategoryId(productBasicInfoShopkeeper.getProductCategoryId());
        //product.setDescription(productBasicInfoShopkeeper.);
    }
}
