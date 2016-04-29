package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.listeners.SearchActivityListener;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.ui.buyer.fragments.MultiSellerSearchFragment;
import com.koleshop.appkoleshop.ui.common.fragments.SingleSellerSearchFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CartUtils;

import org.parceler.Parcels;


import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity implements SearchActivityListener {

    @BindView(R.id.root_search_view)
    FrameLayout rootLinearLayout;
    @BindView(R.id.button_back_amss)
    ImageButton imageButtonBack;
    @BindView(R.id.button_clear_tag_amss)
    ImageButton imageButtonClearTag;
    @BindView(R.id.cardview_search_amss)
    CardView searchBar;
    @BindView(R.id.tv_search_tag_amss)
    TextView textViewSeachTag;
    @BindView(R.id.et_search_amss)
    EditText editTextSearch;
    @BindView(R.id.search_tag_amss)
    FrameLayout frameLayoutSearchTag;
    @BindString(R.string.navigation_drawer_products)
    String myShopString;
    @BindString(R.string.navigation_drawer_inventory)
    String wareHouseString;
    @BindView(R.id.fab_add_new_product)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.text_on_floating_button)
    TextView noOfItemsViewer;
    @BindView(R.id.frame_layout_floating_cart)
    FrameLayout frameLayout;
    Context mContext;

    String searchQuery;
    boolean multiSellerSearch;
    boolean customerView;
    boolean myInventory;
    Long sellerId;
    String sellerSearchTag;
    SellerSettings sellerSettings;
    BroadcastReceiver mBroadcastReceiver;

    MultiSellerSearchFragment multiSellerSearchFragment;
    SingleSellerSearchFragment singleSellerSearchFragment;

    boolean removeSearchTagOnBackPressed;
    boolean searchBarVisible;
    private static final String TAG = "MultiSellerSearchAct";
    private static final String ARG_SEARCH_QUERY = "searchQuery";
    private static final String ARG_MULTI_SELLER_SEARCH = "multiSellerSearch";
    private static final String ARG_CUSTOMER_VIEW = "customerView";
    private static final String ARG_SELLER_ID = "sellerId";
    private static final String ARG_MY_INVENTORY = "myInventory";
    private static final String ARG_SEARCH_TAG = "sellerSearchTag";
    private static final String ARG_SELLER_SETTINGS = "sellerSettings";

    public static Intent newMultiSellerSearch(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(ARG_SEARCH_QUERY, "");
        intent.putExtra(ARG_MULTI_SELLER_SEARCH, true);
        intent.putExtra(ARG_CUSTOMER_VIEW, true);
        intent.putExtra(ARG_MY_INVENTORY, true);
        return intent;
    }

    public static Intent newSingleSellerSearch(Context context, SellerSettings sellerSettings) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(ARG_SEARCH_QUERY, "");
        intent.putExtra(ARG_MULTI_SELLER_SEARCH, false);
        intent.putExtra(ARG_CUSTOMER_VIEW, true);
        intent.putExtra(ARG_MY_INVENTORY, true);
        intent.putExtra(ARG_SELLER_ID, sellerSettings.getUserId());
        intent.putExtra(ARG_SEARCH_TAG, sellerSettings.getAddress().getName());
        intent.putExtra(ARG_SELLER_SETTINGS, Parcels.wrap(sellerSettings));
        return intent;
    }

    public static Intent newMyShopSearch(Context context, boolean myInventory, Long sellerId, String searchTag) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(ARG_SEARCH_QUERY, "");
        intent.putExtra(ARG_MULTI_SELLER_SEARCH, false);
        intent.putExtra(ARG_CUSTOMER_VIEW, false);
        intent.putExtra(ARG_MY_INVENTORY, myInventory);
        intent.putExtra(ARG_SELLER_ID, sellerId);
        intent.putExtra(ARG_SEARCH_TAG, searchTag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchBarVisible = true;
        mContext = this;
        ButterKnife.bind(this);
        updateHotCount();
        frameLayout.setVisibility(View.INVISIBLE);

        Bundle receivedBundle = null;
        if (getIntent() != null && getIntent().getExtras() != null) {
            receivedBundle = getIntent().getExtras();
        }
        if (receivedBundle != null) {
            searchQuery = receivedBundle.getString(ARG_SEARCH_QUERY);
            multiSellerSearch = receivedBundle.getBoolean(ARG_MULTI_SELLER_SEARCH);
            customerView = receivedBundle.getBoolean(ARG_CUSTOMER_VIEW);
            sellerId = receivedBundle.getLong(ARG_SELLER_ID);
            myInventory = receivedBundle.getBoolean(ARG_MY_INVENTORY);
            sellerSearchTag = receivedBundle.getString(ARG_SEARCH_TAG);
            sellerSettings = Parcels.unwrap(receivedBundle.getParcelable(ARG_SELLER_SETTINGS));
        } else if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(ARG_SEARCH_QUERY);
            multiSellerSearch = savedInstanceState.getBoolean(ARG_MULTI_SELLER_SEARCH, false);
            customerView = savedInstanceState.getBoolean(ARG_CUSTOMER_VIEW, false);
            sellerId = savedInstanceState.getLong(ARG_SELLER_ID, 0l);
            myInventory = savedInstanceState.getBoolean(ARG_MY_INVENTORY, false);
            sellerSearchTag = savedInstanceState.getString(ARG_SEARCH_TAG);
            sellerSettings = Parcels.unwrap(savedInstanceState.getParcelable(ARG_SELLER_SETTINGS));
        }

        configureTheFloatingCartButton();
        configureTheSearchBar();
        configureTheRootLayout();
        initializeBroadcastReceivers();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_SEARCH_QUERY, searchQuery);
        outState.putBoolean(ARG_MULTI_SELLER_SEARCH, multiSellerSearch);
        outState.putBoolean(ARG_CUSTOMER_VIEW, customerView);
        outState.putLong(ARG_SELLER_ID, sellerId);
        outState.putBoolean(ARG_MY_INVENTORY, myInventory);
        outState.putString(ARG_SEARCH_TAG, sellerSearchTag);
        outState.putParcelable(ARG_SELLER_SETTINGS, Parcels.wrap(sellerSettings));

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_OPEN_SINGLE_SELLER_RESULTS));
        if (editTextSearch != null && !TextUtils.isEmpty(editTextSearch.getText())) {
            try {
                loadTheSearchResults();
                showSearchBar();
                updateHotCount();
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (removeSearchTagOnBackPressed) {
            multiSellerSearch = true;
            sellerId = 0l;
            sellerSearchTag = "";
            configureTheSearchBar();
            loadTheSearchResults();
        }
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Constants.ACTION_OPEN_SINGLE_SELLER_RESULTS:
                        if (intent.getExtras() != null) {
                            int position = intent.getExtras().getInt("single_seller_position");
                            openSingleSellerResults(position);
                        }
                }
            }
        };
    }

    private void configureTheRootLayout() {
        rootLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configureTheFloatingCartButton() {
        if (customerView) {
            floatingActionButton.setVisibility(View.VISIBLE);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cartActivityIntent = new Intent(mContext, CartActivity.class);
                    startActivity(cartActivityIntent);
                }
            });
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }
    }

    private void configureTheSearchBar() {

        //01. Configure the search tag
        if (customerView && multiSellerSearch) {
            //MULTI SELLER SEARCH
            frameLayoutSearchTag.setVisibility(View.GONE);
            hideSearchTag();
        } else if (customerView) {
            //SINGLE SELLER SEARCH IN CUSTOMER VIEW
            showSearchTag();
            textViewSeachTag.setText(sellerSearchTag);
            refreshTagClearListener();
        } else if (!customerView && myInventory) {
            //SELLER SEARCH IN MY SHOP
            showSearchTag();
            textViewSeachTag.setText(sellerSearchTag);
            refreshTagClearListener();

        } else {
            //SELLER SEARCH IN WAREHOUSE
            showSearchTag();
            sellerSearchTag = wareHouseString;
            textViewSeachTag.setText(sellerSearchTag);
            refreshTagClearListener();
        }

        //02. Add text change listener
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    imageButtonBack.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_arrow_back_grey600_24dp));
                } else {
                    imageButtonBack.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_clear_grey600_24dp));
                }
                //get search suggestions here
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //03. add button click listeners
        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editTextSearch.getText())) {
                    //ON CLEAR
                    editTextSearch.setText(null);
                    loadTheSearchResults();
                } else {
                    //ON BACK PRESSED
                    finish();
                }
            }
        });

        //04. Configure the search edit text
        editTextSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) {
                    return false;
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    hideSoftKeyboard();
                    loadTheSearchResults();
                    return true;
                } else {
                    return false;
                }
            }
        });

        editTextSearch.requestFocus();

    }

    private void refreshTagClearListener() {
        if (customerView) {
            imageButtonClearTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sellerSearchTag = "";
                    if (customerView) {
                        multiSellerSearch = true;
                    } else {
                        myInventory = false;
                    }
                    sellerId = 0l;
                    hideSearchTag();
                    loadTheSearchResults();
                }
            });
        } else if (myInventory) {
            imageButtonClearTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sellerSearchTag = wareHouseString;
                    myInventory = false;
                    refreshSearchTag();
                    loadTheSearchResults();
                }
            });
        } else {
            imageButtonClearTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sellerSearchTag = myShopString;
                    myInventory = true;
                    refreshSearchTag();
                    loadTheSearchResults();
                }
            });
        }
    }

    private void loadTheSearchResults() {
        searchQuery = editTextSearch.getText().toString();
        Log.d(TAG, "loading the search results...");
        if (!TextUtils.isEmpty(searchQuery)) {
            Log.d(TAG, ">>>search query = " + searchQuery);
            editTextSearch.clearFocus();
            if (multiSellerSearch) {
                Log.d(TAG, ">>>multi seller search");
                multiSellerSearchFragment = MultiSellerSearchFragment.newQueryInstance(searchQuery);
                getSupportFragmentManager().beginTransaction().replace(R.id.container_search_view, multiSellerSearchFragment, "multi_seller_search_frag").commit();
                multiSellerSearchFragment.setSearchActivityListener(this);
            } else {
                //nothing
                Log.d(TAG, ">>>single seller search");
                singleSellerSearchFragment = SingleSellerSearchFragment.newInstance(myInventory, customerView, sellerId, searchQuery, null, sellerSettings);
                getSupportFragmentManager().beginTransaction().replace(R.id.container_search_view, singleSellerSearchFragment, "single_seller_search_frag").commit();
                singleSellerSearchFragment.setSearchActivityListener(this);
            }
        } else {
            Log.d(TAG, ">>>search cleared");
            if (multiSellerSearch) {
                if (multiSellerSearchFragment != null) {
                    Log.d(TAG, "removing multi search fragment");
                    getSupportFragmentManager().beginTransaction().remove(multiSellerSearchFragment).commit();
                    frameLayout.setVisibility(View.INVISIBLE);
                }
            } else if (singleSellerSearchFragment != null) {
                Log.d(TAG, "removing single search fragment");
                getSupportFragmentManager().beginTransaction().remove(singleSellerSearchFragment).commit();
                frameLayout.setVisibility(View.INVISIBLE);
            }
        }

    }

    private void openSingleSellerResults(int position) {
        SellerSearchResults singleSellerResults = multiSellerSearchFragment.getResultAtPosition(position);
        multiSellerSearch = false;
        sellerId = singleSellerResults.getSellerSettings().getUserId();
        sellerSearchTag = singleSellerResults.getSellerSettings().getAddress().getName();
        singleSellerSearchFragment = SingleSellerSearchFragment.newInstance(myInventory, customerView, singleSellerResults.getSellerSettings().getUserId(), searchQuery, singleSellerResults.getProducts(), singleSellerResults.getSellerSettings());
        singleSellerSearchFragment.setSearchActivityListener(this);
        removeSearchTagOnBackPressed = true;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.container_search_view, singleSellerSearchFragment, "single_seller_search_frag");
        ft.addToBackStack(null);
        ft.commit();
        configureTheSearchBar();
        if (!searchBarVisible) {
            showSearchBar();
        }
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frameLayout.setVisibility(View.VISIBLE);
                    updateHotCount();
                }
            }, 1000);
        }
    }

    @Override
    public void hideSearchBar() {
        searchBar.setVisibility(View.GONE);
        searchBar.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.hide_search_bar_to_top));
        searchBarVisible = false;
        hideSoftKeyboard();
    }

    @Override
    public void showSearchBar() {
        searchBar.setVisibility(View.VISIBLE);
        searchBar.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.show_search_bar_from_top));
        searchBarVisible = true;
    }

    public void showSearchTag() {
        frameLayoutSearchTag.setVisibility(View.VISIBLE);
        frameLayoutSearchTag.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.show_tag_from_right));
    }

    private void hideSearchTag() {
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.hide_tag_to_right);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                frameLayoutSearchTag.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        frameLayoutSearchTag.startAnimation(anim);
    }

    private void refreshSearchTag() {
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.hide_tag_to_right);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                refreshTagClearListener();
                textViewSeachTag.setText(sellerSearchTag);
                showSearchTag();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        frameLayoutSearchTag.startAnimation(anim);
    }

    public void updateHotCount() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (CartUtils.getCartsTotalCount() == 0) {
                    noOfItemsViewer.setVisibility(View.INVISIBLE);
                } else if (frameLayout.getVisibility() == View.VISIBLE) {
                    noOfItemsViewer.setVisibility(View.VISIBLE);
                    noOfItemsViewer.setText(CartUtils.getCartsTotalCount() + "");
                }
            }
        });

    }
}
