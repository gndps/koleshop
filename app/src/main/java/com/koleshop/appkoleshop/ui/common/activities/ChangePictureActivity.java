package com.koleshop.appkoleshop.ui.common.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.services.SettingsIntentService;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.ImageUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class ChangePictureActivity extends AppCompatActivity {

    private static final String TAG = "ChangePictureActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    @Bind(R.id.app_bar)
    Toolbar toolbar;
    @Bind(R.id.image_view_change_picture)
    ImageView imageViewPicture;
    @BindString(R.string.title_shop_picture)
    String shopTitle;
    @Bind(R.id.ll_activity_change_picture)
    LinearLayout linearLayout;
    @Bind(R.id.pb_acp)
    ProgressBar progressBar;

    Context mContext;
    private String currentPhotoPath;
    private String currentImageFilename;
    private String imageUploadRequestId;
    private BroadcastReceiver broadcastReceiver;
    private String imageUrl;
    private int widthDimensionInDp;
    private boolean isHeaderImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_picture);
        ButterKnife.bind(this);
        mContext = this;
        setupToolbar();
        setupImageView();
        initializeBroadcastReceiver();
        if (savedInstanceState != null) {
            isHeaderImage = savedInstanceState.getBoolean("isHeaderImage");
        } else if (getIntent() != null && getIntent().getExtras() != null) {
            isHeaderImage = getIntent().getExtras().getBoolean("isHeaderImage");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_picture) {
            createImagePickDialog();
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_SUCCESS));
        lbm.registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTION_UPLOAD_IMAGE_FAILED));
        refreshImage();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isHeaderImage", isHeaderImage);
    }

    private void initializeBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_SUCCESS)) {
                    String tag = intent.getStringExtra("tag");
                    String filename = intent.getStringExtra("filename");
                    if (tag != null && !tag.isEmpty() && filename != null && !filename.isEmpty() && tag.equalsIgnoreCase(imageUploadRequestId)) {
                        NetworkUtils.setRequestStatusComplete(mContext, tag);
                        imageUrl = Constants.PUBLIC_PROFILE_IMAGE_URL_PREFIX + filename;
                        SettingsIntentService.refreshSellerSettings(mContext);
                        refreshImage();
                    }

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPLOAD_IMAGE_FAILED)) {
                    String tag = intent.getStringExtra("tag");
                    if (tag != null && !tag.isEmpty() && tag.equalsIgnoreCase(imageUploadRequestId)) {
                        NetworkUtils.setRequestStatusComplete(mContext, tag);
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(linearLayout, "Could not change the picture", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    private void setupToolbar() {
        toolbar.setTitle(shopTitle);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }
    }

    private void setupImageView() {

        //make the image view a square
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int dpWidth = size.x;
        int dpHeight = size.y;
        int dpSmallDimension;
        if (dpHeight > dpWidth) {
            dpSmallDimension = dpWidth;
        } else {
            dpSmallDimension = dpHeight;
        }

        ViewGroup.LayoutParams layoutParams = imageViewPicture.getLayoutParams();
        layoutParams.width = dpSmallDimension;
        layoutParams.height = dpSmallDimension;
        widthDimensionInDp = dpSmallDimension;
        imageViewPicture.setLayoutParams(layoutParams);

    }

    private void refreshImage() {

        if (TextUtils.isEmpty(imageUrl)) {
            SellerSettings sellerSettings = KoleshopUtils.getSettingsFromCache(mContext);

            if (sellerSettings != null
                    &&
                    ((!TextUtils.isEmpty(sellerSettings.getImageUrl()) && !isHeaderImage)
                            ||
                            (!TextUtils.isEmpty(sellerSettings.getHeaderImageUrl()) && isHeaderImage))) {
                if (isHeaderImage) {
                    imageUrl = sellerSettings.getHeaderImageUrl();
                } else {
                    imageUrl = sellerSettings.getImageUrl();
                }
            }
            if (TextUtils.isEmpty(imageUrl)) {
                progressBar.setVisibility(View.GONE);
                if (sellerSettings != null && sellerSettings.getAddress() != null
                        && !TextUtils.isEmpty(sellerSettings.getAddress().getName())) {
                    TextDrawable textDrawable = KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), false);
                    imageViewPicture.setImageDrawable(textDrawable);
                }
            } else {
                if (!TextUtils.isEmpty(currentPhotoPath)) {
                    progressBar.setVisibility(View.GONE);
                    Picasso.with(mContext)
                            .load(currentPhotoPath)
                            .fit().centerCrop()
                            .into(imageViewPicture);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.with(mContext)
                            .load(imageUrl)
                            .fit().centerCrop()
                            .into(imageViewPicture, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
            Picasso.with(mContext)
                    .load(imageUrl)
                    .fit().centerCrop()
                    .into(imageViewPicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
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
                Snackbar.make(linearLayout, "Some problem occured", Snackbar.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Snackbar.make(linearLayout, "Camera unavailable", Snackbar.LENGTH_SHORT).show();
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
                || (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)) {

            //get image path
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri uri = data.getData();
                currentPhotoPath = CommonUtils.getPathFromUri(mContext, uri);
                currentImageFilename = currentPhotoPath.substring(currentPhotoPath.lastIndexOf("/") + 1);
            }

            //show progress bar
            progressBar.setVisibility(View.VISIBLE);

            //upload - happens on new thread
            imageUploadRequestId = CommonUtils.randomString(6);
            ImageUtils.uploadProfilePicture(mContext, currentPhotoPath, currentImageFilename, imageUploadRequestId, isHeaderImage);
        } else {
            //if no image taken, then set image view progress bar to gone
            progressBar.setVisibility(View.GONE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Long userId = CommonUtils.getUserId(mContext);
        String random6 = CommonUtils.randomString(6);
        String imageFileName = "IMG" + "_" + timeStamp + "_" + random6 + "_" + userId + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        /*File image = File.createTempFile(
                imageFileName,  // prefix
                null,         // suffix
                storageDir      // directory
        );*/

        File image = new File(storageDir, imageFileName);

        currentImageFilename = imageFileName + ".jpg";

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
