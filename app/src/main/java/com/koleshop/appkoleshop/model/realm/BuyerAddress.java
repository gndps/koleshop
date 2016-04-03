package com.koleshop.appkoleshop.model.realm;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.BuyerAddressRealmProxy;
import io.realm.RealmObject;

/**
 * Created by Gundeep on 08/03/16.
 */

@Parcel(implementations = {BuyerAddressRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {BuyerAddress.class})
public class BuyerAddress extends RealmObject {

    private Long id;
    private Long userId;
    private String name;
    private String address;
    private int addressType;
    private Long phoneNumber;
    private int countryCode;
    private String nickname;
    private Double gpsLong;
    private Double gpsLat;
    private boolean defaultAddress;
    private boolean validAddress;
    private Date updatedDate;
    private Date syncedToServerDate;

    public BuyerAddress() {
    }

    public BuyerAddress(Long id, Long userId, String name, String address, int addressType, Long phoneNumber, int countryCode, String nickname, Double gpsLong, Double gpsLat, boolean defaultAddress, boolean validAddress, Date updatedDate, Date syncedToServerDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.addressType = addressType;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.nickname = nickname;
        this.gpsLong = gpsLong;
        this.gpsLat = gpsLat;
        this.defaultAddress = defaultAddress;
        this.validAddress = validAddress;
        this.updatedDate = updatedDate;
        this.syncedToServerDate = syncedToServerDate;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public boolean isValidAddress() {
        return validAddress;
    }

    public void setValidAddress(boolean validAddress) {
        this.validAddress = validAddress;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Date getSyncedToServerDate() {
        return syncedToServerDate;
    }

    public void setSyncedToServerDate(Date syncedToServerDate) {
        this.syncedToServerDate = syncedToServerDate;
    }
}
