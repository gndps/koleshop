package com.kolshop.kolshopmaterial.fragments.product;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.adapters.InventoryProductAdapter;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.SerializationUtil;
import com.kolshop.kolshopmaterial.extensions.KolClickListener;
import com.kolshop.kolshopmaterial.extensions.KolRecyclerTouchListener;
import com.kolshop.kolshopmaterial.model.ProductSelectionRequest;
import com.kolshop.kolshopmaterial.model.genericjson.GenericJsonListInventoryProduct;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.kolshopmaterial.singletons.KoleshopSingleton;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.tonicartos.superslim.LayoutManager;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.parceler.Parcels;

import java.util.List;

public class InventoryProductFragment extends Fragment {

    private RecyclerView recyclerView;
    private InventoryProductAdapter inventoryProductAdapter;
    private Context mContext;
    private ViewFlipper viewFlipper;
    BroadcastReceiver mBroadcastReceiverInventoryProductFragment;
    private long categoryId;
    Button buttonRetry, buttonReload;
    private final static String TAG = "InventProductFragment";
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    public InventoryProductFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle=getArguments();
        categoryId = bundle.getLong("categoryId");
        View layout = inflater.inflate(R.layout.fragment_inventory_product, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.rv_inventory_product);
        viewFlipper = (ViewFlipper) layout.findViewById(R.id.view_flipper_inventory_product_fragment);
        viewFlipper.setDisplayedChild(0);//loading
        buttonReload = (Button) layout.findViewById(R.id.button_reload_fragment_inventory_product);
        buttonRetry = (Button) layout.findViewById(R.id.button_retry_fragment_inventory_product);
        View.OnClickListener retryClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchProducts();
            }
        };
        buttonRetry.setOnClickListener(retryClickListener);
        buttonReload.setOnClickListener(retryClickListener);
        initializeBroadcastReceivers();
        LayoutManager lm = new LayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        fetchProducts();
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiverInventoryProductFragment, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiverInventoryProductFragment, new IntentFilter(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED));
        lbm.registerReceiver(mBroadcastReceiverInventoryProductFragment, new IntentFilter(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS));
        lbm.registerReceiver(mBroadcastReceiverInventoryProductFragment, new IntentFilter(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE));
        lbm.registerReceiver(mBroadcastReceiverInventoryProductFragment, new IntentFilter(Constants.ACTION_NOTIFY_PRODUCT_SELECTION_VARIETY_TO_PARENT));
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiverInventoryProductFragment = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isAdded()) {
                    //fetch product success
                    if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_SUCCESS)) {
                        long receivedCategoryId = intent.getLongExtra("catId", 0l);
                        //Log.d(TAG, "receivedCategoryId = " + receivedCategoryId + " and categoryId = " + categoryId);
                        if(receivedCategoryId == categoryId) {
                            //Log.d(TAG, "yippie...will load products now for categoryId = " + categoryId);
                            loadProducts(null);
                        } else {
                            //Log.d(TAG, "wtf is happening");
                        }

                    //fetch products failed
                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_FETCH_INVENTORY_PRODUCTS_FAILED)) {
                        long receivedCategoryId = intent.getLongExtra("catId", 0l);
                        //Log.d(TAG, "FAIL...receivedCategoryId = " + receivedCategoryId + " and categoryId = " + categoryId);
                        if(receivedCategoryId == categoryId) {
                            couldNotLoadProducts();
                        }

                    //update product selection
                    } else if(intent.getAction().equalsIgnoreCase(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS)) {
                        Parcelable parcelableRequest = intent.getParcelableExtra("request");
                        ProductSelectionRequest request = Parcels.unwrap(parcelableRequest);
                        if(inventoryProductAdapter!=null && inventoryProductAdapter.getPendingRequestsRandomIds()!=null &&  inventoryProductAdapter.getPendingRequestsRandomIds().contains(request.getRandomId())) {
                            inventoryProductAdapter.updateProductSelection(request, true);
                        }

                    //update product selection failed
                    } else if(intent.getAction().equalsIgnoreCase(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE)) {
                        Parcelable parcelableRequest = intent.getParcelableExtra("request");
                        ProductSelectionRequest request = Parcels.unwrap(parcelableRequest);
                        if(inventoryProductAdapter!=null && inventoryProductAdapter.getPendingRequestsRandomIds()!=null &&  inventoryProductAdapter.getPendingRequestsRandomIds().contains(request.getRandomId())) {
                            inventoryProductAdapter.updateProductSelection(request, false);
                        }

                    //notification from product variety view
                    } else if(intent.getAction().equalsIgnoreCase(Constants.ACTION_NOTIFY_PRODUCT_SELECTION_VARIETY_TO_PARENT)) {
                        Long requestCategoryId = intent.getLongExtra("requestCategoryId", 0l);
                        Long varietyId = intent.getLongExtra("varietyId", 0l);
                        int position = intent.getIntExtra("position", 0);
                        boolean varietySelected = intent.getBooleanExtra("varietySelected", false);
                        if(varietyId>0 && requestCategoryId == categoryId) {
                            inventoryProductAdapter.requestProductSelection(position, varietyId, varietySelected);
                            inventoryProductAdapter.notifyItemChanged(position);
                        } else {
                            return;
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.unregisterReceiver(mBroadcastReceiverInventoryProductFragment);
    }

    private void fetchProducts() {
        List<InventoryProduct> listOfProducts = getCachedProducts();
        if(listOfProducts!=null && listOfProducts.size()>0) {
            loadProducts(listOfProducts);
        } else {
            fetchProductsFromInternet();
        }
    }

    private void fetchProductsFromInternet() {
        Log.d(TAG, "will fetch products from internet for category id = " + categoryId);
        Intent intent = new Intent(mContext, CommonIntentService.class);
        intent.setAction(Constants.ACTION_FETCH_INVENTORY_PRODUCTS);
        intent.putExtra("categoryId", categoryId);
        mContext.startService(intent);
    }

    private List<InventoryProduct> getCachedProducts() {
        String cacheKey = Constants.CACHE_INVENTORY_PRODUCTS + categoryId;
        byte[] productByteArray = KoleshopSingleton.getSharedInstance().getCachedGenericJsonByteArray(cacheKey, Constants.TIME_TO_LIVE_INV_PRODUCT);
        if(productByteArray!=null && productByteArray.length>0) {
            try {
                GenericJsonListInventoryProduct genericProducts = SerializationUtil.getGenericJsonFromSerializable(productByteArray, GenericJsonListInventoryProduct.class);
                if(genericProducts!=null) {
                    List<InventoryProduct> products = genericProducts.getList();
                    return products;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private void loadProducts(final List<InventoryProduct> listOfProducts) {
        /*recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .margin(getResources().getDimensionPixelSize(R.dimen.recycler_view_left_margin),
                        getResources().getDimensionPixelSize(R.dimen.recycler_view_right_margin))
                .build());*/
        inventoryProductAdapter = new InventoryProductAdapter(getActivity(), categoryId);
        recyclerView.setAdapter(inventoryProductAdapter);
        recyclerView.setHasFixedSize(true);

        //get products to load
        final List<InventoryProduct> products;
        if(listOfProducts!=null && listOfProducts.size()>0) {
            products = listOfProducts;
        } else {
            products = getCachedProducts();
        }

        //set recycler view click listener
        recyclerView.addOnItemTouchListener(new KolRecyclerTouchListener(getActivity(), recyclerView, new KolClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //Toast.makeText(getActivity(), "product selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View v, int position) {
                //Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_LONG).show();
            }
        }));
        //recyclerView.setVerticalScrollBarEnabled(true); //no need of scroll bar...google play doesn't have it


        if(products!=null) {
            inventoryProductAdapter.setProductsList(products);
            //Log.d(TAG, "will set view flipper 2 for category id =" + categoryId);
            viewFlipper.setDisplayedChild(2);
        } else {
            //Log.d(TAG, "will set view flipper 3 for category id =" + categoryId );
            viewFlipper.setDisplayedChild(3);
        }
    }

    private void couldNotLoadProducts() {
        viewFlipper.setDisplayedChild(1);
    }

}
