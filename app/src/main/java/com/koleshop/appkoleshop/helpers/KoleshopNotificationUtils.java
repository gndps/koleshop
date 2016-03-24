package com.koleshop.appkoleshop.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.OrderStatus;
import com.koleshop.appkoleshop.model.OrderLite;
import com.koleshop.appkoleshop.ui.common.activities.OrderDetailsActivity;
import com.koleshop.appkoleshop.ui.seller.activities.SellerOrdersActivity;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.ImageUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 19/03/16.
 */
public class KoleshopNotificationUtils {

    private static final String TAG = "KoleshopNotiUtil";
    private static int NOTIFICATION_ID_SELLER = 1;
    private static int NOTIFICATION_ID_BUYER = 2;

    public static void notifySeller(Context context) {
        Log.d(TAG, "will notify user");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(ImageUtils.getBitmapFromDrawableResource(context, R.drawable.ic_koleshop_noti));
        //.setSmallIcon(R.drawable.notification_icon)
        //.setContentTitle("My notification")
        //.setContentText("Hello World!");
        List<OrderLite> orders = getOrdersList();
        if (orders == null || orders.size() == 0) {
            Log.d(TAG, "no orders to show notification");
            return;
        }

        //PreferenceUtils.getPreferences(context, Constants.KEY_ORDER_NOTIFICATION_MAP)
        int numberOfOrderUpdates = orders.size();
        Log.d(TAG, "number of orders = " + numberOfOrderUpdates);

        //01. NUMBER OF ORDERS UPDATED = 1


        if (numberOfOrderUpdates == 1) {
            OrderLite orderLite = orders.get(0);
            String buyerName = orderLite.getName();
            Integer orderStatus = orderLite.getStatus();

            String notificationContent;
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
            Log.d(TAG, "writing notification content");
            mBuilder.setContentTitle(buyerName);
            mBuilder.setContentInfo(notificationContentInfo);
            mBuilder.setContentText(notificationContent);
            //Bitmap imageBitmap = null;
            //String imageUrl = null;
            //imageUrl = TEST_IMAGE_URL;
            /*Log.d(TAG, "getting image from url");
            if (!TextUtils.isEmpty(imageUrl) && URLUtil.isValidUrl(imageUrl)) {
                Log.d(TAG, "getBitmapFromUrl");
                imageBitmap  = ImageUtils.getBitmapFromURL(context, imageUrl, true);
            }
            if(imageBitmap==null) {
                Log.d(TAG, "image bitmap is null...getting textdrawable");
                imageBitmap = ImageUtils.drawableToBitmap(KoleshopUtils.getTextDrawable(context, buyerName, true));
            }
            Log.d(TAG, "setting large icon");
            mBuilder.setLargeIcon(imageBitmap);*/


            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, OrderDetailsActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            resultIntent.putExtra("orderId", orderLite.getOrderId());
            resultIntent.putExtra("customerView", false);
            PendingIntent notifyIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(notifyIntent);
            mBuilder.setSmallIcon(R.drawable.ic_stat_koleshop_noti);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(orders.get(0).getOrderId().intValue(), mBuilder.build());

        } else {

        //02. NUMBER OF ORDERS UPDATED > 1


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
                buyerName = orderLite.getName();
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
                        incomingOrdersCountString = incomingOrderCount == 1 ? "1 new order" : (incomingOrderCount + " new orders");
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
                        cancelledOrdersCountString = cancelledOrderCount == 1 ? "1 order cancelled" : (cancelledOrderCount + " orders cancelled");
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
                        notDeliveredOrdersCountString = notDeliveredOrderCount == 1 ? "1 order not delivered" : (notDeliveredOrderCount + " orders not delivered");
                        notificationContentTitle = notDeliveredOrdersCountString;
                        break;
                    default:
                        return;
                }
            }

