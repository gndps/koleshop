package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.db.models.Address;
import com.koleshop.koleshopbackend.db.models.InventoryCategory;
import com.koleshop.koleshopbackend.db.models.SellerSettings;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 16/02/16.
 */
public class BuyerService {

    private static final Logger logger = Logger.getLogger(BuyerService.class.getName());

    public List<SellerSettings> getNearbyShops(Long customerId, Double customerGpsLat, Double customerGpsLong, boolean homeDeliveryOnly, boolean openShopsOnly, int limit, int offset) {

        logger.info("finding nearby shops - ");

        int rectangleDistanceInMetres = 50000;
        float oneDegreeLatDistanceInMetres = 111180;
        Double long1 = customerGpsLong - rectangleDistanceInMetres / Math.abs(Math.cos(Math.toRadians(customerGpsLat)) * oneDegreeLatDistanceInMetres);
        Double long2 = customerGpsLong + rectangleDistanceInMetres / Math.abs(Math.cos(Math.toRadians(customerGpsLat)) * oneDegreeLatDistanceInMetres);
        Double lat1 = customerGpsLat - (rectangleDistanceInMetres / oneDegreeLatDistanceInMetres);
        Double lat2 = customerGpsLat + (rectangleDistanceInMetres / oneDegreeLatDistanceInMetres);

        logger.info("long1 = " + long1);
        logger.info("long2 = " + long2);
        logger.info("lat1 = " + lat1);
        logger.info("lat2 = " + lat2);

        List<SellerSettings> listOfNearbyShops = new ArrayList<>();
        Connection dbConnection = DatabaseConnection.getConnection();
        PreparedStatement preparedStatement = null;

        logger.info("finding nearby shops - - -");

        //find nearby shops
        String query = "SELECT ss.id as seller_settings_id,ss.user_id as seller_id,ss.image_url,ss.header_image_url,ss.open_time,ss.close_time,ss.pickup_from_shop," +
                "ss.home_delivery,ss.max_delivery_distance,ss.min_order,ss.delivery_charges,ss.carry_bag_charges,ss.delivery_start_time," +
                "ss.delivery_end_time,a.id as address_id,a.name,a.address,a.phone_number,a.country_code,a.nickname,a.gps_long,a.gps_lat,seller_status.shop_open," +
                " ( 6371000 * acos( cos( radians(?) ) * cos( radians( gps_lat ) ) " +
                "* cos( radians( gps_long ) - radians(?) ) + sin( radians(?) ) * sin(radians(gps_lat)) ) ) AS distance " +
                "FROM SellerSettings ss join Address a on ss.address_id=a.id " +
                "JOIN SellerStatus seller_status on ss.user_id = seller_status.seller_id " +
                "WHERE a.gps_long between ? and ? and a.gps_lat between ? and ? ";

        if (homeDeliveryOnly) {
            query += "and ss.home_delivery='1' ";
        }
        if (openShopsOnly) {
            query += "and seller_status.shop_open = '1' ";
        }

        query += "HAVING distance < " + Constants.NEARBY_SHOPS_DISTANCE + " " +
                "ORDER BY distance " +
                "LIMIT ?, ?";

        try {
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setDouble(1, customerGpsLat);
            preparedStatement.setDouble(2, customerGpsLong);
            preparedStatement.setDouble(3, customerGpsLat);
            preparedStatement.setDouble(4, long1);
            preparedStatement.setDouble(5, long2);
            preparedStatement.setDouble(6, lat1);
            preparedStatement.setDouble(7, lat2);
            preparedStatement.setInt(8, offset);
            preparedStatement.setInt(9, limit);

            logger.info("running nearby shops query : " + preparedStatement.toString());

            ResultSet rs = preparedStatement.executeQuery();

            int index = 0;

            while(rs.next()) {
                logger.info("extracting shop " + index);
                String imageUrl = rs.getString("image_url");
                String headerImageUrl = rs.getString("header_image_url");
                Long addressId = rs.getLong("address_id");
                Long sellerId = rs.getLong("seller_id");
                Long sellerSettingsId = rs.getLong("seller_settings_id");
                String addressName = rs.getString("name");
                String addressString = rs.getString("address");
                Long phoneNumber = rs.getLong("phone_number");
                int countryCode = rs.getInt("country_code");
                String nickname = rs.getString("nickname");
                Double gpsLongitude = rs.getDouble("gps_long");
                Double gpsLatitude = rs.getDouble("gps_lat");
                int openTime = rs.getInt("open_time");
                int closeTime = rs.getInt("close_time");
                boolean homeDelivery = rs.getBoolean("home_delivery");
                Long maxDeliveryDistance = rs.getLong("max_delivery_distance");
                Float minOrder = rs.getFloat("min_order");
                Float deliveryCharges = rs.getFloat("delivery_charges");
                Float carryBagCharges = rs.getFloat("carry_bag_charges");
                int deliveryStartTime = rs.getInt("delivery_start_time");
                int deliveryEndTime = rs.getInt("delivery_end_time");
                boolean shopOpen = rs.getBoolean("shop_open");

                Address address = new Address(addressId, sellerId, addressName, addressString, 1, phoneNumber, countryCode, nickname, gpsLongitude, gpsLatitude);
                SellerSettings sellerSettings = new SellerSettings(sellerSettingsId, sellerId, imageUrl, headerImageUrl, address, openTime, closeTime, true, homeDelivery, maxDeliveryDistance,
                        minOrder, deliveryCharges, carryBagCharges, deliveryStartTime, deliveryEndTime, shopOpen);
                listOfNearbyShops.add(sellerSettings);
                index ++;
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "problem in finding nearby shops", e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
        logger.info("size of listOfNearbyShops = " + listOfNearbyShops.size());
        return listOfNearbyShops;
    }

    public boolean updateProfilePicture(Long userId, String imageUrl, boolean headerImage) {
        Connection dbConnection;
        PreparedStatement preparedStatement = null;
        String query;
        if(headerImage) {
            query = "update BuyerSettings set header_image_url=? where user_id=?";
        } else {
            query = "update BuyerSettings set image_url=? where user_id=?";
        }
        dbConnection = DatabaseConnection.getConnection();
        boolean updated = false;
        try {
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, imageUrl);
            preparedStatement.setLong(2, userId);
            int update = preparedStatement.executeUpdate();
            if(update>0) {
                updated = true;
            } else {
                updated = false;
            }
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception in updating buyer image " + (headerImage?"header ":"") + "url for user_id = " + userId, e);
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
        return updated;
    }

}
