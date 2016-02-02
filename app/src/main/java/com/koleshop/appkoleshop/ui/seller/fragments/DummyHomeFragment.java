package com.koleshop.appkoleshop.ui.seller.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DummyHomeFragment extends Fragment {

    boolean buyerMode;
    @Nullable @Bind(R.id.dummy_seller_switch)
    CardView cardView;
    @Nullable @Bind(R.id.tv_df_sales)
    TextView textViewSales;
    @Nullable @Bind(R.id.tv_df_no_sales)
    TextView textViewNoSales;

    public DummyHomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dummy_home, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle=getArguments();
        //here is your list array
        if(bundle!=null) {
            buyerMode = bundle.getBoolean("buyerMode");
        }
        setupView();
        return view;
    }

    private void setupView() {
        if(buyerMode) {
            if(cardView!=null) {
                cardView.setVisibility(View.GONE);
            }
            if(textViewNoSales!=null) {
                textViewNoSales.setText("No expenses data available");
            }
            if(textViewSales!=null) {
                textViewSales.setText("Expenses");
            }
        }
    }


}
