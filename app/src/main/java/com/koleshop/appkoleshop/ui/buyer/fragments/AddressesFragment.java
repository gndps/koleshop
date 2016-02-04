package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.ui.buyer.adapters.AddressRvAdapter;
import com.koleshop.appkoleshop.ui.common.recyclerview.FlingRecyclerView;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddressesFragment extends Fragment {

    @Bind(R.id.rv_addresses)
    FlingRecyclerView recyclerView;
    @Bind(R.id.vf_addresses)
    ViewFlipper viewFlipper;

    AddressRvAdapter adapter;
    boolean horizontalScroll;
    List<Address> addresses;
    Context mContext;
    Long selectedAddressId;
    BroadcastReceiver mBroadcastReceiver;

    private final int DIALOG_FRAGMENT_REQUEST_CODE = 1;


    private static int VIEW_FLIPPER_ADDRESSES = 0;
    private static int VIEW_FLIPPER_LOADING = 1;
    private boolean activateMapsOnAdapterCreated;

    public AddressesFragment() {
        // Required empty public constructor
    }

    public static AddressesFragment newInstance(boolean horizontalScroll, Long selectedAddressId) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("horizontalScroll", horizontalScroll);
        bundle.putLong("selectedAddressId", selectedAddressId);
        AddressesFragment addressesFragment = new AddressesFragment();
        addressesFragment.setArguments(bundle);
        return addressesFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_addresses, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();
        Bundle bundle = getArguments();
        if (bundle != null) {
            horizontalScroll = bundle.getBoolean("horizontalScroll", false);
            selectedAddressId = bundle.getLong("selectedAddressId", 0l);
        }
        initializeBroadcastReceivers();
        loadAddresses();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_EDIT_ADDRESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_DELETE_ADDRESS));
        if (adapter != null) {
            adapter.setActivateMaps(true);
        } else {
            activateMapsOnAdapterCreated = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.setActivateMaps(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_EDIT_ADDRESS)) {
                    Address address = Parcels.unwrap(intent.getExtras().getParcelable("address"));
                    AddressDialogFragment frag = AddressDialogFragment.create(address);
                    frag.setTargetFragment(AddressesFragment.this, 1);
                    frag.show(getFragmentManager(), "AddressDialogFrag");
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_DELETE_ADDRESS)) {
                    Address address = Parcels.unwrap(intent.getExtras().getParcelable("address"));
                    int position = 0;
                    for (Address add : addresses) {
                        if (add.getId().equals(address.getId())) {
                            Toast.makeText(mContext, "will delete address at position " + position, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        position++;
                    }
                }
            }
        };
    }

    private void loadAddresses() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_LOADING);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addresses = getDummyAddresses();
                initializeRvAndAdapter();
            }
        }, 500);
    }

    private void initializeRvAndAdapter() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_ADDRESSES);
        adapter = new AddressRvAdapter(mContext, addresses, selectedAddressId);
        if (activateMapsOnAdapterCreated) {
            adapter.setActivateMaps(true);
        }
        RecyclerView.LayoutManager lm;
        if (horizontalScroll) {
            lm = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        } else {
            lm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        }
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    private List<Address> getDummyAddresses() {
        List<Address> addressesDummyList = new ArrayList<>();
        Long userId = PreferenceUtils.getUserId(mContext);
        addressesDummyList.add(new Address(1l, userId, "Anmol Singh Brar", "#20014 St. no. 14\nJujhar Singh Nagar", 0, 98372734731l, 91, "anmol", 76d, 32d));
        addressesDummyList.add(new Address(2l, userId, "Gundeep Singh", "#21497 St. no. 6/4\nPower House Road", 0, 8585945716l, 91, "gndp", 76d, 31d));
        addressesDummyList.add(new Address(2l, userId, "Gundeep Singh", "#21497 St. no. 6/4\nPower House Road", 0, 8585945716l, 91, "gndp", 76d, 31d));
        return addressesDummyList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIALOG_FRAGMENT_REQUEST_CODE:

                if (resultCode == Activity.RESULT_OK) {
                    int position = 0;
                    boolean positionFound = false;
                    for (Address address : addresses) {
                        if (address.getId().equals(data.getLongExtra("id", 0l))) {
                            address.setNickname(data.getStringExtra("nickname"));
                            address.setName(data.getStringExtra("name"));
                            address.setAddress(data.getStringExtra("address"));
                            address.setPhoneNumber(data.getLongExtra("phoneNumber", 0l));
                            positionFound = true;
                            break;
                        }
                        position++;
                    }
                    if (positionFound) {
                        adapter.notifyItemChanged(position);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                }

                break;
        }
    }
}
