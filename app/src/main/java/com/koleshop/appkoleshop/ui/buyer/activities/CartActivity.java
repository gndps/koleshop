package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.singletons.CartsSingleton;
import com.koleshop.appkoleshop.ui.buyer.fragments.CartFragment;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.CommonUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class CartActivity extends AppCompatActivity {

    public static final int VIEW_FLIPPER_CHILD_NO_ITEMS = 0x00;
    public static final int VIEW_FLIPPER_CHILD_RECYCLER_VIEW = 0x01;

    @Bind(R.id.appbar_cart_activity)
    Toolbar toolbar;
    @Bind(R.id.ll_activity_cart)
    LinearLayout linearLayout;
    @BindString(R.string.navigation_drawer_cart)
    String titleCart;
    @Bind(R.id.vf_cart_activity)
    ViewFlipper viewFlipper;


    Context mContext;
    List<Cart> carts;
    BroadcastReceiver mBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        mContext = this;
        ButterKnife.bind(this);
        setupToolbar();
        initializeBroadcastReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_REFRESH_CARTS));
        loadCarts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBroadcastReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.koleshop.appkoleshop.R.menu.menu_cart_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == com.koleshop.appkoleshop.R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_item_search:
                //open search overlay activity
                CartUtils.clearAllCarts();
                loadCarts();

                /*View menuView = findViewById(R.id.menu_item_search);
                //revealSearchBar(menuView, true);
                startActivity(SearchActivity.newMultiSellerSearch(mContext));
                return true;
*/
        }
        return false;
    }

    private void setupToolbar() {
        toolbar.setTitle(titleCart);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        CommonUtils.getActionBarTextView(toolbar).setTypeface(typeface);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }
    }

    private void initializeBroadcastReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction()!=null) {
                    switch (intent.getAction()) {
                        case Constants.ACTION_REFRESH_CARTS:
                            loadCarts();
                            break;
                        default:
                            break;
                    }
                }
            }
        };
    }

    private void loadCarts() {
        carts = CartsSingleton.getSharedInstance().getCarts();
        if (carts != null && carts.size()>0) {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_RECYCLER_VIEW);
        } else {
            viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_NO_ITEMS);
            return;
        }
        String FRAG_TAG_PREFIX = "CartFrag_";
        int position = 0;
        boolean firstFrag = true;
        for (Cart cart : carts) {
            //add refreshed fragment
            CartFragment cartFragment = CartFragment.newInstance(cart.getSellerSettings());
            if(firstFrag) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.ll_activity_cart, cartFragment, FRAG_TAG_PREFIX + position)
                        .commitAllowingStateLoss();
                firstFrag = false;
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.ll_activity_cart, cartFragment, FRAG_TAG_PREFIX + position)
                        .commitAllowingStateLoss();
            }
            position++;
        }
    }

    public void startSearch(SellerSettings sellerSettings) {
        startActivity(SearchActivity.newSingleSellerSearch(mContext, sellerSettings));
    }

    /*public void showCartMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.actions);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.archive:
                archive(item);
                return true;
            case R.id.delete:
                delete(item);
                return true;
            default:
                return false;
        }
    }*/
}
