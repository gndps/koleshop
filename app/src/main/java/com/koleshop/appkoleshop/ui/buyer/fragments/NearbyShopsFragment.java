package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.services.BuyerIntentService;
import com.koleshop.appkoleshop.ui.buyer.activities.ShopActivity;
import com.koleshop.appkoleshop.ui.buyer.adapters.NearbyShopsFragmentPagerAdapter;
import com.koleshop.appkoleshop.ui.common.interfaces.FragmentHomeActivityListener;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class NearbyShopsFragment extends Fragment {

    @Bind(R.id.tab_layout_fns)
    TabLayout tabLayout;
    @Bind(R.id.view_pager_fns)
    ViewPager viewPager;
    @Bind(R.id.vf_fns)
    ViewFlipper viewFlipper;
    @BindString(R.string.navigation_drawer_nearby_shops)
    String titleNearbyShops;
    @Bind(R.id.button_refresh_nearby_shops)
    Button buttonRefresh;

    boolean onlyHomeDeliveryShops;
    boolean onlyOnlineShops;
    int loadedShopsCount;
    NearbyShopsFragmentPagerAdapter adapter;

    private static final int VIEW_FLIPPER_TABS = 0;
    private static final int VIEW_FLIPPER_PROCESSING = 1;
    private static final int VIEW_FLIPPER_NO_SHOPS = 2;
    private static final int LOAD_MORE_SHOPS_COUNT = 20;

    BroadcastReceiver mBroadcastReceiver;
    Context mContext;
    FragmentHomeActivityListener fragmentHomeActivityListener;
    List<SellerSettings> sellers;


    public NearbyShopsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nearby_shops, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby_shops, container, false);
        mContext = getActivity();
        fragmentHomeActivityListener = (FragmentHomeActivityListener) getActivity();
        ButterKnife.bind(this, view);
        initializeBroadcastReceivers();
        initializeSomeStuffHere();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_cart:
                //open cart fragment
                Toast.makeText(mContext, "open hte cart", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_item_only_home_delivery:
                item.setChecked(!item.isChecked());
                onlyHomeDeliveryShops = item.isChecked();
                requestNearbyShopsFromInternet();
                return true;
            case R.id.menu_item_only_online_shops:
                item.setChecked(!item.isChecked());
                onlyOnlineShops = item.isChecked();
                requestNearbyShopsFromInternet();
                return true;
        }
        return false;

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NEARBY_SHOPS_RECEIVE_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NEARBY_SHOPS_RECEIVE_FAILED));
        fragmentHomeActivityListener.setBackButtonHandledByFragment(false);
        fragmentHomeActivityListener.setTitle(titleNearbyShops);
        requestNearbyShopsFromInternet();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_NEARBY_SHOPS_RECEIVE_SUCCESS)) {
                    int offset = intent.getIntExtra("offset", 0);
                    Parcelable parcelableSettings = intent.getParcelableExtra("nearbyShopsList");
                    List<SellerSettings> sellers = Parcels.unwrap(parcelableSettings);
                    if (offset == 0) {
                        if (sellers != null && sellers.size() > 0) {
                            loadNearbyShopsList(sellers);
                        } else {
                            //no sellers found at this location
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_SHOPS);
                        }
                    } else {
                        if (sellers != null && sellers.size() > 0) {
                            moreSellersLoaded(sellers);
                        } else {
                            couldNotLoadMoreSellers();
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_NEARBY_SHOPS_RECEIVE_FAILED)) {
                    int offset = intent.getIntExtra("offset", 0);
                    if (offset == 0) {
                        //sellers loading failed
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_SHOPS);
                    } else {
                        couldNotLoadMoreSellers();
                    }
                }
            }
        };
    }

    private void initializeSomeStuffHere() {
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNearbyShopsFromInternet();
            }
        });
    }

    private void loadNearbyShopsList(List<SellerSettings> sellers) {
        fragmentHomeActivityListener.setElevation(0);
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_TABS);//viewpager and tablayout
        this.sellers = sellers;
        adapter = new NearbyShopsFragmentPagerAdapter(getChildFragmentManager(), sellers);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tabLayout.setElevation(8);
        }
    }

    private void moreSellersLoaded(List<SellerSettings> moreSellers) {
        sellers.addAll(moreSellers);
        adapter.moreSellersFetched(moreSellers);
    }

    private void couldNotLoadMoreSellers() {
        adapter.couldNotLoadMoreSellers();
    }

    private void requestNearbyShopsFromInternet() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_PROCESSING);//loading
        BuyerIntentService.getNearbyShops(mContext, onlyHomeDeliveryShops, onlyOnlineShops, LOAD_MORE_SHOPS_COUNT, 0);
    }

    public void requestMoreNearbyShopsFromInternet() {
        //BuyerIntentService.getNearbyShops(mContext, onlyHomeDeliveryShops, onlyOnlineShops, LOAD_MORE_SHOPS_COUNT, sellers.size());
    }


    public void openSeller(SellerSettings selectedSeller) {
        Intent sellerIntent = new Intent(mContext, ShopActivity.class);
        Parcelable parcelableSettings = Parcels.wrap(selectedSeller);
        sellerIntent.putExtra("sellerSettings", parcelableSettings);
        startActivity(sellerIntent);
        //final FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(R.id.fragment_container, new SellerFragment(), "sellerFragment");
        //ft.addToBackStack(null);
        //fragmentHomeActivityListener.setBackButtonHandledByFragment(true);
        //fragmentHomeActivityListener.setTitle(selectedSeller.getAddress().getName());
        //fragmentHomeActivityListener.setElevation(8);
        //ft.commit();
    }


}
