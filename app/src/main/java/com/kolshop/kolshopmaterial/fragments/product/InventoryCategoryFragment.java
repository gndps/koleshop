package com.kolshop.kolshopmaterial.fragments.product;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.activities.InventoryProductActivity;
import com.kolshop.kolshopmaterial.adapters.InventoryCategoryAdapter;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.SerializationUtil;
import com.kolshop.kolshopmaterial.extensions.KolClickListener;
import com.kolshop.kolshopmaterial.extensions.KolRecyclerTouchListener;
import com.kolshop.kolshopmaterial.model.genericjson.GenericJsonListInventoryCategory;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.kolshopmaterial.singletons.KoleshopSingleton;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.lang.reflect.Type;
import java.util.List;

public class InventoryCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryCategoryAdapter inventoryCategoryAdapter;
    ViewFlipper viewFlipper;
    Context mContext;
    BroadcastReceiver mBroadcastReceiverInventoryCategoryFragment;
    private static final String TAG = "InventoryCategoryFrag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        initializeBroadcastReceivers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_inventory_category, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.rv_inventory_category);
        viewFlipper = (ViewFlipper) layout.findViewById(R.id.viewflipper_inventory_category);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .margin(getResources().getDimensionPixelSize(R.dimen.recycler_view_left_margin),
                        getResources().getDimensionPixelSize(R.dimen.recycler_view_right_margin))
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
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiverInventoryCategoryFragment = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS)) {
                    inventoryLoadSuccess(null);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED)) {
                    inventoryLoadFailed();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

    private void inventoryLoadRequest() {
        viewFlipper.setDisplayedChild(0);
        Intent commonIntent = new Intent(getActivity(), CommonIntentService.class);
        commonIntent.setAction(Constants.ACTION_FETCH_INVENTORY_CATEGORIES);
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
            Toast.makeText(mContext, "some problem while parsing gson cached response", Toast.LENGTH_SHORT).show();
        }
    }

    private List<InventoryCategory> getCachedInventoryCategories() {
        List<InventoryCategory> listOfInventoryCategories = null;
        byte[] cachedGenericJsonByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(Constants.CACHE_INVENTORY_CATEGORIES, Constants.TIME_TO_LIVE_INV_CAT);
        if(cachedGenericJsonByteArray==null) {
            return null;
        }
        try {
            GenericJsonListInventoryCategory listCategory = SerializationUtil.getGenericJsonFromSerializable(cachedGenericJsonByteArray, GenericJsonListInventoryCategory.class);
            if(listCategory!=null) {
                listOfInventoryCategories = listCategory.getList();
            }
        } catch (Exception e) {
            Log.e(TAG, "some problem while deserlzng", e);
            return null;
        }
        return listOfInventoryCategories;
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
