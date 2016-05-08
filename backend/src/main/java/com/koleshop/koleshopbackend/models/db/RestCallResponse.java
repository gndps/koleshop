package com.koleshop.koleshopbackend.models.db;

public class RestCallResponse {

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getData() {
        return data;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setData(String data) {
        this.data = data;
    }

    private String status;
    private String reason;
    private String data;

    @Override
    public String toString() {
        return "RestCallResponse [status=" + status + ", reason=" + reason + ", data="
                + data + "]";
    }
}
