package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.enums.FriendStatusEnum;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.UserDTO;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

@Path("FriendListService")
public class UserFriendList {

	@GET
	@Path("/sendFriendRequest/{senderUserId}/{receiverUserId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String sendFriendRequest(@PathParam("senderUserId") Integer senderUserId,
			@PathParam("receiverUserId") Integer receiverUserId) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			conn = DataSourceConnection.getDBConnection();
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, senderUserId);
			callableStatement.setInt(2, receiverUserId);
			callableStatement.setInt(3, 0);
			callableStatement.setInt(4, senderUserId);
			callableStatement.setTimestamp(5, sqlDateNow);
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			if (value == 1) {
			NotificationService.sendNotificationToOneUser(senderUserId , receiverUserId , NotificationsEnum.FRIEND_PENDING_REQUESTS.ordinal() + 1 , 0);	
			
			jsonObject.put("status", true);
			jsonObject.put("message", "FriendRequest Sent Succesfully");
			
			return jsonObject.toString();
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Something went wrong");
		
		return jsonObject.toString();
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
			conn = DataSourceConnection.getDBConnection();
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		return value;
	}

	@GET
	@Path("/approveFriendRequest/{receiverUserId}/{senderUserId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String approveFriendRequest(@PathParam("receiverUserId") Integer receiverUserId,
			@PathParam("senderUserId") Integer senderUserId) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject responseJson = new JSONObject();
		
		try {
			conn = DataSourceConnection.getDBConnection();
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is approving
			// friend request of user2
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, receiverUserId);
			callableStatement.setInt(2, senderUserId);
			callableStatement.setString(3, FriendStatusEnum.ACCEPTED.toString());
			callableStatement.setInt(4, receiverUserId);
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, sqlDateNow);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				UserNotifications userNotifications = new UserNotifications();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				String notificationId = userNotifications.insertUserNotifications(receiverUserId, senderUserId,
						NotificationsEnum.FRIEND_REQUEST_ACCEPTANCE.ordinal() + 1, 0, timestamp);
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
						JSONObject jsonObject = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(receiverUserId));
						String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);

						System.out.println(result);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// sending mails to both sender and acceptor
