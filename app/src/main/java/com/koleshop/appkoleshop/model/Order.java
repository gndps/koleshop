package com.koleshop.appkoleshop.model;

import com.koleshop.appkoleshop.constant.OrderStatus;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

/**
 * Created by Gundeep on 23/01/16.
 */

@Parcel
public class Order {

    Long sellerId;
    String sellerName;
    Long sellerPhone;
    String sellerImageUrl;
    Long buyerId;
    String buyerName;
    Long buyerPhone;
    OrderStatus status;
    List<OrderItem> list;
    float deliveryCharges;
    int orderType; // 0 for pickup, 1 for home delivery
    boolean asap;
    Date orderTime;
    Date deliveryTime;
    String address;
    Date deliveryStartTime;
    int minutesToDelivery; //how many minutes did the seller select when sending the order out for delivery
    float totalAmount;

    public Order(Date deliveryTime, Long sellerId, String sellerName, Long sellerPhone, String sellerImageUrl, Long buyerId, String buyerName, Long buyerPhone, OrderStatus status, List<OrderItem> list, float deliveryCharges, int orderType, boolean asap, Date orderTime, String address, Date deliveryStartTime, int minutesToDelivery, float totalAmount) {
        this.deliveryTime = deliveryTime;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.sellerImageUrl = sellerImageUrl;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.status = status;
        this.list = list;
        this.deliveryCharges = deliveryCharges;
        this.orderType = orderType;
        this.asap = asap;
        this.orderTime = orderTime;
        this.address = address;
        this.deliveryStartTime = deliveryStartTime;
        this.minutesToDelivery = minutesToDelivery;
        this.totalAmount = totalAmount;
    }

    public Order() {
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Long getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(Long sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getSellerImageUrl() {
        return sellerImageUrl;
    }

    public void setSellerImageUrl(String sellerImageUrl) {
        this.sellerImageUrl = sellerImageUrl;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(Long buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getList() {
        return list;
    }

    public void setList(List<OrderItem> list) {
        this.list = list;
    }

    public float getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(float deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public boolean isAsap() {
        return asap;
    }

    public void setAsap(boolean asap) {
        this.asap = asap;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(Date deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }

    public int getMinutesToDelivery() {
        return minutesToDelivery;
    }

    public void setMinutesToDelivery(int minutesToDelivery) {
        this.minutesToDelivery = minutesToDelivery;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

}
