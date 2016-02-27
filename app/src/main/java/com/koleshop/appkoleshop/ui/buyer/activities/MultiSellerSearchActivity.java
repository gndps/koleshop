package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.ui.buyer.fragments.MultiSellerSearchFragment;
import com.koleshop.appkoleshop.ui.common.fragments.SingleSellerFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.mypopsy.widget.FloatingSearchView;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MultiSellerSearchActivity extends AppCompatActivity implements ActionMenuView.OnMenuItemClickListener {

    @Bind(R.id.floating_search_bar)
    FloatingSearchView floatingSearchView;
    @Bind(R.id.root_search_view)
    FrameLayout rootLinearLayout;

    Context mContext;


    String searchQuery;
    boolean multiSellerSearch;
    boolean customerView;
    boolean myInventory;
    Long sellerId;

    long searchTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_seller_search);
        mContext = this;
        ButterKnife.bind(this);

        Bundle receivedBundle = null;
        if (getIntent() != null && getIntent().getExtras() != null) {
            receivedBundle = getIntent().getExtras();
        }
        if (receivedBundle != null) {
            searchQuery = receivedBundle.getString("searchQuery");
            multiSellerSearch = receivedBundle.getBoolean("multiSellerSearch");
            customerView = receivedBundle.getBoolean("customerView");
            sellerId = receivedBundle.getLong("sellerId");
            myInventory = receivedBundle.getBoolean("myInventory");
        } else if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery");
            multiSellerSearch = savedInstanceState.getBoolean("multiSellerSearch", false);
            customerView = savedInstanceState.getBoolean("customerView", false);
            sellerId = savedInstanceState.getLong("sellerId", 0l);
            myInventory = savedInstanceState.getBoolean("myInventory", false);
        }

        configureTheSearchBar();
        configureTheRootLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchQuery", searchQuery);
        outState.putBoolean("multiSellerSearch", multiSellerSearch);
        outState.putBoolean("customerView", customerView);
        outState.putLong("sellerId", sellerId);
        outState.putBoolean("myInventory", myInventory);

    }

    private void configureTheRootLayout() {
        rootLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configureTheSearchBar() {

        floatingSearchView.showIcon(true);
        floatingSearchView.setIcon(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_arrow_back_grey600_24dp));
        floatingSearchView.setOnMenuItemClickListener(this);
        floatingSearchView.setOnIconClickListener(new FloatingSearchView.OnIconClickListener() {
            @Override
            public void onNavigationClick() {
                onBackPressed();
            }
        });
        floatingSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence query, int start, int before, int count) {
                showClearButton(query.length() > 0 && floatingSearchView.isActivated());
                //search(query.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSearchAction(CharSequence text) {
                searchQuery = text.toString();
                if(searchTimestamp==0 || searchTimestamp>500) {
                    loadTheSearchResults();
                }
                searchTimestamp = new Date().getTime();
            }
        });

        floatingSearchView.setOnSearchFocusChangedListener(new FloatingSearchView.OnSearchFocusChangedListener() {
            @Override
            public void onFocusChanged(final boolean focused) {
                boolean textEmpty = floatingSearchView.getText().length() == 0;

                showClearButton(focused && !textEmpty);
                if(!focused) {
                    rootLinearLayout.requestFocus();
                }
            }
        });

        floatingSearchView.setText(searchQuery);
    }

    private void showClearButton(boolean show) {
        floatingSearchView.getMenu().findItem(R.id.menu_item_clear).setVisible(show);
    }

    private void loadTheSearchResults() {
        floatingSearchView.setActivated(false);
        if (multiSellerSearch) {
            MultiSellerSearchFragment fragment = MultiSellerSearchFragment.newQueryInstance(searchQuery);
            getSupportFragmentManager().beginTransaction().add(R.id.container_multi_seller_search_view, fragment, "multi_seller_search_frag").commit();
        } else {
            //nothing
            SingleSellerFragment fragment = SingleSellerFragment.newInstance(myInventory, customerView, sellerId);
            getSupportFragmentManager().beginTransaction().add(R.id.container_multi_seller_search_view, fragment, "single_seller_search_frag").commit();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                floatingSearchView.setText(null);
                floatingSearchView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                break;
        }
        return true;
    }
}
