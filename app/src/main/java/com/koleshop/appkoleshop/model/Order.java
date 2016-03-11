package com.koleshop.appkoleshop.model;

import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.BuyerSettings;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

/**
 * Created by Gundeep on 23/01/16.
 */

@Parcel
public class Order {

    Long id;
    String orderNumber;
    SellerSettings sellerSettings;
    BuyerSettings buyerSettings;
    Address address;
    int status;
    List<OrderItem> orderItems;
    float deliveryCharges;
    float carryBagCharges;
    float totalAmount;
    float amountPayable;
    float notAvailableAmount;
    boolean homeDelivery;
    boolean asap;
    Date orderTime;
    Date requestedDeliveryTime;
    Date actualDeliveryTime;
    Date deliveryStartTime;
    int minutesToDelivery; //how many minutes did the seller select when sending the order out for delivery


    public Order() {
    }

    public Order(Long id, String orderNumber, SellerSettings sellerSettings, BuyerSettings buyerSettings, Address address, int status, List<OrderItem> orderItems, float deliveryCharges, float carryBagCharges, float totalAmount, float amountPayable, float notAvailableAmount, boolean homeDelivery, boolean asap, Date orderTime, Date requestedDeliveryTime, Date actualDeliveryTime, Date deliveryStartTime, int minutesToDelivery) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.sellerSettings = sellerSettings;
        this.buyerSettings = buyerSettings;
        this.address = address;
        this.status = status;
        this.orderItems = orderItems;
        this.deliveryCharges = deliveryCharges;
        this.carryBagCharges = carryBagCharges;
        this.totalAmount = totalAmount;
        this.amountPayable = amountPayable;
        this.notAvailableAmount = notAvailableAmount;
        this.homeDelivery = homeDelivery;
        this.asap = asap;
        this.orderTime = orderTime;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.deliveryStartTime = deliveryStartTime;
        this.minutesToDelivery = minutesToDelivery;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public SellerSettings getSellerSettings() {
        return sellerSettings;
    }

    public void setSellerSettings(SellerSettings sellerSettings) {
        this.sellerSettings = sellerSettings;
    }

    public BuyerSettings getBuyerSettings() {
        return buyerSettings;
    }

    public void setBuyerSettings(BuyerSettings buyerSettings) {
        this.buyerSettings = buyerSettings;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public float getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(float deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public float getCarryBagCharges() {
        return carryBagCharges;
    }

    public void setCarryBagCharges(float carryBagCharges) {
        this.carryBagCharges = carryBagCharges;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public float getAmountPayable() {
        return amountPayable;
    }

    public void setAmountPayable(float amountPayable) {
        this.amountPayable = amountPayable;
    }

    public float getNotAvailableAmount() {
        return notAvailableAmount;
    }

    public void setNotAvailableAmount(float notAvailableAmount) {
        this.notAvailableAmount = notAvailableAmount;
    }

    public boolean isHomeDelivery() {
        return homeDelivery;
    }

    public void setHomeDelivery(boolean homeDelivery) {
        this.homeDelivery = homeDelivery;
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

    public Date getRequestedDeliveryTime() {
        return requestedDeliveryTime;
    }

    public void setRequestedDeliveryTime(Date requestedDeliveryTime) {
        this.requestedDeliveryTime = requestedDeliveryTime;
    }

    public Date getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(Date actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
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
}
