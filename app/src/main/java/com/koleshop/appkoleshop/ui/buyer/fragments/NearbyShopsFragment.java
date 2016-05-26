package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.helpers.MyMenuItemStuffListener;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.services.BuyerIntentService;
import com.koleshop.appkoleshop.ui.buyer.activities.CartActivity;
import com.koleshop.appkoleshop.ui.buyer.activities.HomeActivity;
import com.koleshop.appkoleshop.ui.buyer.activities.ShopActivity;
import com.koleshop.appkoleshop.ui.buyer.adapters.NearbyShopsFragmentPagerAdapter;
import com.koleshop.appkoleshop.ui.common.interfaces.FragmentHomeActivityListener;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.CommonUtils;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;

public class NearbyShopsFragment extends Fragment {

    @BindView(R.id.tab_layout_fns)
    TabLayout tabLayout;
    @BindView(R.id.view_pager_fns)
    ViewPager viewPager;
    @BindView(R.id.vf_fns)
    ViewFlipper viewFlipper;
    @BindString(R.string.navigation_drawer_nearby_shops)
    String titleNearbyShops;
    @BindView(R.id.button_refresh_nearby_shops)
    Button buttonRefresh;
    @BindView(R.id.button_change_gps_location)
    Button buttonChangeGpsLocation;
    @BindView(R.id.button_retry_nearby_shops)
    Button buttonRetry;

    boolean onlyHomeDeliveryShops;
    boolean onlyOnlineShops;
    int loadedShopsCount;
    NearbyShopsFragmentPagerAdapter adapter;

    private static final int VIEW_FLIPPER_TABS = 0;
    private static final int VIEW_FLIPPER_PROCESSING = 1;
    private static final int VIEW_FLIPPER_NO_SHOPS = 2;
    private static final int VIEW_FLIPPER_NO_INTERNET = 3;
    private static final int VIEW_FLIPPER_NO_ADDRESS_SELECTED = 4;

    private static final int LOAD_MORE_SHOPS_COUNT = 20;

    BroadcastReceiver mBroadcastReceiver;
    Context mContext;
    FragmentHomeActivityListener fragmentHomeActivityListener;
    List<SellerSettings> sellers;

    boolean loading;
    private TextView noOfItemsViewer=null;
    private Menu menu;


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
        this.menu = menu;
        MenuItem item1 = menu.findItem(R.id.items_in_cart);
        final View showItemsInCart = MenuItemCompat.getActionView(item1);
        noOfItemsViewer = (TextView) showItemsInCart.findViewById(R.id.no_of_items_in_cart);

        new MyMenuItemStuffListener(showItemsInCart, "Cart") {
            @Override
            public void onClick(View v) {
                Intent cartActivityIntent = new Intent(mContext, CartActivity.class);
                startActivity(cartActivityIntent);
            }
        };

        updateHotCount();

    }

    public void updateHotCount() {
        if (noOfItemsViewer == null) {
           MenuItem item1 = this.menu.findItem(R.id.items_in_cart);
            final View showItemsInCart = MenuItemCompat.getActionView(item1);
            noOfItemsViewer = (TextView) showItemsInCart.findViewById(R.id.no_of_items_in_cart);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (CartUtils.getCartsTotalCount() == 0) {
                    noOfItemsViewer.setVisibility(View.INVISIBLE);
                } else {
                    noOfItemsViewer.setVisibility(View.VISIBLE);
                    noOfItemsViewer.setText(CartUtils.getCartsTotalCount() + "");
                }
            }
        });

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
        loading = true;
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_cart:
                Intent cartActivityIntent = new Intent(mContext, CartActivity.class);
                startActivity(cartActivityIntent);
                break;
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
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NO_ADDRESS_SELECTED));
        fragmentHomeActivityListener.setBackButtonHandledByFragment(false);
        fragmentHomeActivityListener.setTitle(titleNearbyShops);
        if (loading) {
            requestNearbyShopsFromInternet();
        }
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
                    loading = false;
                    int offset = intent.getIntExtra("offset", 0);
                    Parcelable parcelableSettings = intent.getParcelableExtra("nearbyShopsList");
                    Parcelable parcelableBuyerAddress = intent.getParcelableExtra("buyerAddress");
                    List<SellerSettings> sellers = Parcels.unwrap(parcelableSettings);
                    BuyerAddress buyerAddress = Parcels.unwrap(parcelableBuyerAddress);
                    if (offset == 0) {
                        if (sellers != null && sellers.size() > 0) {
                            loadNearbyShopsList(sellers, buyerAddress);
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
                    loading = false;
                    int offset = intent.getIntExtra("offset", 0);
                    if (offset == 0) {
                        //sellers loading failed
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_SHOPS);
                    } else {
                        couldNotLoadMoreSellers();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_NO_ADDRESS_SELECTED)) {
                    loading = false;
                    viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_ADDRESS_SELECTED);
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
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNearbyShopsFromInternet();
            }
        });
        buttonChangeGpsLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGpsLocation();
            }
        });
    }

    private void loadNearbyShopsList(List<SellerSettings> sellers, BuyerAddress buyerAddress) {
        fragmentHomeActivityListener.setElevation(0);
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_TABS);//viewpager and tablayout
        this.sellers = sellers;
        adapter = new NearbyShopsFragmentPagerAdapter(getChildFragmentManager(), sellers, buyerAddress);
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
        if (!CommonUtils.isConnectedToInternet(mContext)) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_INTERNET);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_PROCESSING);//loading
            loading = true;
            BuyerIntentService.getNearbyShops(mContext, onlyHomeDeliveryShops, onlyOnlineShops, LOAD_MORE_SHOPS_COUNT, 0);
        }
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

    private void changeGpsLocation() {
        ((HomeActivity)getActivity()).showAddressesAndChangeGpsLocation();
    }


}
