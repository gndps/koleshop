package com.koleshop.appkoleshop.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.fragments.product.ProductBasicInfoShopkeeper;
import com.koleshop.appkoleshop.fragments.product.ProductVarietyDetailsFragment;
import com.koleshop.appkoleshop.helper.VarietyAttributePool;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.services.CloudEndpointService;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.uipackage.BasicInfo;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

@Deprecated
public class AddEditProductActivity extends AppCompatActivity {

    private static String TAG = "Kolshop_AddEditActivity";

    private Toolbar toolbar;
    private ProductBasicInfoShopkeeper productBasicInfoShopkeeper;
    private BroadcastReceiver mMessageReceiver;
    private int numberOfVarieties;
    private String deleteTag;
    private LinearLayout varietyLinearLayoutContainer;
    private Product product;
    private Context mContext;
    private List<String> fragmentTagList;
    private List<ProductVariety> deletedProductVarieties;

    //POJO Fields
    private String id;
    private String name;
    private String brand;
    private Long brandId;
    private String description;
    private Long categoryId;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        setupViews();
        mContext = this;
        numberOfVarieties = 0;
        String userIdString = PreferenceUtils.getPreferences(mContext, Constants.KEY_USER_ID);
        if (userIdString.isEmpty()) {
            userId = 0l;
            Toast.makeText(AddEditProductActivity.this, "Please login to add product", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            userId = Long.parseLong(userIdString);
        }
        deletedProductVarieties = new ArrayList<>();
        fragmentTagList = new ArrayList<>();
        initializeBroadcastReceivers();
        loadDataToUI();
    }