            if (typeOfNotifications == 1) {
                //notificationContentTitle is already configured
                //notificationContent is also already configured
                Log.d(TAG, "types of notifications = 1");
                if (amountOfIncomingOrders > 0) {
                    //this is the case of multiple incoming orders
                    //need to show content info (total price of orders)
                    //notificationContentInfo is already configured
                    Log.d(TAG, "setting price for many incoming orders");
                    mBuilder.setContentInfo(notificationContentInfo);
                }

            } else if (typeOfNotifications > 0) {
                //notificationContentTitle = (incomingOrderCount + cancelledOrderCount + notDeliveredOrderCount) + " order updates";
                notificationContentTitle = context.getResources().getString(R.string.app_name);
                notificationContent = incomingOrdersCountString;

                if (!incomingOrdersCountString.isEmpty() && !cancelledOrdersCountString.isEmpty()) {
                    notificationContent += ", " + cancelledOrdersCountString;
                } else if (!cancelledOrdersCountString.isEmpty()) {
                    notificationContent = cancelledOrdersCountString;
                }

                if (!notificationContent.isEmpty() && !notDeliveredOrdersCountString.isEmpty()) {
                    notificationContent += ", " + notDeliveredOrdersCountString;
                } else if (!notDeliveredOrdersCountString.isEmpty()) {
                    notificationContent = notDeliveredOrdersCountString;
                }
            } else {
                return;
            }

            Log.d(TAG, "building content of notification for multiple order updates");
            mBuilder.setContentInfo(notificationContentInfo);
            mBuilder.setContentText(notificationContent);
            mBuilder.setContentTitle(notificationContentTitle);
            mBuilder.setAutoCancel(true);


            //show notification
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            Intent resultIntent;
            resultIntent = new Intent(context, SellerOrdersActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(SellerOrdersActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            Log.d(TAG, "initializing a pending intent");
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            mBuilder.setSmallIcon(R.drawable.ic_stat_koleshop_noti);
            Log.d(TAG, "created notification");
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            Log.d(TAG, "showing notification to seller");
            mNotificationManager.notify(NOTIFICATION_ID_SELLER, mBuilder.build());

        }
    }

    public static void notifyBuyer(Context context) {
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
        boolean playedSound = false;
        for (OrderLite orderLite : orders) {
            String sellerName = orderLite.getName();
            String amount = CommonUtils.getPriceStringFromFloat(orderLite.getAmount(), true);
            int tag = orderLite.getOrderId().intValue();
            int statusId = orderLite.getStatus();
            String status = "";
            switch (statusId) {
                case OrderStatus.OUT_FOR_DELIVERY:
                    status = "Your order is out for delivery";
                    break;
                case OrderStatus.READY_FOR_PICKUP:
                    status = "Your order is ready for pickup";
                    break;
                case OrderStatus.REJECTED:
                    status = "Your order was declined";
                    break;
                case OrderStatus.NOT_DELIVERED:
                    status = "Your order could not be delivered";
                    break;
                case OrderStatus.CANCELLED:
                    //this option should never appear
                    status = "Your order was cancelled";
                default:
                    break;
            }
            mBuilder.setContentInfo(amount);
            mBuilder.setContentText(status);
            mBuilder.setContentTitle(sellerName);
            mBuilder.setAutoCancel(true);
            if (!playedSound) {
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(alarmSound);
                playedSound = true;
            }
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, OrderDetailsActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            resultIntent.putExtra("orderId", orderLite.getOrderId());
            resultIntent.putExtra("customerView", true);
            PendingIntent notifyIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(notifyIntent);
            mBuilder.setSmallIcon(R.drawable.ic_stat_koleshop_noti);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(tag, mBuilder.build());
        }
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
        if (PreferenceUtils.isSessionTypeSeller(context)) {
            mNotificationManager.cancel(NOTIFICATION_ID_SELLER);
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID_BUYER);
        }
        clearNotificationOrders();
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

    public static void removeThisOrderFromNotificationOrders(Long orderId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<OrderLite> ordersLite = realm.where(OrderLite.class).equalTo("orderId", orderId).findAll();
        if (ordersLite == null || ordersLite.size() <= 0) {
            realm.close();
            return;
        } else {
            OrderLite orderLite = ordersLite.first();
            realm.beginTransaction();
            orderLite.removeFromRealm();
            realm.commitTransaction();
            realm.close();
        }
    }

}
