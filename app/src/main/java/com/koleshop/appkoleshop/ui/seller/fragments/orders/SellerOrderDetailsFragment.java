package com.koleshop.appkoleshop.ui.seller.fragments.orders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;

import org.parceler.Parcels;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SellerOrderDetailsFragment extends Fragment {

    @Bind(R.id.tv_title_seller_order_details_fragment)
    TextView textViewTitle;
    @Bind(R.id.iv_title_seller_order_details_fragment)
    ImageView imageViewTitleSellerOrderDetailsFragment;
    @Bind(R.id.textview_price_seller_order_details_fragment)
    TextView textViewOrderPrice;
    @Bind(R.id.vf_seller_order_details)
    ViewFlipper viewFlipperHeadline;
    @Bind(R.id.textview_headline_description_fragment_seller_order_details)
    TextView textViewHeadlineDescription;
    @Bind(R.id.textview_headline_fragment_seller_order_details)
    TextView textViewHeadline;
    @Bind(R.id.iv_order_complete_status_fsod)
    ImageView imageViewOrderStatus;
    @Bind(R.id.tv_order_complete_fsod)
    TextView textViewOrderStatus;
    @Bind(R.id.tv_address_fsod)
    TextView textViewAddress;
    @Bind(R.id.tv_order_time_title_fsod)
    TextView textViewOrderTimeTitle;
    @Bind(R.id.tv_order_time_fsod)
    TextView textViewOrderTime;
    @Bind(R.id.tv_delivery_time_title_fsod)
    TextView textViewDeliveryTimeTitle;
    @Bind(R.id.tv_delivery_time_fsod)
    TextView textViewDeliveryTime;
    @Bind(R.id.button_cancel_fsod)
    Button buttonCancel;
    @Bind(R.id.button_delivery_fsod)
    Button buttonDelivery;

    public static int VIEW_FLIPPER_CHILD_ORDER_TIME_REMAINING = 0;
    public static int VIEW_FLIPPER_CHILD_ORDER_STATUS = 1;

    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    Order order;

    public SellerOrderDetailsFragment() {
        // Required empty public constructor
    }

    public static SellerOrderDetailsFragment newInstance() {
        SellerOrderDetailsFragment fragment = new SellerOrderDetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_order_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        lbm.registerReceiver(mBroadcastReceiver, new IntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    private void refreshThisFragment() {

        //set title
        if(!order.isHomeDelivery()) {
            textViewTitle.setText("Pickup");
            imageViewTitleSellerOrderDetailsFragment.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_pickup_bag_circle_background));
            textViewDeliveryTimeTitle.setText("PICKUP TIME");
            textViewHeadlineDescription.setText("PICKUP IN");
        } else {
            textViewTitle.setText("Home Delivery");
            imageViewTitleSellerOrderDetailsFragment.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_delivery_boy_colored_background_circle));
            textViewDeliveryTimeTitle.setText("DELIVERY TIME");
            textViewHeadlineDescription.setText("DELIVERY IN");
        }

        //set price
        textViewOrderPrice.setText(CommonUtils.getPriceStringFromFloat(order.getAmountPayable(), true));

        //set address
        String address = order.getBuyerSettings().getName();
        address += "\n" + order.getAddress().getAddress();
        address += "\nPh." + order.getAddress().getPhoneNumber();
        textViewAddress.setText(address);

        //set time
        String orderTime = CommonUtils.getDateStringInFormat(order.getOrderTime(), "h:mm a") + " " + DateUtils.getRelativeTimeSpanString(order.getOrderTime().getTime(), new java.util.Date().getTime(), DateUtils.DAY_IN_MILLIS, 0).toString();
        textViewOrderTime.setText(orderTime);

        //set delivery / pickup time
        String deliveryPickupTime = CommonUtils.getDateStringInFormat(order.getRequestedDeliveryTime(), "h:mm a") + " " + DateUtils.getRelativeTimeSpanString(order.getRequestedDeliveryTime().getTime(), new java.util.Date().getTime(), DateUtils.DAY_IN_MILLIS, 0).toString();
        if(order.isAsap()) {
            deliveryPickupTime = "ASAP";
        }
        textViewDeliveryTime.setText(deliveryPickupTime);
        if(CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
            textViewHeadlineDescription.setText("LATE BY");
        }

        //set delivery relative time and buttons
        String titleString;
        switch (order.getStatus()) {
            case OrderStatus.INCOMING:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_TIME_REMAINING);
                if(order.isAsap() && !CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
                    titleString = "ASAP";
                } else {
                    titleString = CommonUtils.getRelativeTime(order.getRequestedDeliveryTime());
                }
                textViewHeadline.setText(titleString);
                setupAcceptAndRejectButtons();
                break;
            case OrderStatus.ACCEPTED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_TIME_REMAINING);
                if(order.isAsap() && !CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
                    titleString = "ASAP";
                } else {
                    titleString = CommonUtils.getRelativeTime(order.getRequestedDeliveryTime());
                }
                textViewHeadline.setText(titleString);
                setupDeliverButton();
                break;
            case OrderStatus.REJECTED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.cross));
                textViewOrderStatus.setText("Rejected");
                hideButtons();
                break;
            case OrderStatus.MISSED:
            case OrderStatus.CANCELLED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.cross));
                textViewOrderStatus.setText("Cancelled");
                hideButtons();
                break;
            case OrderStatus.OUT_FOR_DELIVERY:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.check));
                textViewOrderStatus.setText("Out for delivery");
                setupNotDeliveredButton();
                break;
            case OrderStatus.READY_FOR_PICKUP:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.check));
                textViewOrderStatus.setText("Ready for pickup");
                setupNotDeliveredButton();
                break;
            case OrderStatus.DELIVERED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.check));
                textViewOrderStatus.setText("Delivered");
                hideButtons();
                break;
            case OrderStatus.NOT_DELIVERED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.cross));
                textViewOrderStatus.setText("Not Delivered");
                hideButtons();
                break;
        }

    }

    private void setupNotDeliveredButton() {
    }

    private void hideButtons() {

    }

    private void setupDeliverButton() {

    }

    private void setupAcceptAndRejectButtons() {
        buttonDelivery.setText("ACCEPT");
        buttonCancel.setText("REJECT");
        buttonDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show progress bar
            }
        });
    }

    public void setOrder(Order order) {
        this.order = order;
        refreshThisFragment();
    }
}
