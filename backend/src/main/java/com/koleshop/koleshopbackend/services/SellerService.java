package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 17/02/16.
 */
public class SellerService {

    private static final Logger logger = Logger.getLogger(SellerService.class.getName());

    public boolean openCloseShop(Long sellerId, boolean open) {
        Connection dbConnection;
        PreparedStatement preparedStatement = null;
        String query = "update SellerStatus set shop_open=? where seller_id=?";
        dbConnection = DatabaseConnection.getConnection();
        boolean updated = false;
        try {
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setBoolean(1, open);
            preparedStatement.setLong(2, sellerId);
            int update = preparedStatement.executeUpdate();
            if(update>0) {
                updated = true;
            } else {
                updated = false;
            }
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception in open close shop for seller_id = " + sellerId, e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
        return updated;
    }

    public boolean updateProfilePicture(Long sellerId, String imageUrl, boolean headerImage) {
        Connection dbConnection;
        PreparedStatement preparedStatement = null;
        String query;
        if(headerImage) {
            query = "update SellerSettings set header_image_url=? where user_id=?";
        } else {
            query = "update SellerSettings set image_url=? where user_id=?";
        }
        dbConnection = DatabaseConnection.getConnection();
        boolean updated = false;
        try {
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, imageUrl);
            preparedStatement.setLong(2, sellerId);
            int update = preparedStatement.executeUpdate();
            if(update>0) {
                updated = true;
            } else {
                updated = false;
            }
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception in updating seller image " + (headerImage?"header ":"") + "url for seller_id = " + sellerId, e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
        return updated;
    }

}
