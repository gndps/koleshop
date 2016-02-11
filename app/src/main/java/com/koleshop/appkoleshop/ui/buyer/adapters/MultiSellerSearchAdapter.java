package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.ui.buyer.viewholders.MultiSellerSearchViewHolder;

import java.util.List;

/**
 * Created by Gundeep on 07/02/16.
 */
public class MultiSellerSearchAdapter extends RecyclerView.Adapter<MultiSellerSearchViewHolder> {

    List<SellerSearchResults> results;

    public MultiSellerSearchAdapter(List<SellerSearchResults> results) {
        this.results = results;
    }

    @Override
    public MultiSellerSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_multi_seller_search_tile, parent, false);
        MultiSellerSearchViewHolder viewHolder = new MultiSellerSearchViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MultiSellerSearchViewHolder holder, int position) {
        holder.bindData(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

}
