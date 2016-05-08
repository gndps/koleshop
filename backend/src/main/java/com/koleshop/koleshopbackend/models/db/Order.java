package com.koleshop.koleshopbackend.models.db;

import java.util.List;

/**
 * Created by Gundeep on 13/02/16.
 */
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
    float notAvailableAmount;
    float totalAmount;
    float amountPayable;
    boolean homeDelivery;
    boolean asap;
    Long orderTime;
    Long requestedDeliveryTime;
    Long actualDeliveryTime;
    Long deliveryStartTime;
    int minutesToDelivery; //how many minutes did the seller select when sending the order out for delivery

    public Order() {
    }

    public Order(Long id, String orderNumber, SellerSettings sellerSettings, BuyerSettings buyerSettings, Address address, int status, List<OrderItem> orderItems, float deliveryCharges, float carryBagCharges, float notAvailableAmount, float totalAmount, float amountPayable, boolean homeDelivery, boolean asap, Long orderTime, Long requestedDeliveryTime, Long actualDeliveryTime, Long deliveryStartTime, int minutesToDelivery) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.sellerSettings = sellerSettings;
        this.buyerSettings = buyerSettings;
        this.address = address;
        this.status = status;
        this.orderItems = orderItems;
        this.deliveryCharges = deliveryCharges;
        this.carryBagCharges = carryBagCharges;
        this.notAvailableAmount = notAvailableAmount;
        this.totalAmount = totalAmount;
        this.amountPayable = amountPayable;
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

    public Long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Long orderTime) {
        this.orderTime = orderTime;
    }

    public Long getRequestedDeliveryTime() {
        return requestedDeliveryTime;
    }

    public void setRequestedDeliveryTime(Long requestedDeliveryTime) {
        this.requestedDeliveryTime = requestedDeliveryTime;
    }

    public Long getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(Long actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    public Long getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(Long deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }

    public int getMinutesToDelivery() {
        return minutesToDelivery;
    }

    public void setMinutesToDelivery(int minutesToDelivery) {
        this.minutesToDelivery = minutesToDelivery;
    }

}
