package com.kolshop.kolshopbackend.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.kolshop.kolshopbackend.common.Constants;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.db.models.Session;
import com.kolshop.kolshopbackend.utils.CommonUtils;
import com.kolshop.kolshopbackend.utils.Md5Hash;
import com.kolshop.kolshopbackend.utils.RestClient;

public class SessionService {

    public RestCallResponse requestOneTimePassword(Long phone, String deviceId, int deviceType, int sessionType) {
        Long userId, sessionId;
        userId = getUserId(phone);
        String reasonText;
        if(userId > 0) {
            sessionId = createSession(userId, deviceId, deviceType, sessionType);
            if(sessionId > 0) {
                int otp = CommonUtils.generateOtp(4);
                boolean otpSaved = saveOtpInDatabase(otp, sessionId);
                if(otpSaved) {
                    HashMap<String, String> hashmap = new HashMap<>();
                    hashmap.put("to", String.valueOf(phone));
                    hashmap.put("message", "Code : " + otp + " . Enter this code to confirm your KolShop account. Please ignore if not requested");
                    RestCallResponse otpRequestResponse = RestClient.sendGet(Constants.SMS_GATEWAY_URL, hashmap);
                    if(otpRequestResponse.getStatus().equalsIgnoreCase("success")) {
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

        System.err.print(reasonText);
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
        if(userId>0) {
            Connection dbConnection = null;
            PreparedStatement preparedStatement = null;

            String query = "select session_id from OneTimePassword where user_id=? and one_time_password =? and generation_time > DATE_ADD(CURRENT_TIMESTAMP, INTERVAL(-15) MINUTES)";

            try {
                dbConnection = DatabaseConnection.getConnection();
                preparedStatement = dbConnection.prepareStatement(query);
                preparedStatement.setLong(1, userId);
                preparedStatement.setInt(2, otp);
                System.out.println(query);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs!=null && rs.first()) {
                    //otp verified
                    Long sessionId = rs.getLong(1);
                    restCallResponse.setStatus("success");
                    restCallResponse.setData("{userId:" + userId + "}");
                    restCallResponse.setReason(null);
                    validateSession(sessionId);
                } else {
                    //otp verification failed
                    restCallResponse.setStatus("failure");
                    restCallResponse.setReason("otp verification failed");
                    restCallResponse.setData(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    private void validateSession(Long sessionId) {
        Connection dbConnection;
        PreparedStatement preparedStatement;
        String query = "update Session set valid='1' where id=?";
        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setLong(1, sessionId);
            System.out.println(query);
            int executed = preparedStatement.executeUpdate();
            if (executed>0) {
                System.out.print("Session validated with id = " + sessionId);
            } else {
                System.out.print("Session NOT validated with id = " + sessionId);
            }
        } catch (Exception e) {
            System.out.print("Session NOT validated with id = " + sessionId);
            e.printStackTrace();
        }
    }

    private static Long createSession(Long userId, String deviceId, int deviceType, int sessionType) {
        //device id is registration id for android devices
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Long sessionId = 0L;
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
                query = "insert into Session(session_type,user_id) values (?,?)";
                preparedStatement = dbConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                preparedStatement.setInt(1, sessionType);
                preparedStatement.setLong(2, userId);
                int executed = preparedStatement.executeUpdate();

                ResultSet rsSession = preparedStatement.getGeneratedKeys();
                if (executed > 0 && rsSession != null && rsSession.next()) {
                    sessionId = rsSession.getLong(1);
                } else {
                    //problem while creating session
                }
            } else {
                //problem while creating device user
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
                    userId = rs.getLong(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                return userId;
            }
        }
    }

    private static boolean saveOtpInDatabase(int otp, Long sessionId) {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean success = false;
        String query = "insert into OneTimePassword(one_time_password,user_id,session_id) values (?,?)";
        try {
            dbConnection = DatabaseConnection.getConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setInt(1, otp);
            preparedStatement.setLong(2, sessionId);
            System.out.println(query);
            int executed = preparedStatement.executeUpdate();
            if (executed>0) {
                //one time password saved in db
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                return success;
            }
        }

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
                e.printStackTrace();
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
            e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
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
            e.printStackTrace();
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
                e.printStackTrace();
            }

        }
    }

}
