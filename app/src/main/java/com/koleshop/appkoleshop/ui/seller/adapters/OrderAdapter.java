package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.ui.seller.viewholders.OrderViewHolder;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 23/01/16.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    private List<Order> ordersList;
    private Context mContext;
    private boolean showDayHeaders;
    List<LineItem> mItems;

    private static int VIEW_TYPE_HEADER = 0x00;
    private static int VIEW_TYPE_ORDER = 0x01;

    public OrderAdapter(Context mContext, boolean showDayHeaders) {
        this.mContext = mContext;
        this.showDayHeaders = showDayHeaders;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(com.koleshop.appkoleshop.R.layout.view_list_header, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_order_tile, parent, false);
        }

        OrderViewHolder holder = new OrderViewHolder(view, mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {

        if(showDayHeaders) {
            LineItem item = mItems.get(position);
            final View itemView = holder.itemView;

            if(item.isHeader) {
                holder.bindHeader(item.text);
            } else {
                holder.bindData(item.order);
            }

            //sticky header shit
            final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
            // Overrides xml attrs, could use different layouts too.
            if (item.isHeader) {
                lp.headerDisplay = LayoutManager.LayoutParams.HEADER_INLINE;
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.headerEndMarginIsAuto = true;
                lp.headerStartMarginIsAuto = true;
            }
            lp.setSlm(LinearSLM.ID);
            //lp.setColumnWidth(96);
            lp.setFirstPosition(item.sectionFirstPosition);
            itemView.setLayoutParams(lp);

        } else {
            holder.bindData(ordersList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if(showDayHeaders) {
            return mItems == null ? 0 : mItems.size();
        } else {
            return ordersList == null ? 0 : ordersList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(showDayHeaders && mItems.get(position).isHeader) {
                return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ORDER;
        }
    }

    public void setOrdersList(List<Order> ordersList) {
        this.ordersList = ordersList;

        if(showDayHeaders) {
            //show headers like "Today" or "Yesterday"
            mItems = new ArrayList<>();
            //Insert headers into list of items.
            String lastHeader = "";
            int headerCount = 0;
            int sectionFirstPosition = 0;
            for (int i = 0; i < ordersList.size(); i++) {
                String header = CommonUtils.getDayCommonName(ordersList.get(i).getRequestedDeliveryTime());
                if (!TextUtils.equals(lastHeader, header)) {
                    // Insert new header view and update section data.
                    sectionFirstPosition = i + headerCount;
                    lastHeader = header;
                    headerCount += 1;
                    mItems.add(new LineItem(header, true, sectionFirstPosition, null));
                }
                mItems.add(new LineItem("", false, sectionFirstPosition, ordersList.get(i)));
            }
        }

        notifyDataSetChanged();
    }

    private static class LineItem {

        public int sectionFirstPosition;
        public boolean isHeader;
        public String text;
        Order order;


        public LineItem(String text, boolean isHeader, int sectionFirstPosition, Order order) {

            this.isHeader = isHeader;
            this.text = text;
            this.sectionFirstPosition = sectionFirstPosition;
            this.order = order;

        }

    }
}
