package com.koleshop.appkoleshop.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.koleshop.api.productEndpoint.model.InventoryProduct;
import com.koleshop.api.productEndpoint.model.InventoryProductVariety;
import com.koleshop.api.productEndpoint.model.KoleResponse;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.ImageUtils;
import com.koleshop.appkoleshop.common.util.KoleCacheUtil;
import com.koleshop.appkoleshop.common.util.NetworkUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.fragments.productedit.ProductEditFragment;
import com.koleshop.appkoleshop.fragments.productedit.ProductVarietyEditFragment;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.services.ProductIntentService;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ProductActivity extends AppCompatActivity implements ProductVarietyEditFragment.InteractionListener, ProductEditFragment.InteractionListener {

    Context mContext;
    private EditProduct product;
    private BroadcastReceiver productEditBroadcastReceiver;
    @Bind(R.id.container_product_edit_fragments)
    LinearLayout containerVarietyFragments;
    @Bind(R.id.sb_product)
    NestedScrollView scrollView;
    static String tagFragmentBasicInfo = "frag_basic_info";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 2;
    static String TAG = "ProductActivity";
    private String currentPhotoPath, currentImageFilename, imageCaptureTag;
    private boolean dontAddFragsAgain;
    ProgressDialog processing;
    private String productSaveRequestTag;
    FloatingActionButton fab;
    boolean savedUsingBackButton;
    boolean savingProduct;
    Long oldCategoryId;

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

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addVariety();
                Snackbar.make(view, "Variety added", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            dontAddFragsAgain = true;
            Parcelable parcelableProduct = savedInstanceState.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);
            savedUsingBackButton = savedInstanceState.getBoolean("savedUsingBackButton");
            savingProduct = savedInstanceState.getBoolean("savingProduct");
            oldCategoryId = savedInstanceState.getLong("oldCategoryId");

            if (!TextUtils.isEmpty(savedInstanceState.getString("currentPhotoPath"))) {
                currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            }
            if (!TextUtils.isEmpty(savedInstanceState.getString("currentImageFilename"))) {
                currentImageFilename = savedInstanceState.getString("currentImageFilename");
            }
            if (!TextUtils.isEmpty(savedInstanceState.getString("imageCaptureTag"))) {
                imageCaptureTag = savedInstanceState.getString("imageCaptureTag");
            }
            if (!TextUtils.isEmpty(savedInstanceState.getString("productSaveRequestTag"))) {
                productSaveRequestTag = savedInstanceState.getString("productSaveRequestTag");
            }
        } else if (extras != null) {
            Parcelable parcelableProduct = extras.getParcelable("product");
            product = Parcels.unwrap(parcelableProduct);
            productSaveRequestTag = CommonUtils.randomString(6);
            oldCategoryId = product.getCategoryId();
        }
        if (!dontAddFragsAgain) {
            loadFragments();
        }

        initializeBroadcastReceivers();

        setupParent(containerVarietyFragments);

        //set refresh status of subcategories and products -- they should refresh data to show changes made in product/categories
        KoleshopSingleton kss = KoleshopSingleton.getSharedInstance();
        kss.setReloadSubcategories(false);
        kss.setReloadProductsCategoryId(0l);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Parcelable parcelable = Parcels.wrap(product);
        outState.putParcelable("product", parcelable);
        outState.putBoolean("savedUsingBackButton", savedUsingBackButton);
        outState.putBoolean("savingProduct", savingProduct);
        outState.putLong("oldCategoryId", oldCategoryId);
        if (!TextUtils.isEmpty(currentPhotoPath)) {
            outState.putString("currentPhotoPath", currentPhotoPath);
        }
        if (!TextUtils.isEmpty(currentImageFilename)) {
            outState.putString("currentImageFilename", currentImageFilename);
        }
        if (!TextUtils.isEmpty(imageCaptureTag)) {
            outState.putString("imageCaptureTag", imageCaptureTag);
        }
        if (!TextUtils.isEmpty(productSaveRequestTag)) {
            outState.putString("productSaveRequestTag", productSaveRequestTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save_product_changes:
                if (validateProductBeforeSaving()) {
                    saveProduct();
                }
                return true;
            /*case R.id.action_discard_product_changes:
                processingAnimation(false);
                onBackPressed();*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (product.isModified()) {
            //show dialog to save/discard changes
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
            builder.setMessage("Product modified. Save changes?")
                    .setPositiveButton(R.string.product_save_changes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            savedUsingBackButton = true;
                            if (validateProductBeforeSaving()) {
                                saveProduct();
                            }
                        }
                    })
                    .setNegativeButton(R.string.product_dont_save_changes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProductActivity.super.onBackPressed();
                        }
                    })
                    .setNeutralButton(R.string.product_save_cancel, null);
            builder.create().show();
        } else {
            //remove network request status from preferences
            if (product != null && product.getEditProductVars() != null && product.getEditProductVars().size() > 0) {
                for (EditProductVar var : product.getEditProductVars()) {
                    if (var.isShowImageProcessing()) {
                        NetworkUtils.setRequestStatusComplete(mContext, var.getTag());
                    }
                }
            }
            super.onBackPressed();
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


    private void initializeBroadcastReceivers() {
        productEditBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_SAVE_SUCCESS)) {
                    String receivedTag = intent.getStringExtra("requestTag");
                    if (productSaveRequestTag != null && !productSaveRequestTag.isEmpty()
                            && receivedTag != null && !receivedTag.isEmpty() && receivedTag.equalsIgnoreCase(productSaveRequestTag)) {

                        saveProductSuccess();

                    }

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_PRODUCT_SAVE_FAILED)) {
                    String receivedTag = intent.getStringExtra("requestTag");
                    if (productSaveRequestTag != null && !productSaveRequestTag.isEmpty()
                            && receivedTag != null && !receivedTag.isEmpty() && receivedTag.equalsIgnoreCase(productSaveRequestTag)) {

                        saveProductFailed();

                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_SAVE_SUCCESS));
        lbm.registerReceiver(productEditBroadcastReceiver, new IntentFilter(Constants.ACTION_PRODUCT_SAVE_FAILED));

        //fix the product save status if the activity missed the saveProductRequestComplete broadcast
        if (savingProduct) {
            String requestStatus = NetworkUtils.getRequestStatus(mContext, productSaveRequestTag);
            switch (requestStatus) {
                case Constants.REQUEST_STATUS_PROCESSING:
                    processingAnimation(true);
                    break;
                case Constants.REQUEST_STATUS_SUCCESS:
                    saveProductSuccess();
                    break;
                case Constants.REQUEST_STATUS_FAILED:
                    saveProductFailed();
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    protected void onPause() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(productEditBroadcastReceiver);
        super.onPause();
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
                    //ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
        Snackbar.make(fab, "Variety deleted", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    private void addVariety() {
        //scrollView.fullScroll(View.FOCUS_DOWN);
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
        }, 500);
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
    public EditProductVar refreshVariety(String varietyTag) {
        return getProductVarietyByTag(varietyTag);
    }

    @Override
    public void productModified(boolean modified) {
        product.setIsModified(modified);
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
                || (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)) {

            //get image path
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri uri = data.getData();
                currentPhotoPath = CommonUtils.getPathFromUri(mContext, uri);
                currentImageFilename = currentPhotoPath.substring(currentPhotoPath.lastIndexOf("/") + 1);
            }

            //load image in ui
            setProcessingOnVariety(imageCaptureTag, true);
            setTempImageOnVariety(imageCaptureTag, currentPhotoPath);
            productModified(true);

            //upload - happens on new thread
            ImageUtils.uploadBitmap(mContext, currentPhotoPath, currentImageFilename, imageCaptureTag);
        } else {
            //if no image taken, then set image view progress bar to gone
            setProcessingOnVariety(imageCaptureTag, false);
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

    private void setProcessingOnVariety(String tag, boolean processing) {
        getProductVarietyByTag(tag).setShowImageProcessing(processing);
        //getProductVarietyFragByTag(tag).setImageProcessing(processing);
    }

    /*private void setImageUrlOnVariety(String tag, String url) {
        getProductVarietyFragByTag(tag).setImageUrl(url);
    }*/

    private void setTempImageOnVariety(String imageTag, String imagePath) {
        getProductVarietyByTag(imageTag).setImagePath(imagePath);
        //ProductVarietyEditFragment frag = getProductVarietyFragByTag(imageTag);
        //frag.setImagePath(imagePath);
    }

    /*private ProductVarietyEditFragment getProductVarietyFragByTag(String tag) {
        ProductVarietyEditFragment frag = (ProductVarietyEditFragment) getSupportFragmentManager().findFragmentByTag(tag);
        return frag;
    }*/

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

    @Override
    public EditProduct getProductFromParent() {
        return product;
    }

    private void processingAnimation(boolean show) {
        if (processing == null) {
            processing = new ProgressDialog(mContext);
            processing.setCancelable(false);
            processing.setMessage("Saving Product...");
        }
        if (show) {
            processing.show();
        } else {
            processing.hide();
        }
    }

    private void saveProduct() {
        processingAnimation(true);
        removeEmptyProductVarieties();
        ProductIntentService.saveProduct(mContext, product, productSaveRequestTag);
        savingProduct = true;
    }

    private boolean validateProductBeforeSaving() {

        hideSoftKeyboard();

        if (product == null) {
            Snackbar.make(fab, "Please try again", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            return false;
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            ProductEditFragment productEditFragment = (ProductEditFragment) getSupportFragmentManager().findFragmentByTag(tagFragmentBasicInfo);
            productEditFragment.setErrorOnProductName();
            return false;
        }

        if (product.getBrand() == null || product.getBrand().trim().isEmpty()) {
            product.setBrand("No Brand");
        }

        if (product == null || product.getCategoryId() <= 0) {
            scrollView.fullScroll(View.FOCUS_UP);
            Snackbar.make(fab, "Please select a category and subcategory", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show();
            return false;
        }

        if (!productsDuplicateCheckClear()) {
            return false;
        }

        boolean atleastOneVarietyExist = false;
        for (EditProductVar var : product.getEditProductVars()) {
            if (var.isValid()) {
                atleastOneVarietyExist = true;
            }
        }

        if (!atleastOneVarietyExist) {
            Snackbar.make(fab, "Please add a variety using the plus button", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            return false;
        }

        return true;
    }

    private boolean productsDuplicateCheckClear() {
        for (EditProductVar varOuter : product.getEditProductVars()) {
            if (!varOuter.isValid()) {
                continue;
            }
            for (EditProductVar varInner : product.getEditProductVars()) {
                if (!varInner.isValid()) {
                    continue;
                }
                if (!varInner.getTag().equalsIgnoreCase(varOuter.getTag())) {
                    if (varInner.getQuantity().equalsIgnoreCase(varOuter.getQuantity()) && varInner.getPrice() == varOuter.getPrice()) {
                        //duplicate varieties here
                        int pos1 = varOuter.getPosition();
                        int pos2 = varInner.getPosition();
                        Snackbar.make(fab, "Varieties " + (pos1 + 1) + " and " + (pos2 + 1) + " are duplicates", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void removeEmptyProductVarieties() {
        Iterator<EditProductVar> iter = product.getEditProductVars().iterator();
        while (iter.hasNext()) {
            EditProductVar var = iter.next();
            if ((var.getPrice() == 0 || String.valueOf(var.getPrice()).isEmpty()) && (var.getQuantity() == null || var.getQuantity().isEmpty())) {
                iter.remove();
            }
        }
    }

    private void saveProductSuccess() {
        //update ui elements and variables
        NetworkUtils.setRequestStatusComplete(mContext, productSaveRequestTag);
        EditProduct receivedProduct = null;

        //todo extract saved product from shared prefs
        try {
            String savedProductResponse = PreferenceUtils.getPreferences(mContext, "savedProduct");
            KoleResponse result = new Gson().fromJson(savedProductResponse, KoleResponse.class);
            if (result != null) {
                LinkedTreeMap<String, Object> map = (LinkedTreeMap<String, Object>) result.getData();
                InventoryProduct savedProduct = new InventoryProduct();
                savedProduct.setId(Long.parseLong((String) map.get("id")));
                savedProduct.setName((String) map.get("name"));
                savedProduct.setBrand((String) map.get("brand"));

                //extract varieties
                List<InventoryProductVariety> vars = new ArrayList<>();
                ArrayList<LinkedTreeMap<String, Object>> arrayListVarieties = (ArrayList<LinkedTreeMap<String, Object>>) map.get("varieties");
                for (LinkedTreeMap<String, Object> var : arrayListVarieties) {
                    InventoryProductVariety inventoryProductVariety = new InventoryProductVariety();
                    inventoryProductVariety.setId(Long.valueOf((String) var.get("id")));
                    inventoryProductVariety.setQuantity((String) var.get("quantity"));
                    inventoryProductVariety.setPrice(Float.valueOf(String.valueOf(var.get("price"))));
                    inventoryProductVariety.setImageUrl((String) var.get("imageUrl"));
                    inventoryProductVariety.setValid((Boolean) var.get("valid"));
                    inventoryProductVariety.setLimitedStock(((Double) var.get("limitedStock")).intValue());
                    vars.add(inventoryProductVariety);
                }
                savedProduct.setVarieties(vars);

                receivedProduct = new EditProduct(savedProduct, product.getCategoryId());

                //update the product in ui and cache
                boolean newProduct = product.getId() == null || product.getId() == 0;
                product = receivedProduct;
                if (newProduct) {
                    KoleCacheUtil.addNewProductToCache(product);
                } else {
                    if (oldCategoryId == product.getCategoryId()) {
                        KoleCacheUtil.updateProductInCache(product);
                    } else {
                        KoleshopSingleton.getSharedInstance().setReloadSubcategories(true);
                        KoleCacheUtil.invalidateProductsCache(product.getCategoryId(), true);
                        KoleCacheUtil.invalidateProductsCache(oldCategoryId, true);
                        KoleCacheUtil.invalidateInventoryCategories(true);

                        //get parent category ids for both these categories and invalidate their cache
                        Realm realm = Realm.getDefaultInstance();
                        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class)
                                .equalTo("id", product.getCategoryId())
                                .or()
                                .equalTo("id", oldCategoryId);
                        RealmResults<ProductCategory> realmResults = query.findAll();
                        if (realmResults != null && realmResults.size() > 0) {
                            Iterator<ProductCategory> iterator = realmResults.iterator();
                            while (iterator.hasNext()) {
                                ProductCategory cat = iterator.next();
                                if (cat != null && cat.getParentCategoryId() > 0l) {
                                    KoleCacheUtil.invalidateInventorySubcategories(cat.getParentCategoryId(), true);
                                    KoleshopSingleton.getSharedInstance().setReloadProductsCategoryId(0l);
                                }
                            }
                        }
                    }
                }
                KoleshopSingleton.getSharedInstance().setReloadProductsCategoryId(oldCategoryId);
            }

        } catch (Exception e) {
            Log.e(TAG, "some prob in parsing saved product from shared prefs", e);
            KoleCacheUtil.invalidateProductsCache(product.getCategoryId(), true);
            KoleshopSingleton.getSharedInstance().setReloadSubcategories(true);
            PreferenceUtils.setPreferences(mContext, "savedProduct", null);
            super.onBackPressed();
        }

        //remove the saved product from shared prefs
        PreferenceUtils.setPreferences(mContext, "savedProduct", null);

        processingAnimation(false);
        savingProduct = false;
        product.setIsModified(false);

        if (savedUsingBackButton) {
            super.onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), "Product Saved", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            //Snackbar.make(fab, "Product Saved", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            //reload fragments with new fragment tags
            //ProductEditFragment fragmentBasicInfo = new ProductEditFragment();
            //getSupportFragmentManager().beginTransaction().replace(R.id.container_product_edit_fragments, fragmentBasicInfo, tagFragmentBasicInfo).commitAllowingStateLoss();
            /*List<Fragment> frags = getSupportFragmentManager().getFragments();

            for(Fragment frag : frags) {
                if(frag instanceof ProductVarietyEditFragment) {
                    EditProductVar var = ((ProductVarietyEditFragment) frag).getVariety();
                    for(EditProductVar editProductVar : product.getEditProductVars()) {
                        if(var!=null && var.getQuantity().trim().equalsIgnoreCase(editProductVar.getQuantity().trim())
                                && var.getPrice() == editProductVar.getPrice()) {
                            //varieties match
                            getSupportFragmentManager().beginTransaction().replace(R.id.container_product_edit_fragments)
                        }
                    }

                } else if(frag instanceof  ProductEditFragment) {
                    ((ProductEditFragment) frag).refreshData();
                }
            }*/
            //loadValidVarietyFragmentsIntoUi();
        }

    }

    private void saveProductFailed() {

        NetworkUtils.setRequestStatusComplete(mContext, productSaveRequestTag);
        processingAnimation(false);
        savingProduct = false;
        Snackbar.make(fab, "Problem in saving product. Please try again", Snackbar.LENGTH_LONG).setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        }).show();

    }

    protected void setupParent(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }
        //If a layout container, iterate over children
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupParent(innerView);
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        Activity activity = ProductActivity.this;
        if (activity != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
