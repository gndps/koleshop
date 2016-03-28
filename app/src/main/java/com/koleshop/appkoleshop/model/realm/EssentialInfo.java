package com.koleshop.appkoleshop.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 27/03/16.
 */
public class EssentialInfo extends RealmObject {

    private Long callUsPhone;

    @PrimaryKey
    private int apiVersion;

    public EssentialInfo() {
    }

    public EssentialInfo(Long callUsPhone, int apiVersion) {
        this.callUsPhone = callUsPhone;
        this.apiVersion = apiVersion;
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
}
