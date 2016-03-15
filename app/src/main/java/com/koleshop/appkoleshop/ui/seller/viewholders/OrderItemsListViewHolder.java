package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.ui.common.views.ItemCountView;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

/**
 * Created by Gundeep on 26/01/16.
 */
public class OrderItemsListViewHolder extends RecyclerView.ViewHolder implements ItemCountView.ItemCountListener {

    private static final String TAG = "OrderItemsListViewHld";
    @Bind(R.id.tv_vodis_name)
    TextView textViewName;
    @Bind(R.id.tv_requested_item_count_vodis)
    TextView textViewCount;
    @Bind(R.id.tv_unit_price_vodis)
    TextView textViewUnitPrice;
    @Bind(R.id.tv_vodis_price)
    TextView textViewPrice;
    @Bind(R.id.item_count_view_vodis)
    ItemCountView itemCountView;
    @Bind(R.id.iv_vodis_status)
    ImageView imageViewStatus;
    @Bind(R.id.rl_vodis)
    RelativeLayout relativeLayout;
    @Bind(R.id.tv_delivered_count)
    TextView textViewDeliveredCount;
    @BindDrawable(R.drawable.ic_double_check_24dp)
    Drawable drawableCheckGreen;
    @BindDrawable(R.drawable.ic_clear_red_24dp)
    Drawable drawableClearRed;
    @BindDrawable(R.drawable.ic_check_grey600_24dp)
    Drawable drawableCheckGrey;

    private Context mContext;
    private OrderItem orderItem;
    private int orderStatus;
    private int position;

    public OrderItemsListViewHolder(View itemView, Context context) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(OrderItem orderItem, int status, int position) {
        this.orderItem = orderItem;
        this.orderStatus = status;
        this.position = position;
        textViewName.setText(orderItem.getBrand() + " - " + orderItem.getName() + " " + orderItem.getQuantity());
        textViewCount.setText(orderItem.getOrderCount() + "");
        textViewUnitPrice.setText(CommonUtils.getPriceStringFromFloat(orderItem.getPricePerUnit(), true));
        switch (orderStatus) {
            case OrderStatus.INCOMING:
                itemCountView.setVisibility(View.GONE);
                imageViewStatus.setVisibility(View.GONE);
                textViewPrice.setText(CommonUtils.getPriceStringFromFloat(orderItem.getPricePerUnit() * orderItem.getOrderCount(), true));
                adjustHeightOfView();
                break;
            case OrderStatus.ACCEPTED:
                int orderCount = orderItem.getOrderCount();
                int availableCount = orderItem.getAvailableCount();
                itemCountView.setMaximumCount(orderCount);
                itemCountView.setCount(availableCount);
                if(orderCount == availableCount) {
                    imageViewStatus.setImageDrawable(drawableCheckGreen);
                } else if (availableCount > 0 && availableCount < orderCount) {
                    imageViewStatus.setImageDrawable(drawableCheckGrey);
                } else if (availableCount == 0) {
                    imageViewStatus.setImageDrawable(drawableClearRed);
                }
                textViewPrice.setText(CommonUtils.getPriceStringFromFloat(orderItem.getPricePerUnit() * orderItem.getAvailableCount(), true));
                itemCountView.setItemCountListener(this);
                break;
            default:
                itemCountView.setCount(orderItem.getAvailableCount());
                itemCountView.setVisibility(View.GONE);
                textViewDeliveredCount.setText(orderItem.getAvailableCount() + "");
                textViewDeliveredCount.setVisibility(View.VISIBLE);
                textViewPrice.setText(CommonUtils.getPriceStringFromFloat(orderItem.getPricePerUnit() * orderItem.getAvailableCount(), true));
                int orderCount2 = orderItem.getOrderCount();
                int availableCount2 = orderItem.getAvailableCount();
                if(orderCount2 == availableCount2) {
                    imageViewStatus.setImageDrawable(drawableCheckGreen);
                } else if (availableCount2 > 0 && availableCount2 < orderCount2) {
                    imageViewStatus.setImageDrawable(drawableCheckGrey);
                } else if (availableCount2 == 0) {
                    imageViewStatus.setImageDrawable(drawableClearRed);
                }
                adjustHeightOfView();
                break;
        }
    }

    private void adjustHeightOfView() {
        ViewGroup.LayoutParams layoutParams = relativeLayout.getLayoutParams();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, mContext.getResources().getDisplayMetrics());
        layoutParams.height = height; //set height to 64dp
    }

    @Override
    public void onItemCountPlusClicked() {
        Log.d(TAG, "item plus clicked");
        Intent intentCountPlus =new Intent(Constants.ACTION_ORDER_ITEM_COUNT_PLUS);
        intentCountPlus.putExtra("position", position);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentCountPlus);
    }

    @Override
    public void onItemCountMinusClicked() {
        Log.d(TAG, "item minus clicked");
        Intent intentCountMinus =new Intent(Constants.ACTION_ORDER_ITEM_COUNT_MINUS);
        intentCountMinus.putExtra("position", position);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentCountMinus);
    }
}
