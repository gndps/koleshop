package com.kolshop.kolshopbackend.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.google.gson.Gson;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.db.models.Session;
import com.kolshop.kolshopbackend.utils.Md5Hash;

public class SessionService {

	public RestCallResponse login(String username, String password, String deviceId, int deviceType) {
		//device id is registration id for android devices
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		RestCallResponse restCallResponse = new RestCallResponse();
	
		
		String query = "select isValid from User where username=? and password =?";
		
		try {
			dbConnection = DatabaseConnection.getConnection();
			preparedStatement = dbConnection.prepareStatement(query);
			preparedStatement.setString(1,username);
			preparedStatement.setString(2, Md5Hash.hashPassword(password));
 
			System.out.println(query);
 
			// execute select SQL statement
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.first()) {
				query = "insert into DeviceUser(deviceId, userId, deviceType) values (?,?,?) on duplicate key update userId=?, deviceType=?";
				preparedStatement = dbConnection.prepareStatement(query);
				preparedStatement.setString(1, deviceId);
				preparedStatement.setString(2, username);
				preparedStatement.setInt(3, deviceType);
				preparedStatement.setString(4, username);
				preparedStatement.setInt(5, deviceType);
				if(preparedStatement.executeUpdate()>0)
				{
					String sessionId = UUID.randomUUID().toString();
					query = "insert into Session(sessionId, startDate, username) values (?,now(),?)";
					preparedStatement = dbConnection.prepareStatement(query);
					preparedStatement.setString(1, sessionId);
					preparedStatement.setString(2, username);
					if(preparedStatement.executeUpdate()>0)
					{
							Session session = new Session();
							session.setSessionId(sessionId);
							session.setUsername(username);
							Gson gson = new Gson();
							String result = gson.toJson(session);
							restCallResponse.setStatus("success");
							restCallResponse.setReason(null);
							restCallResponse.setData(result);
					}
					else{
							restCallResponse.setStatus("failure");
							restCallResponse.setReason("Could not create session");
							restCallResponse.setData(null);
							
					}
				}
				else{
					restCallResponse.setStatus("failure");
					restCallResponse.setReason("Could not add device");
					restCallResponse.setData(null);
				}
			}
			else{
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}
 
	}

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
			if(rs.first()){
				restCallResponse.setStatus("success");
				restCallResponse.setData("no~"+uniqueId);
				restCallResponse.setReason(null);
			}
			else{
				restCallResponse.setStatus("success");
				restCallResponse.setData("yes"+uniqueId);
				restCallResponse.setReason(null);
			}
			return restCallResponse;
			
		} catch (SQLException e) {
 
			restCallResponse.setStatus("failure");
			restCallResponse.setReason(e.getMessage());
			restCallResponse.setData("~"+uniqueId);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}
 
	}

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
			try{
				deviceUserEntry = preparedStatement2.executeUpdate();
			}
			catch(SQLException e)
			{
				if(e.getMessage().startsWith("Duplicate entry"))
				{
					query2 = "update DeviceUser set userId=?, deviceType=? where deviceId=?";
					preparedStatement2 = dbConnection.prepareStatement(query2);
					preparedStatement2.setString(1, username);
					preparedStatement2.setInt(2, deviceType);
					preparedStatement2.setString(3, registrationId);
					deviceUserEntry = preparedStatement2.executeUpdate();
				}
			}
			if(userEntry>0)
			{
				if(deviceUserEntry>0)
				{
					String sessionId = UUID.randomUUID().toString();
					System.out.println(username + " registered successfully");
					query = "insert into Session (sessionId, startDate, username) values (?,now(),?)";
					preparedStatement = dbConnection.prepareStatement(query);
					preparedStatement.setString(1, sessionId);
					preparedStatement.setString(2, username);
					if(preparedStatement.executeUpdate()>0)
					{
						Session session = new Session();
						session.setSessionId(sessionId);
						session.setUsername(username);
						Gson gson = new Gson();
						String result = gson.toJson(session);
						restCallResponse.setStatus("success");
						restCallResponse.setReason(null);
						restCallResponse.setData(result);
					}
					else{
						Session session = new Session();
						session.setSessionId(null);
						session.setUsername(username);
						Gson gson = new Gson();
						String result = gson.toJson(session);
						restCallResponse.setStatus("success");
						restCallResponse.setReason("Could not create session");
						restCallResponse.setData(result);
						
					}
				}
				else
				{
					restCallResponse.setStatus("failure");
					restCallResponse.setReason("Could not Add Device");
					restCallResponse.setData(null);
				}
				return restCallResponse;
			}
			else{
				System.out.println(username+" registration failed");
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}
 
	}

	public RestCallResponse chooseSessionType(String sessionId,
			int sessionType) {
		Connection dbConnection = null;
		PreparedStatement ps = null;
		RestCallResponse restCallResponse = new RestCallResponse();
 
		String query = "update Session set sessionType=? where sessionId=?";
		
		try {
			dbConnection = DatabaseConnection.getConnection();
			ps = dbConnection.prepareStatement(query);
			ps.setInt(1, sessionType);
			ps.setString(2, sessionId);
 
			if(ps.executeUpdate()>0){
				query = "select * from Session where sessionId=?";
				ps = dbConnection.prepareStatement(query);
				ps.setString(1, sessionId);
				ResultSet resultSet = ps.executeQuery();
				if(resultSet.first()) {
				    sessionId = resultSet.getString("sessionId");
				    sessionType = resultSet.getInt("sessionType");
				    String username = resultSet.getString("username");
				    Session session = new Session();
				    session.setSessionId(sessionId);
				    session.setSessionType(sessionType);
				    session.setUsername(username);
				    String data = new Gson().toJson(session);
					restCallResponse.setStatus("success");
					restCallResponse.setData(data);
					restCallResponse.setReason("Session created successfully");
				}
			}
			else{
				restCallResponse.setStatus("failure");
				restCallResponse.setData(null);
				restCallResponse.setReason("Could not update sessionType for sessionId "+sessionId);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}
	}
	
}