/*			
				MyEmailer.SendMail(userDTO1.getEmailId(), "Your friend request accepted by " + userDTO1.getFullName(),
						"Your friend request accepted by " + userDTO1.getFullName());
				MyEmailer.SendMail(userDTO2.getEmailId(), "Your friend request accepted by " + userDTO2.getFullName(),
						"You are friend now to " + userDTO2.getFullName());*/

				responseJson.put("status", true);
				responseJson.put("message", "Friend request accepted successfully");
				
				return responseJson.toString();
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		}
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");
		
		return responseJson.toString();
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
			conn = DataSourceConnection.getDBConnection();
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
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
			conn = DataSourceConnection.getDBConnection();
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		return "0";
	}

	@GET
	@Path("/friendsList/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String friendsList(@PathParam("userId") Integer userId) {
	
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson =new JSONObject();
	   try { 
			for (Integer friendsId : getFriendList(userId)) {
				JSONObject jsonObject = ServiceUtility.getUserDetailByUserId(friendsId);
				if(jsonObject != null){
					jsonResultsArray.put(jsonObject);
				}
			}

			finalJson.put("status", true);
			finalJson.put("message", "Getting friendlist successfully");
			finalJson.put("friends_array", jsonResultsArray);
			return finalJson.toString();
	   }  catch (Exception e) {
			e.printStackTrace();
		} 
		
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
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
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where ReceiverUserID ='" + userId + "' and requestStatus = 0";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("SenderUserID"));
			}

			Iterator<Integer> iterator = userIds.iterator();
			while (iterator.hasNext()) {
				userId = iterator.next();
				sql = "Select * from tbl_users AS U LEFT OUTER JOIN tbl_usertransactions AS UT ON U.UserID = UT.UserID where U.UserID = '"
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
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
			conn = DataSourceConnection.getDBConnection();
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
				userIds.add(rs.getInt("SenderUserID"));
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/search/{senderUserId}/{search_criteria}")
	@Produces(MediaType.TEXT_PLAIN)
	public String search(@PathParam("senderUserId") int senderUserId, @PathParam("search_criteria") String search_criteria) {
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		try {
		Set<Integer> userIds = getFriendList(senderUserId);
			userIds.add(senderUserId);
			String userArray = "";
			int rowCount = 1;
			for (Integer integer : userIds) {
				
				if(userIds.size() == rowCount){
					userArray += integer+"";
				}else{
					userArray += integer+",";	
				}
				rowCount++;
			}
			System.out.println("userArray============"+userArray.toString());
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			//LEFT OUTER JOIN tbl_usertransactions AS UT ON U.UserID = UT.UserID;
			String sql = "Select U.UserId,U.UserName,U.FirstName,U.LastName,U.EmailName,FL.RequestStatus from tbl_users  AS U  LEFT OUTER JOIN tbl_userfriendlist AS FL ON U.UserID = FL.ActionUserID where ( FirstName like '%"
					+ search_criteria + "%' or LastName like '%" + search_criteria + "%' or UserName like '%"
					+ search_criteria + "%') and U.UserID NOT IN (" + userArray +")";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("userId", rs.getString("UserId"));
				jsonObject.put("fullName", rs.getString("FirstName")+" " + rs.getString("LastName"));
				jsonObject.put("emailId", rs.getString("EmailName"));
				
				if(rs.getString("RequestStatus") != null && rs.getString("RequestStatus").trim().isEmpty()){
					jsonObject.put("status", rs.getString("RequestStatus"));
				} else {
					jsonObject.put("status", FriendStatusEnum.UNFRIEND);
				}
				jsonResultsArray.put(jsonObject);
			}
			finalJson.put("status", true);
			finalJson.put("message", "Friend list fetched successfully");
			finalJson.put("unfriendList", jsonResultsArray);
			
			return finalJson.toString();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Something went wrong");
		
		return finalJson.toString();
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
			conn = DataSourceConnection.getDBConnection();
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
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
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where SenderUserID ='" + userId + "' and requestStatus = 1";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("ReceiverUserID"));
			}

			sql = "Select * from tbl_userfriendlist  where ReceiverUserID ='" + userId + "' and requestStatus = 1";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("SenderUserID"));
			}

			Iterator<Integer> iterator = userIds.iterator();
			while (iterator.hasNext()) {
				userId = iterator.next();
				sql = "Select * from tbl_users AS U LEFT OUTER JOIN tbl_usertransactions AS UT ON U.UserID = UT.UserID where U.UserID = '"
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/friendStatus/{senderUserID}/{receiverUserID}")
	@Produces(MediaType.TEXT_PLAIN)
	public String friendStatus(@PathParam("senderUserID") int senderUserID, @PathParam("receiverUserID") int receiverUserID) {
		Connection conn = null;
		Statement stmt = null;
		String requestStatus = "-1";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT RequestStatus from tbl_userfriendlist where SenderUserID ='" + senderUserID + "'and ReceiverUserID ='"
					+ receiverUserID + "' limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				requestStatus = rs.getInt("RequestStatus") + "";
			}

			if (requestStatus.equals("-1")) {
				sql = "SELECT RequestStatus from tbl_userfriendlist where SenderUserID ='" + receiverUserID + "'and ReceiverUserID ='"
						+ senderUserID + "' and limit 1";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					requestStatus = rs.getInt("RequestStatus") + "";
				}
				if (requestStatus.equals("0")) {
					requestStatus = "4";
				}
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		return requestStatus;
	}

	private Set<Integer> getFriendList(Integer userId){
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		Set<Integer> userIds = new HashSet<Integer>();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql;
			sql = "SELECT SenderUserID,ReceiverUserID FROM tbl_userfriendlist  WHERE (SenderUserID =? OR ReceiverUserID =?) and RequestStatus=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, userId);
			pstmt.setInt(2, userId);
			pstmt.setString(3, FriendStatusEnum.ACCEPTED.toString());
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				if(userId != rs.getInt("ReceiverUserID"))
					userIds.add(rs.getInt("ReceiverUserID"));
				if(userId != rs.getInt("SenderUserID"))
					userIds.add(rs.getInt("SenderUserID"));
			}

		} catch (Exception e) {
		    e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		return userIds;
	}

}
