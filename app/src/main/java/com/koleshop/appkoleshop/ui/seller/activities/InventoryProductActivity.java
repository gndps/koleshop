package com.koleshop.appkoleshop.ui.seller.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.ui.seller.adapters.InventoryCategoryViewPagerAdapter;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleCacheUtil;
import com.koleshop.appkoleshop.ui.seller.fragments.product.InventoryProductFragment;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InventoryProductActivity extends AppCompatActivity implements InventoryProductFragment.InventoryProductFragmentInteractionListener {

    long parentCategoryId;
    String categoryTitle;
    Context mContext;
    ViewFlipper viewFlipper;
    private BroadcastReceiver inventoryProductBroadcastReceiver;
    TabLayout tabLayout;
    ViewPager viewPager;
    private static final String TAG = "InventoryPrductActity";
    private boolean myInventory = false;
    int selectedPage;
    @Bind(R.id.fab_add_new_product)
    FloatingActionButton menuMultipleActions;
    Long selectedCategoryId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_product);
        ButterKnife.bind(this);
        mContext = this;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            parentCategoryId = extras.getLong("categoryId");
            categoryTitle = extras.getString("categoryTitle");
            myInventory = extras.getBoolean("myInventory");
        }
        initializeBroadcastReceivers();
        setupActivity();
        fetchCategories();
        if (savedInstanceState != null) {
            selectedPage = savedInstanceState.getInt("selectedPage");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(inventoryProductBroadcastReceiver, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS));
        lbm.registerReceiver(inventoryProductBroadcastReceiver, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED));
        if (KoleshopSingleton.getSharedInstance().isReloadSubcategories()) {
            KoleshopSingleton.getSharedInstance().setReloadSubcategories(false);
            fetchCategories();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedPage", selectedPage);
    }

    private void initializeBroadcastReceivers() {
        inventoryProductBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS)) {
                    long receivedCategoryId = intent.getLongExtra("catId", 0l);
                    if (receivedCategoryId == parentCategoryId) {
                        loadCategories(null);
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED)) {
                    Long receivedCategoryId = intent.getLongExtra("catId", 0l);
                    if (receivedCategoryId == parentCategoryId) {
                        failedLoadingCategories();
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(inventoryProductBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupActivity() {
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper_inventory_product_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar_inventory_products);
        toolbar.setTitle(categoryTitle);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout = (TabLayout) findViewById(R.id.tab_inventory_product);
        tabLayout.setVisibility(View.GONE);
        initFabMenu();
    }

    private void fetchCategories() {
        List<ProductCategory> list = KoleCacheUtil.getCachedProductCategoriesFromRealm(myInventory, parentCategoryId);
        if (list != null && list.size() > 0 && Constants.KOLE_CACHE_ALLOWED) {
            loadCategories(list);
        } else {
            requestCategoriesFromInternet();
        }
    }

    private void requestCategoriesFromInternet() {
        viewFlipper.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(0);
        if (viewPager != null) {
            viewPager.setVisibility(View.GONE);
        }
        tabLayout.setVisibility(View.GONE);
        Intent serviceIntent = new Intent(mContext, CommonIntentService.class);
        serviceIntent.setAction(Constants.ACTION_FETCH_INVENTORY_SUBCATEGORIES);
        serviceIntent.putExtra("categoryId", parentCategoryId);
        serviceIntent.putExtra("myInventory", myInventory);
        startService(serviceIntent);
    }

    private void loadCategories(List<ProductCategory> list) {
        viewFlipper.setVisibility(View.GONE);
        viewPager = (ViewPager) findViewById(R.id.view_pager_inventory_product);
        viewPager.setVisibility(View.VISIBLE);
        //viewPager.setOffscreenPageLimit(1); 1 is default
        setupViewPager(viewPager, list);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(viewPager);
        if (viewPager.getChildCount() > selectedPage) {
            viewPager.setCurrentItem(selectedPage);
        }
        changeTabsFont();
    }

    private void setupViewPager(ViewPager viewPager, List<ProductCategory> subcategories) {
        InventoryCategoryViewPagerAdapter adapter = new InventoryCategoryViewPagerAdapter(getSupportFragmentManager(), myInventory);
        final List<ProductCategory> categories;
        if (subcategories != null && subcategories.size() > 0) {
            categories = subcategories;
        } else {
            categories = KoleCacheUtil.getCachedProductCategoriesFromRealm(myInventory, parentCategoryId);
        }
        adapter.setInventoryCategories(categories);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectedPage = position;
                selectedCategoryId = categories.get(position).getId();
                Long leftCategoryId = null, rightCategoryId = null;
                if (position - 1 >= 0) {
                    leftCategoryId = categories.get(position - 1).getId();
                }
                if (position + 1 < categories.size()) {
                    rightCategoryId = categories.get(position + 1).getId();
                }
                List<Long> listOfCategoriesWhoseFragmentsAreLoaded = new ArrayList<>();
                listOfCategoriesWhoseFragmentsAreLoaded.add(selectedCategoryId);
                if (leftCategoryId != null) {
                    listOfCategoriesWhoseFragmentsAreLoaded.add(leftCategoryId);
                }
                if (rightCategoryId != null) {
                    listOfCategoriesWhoseFragmentsAreLoaded.add(rightCategoryId);
                }
                KoleshopSingleton.getSharedInstance().setReloadProductsCategoryIds(listOfCategoriesWhoseFragmentsAreLoaded);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        selectedCategoryId = categories.get(0).getId();
        menuMultipleActions.setVisibility(View.VISIBLE);
    }

    private void failedLoadingCategories() {
        viewFlipper.setVisibility(View.VISIBLE);
        viewFlipper.setDisplayedChild(1);
        if (viewPager != null) {
            viewPager.setVisibility(View.GONE);
        }
    }

    public void retry(View v) {
        fetchCategories();
    }

    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Regular.ttf");
                    ((TextView) tabViewChild).setTypeface(typeface, Typeface.NORMAL);
                }
            }
        }
    }

    private void initFabMenu() {
        menuMultipleActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewProduct();
            }
        });
        menuMultipleActions.setSize(FloatingActionButton.SIZE_NORMAL);
        menuMultipleActions.setColorNormalResId(R.color.white);
        menuMultipleActions.setColorPressedResId(R.color.offwhite);
        menuMultipleActions.setIcon(R.drawable.ic_add_grey600_48dp);
    }

    private void addNewProduct() {
        Intent intentAddProduct = new Intent(mContext, ProductActivity.class);
        EditProduct product = new EditProduct();
        product.setCategoryId(selectedCategoryId);
        EditProductVar emptyVar = new EditProductVar();
        List<EditProductVar> varList = new ArrayList<>();
        varList.add(emptyVar);
        product.setEditProductVars(varList);
        Parcelable productParcel = Parcels.wrap(product);
        intentAddProduct.putExtra("product", productParcel);
        startActivity(intentAddProduct);
    }

    @Override
    public void hideFloatingActionButton() {
        if (menuMultipleActions.getVisibility() == View.VISIBLE) {
            menuMultipleActions.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.hide_to_bottom));
            menuMultipleActions.setVisibility(View.GONE);
        }
    }

    @Override
    public void showFloatingActionButton() {
        if (menuMultipleActions.getVisibility() == View.GONE) {
            menuMultipleActions.setVisibility(View.VISIBLE);
            menuMultipleActions.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.show_from_bottom));
        }
    }
}
