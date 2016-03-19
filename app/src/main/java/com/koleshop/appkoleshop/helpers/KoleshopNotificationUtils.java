package com.koleshop.appkoleshop.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderLite;
import com.koleshop.appkoleshop.ui.seller.activities.HomeActivity;
import com.koleshop.appkoleshop.ui.seller.activities.SellerOrdersActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.ImageUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 19/03/16.
 */
public class KoleshopNotificationUtils {

    private static int NOTIFICATION_ID_SELLER = 1;
    private static int NOTIFICATION_ID_BUYER = 2;

    public static void notifySeller(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(ImageUtils.getBitmapFromDrawableResource(context, R.drawable.ic_koleshop_noti));
        //.setSmallIcon(R.drawable.notification_icon)
        //.setContentTitle("My notification")
        //.setContentText("Hello World!");
        List<OrderLite> orders = getOrdersList();
        if (orders == null || orders.size() == 0) {
            return;
        }

        //PreferenceUtils.getPreferences(context, Constants.KEY_ORDER_NOTIFICATION_MAP)
        int numberOfOrderUpdates = orders.size();
        if (numberOfOrderUpdates == 1) {
            OrderLite orderLite = orders.get(0);
            String buyerName = orderLite.getName();
            Integer orderStatus = orderLite.getStatus();

            String notificationContent = "";
            String notificationContentInfo = "";
            switch (orderStatus) {
                case OrderStatus.INCOMING:
                    notificationContent = "1 new order";
                    notificationContentInfo = CommonUtils.getPriceStringFromFloat(orderLite.getAmount(), true);
                    break;
                case OrderStatus.CANCELLED:
                    notificationContent = "1 order cancelled";
                    break;
                case OrderStatus.NOT_DELIVERED:
                    notificationContent = "1 order not delivered";
                    break;
                default:
                    return;
            }
            if (TextUtils.isEmpty(buyerName)) {
                return;
            }
            mBuilder.setContentTitle(buyerName);
            mBuilder.setContentInfo(notificationContentInfo);
            mBuilder.setContentText(notificationContent);
        } else {
            String buyerName = "";
            int typeOfNotifications = 0;
            int incomingOrderCount = 0;
            int cancelledOrderCount = 0;
            int notDeliveredOrderCount = 0;
            String incomingBuyers = "";
            String cancelledBuyers = "";
            String notDeliveredBuyers = "";
            String incomingOrdersCountString = "";
            String cancelledOrdersCountString = "";
            String notDeliveredOrdersCountString = "";
            float amountOfIncomingOrders = 0f;
            String notificationContent = "";
            String notificationContentTitle = "";
            String notificationContentInfo = "";
            for (OrderLite orderLite : orders) {
                int orderStatus = orderLite.getStatus();
                switch (orderStatus) {
                    case OrderStatus.INCOMING:
                        if (incomingOrderCount == 0) {
                            typeOfNotifications++;
                            incomingOrderCount++;
                            incomingBuyers = orderLite.getName();
                        } else {
                            incomingOrderCount++;
                            incomingBuyers += ", " + orderLite.getName();
                            notificationContent = incomingBuyers;
                        }
                        incomingOrdersCountString = incomingOrderCount==1?"1 new order":(incomingOrderCount + " new orders");
                        notificationContentTitle = incomingOrdersCountString;
                        amountOfIncomingOrders += orderLite.getAmount();
                        notificationContentInfo = CommonUtils.getPriceStringFromFloat(amountOfIncomingOrders, true);
                        break;
                    case OrderStatus.CANCELLED:
                        if (cancelledOrderCount == 0) {
                            typeOfNotifications++;
                            cancelledOrderCount++;
                            cancelledBuyers = orderLite.getName();
                        } else {
                            cancelledOrderCount++;
                            cancelledBuyers += ", " + orderLite.getName();
                            notificationContent = cancelledBuyers;
                        }
                        cancelledOrdersCountString = cancelledOrderCount==1?"1 order cancelled":(cancelledOrderCount + " orders cancelled");
                        notificationContentTitle = cancelledOrdersCountString;
                        break;
                    case OrderStatus.NOT_DELIVERED:
                        if (notDeliveredOrderCount == 0) {
                            typeOfNotifications++;
                            notDeliveredOrderCount++;
                            notDeliveredBuyers = orderLite.getName();
                        } else {
                            notDeliveredOrderCount++;
                            notDeliveredBuyers += ", " + orderLite.getName();
                            notificationContent = notDeliveredBuyers;
                        }
                        notDeliveredOrdersCountString = notDeliveredOrderCount==1?"1 order not delivered":(notDeliveredOrderCount + " orders not delivered");
                        notificationContentTitle = notDeliveredOrdersCountString;
                        break;
                    default:
                        return;
                }
                if(typeOfNotifications == 1 ){
                    //notificationContentTitle is already configured
                    //notificationContent is also already configured
                    if(amountOfIncomingOrders>0) {
                        //this is the case of multiple incoming orders
                        //need to show content info (total price of orders)
                        //notificationContentInfo is already configured
                        mBuilder.setContentInfo(notificationContentInfo);
                    }

                } else if(typeOfNotifications > 0) {
                    notificationContentTitle = (incomingOrderCount + cancelledOrderCount + notDeliveredOrderCount) + " order updates";
                    notificationContent = incomingOrdersCountString;

                    if(!incomingOrdersCountString.isEmpty()) {
                        notificationContent += ", " + cancelledOrdersCountString;
                    } else {
                        notificationContent = cancelledOrdersCountString;
                    }

                    if(!notificationContent.isEmpty()) {
                        notificationContent += ", " + notDeliveredOrdersCountString;
                    } else {
                        notificationContent = notDeliveredOrdersCountString;
                    }
                } else {
                    return;
                }
            }
            mBuilder.setContentInfo(notificationContentInfo);
            mBuilder.setContentText(notificationContent);
            mBuilder.setContentTitle(notificationContentTitle);
        }
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, SellerOrdersActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_stat_koleshop_noti);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID_SELLER, mBuilder.build());
    }

    private static List<OrderLite> getOrdersList() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<OrderLite> existingOrders = realm.where(OrderLite.class)
                .findAll();
        if (existingOrders != null && existingOrders.size() > 0) {
            List<OrderLite> orders = realm.copyFromRealm(existingOrders);
            realm.close();
            return orders;
        } else {
            realm.close();
            return null;
        }
    }

    public static void dismissAllNotifications(Context context) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(PreferenceUtils.isSessionTypeSeller(context)) {
            mNotificationManager.cancel(NOTIFICATION_ID_SELLER);
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID_BUYER);
        }
    }

    public static void addOrderToNotifications(OrderLite orderLite) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(orderLite);
        realm.commitTransaction();
        realm.close();
    }


    public static void clearNotificationOrders() {
        //clear notifications from realm
        Realm realm = Realm.getDefaultInstance();
        RealmResults<OrderLite> orders = realm.where(OrderLite.class).findAll();
        realm.beginTransaction();
        orders.clear();
        realm.commitTransaction();
        realm.close();
    }

}
