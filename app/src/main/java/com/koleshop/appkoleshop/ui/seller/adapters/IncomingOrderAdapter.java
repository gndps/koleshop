package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.services.OrdersIntentService;
import com.koleshop.appkoleshop.ui.common.activities.OrderDetailsActivity;
import com.koleshop.appkoleshop.ui.seller.viewholders.IncomingOrderViewHolder;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 23/01/16.
 */
public class IncomingOrderAdapter extends RecyclerView.Adapter<IncomingOrderViewHolder> implements IncomingOrderViewHolder.OrderInteractionListener {

    private List<Order> ordersList;
    private List<Long> processingOrderIdsList;
    private Context mContext;

    public IncomingOrderAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public IncomingOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_incoming_order_tile, parent, false);
        IncomingOrderViewHolder holder = new IncomingOrderViewHolder(view, mContext);
        processingOrderIdsList = new ArrayList<>();
        return holder;
    }

    @Override
    public void onBindViewHolder(IncomingOrderViewHolder holder, int position) {
        Order order = ordersList.get(position);
        //final View itemView = holder.itemView;
        holder.bindData(order, position);
        holder.setOrderInteractionListener(this);
    }

    @Override
    public int getItemCount() {
        return ordersList==null?0:ordersList.size();
    }

    public void setOrdersList(List<Order> ordersList) {
        this.ordersList = ordersList;
        notifyDataSetChanged();
    }

    @Override
    public void onDetailsButtonClicked(int position) {
        Intent orderDetailsIntent = new Intent(mContext, OrderDetailsActivity.class);
        Order order = ordersList.get(position);
        Parcelable parcelableOrder = Parcels.wrap(order);
        orderDetailsIntent.putExtra("order", parcelableOrder);
        mContext.startActivity(orderDetailsIntent);
    }

    @Override
    public void onAcceptButtonClicked(int position) {
        //send request to update order
        Order acceptOrder = ordersList.get(position);
        acceptOrder.setStatus(OrderStatus.ACCEPTED);
        processingOrderIdsList.add(acceptOrder.getId());
        notifyItemChanged(position);
        OrdersIntentService.updateOrder(mContext, acceptOrder);
    }

    @Override
    public void onRejectButtonClicked(int position) {
        processingOrderIdsList.add(ordersList.get(position).getId());
        notifyItemChanged(position);
    }
}
