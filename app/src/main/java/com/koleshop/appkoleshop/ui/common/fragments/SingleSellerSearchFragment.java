package com.koleshop.appkoleshop.ui.common.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.listeners.SearchActivityListener;
import com.koleshop.appkoleshop.model.ProductSelectionRequest;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.services.SearchIntentService;
import com.koleshop.appkoleshop.ui.buyer.activities.SearchActivity;
import com.koleshop.appkoleshop.ui.seller.adapters.InventoryProductAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class SingleSellerSearchFragment extends Fragment {

    private static final String ARG_MY_INVENTORY = "myInventory";
    private static final String ARG_CUSTOMER_VIEW = "customerView";
    private static final String ARG_SELLER_ID = "sellerId";
    private static final String ARG_SEARCH_QUERY = "searchQuery";
    private static final String ARG_PARCELABLE_PRODUCTS = "parcelableProducts";
    private static final String ARG_SELLER_SETTINGS = "sellerSettings";
    private static final String ARG_LOAD_MORE_ON_SCROLL_TO_END = "loadMore";

    private static final int VIEW_FLIPPER_CHILD_LOADING = 0;
    private static final int VIEW_FLIPPER_CHILD_RECYCLER_VIEW = 1;
    private static final int VIEW_FLIPPER_CHILD_INTERNET_PROBLEM = 2;
    private static final int VIEW_FLIPPER_CHILD_NO_RESULTS = 3;

    @Bind(R.id.rv_single_seller_search)
    RecyclerView recyclerView;
    @Bind(R.id.vf_fss)
    ViewFlipper viewFlipper;
    @Bind(R.id.root_frame_layout_fss)
    FrameLayout rootFrameLayout;
    @Bind(R.id.tv_problem_in_single_seller_search)
    TextView textViewProblemText;
    @BindString(R.string.some_problem_occurred)
    String problemString;
    @BindString(R.string.no_search_results_found)
    String noSearchResultsString;
    @Bind(R.id.pb_fragment_single_seller)
    SmoothProgressBar progressBar;

    private Context mContext;
    private boolean myInventory;
    private boolean customerView;
    private Long sellerId;
    private String searchQuery;
    List<Product> products;
    SellerSettings sellerSettings;

    SearchActivityListener mListener;
    private boolean searchBarHidden;
    private boolean searchBarStateKnown;

    private InventoryProductAdapter adapter;
    private BroadcastReceiver mBroadcastReceiver;
    private int cumulativeDy;
    private final String TAG = "SingleSellerFragment";
    private int visibleItemCount;
    private int totalItemCount;
    private LinearLayoutManager mLinearLayoutManager;
    private int firstVisibleItem;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    private int current_page;
    private static final int ITEMS_PER_PAGE = 20;
    private boolean loadMoreOnScrollToEnd;

    public SingleSellerSearchFragment() {
        // Required empty public constructor
    }

    public static SingleSellerSearchFragment newInstance(boolean myInventory, boolean customerView, Long sellerId
            , String searchQuery, List<EditProduct> editProducts, SellerSettings sellerSettings) {
        SingleSellerSearchFragment fragment = new SingleSellerSearchFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_CUSTOMER_VIEW, customerView);
        args.putBoolean(ARG_MY_INVENTORY, myInventory);
        args.putLong(ARG_SELLER_ID, sellerId);
        args.putString(ARG_SEARCH_QUERY, searchQuery);
        if (editProducts != null) {
            args.putParcelable(ARG_PARCELABLE_PRODUCTS, Parcels.wrap(editProducts));
        } else {
            args.putBoolean(ARG_LOAD_MORE_ON_SCROLL_TO_END, true);
        }
        args.putString(ARG_SEARCH_QUERY, searchQuery);
        args.putParcelable(ARG_SELLER_SETTINGS, Parcels.wrap(sellerSettings));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        adapter = null;
        if (getArguments() != null) {
            myInventory = getArguments().getBoolean(ARG_MY_INVENTORY);
            customerView = getArguments().getBoolean(ARG_CUSTOMER_VIEW);
            sellerId = getArguments().getLong(ARG_SELLER_ID);
            searchQuery = getArguments().getString(ARG_SEARCH_QUERY);
            loadMoreOnScrollToEnd = getArguments().getBoolean(ARG_LOAD_MORE_ON_SCROLL_TO_END);
            sellerSettings = Parcels.unwrap(getArguments().getParcelable(ARG_SELLER_SETTINGS));
            Parcelable parcelableProducts = getArguments().getParcelable(ARG_PARCELABLE_PRODUCTS);
            if (parcelableProducts != null) {
                List<EditProduct> editProducts = Parcels.unwrap(parcelableProducts);
                if (editProducts != null) {
                    products = KoleshopUtils.getProductListFromEditProductList(editProducts);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_seller, container, false);
        ButterKnife.bind(this, view);
        setupRootCardView();
        setupLinearLayoutManager();
        progressBar.setScaleY(3f);
        if (products != null) {
            //SHOW THE PRODUCTS (SEARCH RESULTS ALREADY PRESENT)
            /*
            THIS CASE ONLY OCCURS WHEN THIS FRAGMENT IS A CHILD OF MultiSellerSearchFragment (only in case of customer view
             - categoryId of Products can be different and doesn't matter in initializing the adapter in next line
             */
            initializeBroadcastReceivers();
            searchResultsFetchSuccess(products);
        } else if (myInventory && customerView) {
            //CUSTOMER SEARCHED IN A SINGLE SHOP
            initializeBroadcastReceivers();
            getSearchResultsFromInternet();
        } else if (myInventory && !customerView) {
            //SELLER SEARCHED IN HIS "MY PRODUCTS"...get results from internet using searchQuery
            initializeBroadcastReceivers();
            getSearchResultsFromInternet();
        } else {
            //SELLER SEARCHED IN THE WAREHOUSE...GET RESULTS FROM INTERNET
            initializeBroadcastReceivers();
            getSearchResultsFromInternet();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_EMPTY));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NOTIFY_PRODUCT_SELECTION_VARIETY_TO_PARENT));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_COLLAPSE_EXPANDED_PRODUCT));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_INCREASE_VARIETY_COUNT));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_DECREASE_VARIETY_COUNT));

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
                if (isAdded()) {
                    String action = intent.getAction();


                    //SEARCH RESULTS RECEIVERS
                    if (Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS.equalsIgnoreCase(action)) {
                        List<Product> listOfFetchedProducts = KoleshopUtils.getProductListFromEditProductList((List<EditProduct>) Parcels.unwrap(intent.getExtras().getParcelable(ARG_PARCELABLE_PRODUCTS)));
                        searchResultsFetchSuccess(listOfFetchedProducts);
                    } else if (Constants.ACTION_SEARCH_RESULTS_EMPTY.equalsIgnoreCase(action)) {
                        searchResultsFetchEmpty();
                    } else if (Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED.equalsIgnoreCase(action)) {
                        searchResultsFetchFailed();
                    }


                    //OTHER RECEIVERS
                    else if (Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS.equalsIgnoreCase(action)) {
                        Parcelable parcelableRequest = intent.getParcelableExtra("request");
                        ProductSelectionRequest request = Parcels.unwrap(parcelableRequest);
                        if (adapter != null && adapter.getPendingRequestsRandomIds() != null && adapter.getPendingRequestsRandomIds().contains(request.getRandomId())) {
                            adapter.updateProductSelection(request, true);
                        }

                        //update product selection failed
                    } else if (Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE.equalsIgnoreCase(action)) {
                        Parcelable parcelableRequest = intent.getParcelableExtra("request");
                        ProductSelectionRequest request = Parcels.unwrap(parcelableRequest);
                        if (adapter != null && adapter.getPendingRequestsRandomIds() != null && adapter.getPendingRequestsRandomIds().contains(request.getRandomId())) {
                            adapter.updateProductSelection(request, false);
                        }

                        //notification from product variety view
                    } else if (Constants.ACTION_NOTIFY_PRODUCT_SELECTION_VARIETY_TO_PARENT.equalsIgnoreCase(action)) {
                        Long requestCategoryId = intent.getLongExtra("requestCategoryId", 0l);
                        Long varietyId = intent.getLongExtra("varietyId", 0l);
                        int position = intent.getIntExtra("position", 0);
                        boolean varietySelected = intent.getBooleanExtra("varietySelected", false);
                        if (varietyId > 0) {
                            adapter.requestProductSelection(position, varietyId, varietySelected);
                            adapter.notifyItemChanged(position);
                        } else {
                            return;
                        }
                    } else if (Constants.ACTION_COLLAPSE_EXPANDED_PRODUCT.equalsIgnoreCase(action)) {
                        Long receivedCategoryId = intent.getLongExtra("categoryId", 0l);
                        int position = intent.getIntExtra("position", 0);
                        if (position > 0) {
                            adapter.collapseTheExpandedItem();
                        } else {
                            return;
                        }
                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_INCREASE_VARIETY_COUNT)) {
                        Long varietyId = intent.getLongExtra("varietyId", 0l);
                        int position = intent.getIntExtra("position", 0);
                        if (varietyId > 0) {
                            adapter.increaseVarietyCount(position, varietyId);
                            ((SearchActivity)getActivity()).updateHotCount();
                            adapter.notifyItemChanged(position);
                        } else {
                            return;
                        }
                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_DECREASE_VARIETY_COUNT)) {
                        Long varietyId = intent.getLongExtra("varietyId", 0l);
                        int position = intent.getIntExtra("position", 0);
                        if (varietyId > 0) {
                            adapter.decreaseVarietyCount(position, varietyId);
                            ((SearchActivity)getActivity()).updateHotCount();
                            adapter.notifyItemChanged(position);
                        } else {
                            return;
                        }
                    }
                }
            }
        };
    }

    private void setupLinearLayoutManager() {
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        adapter = new InventoryProductAdapter(mContext, 0l, myInventory, customerView);
        recyclerView.setAdapter(adapter);
    }

    private void setupRootCardView() {
        rootFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing...this on click listener is added so that click event doesn't pass to the parent activity
            }
        });
    }

    private void searchResultsFetchSuccess(List<Product> fetchedProducts) {
        if(current_page == 0) {
            //initialize the rv and adapter
            products = fetchedProducts;
            adapter.setProductsList(products);
            adapter.setSellerSettings(sellerSettings);
            adapter.notifyDataSetChanged();
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_RECYCLER_VIEW);
            setupScrollListenerOnRv();
        } else {
            //add products to adapter and rv
            addMoreSearchResults(fetchedProducts);
            progressBar.setVisibility(View.GONE);

        }
    }

    private void searchResultsFetchEmpty() {
        if(current_page == 0) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_RESULTS);
            textViewProblemText.setText(noSearchResultsString);
        } else {
            progressBar.setVisibility(View.GONE);
            //Snackbar.make(rootFrameLayout, "No more products to load", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void searchResultsFetchFailed() {
        if(current_page == 0) {
            if (!CommonUtils.isConnectedToInternet(mContext)) {
                viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_INTERNET_PROBLEM);
            } else {
                viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_RESULTS);
                textViewProblemText.setText(problemString);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            if (!CommonUtils.isConnectedToInternet(mContext)) {
                Snackbar.make(rootFrameLayout, "Please check your connection", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(rootFrameLayout, "Some problem while loading", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void getSearchResultsFromInternet() {
        SearchIntentService.getSingleSellerResults(mContext, searchQuery, ITEMS_PER_PAGE, current_page*ITEMS_PER_PAGE, sellerId, !customerView, myInventory);
    }

    public void setSearchActivityListener(SearchActivityListener listener) {
        this.mListener = listener;
    }

    private void setupScrollListenerOnRv() {
        final int THRESHOLD_FOR_SCROLL = 20;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int dy = scrollY - oldScrollY;

                    //01. Show/hide search bar
                    if ((cumulativeDy <= 0 && dy > 0) || (cumulativeDy >= 0 && dy < 0)) {
                        //if scroll in opposite direction than current direction
                        cumulativeDy = dy;
                    } else {
                        cumulativeDy += dy;
                    }
                    if (cumulativeDy > THRESHOLD_FOR_SCROLL && !searchBarHidden) {
                        Log.d(TAG, "will hide search bar");
                        mListener.hideSearchBar();
                        searchBarHidden = true;
                        searchBarStateKnown = true;
                    } else if ((cumulativeDy < -1 * THRESHOLD_FOR_SCROLL && (searchBarHidden || !searchBarStateKnown)) || scrollY == 0) {
                        Log.d(TAG, "will show search bar");
                        mListener.showSearchBar();
                        searchBarHidden = false;
                        searchBarStateKnown = true;
                    }

                    if(loadMoreOnScrollToEnd) {
                        //02. Load more results
                        visibleItemCount = recyclerView.getChildCount();
                        totalItemCount = mLinearLayoutManager.getItemCount();
                        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {
                            if (totalItemCount > previousTotal) {
                                loading = false;
                                previousTotal = totalItemCount;
                            }
                        }
                        if (!loading && (totalItemCount - visibleItemCount)
                                <= (firstVisibleItem + visibleThreshold)) {
                            // End has been reached

                            // Do something
                            current_page++;
                            loading = true;
                            onLoadMore(current_page);

                        }
                    }

                }
            });
        } else {
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    //01. show/hide search bar
                    if ((cumulativeDy <= 0 && dy > 0) || (cumulativeDy >= 0 && dy < 0)) {
                        //if scroll in opposite direction than current direction
                        cumulativeDy = dy;
                    } else {
                        cumulativeDy += dy;
                    }
                    if (cumulativeDy > THRESHOLD_FOR_SCROLL && !searchBarHidden) {
                        Log.d(TAG, "will hide search bar");
                        mListener.hideSearchBar();
                        searchBarHidden = true;
                        searchBarStateKnown = true;
                    } else if (cumulativeDy < -1 * THRESHOLD_FOR_SCROLL && (searchBarHidden || !searchBarStateKnown)) {
                        Log.d(TAG, "will show search bar");
                        mListener.showSearchBar();
                        searchBarHidden = false;
                        searchBarStateKnown = true;
                    }


                    if(loadMoreOnScrollToEnd) {
                        //02. load more on scroll
                        visibleItemCount = recyclerView.getChildCount();
                        totalItemCount = mLinearLayoutManager.getItemCount();
                        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {
                            if (totalItemCount > previousTotal) {
                                loading = false;
                                previousTotal = totalItemCount;
                            }
                        }
                        if (!loading && (totalItemCount - visibleItemCount)
                                <= (firstVisibleItem + visibleThreshold)) {
                            // End has been reached

                            // Do something
                            current_page++;
                            loading = true;
                            onLoadMore(current_page);

                        }
                    }

                }
            });
        }
    }

    private void onLoadMore(int current_page) {
        progressBar.setVisibility(View.VISIBLE);
        getSearchResultsFromInternet();
    }

    private void addMoreSearchResults(List<Product> moreProducts) {
        if(moreProducts!=null && moreProducts.size()>0) {
            if(products!=null) {
                products.addAll(moreProducts);
                try {
                    adapter.setProductsList(products);
                    adapter.setSellerSettings(sellerSettings);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "exception caught");
                }
            }
        }
    }

}
