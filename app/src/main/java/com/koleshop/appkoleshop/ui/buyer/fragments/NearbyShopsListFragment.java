package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.ui.buyer.adapters.NearbyShopsListAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyShopsListFragment extends Fragment {

    @Bind(R.id.rv_fns_list)
    RecyclerView recyclerView;
    @Bind(R.id.vf_fnsl)
    ViewFlipper viewFlipper;

    private static int VIEW_RECYCLER_VIEW = 0;
    private static int VIEW_ERROR_SCREEN = 1;

    NearbyShopsListAdapter adapter;
    List<SellerInfo> sellers;
    Context mContext;



    public NearbyShopsListFragment() {
        // Required empty public constructor
    }

    public static NearbyShopsListFragment newInstance(List<SellerInfo> sellers) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby_shops_list, container, false);
        ButterKnife.bind(this, view);
        mContext = getActivity();
        initializeFragment();
        return view;
    }

    private void initializeFragment() {
        if(sellers!=null) {
            adapter = new NearbyShopsListAdapter(sellers, mContext);
            recyclerView = new RecyclerView(mContext);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        } else {
            viewFlipper.setDisplayedChild(VIEW_ERROR_SCREEN);
        }
    }

}
