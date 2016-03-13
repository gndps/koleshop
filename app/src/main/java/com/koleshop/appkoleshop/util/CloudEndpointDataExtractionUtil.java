package com.koleshop.appkoleshop.util;

import android.text.TextUtils;

import com.google.api.client.util.ArrayMap;
import com.koleshop.appkoleshop.model.Order;
import com.koleshop.appkoleshop.model.OrderItem;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by Gundeep on 27/02/16.
 */
public class CloudEndpointDataExtractionUtil {

    public static SellerSettings getSellerSettings(ArrayMap<String, Object> sellerSettingsArrayMap) {
        SellerSettings sellerSettings = new SellerSettings();
        if (sellerSettingsArrayMap != null) {
            sellerSettings.setImageUrl((String) sellerSettingsArrayMap.get("imageUrl"));
            sellerSettings.setHeaderImageUrl((String) sellerSettingsArrayMap.get("headerImageUrl"));
            sellerSettings.setId(Long.valueOf((String) sellerSettingsArrayMap.get("id")));
            sellerSettings.setPickupFromShop((Boolean) sellerSettingsArrayMap.get("pickupFromShop"));
            sellerSettings.setHomeDelivery((Boolean) sellerSettingsArrayMap.get("homeDelivery"));
            sellerSettings.setMinimumOrder(((BigDecimal) sellerSettingsArrayMap.get("minimumOrder")).floatValue());
            sellerSettings.setDeliveryCharges(((BigDecimal) sellerSettingsArrayMap.get("deliveryCharges")).floatValue());
            sellerSettings.setCarryBagCharges(((BigDecimal) sellerSettingsArrayMap.get("carryBagCharges")).floatValue());
            sellerSettings.setMaximumDeliveryDistance(Long.valueOf((String) sellerSettingsArrayMap.get("maximumDeliveryDistance")));
            sellerSettings.setDeliveryStartTime(((BigDecimal) sellerSettingsArrayMap.get("deliveryStartTime")).intValue());
            sellerSettings.setDeliveryEndTime(((BigDecimal) sellerSettingsArrayMap.get("deliveryEndTime")).intValue());
            sellerSettings.setShopOpenTime(((BigDecimal) sellerSettingsArrayMap.get("shopOpenTime")).intValue());
            sellerSettings.setShopCloseTime(((BigDecimal) sellerSettingsArrayMap.get("shopCloseTime")).intValue());
            sellerSettings.setShopOpen((Boolean) sellerSettingsArrayMap.get("shopOpen"));
            sellerSettings.setUserId(Long.valueOf((String) sellerSettingsArrayMap.get("userId")));
            sellerSettings.setAddress(getAddress((ArrayMap<String, Object>) sellerSettingsArrayMap.get("address")));
        }
        return sellerSettings;
    }

    public static Address getAddress(ArrayMap<String, Object> addressMap) {
        Address address = new Address();
        if (addressMap != null) {
            address.setUserId(Long.valueOf((String) addressMap.get("userId")));
            address.setId(Long.valueOf((String) addressMap.get("id")));
            address.setAddress((String) addressMap.get("address"));
            address.setPhoneNumber(Long.valueOf((String) addressMap.get("phoneNumber")));
            address.setName((String) addressMap.get("name"));
            address.setAddressType(((BigDecimal) addressMap.get("addressType")).intValue());
            address.setCountryCode(((BigDecimal) addressMap.get("countryCode")).intValue());
            address.setNickname((String) addressMap.get("nickname"));
            address.setGpsLong(((BigDecimal) addressMap.get("gpsLong")).doubleValue());
            address.setGpsLat(((BigDecimal) addressMap.get("gpsLat")).doubleValue());
        }
        return address;
    }

    public static BuyerSettings getBuyerSettings(ArrayMap<String, Object> buyerSettingsArrayMap) {
        BuyerSettings buyerSettings = new BuyerSettings();
        if(buyerSettingsArrayMap.get("id")!=null) {
            buyerSettings.setId(Long.valueOf((String) buyerSettingsArrayMap.get("id")));
        }
        buyerSettings.setName((String) buyerSettingsArrayMap.get("name"));
        buyerSettings.setUserId(Long.valueOf((String) buyerSettingsArrayMap.get("userId")));
        buyerSettings.setImageUrl((String) buyerSettingsArrayMap.get("imageUrl"));
        buyerSettings.setHeaderImageUrl((String) buyerSettingsArrayMap.get("headerImageUrl"));
        return buyerSettings;
    }

