package com.koleshop.appkoleshop.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.koleshop.appkoleshop.adapters.ProductEditAdapter;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.ImageUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.realm.ProductCategory;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import io.realm.Realm;

public class ProductEditActivity extends AppCompatActivity {

    private Context mContext;
    private EditProduct product;
    private RecyclerView recyclerView;
    private ProductEditAdapter productEditAdapter;
    private long categoryId;

    private Map<String, String> requestMap;

    private Realm realm;
    private ArrayList<ProductCategory> parentCategories;
    private ArrayList<ProductCategory> subCategories;
    private boolean categorySelected;
    private boolean subcategorySelected;
    private long categoryIdProduct;
    EditText editTextProductName, editTextBrand;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private BroadcastReceiver productEditBroadcastReceiver;
    private String currentPhotoPath, currentImageFilename;
    private String imageCaptureTag;

    private String TAG = "ProductEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
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
        if (savedInstanceState != null) {
            categoryId = savedInstanceState.getLong("categoryId");
            Parcelable parcelableProduct = savedInstanceState.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);

            //get bitmapMap for product edit adapter
            String bitmapMapString = savedInstanceState.getString("bitmapMap");
            if(bitmapMapString!=null && !bitmapMapString.isEmpty()) {
                Map<String, Bitmap> bitmapMap = new Gson().fromJson(bitmapMapString, new TypeToken<Map<String, Bitmap>>() {}.getType());
                if (bitmapMap != null) {
                    //this.bitmapMap = bitmapMap;
                }
            }

            //get map tag filename
            String mapTagFilenameString = savedInstanceState.getString("mapTagFilename");
            if(mapTagFilenameString!=null && !mapTagFilenameString.isEmpty()) {
                Map<String, String> map = new Gson().fromJson(mapTagFilenameString, Map.class);
                if(map!=null) {
                    //this.mapTagFilename = map;
                }
            }

            if(!TextUtils.isEmpty(savedInstanceState.getString("currentPhotoPath"))) {
                currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            }
            if(!TextUtils.isEmpty(savedInstanceState.getString("currentImageFilename"))) {
                currentImageFilename = savedInstanceState.getString("currentImageFilename");
            }
            if(!TextUtils.isEmpty(savedInstanceState.getString("imageCaptureTag"))) {
                imageCaptureTag = savedInstanceState.getString("imageCaptureTag");
            }

