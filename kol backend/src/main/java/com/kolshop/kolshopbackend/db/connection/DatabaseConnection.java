package com.kolshop.kolshopbackend.db.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.appengine.api.utils.SystemProperty;
import com.kolshop.kolshopbackend.common.Configuration;
import com.kolshop.kolshopbackend.common.Constants;
import com.kolshop.kolshopbackend.servlets.GenericServlet;


public class DatabaseConnection {

    public static Connection getConnection() {
        Connection connection = null;
        if (SystemProperty.environment.value() ==
                SystemProperty.Environment.Value.Production) {
            connection = getGoogleAppEngineDBConnection();
        } else {
            try {
                if (connection == null || connection.isClosed()) {
                    if(Constants.USE_LOCAL_DATABASE) {
                        connection = getLocalConnectionWithoutPool();
                    } else {
                        connection = getConnectionWithoutPool();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static Connection getConnectionWithoutPool() {
        Connection connection = null;

        String url = Configuration.GOOGLE_CLOUD_SQL_URL;
        String username = Configuration.GOOGLE_CLOUD_SQL_ALTERNATIVE_USERNAME;
        String password = Configuration.GOOGLE_CLOUD_SQL_PASSWORD;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Connection getLocalConnectionWithoutPool() {
        Connection connection = null;

        String url = Configuration.LOCAL_SQL_URL;
        String username = Configuration.LOCAL_SQL_USERNAME;
        String password = Configuration.LOCAL_SQL_PASSWORD;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Connection getConnectionFromPool() {

        Connection connection = null;
        try {
            connection = GenericServlet.getConnectionFromDBPool();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (connection != null) {
            return connection;
        } else {
            return null;
        }
    }

    public static Connection getGoogleAppEngineDBConnection() {
        Connection connection = null;
        String url = Configuration.GOOGLE_APP_ENGINE_CLOUD_SQL_CONNECTION_STRING;
        String username = Configuration.GOOGLE_CLOUD_SQL_USERNAME;
        String password = Configuration.GOOGLE_CLOUD_SQL_PASSWORD;
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
