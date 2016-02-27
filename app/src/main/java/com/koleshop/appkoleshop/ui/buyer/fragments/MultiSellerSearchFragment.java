package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.services.SearchIntentService;
import com.koleshop.appkoleshop.ui.buyer.adapters.MultiSellerSearchAdapter;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MultiSellerSearchFragment extends Fragment {

    @Bind(R.id.rv_multi_seller_search)
    RecyclerView recyclerView;
    @Bind(R.id.vf_fmss)
    ViewFlipper viewFlipper;

    MultiSellerSearchAdapter adapter;

    private static final String ARG_SEARCH_RESULTS = "searchResults";
    private static final String ARG_SEARCH_QUERY = "searchQuery";
    private static final int VIEW_FLIPPER_CHILD_LOADING = 0;
    private static final int VIEW_FLIPPER_CHILD_RECYCLER_VIEW = 1;
    private static final int VIEW_FLIPPER_CHILD_INTERNET_PROBLEM = 2;
    private static final int VIEW_FLIPPER_CHILD_NO_RESULTS = 3;

    private Context mContext;
    private List<SellerSearchResults> results;
    private String searchQuery;
    private BroadcastReceiver mBroadcastReceiver;

    public MultiSellerSearchFragment() {
        // Required empty public constructor
    }

    public static MultiSellerSearchFragment newQueryInstance(String queryString) {
        MultiSellerSearchFragment fragment = new MultiSellerSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, queryString);
        fragment.setArguments(args);
        return fragment;
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
        mContext = getActivity();
        if (getArguments() != null) {
            Parcelable resultsParcelable = getArguments().getParcelable(ARG_SEARCH_RESULTS);
            if(resultsParcelable!=null) {
                results = Parcels.unwrap(resultsParcelable);
            }
        }
        initializeBroadcastReceivers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_seller_search, container, false);
        ButterKnife.bind(this, view);
        if(results!=null) {
            adapter = new MultiSellerSearchAdapter(mContext, results);
        } else {
            searchQuery = getArguments().getString(ARG_SEARCH_QUERY);
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            getSearchResultsFromInternet();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_EMPTY));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS.equalsIgnoreCase(action)) {
                    results = Parcels.unwrap(intent.getExtras().getParcelable(ARG_SEARCH_RESULTS));
                    loadTheResultsIntoUi();
                } else if(Constants.ACTION_SEARCH_RESULTS_EMPTY.equalsIgnoreCase(action)) {
                    viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_INTERNET_PROBLEM);
                } else if(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED.equalsIgnoreCase(action)) {
                    viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_RESULTS);
                }
            }
        };
    }

    private void loadTheResultsIntoUi() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        adapter = new MultiSellerSearchAdapter(mContext, results);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void getSearchResultsFromInternet() {
        SearchIntentService.getMultiSellerResults(mContext, searchQuery, false, false, 10, 0);
    }
}
