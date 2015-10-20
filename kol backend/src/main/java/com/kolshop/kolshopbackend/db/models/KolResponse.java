package com.kolshop.kolshopbackend.db.models;

/**
 * Created by Gundeep on 16/10/15.
 */
public class KolResponse {

    boolean success;
    Object data; //will act as data in case of success and as reason string in case of failure

    public KolResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public KolResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
