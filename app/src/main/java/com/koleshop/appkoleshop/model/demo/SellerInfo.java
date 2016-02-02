package com.koleshop.appkoleshop.model.demo;

import org.parceler.Parcel;

/**
 * Created by Gundeep on 28/01/16.
 */

@Parcel
public class SellerInfo {

    private String name;
    private String timings;
    private boolean online;
    private String imageUrl;
    private Double gpsLong;
    private Double gpsLat;

    public SellerInfo() {
    }

    public SellerInfo(String name, String timings, boolean online, String imageUrl, Double gpsLong, Double gpsLat) {
        this.name = name;
        this.timings = timings;
        this.online = online;
        this.imageUrl = imageUrl;
        this.gpsLong = gpsLong;
        this.gpsLat = gpsLat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimings() {
        return timings;
    }

    public void setTimings(String timings) {
        this.timings = timings;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getGpsLong() {
        return gpsLong;
    }

    public void setGpsLong(Double gpsLong) {
        this.gpsLong = gpsLong;
    }

    public Double getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(Double gpsLat) {
        this.gpsLat = gpsLat;
    }
}
