package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.ui.buyer.adapters.MultiSellerSearchAdapter;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MultiSellerSearchFragment extends Fragment {

    @Bind(R.id.rv_multi_seller_search)
    RecyclerView recyclerView;

    MultiSellerSearchAdapter adapter;

    private static final String ARG_SEARCH_RESULTS = "search_results";

    private List<SellerSearchResults> results;

    public MultiSellerSearchFragment() {
        // Required empty public constructor
    }

    public static MultiSellerSearchFragment newInstance(List<SellerSearchResults> searchResults) {
        MultiSellerSearchFragment fragment = new MultiSellerSearchFragment();
        Bundle args = new Bundle();
        Parcelable results = Parcels.wrap(searchResults);
        args.putParcelable(ARG_SEARCH_RESULTS, results);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            results = Parcels.unwrap(getArguments().getParcelable(ARG_SEARCH_RESULTS));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_seller_search, container, false);
        ButterKnife.bind(this, view);
        adapter = new MultiSellerSearchAdapter(results);
        return view;
    }
}
