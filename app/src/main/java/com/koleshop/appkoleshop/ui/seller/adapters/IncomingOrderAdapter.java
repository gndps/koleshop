package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private static final String TAG = "IncomingOrderAdapter";
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
        boolean showProgressBar = processingOrderIdsList.contains(order.getId());
        Log.d(TAG, "binding order at position " + position);
        holder.bindData(order, position, showProgressBar);
        holder.setOrderInteractionListener(this);
    }

    @Override
    public int getItemCount() {
        return ordersList==null?0:ordersList.size();
    }

    public void setOrdersList(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    @Override
    public void onDetailsButtonClicked(Long orderId) {
        Intent orderDetailsIntent = new Intent(mContext, OrderDetailsActivity.class);
        int position = findPositionInOrdersList(orderId);
        if(position>-1) {
            Order order = ordersList.get(position);
            Parcelable parcelableOrder = Parcels.wrap(order);
            orderDetailsIntent.putExtra("order", parcelableOrder);
            mContext.startActivity(orderDetailsIntent);
        }
    }

    @Override
    public void onAcceptButtonClicked(Long orderId) {
        int position = findPositionInOrdersList(orderId);
        if(position>-1) {
            Log.d(TAG, "on accept button clicked at position " + position);
            //send request to update order
            if (ordersList != null && ordersList.size() > 0) {
                Order acceptOrder = ordersList.get(position);
                acceptOrder.setStatus(OrderStatus.ACCEPTED);
                processingOrderIdsList.add(acceptOrder.getId());
                notifyItemChanged(position);
                OrdersIntentService.updateOrder(mContext, acceptOrder);
            }
        }
    }

    @Override
    public void onRejectButtonClicked(Long orderId) {
        //send request to update order
        int position = findPositionInOrdersList(orderId);
        if(position>-1) {
            Log.d(TAG, "on reject button clicked at position " + position);
            if (ordersList != null && ordersList.size() > 0) {
                Order rejectOrder = ordersList.get(position);
                rejectOrder.setStatus(OrderStatus.REJECTED);
                processingOrderIdsList.add(rejectOrder.getId());
                notifyItemChanged(position);
                OrdersIntentService.updateOrder(mContext, rejectOrder);
            }
        }
    }

    public void orderRequestComplete(Long orderId) {
        try {
            processingOrderIdsList.remove(orderId);
        } catch (Exception e) {
            Log.e(TAG, "some problem in removing processing order id from list", e);
        }
    }

    private int findPositionInOrdersList(Long orderId) {
        Log.d(TAG, "finding position of " + orderId + " in orders list...");
        int pos = 0;
        if (ordersList != null) {
            int position = 0;
            for (Order order : ordersList) {
                if(order.getId().equals(orderId)) {
                    return position;
                }
                position++;
            }
        }
        return -1;
    }

}
