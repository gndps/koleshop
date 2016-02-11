package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;

/**
 * Created by Gundeep on 07/02/16.
 */
public class MultiSellerSearchViewHolder extends RecyclerView.ViewHolder {

    private SellerSearchResults results;

    public MultiSellerSearchViewHolder(View itemView) {
        super(itemView);
    }

    public void bindData(SellerSearchResults results) {

        this.results = results;
    }

}
