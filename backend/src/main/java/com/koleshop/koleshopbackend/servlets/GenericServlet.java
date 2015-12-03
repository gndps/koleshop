package com.koleshop.koleshopbackend.servlets;

import com.google.appengine.api.utils.SystemProperty;
import com.koleshop.koleshopbackend.common.Configuration;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class GenericServlet
 */
public class GenericServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static DataSource datasource;

    /**
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public GenericServlet() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {

        if (SystemProperty.environment.value() !=
                SystemProperty.Environment.Value.Production) {
            PoolProperties p = new PoolProperties();
            p.setUrl(Configuration.GOOGLE_CLOUD_SQL_URL);
            p.setDriverClassName("com.mysql.jdbc.Driver");
            p.setUsername(Configuration.GOOGLE_CLOUD_SQL_ALTERNATIVE_USERNAME);
            p.setPassword(Configuration.GOOGLE_CLOUD_SQL_PASSWORD);
            p.setJmxEnabled(true);
            p.setTestWhileIdle(false);
            p.setTestOnBorrow(true);
            p.setValidationQuery("SELECT 1");
            p.setTestOnReturn(false);
            p.setValidationInterval(30000);
            p.setTimeBetweenEvictionRunsMillis(30000);
            p.setMaxActive(100);
            p.setInitialSize(10);
            p.setMaxWait(10000);
            p.setRemoveAbandonedTimeout(60);
            p.setMinEvictableIdleTimeMillis(30000);
            p.setMinIdle(10);
            p.setLogAbandoned(true);
            p.setRemoveAbandoned(true);
            p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                    + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
            datasource = new DataSource();
            datasource.setPoolProperties(p);

        }

    }

    public static Connection getConnectionFromDBPool() throws SQLException {
        if (datasource != null) {
            return datasource.getConnection();
        } else {
            return null;
        }
    }

}
