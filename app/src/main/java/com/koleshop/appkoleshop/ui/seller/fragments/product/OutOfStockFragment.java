package com.koleshop.appkoleshop.ui.seller.fragments.product;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.ProductSelectionRequest;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.services.SellerIntentService;
import com.koleshop.appkoleshop.ui.seller.adapters.OutOfStockAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OutOfStockFragment extends Fragment implements OutOfStockAdapter.OutOfStockAdapterListener {

    @Bind(R.id.view_flipper_fragment_out_of_stock)
    ViewFlipper viewFlipper;
    @Bind(R.id.rv_fragment_out_of_stock)
    RecyclerView recyclerView;
    @Bind(R.id.tv_nothing_here_yet)
    TextView textViewNothingHereYet;
    @Bind(R.id.iv_nothing_here_yet)
    ImageView imageViewNothingHereYet;
    @BindDrawable(R.drawable.ic_pinky_pipni)
    Drawable pinkyHappy;
    @BindString(R.string.no_out_of_stock)
    String noItemsOutOfStock;

    private final int VIEW_FLIPPER_CHILD_LOADING = 0x00;
    private final int VIEW_FLIPPER_CHILD_NO_INTERNET = 0x01;
    private final int VIEW_FLIPPER_CHILD_SOME_PROBLEM = 0x02;
    private final int VIEW_FLIPPER_CHILD_NO_OUT_OF_STOCK = 0x03;
    private final int VIEW_FLIPPER_CHILD_OUT_OF_STOCK_LIST = 0x04;

    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    List<Product> products;
    OutOfStockAdapter adapter;

    public OutOfStockFragment() {
        // Required empty public constructor
    }

    public static OutOfStockFragment newInstance() {
        OutOfStockFragment fragment = new OutOfStockFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_out_of_stock, container, false);
        mContext = getContext();
        ButterKnife.bind(this, view);
        initializeBroadcastReceiver();
        initializeRecyclerView();
        fetchOrdersFromInternet();
        setupNoItemsOutOfStockView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_OUT_OF_STOCK_FETCH_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_NO_ITEMS_OUT_OF_STOCK));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_OUT_OF_STOCK_FETCH_FAILED));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle bundle = intent.getExtras();
                switch (action) {
                    case Constants.ACTION_OUT_OF_STOCK_FETCH_SUCCESS:
                        if (bundle != null) {
                            Parcelable productsParcel = bundle.getParcelable("products");
                            if (productsParcel != null) {
                                products = Parcels.unwrap(productsParcel);
                                loadOutOfStockItems();
                            } else {
                                //some problem occurred
                                viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOME_PROBLEM);
                            }
                        }
                        break;
                    case Constants.ACTION_NO_ITEMS_OUT_OF_STOCK:
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_OUT_OF_STOCK);
                        break;
                    case Constants.ACTION_OUT_OF_STOCK_FETCH_FAILED:
                        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SOME_PROBLEM);
                        break;
                    case Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS:
                        Parcelable parcelableRequest = intent.getParcelableExtra("request");
                        ProductSelectionRequest request = Parcels.unwrap(parcelableRequest);
                        if (adapter != null && adapter.getProductSelectionRandomIdList() != null && adapter.getProductSelectionRandomIdList().contains(request.getRandomId())) {
                            adapter.updateProductSelection(request, true);
                        }
                        break;
                    case Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE:
                        Parcelable parcelableRequestFailed = intent.getParcelableExtra("request");
                        ProductSelectionRequest requestFailed = Parcels.unwrap(parcelableRequestFailed);
                        if (adapter != null && adapter.getProductSelectionRandomIdList() != null && adapter.getProductSelectionRandomIdList().contains(requestFailed.getRandomId())) {
                            adapter.updateProductSelection(requestFailed, false);
                        }
                        Snackbar.make(viewFlipper, "Some problem occured while doing back in stock", Snackbar.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        ;
    }

    private void initializeRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new OutOfStockAdapter(mContext, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupNoItemsOutOfStockView() {
        imageViewNothingHereYet.setImageDrawable(pinkyHappy);
        textViewNothingHereYet.setText(noItemsOutOfStock);
    }

    private void fetchOrdersFromInternet() {
        if (CommonUtils.isConnectedToInternet(mContext)) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
            SellerIntentService.getOutOfStockItems(mContext);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_INTERNET);
        }
    }

    private void loadOutOfStockItems() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_OUT_OF_STOCK_LIST);
        adapter.setProductsList(products);
        adapter.notifyDataSetChanged();
    }

    @OnClick({R.id.button_retry_vinc, R.id.button_retry_vspo})
    public void retry() {
        fetchOrdersFromInternet();
    }

    @Override
    public void removeProductAtPosition(int position) {
        products.remove(position);
        adapter.setProductsList(products);
        adapter.notifyItemRemoved(position);
    }
}
