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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.UserDTO;
import org.util.service.ServiceUtility;

import sun.misc.BASE64Encoder;

@Path("FriendListService")
public class UserFriendList {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private static Connection getDBConnection() {

		Connection dbConnection = null;

		try {
			Class.forName(JDBC_DRIVER);

		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}

		try {
			dbConnection = DriverManager.getConnection(Constants.DB_URL, Constants.USER, Constants.PASS);
			return dbConnection;

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return dbConnection;

	}

	@GET
	@Path("/sendFriendRequest/{username1}/{username2}")
	@Produces(MediaType.TEXT_PLAIN)
	public String sendFriendRequest(@PathParam("username1") String userName1,
			@PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;

		try {
			/*
			 * conn = getDBConnection(); stmt = conn.createStatement(); String
			 * sql; sql = "SELECT userID from tbl_users where username ='" +
			 * userName1 + "'" + " limit 1"; ResultSet rs =
			 * stmt.executeQuery(sql);
			 * 
			 * while (rs.next()) { userId1 = rs.getInt("userid"); } /*sql =
			 * "SELECT userID from tbl_users where username ='" + userName2 +
			 * "'" + " limit 1"; rs = stmt.executeQuery(sql);
			 * 
			 * while (rs.next()) { userId2 = rs.getInt("userid"); }
			 */

			UserDTO userDTO = ServiceUtility.getUserDetailsByUserName(userName1);
			if (userDTO != null) {
				userId1 = userDTO.getUserId();
			}

			userDTO = ServiceUtility.getUserDetailsByUserName(userName2);
			if (userDTO != null) {
				userId2 = userDTO.getUserId();
			}
         
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is sending
			// friend request to user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			conn = getDBConnection();
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 0);
			callableStatement.setInt(4, userId1);
			callableStatement.setTimestamp(5, sqlDateNow);
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			if (value == 1) {
				UserNotifications userNotifications = new UserNotifications();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				String notificationId = userNotifications.insertUserNotifications(userId2, userId1,
						NotificationsEnum.Friend_Pending_Requests.ordinal() + 1, 0, timestamp);
				
				JSONObject json = new JSONArray(
						userNotifications.getUserNotifications(0, Integer.parseInt(notificationId))).getJSONObject(0);
				String notificationMessage = json.getString("NotificationMessage");
				String NotificationName = json.getString("NotificationName");
				Sender sender = new Sender(Constants.GCM_APIKEY);
				Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
						.addData("message", notificationMessage).addData("NotificationName", NotificationName)
						.addData("userName", userName1).build();

				try {
					AuthenticateUser authenticateUser = new AuthenticateUser();
					JSONObject jsonObject = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId2));
					String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
					Result result = sender.send(message, deviceRegistrationId, 1);
					System.out.println(result);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "1";
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return "0";
	}

	@GET
	@Path("/cancelFriendRequest/{username1}/{username2}")
	@Produces(MediaType.TEXT_PLAIN)
	public String cancelFriendRequest(@PathParam("username1") String userName1,
			@PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;
		String value = "";

		try {
			conn = getDBConnection();
			UserDTO userDTO1 = ServiceUtility.getUserDetailsByUserName(userName1);
			  if(userDTO1 != null){
				    userId1 = userDTO1.getUserId();
			  }
			
			  UserDTO userDTO2 = ServiceUtility.getUserDetailsByUserName(userName2);
			  if(userDTO2 != null){
				    userId2 = userDTO2.getUserId();
			  }
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is sending
			// friend request to user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, -1);
			callableStatement.setInt(4, userId1);
			callableStatement.setTimestamp(5, sqlDateNow);
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			value = callableStatement.executeUpdate() + "";
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return value;
	}

	@GET
	@Path("/approveFriendRequest/{username1}/{username2}")
	@Produces(MediaType.TEXT_PLAIN)
	public String approveFriendRequest(@PathParam("username1") String userName1,
			@PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;

		try {
			conn = getDBConnection();
			/*stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID,emailName,firstName,lastName FROM tbl_users WHERE username ='" + userName1 + "'"
					+ " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
				senderEmailId = rs.getString("emailName");
				senderName = rs.getString("firstName") + rs.getString("lastName");
			}

			sql = "SELECT userID,emailName,firstName,lastName FROM tbl_users WHERE username ='" + userName2 + "'"
					+ " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userId2 = rs.getInt("userid");
				acceptorEmailId = rs.getString("emailName");
				acceptorName = rs.getString("firstName") + rs.getString("lastName");
			} */
			
			 UserDTO userDTO1 = ServiceUtility.getUserDetailsByUserName(userName1);
			  if(userDTO1 != null){
				    userId1 = userDTO1.getUserId();
			  }
			
			  UserDTO userDTO2 = ServiceUtility.getUserDetailsByUserName(userName2);
			  if(userDTO2 != null){
				    userId2 = userDTO2.getUserId();
			  }

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is approving
			// friend request of user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 1);
			callableStatement.setInt(4, userId2);
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, sqlDateNow);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				UserNotifications userNotifications = new UserNotifications();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				String notificationId = userNotifications.insertUserNotifications(userId1, userId2,
						NotificationsEnum.Friend_Request_Acceptance.ordinal() + 1, 0, timestamp);
				JSONArray jsonArray = new JSONArray(
						userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)));
				if (jsonArray.length() > 0) {

					JSONObject json = jsonArray.getJSONObject(0);

					String notificationMessage = json.getString("NotificationMessage");
					String NotificationName = json.getString("NotificationName");
					Sender sender = new Sender(Constants.GCM_APIKEY);
					Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
							.addData("message", notificationMessage).addData("NotificationName", NotificationName)
							.build();

					try {
						AuthenticateUser authenticateUser = new AuthenticateUser();
						JSONObject jsonObject = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId1));
						String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);

