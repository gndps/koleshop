package com.koleshop.appkoleshop.model;

import org.parceler.Parcel;

import io.realm.OrderLiteRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 19/03/16.
 */

@Parcel(implementations = {OrderLiteRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {OrderLite.class})
public class OrderLite extends RealmObject {

    @PrimaryKey
    private Long orderId;
    private String name;
    private float amount;
    private int status;
    private String imageUrl;

    public OrderLite() {
    }

    public OrderLite(Long orderId, String name, float amount, int status, String imageUrl) {
        this.orderId = orderId;
        this.name = name;
        this.amount = amount;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
