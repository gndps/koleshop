package com.koleshop.appkoleshop.ui.seller.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.services.SellerIntentService;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DummyHomeFragment extends Fragment {

    boolean buyerMode;
    @Nullable
    @Bind(R.id.tv_df_sales)
    TextView textViewSales;
    @Nullable
    @Bind(R.id.tv_df_no_sales)
    TextView textViewNoSales;
    @Nullable
    @Bind(R.id.ll_fdh)
    LinearLayout linearLayout;

    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    SwitchCompat switchOpenClose;
    ProgressBar progressBarOpenClose;
    boolean dontSendToggleRequest;
    SellerSettings sellerSettings;

    public DummyHomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dummy_home, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        //here is your list array
        if (bundle != null) {
            buyerMode = bundle.getBoolean("buyerMode");
            sellerSettings = Parcels.unwrap(bundle.getParcelable("sellerSettings"));
        }
        setupView();
        mContext = getActivity();
        initializeBroadcastReceiver();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(com.koleshop.appkoleshop.R.menu.menu_seller_home, menu);
        LinearLayout switchLayout = (LinearLayout) menu.findItem(R.id.menu_shop_switch).getActionView();
        switchOpenClose = (SwitchCompat) switchLayout.findViewById(R.id.switch_shop_open_close);
        progressBarOpenClose = (ProgressBar) switchLayout.findViewById(R.id.progress_bar_shop_open_close);
        if(sellerSettings!=null) {
            switchOpenClose.setChecked(sellerSettings.isShopOpen());
            if (!sellerSettings.isShopOpen()) {
                switchOpenClose.setText("Close ");
            }
        }
        switchOpenClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!dontSendToggleRequest) {
                    setShopToggleProcessing(true, "");
                    SellerIntentService.startActionToggleStatus(mContext, isChecked);
                } else {
                    dontSendToggleRequest = false;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SHOP_STATUS_UPDATED_SUCCESS));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_SHOP_STATUS_UPDATED_FAILED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void setShopToggleProcessing(boolean shopToggleProcessing, String toggleButtonTitle) {
        if (shopToggleProcessing) {
            switchOpenClose.setText("");
            progressBarOpenClose.setVisibility(View.VISIBLE);
        } else {
            switchOpenClose.setText(toggleButtonTitle);
            progressBarOpenClose.setVisibility(View.GONE);
        }
    }

    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.ACTION_SHOP_STATUS_UPDATED_SUCCESS.equalsIgnoreCase(intent.getAction())) {
                    boolean isChecked = intent.getBooleanExtra("isChecked", false);
                    if (isChecked) {
                        setShopToggleProcessing(false, "Open ");
                    } else {
                        setShopToggleProcessing(false, "Close ");
                    }
                } else if (Constants.ACTION_SHOP_STATUS_UPDATED_FAILED.equalsIgnoreCase(intent.getAction())) {
                    boolean isChecked = intent.getBooleanExtra("isChecked", false);
                    dontSendToggleRequest = true;
                    if (isChecked) {
                        setShopToggleProcessing(false, "Close ");
                        switchOpenClose.setChecked(false);
                    } else {
                        setShopToggleProcessing(false, "Open ");
                        switchOpenClose.setChecked(true);
                    }
                    Snackbar.make(linearLayout, "Some problem in " + (isChecked?"opening":"closing") + " shop", Snackbar.LENGTH_LONG).show();
                }
            }
        };
    }

    private void setupView() {
        if (buyerMode) {
            if (textViewNoSales != null) {
                textViewNoSales.setText("No expenses data available");
            }
            if (textViewSales != null) {
                textViewSales.setText("Expenses");
            }
        }
    }


}
