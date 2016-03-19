package com.koleshop.appkoleshop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.model.OrderLite;
import com.koleshop.appkoleshop.util.PreferenceUtils;

public class OrderUpdateReceiver extends BroadcastReceiver {
    public OrderUpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //notification will reach here only when it is not used by the dynamic ui receiver
        String action = intent.getAction();
        if (action.equalsIgnoreCase(Constants.ACTION_ORDER_UPDATE_NOTIFICATION)) {
            int orderStatus = intent.getIntExtra("status", 0);
            Long orderId = intent.getLongExtra("orderId", 0);
            String name = intent.getStringExtra("name");
            float orderAmount = intent.getFloatExtra("amount", 0f);
            String imageUrl = intent.getStringExtra("imageUrl");

            PreferenceUtils.setPreferencesFlag(context, Constants.KEY_ORDERS_NEED_REFRESHING, true);
            //show notification based on status and session_type
            if (PreferenceUtils.isSessionTypeSeller(context)) {
                switch (orderStatus) {
                    case OrderStatus.INCOMING:
                    case OrderStatus.CANCELLED:
                    case OrderStatus.NOT_DELIVERED:
                        OrderLite orderLite = new OrderLite(orderId, name, orderAmount, orderStatus, imageUrl);
                        KoleshopNotificationUtils.addOrderToNotifications(orderLite);
                        KoleshopNotificationUtils.notifySeller(context);
                        break;
                    default:
                        break;
                }
            } else if (PreferenceUtils.isSessionTypeBuyer(context)) {
                switch (orderStatus) {
                    case OrderStatus.ACCEPTED:
                        //then show notification...your order has been accepted by the store
                        break;
                    case OrderStatus.REJECTED:
                        //then show notification...order has been declined by the store
                        break;
                    case OrderStatus.CANCELLED:
                        //hide the incoming notification if any
                        //show notification that order has been cancelled
                        break;
                    case OrderStatus.OUT_FOR_DELIVERY:
                        //show notification to user that his order is out for delivery
                        break;
                    case OrderStatus.READY_FOR_PICKUP:
                        //same as above
                        break;
                    case OrderStatus.NOT_DELIVERED:
                        //if there is any other notification for same order id...then dismiss it
                        //seller was not able to deliver your order
                    default:
                        break;
                }
            } else {
                //ignore this update
            }
        }
    }
}
