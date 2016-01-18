package com.koleshop.koleshopbackend.db.models;

import java.util.Date;

/**
 * Created by Gundeep on 14/01/16.
 */
public class SellerSettings {

    Long id;
    Long userId;
    Address address;
    Date shopOpenTime;
    Date shopCloseTime;
    boolean pickupFromShop;
    boolean homeDelivery;
    Long maximumDeliveryDistance;
    Float minimumOrder;
    Float deliveryCharges;
    Date deliveryStartTime;
    Date deliveryEndTime;

    public SellerSettings(Long id, Long userId, Address address, Date shopOpenTime, Date shopCloseTime, boolean pickupFromShop, boolean homeDelivery, Long maximumDeliveryDistance, Float minimumOrder, Float deliveryCharges, Date deliveryStartTime, Date deliveryEndTime) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.shopOpenTime = shopOpenTime;
        this.shopCloseTime = shopCloseTime;
        this.pickupFromShop = pickupFromShop;
        this.homeDelivery = homeDelivery;
        this.maximumDeliveryDistance = maximumDeliveryDistance;
        this.minimumOrder = minimumOrder;
        this.deliveryCharges = deliveryCharges;
        this.deliveryStartTime = deliveryStartTime;
        this.deliveryEndTime = deliveryEndTime;
    }

    public SellerSettings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Date getShopOpenTime() {
        return shopOpenTime;
    }

    public void setShopOpenTime(Date shopOpenTime) {
        this.shopOpenTime = shopOpenTime;
    }

    public Date getShopCloseTime() {
        return shopCloseTime;
    }

    public void setShopCloseTime(Date shopCloseTime) {
        this.shopCloseTime = shopCloseTime;
    }

    public boolean isPickupFromShop() {
        return pickupFromShop;
    }

    public void setPickupFromShop(boolean pickupFromShop) {
        this.pickupFromShop = pickupFromShop;
    }

    public boolean isHomeDelivery() {
        return homeDelivery;
    }

    public void setHomeDelivery(boolean homeDelivery) {
        this.homeDelivery = homeDelivery;
    }

    public Long getMaximumDeliveryDistance() {
        return maximumDeliveryDistance;
    }

    public void setMaximumDeliveryDistance(Long maximumDeliveryDistance) {
        this.maximumDeliveryDistance = maximumDeliveryDistance;
    }

    public Float getMinimumOrder() {
        return minimumOrder;
    }

    public void setMinimumOrder(Float minimumOrder) {
        this.minimumOrder = minimumOrder;
    }

    public Float getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(Float deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public Date getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(Date deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }

    public Date getDeliveryEndTime() {
        return deliveryEndTime;
    }

    public void setDeliveryEndTime(Date deliveryEndTime) {
        this.deliveryEndTime = deliveryEndTime;
    }
}
