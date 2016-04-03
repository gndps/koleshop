package com.koleshop.appkoleshop.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 27/03/16.
 */
public class EssentialInfo extends RealmObject {

    private Long callUsPhone;
    private String latestAppVersion;
    private String deprecatedAppVersion;
    private Long deprecatedDate;
    private Long dateToday;

    @PrimaryKey
    private int apiVersion;

    public EssentialInfo() {
    }

    public EssentialInfo(Long callUsPhone, String latestAppVersion, String deprecatedAppVersion, Long deprecatedDate, int apiVersion, Long dateToday) {
        this.callUsPhone = callUsPhone;
        this.latestAppVersion = latestAppVersion;
        this.deprecatedAppVersion = deprecatedAppVersion;
        this.deprecatedDate = deprecatedDate;
        this.apiVersion = apiVersion;
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
