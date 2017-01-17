package org.util.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.AuthenticateUser;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.NotificationsEnum;
import org.kleverlinks.webservice.UserNotifications;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.UserDTO;

public class ServiceUtility {

	public static UserDTO getUserDetailsByUserId(Integer userId) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT emailName,firstName,lastName FROM tbl_users WHERE userId ='" + userId + "'" + " limit 1";
			rs = stmt.executeQuery(sql);
			userDTO = new UserDTO();
			while (rs.next()) {
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setFullName(rs.getString("firstName") + rs.getString("lastName"));
			}
			return userDTO;
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return null;
	}

	public static UserDTO getUserDetailsByUserName(String userName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT userId,emailName,firstName,lastName FROM tbl_users WHERE userName ='" + userName + "'" + " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setUserId(rs.getInt("userId"));
				userDTO.setFullName(rs.getString("firstName")+"" + rs.getString("lastName")+"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return userDTO;

	}
	
	
	public static UserDTO getUserDetailsByUserNameAndEmail(String userName , String emailId) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT userId , userName , EmailName  FROM tbl_users WHERE userName ='" + userName + "' OR EmailName ='"+ emailId +"'" + " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setUserId(rs.getInt("userId"));
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setUserName(rs.getString("userName"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return userDTO;

	}
	
	public static void sendNotification(int userId1 , String meassage){
		System.out.println("meassage===="+meassage);
		UserNotifications userNotifications = new UserNotifications();
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		String notificationId = userNotifications.insertUserNotifications(userId1, 0,
				NotificationsEnum.Meeting_Request_Acceptance.ordinal() + 1, 0, timestamp);
		JSONArray jsonArray = new JSONArray(
				userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)));
		if (jsonArray.length() > 0) {

			JSONObject json = jsonArray.getJSONObject(0);

			//String notificationMessage = json.getString("NotificationMessage");
			String NotificationName = json.getString("NotificationName");
			Sender sender = new Sender(Constants.GCM_APIKEY);
			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
					.addData("message", meassage).addData("NotificationName", NotificationName)
					.build();

			try {
				AuthenticateUser authenticateUser = new AuthenticateUser();
				JSONObject jsonObject = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId1));
				String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
				Result result = sender.send(message, deviceRegistrationId, 1);
				System.out.println(result);
				System.out.println("Notification sent successfully");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
