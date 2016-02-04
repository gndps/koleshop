package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.ui.buyer.adapters.NearbyShopsListAdapter;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NearbyShopsListFragment extends Fragment {

    @Bind(R.id.rv_fns_list)
    RecyclerView recyclerView;
    @Bind(R.id.vf_fnsl)
    ViewFlipper viewFlipper;

    private static int VIEW_RECYCLER_VIEW = 0;
    private static int VIEW_ERROR_SCREEN = 1;

    private static String TAG = "fns_list";

    NearbyShopsListAdapter adapter;
    List<SellerInfo> sellers;
    Context mContext;



    public NearbyShopsListFragment() {
        // Required empty public constructor
    }

    public static NearbyShopsListFragment newInstance(List<SellerInfo> sellers) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("sellers", Parcels.wrap(sellers));
        NearbyShopsListFragment nearbyShopsListFragment = new NearbyShopsListFragment();
        nearbyShopsListFragment.setArguments(bundle);
        return nearbyShopsListFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby_shops_list, container, false);
        try {
            sellers = Parcels.unwrap(getArguments().getParcelable("sellers"));
        } catch (Exception e) {
            //some problem while accepting parcel
            Log.d(TAG,"problem in accepting sellers parcel", e);
        }
        ButterKnife.bind(this, view);
        mContext = getActivity();
        initializeFragment();
        return view;
    }

    private void initializeFragment() {
        if(sellers!=null) {
            viewFlipper.setDisplayedChild(VIEW_RECYCLER_VIEW);

            //rv layout manager
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(mLayoutManager);

            //set adapter
            adapter = new NearbyShopsListAdapter(sellers, mContext);
            recyclerView.setAdapter(adapter);
        } else {
            viewFlipper.setDisplayedChild(VIEW_ERROR_SCREEN);
        }
    }

}
