package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 23/01/16.
 */
public class IncomingOrderViewHolder extends RecyclerView.ViewHolder {
    private Object order;
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

    public IncomingOrderViewHolder(View itemView, Context context) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Order order) {
        this.order = order;
        if (order == null || order.getBuyerSettings() == null) {
            return;
        }

        BuyerSettings buyerSettings = order.getBuyerSettings();

        //1. load image view

        String buyerImageUrl = buyerSettings.getImageUrl();
        if (buyerImageUrl != null && !buyerImageUrl.isEmpty()) {
            Picasso.with(mContext)
                    .load(buyerImageUrl)
                    .into(imageViewAvatar);
        }

        //2. set buyer name
        textViewName.setText(buyerSettings.getName());

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

            time = day + CommonUtils.getDateStringInFormat(order.getRequestedDeliveryTime(), "h:mm a");
            if (time.endsWith(":0")) {
                time = day + CommonUtils.getDateStringInFormat(order.getRequestedDeliveryTime(), "h a");
            }

            //append pickup if applicable
            if (pickup) {
                time += " Pickup";
            }
        }
        textViewTimings.setText(time);

    }

    public void setDetailsButtonClickListener(View.OnClickListener clickListener) {
        buttonDetails.setOnClickListener(clickListener);
    }
}
