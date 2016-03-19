package com.koleshop.appkoleshop.ui.seller.fragments.orders;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.ui.common.activities.OrderDetailsActivity;
import com.koleshop.appkoleshop.ui.seller.fragments.DeliveryTimeRemainingDialogFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OrderDetailsFragment extends Fragment {

    @Bind(R.id.tv_title_order_details_fragment)
    TextView textViewTitle;
    @Bind(R.id.iv_title_order_details_fragment)
    ImageView imageViewTitleSellerOrderDetailsFragment;
    @Bind(R.id.textview_price_order_details_fragment)
    TextView textViewOrderPrice;
    @Bind(R.id.vf_seller_details)
    ViewFlipper viewFlipperHeadline;
    @Bind(R.id.textview_headline_description_fragment_order_details)
    TextView textViewHeadlineDescription;
    @Bind(R.id.textview_headline_fragment_order_details)
    TextView textViewHeadline;
    @Bind(R.id.iv_order_complete_status_fod)
    ImageView imageViewOrderStatus;
    @Bind(R.id.tv_order_complete_fod)
    TextView textViewOrderStatus;
    @Bind(R.id.tv_address_fod)
    TextView textViewAddress;
    @Bind(R.id.tv_order_time_title_fod)
    TextView textViewOrderTimeTitle;
    @Bind(R.id.tv_order_time_fod)
    TextView textViewOrderTime;
    @Bind(R.id.tv_delivery_time_title_fod)
    TextView textViewDeliveryTimeTitle;
    @Bind(R.id.tv_delivery_time_fod)
    TextView textViewDeliveryTime;
    @Bind(R.id.button_cancel_fod)
    Button buttonCancel;
    @Bind(R.id.button_delivery_fod)
    Button buttonDelivery;
    @Bind(R.id.pb_status_order_details_fragment)
    DilatingDotsProgressBar dilatingDotsProgressBar;

    public static int VIEW_FLIPPER_CHILD_ORDER_TIME_REMAINING = 0;
    public static int VIEW_FLIPPER_CHILD_ORDER_STATUS = 1;

    Context mContext;
    BroadcastReceiver mBroadcastReceiver;
    Order order;
    boolean customerView;

    public OrderDetailsFragment() {
        // Required empty public constructor
    }

    public static OrderDetailsFragment newInstance() {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
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
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);
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

    public void setCustomerView(boolean customerView) {
        this.customerView = customerView;
    }

    public void setOrder(Order order) {
        this.order = order;
        refreshThisFragment();
    }

    private void refreshThisFragment() {

        showProgressBar(false);

        //01. Set titles
        if (order == null) {
            return;
        }
        if (!order.isHomeDelivery()) {
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

        //02. Set price
        textViewOrderPrice.setText(CommonUtils.getPriceStringFromFloat(order.getAmountPayable(), true));

        //03. Set delivery address / seller name
        if (!customerView) {
            String address = order.getBuyerSettings().getName();
            address += "\n" + order.getAddress().getAddress();
            address += "\nPh." + order.getAddress().getPhoneNumber();
            textViewAddress.setText(address);
        } else {
            textViewAddress.setText(order.getSellerSettings().getAddress().getName());
        }

        //04. Set times
        String orderTime = CommonUtils.getSimpleTimeString(order.getOrderTime()) + " " + CommonUtils.getTodayTomorrowYesterdayOrDate(order.getOrderTime()).toString();
        textViewOrderTime.setText(orderTime);

        //set delivery / pickup time
        String deliveryPickupTime = CommonUtils.getSimpleTimeString(order.getRequestedDeliveryTime()) + " " + CommonUtils.getTodayTomorrowYesterdayOrDate(order.getRequestedDeliveryTime()).toString();
        if (order.isAsap()) {
            deliveryPickupTime = "ASAP";
        }
        textViewDeliveryTime.setText(deliveryPickupTime);
        if (CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
            if (order.isAsap()) {
                textViewHeadlineDescription.setText("TIME ELAPSED");
            } else {
                textViewHeadlineDescription.setText("LATE BY");
                textViewHeadlineDescription.setTextColor(AndroidCompatUtil.getColor(mContext, R.color.cool_red));
            }
        }

        //05. Set delivery relative time and configure buttons
        //customer and seller cases are handled separately
        String titleString;
        switch (order.getStatus()) {
            case OrderStatus.INCOMING:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_TIME_REMAINING);
                if (order.isAsap() && !CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
                    titleString = "ASAP";
                } else {
                    titleString = CommonUtils.getRelativeTime(order.getRequestedDeliveryTime());
                }
                textViewHeadline.setText(titleString);
                setupIncomingButtons();
                break;
            case OrderStatus.ACCEPTED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_TIME_REMAINING);
                if (order.isAsap() && !CommonUtils.isTimeInPast(order.getRequestedDeliveryTime())) {
                    titleString = "ASAP";
                } else {
                    titleString = CommonUtils.getRelativeTime(order.getRequestedDeliveryTime());
                }
                textViewHeadline.setText(titleString);
                setupPendingButtons();
                break;
            case OrderStatus.REJECTED:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.cross));
                textViewOrderStatus.setText("Declined");
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
                hideButtons();
                //setupNotDeliveredButton();
                break;
            case OrderStatus.READY_FOR_PICKUP:
                viewFlipperHeadline.setDisplayedChild(VIEW_FLIPPER_CHILD_ORDER_STATUS);
                imageViewOrderStatus.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.check));
                textViewOrderStatus.setText("Ready for pickup");
                hideButtons();
                //setupNotDeliveredButton();
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
        buttonCancel.setVisibility(View.GONE);
        buttonDelivery.setText("NOT DELIVERED");
        buttonDelivery.setTextColor(AndroidCompatUtil.getColor(mContext, R.color.primary_text_grey));
        buttonDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Mark this order as not delivered?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                order.setStatus(OrderStatus.NOT_DELIVERED);
                                updateOrderInCloud();
                            }
                        })
                        .setNegativeButton("CANCEL", null);
                builder.create().show();
            }
        });
    }

    private void setupIncomingButtons() {
        if (!customerView) {
            buttonDelivery.setText("ACCEPT");
            buttonCancel.setText("DECLINE");
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Decline this order?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    order.setStatus(OrderStatus.REJECTED);
                                    updateOrderInCloud();
                                }
                            })
                            .setNegativeButton("NO", null);
                    builder.create().show();
                }
            });
            buttonDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //show progress bar
                    order.setStatus(OrderStatus.ACCEPTED);
                    updateOrderInCloud();
                }
            });
        } else {
            hideButtons();
            showProgressBar(true);
        }
    }

    private void setupPendingButtons() {
        if (!customerView) {
            if (order.isHomeDelivery()) {
                buttonDelivery.setText("DELIVER");
            } else {
                buttonDelivery.setText("READY FOR PICKUP");
            }
            //buttonCancel.setText("CANCEL");
            /*buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Cancel this order?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    order.setStatus(OrderStatus.CANCELLED);
                                    updateOrderInCloud();
                                }
                            })
                            .setNegativeButton("NO", null);
                    builder.create().show();
                }
            });*/
            buttonCancel.setVisibility(View.GONE);
            buttonDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateChecklist()) {
                        if (order.isHomeDelivery()) {
                            //show delivery time popup
                            DeliveryTimeRemainingDialogFragment dialog = DeliveryTimeRemainingDialogFragment.create(mContext, 0, 0);
                            android.support.v4.app.FragmentManager fm = getFragmentManager();
                            dialog.show(fm, "tag");
                        } else {
                            order.setStatus(OrderStatus.READY_FOR_PICKUP);
                            updateOrderInCloud();
                        }
                    } else {
                        //show snackbar to check atleast one item
                        Snackbar.make(viewFlipperHeadline, "No item is selected in check list", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            hideButtons();
            showProgressBar(false);
        }
    }

    private void updateOrderInCloud() {
        //show processing
        showProgressBar(true);
        //send update request
        ((OrderDetailsActivity) getActivity()).updateOrderInCloud(order);
        //the changes will be reflected when the update request is successful
    }

    private void hideButtons() {
        buttonDelivery.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.GONE);
    }

    private boolean validateChecklist() {
        boolean atLeastOneItemIsChecked = false;
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getAvailableCount() > 0) {
                atLeastOneItemIsChecked = true;
                break;
            }
        }
        return atLeastOneItemIsChecked;
    }

    public void showProgressBar(boolean show) {
        if (show) {
            dilatingDotsProgressBar.show();
        } else {
            dilatingDotsProgressBar.hide();
        }
    }
}
