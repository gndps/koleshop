package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.ui.buyer.viewholders.MultiSellerSearchViewHolder;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;

import java.util.List;

/**
 * Created by Gundeep on 07/02/16.
 */
public class MultiSellerSearchAdapter extends RecyclerView.Adapter<MultiSellerSearchViewHolder> {

    List<SellerSearchResults> results;
    Context mContext;
    int expandedProductPosition;
    int expandedSettingsPosition;
    int oldExpandedSettingsPosition;

    public MultiSellerSearchAdapter(Context context, List<SellerSearchResults> results) {
        this.mContext = context;
        this.results = results;
        this.expandedProductPosition = -1;
        this.expandedSettingsPosition = -1;
    }

    @Override
    public MultiSellerSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_multi_seller_search_tile, parent, false);
        MultiSellerSearchViewHolder viewHolder = new MultiSellerSearchViewHolder(mContext, view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MultiSellerSearchViewHolder holder, int position) {
        boolean expandProduct = false;
        if (position == expandedSettingsPosition) {
            expandProduct = true;
        }
        holder.bindData(results.get(position), expandProduct, position, expandedProductPosition);
    }

    @Override
    public int getItemCount() {
        return results!=null?results.size():0;
    }

    public void expandProduct(boolean expand, int settingsPosition, int productPosition) {
        if(expand) {
            oldExpandedSettingsPosition = expandedSettingsPosition;
            this.expandedSettingsPosition = settingsPosition;
            this.expandedProductPosition = productPosition;
        } else {
            this.expandedProductPosition = -1;
            this.expandedSettingsPosition = -1;
        }
        notifyItemChanged(oldExpandedSettingsPosition);
        if(oldExpandedSettingsPosition!=expandedSettingsPosition) {
            notifyItemChanged(expandedSettingsPosition);
        }
    }

    public void increaseVarietyCount(int settingsPosition, int position, Long varietyId) {
        if(results!=null) {
            ProductVariety productVariety = getProductVariety(settingsPosition, position, varietyId);
            if(productVariety!=null) {
                CartUtils.increaseCount(productVariety, results.get(settingsPosition).getSellerSettings());
                notifyItemChanged(settingsPosition);
            }
        }
    }

    public void decreaseVarietyCount(int settingsPosition, int position, Long varietyId) {
        if(results!=null) {
            ProductVariety productVariety = getProductVariety(settingsPosition, position, varietyId);
            if(productVariety!=null) {
                CartUtils.decreaseCount(productVariety, results.get(settingsPosition).getSellerSettings());
                notifyItemChanged(settingsPosition);
            }
        }
    }

    private ProductVariety getProductVariety(int settingsPosition, int position, Long varietyId) {
        try {
            EditProduct editProduct = results.get(settingsPosition).getProducts().get(position);
            Product currentProduct = KoleshopUtils.getProductFromEditProduct(editProduct);
            ProductVariety productVariety = KoleshopUtils.getProductVarietyFromProduct(currentProduct, varietyId);
            return productVariety;
        } catch (Exception e) {
            return null;
        }
    }
}