    public static List<Product> getProductsList(ArrayList<ArrayMap<String, Object>> list, Long sellerId, Long categoryId, boolean myInventory) {
        List<Product> products = new ArrayList<>();
        if (list != null) {
            for (ArrayMap<String, Object> map : list)
                if (map != null) {
                    Product prod = new Product();
                    prod.setId(Long.valueOf((String) map.get("id")));
                    prod.setSellerId(sellerId);
                    prod.setName((String) map.get("name"));
                    //prod.setDescription((String) map.get("description"));
                    prod.setBrand((String) map.get("brand"));
                    prod.setCategoryId(categoryId);
                    if (myInventory) {
                        prod.setUpdateDateMyShop(new Date());
                    } else {
                        prod.setUpdateDateWareHouse(new Date());
                    }
                    //prod.setAdditionalInfo((String) map.get("additionalInfo"));
                    //prod.setAdditionalInfo((String) map.get("specialDescription"));
                    //prod.setPrivateToUser(Boolean.valueOf((Boolean) map.get("privateToUser")));
                    //prod.setSelectedByUser(Boolean.valueOf((Boolean) map.get("selectedByUser")));
                    ArrayList<ArrayMap<String, Object>> varieties = (ArrayList<ArrayMap<String, Object>>) map.get("varieties");
                    List<ProductVariety> productVarieties = new ArrayList<>();
                    for (ArrayMap<String, Object> variety : varieties) {
                        ProductVariety proVar = new ProductVariety();
                        proVar.setId(Long.valueOf((String) variety.get("id")));
                        proVar.setQuantity((String) variety.get("quantity"));
                        proVar.setPrice(((BigDecimal) variety.get("price")).floatValue());
                        proVar.setImageUrl((String) variety.get("imageUrl"));
                        //proVar.setVegNonVeg((String) variety.get("vegNonVeg"));
                        proVar.setVarietyValid((Boolean) variety.get("valid"));
                        proVar.setLimitedStock((Boolean) variety.get("limitedStock"));
                        productVarieties.add(proVar);
                    }

                    prod.setVarieties(new RealmList<>(productVarieties.toArray(new ProductVariety[productVarieties.size()])));
                    products.add(prod);
                }
        }
        return products;
    }

    public static List<EditProduct> getEditProductsList(ArrayList<ArrayMap<String, Object>> list, Long sellerId) {
        List<EditProduct> products = new ArrayList<>();
        if (list != null) {
            for (ArrayMap<String, Object> map : list)
                if (map != null) {
                    EditProduct prod = new EditProduct();
                    prod.setId(Long.valueOf((String) map.get("id")));
                    prod.setSellerId(sellerId);
                    prod.setName((String) map.get("name"));
                    //prod.setDescription((String) map.get("description"));
                    prod.setBrand((String) map.get("brand"));
                    prod.setCategoryId(Long.valueOf((String) map.get("categoryId")));
                    //prod.setAdditionalInfo((String) map.get("additionalInfo"));
                    //prod.setAdditionalInfo((String) map.get("specialDescription"));
                    //prod.setPrivateToUser(Boolean.valueOf((Boolean) map.get("privateToUser")));
                    //prod.setSelectedByUser(Boolean.valueOf((Boolean) map.get("selectedByUser")));
                    ArrayList<ArrayMap<String, Object>> varieties = (ArrayList<ArrayMap<String, Object>>) map.get("varieties");
                    List<EditProductVar> productVarieties = new ArrayList<>();
                    for (ArrayMap<String, Object> variety : varieties) {
                        EditProductVar proVar = new EditProductVar();
                        proVar.setId(Long.valueOf((String) variety.get("id")));
                        proVar.setQuantity((String) variety.get("quantity"));
                        proVar.setPrice(((BigDecimal) variety.get("price")).floatValue());
                        proVar.setImageUrl((String) variety.get("imageUrl"));
                        //proVar.setVegNonVeg((String) variety.get("vegNonVeg"));
                        proVar.setValid((Boolean) variety.get("valid"));
                        proVar.setLimitedStock((Boolean) variety.get("limitedStock"));
                        productVarieties.add(proVar);
                    }

                    prod.setEditProductVars(productVarieties);
                    products.add(prod);
                }
        }
        return products;
    }

