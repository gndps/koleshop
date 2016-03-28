package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.listeners.KolClickListener;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 23/01/16.
 */
public class IncomingOrderViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "IncomingOrderVH";
    private Order order;
    private int position;
    private boolean showProgressBar;
    private Context mContext;
    @Bind(R.id.iv_iot_avatar)
    CircleImageView imageViewAvatar;
    @Bind(R.id.button_iot_accept)
    Button buttonAccept;
    @Bind(R.id.button_iot_reject)
    Button buttonReject;
    @Bind(R.id.button_iot_details)
    Button buttonDetails;
    @Bind(R.id.tv_iot_address)
    TextView textViewAddress;
    @Bind(R.id.tv_iot_name)
    TextView textViewName;
    @Bind(R.id.tv_iot_price)
    TextView textViewPrice;
    @Bind(R.id.tv_iot_timings)
    TextView textViewTimings;
    @Bind(R.id.iv_iot_home_delivery_pickup)
    ImageView imageViewDeliveryPickup;
    @Bind(R.id.pb_status_incoming_order_tile)
    DilatingDotsProgressBar dotsProgressBar;
    @BindDrawable(R.drawable.ic_delivery_boy_colored_circle_24dp)
    Drawable drawableHomeDelivery;
    @BindDrawable(R.drawable.ic_pickup_bag_circle_24dp)
    Drawable drawablePickup;
    private OrderInteractionListener orderInteractionListener;

    public IncomingOrderViewHolder(View itemView, Context context) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
        setupClickListeners(itemView);
    }

    public void bindData(Order order, int position, boolean showProgressBar) {
        this.order = order;
        this.position = position;
        this.showProgressBar = showProgressBar;
        if (order == null || order.getBuyerSettings() == null) {
            return;
        }

        final BuyerSettings buyerSettings = order.getBuyerSettings();
        final String buyerName = order.getAddress().getName();

        //1. load image view
        final String buyerImageUrl = buyerSettings.getImageUrl();
        if (!TextUtils.isEmpty(buyerImageUrl)) {
            Picasso.with(mContext)
                    .load(buyerImageUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(KoleshopUtils.getTextDrawable(mContext, buyerName, true))
                    .into(imageViewAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(buyerImageUrl)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(KoleshopUtils.getTextDrawable(mContext, buyerName, true))
                                    .error(KoleshopUtils.getTextDrawable(mContext, buyerName, true))
                                    .into(imageViewAvatar);
                        }
                    });
        } else if (!TextUtils.isEmpty(buyerName)) {
            imageViewAvatar.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, buyerName, true));
        }

        //2. set buyer name
        textViewName.setText(buyerName);

        //3. set delivery address in case of home delivery
        boolean pickup = false;
        if (order.isHomeDelivery()) {
            //home delivery
            textViewAddress.setText(order.getAddress().getName() + "\n" + order.getAddress().getAddress() + "\nPh. " + order.getAddress().getPhoneNumber());
        } else {
            //pickup...address textview should be empty in this case
            pickup = true;
        }

        //4. set bill amount
        textViewPrice.setText(CommonUtils.getPriceStringFromFloat(order.getAmountPayable(), true));

        //5. set delivery timings
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

            time = day + CommonUtils.getSimpleTimeString(order.getRequestedDeliveryTime());

            //append pickup if applicable
            /*if (pickup) {
                time += " Pickup";
            }*/
        }
        textViewTimings.setText(time);

        //6. set delivery or pickup
        if(order.isHomeDelivery()) {
            imageViewDeliveryPickup.setImageDrawable(drawableHomeDelivery);
        } else {
            imageViewDeliveryPickup.setImageDrawable(drawablePickup);
        }

        if(showProgressBar) {
            dotsProgressBar.show();
            dotsProgressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "will show progress bar at position " + position);
        } else {
            dotsProgressBar.setVisibility(View.GONE);
            Log.d(TAG, "wont show progress bar at position " + position);
        }

    }

    public void setupClickListeners(View itemView) {
        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderInteractionListener.onAcceptButtonClicked(order.getId());
            }
        });
        buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderInteractionListener.onRejectButtonClicked(order.getId());
            }
        });
        buttonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderInteractionListener.onDetailsButtonClicked(order.getId());
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderInteractionListener.onDetailsButtonClicked(order.getId());
            }
        });
    }

    public void setOrderInteractionListener(OrderInteractionListener orderInteractionListener) {
        this.orderInteractionListener = orderInteractionListener;
    }

    public interface OrderInteractionListener {
        void onDetailsButtonClicked(Long orderId);
        void onAcceptButtonClicked(Long orderId);
        void onRejectButtonClicked(Long orderId);
    }
}
