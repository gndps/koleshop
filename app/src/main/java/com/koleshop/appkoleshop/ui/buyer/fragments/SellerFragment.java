package com.koleshop.appkoleshop.ui.buyer.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellerFragment extends Fragment {

    Context mContext;

    public SellerFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_seller_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                Toast.makeText(mContext, "search", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_item_cart:
                Toast.makeText(mContext, "cart", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_item_call:
                Toast.makeText(mContext, "call", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_item_star:
                Toast.makeText(mContext, "favorite", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller, container, false);
    }

}
