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

@Path("TimelineService")
public class Timeline {

		// JDBC driver name and database URL
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		
		@GET  
	    @Path("/insertUserFreeTimes/{userId}/{fromTime}/{toTime}/{description}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String insertUserFreeTimes(@PathParam("userId") int userId,
				@PathParam("fromTime") Timestamp fromTime, @PathParam("toTime") Timestamp toTime,
				@PathParam("description") String description ){
			Connection conn = null;
			Statement stmt = null;
			String isError="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_InsertUserFreeTimes(?,?,?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.setTimestamp(2, fromTime);
				callableStatement.setTimestamp(3, toTime);
				callableStatement.setString(4, description);
				callableStatement.registerOutParameter(5, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				isError = callableStatement.getInt(5)+"";
			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
			return isError;
		}
		
		@GET  
	    @Path("/updateUserFreeTimes/{userFreetimeId}/{userId}/{fromTime}/{toTime}/{description}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String updateUserFreeTimes(@PathParam("userFreetimeId") int userFreetimeId,@PathParam("userId") int userId,
				@PathParam("fromTime") Timestamp fromTime, @PathParam("toTime") Timestamp toTime,
				@PathParam("description") String description ){
			Connection conn = null;
			Statement stmt = null;
			String isError="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_UpdateUserFreeTimes(?,?,?,?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userFreetimeId);
				callableStatement.setInt(2, userId);
				callableStatement.setTimestamp(3, fromTime);
				callableStatement.setTimestamp(4, toTime);
				callableStatement.setString(5, description);
				callableStatement.registerOutParameter(6, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				isError = callableStatement.getInt(6)+"";

			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
			return isError;
		}
		
		@GET  
	    @Path("/getUserFreeTimeByUserID/{userId}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String getUserFreeTimeByUserID(@PathParam("userId") int userId
				){
			Connection conn = null;
			Statement stmt = null;
			JSONArray jsonResultsArray = new JSONArray();
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_GetUserFreeTime_ByUserID(?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.execute();
				ResultSet rs = callableStatement.getResultSet();

				while(rs.next()){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserFreeTimeID", rs.getString("UserFreeTimeID"));
					jsonObject.put("FromDateTime", rs.getString("FromDateTime"));
					jsonObject.put("ToDateTime", rs.getString("ToDateTime"));
					jsonObject.put("Description", rs.getString("Description"));
					jsonResultsArray.put(jsonObject);
				}
			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
			return jsonResultsArray.toString();
		}
		
		@GET  
	    @Path("/getUserTimelines/{userId}/{viewerUserID}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String getUserTimelines(@PathParam("userId") int userId,
				@PathParam("viewerUserID") int viewerUserID
				){
			Connection conn = null;
			Statement stmt = null;
			JSONArray jsonResultsArray = new JSONArray();
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_GetUserTimelines(?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.setInt(1, viewerUserID);
				callableStatement.execute();
				ResultSet rs = callableStatement.getResultSet();

				while(rs.next()){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserID", rs.getString("UserID"));
					jsonObject.put("Description", rs.getString("Description"));
					jsonObject.put("IsPrivate", rs.getString("IsPrivate"));
					jsonResultsArray.put(jsonObject);
				}
			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
			return jsonResultsArray.toString();
		}
		
		@GET  
	    @Path("/insertUserStatus/{userId}/{description}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String insertUserStatus(@PathParam("userId") int userId,
				@PathParam("description") String description ){
			Connection conn = null;
			Statement stmt = null;
			String isError="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_InsertUserStatus(?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.setString(2, description);
				callableStatement.registerOutParameter(3, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				isError = callableStatement.getInt(3)+"";

			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
			return isError;
		}
		
		@GET  
	    @Path("/insertUserTimeLines/{userId}/{description}/{isPrivate}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String insertUserTimeLines(@PathParam("userId") int userId,
				@PathParam("description") String description,
				@PathParam("isPrivate") int isPrivate){
			Connection conn = null;
			Statement stmt = null;
			String isError="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_InsertUserTimeLines(?,?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.setString(2, description);
				callableStatement.setInt(3, isPrivate);
				callableStatement.registerOutParameter(4, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				isError = callableStatement.getInt(4)+"";

			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
			return isError;
		}
	
		@GET
		@Path("/getUserPostedText/{userId}")
		@Produces(MediaType.TEXT_PLAIN)
		public String getUserPostedText(@PathParam("userId") int userId){
			System.out.println("userId===================="+userId);
			Connection conn = null;
			Statement stmt = null;
			
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				String sql = "SELECT * from tbl_UserTimelines WHERE UserId="+ userId;
				ResultSet rs = stmt.executeQuery(sql);
				
				while(rs.next()) {
					System.out.println("desc====="+rs.getString(3)+"  =IsPrivate== "+rs.getString(4));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}

}
