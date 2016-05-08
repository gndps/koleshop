package com.koleshop.koleshopbackend.models.db;

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

    public static KoleResponse failedResponse() {
        KoleResponse kr = new KoleResponse();
        kr.setSuccess(false);
        kr.setData(null);
        return kr;
    }

    public static KoleResponse successResponse() {
        KoleResponse kr = new KoleResponse();
        kr.setSuccess(true);
        kr.setData(null);
        return kr;
    }

    public static KoleResponse failedResponse(String reason) {
        KoleResponse kr = new KoleResponse();
        kr.setStatus(reason);
        kr.setData(null);
        return kr;
    }
}
