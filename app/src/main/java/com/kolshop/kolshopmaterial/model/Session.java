package com.kolshop.kolshopmaterial.model;

/**
 * Created by gundeepsingh on 17/08/14.
 */
public class Session {

    private String sessionId;
    private String username;
    private int sessionType;

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

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    @Override
    public String toString() {
        return "Session [sessionId=" + sessionId + ", username=" + username + ", sessionType=" + sessionType + "]";
    }

}
