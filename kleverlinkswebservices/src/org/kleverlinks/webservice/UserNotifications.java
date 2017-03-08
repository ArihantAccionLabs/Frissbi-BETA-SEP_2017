package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
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

@Path("UserNotificationsService")
public class UserNotifications {

		
		@GET  
	    @Path("/insertUserNotifications/{userId}/{senderUserId}/{notificationMasterId}/{showStatus}/{notificationDateTime}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String insertUserNotifications(@PathParam("userId") Long userId,
				@PathParam("senderUserId") Long senderUserId, @PathParam("notificationMasterId") Long notificationMasterId,
				@PathParam("showStatus") Long showStatus, @PathParam("notificationDateTime") Timestamp notificationDateTime ){
			Connection conn = null;
			Statement stmt = null;
			String notificationId="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_InsertUserNotification(?,?,?,?,?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setLong(1, userId);
				callableStatement.setLong(2, senderUserId);
				callableStatement.setLong(3, notificationMasterId);
				callableStatement.setLong(4, showStatus);
				callableStatement.setTimestamp(5, notificationDateTime);
				callableStatement.registerOutParameter(6, Types.INTEGER);
				callableStatement.registerOutParameter(7, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				notificationId = callableStatement.getInt(6)+"";
              System.out.println("notificationId============="+notificationId);
			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		   ServiceUtility.closeSatetment(stmt);
			return notificationId;
		}
	
		@GET  
	    @Path("/getUserNotifications/{userId}/{notificationId}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String getUserNotifications(@PathParam("userId") Long userId,@PathParam("notificationId") Long notificationId){
			Connection conn = null;
			CallableStatement callableStatement = null;
			JSONArray jsonResultsArray = new JSONArray();
			try {
				conn = DataSourceConnection.getDBConnection();
				String insertStoreProc = "{call usp_GetUserNotification(?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setLong(1, userId);
				callableStatement.setLong(2, notificationId);
				callableStatement.execute();
				ResultSet rs = callableStatement.getResultSet();

				while(rs.next()){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserNotificationID", rs.getString("UserNotificationID"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("NotificationName", rs.getString("NotificationName"));
					jsonObject.put("NotificationMessage", rs.getString("NotificationMessage"));
					jsonResultsArray.put(jsonObject);
				}
			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			ServiceUtility.closeConnection(conn);
		   ServiceUtility.closeCallableSatetment(callableStatement);
			return jsonResultsArray.toString();
		}
}
