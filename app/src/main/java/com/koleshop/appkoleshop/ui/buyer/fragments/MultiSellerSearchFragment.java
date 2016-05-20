package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.listeners.SearchActivityListener;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.services.SearchIntentService;
import com.koleshop.appkoleshop.ui.buyer.activities.SearchActivity;
import com.koleshop.appkoleshop.ui.buyer.adapters.MultiSellerSearchAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;

public class MultiSellerSearchFragment extends Fragment {

    @BindView(R.id.rv_multi_seller_search)
    RecyclerView recyclerView;
    @BindView(R.id.vf_fmss)
    ViewFlipper viewFlipper;
    @BindView(R.id.root_frame_layout_fmss)
    FrameLayout rootFrameLayout;
    @BindView(R.id.tv_problem_in_multi_seller_search)
    TextView textViewProblemText;
    @BindString(R.string.some_problem_occurred)
    String problemString;
    @BindString(R.string.no_search_results_found)
    String noSearchResultsString;

    MultiSellerSearchAdapter adapter;

    private static final String ARG_SEARCH_RESULTS = "searchResults";
    private static final String ARG_SEARCH_QUERY = "searchQuery";
    private static final int VIEW_FLIPPER_CHILD_LOADING = 0;
    private static final int VIEW_FLIPPER_CHILD_RECYCLER_VIEW = 1;
    private static final int VIEW_FLIPPER_CHILD_INTERNET_PROBLEM = 2;
    private static final int VIEW_FLIPPER_CHILD_NO_RESULTS = 3;
    private static final int VIEW_FLIPPER_NO_ADDRESS_SELECTED = 4;

    private Context mContext;
    private List<SellerSearchResults> results;
    private String searchQuery;
    private BroadcastReceiver mBroadcastReceiver;
    private int cummulativeDy;

