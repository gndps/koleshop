package com.koleshop.koleshopbackend.db.models;

import java.util.Date;
import java.util.List;

/**
 * Created by Gundeep on 13/02/16.
 */
public class Order {

    Long id;
    Long customerId;
    Long sellerId;
    Address customerAddress;
    int statusId;
    Float totalAmount;
    Float notAvailableAmount;
    Float deliveryCharges;
    Float carryBagCharges;
    Float amountPayable;
    Date orderTime;
    Date requestedDeliveryTime;
    Date actualDeliveryTime;
    boolean deliveryOrPickup;
    List<OrderItem> orderItems;

    public Order() {
    }

    public Order(Long id, Long customerId, Long sellerId, Address customerAddress, int statusId, Float totalAmount, Float notAvailableAmount, Float deliveryCharges, Float carryBagCharges, Float amountPayable, Date orderTime, Date requestedDeliveryTime, Date actualDeliveryTime, boolean deliveryOrPickup, List<OrderItem> orderItems) {
        this.id = id;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.customerAddress = customerAddress;
        this.statusId = statusId;
        this.totalAmount = totalAmount;
        this.notAvailableAmount = notAvailableAmount;
        this.deliveryCharges = deliveryCharges;
        this.carryBagCharges = carryBagCharges;
        this.amountPayable = amountPayable;
        this.orderTime = orderTime;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.deliveryOrPickup = deliveryOrPickup;
        this.orderItems = orderItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Address getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(Address customerAddress) {
        this.customerAddress = customerAddress;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Float getNotAvailableAmount() {
        return notAvailableAmount;
    }

    public void setNotAvailableAmount(Float notAvailableAmount) {
        this.notAvailableAmount = notAvailableAmount;
    }

    public Float getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(Float deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public Float getCarryBagCharges() {
        return carryBagCharges;
    }

    public void setCarryBagCharges(Float carryBagCharges) {
        this.carryBagCharges = carryBagCharges;
    }

    public Float getAmountPayable() {
        return amountPayable;
    }

    public void setAmountPayable(Float amountPayable) {
        this.amountPayable = amountPayable;
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

    public boolean isDeliveryOrPickup() {
        return deliveryOrPickup;
    }

    public void setDeliveryOrPickup(boolean deliveryOrPickup) {
        this.deliveryOrPickup = deliveryOrPickup;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
