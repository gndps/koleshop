package com.koleshop.appkoleshop.model.parcel;

import org.parceler.Parcel;

import io.realm.BuyerSettingsRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Gundeep on 10/03/16.
 */
@Parcel(implementations = {BuyerSettingsRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {BuyerSettings.class})
public class BuyerSettings extends RealmObject {

    private Long id;
    @PrimaryKey
    private Long userId;
    private String name;
    private String imageUrl;
    private String headerImageUrl;

    public BuyerSettings() {
    }

    public BuyerSettings(Long id, Long userId, String name, String imageUrl, String headerImageUrl) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.headerImageUrl = headerImageUrl;
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
}