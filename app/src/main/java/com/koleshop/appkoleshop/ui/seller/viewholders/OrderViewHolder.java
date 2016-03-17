package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.ui.common.activities.OrderDetailsActivity;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.squareup.picasso.Picasso;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.BindDrawable;
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
    @BindDrawable(R.drawable.ic_check_grey600_24dp)
    Drawable drawableCheckGrey;
    @BindDrawable(R.drawable.ic_check_green_24dp)
    Drawable drawableCheckGreen;
    @BindDrawable(R.drawable.ic_double_check_24dp)
    Drawable drawableDoubleCheck;
    @BindDrawable(R.drawable.ic_clear_red_24dp)
    Drawable drawableClearRed;
    @BindDrawable(R.drawable.ic_delivery_boy_colored_circle_24dp)
    Drawable drawableDeliveryBoy;
    @BindDrawable(R.drawable.ic_pickup_bag_circle_24dp)
    Drawable drawablePickup;
    private Order order;


    public OrderViewHolder(View itemView, Context context, final boolean customerView) {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderDetailsIntent = new Intent(mContext, OrderDetailsActivity.class);
                Parcelable parcelableOrder = Parcels.wrap(order);
                orderDetailsIntent.putExtra("order", parcelableOrder);
                orderDetailsIntent.putExtra("customerView", customerView);
                mContext.startActivity(orderDetailsIntent);
            }
        });
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
        setOrderDetailsSellerView();
        textViewRelativeTime.setText(DateUtils.getRelativeTimeSpanString(order.getOrderTime().getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
    }

    private void setOrderDetailsSellerView() {

        Drawable statusDrawable = null;
        String orderDetailsText = null;
        if (order != null) {
            switch (order.getStatus()) {
                case OrderStatus.INCOMING:
                    //not possible...incoming orders are shown in different view
                    break;
                case OrderStatus.ACCEPTED:
                    //asap or time selected
                    if (order.isAsap()) {
                        orderDetailsText = "ASAP";
                        if (order.isHomeDelivery()) {
                            orderDetailsText += " Delivery";
                            statusDrawable = drawableDeliveryBoy;
                        } else {
                            orderDetailsText += " Pickup";
                            statusDrawable = drawablePickup;
                        }
                    } else {

                        //pickup or home delivery
                        if (order.isHomeDelivery()) {
                            orderDetailsText = "Delivery ";
                            statusDrawable = drawableDeliveryBoy;
                        } else {
                            orderDetailsText = "Pickup ";
                            statusDrawable = drawablePickup;
                        }

                        //late or in time
                        if (CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
                            //show time in minutes/hours/days
                            orderDetailsText += "late by ";
                            orderDetailsText += CommonUtils.getRelativeTime(order.getRequestedDeliveryTime());
                            textViewDetails.setTextColor(AndroidCompatUtil.getColor(mContext, R.color.cool_red));
                        } else {
                            //show time in minutes/hours or tomorrow with time
                            orderDetailsText += getHoursMinutesOrTomorrowTime(order.getRequestedDeliveryTime());
                            textViewDetails.setTextColor(AndroidCompatUtil.getColor(mContext, R.color.secondary_text));
                        }
                    }
                    break;
                case OrderStatus.REJECTED:
                    orderDetailsText = "Declined";
                    statusDrawable = drawableClearRed;
                    break;
                case OrderStatus.MISSED:
                    orderDetailsText = "Missed";
                    statusDrawable = drawableClearRed;
                    break;
                case OrderStatus.CANCELLED:
                    orderDetailsText = "Cancelled";
                    statusDrawable = drawableClearRed;
                    break;
                case OrderStatus.OUT_FOR_DELIVERY:
                    /*Date actualDeliveryTime = order.getActualDeliveryTime();
                    String timeRemainingString = "";
                    if (!CommonUtils.isTimeInPast(actualDeliveryTime)) {
                        timeRemainingString = "Delivering in ";
                    } else {
                        timeRemainingString = "Delivering late by ";
                    }
                    timeRemainingString += CommonUtils.getRelativeTime(actualDeliveryTime);*/
                    orderDetailsText = "Out for delivery";
                    statusDrawable = drawableDeliveryBoy;
                    break;
                case OrderStatus.READY_FOR_PICKUP:
                    orderDetailsText = "Ready for pickup";
                    statusDrawable = drawablePickup;
                    break;
                case OrderStatus.DELIVERED:
                    if (order.isHomeDelivery()) {
                        orderDetailsText = "Delivered";
                    } else {
                        orderDetailsText = "Picked Up";
                    }
                    statusDrawable = drawableDoubleCheck;
                    break;
                case OrderStatus.NOT_DELIVERED:
                    orderDetailsText = "Not Delivered";
                    statusDrawable = drawableClearRed;
                    break;

            }
            imageViewOrderStatus.setVisibility(View.VISIBLE);
            progressBarStatus.setVisibility(View.GONE);
            textViewDetails.setText(orderDetailsText);
            imageViewOrderStatus.setImageDrawable(statusDrawable);

        }
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
            textViewName.setText(sellerSettings.getAddress().getName() + " (" + numberOfItems + (numberOfItems > 1 ? " items)" : " item)"));
        }

        //3. set bill amount
        textViewPrice.setText(CommonUtils.getPriceStringFromFloat(order.getAmountPayable(), true));

        //4. set order details
        setOrderDetailsCustomerView();
        textViewRelativeTime.setText(DateUtils.getRelativeTimeSpanString(order.getOrderTime().getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
    }

    private void setOrderDetailsCustomerView() {
        boolean showProgressBar = false;
        Drawable statusDrawable = null;
        String orderDetailsText = null;
        if (order != null) {
            switch (order.getStatus()) {
                case OrderStatus.INCOMING:
                    orderDetailsText = "Ordering";
                    showProgressBar = true;
                    break;
                case OrderStatus.ACCEPTED:
                    orderDetailsText = "Accepted";
                    statusDrawable = drawableCheckGreen;
                    break;
                case OrderStatus.REJECTED:
                    orderDetailsText = "Declined";
                    statusDrawable = drawableClearRed;
                    break;
                case OrderStatus.MISSED:
                    orderDetailsText = "Missed";
                    statusDrawable = drawableClearRed;
                    break;
                case OrderStatus.CANCELLED:
                    orderDetailsText = "Cancelled";
                    statusDrawable = drawableClearRed;
                    break;
                case OrderStatus.OUT_FOR_DELIVERY:
                    Date actualDeliveryTime = order.getActualDeliveryTime();
                    String timeRemainingString = "";
                    if (!CommonUtils.isTimeInPast(actualDeliveryTime)) {
                        timeRemainingString = "Delivering in ";
                        timeRemainingString += CommonUtils.getRelativeTime(actualDeliveryTime);
                    } else {
                        timeRemainingString = "Delivering";
                    }
                    orderDetailsText = timeRemainingString;
                    statusDrawable = drawableDeliveryBoy;
                    break;
                case OrderStatus.READY_FOR_PICKUP:
                    orderDetailsText = "Ready for pickup";
                    statusDrawable = drawablePickup;
                    break;
                case OrderStatus.DELIVERED:
                    if (order.isHomeDelivery()) {
                        orderDetailsText = "Delivered";
                    } else {
                        orderDetailsText = "Picked Up";
                    }
                    statusDrawable = drawableDoubleCheck;
                    break;
                case OrderStatus.NOT_DELIVERED:
                    orderDetailsText = "Not Delivered";
                    statusDrawable = drawableClearRed;
                    break;

            }
            if (!showProgressBar) {
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
        if (minutes >= 60) {
            hours = minutes / 60;
            minutes = minutes % 60;
        }
        if (hours > 0 && minutes > 30) {
            hours += 1;
        }
        if (hours > 0) {
            return hours + " hours left";
        } else {
            return minutes + " minutes left";
        }

    }

    private String getHoursMinutesOrTomorrowTime(Date date) {
        String timeString = "";
        //get today or tomorrow here
        Calendar cal = Calendar.getInstance();
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);
        cal.setTime(new Date());
        int dateToday = cal.get(Calendar.DAY_OF_YEAR);
        int yearToday = cal.get(Calendar.YEAR);
        int dateDate = calendarDate.get(Calendar.DAY_OF_YEAR);
        int yearDate = calendarDate.get(Calendar.YEAR);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        int dateTomorrow = cal.get(Calendar.DAY_OF_YEAR);
        int yearTomorrow = cal.get(Calendar.YEAR);
        if (dateToday == dateDate && yearToday == yearDate) {
            //this is today...return hours/minutes
            timeString = "in " + CommonUtils.getRelativeTime(date);
        } else if (dateTomorrow == dateDate && yearTomorrow == yearDate) {
            //the given date is tomorrow
            timeString = "Tomorrow " + CommonUtils.getSimpleTimeString(order.getRequestedDeliveryTime());
        } else {
            timeString = CommonUtils.getSimpleTimeString(order.getRequestedDeliveryTime()) + CommonUtils.getDateStringInFormat(order.getRequestedDeliveryTime(), " d MMM");
        }
        return timeString;
    }

    public void bindHeader(String headerTitle) {
        textViewHeader.setText(headerTitle);
    }

}
