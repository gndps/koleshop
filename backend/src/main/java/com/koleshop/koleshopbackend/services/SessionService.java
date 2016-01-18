package com.koleshop.koleshopbackend.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.ThreadManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koleshop.koleshopbackend.db.connection.DatabaseConnection;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.db.models.RestCallResponse;
import com.koleshop.koleshopbackend.db.models.Session;
import com.koleshop.koleshopbackend.gcm.GcmHelper;
import com.koleshop.koleshopbackend.utils.CommonUtils;
import com.koleshop.koleshopbackend.utils.Md5Hash;
import com.koleshop.koleshopbackend.utils.RestClient;

public class SessionService {

    private static final Logger logger = Logger.getLogger(SessionService.class.getName());

    public RestCallResponse requestOneTimePassword(Long phone, String deviceId, int deviceType, int sessionType) {
        Long userId;
        String sessionId;
        userId = getUserId(phone);
        String reasonText;
        if (userId > 0) {
            sessionId = createSession(userId, deviceId, deviceType, sessionType);
            if (sessionId != null && !sessionId.isEmpty()) {
                int otp = CommonUtils.generateOtp(4);
                boolean otpSaved = saveOtpInDatabase(otp, sessionId);
                if (otpSaved) {
                    int gatewayNumber = Constants.USE_GATEWAY_NUMBER;
                    String gatewayUrl = null;// = Constants.SMS_GATEWAY_URL;
                    HashMap<String, String> hashmap = new HashMap<>();
                    if (gatewayNumber == 1) {
                        hashmap.put("to", String.valueOf(phone));
                        hashmap.put("message", otp + " is your one time code for koleshop");
                        gatewayUrl = Constants.SMS_GATEWAY_URL;
                    } else if (gatewayNumber == 2) {
                        hashmap.put("mobiles", String.valueOf(phone));
                        hashmap.put("message", otp + " is your one time code for koleshop");
                        gatewayUrl = Constants.SMS_GATEWAY_URL_2;
                    } else if (gatewayNumber == 3) {
                        hashmap.put("numbers", String.valueOf(phone));
                        hashmap.put("message", otp + " is your one time code for koleshop");
                        gatewayUrl = Constants.SMS_GATEWAY_URL_3;
                    }
                    RestCallResponse otpRequestResponse = RestClient.sendGet(gatewayUrl, hashmap);
                    if (otpRequestResponse.getStatus().equalsIgnoreCase("success")) {
                        RestCallResponse restCallResponse = new RestCallResponse();
                        restCallResponse.setStatus("success");
                        restCallResponse.setData("otp will shorty be received");
                        restCallResponse.setReason(null);
                        return restCallResponse;
                    } else {
                        RestCallResponse restCallResponse = new RestCallResponse();
                        restCallResponse.setStatus("failure");
                        restCallResponse.setData(null);
                        restCallResponse.setReason(otpRequestResponse.getReason());
                        return restCallResponse;
                    }
                } else {
                    reasonText = "-- otp not saved for phone = " + phone + " and user id = " + userId + " --";
                }
            } else {
                reasonText = "-- session not created for phone = " + phone + " and user id = " + userId + " --";
            }
        } else {
            reasonText = "-- user not created for phone = " + phone + " and user id = " + userId + " --";
        }

        logger.log(Level.SEVERE, reasonText);
        RestCallResponse restCallResponse = new RestCallResponse();
        restCallResponse.setStatus("failure");
        restCallResponse.setData(null);
        restCallResponse.setReason(reasonText);
        return restCallResponse;

    }

