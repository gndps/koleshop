package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 20/11/15.
 */


public class UserService {

    private static final Logger logger=Logger.getLogger(UserService.class.getName());

    public static List<String> getDeviceIdsForUserId(Long userId) {
        List<String> result = null;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String query = "select device_id from DeviceUser where user_id=?";
        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            System.out.println(query);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null) {
                result = new ArrayList<>();
                while (rs.next()) {
                    String deviceId = rs.getString(1);
                    result.add(deviceId);
                }
            } else {
                //FATAL - some problem occurred
                logger.log(Level.SEVERE, "problem in getting device ids for userId = " + userId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception while getting device ids for userId = " + userId);
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return result;
    }

}
