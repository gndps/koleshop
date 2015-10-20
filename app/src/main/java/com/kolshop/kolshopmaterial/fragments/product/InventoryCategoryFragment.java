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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.activities.VerifyOTPActivity;
import com.kolshop.kolshopmaterial.adapters.InventoryCategoryAdapter;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.PreferenceUtils;
import com.kolshop.kolshopmaterial.extensions.KolClickListener;
import com.kolshop.kolshopmaterial.extensions.KolRecyclerTouchListener;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.kolshopmaterial.singletons.KolShopSingleton;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public class InventoryCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryCategoryAdapter inventoryCategoryAdapter;
    ViewFlipper viewFlipper;
    Context mContext;
    BroadcastReceiver mBroadcastReceiverInventoryCategoryFragment;

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
                final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.exit_to_right, R.anim.enter_from_left);
                ft.replace(R.id.fragment_container, new InventoryProductFragment(), "InventoryProductFragment");
                ft.addToBackStack(null);
                ft.commit();
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
                    inventoryCategoryAdapter.setNewDataOnAdapter(KolShopSingleton.getSharedInstance().getInventoryCategories());
                    viewFlipper.setDisplayedChild(1);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_CATEGORIES_FAILED)) {
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
        if(KolShopSingleton.getSharedInstance().getInventoryCategories()!=null) {
            inventoryCategoryAdapter.setNewDataOnAdapter(KolShopSingleton.getSharedInstance().getInventoryCategories());
            viewFlipper.setDisplayedChild(1);
        } else {
            viewFlipper.setDisplayedChild(0);
            Intent commonIntent = new Intent(getActivity(), CommonIntentService.class);
            commonIntent.setAction(Constants.ACTION_FETCH_INVENTORY_CATEGORIES);
            getActivity().startService(commonIntent);
        }
    }

}
