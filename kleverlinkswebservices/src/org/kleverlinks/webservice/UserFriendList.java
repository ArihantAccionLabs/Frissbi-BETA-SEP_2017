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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.FriendBean;
import org.kleverlinks.enums.FriendStatusEnum;
import org.mongo.dao.MongoDBJDBC;
import org.service.dto.NotificationInfoDTO;
import org.util.Utility;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

@Path("FriendListService")
public class UserFriendList {

	@POST
	@Path("/sendFriendRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	public String sendFriendRequest(String userIds) {
		System.out.println("userIds   :  " + userIds.toString());
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			JSONObject userIdsJson = new JSONObject(userIds);
			FriendBean friendBean = new FriendBean();
			friendBean.toFriendBean(userIdsJson);
			String insertStoreProc = "{call usp_InsertUserFriendship(?,?,?,?,?,?,?)}";
			conn = DataSourceConnection.getDBConnection();
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, friendBean.getUserId());
			callableStatement.setLong(2, friendBean.getFreindId());
			callableStatement.setString(3, FriendStatusEnum.WAITING.toString());
			callableStatement.setLong(4, friendBean.getUserId());
			callableStatement.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.BIGINT);
			int value = callableStatement.executeUpdate();

			Long userFriendListId = callableStatement.getLong(7);
			System.out.println("userFriendListId  ::::::   " + userFriendListId + "  value  ::  " + value);
			if (value == 1 && userFriendListId != 0) {

				JSONObject senderJsonObject = ServiceUtility.getUserDetailByUserId(friendBean.getUserId());
				String message = senderJsonObject.getString("fullName")
						+ " wants to be friends with you.";/*
															 * +" on "+new
															 * SimpleDateFormat("dd-mm-yyyy hh:mm a"
															 * ).format(new
															 * Date());
															 */

				NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				notificationInfoDTO.setUserId(friendBean.getFreindId());
				notificationInfoDTO.setUserFriendListId(userFriendListId);
				notificationInfoDTO.setSenderUserId(friendBean.getUserId());
				notificationInfoDTO.setNotificationType(NotificationsEnum.FRIEND_PENDING_REQUESTS.toString());
				notificationInfoDTO.setMessage(message);

				NotificationService.sendFriendNotification(notificationInfoDTO);

				// NotificationService.sendNotificationToOneUser(senderUserId ,
				// receiverUserId ,
				// NotificationsEnum.FRIEND_PENDING_REQUESTS.ordinal() + 1 , 0);

				jsonObject.put("status", true);
				jsonObject.put("message", "FriendRequest Sent Succesfully");

				return jsonObject.toString();
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Something went wrong");

		return jsonObject.toString();
	}

	@POST
	@Path("/approveFriendRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String approveFriendRequest(String userIds) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject responseJson = new JSONObject();
		try {
			JSONObject userIdsJson = new JSONObject(userIds);
			FriendBean friendBean = new FriendBean();
			friendBean.toFriendBean(userIdsJson);
			conn = DataSourceConnection.getDBConnection();

			String insertStoreProc = "{call usp_UpdateUserFriendship(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, friendBean.getUserId());
			callableStatement.setLong(2, friendBean.getFreindId());
			callableStatement.setString(3, FriendStatusEnum.ACCEPTED.toString());
			callableStatement.setLong(4, friendBean.getFreindId());
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
			int value = callableStatement.executeUpdate();
			System.out.println("value  :::  " + value);
			if (value == 1) {
				JSONObject receiverJsonObject = ServiceUtility.getUserDetailByUserId(friendBean.getFreindId());
				// JSONObject senderJsonObject =
				// ServiceUtility.getUserDetailByUserId(senderUserId);

				String message = receiverJsonObject.getString("fullName")
						+ "  accepted your friend request";/*
															 * on
															 * "+new SimpleDateFormat("
															 * dd-mm-yyyy hh:mm
															 * a").format(new
															 * Date());
															 */
				NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				notificationInfoDTO.setUserId(friendBean.getUserId());
				notificationInfoDTO.setSenderUserId(friendBean.getFreindId());
				notificationInfoDTO.setNotificationType(NotificationsEnum.FRIEND_REQUEST_ACCEPTANCE.toString());
				notificationInfoDTO.setMessage(message);

				NotificationService.sendFriendNotification(notificationInfoDTO);
				responseJson.put("status", true);
				responseJson.put("message", "Friend request accepted successfully");

				return responseJson.toString();
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");

		return responseJson.toString();
	}

	@POST
	@Path("/rejectFriendRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	public String rejectFriendRequest(String userIds) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject finalJson = new JSONObject();
		System.out.println("userIds ::   "+userIds);
		try {
			JSONObject userIdsJson = new JSONObject(userIds);
			FriendBean friendBean = new FriendBean();
			friendBean.toFriendBean(userIdsJson);
			conn = DataSourceConnection.getDBConnection();

			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserFriendship(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, friendBean.getUserId());
			callableStatement.setLong(2, friendBean.getFreindId());
			callableStatement.setString(3, FriendStatusEnum.REJECTED.toString());
			callableStatement.setLong(4, friendBean.getFreindId());
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
			int value = callableStatement.executeUpdate();
            System.out.println("value  ::::  "+value);
			if (value == 1) {
				finalJson.put("status", true);
				finalJson.put("message", "Friend is rejected");
				return finalJson.toString();
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(stmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		System.out.println("Stored procedure executed");
		return finalJson.toString();
	}

	@POST
	@Path("/unFriendRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String unFriendRequest(String userIds) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject finalJson = new JSONObject();
		try {
			JSONObject userIdsJson = new JSONObject(userIds);
			FriendBean friendBean = new FriendBean();
			friendBean.toFriendBean(userIdsJson);
			conn = DataSourceConnection.getDBConnection();

			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserFriendship(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, friendBean.getUserId());
			callableStatement.setLong(2, friendBean.getFreindId());
			callableStatement.setString(3, FriendStatusEnum.ACCEPTED.toString());
			callableStatement.setLong(4, friendBean.getFreindId());
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
			int value = callableStatement.executeUpdate();
			System.out.println("value ::   "+value);
			if (value == 1) {
				finalJson.put("status", true);
				finalJson.put("message", "Successfully unfriend");
				return finalJson.toString();
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(stmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}

	@POST
	@Path("/blockFriend")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String blockFriendRequest(String userIds) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject finalJson = new JSONObject();
		try {
			JSONObject userIdsJson = new JSONObject(userIds);
			FriendBean friendBean = new FriendBean();
			friendBean.toFriendBean(userIdsJson);
			conn = DataSourceConnection.getDBConnection();

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserFriendship(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, friendBean.getUserId());
			callableStatement.setLong(2, friendBean.getFreindId());
			callableStatement.setString(3, FriendStatusEnum.BLOCKED.toString());
			callableStatement.setLong(4, friendBean.getFreindId());
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				finalJson.put("status", true);
				finalJson.put("message", "Successfully unfriend");
				return finalJson.toString();
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(stmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}

	@GET
	@Path("/friendsList/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String friendsList(@PathParam("userId") Long userId) {

		System.out.println("friendsList  userId   :    " + userId);
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		Connection conn = null;
		Statement pstmt = null;
		JSONObject jsonObject = null;
		try {
			conn = DataSourceConnection.getDBConnection();

			String friendUserIds = Utility.getFriendUserIdInString(userId).stream().collect(Collectors.joining(", "));
			System.out.println(friendUserIds.length() + "  " + (friendUserIds.length() != 0));
			if (friendUserIds.length() != 0) {
				String sql = "SELECT UserID,EmailName,FirstName,LastName,ProfileImageId,ContactNumber FROM tbl_users WHERE UserID IN ("
						+ friendUserIds + ")";
				pstmt = conn.createStatement();
				ResultSet rs = pstmt.executeQuery(sql);
				while (rs.next()) {

					jsonObject = new JSONObject();
					jsonObject.put("fullName", rs.getString("FirstName") + rs.getString("LastName"));
					jsonObject.put("userId", rs.getLong("UserID"));
					jsonObject.put("phoneNumber", rs.getString("ContactNumber"));
					jsonObject.put("profileImageId", rs.getString("ProfileImageID"));
					jsonObject.put("emailId", rs.getString("EmailName"));
					jsonResultsArray.put(jsonObject);
				}
				System.out.println("jsonResultsArray  >>>>    " + jsonResultsArray.toString());
				finalJson.put("status", true);
				finalJson.put("message", "Getting friendlist successfully");
				finalJson.put("friends_array", jsonResultsArray);
			} else {
				finalJson.put("status", true);
				finalJson.put("message", "No friends found");
			}
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
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
		Long userId = 0l;
		ArrayList<Long> userIds = new ArrayList<Long>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getLong("userId");
			}
			sql = "Select * from tbl_userfriendlist  where ReceiverUserID ='" + userId + "' and requestStatus = 0";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getLong("SenderUserID"));
			}

			Iterator<Long> iterator = userIds.iterator();
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
					jsonResultsArray.put(jsonObject);
				}
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
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
		Long userId = 0l;
		ArrayList<Long> userIds = new ArrayList<Long>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getLong("userId");
			}
			sql = "Select * from tbl_userfriendlist  where ActionUserID ='" + userId + "' and requestStatus = 2";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getLong("SenderUserID"));
			}

			Iterator<Long> iterator = userIds.iterator();
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
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(stmt);
		}
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/search/{senderUserId}/{search_criteria}")
	@Produces(MediaType.TEXT_PLAIN)
	public String search(@PathParam("senderUserId") Long senderUserId,
			@PathParam("search_criteria") String search_criteria) {
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		Set<Long> waitingUserIds = new HashSet<Long>();
		Set<Long> acceptedUserIds = new HashSet<Long>();
		Set<Long> allUserIds = new HashSet<Long>();
		try {
			Map<String, Set<Long>> map = getFriendList(senderUserId);
			System.out.println("" + map.toString());
			waitingUserIds = map.get("waiting");
			acceptedUserIds = map.get("accepted");
			allUserIds.addAll(acceptedUserIds);
			allUserIds.addAll(waitingUserIds);
			allUserIds.add(senderUserId);

			String userArray = "";
			int rowCount = 1;
			for (Long integer : allUserIds) {

				if (allUserIds.size() == rowCount) {
					userArray += integer + "";
				} else {
					userArray += integer + ",";
				}
				rowCount++;
			}
			// System.out.println("userArray============"+userArray.toString());
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql = "SELECT U.UserId,U.ContactNumber,U.UserName,U.FirstName,U.LastName,U.EmailName,U.Gender,U.dob,U.ProfileImageID FROM tbl_users AS U  where (U.FirstName LIKE '%"
					+ search_criteria + "%' OR U.LastName LIKE '%" + search_criteria + "%' OR U.UserName LIKE '%"
					+ search_criteria + "%') AND U.UserID NOT IN (" + userArray + ")";

			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("userId", rs.getString("UserId"));
				jsonObject.put("fullName", rs.getString("FirstName") + " " + rs.getString("LastName"));
				jsonObject.put("emailId", rs.getString("EmailName"));
				jsonObject.put("gender", rs.getString("Gender"));
				jsonObject.put("dob", rs.getString("dob"));
				if (Utility.checkValidString(rs.getString("ProfileImageID"))) {
					jsonObject.put("profileImageId", rs.getString("ProfileImageID"));
				}
				if (Utility.checkValidString(rs.getString("ContactNumber"))) {
					jsonObject.put("phoneNumber", rs.getString("ContactNumber"));
				}
				jsonObject.put("status", FriendStatusEnum.UNFRIEND);

				jsonResultsArray.put(jsonObject);
			}
			waitingUserIds.remove(senderUserId);

			finalJson.put("status", true);
			finalJson.put("message", "Friend list fetched successfully");
			finalJson.put("unfriendList", getWaitingFriends(filterWaitingOrder(waitingUserIds, search_criteria),
					senderUserId, jsonResultsArray));

			return finalJson.toString();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(stmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Something went wrong");

		return finalJson.toString();
	}

	private Map<String, Set<Long>> getFriendList(Long userId) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		Map<String, Set<Long>> map = new HashMap<String, Set<Long>>();

		Set<Long> waitingUserIds = new HashSet<Long>();
		Set<Long> acceptedUserIds = new HashSet<Long>();
		List<String> statusList = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql;
			sql = "SELECT SenderUserID,ReceiverUserID,RequestStatus FROM tbl_userfriendlist  WHERE (SenderUserID =? OR ReceiverUserID =?) and RequestStatus!=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, userId);
			pstmt.setLong(2, userId);
			pstmt.setString(3, FriendStatusEnum.UNFRIEND.toString());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {

				statusList = new ArrayList<>();
				statusList.add(FriendStatusEnum.BLOCKED.toString());
				statusList.add(FriendStatusEnum.REJECTED.toString());
				statusList.add(FriendStatusEnum.ACCEPTED.toString());

				if (Utility.checkValidString(rs.getString("RequestStatus"))) {

					if (rs.getString("RequestStatus").trim().equals(FriendStatusEnum.WAITING.toString())) {
						if (userId != rs.getLong("ReceiverUserID"))
							waitingUserIds.add(rs.getLong("ReceiverUserID"));
						if (userId != rs.getLong("SenderUserID"))
							waitingUserIds.add(rs.getLong("SenderUserID"));
					} else if (statusList.contains(rs.getString("RequestStatus").trim())) {
						if (userId != rs.getLong("ReceiverUserID"))
							acceptedUserIds.add(rs.getLong("ReceiverUserID"));
						if (userId != rs.getLong("SenderUserID"))
							acceptedUserIds.add(rs.getLong("SenderUserID"));
					}
				}
			}
			map.put("waiting", waitingUserIds);
			map.put("accepted", acceptedUserIds);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		return map;
	}

	@POST
	@Path("/seeOtherProfile")
	public String seeOtherProfile(String userIds) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		JSONObject finalJson = new JSONObject();
		try {
			JSONObject userIdsJson = new JSONObject(userIds);
			FriendBean friendBean = new FriendBean();
			friendBean.toFriendBean(userIdsJson);
			System.out.println("" + friendBean.toString());
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT U.UserId,U.UserName,U.FirstName,U.LastName,U.EmailName,U.Gender,U.dob,U.ProfileImageID,FR.SenderUserID,FR.ReceiverUserID,FR.RequestStatus FROM tbl_users AS U  INNER JOIN tbl_userfriendlist AS FR "
					+ " ON U.UserID=FR.ReceiverUserID " + "  WHERE FR.SenderUserID =? AND FR.ReceiverUserID =? "
					+ " UNION "
					+ " SELECT U.UserId,U.UserName,U.FirstName,U.LastName,U.EmailName,U.Gender,U.dob,U.ProfileImageID,FR.SenderUserID,FR.ReceiverUserID,FR.RequestStatus FROM tbl_users AS U INNER JOIN tbl_userfriendlist AS FR "
					+ " ON U.UserID=FR.SenderUserID " + " WHERE FR.ReceiverUserID =? AND FR.SenderUserID =? ";

			pstmt = conn.prepareStatement(sql);

			pstmt.setLong(1, friendBean.getUserId());
			pstmt.setLong(2, friendBean.getFreindId());
			pstmt.setLong(3, friendBean.getUserId());
			pstmt.setLong(4, friendBean.getFreindId());

			ResultSet rs = pstmt.executeQuery();
			JSONObject jsonObject = new JSONObject();
			MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
			while (rs.next()) {
				jsonObject.put("userId", rs.getLong("UserId"));
				jsonObject.put("fullName", rs.getString("FirstName") + " " + rs.getString("LastName"));
				jsonObject.put("emailId", rs.getString("EmailName"));
				jsonObject.put("gender", rs.getString("Gender"));
				jsonObject.put("dob", rs.getString("dob"));
				if (Utility.checkValidString(rs.getString("ProfileImageID"))) {
					jsonObject.put("profileImage", mongoDBJDBC.getUriImage(rs.getString("ProfileImageID").trim()));
				}
				jsonObject.put("userName", rs.getString("UserName"));

				if (Utility.checkValidString(rs.getString("RequestStatus"))) {

					if (rs.getString("RequestStatus").trim().equals(FriendStatusEnum.WAITING.toString())) {
						if (friendBean.getUserId() == rs.getLong("SenderUserID")
								&& friendBean.getFreindId() == rs.getLong("ReceiverUserID")) {
							jsonObject.put("status", FriendStatusEnum.WAITING);
						} else if (friendBean.getUserId() == rs.getLong("ReceiverUserID")
								&& friendBean.getFreindId() == rs.getLong("SenderUserID")) {
							jsonObject.put("status", FriendStatusEnum.CONFIRM);
						}
					} else if (rs.getString("RequestStatus").trim().equals(FriendStatusEnum.ACCEPTED.toString())) {
						jsonObject.put("status", FriendStatusEnum.FRIENDS);
					}
				} else {
					jsonObject.put("status", FriendStatusEnum.UNFRIEND);
				}

				finalJson.put("viewProfile", jsonObject);
			}
			System.out.println((!jsonObject.has("userId")) + "    jsonObject===== :   " + jsonObject);
			if (!jsonObject.has("userId")) {
				finalJson = seeProfile(friendBean.getFreindId());
			}

			finalJson.put("status", true);
			finalJson.put("message", "Profile fetched successfully ");

			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Something went wrong");
		return finalJson.toString();
	}

	@GET
	@Path("/viewProfile/{userId}")
	public String viewProfile(@PathParam("userId") Long userId) {
		System.out.println("userId  :  " + userId);
		JSONObject finalJson = new JSONObject();
		try {
			finalJson = seeProfile(userId);
			if (finalJson.length() == 0) {
				finalJson.put("status", false);
				finalJson.put("message", "No profile is there  with requested user id");
			} else {
				finalJson.put("message", "profile fetched successfully");
				finalJson.put("status", true);
			}

			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Something went wrong");
		return finalJson.toString();
	}

	public JSONObject seeProfile(Long friendUserId) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		JSONObject finalJson = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT U.UserId,U.UserName,U.FirstName,U.LastName,U.EmailName,U.Gender,U.dob,U.ContactNumber,U.ProfileImageID,U.CoverImageID FROM tbl_users  U WHERE U.UserID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, friendUserId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("userId", rs.getLong("UserId"));
				jsonObject.put("firstName", rs.getString("FirstName"));
				jsonObject.put("lastName", rs.getString("LastName"));
				jsonObject.put("userName", rs.getString("UserName"));
				jsonObject.put("email", rs.getString("EmailName"));
				jsonObject.put("gender", rs.getString("Gender"));
				jsonObject.put("contactNumber", rs.getString("ContactNumber"));
				jsonObject.put("dob", rs.getString("dob"));
				jsonObject.put("status", FriendStatusEnum.UNFRIEND);

				if (Utility.checkValidString(rs.getString("ProfileImageID"))) {

					jsonObject.put("profileImageId", rs.getString("ProfileImageID"));
				}
				if (Utility.checkValidString(rs.getString("CoverImageID"))) {

					jsonObject.put("coverImageId", rs.getString("CoverImageID"));
				}
				/*
				 * if
				 * (Utility.checkValidString(rs.getString("U.ProfileImageID")))
				 * { jsonObject.put("profileImage",
				 * mongoDBJDBC.getUriImage(rs.getString("ProfileImageID").trim()
				 * )); }
				 */

				finalJson.put("viewProfile", jsonObject);
			}
			return finalJson;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		return finalJson;
	}

	public JSONArray getWaitingFriends(Set<Long> filterWaitingList, Long senderUserId, JSONArray jsonResultsArray) {
		System.out.println("getRequestStatus V=====" + jsonResultsArray.toString());
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT U.UserId,U.ContactNumber,U.UserName,U.FirstName,U.LastName,U.EmailName,U.Gender,U.dob,U.ProfileImageID,FR.SenderUserID FROM tbl_users AS U INNER JOIN tbl_userfriendlist AS FR "
					+ " ON (U.UserID=FR.SenderUserID)  "
					+ "  WHERE (FR.SenderUserID =? AND FR.ReceiverUserID =?) OR (FR.ReceiverUserID =? AND FR.SenderUserID =?) "
					+ " UNION "
					+ " SELECT U.UserId,U.ContactNumber,U.UserName,U.FirstName,U.LastName,U.EmailName,U.Gender,U.dob,U.ProfileImageID,FR.SenderUserID FROM tbl_users AS U INNER JOIN tbl_userfriendlist AS FR "
					+ " ON (U.UserID=FR.ReceiverUserID) "
					+ " WHERE (FR.SenderUserID =? AND FR.ReceiverUserID =?) OR (FR.ReceiverUserID =? AND FR.SenderUserID =?)";

			pstmt = conn.prepareStatement(sql);
			for (Long waitingUserId : filterWaitingList) {

				pstmt.setLong(1, senderUserId);
				pstmt.setLong(2, waitingUserId);
				pstmt.setLong(3, senderUserId);
				pstmt.setLong(4, waitingUserId);
				pstmt.setLong(5, senderUserId);
				pstmt.setLong(6, waitingUserId);
				pstmt.setLong(7, senderUserId);
				pstmt.setLong(8, waitingUserId);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					Long userId = rs.getLong("UserId");
					if (!(userId.longValue() == senderUserId.longValue())) {
						jsonObject.put("userId", rs.getLong("UserId"));
						jsonObject.put("fullName", rs.getString("FirstName") + " " + rs.getString("LastName"));
						jsonObject.put("emailId", rs.getString("EmailName"));
						jsonObject.put("gender", rs.getString("Gender"));
						jsonObject.put("dob", rs.getString("dob"));
						if (Utility.checkValidString(rs.getString("ProfileImageID"))) {
							jsonObject.put("profileImageId", rs.getString("ProfileImageID"));
						}
						if (Utility.checkValidString(rs.getString("ContactNumber"))) {
							jsonObject.put("phoneNumber", rs.getString("ContactNumber"));
						}
						Long senderId = rs.getLong("SenderUserID");
						jsonObject.put("status", (senderId.longValue() == senderUserId.longValue())
								? FriendStatusEnum.WAITING : FriendStatusEnum.CONFIRM);

						jsonResultsArray.put(jsonObject);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		System.out.println("getRequestStatus===A==" + jsonResultsArray.toString());
		return jsonResultsArray;
	}

	public Set<Long> filterWaitingOrder(Set<Long> waitingList, String search_criteria) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		Set<Long> filterWaitingList = new HashSet<>();
		try {
			conn = DataSourceConnection.getDBConnection();

			String userArray = "";
			int rowCount = 1;
			for (Long integer : waitingList) {

				if (waitingList.size() == rowCount) {
					userArray += integer + "";
				} else {
					userArray += integer + ",";
				}
				rowCount++;
			}
			if (userArray.length() != 0) {

				String sql = "SELECT U.UserId FROM tbl_users AS U WHERE U.UserID  IN (" + userArray
						+ ") AND (U.FirstName like '%" + search_criteria + "%' or U.LastName like '%" + search_criteria
						+ "%' or U.UserName like '%" + search_criteria + "%')";

				pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					filterWaitingList.add(rs.getLong("UserId"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		System.out.println("filterWaitingList  :  " + filterWaitingList.toString());
		return filterWaitingList;
	}

	@POST
	@Path("/adviseFissbiAppInstall")
	@Consumes(MediaType.APPLICATION_JSON)
	public String adviseFrissbiAppToInstall(String userDetails) {

		JSONObject finalJson = new JSONObject();
		try {
			JSONObject userJson = new JSONObject(userDetails);

			JSONObject userObject = ServiceUtility.getUserDetailByUserId(userJson.getLong("userId"));
			if (userObject != null) {

				if (userJson.has("emailId")) {
					String message = "<p>Hi " + userObject.getString("fullName") + " has invited to you on Frissbi ";
					message += " Please click  " + Constants.SERVER_URL + " url to install  FRISSBI App</p>";
					EmailService.SendMail(userJson.getString("emailId"), "Frissbi App installation", message);
					finalJson.put("message", "Mail sent succesfully for inviting the user");
				} else if (userJson.has("phoneNumber")) {
					SmsService smsService = new SmsService();

					String message = "HI " + userObject.getString("fullName") + " has invited to you on Frissbi ";
					message += " Please click  " + Constants.SERVER_URL + " url to install  FRISSBI App ";

					smsService.sendSms(userJson.getString("phoneNumber"), message);
					finalJson.put("message", "Sms sent successfully for inviting the user");
				}
			}

			finalJson.put("status", true);
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops Something went wrong");
		return finalJson.toString();
	}

	@GET
	@Path("/getNotificationLogByUserId/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getNotificationLogByUserId(@PathParam("userId") Long userId) {

		JSONObject finalJson = new JSONObject();
		CallableStatement callableStatement = null;
		Connection conn = null;
		JSONArray notificationArray = new JSONArray();
		try {

			conn = DataSourceConnection.getDBConnection();
			String notificationLogStoreProc = "{call usp_getNotificationLogByUserId(?,?,?)}";

			callableStatement = conn.prepareCall(notificationLogStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setString(2, NotificationsEnum.ADDED_TO_GROUP.toString());
			callableStatement.setString(3, NotificationsEnum.FRIEND_PENDING_REQUESTS.toString());

			ResultSet rs = callableStatement.executeQuery();
			JSONObject jsonObject = null;

			while (rs.next()) {

				if (Utility.checkValidString(rs.getString("NotificationType"))) {

					if (rs.getString("NotificationType").equals(NotificationsEnum.FRIEND_PENDING_REQUESTS.toString())) {
						Long userFriendListId = rs.getLong("UserFriendListID");
						if (userFriendListId != null) {

							jsonObject = ServiceUtility.getUserDetailByUserId(rs.getLong("SenderUserID"));
							jsonObject.remove("emailId");
							jsonObject.remove("userId");
							jsonObject.put("status", Utility.getFriendStatusByFriendListId(userFriendListId));
							jsonObject.put("senderUserId", rs.getLong("SenderUserID"));
							jsonObject.put("type", "FRIEND_TYPE");
						}
					} else {
						jsonObject = Utility.getGroupInfoById(rs.getLong("GroupId"));
						jsonObject.put("type", "GROUP_TYPE");
					}
				}
				if (rs.getString("IsRead").equals("true")) {
					jsonObject.put("isRead", true);
				} else {
					jsonObject.put("isRead", false);
				}
				jsonObject.put("notificationId", rs.getLong("NotificationID"));

				notificationArray.put(jsonObject);
			}

			finalJson.put("status", true);
			finalJson.put("message", "Notification log fetched successfully");
			finalJson.put("notificationArray", notificationArray);

			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops Something went wrong");
		return finalJson.toString();
	}

	@GET
	@Path("/updateNotificationAsRead/{userId}/{isRead}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getNotificationLogByUserId(@PathParam("userId") Long userId, @PathParam("isRead") String isRead) {

		JSONObject finalJson = new JSONObject();
		CallableStatement callableStatement = null;
		Connection conn = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			String notificationLogStoreProc = "{call usp_updateNotificationAsRead(?,?)}";

			callableStatement = conn.prepareCall(notificationLogStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setString(2, isRead);

			int isUpdated = callableStatement.executeUpdate();

			if (isUpdated != 0) {

				finalJson.put("status", true);
				finalJson.put("message", "Notification updated successfully");
			}
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops Something went wrong");
		return finalJson.toString();
	}

}
