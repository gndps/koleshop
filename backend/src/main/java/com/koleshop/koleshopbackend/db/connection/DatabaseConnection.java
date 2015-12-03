package com.koleshop.koleshopbackend.db.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;
import com.koleshop.koleshopbackend.servlets.GenericServlet;
import com.koleshop.koleshopbackend.common.Configuration;
import com.koleshop.koleshopbackend.common.Constants;


public class DatabaseConnection {

    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    public static Connection getConnection() {
        Connection connection = null;
        if (SystemProperty.environment.value() ==
                SystemProperty.Environment.Value.Production) {
            logger.log(Level.INFO, "will make connection using google app engine");
            connection = getGoogleAppEngineDBConnection();
        } else {
            logger.log(Level.INFO, "will make a regular connection (w/o using google app engine)");
            try {
                if (connection == null || connection.isClosed()) {
                    if(Constants.USE_LOCAL_DATABASE) {
                        connection = getLocalConnectionWithoutPool();
                    } else {
                        connection = getConnectionWithoutPool();
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.INFO, "exception while making a regular connection", e);
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
            logger.log(Level.SEVERE, "exception while creating mysql driver", e);
            return null;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "sql exception while creating mysql driver", e);
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
            logger.log(Level.SEVERE, "exception while creating mysql driver", e);
            return null;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "sql exception while creating mysql driver", e);
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
            logger.log(Level.SEVERE, "exception while creating google sql driver", e);
            return null;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "exception while creating google sql driver", e);
            return null;
        }
    }
}