    public static Order getOrderFromJsonResult(ArrayMap<String, Object> resultArrayMap) {

        Order order = new Order();
        order.setId(Long.valueOf((String) resultArrayMap.get("id")));
        order.setOrderNumber((String) resultArrayMap.get("orderNumber"));
        order.setSellerSettings(getSellerSettings((ArrayMap<String, Object>) resultArrayMap.get("sellerSettings")));
        order.setBuyerSettings(getBuyerSettings((ArrayMap<String, Object>) resultArrayMap.get("buyerSettings")));
        order.setAddress(getAddress((ArrayMap<String, Object>) resultArrayMap.get("address")));
        order.setStatus(((BigDecimal) resultArrayMap.get("status")).intValue());
        order.setOrderItems(getOrderItems((ArrayList<ArrayMap<String, Object>>) resultArrayMap.get("orderItems")));
        order.setDeliveryCharges(((BigDecimal) resultArrayMap.get("deliveryCharges")).floatValue());
        order.setCarryBagCharges(((BigDecimal) resultArrayMap.get("carryBagCharges")).floatValue());
        order.setTotalAmount(((BigDecimal) resultArrayMap.get("totalAmount")).floatValue());
        order.setNotAvailableAmount(((BigDecimal) resultArrayMap.get("notAvailableAmount")).floatValue());
        order.setAmountPayable(((BigDecimal) resultArrayMap.get("amountPayable")).floatValue());
        order.setHomeDelivery((Boolean) resultArrayMap.get("homeDelivery"));
        order.setAsap((Boolean) resultArrayMap.get("asap"));
        order.setOrderTime(new Date(Long.valueOf((String) resultArrayMap.get("orderTime"))));
        if(resultArrayMap.get("requestedDeliveryTime")!=null) {
            order.setRequestedDeliveryTime(new Date(Long.valueOf((String) resultArrayMap.get("requestedDeliveryTime"))));
        }
        if(resultArrayMap.get("actualDeliveryTime")!=null) {
            order.setActualDeliveryTime(new Date(Long.valueOf((String) resultArrayMap.get("actualDeliveryTime"))));
        }
        if(resultArrayMap.get("deliveryStartTime")!=null) {
            order.setDeliveryStartTime(new Date(Long.valueOf((String) resultArrayMap.get("deliveryStartTime"))));
        }
        if(resultArrayMap.get("minutesToDelivery")!=null) {
            order.setMinutesToDelivery(((BigDecimal) resultArrayMap.get("minutesToDelivery")).intValue());
        }

        return order;
    }

    public static List<Order> getOrdersListFromJsonResult(ArrayList<ArrayMap<String, Object>> list) {
        List<Order> orders = new ArrayList<>();
        if(list!=null && list.size()>0) {
            for(ArrayMap<String, Object> jsonOrder : list) {
                Order order = getOrderFromJsonResult(jsonOrder);
                orders.add(order);
            }
        }
        return orders;
    }

    public static List<OrderItem> getOrderItems(ArrayList<ArrayMap<String, Object>> orderItemsList) {

        List<OrderItem> orderItems = new ArrayList<>();
        if (orderItemsList != null) {
            for (ArrayMap<String, Object> map : orderItemsList)
                if (map != null) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductVarietyId(Long.valueOf((String) map.get("productVarietyId")));
                    orderItem.setName((String) map.get("name"));
                    orderItem.setBrand((String) map.get("brand"));
                    orderItem.setQuantity((String) map.get("quantity"));
                    orderItem.setPricePerUnit(((BigDecimal) map.get("pricePerUnit")).floatValue());
                    orderItem.setImageUrl((String) map.get("imageUrl"));
                    orderItem.setOrderCount(((BigDecimal) map.get("orderCount")).intValue());
                    orderItem.setAvailableCount(((BigDecimal) map.get("availableCount")).intValue());
                    orderItems.add(orderItem);
                }
        }
        return orderItems;
    }

}
