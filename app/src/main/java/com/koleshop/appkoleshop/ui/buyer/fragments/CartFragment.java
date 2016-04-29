package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CartFragment extends Fragment implements CartFragmentAdapter.CartFragmentAdapterListener {

    private static final String ARG_SELLER_SETTINGS = "argSellerSettings";

    @BindView(R.id.tv_cart_store_name)
    TextView textViewCartStoreName;
    @BindView(R.id.rv_fragment_cart)
    RecyclerView recyclerView;
    @BindView(R.id.button_order_cart_fragment)
    Button buttonOrder;
    @BindView(R.id.tv_bill_details_total)
    TextView textViewBillTotal;
    @BindView(R.id.rl_bill_details_not_available)
    RelativeLayout relativeLayoutBillNotAvailable;
    @BindView(R.id.tv_bill_details_not_available)
    TextView textViewNoAvailable;
    @BindView(R.id.tv_bill_details_delivery_charges)
    TextView textViewDeliveryCharges;
    @BindView(R.id.tv_bill_details_carry_bag_charges)
    TextView textViewCarryBagCharges;
    @BindView(R.id.tv_bill_details_amount_payable)
    TextView textViewAmountPayable;
    @BindView(R.id.ib_more_in_cart)
    ImageButton clearCartButton;
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
    public void showPopUpMenu() {
        PopupMenu popup = new PopupMenu(getActivity(),clearCartButton);
        popup.getMenuInflater().inflate(R.menu.popup_menu_clear_cart, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CartUtils.clearCart(cart);
                ((CartActivity) getActivity()).loadCarts();
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void updateBill() {
        Float itemsTotalPrice = KoleshopUtils.getItemsTotalPrice(cart.getProductVarietyCountList());
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
}