    SearchActivityListener mListener;
    private boolean searchBarHidden;
    private boolean searchBarStateKnown;
    private final String TAG = "MultiSellerSearchFrag";
    private String randomSearchId = "";

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
            if (resultsParcelable != null) {
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
        if (results != null) {
            BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
            if(buyerAddress!=null) {
                adapter = new MultiSellerSearchAdapter(mContext, results, buyerAddress);
            } else {
                viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_ADDRESS_SELECTED);
            }
        } else {
            searchQuery = getArguments().getString(ARG_SEARCH_QUERY);
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            getSearchResultsFromInternet();
        }
        setupRootCardView();
        setupScrollListenerOnRv();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_EMPTY));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_COLLAPSE_EXPANDED_PRODUCT));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_INCREASE_VARIETY_COUNT));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_DECREASE_VARIETY_COUNT));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void setupRootCardView() {
        rootFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS.equalsIgnoreCase(action)) {
                    String randomSearchIdReceived = intent.getExtras().getString("randomSearchId");
                    if (randomSearchIdReceived.equalsIgnoreCase(randomSearchId)) {
                        results = Parcels.unwrap(intent.getExtras().getParcelable(ARG_SEARCH_RESULTS));
                        loadTheResultsIntoUi();
                    }
                } else if (Constants.ACTION_SEARCH_RESULTS_EMPTY.equalsIgnoreCase(action)) {
                    String randomSearchIdReceived = intent.getExtras().getString("randomSearchId");
                    if (randomSearchIdReceived.equalsIgnoreCase(randomSearchId)) {
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_RESULTS);
                    }
                } else if (Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED.equalsIgnoreCase(action)) {
                    String randomSearchIdReceived = intent.getExtras().getString("randomSearchId");
                    if (randomSearchIdReceived.equalsIgnoreCase(randomSearchId)) {
                        if (!CommonUtils.isConnectedToInternet(mContext)) {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_INTERNET_PROBLEM);
                            textViewProblemText.setText(problemString);
                        } else {
                            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_RESULTS);
                            textViewProblemText.setText(noSearchResultsString);
                        }
                    }
                } else if (Constants.ACTION_COLLAPSE_EXPANDED_PRODUCT.equalsIgnoreCase(action)) {
                    boolean expand = false;
                    int settingsPosition = 0;
                    int productPosition = 0;
                    if (intent.getExtras() != null) {
                        expand = intent.getExtras().getBoolean("expand");
                        settingsPosition = intent.getExtras().getInt("settingsPosition");
                        productPosition = intent.getExtras().getInt("productPosition");
                    }
                    expandOrCollapseProduct(expand, settingsPosition, productPosition);

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_INCREASE_VARIETY_COUNT)) {
                    Long varietyId = intent.getLongExtra("varietyId", 0l);
                    int position = intent.getIntExtra("position", 0);
                    int settingsPosition = intent.getIntExtra("settingsPosition", 0);
                    if (varietyId > 0) {
                        adapter.increaseVarietyCount(settingsPosition, position, varietyId);
                        ((SearchActivity)getActivity()).updateHotCount();
                        adapter.notifyItemChanged(position);
                    } else {
                        return;
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_DECREASE_VARIETY_COUNT)) {
                    Long varietyId = intent.getLongExtra("varietyId", 0l);
                    int position = intent.getIntExtra("position", 0);
                    int settingsPosition = intent.getIntExtra("settingsPosition", 0);
                    if (varietyId > 0) {
                        adapter.decreaseVarietyCount(settingsPosition, position, varietyId);
                        ((SearchActivity)getActivity()).updateHotCount();
                        adapter.notifyItemChanged(position);
                    } else {
                        return;
                    }
                }
            }
        };
    }

    private void loadTheResultsIntoUi() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
        if(buyerAddress!=null) {
            adapter = new MultiSellerSearchAdapter(mContext, results, buyerAddress);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_RECYCLER_VIEW);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_NO_ADDRESS_SELECTED);
        }
    }

    private void getSearchResultsFromInternet() {
        randomSearchId = CommonUtils.randomString(10);
        SearchIntentService.getMultiSellerResults(mContext, searchQuery, false, false, 10, 0, randomSearchId);
    }

    public SellerSearchResults getResultAtPosition(int position) {
        return results.get(position);
    }

    public void setSearchActivityListener(SearchActivityListener listener) {
        this.mListener = listener;
    }

    private void setupScrollListenerOnRv() {
        final int THRESHHOLD_FOR_SCROLL = 20;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int dy = scrollY - oldScrollY;
                    if ((cummulativeDy <= 0 && dy > 0) || (cummulativeDy >= 0 && dy < 0)) {
                        //if scroll in opposite direction than current direction
                        cummulativeDy = dy;
                    } else {
                        cummulativeDy += dy;
                    }
                    if (cummulativeDy > THRESHHOLD_FOR_SCROLL && !searchBarHidden) {
                        Log.d(TAG, "will hide search bar");
                        mListener.hideSearchBar();
                        searchBarHidden = true;
                        searchBarStateKnown = true;
                    } else if ((cummulativeDy < -1 * THRESHHOLD_FOR_SCROLL && (searchBarHidden || !searchBarStateKnown)) || scrollY == 0) {
                        Log.d(TAG, "will show search bar");
                        mListener.showSearchBar();
                        searchBarHidden = false;
                        searchBarStateKnown = true;
                    }

                }
            });
        } else {
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if ((cummulativeDy <= 0 && dy > 0) || (cummulativeDy >= 0 && dy < 0)) {
                        //if scroll in opposite direction than current direction
                        cummulativeDy = dy;
                    } else {
                        cummulativeDy += dy;
                    }
                    if (cummulativeDy > THRESHHOLD_FOR_SCROLL && !searchBarHidden) {
                        Log.d(TAG, "will hide search bar");
                        mListener.hideSearchBar();
                        searchBarHidden = true;
                        searchBarStateKnown = true;
                    } else if (cummulativeDy < -1 * THRESHHOLD_FOR_SCROLL && (searchBarHidden || !searchBarStateKnown)) {
                        Log.d(TAG, "will show search bar");
                        mListener.showSearchBar();
                        searchBarHidden = false;
                        searchBarStateKnown = true;
                    }
                }
            });
        }
    }

    private void expandOrCollapseProduct(boolean expand, int settingsPosition, int productPosition) {
        adapter.expandProduct(expand, settingsPosition, productPosition);
    }
}
