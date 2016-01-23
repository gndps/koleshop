package com.koleshop.appkoleshop.ui.seller.fragments.orders;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CompleteOrdersFragment extends Fragment {

    @Bind(R.id.view_flipper_fragment_complete_orders)
    ViewFlipper viewFlipper;
    @Bind(R.id.rv_fragment_complete_orders)
    RecyclerView recyclerView;
    @Bind(R.id.button_retry_complete_orders)
    Button buttonRetry;

    public CompleteOrdersFragment() {
        // Required empty public constructor
    }

    public static CompleteOrdersFragment newInstance() {
        CompleteOrdersFragment fragment = new CompleteOrdersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete_orders, container, false);
        ButterKnife.bind(this, view);
        initializeFragment();
        return view;
    }

    private void initializeFragment() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewFlipper.setDisplayedChild(1);
            }
        }, 2000);
    }
}
