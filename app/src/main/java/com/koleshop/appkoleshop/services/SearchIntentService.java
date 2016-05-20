package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.ArrayMap;
import com.koleshop.api.buyerEndpoint.BuyerEndpoint;
import com.koleshop.api.buyerEndpoint.model.KoleResponse;
import com.koleshop.api.sellerEndpoint.SellerEndpoint;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.util.CloudEndpointDataExtractionUtil;
import com.koleshop.appkoleshop.util.KoleCacheUtil;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;


public class SearchIntentService extends IntentService {

    private static final String ACTION_SEARCH_MULTI_SELLER = "com.koleshop.appkoleshop.services.action.SEARCH_MULTI_SELLER";
    private static final String ACTION_SEARCH_SINGLE_SELLER = "com.koleshop.appkoleshop.services.action.SEARCH_SINGLE_SELLER";

    private static final String TAG = "SearchIntentService";
    private static final String EXTRA_SEARCH_QUERY = "com.koleshop.appkoleshop.services.extra.EXTRA_SEARCH_QUERY";
    private static String EXTRA_HOME_DELIVERY_ONLY = "com.koleshop.appkoleshop.services.extra.EXTRA_HOME_DELIVERY_ONLY";
    private static String EXTRA_OPEN_SHOPS_ONLY = "com.koleshop.appkoleshop.services.extra.EXTRA_OPEN_SHOPS_ONLY";
    private static String EXTRA_LIMIT = "com.koleshop.appkoleshop.services.extra.EXTRA_LIMIT";
    private static String EXTRA_OFFSET = "com.koleshop.appkoleshop.services.extra.EXTRA_OFFSET";
    private static String EXTRA_SELLER_ID = "com.koleshop.appkoleshop.services.extra.EXTRA_SELLER_ID";
    private static String EXTRA_SELLER_SIDE_SEARCH = "com.koleshop.appkoleshop.services.extra.EXTRA_SELLER_SIDE_SEARCH";
    private static String EXTRA_MY_INVENTORY = "com.koleshop.appkoleshop.services.extra.EXTRA_MY_INVENTORY";
    private static String EXTRA_RANDOM_SEARCH_ID = "com.koleshop.appkoleshop.services.extra.EXTRA_RANDOM_SEARCH_ID";

    public SearchIntentService() {
        super("SearchIntentService");
    }

    public static void getMultiSellerResults(Context context, String searchQuery, boolean homeDeliveryOnly, boolean openShopsOnly, int limit, int offset, String randomSearchId) {
        Intent intent = new Intent(context, SearchIntentService.class);
        intent.setAction(ACTION_SEARCH_MULTI_SELLER);
        intent.putExtra(EXTRA_SEARCH_QUERY, searchQuery);
        intent.putExtra(EXTRA_HOME_DELIVERY_ONLY, homeDeliveryOnly);
        intent.putExtra(EXTRA_OPEN_SHOPS_ONLY, openShopsOnly);
        intent.putExtra(EXTRA_LIMIT, limit);
        intent.putExtra(EXTRA_OFFSET, offset);
        intent.putExtra(EXTRA_RANDOM_SEARCH_ID, randomSearchId);
        context.startService(intent);
    }

