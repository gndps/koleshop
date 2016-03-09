package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.Order;
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
public class OrderViewHolder extends RecyclerView.ViewHolder {

    Context mContext;

    @Nullable @Bind(R.id.iv_ot_avatar)
    CircleImageView imageViewAvatar;
    @Nullable @Bind(R.id.tv_ot_name)
    TextView textViewName;
    @Nullable @Bind(R.id.tv_ot_details)
    TextView textViewDetails;
    @Nullable @Bind(R.id.tv_ot_price)
    TextView textViewPrice;
    @Nullable @Bind(R.id.iv_ot_order_status)
    ImageView imageViewOrderStatus;
    @Nullable @Bind(R.id.tv_list_header)
    TextView textViewHeader;
    private Order order;


    public OrderViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Order order) {
        this.order = order;
        if(order==null) {
            return;
        }

        //1. load image view
        if(order.getBuyerImageUrl()!=null && !order.getBuyerImageUrl().isEmpty()) {
            Picasso.with(mContext)
                    .load(order.getSellerImageUrl())
                    .into(imageViewAvatar);
        }

        //2. set buyer name
        textViewName.setText(order.getBuyerName());

        //3. set bill amount
        textViewPrice.setText(CommonUtils.getPriceStringFromFloat(order.getTotalAmount(), true));

        //4. set delivery details
        boolean pickup = false;
        if(order.getOrderType()==0) {
            pickup = true;
        }
        String time = "";
        if(order.isAsap()) {
            time = "ASAP";
        } else {
            //get today or tomorrow here
            String day = "";
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            int dateToday =  cal.get(Calendar.DAY_OF_MONTH);
            Date orderDeliveryTime = order.getDeliveryTime();
            cal.setTime(orderDeliveryTime);
            int orderDate =  cal.get(Calendar.DAY_OF_MONTH);
            if(orderDate == dateToday) {
                day = "";
            } else {
                day = "Tomorrow ";
            }

            time = day + CommonUtils.getDateStringInFormat(order.getDeliveryTime(), "h:mm a");
            if(time.endsWith(":00")) {
                time = day + CommonUtils.getDateStringInFormat(order.getDeliveryTime(), "h a");
            }

            //append pickup if applicable
            if(pickup) {
                time += " Pickup";
            } else {
                time += " Home delivery";
            }
        }
        textViewDetails.setText(time);
    }

    public void bindHeader(String headerTitle) {
        textViewHeader.setText(headerTitle);
    }

}
