package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.ProductSelectionRequest;
import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.ui.seller.activities.InventoryProductActivity;
import com.koleshop.appkoleshop.ui.seller.fragments.product.InventoryCategoryFragment;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NearbyShopsFragment extends Fragment {

    BroadcastReceiver mBroadcastReceiver;
    Context mContext;


    public NearbyShopsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby_shops, container, false);
        mContext = getActivity();
        initializeBroadcastReceivers();
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter("update_nearby_shops"));
    }

    @Override
    public void onPause() {
        super.onPause();
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


}
