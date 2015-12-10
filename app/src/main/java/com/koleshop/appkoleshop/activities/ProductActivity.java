package com.koleshop.appkoleshop.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.ImageUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.fragments.productedit.ProductEditFragment;
import com.koleshop.appkoleshop.fragments.productedit.ProductVarietyEditFragment;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity implements ProductVarietyEditFragment.InteractionListener, ProductEditFragment.InteractionListener {

    Context mContext;
    private EditProduct product;
    private BroadcastReceiver productEditBroadcastReceiver;
    @Bind(R.id.container_product_edit_fragments)
    LinearLayout containerVarietyFragments;
    @Bind(R.id.sb_product)
    ScrollView scrollView;
    static String tagFragmentBasicInfo = "frag_basic_info";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 2;
    static String TAG = "ProductActivity";
    private String currentPhotoPath, currentImageFilename, imageCaptureTag;
    private boolean dontAddFragsAgain;

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
                addVariety();
                //Snackbar.make(view, "New variety added", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            dontAddFragsAgain = true;
            Parcelable parcelableProduct = savedInstanceState.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);

            if (!TextUtils.isEmpty(savedInstanceState.getString("currentPhotoPath"))) {
                currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            }
            if (!TextUtils.isEmpty(savedInstanceState.getString("currentImageFilename"))) {
                currentImageFilename = savedInstanceState.getString("currentImageFilename");
            }
            if (!TextUtils.isEmpty(savedInstanceState.getString("imageCaptureTag"))) {
                imageCaptureTag = savedInstanceState.getString("imageCaptureTag");
            }
        } else if (extras != null) {
            Parcelable parcelableProduct = extras.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);
        }
        if (!dontAddFragsAgain) {
            loadFragments();
        }
        initializeBroadcastReceivers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Parcelable parcelable = Parcels.wrap(product);
        outState.putParcelable("product", parcelable);
        if (!TextUtils.isEmpty(currentPhotoPath)) {
            outState.putString("currentPhotoPath", currentPhotoPath);
        }
        if (!TextUtils.isEmpty(currentImageFilename)) {
            outState.putString("currentImageFilename", currentImageFilename);
        }
        if (!TextUtils.isEmpty(imageCaptureTag)) {
            outState.putString("imageCaptureTag", imageCaptureTag);
        }
    }

    private void loadFragments() {

        //initialize basic info fragment
        ProductEditFragment fragmentBasicInfo = new ProductEditFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container_product_edit_fragments, fragmentBasicInfo, tagFragmentBasicInfo).commit();
        //fragmentBasicInfo.setProduct(product);

        //initialize variety fragments
        loadValidVarietyFragmentsIntoUi();
    }

    private void loadValidVarietyFragmentsIntoUi() {
        int fragmentNumbering = 0;
        for (EditProductVar editProductVar : product.getEditProductVars()) {
            if (editProductVar.isValid()) {
                editProductVar.setPosition(fragmentNumbering);
                ProductVarietyEditFragment varietyFragment = new ProductVarietyEditFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.container_product_edit_fragments, varietyFragment, editProductVar.getTag()).commit();
                //varietyFragment.setData(editProductVar, fragmentNumbering);
                fragmentNumbering++;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_SUCCESS));
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_FAILED));
        //fix the image uploading status if the activity missed the broadcast while capturing another image
        fixImageUploadingProcess();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(productEditBroadcastReceiver);
        super.onPause();
    }

    private void initializeBroadcastReceivers() {
        productEditBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_SUCCESS)) {
                    String tag = intent.getStringExtra("tag");
                    String filename = intent.getStringExtra("filename");
                    if (tag != null && !tag.isEmpty() && filename != null && !filename.isEmpty()) {
                        PreferenceUtils.setPreferences(mContext, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, null);
                        String url = Constants.PUBLIC_IMAGE_URL_PREFIX + filename;
                        setProcessingOnVariety(tag, false);
                        setImageUrlOnVariety(tag, url);
                    }

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_FAILED)) {
                    String tag = intent.getStringExtra("tag");
                    if (tag != null && !tag.isEmpty()) {
                        PreferenceUtils.setPreferences(mContext, Constants.IMAGE_UPLOAD_STATUS_PREFIX + tag, null);
                        setProcessingOnVariety(tag, false);
                        setTempImageOnVariety(tag, null, null);
                    }
                }
            }
        };
    }

    @Override
    public void deleteVariety(String deleteVarietyTag) {
        int position = 0;
        FragmentManager fm = getSupportFragmentManager();
        for (EditProductVar editProductVar : product.getEditProductVars()) {
            if (editProductVar.isValid()) {
                String tag = editProductVar.getTag();
                ProductVarietyEditFragment frag = (ProductVarietyEditFragment) fm.findFragmentByTag(tag);
                if (tag.equalsIgnoreCase(deleteVarietyTag)) {
                    //delete the variety
                    editProductVar.setValid(false);
                    //fragment transaction
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    ft.remove(frag);
                    // Start the animated transition.
                    ft.commit();
                } else {
                    //update the fragment position
                    frag.setPosition(position);
                    position++;
                }
            } // else skip this deleted variety
        }
    }

    private void addVariety() {
        EditProductVar newvar = new EditProductVar();
        newvar.setTag(CommonUtils.randomString(10));
        int numberOfValidVarieties = 0;
        for (EditProductVar var : product.getEditProductVars()) {
            if (var.isValid()) {
                numberOfValidVarieties++;
            }
        }
        newvar.setPosition(numberOfValidVarieties);
        newvar.setValid(true);
        newvar.setLimitedStock(1);
        product.getEditProductVars().add(newvar);
        ProductVarietyEditFragment varietyFragment = new ProductVarietyEditFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container_product_edit_fragments, varietyFragment, newvar.getTag()).commit();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 100);
    }

    @Override
    public void captureImage(String varietyTag) {
        imageCaptureTag = varietyTag;
        createImagePickDialog();
    }

    public void createImagePickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_image_picker)
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if (position == 0) {
                            dispatchTakePictureIntent();
                        } else if (position == 1) {
                            chooseImageFromGallery();
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public EditProductVar getVariety(String varietyTag) {
        return getProductVarietyByTag(varietyTag);
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
                Log.d(TAG, "could not create image capture file", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void chooseImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
                ||(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)) {

            //set processing on variety image view
            setProcessingOnVariety(imageCaptureTag, true);

            if(requestCode == PICK_IMAGE_REQUEST) {
                Uri uri = data.getData();
                currentPhotoPath = CommonUtils.getPathFromUri(mContext, uri);
                currentImageFilename = currentPhotoPath.substring(currentPhotoPath.lastIndexOf("/")+1);
            }

            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {

                    // 1. Resize the image
                    final String varietyTag = params[0];
                    final Bitmap resizedBitmap = ImageUtils.getResizedBitmap(600, 600, currentPhotoPath); //aspect ratio will be maintained

                    //2. Set resized bitmap to image vew
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTempImageOnVariety(varietyTag, resizedBitmap, currentImageFilename);
                        }
                    });

                    // 3. Upload the image and add to gallery
                    ImageUtils.uploadBitmap(mContext, resizedBitmap, currentImageFilename, varietyTag);
                    galleryAddPic();
                    return null;
                }
            }.execute(imageCaptureTag, null, null);
        }
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

    private void setProcessingOnVariety(String tag, boolean processing) {
        getProductVarietyByTag(tag).setShowImageProcessing(processing);
        reloadDataInFragmentWithTag(tag);
    }

    private void setImageUrlOnVariety(String tag, String url) {
        getProductVarietyByTag(tag).setImageUrl(url);
        reloadDataInFragmentWithTag(tag);
    }

    private void setTempImageOnVariety(String imageTag, Bitmap bitmap, String currentImageFilename) {
        EditProductVar productVar = getProductVarietyByTag(imageTag);
        productVar.setTempBitmapByteArray(ImageUtils.getByteArrayFromBitmap(bitmap));
        productVar.setImageFilename(currentImageFilename);
        reloadDataInFragmentWithTag(imageTag);
    }

    private EditProductVar getProductVarietyByTag(String tag) {
        for (EditProductVar var : product.getEditProductVars()) {
            if (var.getTag().equalsIgnoreCase(tag)) {
                return var;
            }
        }
        return null;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void reloadDataInFragmentWithTag(String tag) {
        ProductVarietyEditFragment frag = (ProductVarietyEditFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (frag != null) {
            frag.reloadData();
        }
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
                            setTempImageOnVariety(status, null, null);
                        } else if (status.equalsIgnoreCase("upload_success")) {
                            String filename = var.getImageFilename();
                            if (filename != null && !filename.isEmpty()) {
                                String url = Constants.PUBLIC_IMAGE_URL_PREFIX + filename;
                                var.setImageUrl(url);
                            } else {
                                Log.d(TAG, "some problem while getting filename for tag");
                            }
                        }
                    }
                } else {
                    //variety has the correct status
                }
            }
        }
    }

    @Override
    public EditProduct getProductFromParent() {
        return product;
    }
}
