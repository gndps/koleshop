package com.koleshop.appkoleshop.ui.common.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;

public class SingleSellerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MY_INVENTORY = "myInventory";
    private static final String ARG_CUSTOMER_VIEW = "customerView";
    private static final String ARG_SELLER_ID = "sellerId";

    // TODO: Rename and change types of parameters
    private boolean myInventory;
    private boolean customerView;
    private Long sellerId;

    public SingleSellerFragment() {
        // Required empty public constructor
    }

    public static SingleSellerFragment newInstance(boolean myInventory, boolean customerView, Long sellerId) {
        SingleSellerFragment fragment = new SingleSellerFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_CUSTOMER_VIEW, customerView);
        args.putBoolean(ARG_MY_INVENTORY, myInventory);
        args.putLong(ARG_SELLER_ID, sellerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            myInventory = getArguments().getBoolean(ARG_MY_INVENTORY);
            customerView = getArguments().getBoolean(ARG_CUSTOMER_VIEW);
            sellerId = getArguments().getLong(ARG_SELLER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_seller, container, false);
    }

}
