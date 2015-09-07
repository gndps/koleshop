package com.kolshop.kolshopbackend.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kolshop.kolshopbackend.common.MessageType;
import com.kolshop.kolshopbackend.db.connection.DatabaseConnection;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.db.models.ShopSettings;
import com.kolshop.kolshopbackend.gcm.GcmHelper;

public class ShopService {
	
	public String getShopNamesWithOwnerID(String ownerId) {
		 
		Connection dbConnection = null;
		Statement statement = null;
		String result ="";
 
		String query = "SELECT Name from shop where OwnerID='_id'";
		query = query.replace("_id", ownerId);
		try {
			dbConnection = DatabaseConnection.getConnection();
			statement = dbConnection.createStatement();
 
			System.out.println(query);
 
			// execute select SQL stetement
			ResultSet rs = statement.executeQuery(query);
			int i=0;
			while (rs.next()) {
 
				String shopName = rs.getString("name");
				if(i==0){
					result+=shopName;
					i++;
				}
				else{
					result+="~"+shopName;
				}
			}
			
			return result;
			
		} catch (SQLException e) {
 
			System.out.println(e.getMessage());
			return "error";
 
		} finally {
 
			try {
				if (statement != null) {
					statement.close();
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

	@SuppressWarnings("unchecked")
	public RestCallResponse saveShopSettings(String shopSettingsString) {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		RestCallResponse restCallResponse = new RestCallResponse();
		final Map<String, String> shopSettingsHashMap = new Gson().fromJson(shopSettingsString, new TypeToken<Map<String, String>>(){}.getType());
		
        Iterator it = shopSettingsHashMap.entrySet().iterator();
        String query = "INSERT INTO ShopSettings (Username, SettingName, SettingValue, UpdateTime) values (?,?,?,?) "
            	+ 	"ON DUPLICATE KEY UPDATE "
            	+	"Username = IF((@update_settings := (UpdateTime < VALUES(UpdateTime))), VALUES(Username), Username),"
            	+	"SettingValue = IF(@update_settings, VALUES(SettingValue), SettingValue),"
   				+	"UpdateTime = IF(@update_settings, VALUES(UpdateTime), UpdateTime);";
        dbConnection = DatabaseConnection.getConnection();
        try{
        	boolean atleastOneSettingUpdated = false;
	        while(it.hasNext())
	        {
	            Map.Entry pairs = (Map.Entry) it.next();
	            ShopSettings shopSettings = new Gson().fromJson(pairs.getValue().toString(), ShopSettings.class);
	            if(shopSettings.getSettingName()!=null && shopSettings.getSettingValue()!=null && !shopSettings.getSettingValue().equalsIgnoreCase(""))
	            {	
	            	try {
	            		java.util.Date dateNow = new java.util.Date();
	        			preparedStatement = dbConnection.prepareStatement(query);
	        			preparedStatement.setString(1, shopSettings.getUsername());
	        			preparedStatement.setString(2, shopSettings.getSettingName());
	        			preparedStatement.setString(3, shopSettings.getSettingValue());
	        			preparedStatement.setTimestamp(4, shopSettings.getUpdateTime());
	        			
						int settingsEntry = preparedStatement.executeUpdate();
						
						ShopSettings ss = new ShopSettings();
						ss.setSyncedToServer(true);
						if(settingsEntry>0)
						{
							atleastOneSettingUpdated = true;
							ss.setUsername(shopSettings.getUsername());
							ss.setSettingName(shopSettings.getSettingName());
							ss.setSettingValue(shopSettings.getSettingValue());
							ss.setUpdateTime(shopSettings.getUpdateTime());
							ss.setSyncedToServer(true);
						}
						else
						{
							String selectQuery = "SELECT * from ShopSetting where Username = ? and SettingName = ?;";
							preparedStatement = dbConnection.prepareStatement(selectQuery);
							preparedStatement.setString(1,shopSettings.getUsername());
							preparedStatement.setString(2, shopSettings.getSettingName());
							ResultSet rs = preparedStatement.executeQuery();
							if(rs.first())
							{
								ss.setUsername(shopSettings.getUsername());
								ss.setSettingName(rs.getString("SettingName"));
								ss.setSettingValue(rs.getString("SettingValue"));
								ss.setUpdateTime(rs.getTimestamp("UpdateTime"));
								ss.setSyncedToServer(true);
							}
						}
						
						String broadcastSettingsString = new Gson().toJson(ss);
						JsonObject data = new JsonObject();
						data.addProperty("type", MessageType.SHOP_SETTINGS.name());
						data.addProperty("settings", broadcastSettingsString);
						GcmHelper.broadcastToUser(shopSettings.getUsername(), data);
						
	            	} catch (Exception e) {
						e.printStackTrace();
					}
				}
	        }
	        if(atleastOneSettingUpdated)
	        {
	        	restCallResponse.setStatus("success");
	        	restCallResponse.setReason("");
	        	restCallResponse.setData("settings updated");
	        	return restCallResponse;
	        }
	        else
	        {
	        	throw new Exception("no settings updated");
	        }
        } catch (Exception e) {
		 
        			e.printStackTrace();
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
					catch( Exception e) {
						e.printStackTrace();
					}
		}
	}
	
	public static List<String> getRegistrationIdsForUser(String username)
	{
		List<String> deviceIdsList = null;
		Connection dbConnection = null;
		Statement statement = null;
 
		String query = "SELECT deviceId from deviceUser where userID=?";
		
		try {
			dbConnection = DatabaseConnection.getConnection();
			PreparedStatement ps = dbConnection.prepareStatement(query);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				if(deviceIdsList == null)
				{
					deviceIdsList = new LinkedList<String>();
				}
				String deviceId = rs.getString("deviceId");
				deviceIdsList.add(deviceId);
			}
			
			return deviceIdsList;
			
		} catch (SQLException e) {
 
			System.out.println(e.getMessage());
			return null;
 
		} finally {
 
			try {
				if (statement != null) {
					statement.close();
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
