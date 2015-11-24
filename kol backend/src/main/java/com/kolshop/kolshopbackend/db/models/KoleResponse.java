package com.kolshop.kolshopbackend.db.models;

/**
 * Created by Gundeep on 16/10/15.
 */
public class KoleResponse {

    public static String STATUS_KOLE_RESPONSE_SUCCESS = "success";
    public static String STATUS_KOLE_RESPONSE_FAILURE = "failure";

    String status;
    Object data; //will act as data in case of success and as reason string in case of failure

    public KoleResponse(String status, Object data) {
        this.status = status;
        this.data = data;
    }

    public KoleResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String success) {
        this.status = status;
    }

    public void setSuccess(boolean success) {
        if(success) {
            status = STATUS_KOLE_RESPONSE_SUCCESS;
        } else {
            status = STATUS_KOLE_RESPONSE_FAILURE;
        }
    }

    public boolean getSuccess() {
        if(status==null) {
            return false;
        } else {
            return status.equalsIgnoreCase(STATUS_KOLE_RESPONSE_SUCCESS);
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
