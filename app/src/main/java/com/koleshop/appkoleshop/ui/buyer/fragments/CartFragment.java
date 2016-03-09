package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.singletons.CartsSingleton;
import com.koleshop.appkoleshop.ui.buyer.activities.CartActivity;
import com.koleshop.appkoleshop.ui.buyer.activities.PlaceOrderActivity;
import com.koleshop.appkoleshop.ui.buyer.adapters.CartFragmentAdapter;
import com.koleshop.appkoleshop.util.CommonUtils;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CartFragment extends Fragment implements CartFragmentAdapter.CartFragmentAdapterListener {

    private static final String ARG_SELLER_SETTINGS = "argSellerSettings";

    @Bind(R.id.tv_cart_store_name)
    TextView textViewCartStoreName;
    @Bind(R.id.rv_fragment_cart)
    RecyclerView recyclerView;
    @Bind(R.id.button_order_cart_fragment)
    Button buttonOrder;
    @Bind(R.id.tv_bill_details_total)
    TextView textViewBillTotal;
    @Bind(R.id.rl_bill_details_not_available)
    RelativeLayout relativeLayoutBillNotAvailable;
    @Bind(R.id.tv_bill_details_not_available)
    TextView textViewNoAvailable;
    @Bind(R.id.tv_bill_details_delivery_charges)
    TextView textViewDeliveryCharges;
    @Bind(R.id.tv_bill_details_carry_bag_charges)
    TextView textViewCarryBagCharges;
    @Bind(R.id.tv_bill_details_amount_payable)
    TextView textViewAmountPayable;

    private SellerSettings sellerSettings;
    private Context mContext;
    private CartFragmentAdapter cartFragmentAdapter;
    Cart cart;

    public CartFragment() {
        // Required empty public constructor
    }

    public static CartFragment newInstance(SellerSettings sellerSettings) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SELLER_SETTINGS, Parcels.wrap(sellerSettings));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            sellerSettings = Parcels.unwrap(getArguments().getParcelable(ARG_SELLER_SETTINGS));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        ButterKnife.bind(this, view);
        initializeStuff();
        return view;
    }

    private void initializeStuff() {
        if (sellerSettings != null) {
            textViewCartStoreName.setText(sellerSettings.getAddress().getName());
            textViewCarryBagCharges.setText(CommonUtils.getPriceStringFromFloat(sellerSettings.getCarryBagCharges(), true));
            relativeLayoutBillNotAvailable.setVisibility(View.GONE);
            cart = CartsSingleton.getSharedInstance().getCart(sellerSettings);
            buttonOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlaceOrderActivity.startActivityNow(mContext, cart);
                }
            });
            if (cart != null) {
                updateBill();
                cartFragmentAdapter = new CartFragmentAdapter(mContext, cart, this);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(cartFragmentAdapter);
            }
        }
    }

    @OnClick(R.id.ib_search_in_cart_shop)
    public void searchInThisShop() {
        ((CartActivity) getActivity()).startSearch(sellerSettings);
    }

    @OnClick(R.id.ib_more_in_cart)
    public void moreInCart() {

    }

    @Override
    public void updateBill() {
        Float itemsTotalPrice = getItemsTotalPrice();
        Float deliveryCharges = 0f;
        if (itemsTotalPrice < sellerSettings.getMinimumOrder()) {
            deliveryCharges = sellerSettings.getDeliveryCharges();
        }
        Float carryBagCharges = sellerSettings.getCarryBagCharges();
        Float totalBill = itemsTotalPrice + deliveryCharges + carryBagCharges;
        textViewDeliveryCharges.setText(CommonUtils.getPriceStringFromFloat(deliveryCharges, true));
        textViewBillTotal.setText(CommonUtils.getPriceStringFromFloat(itemsTotalPrice, true));
        textViewAmountPayable.setText(CommonUtils.getPriceStringFromFloat(totalBill, true));
    }

    private Float getItemsTotalPrice() {
        Float total = 0f;
        for (ProductVarietyCount productVarietyCount : cart.getProductVarietyCountList()) {
            total += productVarietyCount.getCartCount() * productVarietyCount.getProductVariety().getPrice();
        }
        return total;
    }
}