    public RestCallResponse verifyOneTimePassword(Long phone, int otp) {
        Long userId;
        userId = getUserId(phone);
        RestCallResponse restCallResponse = new RestCallResponse();
        if (userId > 0) {
            Connection dbConnection = null;
            PreparedStatement preparedStatement = null;

            String query = "select otp.session_id,s.session_type from OneTimePassword otp join Session s on otp.session_id=s.id where s.user_id=? and one_time_password=? and generation_time > DATE_ADD(CURRENT_TIMESTAMP, INTERVAL(-"+ Constants.OTP_TIME_TO_LIVE +") MINUTE)";

            try {
                dbConnection = DatabaseConnection.getConnection();
                preparedStatement = dbConnection.prepareStatement(query);
                preparedStatement.setLong(1, userId);
                preparedStatement.setInt(2, otp);
                System.out.println(query);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs != null && rs.first()) {
                    //otp verified
                    String sessionId = rs.getString(1);
                    int sessionType = rs.getInt(2);
                    restCallResponse.setStatus("success");
                    restCallResponse.setData("{userId:\"" + userId + "\",sessionId:\"" + sessionId + "\"}");
                    restCallResponse.setReason(null);
                    validateSession(sessionId);
                    if(sessionType == Constants.USER_SESSION_TYPE_SELLER) {
                        createAddressAndShopSettingsIfNotAlreadyExists(userId);
                        createUserInventoryFromGlobalInventory(userId);
                    }
                } else {
                    //otp verification failed
                    restCallResponse.setStatus("failure");
                    restCallResponse.setReason("otp verification failed");
                    restCallResponse.setData(null);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                restCallResponse.setStatus("failure");
                restCallResponse.setReason("otp verification failed");
                restCallResponse.setData(null);
            }
        } else {
            restCallResponse.setStatus("failure");
            restCallResponse.setReason("otp verification failed");
            restCallResponse.setData(null);
        }
        return restCallResponse;
    }

    private void createAddressAndShopSettingsIfNotAlreadyExists(Long userId) {
        Connection dbConnection;
        PreparedStatement preparedStatement;
        Long addressId = null;
        boolean alreadyExists = false;

        String query = "select id from Address where user_id=? and address_type='" + Constants.ADDRESS_TYPE_SELLER + "'";

        try {
            //check if address already exists
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            System.out.println(query);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null && rs.first()) {
                addressId = rs.getLong(1);
                if(addressId!=null && addressId>0) {
                    alreadyExists = true;
                } else {
                    //insert shop address, if not already exists
                    query = "insert into Address(user_id, address_type) values (?,'" + Constants.ADDRESS_TYPE_SELLER + "')";
                    preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    preparedStatement.setLong(1, userId);
                    System.out.println(query);
                    int executed = preparedStatement.executeUpdate();
                    ResultSet rsSession = preparedStatement.getGeneratedKeys();
                    if (executed > 0 && rsSession != null && rsSession.next()) {
                        addressId = rsSession.getLong(1);
                    } else {
                        logger.severe("address not generated for userId " + userId);
                    }
                }
            }

            if(!alreadyExists && addressId > 0) {
                //create seller settings
                query = "insert into SellerSettings(user_id, address_id) values (?,?)";
                preparedStatement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setLong(1, userId);
                System.out.println(query);
                int executed = preparedStatement.executeUpdate();
                ResultSet rsSession = preparedStatement.getGeneratedKeys();
                if (executed > 0 && rsSession != null && rsSession.next()) {
                    rsSession.getLong(1);
                } else {
                    logger.severe("settings not generated for userId " + userId);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createUserInventoryFromGlobalInventory(final Long userId) {
        //run this process in background
        Runnable r = new Runnable() {
            public void run() {
                Connection dbConnection = null;
                PreparedStatement preparedStatement = null;
                String query = "select count(*) as cnt from Product where user_id=?";
                try {
                    dbConnection = DatabaseConnection.getConnection();
                    preparedStatement = dbConnection.prepareStatement(query);
                    preparedStatement.setLong(1, userId);
                    System.out.println(query);
                    ResultSet rs = preparedStatement.executeQuery();
                    if(rs!=null && rs.next()) {
                            int count = rs.getInt(1);
                            if(count>0) {
                                //products are already there - this is an old customer
                            } else {
                                //todo add products to user inventory - start stored procedure
                                query = "call make_user_inventory(?);";
                                preparedStatement = dbConnection.prepareStatement(query);
                                preparedStatement.setLong(1, userId);
                                int update = preparedStatement.executeUpdate();
                                if(update>0) {
                                    JsonObject notificationJsonObject = new JsonObject();
                                    notificationJsonObject.addProperty("type", Constants.GCM_NOTI_USER_INVENTORY_CREATED);
                                    notificationJsonObject.addProperty("deviceAdded", true);
                                    GcmHelper.notifyUser(userId, notificationJsonObject, Constants.GCM_NOTI_COLLAPSE_KEY_INVENTORY_CREATED);
                                } else {
                                    //some problem occurred
                                }
                            }
                    } else {
                        //FATAL - some problem occurred
                        logger.log(Level.SEVERE, "problem while making UserInventory for userId = " + userId);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "problem while making UserInventory for userId = " + userId);
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
            }
        };
        ThreadFactory tf = ThreadManager.currentRequestThreadFactory();
        tf.newThread(r).start();
    }

    private void validateSession(String sessionId) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String query = "update Session set valid='1' where id=?";
        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, sessionId);
            System.out.println(query);
            int executed = preparedStatement.executeUpdate();
            if (executed > 0) {
                System.out.print("Session validated with id = " + sessionId);
            } else {
                System.out.print("Session NOT validated with id = " + sessionId);
            }
        } catch (Exception e) {
            System.out.print("Session NOT validated with id = " + sessionId);
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
    }

    private static String createSession(Long userId, String deviceId, int deviceType, int sessionType) {
        //device id is google_registration_id for android devices
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String sessionId = "";
        try {
            dbConnection = DatabaseConnection.getConnection();
            String query = "insert into DeviceUser(device_id, user_id, device_type) values (?,?,?) on duplicate key update user_id=?, device_type=?";
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, deviceId);
            preparedStatement.setLong(2, userId);
            preparedStatement.setInt(3, deviceType);
            preparedStatement.setLong(4, userId);
            preparedStatement.setInt(5, deviceType);
            if (preparedStatement.executeUpdate() > 0) {

                //device user added or already existed...now create a invalid session that should be validated only after phone number verification
                query = "insert into Session(id,session_type,user_id) values (?,?,?)";
                String insertSessionId = UUID.randomUUID().toString();
                preparedStatement = dbConnection.prepareStatement(query);
                preparedStatement.setString(1, insertSessionId);
                preparedStatement.setInt(2, sessionType);
                preparedStatement.setLong(3, userId);
                int executed = preparedStatement.executeUpdate();

                if (executed > 0) {
                    sessionId = insertSessionId;
                } else {
                    //problem while creating session
                }
            } else {
                //problem while creating device user
            }

        } catch (SQLException e) {
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
            } finally {
                return sessionId;
            }

        }

    }

