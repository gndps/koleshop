package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.ui.seller.viewholders.IncomingProductViewHolder;

import java.util.List;

/**
 * Created by Gundeep on 23/01/16.
 */
public class IncomingProductAdapter extends RecyclerView.Adapter<IncomingProductViewHolder> {

    private List<Order> ordersList;
    private Context mContext;

    public IncomingProductAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public IncomingProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_incoming_order_tile, parent, false);
        IncomingProductViewHolder holder = new IncomingProductViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(IncomingProductViewHolder holder, int position) {
        Order order = ordersList.get(position);
        //final View itemView = holder.itemView;
        holder.bindData(order);
    }

    @Override
    public int getItemCount() {
        return ordersList==null?0:ordersList.size();
    }

    public void setOrdersList(List<Order> ordersList) {
        this.ordersList = ordersList;
        notifyDataSetChanged();
    }
}
