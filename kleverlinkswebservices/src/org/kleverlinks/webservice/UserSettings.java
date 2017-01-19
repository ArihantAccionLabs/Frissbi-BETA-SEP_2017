package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.util.service.ServiceUtility;

@Path("UserSettingsService")
public class UserSettings {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	@GET  
    @Path("/insertUserPreferredLocations/{userId}/{latitude}/{longitude}/{locationName}/{locationType}/{isDefault}"
    		)  
    @Produces(MediaType.TEXT_PLAIN)
	public String insertUserPreferredLocations(@PathParam("userId") int userId,
			@PathParam("latitude") String latitude,@PathParam("longitude") String longitude,
			@PathParam("locationName") String locationName,@PathParam("locationType") int locationType,
			@PathParam("isDefault") int isDefault
			) {
		JSONObject finalJson =new JSONObject();
		Connection conn = null;
		Statement stmt = null;
		int isError = 0;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUserPreferredLocations(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, latitude);
			callableStatement.setString(3, longitude);
			callableStatement.setString(4, locationName);
			callableStatement.setInt(5, locationType);
			callableStatement.setInt(6, isDefault);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate	();
			isError = callableStatement.getInt(7);
            System.out.println("isError====="+isError+"  value==="+value);
            if(isError == 0 && value == 1){
            	finalJson.put("status", true);
            	finalJson.put("isInserted", true);
            	finalJson.put("message", "Location saved successfully");
            	return finalJson.toString();
            }
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeSatetment(stmt);//closing Statement
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}
	
	@GET  
    @Path("/updateUserPreferredLocations/{userPreferredLocationID}/{userId}/{latitude}/{longitude}/{locationName}/{locationType}/{isDefault}"
    		)  
    @Produces(MediaType.TEXT_PLAIN)
	public String updateUserPreferredLocations(@PathParam("userPreferredLocationID") int userPreferredLocationID,@PathParam("userId") int userId,
			@PathParam("latitude") String latitude,@PathParam("longitude") String longitude,
			@PathParam("locationName") String locationName,@PathParam("locationType") int locationType,
			@PathParam("isDefault") int isDefault
			) {

		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserPreferredLocations(?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userPreferredLocationID);
			callableStatement.setInt(2, userId);
			callableStatement.setString(3, latitude);
			callableStatement.setString(4, longitude);
			callableStatement.setString(5, locationName);
			callableStatement.setInt(6, locationType);
			callableStatement.setInt(7, isDefault);
			callableStatement.registerOutParameter(8, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(8)+"";

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeSatetment(stmt);//closing Statement
		return isError;
	}
	
	@GET  
    @Path("/getUserPreferredLocations/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserPreferredLocations(@PathParam("userId") int userId) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject finalJson =new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserPreferredLocations(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserPreferredLocationID", rs.getString("UserPreferredLocationID"));
				jsonObject.put("UserID", rs.getInt("UserID"));
				jsonObject.put("Latitude", rs.getString("Latitude"));
				jsonObject.put("Longitude", rs.getString("Longitude"));
				jsonObject.put("LocationName", rs.getString("LocationName"));
				jsonObject.put("LocationType", rs.getString("LocationType"));
				jsonObject.put("IsDefault", rs.getString("IsDefault"));
				jsonResultsArray.put(jsonObject);
			}
			

			finalJson.put("status", true);
			finalJson.put("message", "Getting preferred locations successfully");
			finalJson.put("preferred_location_array", jsonResultsArray);
			return finalJson.toString();
			
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeSatetment(stmt);//closing Statement
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}
	
	@GET  
    @Path("/getExistenceUserPreferredLocations/{userId}/{locationName}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistenceUserPreferredLocations(@PathParam("userId") int userId,
			@PathParam("locationName") String locationName) {

		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		JSONObject finalJson =new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetExistenceUserPreferredLocations(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, locationName);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				jsonObject.put("IfLocationExists", rs.getString("IfLocationExists"));
				jsonObject.put("IsDefaultExists", rs.getString("IsDefaultExists"));
			}
			
			finalJson.put("status", true);
			finalJson.put("message", "Getting existence location successfully");
			finalJson.put("existence_preferred_location", jsonObject);
			return finalJson.toString();
			
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeSatetment(stmt);//closing Statement
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}
	@GET  
    @Path("/insertUpdateUserAlarmSettings/{userId}/{alarmTime}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String insertUpdateUserAlarmSettings(@PathParam("userId") int userId,
			@PathParam("alarmTime") Timestamp alarmTime
			) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		String isError = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_InsertUpdateUserAlarmSettings(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setTimestamp(2, alarmTime);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3)+"";

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeCallableSatetment(callableStatement);//closing Statement
		return isError;
	}
	
	@GET  
    @Path("/getUserAlarmSettings/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserAlarmSettings(@PathParam("userId") int userId
			) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetUserAlarmSettings(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				jsonObject.put("AlarmTime", rs.getString("AlarmTime"));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeCallableSatetment(callableStatement);//closing Statement
		return jsonObject.toString();
	}
	
	@GET  
    @Path("/getFrissbiPrivacyPolicy/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getFrissbiPrivacyPolicy(@PathParam("userId") int userId
			) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetFrissbiPrivacyPolicy(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				if (rs.getString("FrissbiPrivacyPolicyID").equals("1") ){
				jsonObject.put("PrivacyPolicyText", rs.getString("PrivacyPolicyText"));
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeCallableSatetment(callableStatement);//closing Statement
		return jsonObject.toString();
	}
	
	@GET  
    @Path("/getTermsandConditions/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTermsandConditions(@PathParam("userId") int userId
			) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetFrissbiPrivacyPolicy(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				if (rs.getString("FrissbiPrivacyPolicyID").equals("2") ){
				jsonObject.put("PrivacyPolicyText", rs.getString("PrivacyPolicyText"));
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);//closing connection
		ServiceUtility.closeCallableSatetment(callableStatement);//closing Statement
		return jsonObject.toString();
	}

	public static void main(String []args){
		UserSettings userSettings = new UserSettings();
		//System.out.println(userSettings.getUserPreferredOriginsByUserID(54));
		System.out.println(userSettings.getUserPreferredLocations(56));
	}
}
