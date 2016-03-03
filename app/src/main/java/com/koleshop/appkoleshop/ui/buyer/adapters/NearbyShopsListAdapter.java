package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.ui.buyer.viewholders.NearbyShopsListViewHolder;

import java.util.List;

/**
 * Created by Gundeep on 02/02/16.
 */
public class NearbyShopsListAdapter extends RecyclerView.Adapter<NearbyShopsListViewHolder> {

    List<SellerSettings> nearbySellers;
    Context mContext;

    public NearbyShopsListAdapter(List<SellerSettings> nearbySellers, Context mContext) {
        this.nearbySellers = nearbySellers;
        this.mContext = mContext;
    }

    @Override
    public NearbyShopsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_nearby_shops_item, parent, false);
        NearbyShopsListViewHolder nearbyShopsListViewHolder = new NearbyShopsListViewHolder(view, mContext);
        return nearbyShopsListViewHolder;
    }

    @Override
    public void onBindViewHolder(NearbyShopsListViewHolder holder, int position) {
        holder.bindData(nearbySellers.get(position));
    }

    @Override
    public int getItemCount() {
        return nearbySellers!=null?nearbySellers.size():0;
    }
}