    private void initializeBroadcastReceivers() {
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int numberOfVar = KoleshopSingleton.getSharedInstance().getNumberOfVarieties();
                if (intent.getAction().equals(Constants.ACTION_ADD_VARIETY)) {
                    if (numberOfVar < 32) {
                        addNewVariety();
                        if(numberOfVar == 1) {
                            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                            Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_PRODUCT_VARIETY_UI);
                            broadcastManager.sendBroadcast(broadcastIntent);
                        }
                    } else {
                        Toast.makeText(context, "Maximum number of varieties created", Toast.LENGTH_SHORT).show();
                    }
                } else if (intent.getAction().equals(Constants.ACTION_DELETE_VARIETY)) {
                    deleteTag = intent.getStringExtra("fragmentTag");
                    if (numberOfVar>1 && deleteTag != null && !deleteTag.isEmpty()) {
                        deleteVariety(deleteTag);
                        if(numberOfVar == 2) {
                            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                            Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_PRODUCT_VARIETY_UI);
                            broadcastManager.sendBroadcast(broadcastIntent);
                        }
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Constants.ACTION_ADD_VARIETY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Constants.ACTION_DELETE_VARIETY));
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
        } else if (id == R.id.action_save_product) {
            if (refreshProduct()) {
                Intent intent = new Intent(mContext, CloudEndpointService.class);
                intent.setAction(Constants.ACTION_SAVE_PRODUCT);
                intent.putExtra("product", Parcels.wrap(Product.class, product));
                startService(intent);
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean refreshProduct() {
        BasicInfo basicInfo = productBasicInfoShopkeeper.getBasicInfo();
        if(basicInfo!=null) {
            if(areProductVarietiesValid()) {
                //reset the va pool
                VarietyAttributePool.getInstance().reset();

                //build product
                product = new Product();
                product.setName(basicInfo.getName());
                product.setDescription(basicInfo.getDescription());
                product.setBrand(basicInfo.getBrand());
                product.setBrandId(basicInfo.getBrandId());
                product.setProductCategoryId(basicInfo.getProductCategoryId());
                if(id.isEmpty()) {
                    id = CommonUtils.generateRandomIdForDatabaseObject();
                }
                product.setId(id);
                product.setUserId(userId);
                product.setListProductVariety(getProductVarietyList());
                return true;
            }
        }
        return false;
    }

    private boolean areProductVarietiesValid() {
        for (String tag : fragmentTagList) {
            ProductVarietyDetailsFragment frag = (ProductVarietyDetailsFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (!frag.isFormValid()) {
                return false;
            }
        }
        return true;
    }

    private RealmList<ProductVariety> getProductVarietyList() {
        RealmList<ProductVariety> productVarietyRealmList = new RealmList<>();
        for (String tag : fragmentTagList) {
            ProductVarietyDetailsFragment frag = (ProductVarietyDetailsFragment) getSupportFragmentManager().findFragmentByTag(tag);
            ProductVariety productVariety = frag.getProductVariety();
            productVariety.setProductId(id);
            productVarietyRealmList.add(productVariety);
        }
        for (ProductVariety pv : deletedProductVarieties) {
            productVarietyRealmList.add(pv);
        }
        return productVarietyRealmList;
    }

    public void setupViews() {
        toolbar = (Toolbar) findViewById(R.id.add_edit_product_app_bar);
        productBasicInfoShopkeeper = (ProductBasicInfoShopkeeper) getSupportFragmentManager().findFragmentById(R.id.activity_add_edit_product_fragment_basic_product_info);
        varietyLinearLayoutContainer = (LinearLayout) productBasicInfoShopkeeper.getView().findViewById(R.id.linear_layout_product_varieties_details_container);
        //productBasicInfoShopkeeper.setProduct(selected product or null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ///getSupportActionBar().setHomeActionContentDescription("Save Product");
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_save);
    }

    public void loadDataToUI() {

        //extract data from bundle
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        Parcelable productParcel;
        Product product = null;
        if (bundle != null) {
            productParcel = bundle.getParcelable("product");
            product = Parcels.unwrap(productParcel);
        }
        if (product != null) {
            //prepare data
            this.product = product;
            id = product.getId();
            name = product.getName();
            brand = product.getBrand();
            brandId = product.getBrandId();
            description = product.getDescription();
            categoryId = product.getProductCategoryId();
            RealmList<ProductVariety> productVarieties = product.getListProductVariety();
            KoleshopSingleton.getSharedInstance().setNumberOfVarieties(getNumberOfValidVarieties(productVarieties));
            Bundle basicInfoBundle = new Bundle();
            basicInfoBundle.putString("name", name);
            basicInfoBundle.putString("brand", brand);
            basicInfoBundle.putLong("brandId", brandId);
            basicInfoBundle.putString("description", description);
            basicInfoBundle.putLong("categoryId", categoryId);
            productBasicInfoShopkeeper.setArguments(basicInfoBundle);

            //initialize User Interface
            for (int index = 0; index < productVarieties.size(); index++) {
                addProductVarietyToUserInterface(productVarieties.get(index), index);
            }
        } else {

            KoleshopSingleton.getSharedInstance().setNumberOfVarieties(0);
            //prepare empty data
            this.product = new Product();
            id = "";
            this.product.setId(id);
            this.product.setName("");
            this.product.setBrand("");
            this.product.setBrandId(0l);
            this.product.setDescription("");
            this.product.setProductCategoryId(0l);
            //initialize User Interface
            addNewVariety();
        }
    }

    private int getNumberOfValidVarieties(RealmList<ProductVariety> realmListProductVariety) {
        return realmListProductVariety.size(); //because we should initialize the product with the valid varieties only
        /*int i = 0;
        if(realmListProductVariety!=null) {
            for (ProductVariety pv : realmListProductVariety) {
                if (pv.isValidVariety()) i++;
            }
            return i;
        } else {
            return 1;
        }*/
    }

    private void addProductVarietyToUserInterface(ProductVariety productVariety, int index) {
        if(productVariety.isValidVariety()) {
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
            fragmentTagList.add(fragmentTag);
        }
    }

    private void addNewVariety() {
        //add a new variety fragment at the end....properties will be initialized from within ProductVarietyDetailsFragment
        String fragmentTag = CommonUtils.randomString(8);
        ProductVarietyDetailsFragment productVarietyDetailsFragment = new ProductVarietyDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("sortOrder", KoleshopSingleton.getSharedInstance().getNumberOfVarieties());
        productVarietyDetailsFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().add(R.id.linear_layout_product_varieties_details_container, productVarietyDetailsFragment, fragmentTag).commit();
        fragmentTagList.add(fragmentTag);
        KoleshopSingleton.getSharedInstance().increaseNumberOfVarieties();
        productBasicInfoShopkeeper.updateNumberOfVarieties();
        Log.d(TAG, "added a new variety with tag = " + fragmentTag);
    }

    private void deleteVariety(String framentTag) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(framentTag);
        notifyChangeSortOrder(framentTag);
        fragmentTagList.remove(frag.getTag());
        ProductVarietyDetailsFragment deletedFragment = (ProductVarietyDetailsFragment) frag;
        if(!deletedFragment.isBrandNewVariety()) {
            ProductVariety productVariety = deletedFragment.getProductVariety();
            productVariety.setValidVariety(false);
            deletedProductVarieties.add(productVariety);
        }
        getSupportFragmentManager().beginTransaction().remove(frag).commit();
        KoleshopSingleton.getSharedInstance().decreaseNumberOfVarieties();
        productBasicInfoShopkeeper.updateNumberOfVarieties();
        Log.d(TAG, "deleted variety with tag = " + framentTag);
    }

    private void notifyChangeSortOrder(String afterTag) {
        boolean notify = false;
        for(String fragTag : fragmentTagList) {
            if(fragTag.equalsIgnoreCase(afterTag))
            {
                notify = true;
            }
            if(notify) {
                ((ProductVarietyDetailsFragment) getSupportFragmentManager().findFragmentByTag(fragTag)).decrementSortOrder();
            }
        }
    }

    private Product getProduct() {
        Product product = new Product();
        //todo handle this shit
        return null;
        //product.setUserId();
        //product.setProductCategoryId(productBasicInfoShopkeeper.getProductCategoryId());
        //product.setDescription(productBasicInfoShopkeeper.);
    }
}
