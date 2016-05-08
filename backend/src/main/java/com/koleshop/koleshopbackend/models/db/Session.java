package com.koleshop.koleshopbackend.models.db;

public class Session {

    private String sessionId;
    private int userId;
    private int sessionType;

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Session [sessionId=" + sessionId + ",sessionType=" + sessionType + ", userId=" + userId + "]";
    }

}