    public static void getSingleSellerResults(Context context, String searchQuery, int limit, int offset, Long sellerId, boolean sellerSideSearch, boolean myInventory, String randomSearchId) {
        Intent intent = new Intent(context, SearchIntentService.class);
        intent.setAction(ACTION_SEARCH_SINGLE_SELLER);
        intent.putExtra(EXTRA_SEARCH_QUERY, searchQuery);
        intent.putExtra(EXTRA_LIMIT, limit);
        intent.putExtra(EXTRA_OFFSET, offset);
        intent.putExtra(EXTRA_SELLER_ID, sellerId);
        intent.putExtra(EXTRA_SELLER_SIDE_SEARCH, sellerSideSearch);
        intent.putExtra(EXTRA_MY_INVENTORY, myInventory);
        intent.putExtra(EXTRA_RANDOM_SEARCH_ID, randomSearchId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEARCH_MULTI_SELLER.equals(action)) {
                final String searchQuery = intent.getStringExtra(EXTRA_SEARCH_QUERY);
                final boolean homeDeliveryOnly = intent.getBooleanExtra(EXTRA_HOME_DELIVERY_ONLY, false);
                final boolean openShopsOnly = intent.getBooleanExtra(EXTRA_OPEN_SHOPS_ONLY, false);
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 10);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                final String randomSearchId = intent.getStringExtra(EXTRA_RANDOM_SEARCH_ID);
                getMultiSellerSearchResultsFromInternet(searchQuery, homeDeliveryOnly, openShopsOnly, limit, offset, randomSearchId);
            } else if (ACTION_SEARCH_SINGLE_SELLER.equalsIgnoreCase(action)) {
                final String searchQuery = intent.getStringExtra(EXTRA_SEARCH_QUERY);
                final int limit = intent.getIntExtra(EXTRA_LIMIT, 10);
                final int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                final Long sellerId = intent.getLongExtra(EXTRA_SELLER_ID, 0l);
                final boolean sellerSideSearch = intent.getBooleanExtra(EXTRA_SELLER_SIDE_SEARCH, false);
                final boolean myInventory = intent.getBooleanExtra(EXTRA_MY_INVENTORY, false);
                final String randomSearchId = intent.getStringExtra(EXTRA_RANDOM_SEARCH_ID);
                if(!sellerSideSearch) {
                    getSingleSellerSearchResultsFromInternet(searchQuery, limit, offset, sellerId, randomSearchId);
                } else {
                    searchProductsForSeller(myInventory, searchQuery, limit, offset, sellerId, randomSearchId);
                }
            }
        }
    }

    private void getMultiSellerSearchResultsFromInternet(String searchQuery, boolean homeDeliveryOnly, boolean openShopsOnly, int limit, int offset, String randomSearchId) {
        BuyerEndpoint buyerEndpoint = null;
        BuyerEndpoint.Builder builder = new BuyerEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setApplicationName(Constants.APP_NAME)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        buyerEndpoint = builder.build();

        KoleResponse result = null;

        try {
            Context context = getApplicationContext();
            //Long userId = PreferenceUtils.getUserId(context);
            //String sessionId = PreferenceUtils.getSessionId(context);
            BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
            Double gpsLong = buyerAddress.getGpsLong();
            Double gpsLat = buyerAddress.getGpsLat();
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = buyerEndpoint.searchProductsMultipleSeller(gpsLong, gpsLat, homeDeliveryOnly, openShopsOnly, limit, offset, searchQuery).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if(result!=null && result.getSuccess()) {
            ArrayList<ArrayMap<String, Object>> multiSellerResultsList = (ArrayList<ArrayMap<String, Object>>) result.getData();

            if(multiSellerResultsList!=null && multiSellerResultsList.size()>0) {
                //search results found
                List<SellerSearchResults> searchResults = new ArrayList<>();
                for (ArrayMap<String, Object> oneSellerResult : multiSellerResultsList) {
                    SellerSearchResults singleResult = new SellerSearchResults();

                    //01. find seller settings
                    ArrayMap<String, Object> sellerSettingsMap = (ArrayMap<String, Object>) oneSellerResult.get("sellerSettings");
                    SellerSettings sellerSettings = CloudEndpointDataExtractionUtil.getSellerSettings(sellerSettingsMap);
                    singleResult.setSellerSettings(sellerSettings);

                    //02. find total search results count
                    singleResult.setTotalSearchResultsCount(((BigDecimal) oneSellerResult.get("totalSearchResultsCount")).intValue());

                    //03. extract products list
                    ArrayList<ArrayMap<String, Object>> productsArraylist = (ArrayList<ArrayMap<String, Object>>) oneSellerResult.get("products");
                    List<EditProduct> productsList = CloudEndpointDataExtractionUtil.getEditProductsList(productsArraylist, sellerSettings.getUserId());
                    singleResult.setProducts(productsList);

                    //04. add to search results
                    searchResults.add(singleResult);
                }

                //broadcast the search results
                Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS);
                Parcelable parcelableSearchResults = Parcels.wrap(searchResults);
                intent.putExtra("searchResults", parcelableSearchResults);
                intent.putExtra("randomSearchId", randomSearchId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                //no search results found
                Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_EMPTY);
                intent.putExtra("randomSearchId", randomSearchId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        } else {
            Log.e(TAG, "search multiple sellers failed for query = " + searchQuery);
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED);
            intent.putExtra("randomSearchId", randomSearchId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void getSingleSellerSearchResultsFromInternet(String searchQuery, int limit, int offset, Long sellerId, String randomSearchId) {
        BuyerEndpoint buyerEndpoint = null;
        BuyerEndpoint.Builder builder = new BuyerEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setApplicationName(Constants.APP_NAME)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        buyerEndpoint = builder.build();

        KoleResponse result = null;

        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = buyerEndpoint.searchProducts(sellerId, limit, offset, searchQuery).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if(result!=null && result.getSuccess()) {
            ArrayList<ArrayMap<String, Object>> productsArrayList = (ArrayList<ArrayMap<String, Object>>) result.getData();
            List<EditProduct> productsList = CloudEndpointDataExtractionUtil.getEditProductsList(productsArrayList, sellerId);

            if(productsList!=null && productsList.size()>0) {
                //broadcast the search results
                Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS);
                Parcelable parcelableSearchResults = Parcels.wrap(productsList);
                intent.putExtra("parcelableProducts", parcelableSearchResults);
                intent.putExtra("randomSearchId", randomSearchId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                //no search results found
                Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_EMPTY);
                intent.putExtra("randomSearchId", randomSearchId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        } else {
            Log.e(TAG, "search multiple sellers failed for query = " + searchQuery);
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED);
            intent.putExtra("randomSearchId", randomSearchId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void searchProductsForSeller(boolean myInventory, String searchQuery, int limit, int offset, Long sellerId, String randomSearchId) {
        SellerEndpoint sellerEndpoint = null;
        SellerEndpoint.Builder builder = new SellerEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // use 10.0.2.2 for localhost testing
                .setRootUrl(Constants.SERVER_URL)
                .setApplicationName(Constants.APP_NAME)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        sellerEndpoint = builder.build();
        String sessionId = PreferenceUtils.getSessionId(getApplicationContext());

        com.koleshop.api.sellerEndpoint.model.KoleResponse result = null;

        try {
            int count = 0;
            int maxTries = 3;
            while (count < maxTries) {
                try {
                    result = sellerEndpoint.searchProducts(sellerId, sessionId, searchQuery, myInventory, limit, offset).execute();
                    count = maxTries;
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        if(result!=null && result.getSuccess()) {
            ArrayList<ArrayMap<String, Object>> productsArrayList = (ArrayList<ArrayMap<String, Object>>) result.getData();
            List<EditProduct> productsList = CloudEndpointDataExtractionUtil.getEditProductsList(productsArrayList, sellerId);

            if(productsList!=null && productsList.size()>0) {
                //broadcast the search results
                Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_FETCH_SUCCESS);
                Parcelable parcelableSearchResults = Parcels.wrap(productsList);
                intent.putExtra("parcelableProducts", parcelableSearchResults);
                intent.putExtra("randomSearchId", randomSearchId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                //no search results found
                Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_EMPTY);
                intent.putExtra("randomSearchId", randomSearchId);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        } else {
            Log.e(TAG, "search multiple sellers failed for query = " + searchQuery);
            if (result != null && result.getData() != null) Log.e(TAG, (String) result.getData());
            Intent intent = new Intent(Constants.ACTION_SEARCH_RESULTS_FETCH_FAILED);
            intent.putExtra("randomSearchId", randomSearchId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

}
