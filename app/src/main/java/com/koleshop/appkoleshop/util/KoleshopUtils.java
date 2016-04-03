package com.koleshop.appkoleshop.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.api.client.googleapis.notifications.NotificationUtils;
import com.koleshop.api.productEndpoint.model.InventoryProductVariety;
import com.koleshop.api.productEndpoint.model.InventoryProduct;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.helpers.KoleshopNotificationUtils;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.model.realm.EssentialInfo;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.services.CommonIntentService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by Gundeep on 22/12/15.
 */
public class KoleshopUtils {

    private static final String TAG = "KoleshopUtils";

    public static InventoryProduct getInventoryProductFromEditProduct(EditProduct product) {
        //prepare data
        InventoryProduct inventoryProduct = new InventoryProduct();
        inventoryProduct.setId(product.getId());
        inventoryProduct.setName(product.getName());
        inventoryProduct.setBrand(product.getBrand());
        List<InventoryProductVariety> vars = new ArrayList<>();
        for (EditProductVar var : product.getEditProductVars()) {
            InventoryProductVariety variety = new InventoryProductVariety();
            variety.setId(var.getId());
            variety.setQuantity(var.getQuantity());
            variety.setImageUrl(var.getImageUrl());
            variety.setLimitedStock(var.getLimitedStock());
            variety.setPrice(var.getPrice());
            variety.setValid(var.isValid());
            vars.add(variety);
        }
        inventoryProduct.setVarieties(vars);
        return inventoryProduct;
    }

    public static com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct getInventoryProductFromEditProduct2(EditProduct product) {
        //prepare data
        com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct inventoryProduct = new com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct();
        inventoryProduct.setId(product.getId());
        inventoryProduct.setName(product.getName());
        inventoryProduct.setBrand(product.getBrand());
        List<com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety> vars = new ArrayList<>();
        for (EditProductVar var : product.getEditProductVars()) {
            com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety variety = new com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety();
            variety.setId(var.getId());
            variety.setQuantity(var.getQuantity());
            variety.setImageUrl(var.getImageUrl());
            variety.setLimitedStock(var.getLimitedStock());
            variety.setPrice(var.getPrice());
            variety.setValid(var.isValid());
            vars.add(variety);
        }
        inventoryProduct.setVarieties(vars);
        return inventoryProduct;
    }

    public static Product getProductFromEditProduct(EditProduct editProduct) {
        //prepare data
        Product product = new Product();
        product.setId(editProduct.getId());
        product.setName(editProduct.getName());
        product.setBrand(editProduct.getBrand());
        product.setCategoryId(editProduct.getCategoryId());
        List<ProductVariety> vars = new ArrayList<>();
        for (EditProductVar var : editProduct.getEditProductVars()) {
            ProductVariety variety = getProductVarietyFromEditProductVar(var);
            vars.add(variety);
        }
        product.setVarieties(new RealmList<>(vars.toArray(new ProductVariety[vars.size()])));
        return product;
    }

    public static ProductVariety getProductVarietyFromEditProductVar(EditProductVar var) {
        ProductVariety variety = new ProductVariety();
        variety.setId(var.getId());
        variety.setQuantity(var.getQuantity());
        variety.setImageUrl(var.getImageUrl());
        variety.setLimitedStock(var.getLimitedStock());
        variety.setPrice(var.getPrice());
        variety.setVarietyValid(var.isValid());
        return variety;
    }

    public static List<Product> getProductListFromEditProductList(List<EditProduct> editProducts) {
        List<Product> products = new ArrayList<>();
        for (EditProduct editProduct : editProducts) {
            Product product = getProductFromEditProduct(editProduct);
            products.add(product);
        }
        return products;
    }

    public static SellerSettings getSettingsFromCache(Context context) {
        SellerSettings settingsString = RealmUtils.getSellerSettings(context);
        return settingsString;
    }

    public static TextDrawable getTextDrawable(Context context, String name, boolean round) {
        return getTextDrawable(context, name, 40, round);
    }

    public static TextDrawable getTextDrawable(Context context, String name, int widthInDp, boolean round) {
        float px = CommonUtils.getPixelsFromDp(context, widthInDp);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(name);
        TextDrawable textDrawable;
        TextDrawable.IShapeBuilder textDrawableBuilder = TextDrawable.builder()
                .beginConfig()
                .width((int) px)  // width in px
                .height((int) px) // height in px
                .endConfig();
        if (round) {
            textDrawable = textDrawableBuilder.buildRound(name.substring(0, 1).toUpperCase(), color);
        } else {
            textDrawable = textDrawableBuilder.buildRect(name.substring(0, 1).toUpperCase(), color);
        }

        return textDrawable;
    }