    private static Long getUserId(Long phone) {
        //device id is registration id for android devices
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Long userId = 0L;
        String query = "select id from User where phone=? and country_code=" + Constants.COUNTRY_CODE + ";";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, phone);
            System.out.println(query);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null && rs.first()) {
                //user already exists
                userId = rs.getLong(1);
            } else {
                //create user and return userId
                query = "insert into User(phone, country_code) values (?," + Constants.COUNTRY_CODE + ");";
                preparedStatement = dbConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                preparedStatement.setLong(1, phone);
                int executed = preparedStatement.executeUpdate();
                ResultSet rsSession = preparedStatement.getGeneratedKeys();
                if (executed > 0 && rsSession != null && rsSession.next()) {
                    userId = rsSession.getLong(1);
                }
            }
        } catch (Exception e) {
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
            } finally {
                return userId;
            }
        }
    }

    private static boolean saveOtpInDatabase(int otp, String sessionId) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean success = false;
        String query = "insert into OneTimePassword(one_time_password,session_id) values (?,?)";
        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setInt(1, otp);
            preparedStatement.setString(2, sessionId);
            System.out.println(query);
            int executed = preparedStatement.executeUpdate();
            if (executed > 0) {
                //one time password saved in db
                success = true;
            }
        } catch (Exception e) {
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
            } finally {
                return success;
            }
        }

    }

    public static boolean verifyUserAuthenticity(Long userId, String sessionId) {

        if(userId==null || sessionId==null || userId<1 || sessionId.isEmpty()) {
            return false;
        }

        boolean userValid = false;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String query = "select valid from Session where user_id=? and id=?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, sessionId);
            System.out.println(query);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs != null && rs.first() && rs.getBoolean(1)) {
                //valid session of user
                userValid = true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        logger.log(Level.INFO, "authentic request from userId = " + userId);
        return userValid;
    }

    @Deprecated
    public RestCallResponse login(String username, String password, String deviceId, int deviceType) {
        //device id is registration id for android devices
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        RestCallResponse restCallResponse = new RestCallResponse();

        String query = "select id,email,isValid from User where username=? and password =?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, Md5Hash.hashPassword(password));

            System.out.println(query);

            // execute select SQL statement
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                query = "insert into DeviceUser(deviceId, userId, deviceType) values (?,?,?) on duplicate key update userId=?, deviceType=?";
                preparedStatement = dbConnection.prepareStatement(query);
                preparedStatement.setString(1, deviceId);
                preparedStatement.setString(2, username);
                preparedStatement.setInt(3, deviceType);
                preparedStatement.setString(4, username);
                preparedStatement.setInt(5, deviceType);
                if (preparedStatement.executeUpdate() > 0) {
                    String sessionId = UUID.randomUUID().toString();
                    query = "insert into Session(sessionId, startDate, username) values (?,now(),?)";
                    preparedStatement = dbConnection.prepareStatement(query);
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, username);
                    if (preparedStatement.executeUpdate() > 0) {
                        Session session = new Session();
                        session.setSessionId(sessionId);
                        //session.setUsername(username);
                        //session.setUserId(userId);

                        Gson gson = new Gson();
                        String result = gson.toJson(session);
                        restCallResponse.setStatus("success");
                        restCallResponse.setReason(null);
                        restCallResponse.setData(result);
                    } else {
                        restCallResponse.setStatus("failure");
                        restCallResponse.setReason("Could not create session");
                        restCallResponse.setData(null);
                    }
                } else {
                    restCallResponse.setStatus("failure");
                    restCallResponse.setReason("Could not add device");
                    restCallResponse.setData(null);
                }
            } else {
                restCallResponse.setStatus("failure");
                restCallResponse.setReason("Invalid Username or Password");
                restCallResponse.setData(null);
            }

            return restCallResponse;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            restCallResponse.setStatus("failure");
            restCallResponse.setReason(e.getMessage());
            restCallResponse.setData(null);
            return restCallResponse;

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

    }

    @Deprecated
    public RestCallResponse isUsernameAvailable(String username, String uniqueId) {

        Connection dbConnection = null;
        PreparedStatement ps = null;
        RestCallResponse restCallResponse = new RestCallResponse();

        String query = "select isValid from User where username=?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            ps = dbConnection.prepareStatement(query);
            ps.setString(1, username);
            System.out.println(query);

            // execute select SQL statement
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                restCallResponse.setStatus("success");
                restCallResponse.setData("no~" + uniqueId);
                restCallResponse.setReason(null);
            } else {
                restCallResponse.setStatus("success");
                restCallResponse.setData("yes" + uniqueId);
                restCallResponse.setReason(null);
            }
            return restCallResponse;

        } catch (SQLException e) {

            restCallResponse.setStatus("failure");
            restCallResponse.setReason(e.getMessage());
            restCallResponse.setData("~" + uniqueId);
            logger.log(Level.SEVERE, e.getMessage(), e);
            return restCallResponse;

        } finally {

            try {
                if (ps != null) {
                    ps.close();
                }

                if (dbConnection != null) {
                    dbConnection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }

    }

    @Deprecated
    public RestCallResponse register(String username, String password, String registrationId, String email, int deviceType) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement2 = null;
        RestCallResponse restCallResponse = new RestCallResponse();

        //prepared statement
        String query = "insert into User (username,password,email) values (?,?,?)";
        String query2 = "insert into DeviceUser (deviceId, userId, deviceType) values (?,?,?)";

        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement2 = dbConnection.prepareStatement(query2);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, Md5Hash.hashPassword(password));
            preparedStatement.setString(3, email);
            preparedStatement2.setString(1, registrationId);
            preparedStatement2.setString(2, username);
            preparedStatement2.setInt(3, deviceType);

            int userEntry = preparedStatement.executeUpdate();
            int deviceUserEntry = 0;
            try {
                deviceUserEntry = preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                if (e.getMessage().startsWith("Duplicate entry")) {
                    query2 = "update DeviceUser set userId=?, deviceType=? where deviceId=?";
                    preparedStatement2 = dbConnection.prepareStatement(query2);
                    preparedStatement2.setString(1, username);
                    preparedStatement2.setInt(2, deviceType);
                    preparedStatement2.setString(3, registrationId);
                    deviceUserEntry = preparedStatement2.executeUpdate();
                }
            }
            if (userEntry > 0) {
                if (deviceUserEntry > 0) {
                    String sessionId = UUID.randomUUID().toString();
                    System.out.println(username + " registered successfully");
                    query = "insert into Session (sessionId, startDate, username) values (?,now(),?)";
                    preparedStatement = dbConnection.prepareStatement(query);
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, username);
                    if (preparedStatement.executeUpdate() > 0) {
                        Session session = new Session();
                        session.setSessionId(sessionId);
                        //session.setUsername(username);
                        Gson gson = new Gson();
                        String result = gson.toJson(session);
                        restCallResponse.setStatus("success");
                        restCallResponse.setReason(null);
                        restCallResponse.setData(result);
                    } else {
                        Session session = new Session();
                        session.setSessionId(null);
                        //session.setUsername(username);
                        Gson gson = new Gson();
                        String result = gson.toJson(session);
                        restCallResponse.setStatus("success");
                        restCallResponse.setReason("Could not create session");
                        restCallResponse.setData(result);

                    }
                } else {
                    restCallResponse.setStatus("failure");
                    restCallResponse.setReason("Could not Add Device");
                    restCallResponse.setData(null);
                }
                return restCallResponse;
            } else {
                System.out.println(username + " registration failed");
                restCallResponse.setStatus("failure");
                restCallResponse.setReason("Could not Sign Up");
                restCallResponse.setData(null);
                return restCallResponse;
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            restCallResponse.setStatus("failure");
            restCallResponse.setReason(e.getMessage());
            restCallResponse.setData(null);
            return restCallResponse;

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

    }

    //todo will have to change this
    public RestCallResponse chooseSessionType(String sessionId, int sessionType) {
        Connection dbConnection = null;
        PreparedStatement ps = null;
        RestCallResponse restCallResponse = new RestCallResponse();

        String query = "update Session set sessionType=? where sessionId=?";

        try {
            dbConnection = DatabaseConnection.getConnection();
            ps = dbConnection.prepareStatement(query);
            ps.setInt(1, sessionType);
            ps.setString(2, sessionId);

            if (ps.executeUpdate() > 0) {
                query = "select * from Session where sessionId=?";
                ps = dbConnection.prepareStatement(query);
                ps.setString(1, sessionId);
                ResultSet resultSet = ps.executeQuery();
                if (resultSet.first()) {
                    sessionId = resultSet.getString("sessionId");
                    sessionType = resultSet.getInt("sessionType");
                    String username = resultSet.getString("username");
                    Session session = new Session();
                    session.setSessionId(sessionId);
                    session.setSessionType(sessionType);
                    //session.setUsername(username);
                    String data = new Gson().toJson(session);
                    restCallResponse.setStatus("success");
                    restCallResponse.setData(data);
                    restCallResponse.setReason("Session created successfully");
                }
            } else {
                restCallResponse.setStatus("failure");
                restCallResponse.setData(null);
                restCallResponse.setReason("Could not update sessionType for sessionId " + sessionId);
            }
            return restCallResponse;

        } catch (SQLException e) {

            restCallResponse.setStatus("failure");
            restCallResponse.setReason(e.getMessage());
            restCallResponse.setData(null);
            logger.log(Level.SEVERE, e.getMessage(), e);
            return restCallResponse;

        } finally {

            try {
                if (ps != null) {
                    ps.close();
                }

                if (dbConnection != null) {
                    dbConnection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }
    }

}
