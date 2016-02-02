package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyShopsMapFragment extends Fragment {


    public NearbyShopsMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby_shops_map, container, false);
    }

}
