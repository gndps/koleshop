package com.kolshop.kolshopbackend.db.models;

public class Session {

    private String sessionId;
    private String username;
    private int userId;
    private String email;
    private int sessionType;

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Session [sessionId=" + sessionId + ", username=" + username + ",sessionType="
                + sessionType + ", email=" + email + ", userId=" + userId + "]";
    }

}
