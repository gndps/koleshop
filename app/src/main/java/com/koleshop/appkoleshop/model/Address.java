package com.koleshop.appkoleshop.model;

/**
 * Created by gundeepsingh on 29/08/14.
 */
public class Address {

    private int id;
    private String name;
    private int pincode;
    private String address;
    private String landmark;
    private int cityId;
    private int stateId;
    private int countryId;
    private String phone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPincode() {
        return pincode;
    }

    public void setPincode(int pincode) {
        this.pincode = pincode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Address [id=" + id + ", name=" + name + ", pincode=" + pincode +
                ", address=" + address + ", landmark=" + landmark + ", cityId=" + cityId +
                ", stateId=" + stateId + ", countryId=" + countryId + ", phone=" + phone + "]";
    }

}
