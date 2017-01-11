package org.kleverlinks.webservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;

@Path("UserNotificationsService")
public class UserNotifications {

		
		@GET  
	    @Path("/insertUserNotifications/{userId}/{senderUserId}/{notificationMasterId}/{showStatus}/{notificationDateTime}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String insertUserNotifications(@PathParam("userId") int userId,
				@PathParam("senderUserId") int senderUserId, @PathParam("notificationMasterId") int notificationMasterId,
				@PathParam("showStatus") int showStatus, @PathParam("notificationDateTime") Timestamp notificationDateTime ){
			Connection conn = null;
			Statement stmt = null;
			String notificationId="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_InsertUserNotification(?,?,?,?,?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.setInt(2, senderUserId);
				callableStatement.setInt(3, notificationMasterId);
				callableStatement.setInt(4, showStatus);
				callableStatement.setTimestamp(5, notificationDateTime);
				callableStatement.registerOutParameter(6, Types.INTEGER);
				callableStatement.registerOutParameter(7, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				notificationId = callableStatement.getInt(6)+"";

			} catch (SQLException se) {
				// Handle errors for JDBC
				se.printStackTrace();
			} catch (Exception e) {
				// Handle errors for Class.forName
				e.printStackTrace();
			} finally {
				// finally block used to close resources
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException se2) {
				}// nothing we can do
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}// end finally try
			}// end try
			return notificationId;
		}
		
		@GET  
	    @Path("/updateUserNotifications/{notificationId}/{showStatus}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String updateUserNotifications(@PathParam("notificationId") int notificationId,
				@PathParam("showStatus") int showStatus ){
			Connection conn = null;
			Statement stmt = null;
			String isError="";
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_UpdateUserNotification(?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, notificationId);
				callableStatement.setInt(2, showStatus);
				callableStatement.registerOutParameter(3, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				isError = callableStatement.getInt(3)+"";

			} catch (SQLException se) {
				// Handle errors for JDBC
				se.printStackTrace();
			} catch (Exception e) {
				// Handle errors for Class.forName
				e.printStackTrace();
			} finally {
				// finally block used to close resources
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException se2) {
				}// nothing we can do
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}// end finally try
			}// end try
			return isError;
		}
		
		@GET  
	    @Path("/getUserNotifications/{userId}/{notificationId}")
	    @Produces(MediaType.TEXT_PLAIN)
		public String getUserNotifications(@PathParam("userId") Integer userId,@PathParam("notificationId") Integer notificationId
				){
			Connection conn = null;
			Statement stmt = null;
			JSONArray jsonResultsArray = new JSONArray();
			try {
				conn = DataSourceConnection.getDBConnection();
				stmt = conn.createStatement();
				CallableStatement callableStatement = null;
				String insertStoreProc = "{call usp_GetUserNotification(?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setInt(1, userId);
				callableStatement.setInt(2, notificationId);
				callableStatement.execute();
				ResultSet rs = callableStatement.getResultSet();

				while(rs.next()){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserNotificationID", rs.getString("UserNotificationID"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					if ( rs.getString("AvatarPath") == null ){
						jsonObject.put("AvatarPath", "");
					}else{
					jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
					}
					jsonObject.put("NotificationName", rs.getString("NotificationName"));
					jsonObject.put("NotificationMessage", rs.getString("NotificationMessage"));
					jsonResultsArray.put(jsonObject);
				}
			} catch (SQLException se) {
				// Handle errors for JDBC
				se.printStackTrace();
			} catch (Exception e) {
				// Handle errors for Class.forName
				e.printStackTrace();
			} finally {
				// finally block used to close resources
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException se2) {
				}// nothing we can do
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}// end finally try
			}// end try
			return jsonResultsArray.toString();
		}
}
