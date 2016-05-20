package com.koleshop.koleshopbackend.services;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.gcm.GcmHelper;
import com.koleshop.koleshopbackend.models.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.models.db.Address;
import com.koleshop.koleshopbackend.models.db.EssentialInfo;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.models.db.SellerSettings;
import com.koleshop.koleshopbackend.models.gcm.Message;
import com.koleshop.koleshopbackend.utils.DatabaseConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 10/01/16.
 */
public class CommonService {

    private static final Logger logger = Logger.getLogger(CommonService.class.getName());

    public KoleResponse saveOrUpdateAddress(Address addressObject) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        KoleResponse response = new KoleResponse();

        Long addressId = addressObject.getId();
        Long userId = addressObject.getUserId();
        String address = addressObject.getAddress();
        int addressType = addressObject.getAddressType();
        String name = addressObject.getName();
        String nickname = addressObject.getNickname();
        Long phone = addressObject.getPhoneNumber();
        int countryCode = addressObject.getCountryCode();
        Double gpsLong = addressObject.getGpsLong();
        Double gpsLat = addressObject.getGpsLat();

        String query;

        logger.log(Level.INFO, "save/update address");

        if (addressId != null && addressId > 0) {

            logger.log(Level.INFO, "updating...");

            //update address and phone
            query = "update Address set name=?, address=?, address_type=?, phone_number=?, country_code=?, nickname=?, gps_long=?, gps_lat=?" +
                    " where user_id=? and id=?";

            try {
                dbConnection = DatabaseConnection.getConnection();
                preparedStatement = dbConnection.prepareStatement(query);

                preparedStatement.setString(1, name);
                preparedStatement.setString(2, address);
                preparedStatement.setInt(3, addressType);
                preparedStatement.setLong(4, phone);
                preparedStatement.setInt(5, countryCode);
                preparedStatement.setString(6, nickname);
                preparedStatement.setDouble(7, gpsLong);
                preparedStatement.setDouble(8, gpsLat);
                preparedStatement.setLong(9, userId);
                preparedStatement.setLong(10, addressId);
                logger.log(Level.INFO, "update address query = " + preparedStatement.toString());
                int update = preparedStatement.executeUpdate();
                if (update > 0) {
                    //address updated
                    response.setSuccess(true);
                    logger.log(Level.INFO, "address updated");
                    response.setData(addressObject);
                } else {
                    //address update failed
                    response.setStatus("address_not_updated");
                    response.setData(null);
                }

                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
                return response;

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                response.setStatus(e.getLocalizedMessage());
                response.setData(null);
                return response;
            } finally {
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }

        } else {
            logger.log(Level.INFO, "saving...");
            //add new address
            query = "insert into Address(user_id,name,address,address_type,phone_number,country_code,nickname,gps_long,gps_lat)" +
                    " values (?,?,?,?,?,?,?,?,?)";

            try {
                dbConnection = DatabaseConnection.getConnection();
                preparedStatement = dbConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

                preparedStatement.setLong(1, userId);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, address);
                preparedStatement.setInt(4, addressType);
                preparedStatement.setLong(5, phone);
                preparedStatement.setInt(6, countryCode);
                preparedStatement.setString(7, nickname);
                preparedStatement.setDouble(8, gpsLong);
                preparedStatement.setDouble(9, gpsLat);

                logger.log(Level.INFO, "saving new address=" + preparedStatement.toString());
                int executed = preparedStatement.executeUpdate();
                ResultSet rsAddress = preparedStatement.getGeneratedKeys();
                if (executed > 0 && rsAddress != null && rsAddress.next()) {
                    //address added successfully
                    addressId = rsAddress.getLong(1);
                    addressObject = new Address(addressId, userId, name, address, addressType, phone, countryCode, nickname, gpsLong, gpsLat);
                    response.setSuccess(true);
                    response.setData(addressObject);
                } else {
                    //address adding failed
                    response.setStatus("address_not_updated");
                    response.setData(null);
                }

                DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
                return response;

            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                response.setStatus(e.getLocalizedMessage());
                response.setData(null);
                return response;
            } finally {
                DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
            }

        }
    }

    public KoleResponse getAddresses(Long userId) {
        return null;
    }

    public KoleResponse updateSellerSettings(Long userId, SellerSettings settings) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        KoleResponse response = new KoleResponse();

        Address addressObject = settings.getAddress();

        int openTime = settings.getShopOpenTime();
        int closeTime = settings.getShopCloseTime();
        logger.log(Level.INFO, "updating shop settings...");
        logger.log(Level.INFO, "shop open time : " + openTime);
        logger.log(Level.INFO, "shop close time : " + closeTime);
        boolean pickupFromShop = settings.isPickupFromShop();
        boolean homeDelivery = settings.isHomeDelivery();
        Long maxDeliveryDistance = settings.getMaximumDeliveryDistance();
        Float minOrder = settings.getMinimumOrder();
        Float deliveryCharges = settings.getDeliveryCharges();
        Float carryBagCharges = settings.getCarryBagCharges();
        int deliveryStartTime = settings.getDeliveryStartTime();
        int deliveryEndTime = settings.getDeliveryEndTime();
        logger.log(Level.INFO, "deliveryStartTime: " + deliveryStartTime);
        logger.log(Level.INFO, "deliveryEndTime: " + deliveryEndTime);
        String address = null;
        String shopName = null;
        Long phone = null;
        int countryCode = 91;
        Double gpsLong = null;
        Double gpsLat = null;

        if (addressObject != null) {
            shopName = addressObject.getName();
            phone = addressObject.getPhoneNumber();
            countryCode = addressObject.getCountryCode();
            gpsLong = addressObject.getGpsLong();
            gpsLat = addressObject.getGpsLat();
            address = addressObject.getAddress();
        }

        String query;

        //update address and settings
        query = "update Address a join SellerSettings ss on a.id = ss.address_id and a.user_id = ss.user_id and ss.user_id=?" +
                " set a.name=?, a.address=?, a.phone_number=?, a.country_code=?, a.nickname=?, a.gps_long=?, a.gps_lat=?," +
                " ss.open_time=?, ss.close_time=?, ss.pickup_from_shop=?, ss.home_delivery=?, ss.max_delivery_distance=?," +
                " ss.min_order=?, ss.delivery_charges=?, ss.carry_bag_charges=?, ss.delivery_start_time=?, ss.delivery_end_time=?" +
                " where a.user_id=? and a.address_type='" + Constants.ADDRESS_TYPE_SELLER + "'";

        logger.log(Level.INFO, "query=" + query);

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, shopName);
            preparedStatement.setString(3, address);
            preparedStatement.setLong(4, phone);
            preparedStatement.setInt(5, countryCode);
            preparedStatement.setString(6, Constants.SELLER_ADDRESS_NICKNAME);
            preparedStatement.setDouble(7, gpsLong);
            preparedStatement.setDouble(8, gpsLat);
            preparedStatement.setInt(9, openTime);
            preparedStatement.setInt(10, closeTime);
            preparedStatement.setBoolean(11, pickupFromShop);
            preparedStatement.setBoolean(12, homeDelivery);
            preparedStatement.setLong(13, maxDeliveryDistance == null ? 0l : maxDeliveryDistance);
            preparedStatement.setFloat(14, minOrder == null ? 0f : minOrder);
            preparedStatement.setFloat(15, deliveryCharges == null ? 0 : deliveryCharges);
            preparedStatement.setFloat(16, carryBagCharges == null ? 0 : carryBagCharges);
            preparedStatement.setInt(17, deliveryStartTime);
            preparedStatement.setInt(18, deliveryEndTime);
            preparedStatement.setLong(19, userId);

            logger.log(Level.INFO, "prepared statement = " + preparedStatement.toString());

            int update = preparedStatement.executeUpdate();
            if (update > 0) {
                //settings updated
                logger.log(Level.INFO, "prepare statement update executed");
                response.setSuccess(true);
                response.setData("settings_updated");

                //delete any old caches stored in phones
                Message gcmMessage = new Message.Builder()
                        .collapseKey(Constants.GCM_NOTI_COLLAPSE_KEY_DELETE_OLD_SETTINGS_CACHE)
                        .priority(Message.Priority.HIGH)
                        .addData("type", Constants.GCM_NOTI_DELETE_OLD_SETTINGS_CACHE)
                        .build();
                GcmHelper.notifyUser(userId, gcmMessage, 2);
            } else {
                //settings update
                logger.log(Level.INFO, "prepare statement update did not work...settings not updated");
                response.setStatus("settings_not_updated");
                response.setData(null);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return response;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQLException = " + e.getLocalizedMessage(), e);
            response.setStatus(e.getLocalizedMessage());
            response.setData(null);
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            response.setStatus(e.getLocalizedMessage());
            response.setData(null);
            return response;
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }

    }

    public KoleResponse getSellerSettings(Long userId) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        KoleResponse response = new KoleResponse();

        String query;

        //get address and settings
        query = "select a.id as address_id, ss.id as seller_settings_id, ss.image_url, ss.header_image_url, ss.carry_bag_charges," +
                " a.name, a.address, a.phone_number, a.country_code, a.nickname, a.gps_long, a.gps_lat," +
                " ss.open_time, ss.close_time, ss.home_delivery, ss.max_delivery_distance," +
                " ss.min_order, ss.delivery_charges, ss.carry_bag_charges, ss.delivery_start_time, ss.delivery_end_time, shop_open" +
                " from Address a join SellerSettings ss on a.id = ss.address_id and a.user_id = ss.user_id and ss.user_id=?" +
                " join SellerStatus seller_status on seller_status.seller_id=ss.user_id" +
                " where a.address_type='" + Constants.ADDRESS_TYPE_SELLER + "'";

        try {
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            preparedStatement = dbConnection.prepareStatement(query);

            preparedStatement.setLong(1, userId);

            logger.log(Level.INFO, "prepared statement=" + preparedStatement.toString());

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String imageUrl = rs.getString("image_url");
                String headerImageUrl = rs.getString("header_image_url");
                Long addressId = rs.getLong("address_id");
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

                Address address = new Address(addressId, userId, addressName, addressString, 1, phoneNumber, countryCode, nickname, gpsLongitude, gpsLatitude);
                SellerSettings sellerSettings = new SellerSettings(sellerSettingsId, userId, imageUrl, headerImageUrl, address, openTime, closeTime, true, homeDelivery, maxDeliveryDistance,
                        minOrder, deliveryCharges, carryBagCharges, deliveryStartTime, deliveryEndTime, shopOpen);
                response.setSuccess(true);
                response.setData(sellerSettings);
            }

            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return response;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            response.setStatus(e.getLocalizedMessage());
            response.setData(null);
            return response;
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
    }

    public KoleResponse saveFeedback(String message, String deviceModel, String deviceManufacturer, String osVersion, String heightDp, String widthDp, String screenSize, String deviceTime, String sessionType,
                                     String gpsLat, String gpsLong, String networkName, String isWifiConnected, String userId, String sessionId, String version, String versionCode) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "insert into Feedback(message, device_model, device_manufacturer, os_version, height_dp, width_dp, screen_size, device_time, session_type, gps_lat, gps_long, network_name, is_wifi, user_id, session_id, app_version, app_version_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            int index = 1;
            preparedStatement.setString(index++, message == null ? "" : message);
            preparedStatement.setString(index++, deviceModel == null ? "" : deviceModel);
            preparedStatement.setString(index++, deviceManufacturer == null ? "" : deviceManufacturer);
            preparedStatement.setString(index++, osVersion == null ? "" : osVersion);
            preparedStatement.setString(index++, heightDp == null ? "" : heightDp);
            preparedStatement.setString(index++, widthDp == null ? "" : widthDp);
            preparedStatement.setString(index++, screenSize == null ? "" : screenSize);
            preparedStatement.setString(index++, deviceTime == null ? "" : deviceTime);
            preparedStatement.setString(index++, sessionType == null ? "" : sessionType);
            preparedStatement.setString(index++, gpsLat == null ? "" : gpsLat);
            preparedStatement.setString(index++, gpsLong == null ? "" : gpsLong);
            preparedStatement.setString(index++, networkName == null ? "" : networkName);
            preparedStatement.setString(index++, isWifiConnected == null ? "" : isWifiConnected);
            preparedStatement.setString(index++, userId == null ? "" : userId);
            preparedStatement.setString(index++, sessionId == null ? "" : sessionId);
            preparedStatement.setString(index++, version == null ? "" : version);
            preparedStatement.setString(index++, versionCode == null ? "" : versionCode);

            logger.log(Level.INFO, "will execute this prepared statement=" + preparedStatement.toString());

            preparedStatement.execute();
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return KoleResponse.successResponse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "problem while capturing feedback", e);
            return KoleResponse.failedResponse();
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
    }

    public KoleResponse getEssentialInfo(boolean isBuyer) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        logger.info("getting essential info");

        String query = "select * from EssentialInfo order by api_version desc limit 1";

        try {
            dbConnection = DatabaseConnection.getConnection();
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            preparedStatement = dbConnection.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();
            if(rs!=null) {
                rs.first();
            }
            Long callUsPhone = rs.getLong("call_us_phone");
            int apiVersion = rs.getInt("api_version");
            String latestAppVersion = rs.getString("latest_app_version");
            String deprecatedAppVersion = rs.getString("deprecated_app_version");
            Long deprecatedDate = rs.getTimestamp("deprecated_date")!=null?rs.getTimestamp("deprecated_date").getTime():Constants.TIME_IN_FAR_FUTURE;
            Long dateToday = rs.getDate("date_today")!=null?rs.getTimestamp("date_today").getTime():new Date().getTime();
            EssentialInfo essentialInfo = new EssentialInfo(callUsPhone, apiVersion, latestAppVersion, deprecatedAppVersion, deprecatedDate, dateToday);

            KoleResponse response = new KoleResponse();
            response.setData(essentialInfo);
            response.setSuccess(true);
            DatabaseConnectionUtils.closeStatementAndConnection(preparedStatement, dbConnection);
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "problem while getting essential info", e);
            return KoleResponse.failedResponse();
        } finally {
            DatabaseConnectionUtils.finallyCloseStatementAndConnection(preparedStatement, dbConnection);
        }
    }
}