            //fix the image uploading status if the activity missed the broadcast while capturing a new image
            fixImageUploadingProcess();
        } else if (extras != null) {
            categoryId = extras.getLong("categoryId");
            Parcelable parcelableProduct = extras.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(com.koleshop.appkoleshop.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        recyclerView = (RecyclerView) findViewById(com.koleshop.appkoleshop.R.id.rv_edit_product_variety);
        loadRecyclerView();
        initializeBroadcastReceivers();
    }

    private void initializeBroadcastReceivers() {
        productEditBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_CAPTURE_PRODUCT_VARIETY_PICTURE)) {
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
                }
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Parcelable parcelable = Parcels.wrap(product);
        outState.putParcelable("product", parcelable);
        outState.putLong("categoryId", categoryId);
        if (productEditAdapter != null) {
            Map<String, Bitmap> bitmapMap = productEditAdapter.getTempBitmapMap();
            if (bitmapMap != null) {
                String bitmapMapString = new Gson().toJson(bitmapMap);
                outState.putString("bitmapMap", bitmapMapString);
            }
        }
        /*if(mapTagFilename!=null) {
            String mapTagFilenameString = new Gson().toJson(mapTagFilename);
            outState.putString("mapTagFilename", mapTagFilenameString);
        }*/
        if(!TextUtils.isEmpty(currentPhotoPath)) {
            outState.putString("currentPhotoPath", currentPhotoPath);
        }
        if(!TextUtils.isEmpty(currentImageFilename)) {
            outState.putString("currentImageFilename", currentImageFilename);
        }
        if(!TextUtils.isEmpty(imageCaptureTag)) {
            outState.putString("imageCaptureTag", imageCaptureTag);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_CAPTURE_PRODUCT_VARIETY_PICTURE));
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_SUCCESS));
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_FAILED));
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
        //productEditAdapter.setTempBitmapMap(bitmapMap);
        recyclerView.setAdapter(productEditAdapter);
        productEditAdapter.notifyDataSetChanged();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //set processing on variety image view
            setProcessingOnVariety(imageCaptureTag, true, true);

            //1. resize the image and set to image vew......2. Upload the image
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    String varietyTag = params[0];
                    Bitmap resizedBitmap = ImageUtils.getResizedBitmap(600, 600, currentPhotoPath); //aspect ratio will be maintained
                    setTempBitmapOnVariety(varietyTag, resizedBitmap, true);
                    galleryAddPic();
                    /*if (mapTagFilename == null) {
                        mapTagFilename = new HashMap<>();
                    }
                    mapTagFilename.put(varietyTag, currentImageFilename);*/
                    ImageUtils.uploadBitmap(mContext, resizedBitmap, currentImageFilename, varietyTag);
                    return null;
                }
            }.execute(imageCaptureTag, null, null);
        }
    }

    private void setTempBitmapOnVariety(String imageTag, Bitmap bitmap, boolean notifyItemChanged) {
        productEditAdapter.setTempBitmap(imageTag, bitmap);
        if (notifyItemChanged) {
            int pos = getPositionOfEditProductVar(imageCaptureTag);
            productEditAdapter.notifyItemChanged(pos + 1);
        }
    }

    private void setProcessingOnVariety(String tag, boolean processing, boolean notifyItemChanged) {
        int positionOfEditProductVar = getPositionOfEditProductVar(tag);
        product.getEditProductVars().get(positionOfEditProductVar).setShowImageProcessing(processing);
        if (notifyItemChanged) {
            int positionInRecyclerView = positionOfEditProductVar + 1;
            productEditAdapter.notifyItemChanged(positionInRecyclerView);
        }
    }

    private void setImageUrlOnVariety(String tag, String url, boolean notifyItemChanged) {
        int positionOfEditProductVar = getPositionOfEditProductVar(tag);
        product.getEditProductVars().get(positionOfEditProductVar).setImageUrl(url);
        if (notifyItemChanged) {
            int positionInRecyclerView = positionOfEditProductVar + 1;
            productEditAdapter.notifyItemChanged(positionInRecyclerView);
        }
    }

    private int getPositionOfEditProductVar(String tag) {
        int index = 0;
        for (EditProductVar editProductVar : product.getEditProductVars()) {
            if (editProductVar.getTag().equalsIgnoreCase(tag)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Long userId = CommonUtils.getUserId(mContext);
        String random4 = CommonUtils.randomString(4);
        String imageFileName = "IMG_" + userId + "_" + timeStamp + "_" + random4;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentImageFilename = imageFileName + ".jpg";

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void fixImageUploadingProcess() {
        for (EditProductVar var : product.getEditProductVars()) {
            if (var.isShowImageProcessing()) {
                String status = PreferenceUtils.getPreferences(mContext, Constants.IMAGE_UPLOAD_STATUS_PREFIX + var.getTag());
                if (status != null && !status.isEmpty()) {
                    //set the correct status
                    boolean uploading = status.equalsIgnoreCase("uploading") ? true : false;
                    if (!uploading) {
                        var.setShowImageProcessing(false);
                    } else {
                        PreferenceUtils.setPreferences(mContext, Constants.IMAGE_UPLOAD_STATUS_PREFIX + var.getTag(), null);
                        if (status.equalsIgnoreCase("upload_failed")) {
                            setTempBitmapOnVariety(status, null, true);
                        } else if (status.equalsIgnoreCase("upload_success")) {
                            /*String filename = mapTagFilename.get(var.getTag());
                            if (filename != null && !filename.isEmpty()) {
                                String url = Constants.PUBLIC_IMAGE_URL_PREFIX + filename;
                                var.setImageUrl(url);
                            } else {
                                Log.d(TAG, "some problem while getting filename for tag");
                            }*/
                        }
                    }
                } else {
                    //variety has the correct status
                }
            }
        }
    }

}
