package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.ui.buyer.adapters.NearbyShopsFragmentPagerAdapter;
import com.koleshop.appkoleshop.ui.common.interfaces.FragmentHomeActivityListener;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.mypopsy.widget.FloatingSearchView;

import java.util.ArrayList;
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

    BroadcastReceiver mBroadcastReceiver;
    Context mContext;
    FragmentHomeActivityListener fragmentHomeActivityListener;
    List<SellerInfo> sellers;


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
        loadNearbySellersList();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_cart:
                //open cart fragment
                Toast.makeText(mContext, "open hte cart", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter("update_nearby_shops"));
        fragmentHomeActivityListener.setBackButtonHandledByFragment(false);
        fragmentHomeActivityListener.setTitle(titleNearbyShops);
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
                if (intent.getAction().equalsIgnoreCase("update_nearby_shops")) {
                    //updateSellerInfoTile();
                }
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initializeViewPagerAndTabLayout(List<SellerInfo> sellers) {
        fragmentHomeActivityListener.setElevation(0);
        viewFlipper.setDisplayedChild(0);//viewpager and tablayout
        viewPager.setAdapter(new NearbyShopsFragmentPagerAdapter(getChildFragmentManager(), sellers));
        tabLayout.setupWithViewPager(viewPager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tabLayout.setElevation(8);
        }
    }

    private Drawable getDrawable(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resId, mContext.getTheme());
        } else {
            return getResources().getDrawable(resId);
        }
    }

    private SellerInfo getSellerInfo(Context context) {
        String nearbyShopsSettings = PreferenceUtils.getPreferences(context, "nearby_shops");
        SellerInfo sellerInfo = new Gson().fromJson(nearbyShopsSettings, SellerInfo.class);
        return sellerInfo;
    }

    private void loadNearbySellersList() {
        viewFlipper.setDisplayedChild(1);//loading
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sellers = getSellersDummyData();
                initializeViewPagerAndTabLayout(sellers);
            }
        }, 1000);
    }

    private List<SellerInfo> getSellersDummyData() {
        List<SellerInfo> sellers = new ArrayList<>();
        sellers.add(new SellerInfo("Jagdish General Store", "Delivery 7 am - 9 pm", true, "", 76.0d, 32.0d));
        sellers.add(new SellerInfo("Gandhi New Store", "Delivery 7:30 am - 8:30 pm", true, "", 77.0d, 33.0d));
        sellers.add(new SellerInfo("Funky store", "Delivery 7 am - 9:30 pm", true, "", 76.0d, 34.0d));
        sellers.add(new SellerInfo("Some Store", "Delivery 6:30 am - 10 pm", false, "", 76.0d, 33.0d));
        sellers.add(new SellerInfo("Cool Shop", "Delivery 10 am - 7 pm", true, "", 75.0d, 31.0d));
        sellers.add(new SellerInfo("My Daily Needs", "Delivery 9 am - 7 pm", true, "", 75.0d, 32.0d));
        return sellers;
    }

    public void openSeller(int position) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new SellerFragment(), "sellerFragment");
        ft.addToBackStack(null);
        fragmentHomeActivityListener.setBackButtonHandledByFragment(true);
        fragmentHomeActivityListener.setTitle(sellers.get(position).getName());
        fragmentHomeActivityListener.setElevation(8);
        ft.commit();
    }


}
