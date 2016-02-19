package com.koleshop.appkoleshop.model.parcel;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Created by Gundeep on 14/01/16.
 */

@Parcel
public class SellerSettings {

    Long id;
    Long userId;
    String imageUrl;
    String headerImageUrl;
    Address address;
    int shopOpenTime;
    int shopCloseTime;
    boolean pickupFromShop;
    boolean homeDelivery;
    Long maximumDeliveryDistance;
    Float minimumOrder;
    Float deliveryCharges;
    Float carryBagCharges;
    int deliveryStartTime;
    int deliveryEndTime;
    boolean shopOpen;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHeaderImageUrl() {
        return headerImageUrl;
    }

    public void setHeaderImageUrl(String headerImageUrl) {
        this.headerImageUrl = headerImageUrl;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getShopOpenTime() {
        return shopOpenTime;
    }

    public void setShopOpenTime(int shopOpenTime) {
        this.shopOpenTime = shopOpenTime;
    }

    public int getShopCloseTime() {
        return shopCloseTime;
    }

    public void setShopCloseTime(int shopCloseTime) {
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

    public Float getCarryBagCharges() {
        return carryBagCharges;
    }

    public void setCarryBagCharges(Float carryBagCharges) {
        this.carryBagCharges = carryBagCharges;
    }

    public int getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(int deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }

    public int getDeliveryEndTime() {
        return deliveryEndTime;
    }

    public void setDeliveryEndTime(int deliveryEndTime) {
        this.deliveryEndTime = deliveryEndTime;
    }

    public boolean isShopOpen() {
        return shopOpen;
    }

    public void setShopOpen(boolean shopOpen) {
        this.shopOpen = shopOpen;
    }
}
