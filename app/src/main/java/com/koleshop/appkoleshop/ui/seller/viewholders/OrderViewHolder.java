package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.squareup.picasso.Picasso;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 23/01/16.
 */
public class OrderViewHolder extends RecyclerView.ViewHolder {

    Context mContext;
    private boolean customerView;

    @Nullable
    @Bind(R.id.iv_ot_avatar)
    CircleImageView imageViewAvatar;
    @Nullable
    @Bind(R.id.tv_ot_name)
    TextView textViewName;
    @Nullable
    @Bind(R.id.tv_ot_details)
    TextView textViewDetails;
    @Nullable
    @Bind(R.id.tv_ot_price)
    TextView textViewPrice;
    @Nullable
    @Bind(R.id.iv_ot_order_status)
    ImageView imageViewOrderStatus;
    @Nullable
    @Bind(R.id.tv_list_header)
    TextView textViewHeader;
    @Nullable
    @Bind(R.id.tv_relative_time_vot)
    TextView textViewRelativeTime;
    @Nullable
    @Bind(R.id.pb_ot_order_status)
    DilatingDotsProgressBar progressBarStatus;
    private Order order;


    public OrderViewHolder(View itemView, Context context, boolean customerView) {
        super(itemView);
        mContext = context;
        this.customerView = customerView;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Order order) {
        this.order = order;
        if (order == null || order.getBuyerSettings() == null || order.getSellerSettings() == null) {
            return;
        }

        if (!customerView) {
            loadDataWithSellerView();
        } else {
            loadDataWithCustomerView();
        }

    }

    private void loadDataWithSellerView() {
        BuyerSettings buyerSettings = order.getBuyerSettings();
        String buyerImageUrl = buyerSettings.getImageUrl();

        //1. load image view
        if (!TextUtils.isEmpty(buyerImageUrl)) {
            Picasso.with(mContext)
                    .load(buyerImageUrl)
                    .into(imageViewAvatar);
        } else if (!TextUtils.isEmpty(buyerSettings.getName())) {
            imageViewAvatar.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, buyerSettings.getName(), true));
        }

        //2. set buyer name
        textViewName.setText(buyerSettings.getName());

        //3. set bill amount
        textViewPrice.setText(CommonUtils.getPriceStringFromFloat(order.getAmountPayable(), true));

        //4. set delivery details
        boolean pickup = false;
        if (order.isHomeDelivery()) {
            pickup = true;
        }
        String time = "";
        if (order.isAsap()) {
            time = "ASAP";
        } else {
            //get today or tomorrow here
            String day = "";
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            int dateToday = cal.get(Calendar.DAY_OF_MONTH);
            Date orderDeliveryTime = order.getRequestedDeliveryTime();
            cal.setTime(orderDeliveryTime);
            int orderDate = cal.get(Calendar.DAY_OF_MONTH);
            if (orderDate == dateToday) {
                day = "";
            } else {
                day = "Tomorrow ";
            }

            time = day + CommonUtils.getDateStringInFormat(order.getRequestedDeliveryTime(), "h:mm a");
            if (time.endsWith(":00")) {
                time = day + CommonUtils.getDateStringInFormat(order.getRequestedDeliveryTime(), "h a");
            }

            //append pickup if applicable
            if (pickup) {
                time += " Pickup";
            } else {
                time += " Home delivery";
            }
        }
        textViewDetails.setText(time);
    }

    private void loadDataWithCustomerView() {
        SellerSettings sellerSettings = order.getSellerSettings();
        String sellerImageUrl = sellerSettings.getImageUrl();

        //1. load image view
        if (!TextUtils.isEmpty(sellerImageUrl)) {
            Picasso.with(mContext)
                    .load(sellerImageUrl)
                    .into(imageViewAvatar);
        } else if (sellerSettings.getAddress() != null && !TextUtils.isEmpty(sellerSettings.getAddress().getName())) {
            imageViewAvatar.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, sellerSettings.getAddress().getName(), true));
        }

        //2. set seller name and number of items
        int numberOfItems = order.getOrderItems().size();
        if (sellerSettings.getAddress() != null && !TextUtils.isEmpty(sellerSettings.getAddress().getName())) {
            textViewName.setText(sellerSettings.getAddress().getName() + " (" + numberOfItems + (numberOfItems>1?" items)":" item)"));
        }

        //3. set bill amount
        textViewPrice.setText(CommonUtils.getPriceStringFromFloat(order.getAmountPayable(), true));

        //4. set order details
        setOrderDetails();
        textViewRelativeTime.setText(DateUtils.getRelativeTimeSpanString(order.getOrderTime().getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
    }

    private void setOrderDetails() {
        boolean showProgressBar = false;
        Drawable statusDrawable = null;
        String orderDetailsText = null;
        if(order!=null) {
            switch (order.getStatus()) {
                case OrderStatus.INCOMING:
                    orderDetailsText = "Ordering";
                    showProgressBar = true;
                    break;
                case OrderStatus.ACCEPTED:
                    orderDetailsText = "Accepted";
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_check_grey600_24dp);
                    break;
                case OrderStatus.REJECTED:
                    orderDetailsText = "Cancelled";
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_clear_red_24dp);
                    break;
                case OrderStatus.MISSED:
                    orderDetailsText = "Cancelled";
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_clear_red_24dp);
                    break;
                case OrderStatus.CANCELLED:
                    orderDetailsText = "Cancelled";
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_clear_red_24dp);
                    break;
                case OrderStatus.OUT_FOR_DELIVERY:
                    String timeRemainingString = getTimeRemainingString(order.getMinutesToDelivery());
                    orderDetailsText = timeRemainingString;
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_delivery_boy_colored_24dp);
                    break;
                case OrderStatus.READY_FOR_PICKUP:
                    orderDetailsText = "Ready for pickup";
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_shopping_bag_green_24dp);
                    break;
                case OrderStatus.DELIVERED:
                    if(order.isHomeDelivery()) {
                        orderDetailsText = "Delivered";
                    } else {
                        orderDetailsText = "Picked Up";
                    }
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_check_green_24dp);
                    break;
                case OrderStatus.NOT_DELIVERED:
                    orderDetailsText = "Not Delivered";
                    statusDrawable = AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_clear_red_24dp);
                    break;

            }
            if(!showProgressBar) {
                progressBarStatus.setVisibility(View.GONE);
                if (statusDrawable != null) {
                    imageViewOrderStatus.setVisibility(View.VISIBLE);
                    imageViewOrderStatus.setImageDrawable(statusDrawable);
                } else {
                    imageViewOrderStatus.setVisibility(View.GONE);
                }
            } else {
                imageViewOrderStatus.setVisibility(View.GONE);
                progressBarStatus.setVisibility(View.VISIBLE);
                progressBarStatus.show();
            }

            textViewDetails.setText(orderDetailsText);

        }
    }

    private String getTimeRemainingString(int minutesToDelivery) {
        int minutes = minutesToDelivery;
        int hours = 0;
        if(minutes>=60) {
            hours = minutes/60;
            minutes = minutes%60;
        }
        if(hours>0 && minutes>30) {
            hours+=1;
        }
        if(hours>0) {
            return hours + "hours left";
        } else {
            return minutes + "mins left";
        }

    }

    public void bindHeader(String headerTitle) {
        textViewHeader.setText(headerTitle);
    }

}