    public static TextDrawable getTextDrawable(Context context, String name, int widthInDp, boolean round, int color, int textColor) {
        Resources r = context.getResources();
        float px = CommonUtils.getPixelsFromDp(context, widthInDp);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        if (color == -1) {
            color = generator.getColor(name);
        }

        TextDrawable textDrawable;
        TextDrawable.IShapeBuilder textDrawableBuilder = TextDrawable.builder()
                .beginConfig()
                .width((int) px)  // width in px
                .height((int) px) // height in px
                .useFont(Typeface.SANS_SERIF)
                .textColor(textColor)
                .endConfig();
        if (round) {
            textDrawable = textDrawableBuilder.buildRound(name, color);
        } else {
            textDrawable = textDrawableBuilder.buildRect(name, color);
        }

        return textDrawable;
    }

    public static boolean willSellerDeliverNow(int sellerDeliveryEndTimeInMinutes) {
        Date deliveryEndTime = CommonUtils.getDateFromNumberOfMinutes(sellerDeliveryEndTimeInMinutes);
        Date timeNow = new Date();
        return deliveryEndTime.after(timeNow);
    }

    public static String getDeliveryTimeStringFromOpenAndCloseTime(int deliveryStartTime, int deliveryEndTime) {
        String startTime = CommonUtils.getTimeStringFromMinutes(deliveryStartTime, true);
        String endTime = CommonUtils.getTimeStringFromMinutes(deliveryEndTime, true);
        String deliveryTimeString = "-";
        if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
            deliveryTimeString = "Delivery " + startTime + " to " + endTime;
        }
        return deliveryTimeString;
    }

    public static boolean isStarred(ProductVariety productVariety) {
        return false;
    }

    public static ProductVariety getProductVarietyFromProduct(Product currentProduct, Long varietyId) {
        List<ProductVariety> varieties = currentProduct.getVarieties();
        if (varieties != null) {
            for (ProductVariety variety : varieties) {
                if (variety.getId().equals(varietyId)) {
                    return variety;
                }
            }
        }
        return null;
    }

    public static Float getItemsTotalPrice(List<ProductVarietyCount> productVarietyCounts) {
        Float total = 0f;
        for (ProductVarietyCount productVarietyCount : productVarietyCounts) {
            total += productVarietyCount.getCartCount() * productVarietyCount.getProductVariety().getPrice();
        }
        return total;
    }

    public static com.koleshop.api.orderEndpoint.model.Order getEndpointOrder(Order order) {

        com.koleshop.api.orderEndpoint.model.Order endpointOrder = new com.koleshop.api.orderEndpoint.model.Order();
        endpointOrder.setId(order.getId());
        endpointOrder.setOrderNumber(order.getOrderNumber());
        endpointOrder.setSellerSettings(getEndpointSellerSettings(order.getSellerSettings()));
        endpointOrder.setBuyerSettings(getEndpointBuyerSettings(order.getBuyerSettings()));
        endpointOrder.setAddress(getEndpointAddress(order.getAddress()));
        endpointOrder.setStatus(order.getStatus());
        endpointOrder.setOrderItems(getOrderItems(order.getOrderItems()));
        endpointOrder.setDeliveryCharges(order.getDeliveryCharges());
        endpointOrder.setCarryBagCharges(order.getCarryBagCharges());
        endpointOrder.setNotAvailableAmount(order.getNotAvailableAmount());
        endpointOrder.setTotalAmount(order.getTotalAmount());
        endpointOrder.setAmountPayable(order.getAmountPayable());
        endpointOrder.setHomeDelivery(order.isHomeDelivery());
        endpointOrder.setAsap(order.isAsap());
        //endpointOrder.setOrderTime(new DateTime(order.getOrderTime()));
        //endpointOrder.setRequestedDeliveryTime(new DateTime(order.getRequestedDeliveryTime()));
        //endpointOrder.setActualDeliveryTime(order.getActualDeliveryTime().getTime());
        //endpointOrder.setDeliveryStartTime(order.getDeliveryStartTime().getTime());
        endpointOrder.setMinutesToDelivery(order.getMinutesToDelivery());
        return endpointOrder;
    }

    private static List<com.koleshop.api.orderEndpoint.model.OrderItem> getOrderItems(List<OrderItem> orderItems) {
        List<com.koleshop.api.orderEndpoint.model.OrderItem> endpointOrderItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            com.koleshop.api.orderEndpoint.model.OrderItem endpointItem = new com.koleshop.api.orderEndpoint.model.OrderItem();
            endpointItem.setProductVarietyId(item.getProductVarietyId());
            endpointItem.setName(item.getName());
            endpointItem.setBrand(item.getBrand());
            endpointItem.setQuantity(item.getQuantity());
            endpointItem.setPricePerUnit(item.getPricePerUnit());
            endpointItem.setImageUrl(item.getImageUrl());
            endpointItem.setOrderCount(item.getOrderCount());
            endpointItem.setAvailableCount(item.getAvailableCount());
            endpointOrderItems.add(endpointItem);
        }
        return endpointOrderItems;
    }

    private static com.koleshop.api.orderEndpoint.model.Address getEndpointAddress(Address address) {
        com.koleshop.api.orderEndpoint.model.Address backendAddress = new com.koleshop.api.orderEndpoint.model.Address();
        if (address != null) {
            backendAddress.setId(address.getId());
            backendAddress.setAddress(address.getAddress());
            backendAddress.setAddressType(address.getAddressType());
            backendAddress.setCountryCode(address.getCountryCode());
            backendAddress.setGpsLat(address.getGpsLat());
            backendAddress.setGpsLong(address.getGpsLong());
            backendAddress.setName(address.getName());
            backendAddress.setPhoneNumber(address.getPhoneNumber());
            backendAddress.setNickname(address.getNickname());
            backendAddress.setUserId(address.getUserId());
        }
        return backendAddress;
    }

    private static com.koleshop.api.orderEndpoint.model.BuyerSettings getEndpointBuyerSettings(BuyerSettings buyerSettings) {
        com.koleshop.api.orderEndpoint.model.BuyerSettings endpointBuyerSettings = new com.koleshop.api.orderEndpoint.model.BuyerSettings();
        endpointBuyerSettings.setId(buyerSettings.getId());
        endpointBuyerSettings.setName(buyerSettings.getName());
        endpointBuyerSettings.setUserId(buyerSettings.getUserId());
        endpointBuyerSettings.setImageUrl(buyerSettings.getImageUrl());
        endpointBuyerSettings.setHeaderImageUrl(buyerSettings.getHeaderImageUrl());
        return endpointBuyerSettings;
    }

    private static com.koleshop.api.orderEndpoint.model.SellerSettings getEndpointSellerSettings(SellerSettings sellerSettings) {
        Address address = sellerSettings.getAddress();
        com.koleshop.api.orderEndpoint.model.SellerSettings backendSettings = new com.koleshop.api.orderEndpoint.model.SellerSettings();

        backendSettings.setId(sellerSettings.getId());
        backendSettings.setUserId(sellerSettings.getUserId());
        backendSettings.setAddress(getEndpointAddress(address));
        backendSettings.setDeliveryCharges(sellerSettings.getDeliveryCharges());
        backendSettings.setCarryBagCharges(sellerSettings.getCarryBagCharges());
        backendSettings.setDeliveryEndTime(sellerSettings.getDeliveryEndTime());
        backendSettings.setDeliveryStartTime(sellerSettings.getDeliveryStartTime());
        backendSettings.setHomeDelivery(sellerSettings.isHomeDelivery());
        backendSettings.setMaximumDeliveryDistance(sellerSettings.getMaximumDeliveryDistance());
        backendSettings.setMinimumOrder(sellerSettings.getMinimumOrder());
        backendSettings.setPickupFromShop(true);
        backendSettings.setShopCloseTime(sellerSettings.getShopCloseTime());
        backendSettings.setShopOpenTime(sellerSettings.getShopOpenTime());
        return backendSettings;
    }

    public static String getSmallImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.contains("/product_images/")) {
                imageUrl = imageUrl.replace("/product_images/", "/product_images_small/");
            }
            return imageUrl;
        } else {
            return null;
        }
    }

    public static String getThumbnailImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.contains("/profile/")) {
                imageUrl = imageUrl.replace("/profile/", "/profile_thumb/");
            }
        }
        return imageUrl;
    }

    public static boolean doesSellerDeliverToBuyerLocation(SellerSettings sellerSettings) {
        float[] results = new float[3];
        BuyerAddress buyerAddress = RealmUtils.getDefaultUserAddress();
        Double userLat = buyerAddress.getGpsLat();
        Double userLong = buyerAddress.getGpsLong();
        Location.distanceBetween(userLat, userLong, sellerSettings.getAddress().getGpsLat(), sellerSettings.getAddress().getGpsLong(), results);
        float userDistanceFromShopInMeters = results[0];
        if (results != null && results.length > 0) {
            //distanceText = CommonUtils.getReadableDistanceFromMetres(userDistanceFromShopInMeters);
        }
        return (sellerSettings.getMaximumDeliveryDistance() + Constants.DELIVERY_DISTANCE_APPROXIMATION_ERROR) >= userDistanceFromShopInMeters;
    }

    public static void showTheUpdateNotificationsIfRequired(final Context context) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                EssentialInfo essentialInfo = RealmUtils.getEssentialInfo();
                boolean isBuyer = PreferenceUtils.isSessionTypeBuyer(context);
                if (essentialInfo != null) {
                    Log.d(TAG, "handshake info is not null");
                    //check the date updated....if older than 1 day, then update the data
                    Long dateUpdated = essentialInfo.getDateToday();
                    Long dateNow = new Date().getTime();
                    if (dateNow - dateUpdated > 3 * DateUtils.DAY_IN_MILLIS) {
                        //Essential info needs update
                        Log.d(TAG, "handshake info needs update");
                        new CommonIntentService().loadEssentialInformationSync(isBuyer);
                    } else {
                        Log.d(TAG, "handshake info is up to date");
                        //Check if the app needs update...Show appropriate notification
                        doesTheAppNeedsImmediateUpdate(context, essentialInfo);
                    }
                } else {
                    //handshake info doesn't exist
                    Log.d(TAG, "handshake info is not null...downloading latest");
                }
                return null;
            }

        }.execute();
    }

    //this function checks if the app needs update...it also shows the appropriate update notification
    private static boolean doesTheAppNeedsImmediateUpdate(Context context, EssentialInfo essentialInfo) {
        try {
            //01. get app current version and latest app version
            PackageInfo pInfo = null;
            int currentAppVersionCode = 0;
            try {
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                currentAppVersionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "some problem in finding app version", e);
            }
            if(essentialInfo.getLatestAppVersion() == null) {
                return false;
            }
            int latestAppVersionCode = Integer.parseInt(essentialInfo.getLatestAppVersion());

            //02. check if app needs update/immediate update/nothing
            if (currentAppVersionCode == 0) {
                Log.wtf(TAG, "app version not found");
                return false;
            } else if (latestAppVersionCode > currentAppVersionCode) {
                //app update is available
                Long dateDeprecating = essentialInfo.getDeprecatedDate();
                Long dateToday = new Date().getTime();
                if (dateToday > dateDeprecating) {
                    //app needs immediate update
                    //show notification to user...you need to update the app to latest version...this version no longer works
                    KoleshopNotificationUtils.notifyAppUpdate(context, true, 0);
                    //show a toast...please update the app
                    return true;
                } else {
                    //some days are remaining
                    //NOTIFICATION TYPE - silent notification...show notification only if deprecating date is less than 15 days away
                    //MESSAGE - Please update to the latest version of koleshop...This version will stop working after 12 days
                    //FREQUENCY - show notification once in 5 days
                    Long dateDiff = dateDeprecating - dateToday;
                    int numberOfDaysInDeprecation = (int) (dateDiff / DateUtils.DAY_IN_MILLIS);
                    if (dateDiff > Constants.NOTIFY_USERS_ABOUT_UPDATE_BEFORE_APP_STOP_WORKING_IN_DAYS) {
                        //date diff is greater than notification time...no need to notify
                    } else {
                        //notify the user about the app update if no notification has been shown in last 5 days
                        boolean shouldShowNotification = true;
                        String lastUpdateNotiDateString = PreferenceUtils.getPreferences(context, Constants.KEY_LAST_UPDATE_NOTIFICATION_SHOWN_DATE);
                        if (!TextUtils.isEmpty(lastUpdateNotiDateString)) {
                            Long lastUpdateNotificationDate = Long.valueOf(lastUpdateNotiDateString);
                            if (dateToday - lastUpdateNotificationDate <= DateUtils.DAY_IN_MILLIS * Constants.MINIMUM_DAYS_BW_CONSECUTIVE_NOTIFICATIONS) {
                                shouldShowNotification = false;
                            }
                        }
                        if (shouldShowNotification) {
                            KoleshopNotificationUtils.notifyAppUpdate(context, false, numberOfDaysInDeprecation);
                            PreferenceUtils.setPreferences(context, Constants.KEY_LAST_UPDATE_NOTIFICATION_SHOWN_DATE, new Date().getTime() + "");
                        }
                    }
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "exception in doesTheAppNeedsImmediateUpdate", e);
            return false;
        }
    }

}
