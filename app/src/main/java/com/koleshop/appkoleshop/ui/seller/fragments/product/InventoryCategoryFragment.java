package com.koleshop.appkoleshop.ui.seller.fragments.product;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.ui.seller.activities.InventoryProductActivity;
import com.koleshop.appkoleshop.ui.seller.activities.ProductActivity;
import com.koleshop.appkoleshop.ui.common.activities.VerifyPhoneNumberActivity;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleCacheUtil;
import com.koleshop.appkoleshop.listeners.KolClickListener;
import com.koleshop.appkoleshop.listeners.KolRecyclerTouchListener;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.koleshop.appkoleshop.ui.seller.adapters.InventoryCategoryAdapter;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InventoryCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryCategoryAdapter inventoryCategoryAdapter;
    ViewFlipper viewFlipper;
    Context mContext;
    BroadcastReceiver mBroadcastReceiverInventoryCategoryFragment;
    private static final String TAG = "InventoryCategoryFrag";
    private boolean myInventory = false;
    @Bind(R.id.multiple_actions)
    FloatingActionsMenu menuMultipleActions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        Bundle args = getArguments();
        if (args != null) {
            myInventory = args.getBoolean("myInventory", false);
        }
        initializeBroadcastReceivers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(com.koleshop.appkoleshop.R.layout.fragment_inventory_category, container, false);
        ButterKnife.bind(this, layout);
        recyclerView = (RecyclerView) layout.findViewById(com.koleshop.appkoleshop.R.id.rv_inventory_category);
        viewFlipper = (ViewFlipper) layout.findViewById(com.koleshop.appkoleshop.R.id.viewflipper_inventory_category);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .margin(getResources().getDimensionPixelSize(com.koleshop.appkoleshop.R.dimen.recycler_view_left_margin),
                        getResources().getDimensionPixelSize(com.koleshop.appkoleshop.R.dimen.recycler_view_right_margin))
                .build());
        inventoryCategoryAdapter = new InventoryCategoryAdapter(getActivity(), null);
        //inventoryCategoryAdapter.
        recyclerView.setAdapter(inventoryCategoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new KolRecyclerTouchListener(getActivity(), recyclerView, new KolClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if(menuMultipleActions!=null && menuMultipleActions.isExpanded()) {
                    menuMultipleActions.collapse();
                } else {
                    Intent intent = new Intent(mContext, InventoryProductActivity.class);
                    intent.putExtra("categoryId", inventoryCategoryAdapter.getInventoryCategoryId(position));
                    intent.putExtra("myInventory", myInventory);
                    String categoryName = inventoryCategoryAdapter.getInventoryCategoryName(position);
                    if (categoryName != null && !categoryName.isEmpty()) {
                        intent.putExtra("categoryTitle", categoryName);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(mContext)
                                .setTitle("Some problem occurred")
                                .setMessage("Please try again")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                /*final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.exit_to_right, R.anim.enter_from_left);
                ft.replace(R.id.fragment_container, new InventoryProductFragment(), "InventoryProductFragment");
                ft.addToBackStack(null);
                ft.commit();*/
                    //Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {
                //Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_LONG).show();

            }
        }));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(menuMultipleActions!=null && menuMultipleActions.isExpanded()) {
                    menuMultipleActions.collapse();
                } else {
                    super.onScrolled(recyclerView, dx, dy);
                }
            }
        });
        initFabMenu();
        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory_category_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_product:
                addNewProduct();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiverInventoryCategoryFragment, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiverInventoryCategoryFragment, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED));
        lbm.registerReceiver(mBroadcastReceiverInventoryCategoryFragment, new IntentFilter(Constants.ACTION_GCM_BROADCAST_INVENTORY_CREATED));
        loadInventoryCategories();
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiverInventoryCategoryFragment = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS)) {
                    inventoryLoadSuccess(null);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED)) {
                    if (intent.getExtras() != null && intent.getStringExtra("status") != null) {
                        intent.getStringExtra("status").equalsIgnoreCase(Constants.STATUS_KOLE_RESPONSE_CREATING_INVENTORY);
                        //wait for push broadcast of inventory created
                        //do nothing -- because processing is already showing...
                    } else {
                        inventoryLoadFailed();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_GCM_BROADCAST_INVENTORY_CREATED)) {
                    loadInventoryCategories();
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.unregisterReceiver(mBroadcastReceiverInventoryCategoryFragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loadInventoryCategories() {
        List<ProductCategory> cachedCategories = getCachedInventoryCategories();
        if (cachedCategories != null && Constants.KOLE_CACHE_ALLOWED) {
            inventoryLoadSuccess(cachedCategories);
        } else {
            loadInventoryCategoriesFromInternet();
        }
    }

    public List<ProductCategory> getCachedInventoryCategories() {
        return KoleCacheUtil.getCachedProductCategoriesFromRealm(myInventory, null);
    }

    public void loadInventoryCategoriesFromInternet() {
        viewFlipper.setDisplayedChild(0);
        Intent commonIntent = new Intent(getActivity(), CommonIntentService.class);
        commonIntent.setAction(Constants.ACTION_FETCH_INVENTORY_CATEGORIES);
        commonIntent.putExtra("myInventory", myInventory);
        getActivity().startService(commonIntent);
    }

    private void inventoryLoadSuccess(List<ProductCategory> categories) {
        List<ProductCategory> listOfInventoryCategories;
        if (categories == null) {
            listOfInventoryCategories = getCachedInventoryCategories();
        } else {
            listOfInventoryCategories = categories;
        }
        if (listOfInventoryCategories != null) {
            inventoryCategoryAdapter.setData(listOfInventoryCategories);
            viewFlipper.setDisplayedChild(1);
        } else {
            inventoryLoadFailed();
            Toast.makeText(mContext, "some problem while parsing gson cached response", Toast.LENGTH_SHORT).show();
        }
    }

    private void inventoryLoadFailed() {
        viewFlipper.setDisplayedChild(2);
        String title;
        if (myInventory) {
            title = "My Shop";
        } else {
            title = "Ware House";
        }
        if (CommonUtils.getUserId(mContext) == null || CommonUtils.getUserId(mContext) <= 0) {
            new AlertDialog.Builder(mContext)
                    .setTitle("Please login to open " + title)
                    .setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intentLogin = new Intent(mContext, VerifyPhoneNumberActivity.class);
                            startActivity(intentLogin);
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        } else {
            new AlertDialog.Builder(mContext)
                    .setTitle("Problem in loading " + title)
                    .setMessage("Please try again")
                    .setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadInventoryCategories();
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        }
    }

    private void initFabMenu() {

        if (myInventory) {

            //1. add new product fab
            final FloatingActionButton actionAddNewProduct = new FloatingActionButton(mContext);
            actionAddNewProduct.setTitle("Add new product");
            actionAddNewProduct.setSize(FloatingActionButton.SIZE_MINI);
            actionAddNewProduct.setColorNormalResId(R.color.white);
            actionAddNewProduct.setColorPressedResId(R.color.offwhite);
            actionAddNewProduct.setIcon(R.drawable.ic_add_grey600_48dp);
            actionAddNewProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuMultipleActions.collapse();
                    addNewProduct();
                }
            });
            menuMultipleActions.addButton(actionAddNewProduct);

            //2. add products from warehouse
            final FloatingActionButton actionAddFromWarehouse = new FloatingActionButton(mContext);
            actionAddFromWarehouse.setTitle("✓/✗ from Ware House");
            actionAddFromWarehouse.setSize(FloatingActionButton.SIZE_MINI);
            actionAddFromWarehouse.setColorNormalResId(R.color.white);
            actionAddFromWarehouse.setColorPressedResId(R.color.offwhite);
            actionAddFromWarehouse.setIcon(R.drawable.ic_store_grey600_24dp);
            actionAddFromWarehouse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuMultipleActions.collapse();
                    Intent intent = new Intent(Constants.ACTION_SWITCH_TO_WAREHOUSE);
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                    broadcastManager.sendBroadcast(intent);
                }
            });
            menuMultipleActions.addButton(actionAddFromWarehouse);

        } else {
            //change the menu multiple actions
            //1. add new product fab
            final FloatingActionButton actionAddNewProduct = new FloatingActionButton(mContext);
            actionAddNewProduct.setTitle("Add new product");
            actionAddNewProduct.setSize(FloatingActionButton.SIZE_MINI);
            actionAddNewProduct.setColorNormalResId(R.color.white);
            actionAddNewProduct.setColorPressedResId(R.color.offwhite);
            actionAddNewProduct.setIcon(R.drawable.ic_add_grey600_48dp);
            actionAddNewProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuMultipleActions.collapse();
                    addNewProduct();
                }
            });
            menuMultipleActions.addButton(actionAddNewProduct);

            //2. back to my shop
            final FloatingActionButton actionSwitchBackToMyShop = new FloatingActionButton(mContext);
            actionSwitchBackToMyShop.setTitle("Back to My Shop");
            actionSwitchBackToMyShop.setSize(FloatingActionButton.SIZE_MINI);
            actionSwitchBackToMyShop.setColorNormalResId(R.color.white);
            actionSwitchBackToMyShop.setColorPressedResId(R.color.offwhite);
            actionSwitchBackToMyShop.setIcon(R.drawable.ic_home_grey600_24dp);
            actionSwitchBackToMyShop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuMultipleActions.collapse();
                    Intent intent = new Intent(Constants.ACTION_SWITCH_BACK_TO_MY_SHOP);
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                    broadcastManager.sendBroadcast(intent);
                }
            });
            menuMultipleActions.addButton(actionSwitchBackToMyShop);
        }
    }

    private void addNewProduct() {
        Intent intentAddProduct = new Intent(mContext, ProductActivity.class);
        EditProduct product = new EditProduct();
        EditProductVar emptyVar = new EditProductVar();
        List<EditProductVar> varList = new ArrayList<>();
        varList.add(emptyVar);
        product.setEditProductVars(varList);
        Parcelable productParcel = Parcels.wrap(product);
        intentAddProduct.putExtra("product", productParcel);
        startActivity(intentAddProduct);
    }

    public boolean isBackAllowed() {
        if(menuMultipleActions!=null && menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapse();
            return false;
        }
        return true;
    }

}