						System.out.println(result);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// sending mails to both sender and acceptor
			
				MyEmailer.SendMail(userDTO1.getEmailId(), "Your friend request accepted by " + userDTO1.getFullName(),
						"Your friend request accepted by " + userDTO1.getFullName());
				MyEmailer.SendMail(userDTO2.getEmailId(), "Your friend request accepted by " + userDTO2.getFullName(),
						"You are friend now to " + userDTO2.getFullName());

				return "1";
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return "0";
	}

	@GET
	@Path("/rejectFriendRequest/{username1}/{username2}")
	@Produces(MediaType.TEXT_PLAIN)
	public String rejectFriendRequest(@PathParam("username1") String userName1,
			@PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName1 + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
			}

			sql = "SELECT userID from tbl_users where username ='" + userName2 + "'" + " limit 1";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId2 = rs.getInt("userid");
			}

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is rejecting
			// friend request of user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 2);
			callableStatement.setInt(4, userId2);
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, sqlDateNow);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				return "1";
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return "0";
	}

	@GET
	@Path("/unFriendRequest/{username1}/{username2}")
	@Produces(MediaType.TEXT_PLAIN)
	public String unFriendRequest(@PathParam("username1") String userName1, @PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName1 + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
			}

			sql = "SELECT userID from tbl_users where username ='" + userName2 + "'" + " limit 1";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId2 = rs.getInt("userid");
			}

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is sending
			// unfriend request to user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 3);
			callableStatement.setInt(4, userId1);
			callableStatement.setTimestamp(5, sqlDateNow);
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				return "1";
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return "0";
	}

	@GET
	@Path("/friendsList/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public String friendsList(@PathParam("username") String userName) {
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where UserID1 ='" + userId + "' and requestStatus = 1";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID2"));
			}

			sql = "Select * from tbl_userfriendlist  where UserID2 ='" + userId + "' and requestStatus = 1";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}

			Iterator<Integer> iterator = userIds.iterator();
			while (iterator.hasNext()) {
				userId = iterator.next();
				sql = "Select * from tbl_users  AS U LEFT OUTER JOIN FrissDB.tbl_usertransactions AS UT ON U.UserID = UT.UserID where U.UserID = '"
						+ userId + "'";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					if (rs.getString("AvatarPath") == null) {
						jsonObject.put("AvatarPath", "");
					} else {
						jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
					}
					jsonResultsArray.put(jsonObject);
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/pendingForApprovalList/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public String pendingForApprovalList(@PathParam("username") String userName) {
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where UserID2 ='" + userId + "' and requestStatus = 0";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}

			Iterator<Integer> iterator = userIds.iterator();
			while (iterator.hasNext()) {
				userId = iterator.next();
				sql = "Select * from tbl_users AS U LEFT OUTER JOIN FrissDB.tbl_usertransactions AS UT ON U.UserID = UT.UserID where U.UserID = '"
						+ userId + "'";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					if (rs.getString("AvatarPath") == null) {
						jsonObject.put("AvatarPath", "");
					} else {
						jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
					}
					jsonResultsArray.put(jsonObject);
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/blockedFriendsList/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public String blockedFriendsList(@PathParam("username") String userName) {
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where ActionUserID ='" + userId + "' and requestStatus = 2";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}

			Iterator<Integer> iterator = userIds.iterator();
			while (iterator.hasNext()) {
				userId = iterator.next();
				sql = "Select * from tbl_users where UserID = '" + userId + "'";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					jsonResultsArray.put(jsonObject);
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/search/{userId}/{search_criteria}")
	@Produces(MediaType.TEXT_PLAIN)
	public String search(@PathParam("userId") int userId1, @PathParam("search_criteria") String search_criteria) {
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "Select * from tbl_users  AS U LEFT OUTER JOIN FrissDB.tbl_usertransactions AS UT ON U.UserID = UT.UserID where ( FirstName like '%"
					+ search_criteria + "%' or LastName like '%" + search_criteria + "%' or UserName like '%"
					+ search_criteria + "%') and U.UserID <> '" + userId1 + "'";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("EmailName", rs.getString("EmailName"));
				if (rs.getString("AvatarPath") == null) {
					jsonObject.put("AvatarPath", "");
				} else {
					jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/searchResults/{userId}/{search_criteria}/{resultsPage}/{pageNo}")
	@Produces(MediaType.TEXT_PLAIN)
	public String searchResults(@PathParam("userId") int userId, @PathParam("search_criteria") String search_criteria,
			@PathParam("resultsPage") int resultsPage, @PathParam("pageNo") int pageNo) {
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetSearchBoxResults(?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, search_criteria);
			callableStatement.setInt(3, resultsPage);
			callableStatement.setInt(4, pageNo);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				if (rs.getString("AvatarPath") == null) {
					jsonObject.put("AvatarPath", "");
				} else {
					jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/searchFriends/{userName}/{search_criteria}")
	@Produces(MediaType.TEXT_PLAIN)
	public String searchFriends(@PathParam("userName") String userName,
			@PathParam("search_criteria") String search_criteria) {
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where UserID1 ='" + userId + "' and requestStatus = 1";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID2"));
			}

			sql = "Select * from tbl_userfriendlist  where UserID2 ='" + userId + "' and requestStatus = 1";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}

			Iterator<Integer> iterator = userIds.iterator();
			while (iterator.hasNext()) {
				userId = iterator.next();
				sql = "Select * from tbl_users AS U LEFT OUTER JOIN FrissDB.tbl_usertransactions AS UT ON U.UserID = UT.UserID where U.UserID = '"
						+ userId + "' and ( FirstName like '%" + search_criteria + "%' or LastName like '%"
						+ search_criteria + "%' or UserName like '%" + search_criteria + "%')";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					if (rs.getString("AvatarPath") == null) {
						jsonObject.put("AvatarPath", "");
					} else {
						jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
					}
					jsonResultsArray.put(jsonObject);
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/friendStatus/{userID1}/{userID2}")
	@Produces(MediaType.TEXT_PLAIN)
	public String friendStatus(@PathParam("userID1") int userID1, @PathParam("userID2") int userID2) {
		Connection conn = null;
		Statement stmt = null;
		String requestStatus = "-1";
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT RequestStatus from tbl_userfriendlist where userID1 ='" + userID1 + "'and userID2 ='"
					+ userID2 + "' and isDeleted =0 limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				requestStatus = rs.getInt("RequestStatus") + "";
			}

			if (requestStatus.equals("-1")) {
				sql = "SELECT RequestStatus from tbl_userfriendlist where userID1 ='" + userID2 + "'and userID2 ='"
						+ userID1 + "' and isDeleted =0 limit 1";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					requestStatus = rs.getInt("RequestStatus") + "";
				}
				if (requestStatus.equals("0")) {
					requestStatus = "4";
				}
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return requestStatus;
	}

	public void test() {
		// sendFriendRequest("dharmakolla96", "dharmakolla98");
		// approveFriendRequest("dharmakolla96","dharmakolla98");
		// rejectFriendRequest("dharmakolla123","dharmakolla23");
		// unFriendRequest("dharmakolla123","dharmakolla23");

		// search("a");
		// friendsList("dharmakolla96");
		// friendsList("dharmakolla98");

		// searchFriends("dharmakolla98", "a");
		// friendStatus(41,40);
		searchResults(40, "a", 8, 1);
	}

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;

		UserFriendList friendList = new UserFriendList();
		// friendList.test();
		System.out.println(friendList.friendsList("Chandu"));

	}
}
