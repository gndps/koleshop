package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.ui.seller.viewholders.OrderItemsListViewHolder;

/**
 * Created by Gundeep on 26/01/16.
 */
public class OrderItemsListAdapter extends RecyclerView.Adapter<OrderItemsListViewHolder> {

    Context mContext;
    private boolean customerView;
    Order order;

    public OrderItemsListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public OrderItemsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_order_details_item_seller, parent, false);
        OrderItemsListViewHolder viewHolder = new OrderItemsListViewHolder(view, mContext);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(OrderItemsListViewHolder holder, int position) {
        holder.bindData(order.getOrderItems().get(position), order.getStatus(), position, customerView);
    }

    @Override
    public int getItemCount() {
        if(order!=null && order.getOrderItems()!=null) {
            return order.getOrderItems().size();
        } else {
            return 0;
        }
    }

    public void setData(Order order, boolean customerView) {
        this.customerView = customerView;
        this.order = order;
    }
}
