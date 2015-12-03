package com.koleshop.appkoleshop.fragments.product;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.activities.InventoryProductActivity;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.KoleCacheUtil;
import com.koleshop.appkoleshop.extensions.KolClickListener;
import com.koleshop.appkoleshop.extensions.KolRecyclerTouchListener;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.koleshop.appkoleshop.adapters.InventoryCategoryAdapter;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

public class InventoryCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryCategoryAdapter inventoryCategoryAdapter;
    ViewFlipper viewFlipper;
    Context mContext;
    BroadcastReceiver mBroadcastReceiverInventoryCategoryFragment;
    private static final String TAG = "InventoryCategoryFrag";
    private boolean myInventory = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Bundle args = getArguments();
        if(args!=null) {
            myInventory = args.getBoolean("myInventory", false);
        }
        initializeBroadcastReceivers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(com.koleshop.appkoleshop.R.layout.fragment_inventory_category, container, false);
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

            @Override
            public void onItemLongClick(View v, int position) {
                Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_LONG).show();

            }
        }));
        loadInventoryCategories();
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiverInventoryCategoryFragment, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiverInventoryCategoryFragment, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED));
        lbm.registerReceiver(mBroadcastReceiverInventoryCategoryFragment, new IntentFilter(Constants.ACTION_GCM_BROADCAST_INVENTORY_CREATED));
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiverInventoryCategoryFragment = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS)) {
                    inventoryLoadSuccess(null);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED)) {
                    if(intent.getExtras()!=null && intent.getStringExtra("status")!=null) {
                        intent.getStringExtra("status").equalsIgnoreCase(Constants.STATUS_KOLE_RESPONSE_CREATING_INVENTORY);
                        //wait for push broadcast of inventory created
                        //do nothing -- because processing is already showing...
                    } else {
                        inventoryLoadFailed();
                    }
                } else if(intent.getAction().equalsIgnoreCase(Constants.ACTION_GCM_BROADCAST_INVENTORY_CREATED)) {
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
        List<InventoryCategory> cachedCategories = getCachedInventoryCategories();
        if (cachedCategories != null) {
            inventoryLoadSuccess(cachedCategories);
        } else {
            inventoryLoadRequest();
        }
    }

    public List<InventoryCategory> getCachedInventoryCategories() {
        return KoleCacheUtil.getCachedInventoryCategories(myInventory);
    }

    public void inventoryLoadRequest() {
        viewFlipper.setDisplayedChild(0);
        Intent commonIntent = new Intent(getActivity(), CommonIntentService.class);
        commonIntent.setAction(Constants.ACTION_FETCH_INVENTORY_CATEGORIES);
        commonIntent.putExtra("myInventory", myInventory);
        getActivity().startService(commonIntent);
    }

    private void inventoryLoadSuccess(List<InventoryCategory> categories) {
        List<InventoryCategory> listOfInventoryCategories;
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
        new AlertDialog.Builder(mContext)
                .setTitle("Problem in loading inventory")
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
