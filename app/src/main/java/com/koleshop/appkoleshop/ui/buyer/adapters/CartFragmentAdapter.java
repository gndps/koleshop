package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.singletons.CartsSingleton;
import com.koleshop.appkoleshop.ui.buyer.viewholders.CartFragmentViewHolder;
import com.koleshop.appkoleshop.util.CartUtils;

/**
 * Created by Gundeep on 04/03/16.
 */
public class CartFragmentAdapter extends RecyclerView.Adapter<CartFragmentViewHolder> implements CartFragmentViewHolder.CartFragmentAdapterViewHolderListener {

    private static final String TAG = "CartFragmentAdapter";
    private Cart cart;
    private final SellerSettings sellerSettings;
    private final Context mContext;
    private final CartFragmentAdapterListener mListener;

    public CartFragmentAdapter(Context context, Cart cart, CartFragmentAdapterListener listener) {
        this.cart = cart;
        this.mContext = context;
        this.mListener = listener;
        this.sellerSettings = cart.getSellerSettings();
    }

    @Override
    public CartFragmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_cart_item, parent, false);

        CartFragmentViewHolder holder = new CartFragmentViewHolder(view, mContext, sellerSettings);
        Log.d(TAG, "creating cart frag view holder");
        return holder;
    }

    @Override
    public void onBindViewHolder(CartFragmentViewHolder holder, int position) {

        holder.bindData(cart.getProductVarietyCountList().get(position), position, this);
    }

    @Override
    public int getItemCount() {
        return cart != null ? cart.getProductVarietyCountList().size() : 0;
    }

    @Override
    public void onItemCountPlusClicked(int position) {
        CartUtils.increaseCount(cart.getProductVarietyCountList().get(position), sellerSettings);
        notifyItemChanged(position);
        mListener.updateBill();
    }

    @Override
    public void onItemCountMinusClicked(int position) {
        ProductVarietyCount productVarietyCount = cart.getProductVarietyCountList().get(position);
        int oldCount = productVarietyCount.getCartCount();
        int numberOfItemsInCart = cart.getProductVarietyCountList().size();
        CartUtils.decreaseCount(productVarietyCount.getProductVariety(), sellerSettings);
        if(oldCount == 1) {
            //new count = 0
            if(numberOfItemsInCart == 1) {
                //no items in cart now
                Intent intentRefreshCarts = new Intent(Constants.ACTION_REFRESH_CARTS);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentRefreshCarts);
            } else {
                notifyDataSetChanged();
            }
        } else {
            notifyItemChanged(position);
        }
        mListener.updateBill();
    }

    public interface CartFragmentAdapterListener {
        void updateBill();
    }
}
