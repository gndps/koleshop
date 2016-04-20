package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.ui.buyer.adapters.AddressRvAdapter;
import com.koleshop.appkoleshop.ui.common.activities.MapsActivity;
import com.koleshop.appkoleshop.ui.common.recyclerview.FlingRecyclerView;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddressesFragment extends Fragment implements AddressRvAdapter.AddressesRvAdapterListener {

    @Bind(R.id.rv_addresses)
    FlingRecyclerView recyclerView;
    @Bind(R.id.vf_addresses)
    ViewFlipper viewFlipper;
    @Bind(R.id.fab_fragment_addresses)
    FloatingActionButton fab;

    AddressRvAdapter adapter;
    boolean horizontalScroll;
    List<BuyerAddress> addresses;
    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    boolean showOnlyDefaultAddress;

    private final int REQUEST_CODE_DIALOG_FRAGMENT = 1;
    private final int REQUEST_CODE_ADD_NEW_ADDRESS = 2;


    private static int VIEW_FLIPPER_ADDRESSES = 0;
    private static int VIEW_FLIPPER_LOADING = 1;
    private static int VIEW_FLIPPER_NO_ADDRESS_ADDED = 2;
    private boolean activateMapsOnAdapterCreated;

    public AddressesFragment() {
        // Required empty public constructor
    }

    public static AddressesFragment newInstance(boolean horizontalScroll, boolean showOnlyDefaultAddress) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("horizontalScroll", horizontalScroll);
        bundle.putBoolean("showOnlyDefaultAddress", showOnlyDefaultAddress);
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
            showOnlyDefaultAddress = bundle.getBoolean("showOnlyDefaultAddress", false);
            showFabButton(false);
        }
        initializeBroadcastReceivers();
        loadAddresses();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLocationForNewAddress();
            }
        });
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
        if (adapter != null) {
            adapter.setActivateMaps(false);
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
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
                    BuyerAddress address = Parcels.unwrap(intent.getExtras().getParcelable("address"));
                    AddressDialogFragment frag = AddressDialogFragment.create(address);
                    frag.setTargetFragment(AddressesFragment.this, 1);
                    frag.show(getFragmentManager(), "AddressDialogFrag");
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_DELETE_ADDRESS)) {
                    BuyerAddress address = Parcels.unwrap(intent.getExtras().getParcelable("address"));
                    int position = 0;
                    for (BuyerAddress add : addresses) {
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

    private void selectLocationForNewAddress() {
        Intent mapsActivityIntent = new Intent(mContext, MapsActivity.class);
        mapsActivityIntent.putExtra("twoButtonMode", false);
        mapsActivityIntent.putExtra("title", getString(R.string.title_set_delivery_location));
        mapsActivityIntent.putExtra("markerTitle", "Delivery location");
        mapsActivityIntent.putExtra("actionButtonTitle", getString(R.string.title_add_new_address));
        startActivityForResult(mapsActivityIntent, REQUEST_CODE_ADD_NEW_ADDRESS);
    }

    private void loadAddresses() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_LOADING);
        showFabButton(false);
        loadAddressesFromRealm();
        /*if(PreferenceUtils.getUserId(mContext)>0l) {
            //user logged in...get addresses from internet


        } else {
            //get default address from prefs
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addresses = getDummyAddresses();
                initializeRvAndAdapter();
            }
        }, 500);*/
    }

    private void loadAddressesFromRealm() {
        if(showOnlyDefaultAddress) {
            BuyerAddress buyerDefaultAddress = RealmUtils.getDefaultUserAddress();
            addresses = new ArrayList<>();
            addresses.add(buyerDefaultAddress);
        } else {
            addresses = RealmUtils.getBuyerAddresses();
        }
        if (addresses != null && addresses.size() > 0) {
            initializeRvAndAdapter();
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_ADDRESS_ADDED);
            showFabButton(true);
        }
    }

    private void initializeRvAndAdapter() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_ADDRESSES);
        showFabButton(true);
        adapter = new AddressRvAdapter(mContext, addresses, this, showOnlyDefaultAddress);
        if (activateMapsOnAdapterCreated) {
            adapter.setActivateMaps(true);
        }
        boolean enableSnappyFling;
        RecyclerView.LayoutManager lm;
        if (horizontalScroll) {
            lm = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            enableSnappyFling = true;
        } else {
            lm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            enableSnappyFling = false;
        }
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
        recyclerView.setSnappyFling(enableSnappyFling);
    }

    /*private List<Address> getDummyAddresses() {
        List<Address> addressesDummyList = new ArrayList<>();
        Long userId = PreferenceUtils.getUserId(mContext);
        addressesDummyList.add(new Address(1l, userId, "Anmol Singh Brar", "#20014 St. no. 14\nJujhar Singh Nagar", 0, 98372734731l, 91, "anmol", 76d, 32d));
        addressesDummyList.add(new Address(2l, userId, "Gundeep Singh", "#21497 St. no. 6/4\nPower House Road", 0, 8585945716l, 91, "gndp", 76d, 31d));
        addressesDummyList.add(new Address(2l, userId, "Gundeep Singh", "#21497 St. no. 6/4\nPower House Road", 0, 8585945716l, 91, "gndp", 76d, 31d));
        return addressesDummyList;
    }*/

    private void showFabButton(boolean show) {
        fab.setVisibility(show && !horizontalScroll ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DIALOG_FRAGMENT:

                if (resultCode == Activity.RESULT_OK) {
                    Double gpsLong = data.getDoubleExtra("gpsLong", 0l);
                    Double gpsLat = data.getDoubleExtra("gpsLat", 0l);
                    for (BuyerAddress address : addresses) {
                        if (address.getGpsLat().equals(gpsLat) && address.getGpsLong().equals(gpsLong)) {
                            address.setNickname(data.getStringExtra("nickname"));
                            address.setName(data.getStringExtra("name"));
                            String addressString = data.getStringExtra("addressString");
                            address.setAddress(addressString);
                            if (data.getLongExtra("phoneNumber", 0l) > 0) {
                                address.setPhoneNumber(data.getLongExtra("phoneNumber", 0l) > 0 ? data.getLongExtra("phoneNumber", 0l) : null);
                            }
                            RealmUtils.updateBuyerAddress(address);
                            break;
                        }
                    }
                    refreshAddresses();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                }

                break;


            case REQUEST_CODE_ADD_NEW_ADDRESS:
                if (resultCode == Activity.RESULT_OK) {
                    Double gpsLong = data.getDoubleExtra("gpsLong", 0l);
                    Double gpsLat = data.getDoubleExtra("gpsLat", 0l);
                    if(gpsLat>0 && gpsLong>0) {
                        RealmUtils.createBuyerAddress(gpsLong, gpsLat);
                        loadAddressesFromRealm();
                    } else {
                        Toast.makeText(mContext, "Some problem occurred in creating address", Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //do nothing
                }
        }
    }

    @Override
    public void refreshAddresses() {
        activateMapsOnAdapterCreated = true;
        loadAddressesFromRealm();
    }

    public BuyerAddress getSelectedAddress() {
        if(addresses!=null && addresses.size()>0) {
            for(BuyerAddress adrs : addresses) {
                if(adrs.isDefaultAddress()) {
                    return adrs;
                }
            }
        }
        return null;
    }
}
