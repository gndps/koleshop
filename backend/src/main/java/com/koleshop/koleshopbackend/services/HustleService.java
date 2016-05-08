package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.models.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.models.db.KoleResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 04/05/16.
 */
public class HustleService {

    private static final Logger logger = Logger.getLogger(HustleService.class.getName());

    public static boolean isUserValid(String username, String password) {
        logger.log(Level.INFO, "Checking if user is valid...");
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select valid from AdminUser where username like ? and password like ?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            logger.log(Level.INFO, "executing prepared statement = " + preparedStatement.toString());
            ResultSet rs = preparedStatement.executeQuery();
            if(rs!=null && rs.next()) {
                boolean isUserValid = rs.getBoolean("valid");
                if(isUserValid) {
                    logger.log(Level.INFO, "user is valid");
                    return true;
                } else {
                    logger.log(Level.INFO, "user is invalid");
                    return false;
                }
            } else {
                logger.log(Level.INFO, "result set is null");
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "some problem in hustle login", e);
            return false;
        }
    }

    public static KoleResponse getRecentOrders() {
        KoleResponse koleResponse = new KoleResponse();
        koleResponse.setSuccess(true);
        koleResponse.setData("its working yo!");
        return koleResponse;
    }

}
