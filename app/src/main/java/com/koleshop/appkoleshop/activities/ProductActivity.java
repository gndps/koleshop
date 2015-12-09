package com.koleshop.appkoleshop.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.fragments.productedit.ProductEditFragment;
import com.koleshop.appkoleshop.fragments.productedit.ProductVarietyEditFragment;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;

import org.parceler.Parcels;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity implements ProductEditFragment.OnFragmentInteractionListener, ProductVarietyEditFragment.OnFragmentInteractionListener{

    Context mContext;
    private Long categoryId;
    private EditProduct product;
    private BroadcastReceiver productEditBroadcastReceiver;
    @Bind(R.id.container_product_edit_fragments) LinearLayout containerVarietyFragments;
    static String tagFragmentBasicInfo = "frag_basic_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            categoryId = savedInstanceState.getLong("categoryId");
            Parcelable parcelableProduct = savedInstanceState.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);

            /*//get bitmapMap for product edit adapter
            String bitmapMapString = savedInstanceState.getString("bitmapMap");
            if(bitmapMapString!=null && !bitmapMapString.isEmpty()) {
                Map<String, Bitmap> bitmapMap = new Gson().fromJson(bitmapMapString, new TypeToken<Map<String, Bitmap>>() {}.getType());
                if (bitmapMap != null) {
                    this.bitmapMap = bitmapMap;
                }
            }*/

            /*//get map tag filename
            String mapTagFilenameString = savedInstanceState.getString("mapTagFilename");
            if(mapTagFilenameString!=null && !mapTagFilenameString.isEmpty()) {
                Map<String, String> map = new Gson().fromJson(mapTagFilenameString, Map.class);
                if(map!=null) {
                    this.mapTagFilename = map;
                }
            }*/

            /*if(!TextUtils.isEmpty(savedInstanceState.getString("currentPhotoPath"))) {
                currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            }
            if(!TextUtils.isEmpty(savedInstanceState.getString("currentImageFilename"))) {
                currentImageFilename = savedInstanceState.getString("currentImageFilename");
            }*/
            /*if(!TextUtils.isEmpty(savedInstanceState.getString("imageCaptureTag"))) {
                imageCaptureTag = savedInstanceState.getString("imageCaptureTag");
            }*/

            //fix the image uploading status if the activity missed the broadcast while capturing a new image
            //fixImageUploadingProcess();
        } else if (extras != null) {
            categoryId = extras.getLong("categoryId");
            Parcelable parcelableProduct = extras.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);
        }
        loadFragments();
    }

    private void loadFragments() {

        //initialize basic info fragment
        Fragment fragmentBasicInfo = new ProductEditFragment();
        Bundle args = new Bundle();
        args.putLong("id", product.getId());
        args.putString("name", product.getName());
        args.putString("brand", product.getBrand());
        args.putLong("categoryId", categoryId);
        fragmentBasicInfo.setArguments(args);
        getSupportFragmentManager().beginTransaction().add(R.id.container_product_edit_fragments, fragmentBasicInfo, tagFragmentBasicInfo).commit();

        //initialize variety fragments
        for(EditProductVar editProductVar : product.getEditProductVars()) {
            Fragment varietyFragment = new ProductVarietyEditFragment();
            Bundle argsVar = new Bundle();
            Parcelable productVariety = Parcels.wrap(editProductVar);
            argsVar.putParcelable("variety", productVariety);
            varietyFragment.setArguments(argsVar);
            getSupportFragmentManager().beginTransaction().add(R.id.container_product_edit_fragments, varietyFragment, varietyFragment.getTag()).commit();
        }
    }

    private void initializeBroadcastReceivers() {
        productEditBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /*if (intent.getAction().equalsIgnoreCase(Constants.ACTION_CAPTURE_PRODUCT_VARIETY_PICTURE)) {
                    String tag = intent.getStringExtra("tag");
                    if (tag != null && !tag.isEmpty()) {
                        imageCaptureTag = tag;
                        dispatchTakePictureIntent();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_SUCCESS)) {
                    String tag = intent.getStringExtra("tag");
                    String filename = intent.getStringExtra("filename");
                    if (tag != null && !tag.isEmpty() && filename != null && !filename.isEmpty()) {
                        PreferenceUtils.setPreferences(mContext, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, null);
                        String url = Constants.PUBLIC_IMAGE_URL_PREFIX + filename;
                        setProcessingOnVariety(tag, false, false);
                        setImageUrlOnVariety(tag, url, true);
                    }

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_FAILED)) {
                    String tag = intent.getStringExtra("tag");
                    if (tag != null && !tag.isEmpty()) {
                        PreferenceUtils.setPreferences(mContext, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, null);
                        setProcessingOnVariety(tag, false, false);
                        setTempBitmapOnVariety(tag, null, true);
                    }
                }*/
            }
        };
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
