package com.gndps.kolshopmaterial.model;

/**
 * Created by gundeepsingh on 17/08/14.
 */
public class RestCallResponse {

    private String status;
    private String reason;
    private String data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RestCallResponse [status=" + status + ", reason=" + reason + ", data="
                + data + "]";
    }
}
