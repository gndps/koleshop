package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.ui.common.activities.OrderDetailsActivity;
import com.koleshop.appkoleshop.ui.seller.viewholders.IncomingOrderViewHolder;

import java.util.List;

/**
 * Created by Gundeep on 23/01/16.
 */
public class IncomingOrderAdapter extends RecyclerView.Adapter<IncomingOrderViewHolder> {

    private List<Order> ordersList;
    private Context mContext;

    public IncomingOrderAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public IncomingOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_incoming_order_tile, parent, false);
        IncomingOrderViewHolder holder = new IncomingOrderViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(IncomingOrderViewHolder holder, int position) {
        Order order = ordersList.get(position);
        //final View itemView = holder.itemView;
        holder.bindData(order);
        holder.setDetailsButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderDetailsIntent = new Intent(mContext, OrderDetailsActivity.class);
                mContext.startActivity(orderDetailsIntent);
            }
        });
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
