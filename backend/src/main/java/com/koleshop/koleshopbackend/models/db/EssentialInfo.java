package com.koleshop.koleshopbackend.models.db;

/**
 * Created by Gundeep on 27/03/16.
 */
public class EssentialInfo {

    Long callUsPhone;
    int apiVersion;
    private String latestAppVersion;
    private String deprecatedAppVersion;
    private Long deprecatedDate;
    private Long dateToday;

    public EssentialInfo(Long callUsPhone, int apiVersion, String latestAppVersion, String deprecatedAppVersion, Long deprecatedDate, Long dateToday) {
        this.callUsPhone = callUsPhone;
        this.apiVersion = apiVersion;
        this.latestAppVersion = latestAppVersion;
        this.deprecatedAppVersion = deprecatedAppVersion;
        this.deprecatedDate = deprecatedDate;
        this.dateToday = dateToday;
    }

    public Long getCallUsPhone() {
        return callUsPhone;
    }

    public void setCallUsPhone(Long callUsPhone) {
        this.callUsPhone = callUsPhone;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getLatestAppVersion() {
        return latestAppVersion;
    }

    public void setLatestAppVersion(String latestAppVersion) {
        this.latestAppVersion = latestAppVersion;
    }

    public String getDeprecatedAppVersion() {
        return deprecatedAppVersion;
    }

    public void setDeprecatedAppVersion(String deprecatedAppVersion) {
        this.deprecatedAppVersion = deprecatedAppVersion;
    }

    public Long getDeprecatedDate() {
        return deprecatedDate;
    }

    public void setDeprecatedDate(Long deprecatedDate) {
        this.deprecatedDate = deprecatedDate;
    }

    public Long getDateToday() {
        return dateToday;
    }

    public void setDateToday(Long dateToday) {
        this.dateToday = dateToday;
    }
}
