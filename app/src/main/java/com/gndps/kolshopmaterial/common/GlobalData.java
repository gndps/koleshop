package com.gndps.kolshopmaterial.common;

import com.gndps.kolshopmaterial.model.Session;

public class GlobalData {
    private static GlobalData mInstance = null;
    private Session session;

    private GlobalData() {

    }

    public static GlobalData getInstance() {
        if (mInstance == null) {
            mInstance = new GlobalData();
        }
        return mInstance;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

}
