package com.koleshop.appkoleshop.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.koleshop.appkoleshop.adapters.ProductEditAdapter;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;

import io.realm.Realm;

public class ProductEditActivity extends AppCompatActivity {

    private Context mContext;
    private InventoryProduct product;
    private RecyclerView recyclerView;
    private ProductEditAdapter productEditAdapter;
    private long categoryId;

    private Realm realm;
    private ArrayList<ProductCategory> parentCategories;
    private ArrayList<ProductCategory> subCategories;
    private boolean categorySelected;
    private boolean subcategorySelected;
    private long categoryIdProduct;
    EditText editTextProductName, editTextBrand;
    MaterialBetterSpinner spinnerCategory, spinnerSubcategory;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private BroadcastReceiver productEditBroadcastReceiver;
    private int currentImageCapturePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        currentImageCapturePosition = -1;
        super.onCreate(savedInstanceState);
        setContentView(com.koleshop.appkoleshop.R.layout.activity_product_edit);
        Toolbar toolbar = (Toolbar) findViewById(com.koleshop.appkoleshop.R.id.toolbar);
        toolbar.setTitle("Edit product");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            categoryId = extras.getLong("categoryId");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(com.koleshop.appkoleshop.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        product = KoleshopSingleton.getSharedInstance().getCurrentProduct();
        recyclerView = (RecyclerView) findViewById(com.koleshop.appkoleshop.R.id.rv_edit_product_variety);
        loadRecyclerView();
        initializeBroadcastReceivers();
    }

    private void initializeBroadcastReceivers() {
        productEditBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_CAPTURE_PRODUCT_VARIETY_PICTURE)) {
                    int position = intent.getIntExtra("position", -1);
                    if(position >= 0) {
                        currentImageCapturePosition = position;
                        dispatchTakePictureIntent();
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_CAPTURE_PRODUCT_VARIETY_PICTURE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(productEditBroadcastReceiver);
        super.onPause();
    }

    private void loadRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        productEditAdapter = new ProductEditAdapter(this, product, categoryId);
        recyclerView.setAdapter(productEditAdapter);
        productEditAdapter.notifyDataSetChanged();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && currentImageCapturePosition >= 0) {
            InventoryProductVariety var = product.getVarieties().get(currentImageCapturePosition);
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            productEditAdapter.changeImage(currentImageCapturePosition, imageBitmap);

            //start uploading the image and
        }
    }

}
