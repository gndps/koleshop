package com.kolshop.kolshopmaterial.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.adapters.ProductEditAdapter;
import com.kolshop.kolshopmaterial.model.realm.ProductCategory;
import com.kolshop.kolshopmaterial.singletons.KoleshopSingleton;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        product = KoleshopSingleton.getSharedInstance().getCurrentProduct();
        recyclerView = (RecyclerView) findViewById(R.id.rv_edit_product_variety);
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        productEditAdapter = new ProductEditAdapter(this, product, categoryId);
        recyclerView.setAdapter(productEditAdapter);
        productEditAdapter.notifyDataSetChanged();
    }

}
