package com.koleshop.appkoleshop.model;

import java.sql.Timestamp;

/**
 * Created by gundeepsingh on 29/08/14.
 */
public class ShopSettings {

    private String username;
    private String settingName;
    private String settingValue;
    private Timestamp updateTime;
    private boolean syncedToServer;

    public boolean isSyncedToServer() {
        return syncedToServer;
    }

    public void setSyncedToServer(boolean syncedToServer) {
        this.syncedToServer = syncedToServer;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Address [username=" + username + ", settingName=" + settingName + ", settingValue=" + settingValue +
                ", updateTime=" + updateTime + ", syncedToServer=" + syncedToServer + "]";
    }

}
