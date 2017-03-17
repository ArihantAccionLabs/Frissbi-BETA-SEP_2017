package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.ActivityBean;
import org.kleverlinks.bean.AppUserFriendBean;
import org.kleverlinks.enums.ActivityType;
import org.kleverlinks.enums.FriendStatusEnum;
import org.mongo.dao.MongoDBJDBC;
import org.util.Utility;
import org.util.service.ServiceUtility;

import com.google.api.client.util.Lists;
import com.mongodb.DBObject;


@Path("FriendActivityService")
public class FriendActivityService {

	@GET
	@Path("/getFriendActivity/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getFriendActivity(@PathParam("userId") Long userId){
		JSONObject finalJson = new JSONObject();
		List<ActivityBean> userActivityBeanList = new ArrayList<ActivityBean>();
		try {
			String friendUserIds = Utility.getFriendList(userId).stream().collect(Collectors.joining(", "));
			userActivityBeanList.addAll(getUserDetails(getUsersProfiles(friendUserIds)));			
			userActivityBeanList.addAll(getFriendsMeetingActivity(friendUserIds , userId));
			userActivityBeanList.addAll(getFriendsPostedActivity(friendUserIds));
			
			Utility.sortList(userActivityBeanList);
			
			System.out.println("userActivityBeanList   size  :   "+userActivityBeanList.size());
			
			if(userActivityBeanList.size() > 10){
				int isInserted = insertUserActivityToTempTable(userActivityBeanList , friendUserIds , userId);
				if(isInserted == 0){
					finalJson.put("status", false);
					finalJson.put("message", "Record is not inserted in temp table");
					
					return finalJson.toString();
				}
			}
			finalJson.put("isNextActivityExist", userActivityBeanList.size() > 10 ? true:false);
			finalJson.put("status", true);
			finalJson.put("message", "User activity fetched successfully");
			finalJson.put("userActivityArray", setUserActivity(userActivityBeanList));
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
		
	}
	
	public List<ActivityBean> getFriendsMeetingActivity(String friendUserIds , Long userId) {

		Connection conn = null;
		Statement statement = null;
		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		try {
			System.out.println("userIds  ::  "+friendUserIds.toString());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
			String fullName = "";
			String others = "";
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT  MeetingID,SenderUserID,RequestDateTime,SenderFromDateTime,SenderToDateTime,MeetingDescription	"
					+ "	FROM tbl_MeetingDetails    WHERE SenderUserID IN ("+friendUserIds+") AND MeetingStatus='ACTIVE' "
					+ "UNION "
					+ "SELECT R.MeetingID  ,M.SenderUserID,M.RequestDateTime,M.SenderFromDateTime,M.SenderToDateTime,M.MeetingDescription	"
					+ "FROM tbl_RecipientsDetails AS R " + "INNER JOIN tbl_MeetingDetails AS M "
					+ "ON R.MeetingID = M.MeetingID " + "WHERE M.MeetingStatus='ACTIVE' AND (R.UserID IN  ("+friendUserIds+") AND R.Status =1)";
			
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				JSONArray usersArray = ServiceUtility.getReceptionistDetailsByMeetingId(rs.getLong("MeetingID"));
				
				if(usersArray.length() > 0){
					
				userActivityBean = new ActivityBean();
				java.util.Date senderFromDateTime = df.parse(rs.getString("SenderFromDateTime"));
				java.util.Date senderToDateTime = df.parse(rs.getString("SenderToDateTime"));
				userActivityBean.setDate(df.parse(rs.getString("RequestDateTime")));
			
				userActivityBean.setMeetingId(rs.getLong("MeetingID"));
				Set<Long> usersSet = new HashSet<Long>();
				for (int i = 0; i < usersArray.length(); i++) {
					usersSet.add(usersArray.getJSONObject(i).getLong("userId"));
				}
				if(usersSet.size() > 1){
					 others = " and "+ (usersSet.size()-1)+" others ";
				}
				
				Long friendId = rs.getLong("SenderUserID") == userId ? Lists.newArrayList(usersSet).get(0) : rs.getLong("SenderUserID");
				userActivityBean.setUserId(friendId);
				
				JSONObject friendJson =	ServiceUtility.getUserDetailByUserId(friendId);
				
				if(friendJson != null){
					fullName = friendJson.getString("fullName");
					userActivityBean.setMeetingUserImageId(friendJson.getString("profileImageId"));
				}
				userActivityBean.setMeetingMessage("You have a meeting " + rs.getString("MeetingDescription") +" with "+fullName +others+" from "+new SimpleDateFormat("HH:mm").format(senderFromDateTime)+" to "+new SimpleDateFormat("HH:mm").format(senderToDateTime)+" on "+dateFormat.format(senderFromDateTime));

				userActivityBeanList.add(userActivityBean);
			}
			}
			System.out.println("Size == "+userActivityBeanList.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return userActivityBeanList;
	}
	
	public List<ActivityBean> getUserDetails(List<AppUserFriendBean> appUserFriendBeanList) {

		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();

		for (AppUserFriendBean appUserFriendBean : appUserFriendBeanList) {

			if (Utility.checkValidString(appUserFriendBean.getProfileImageId())) {
				DBObject profileDbObj = mongoDBJDBC.getFile(appUserFriendBean.getProfileImageId());
				if (profileDbObj != null) {

					userActivityBean = new ActivityBean();
					userActivityBean.setUserId(appUserFriendBean.getUserId());
					JSONObject mongoJson = new JSONObject(profileDbObj.toString());
					userActivityBean.setProfileImage(appUserFriendBean.getProfileImageId());
					try {
						userActivityBean.setDate(java.util.Date
								.from(Instant.parse(mongoJson.getJSONObject("createdDate").getString("$date"))));
					} catch (Exception e) {
						e.printStackTrace();
					}
					userActivityBeanList.add(userActivityBean);
				}
			}
			if (Utility.checkValidString(appUserFriendBean.getCoverImageId())) {
				DBObject coverDbObj = mongoDBJDBC.getFile(appUserFriendBean.getCoverImageId());
				if (coverDbObj != null) {
					JSONObject mongoJson = new JSONObject(coverDbObj.toString());
					userActivityBean = new ActivityBean();
					userActivityBean.setUserId(appUserFriendBean.getUserId());
					userActivityBean.setCoverImage(appUserFriendBean.getCoverImageId());
					try {
						userActivityBean.setDate(java.util.Date
								.from(Instant.parse(mongoJson.getJSONObject("createdDate").getString("$date"))));
					} catch (Exception e) {
						e.printStackTrace();
					}

					userActivityBeanList.add(userActivityBean);
				}
			}
			if (Utility.checkValidString(appUserFriendBean.getRegistrationDate())) {
				userActivityBean = new ActivityBean();
				userActivityBean.setUserId(appUserFriendBean.getUserId());
				try {
					userActivityBean.setDate(dateTimeFormat.parse(appUserFriendBean.getRegistrationDate()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				userActivityBean.setRegistrationDate(appUserFriendBean.getRegistrationDate());

				userActivityBeanList.add(userActivityBean);
			}
		}
		return userActivityBeanList;
	}
	
	public List<ActivityBean> getFriendsPostedActivity(String friendUserIds) {
		Connection conn = null;
		Statement statement = null;
		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		try {
			conn = DataSourceConnection.getDBConnection();
	    
			String sql = "SELECT U.ProfileImageID,UA.UserActivityID,UA.UserID,UA.StatusDescription,UA.ImageDescription,UA.ImageID,UA.FromDateTime,UA.ToDateTime,UA.IsPrivate,Address,UA.CreatedDateTime"
					+ " FROM tbl_UserActivity AS UA INNER JOIN tbl_users AS U ON U.UserID=UA.UserID WHERE UA.UserID IN ("+friendUserIds+")";
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			while (rs.next()) {
				userActivityBean = new ActivityBean();
				userActivityBean.setUserId(rs.getLong("UserID"));
				userActivityBean.setActivityId(rs.getLong("UserActivityID"));
				userActivityBean.setStatus(rs.getString("StatusDescription"));
				userActivityBean.setImageDescription(rs.getString("ImageDescription"));
				userActivityBean.setImage(rs.getString("ImageID"));
				userActivityBean.setFromDate(rs.getString("FromDateTime"));
				userActivityBean.setToDate(rs.getString("ToDateTime"));
				userActivityBean.setAddress(rs.getString("Address"));
				userActivityBean.setIsPrivate(rs.getInt("IsPrivate"));
				userActivityBean.setDate(df.parse(rs.getString("CreatedDateTime")));
				userActivityBean.setUserProfileImageId(rs.getString("ProfileImageID"));
				
				userActivityBeanList.add(userActivityBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return userActivityBeanList;
	}
	public  List<AppUserFriendBean> getUsersProfiles(String friendUserIds) {
		Connection conn = null;
		Statement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		AppUserFriendBean appUserFriendBean = null;
		List<AppUserFriendBean> appUserFriendBeanList = new ArrayList<>();
		System.out.println(""+friendUserIds.toString());
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT UserID,ProfileImageId,CoverImageID,RegistrationDateTime FROM tbl_users WHERE UserID IN ("+friendUserIds+")";
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				appUserFriendBean = new AppUserFriendBean();
					
					if(Utility.checkValidString(rs.getString("ProfileImageId"))){
						appUserFriendBean.setProfileImageId(rs.getString("ProfileImageId"));
					}
					if(Utility.checkValidString(rs.getString("CoverImageID"))){
						appUserFriendBean.setCoverImageId(rs.getString("CoverImageID"));
					}
					appUserFriendBean.setRegistrationDate(rs.getString("RegistrationDateTime"));
					appUserFriendBean.setUserId(rs.getLong("UserID"));
					
					appUserFriendBeanList.add(appUserFriendBean);
				}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(pstmt);
		}
		return appUserFriendBeanList;
	}
	public int insertUserActivityToTempTable(List<ActivityBean> activityBeanList , String friendUserIds , Long searchById) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProced = "{call usp_insertFriendActivityToTempTable(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProced);
			deleteTemUserActivity(friendUserIds);
			
			for (ActivityBean activityBean : activityBeanList) {

				
				callableStatement.setLong(1, activityBean.getUserId());
				callableStatement.setLong(2, searchById);
				if (activityBean.getMeetingId() != null)
					callableStatement.setLong(3, activityBean.getMeetingId());
				else
					callableStatement.setLong(3, 0l);
				callableStatement.setString(4, activityBean.getMeetingMessage());
				callableStatement.setString(5, activityBean.getMeetingUserImageId());
				if (Utility.checkValidString(activityBean.getFromDate()))
					callableStatement.setTimestamp(6, Timestamp.valueOf(activityBean.getFromDate()));
				else
					callableStatement.setTimestamp(6, null);
				if (Utility.checkValidString(activityBean.getToDate()))
					callableStatement.setTimestamp(7, Timestamp.valueOf(activityBean.getToDate()));
				else
					callableStatement.setTimestamp(7, null);
				if (Utility.checkValidString(activityBean.getRegistrationDate()))
					callableStatement.setTimestamp(8, Timestamp.valueOf(activityBean.getRegistrationDate()));
				else
					callableStatement.setTimestamp(8, null);
				callableStatement.setString(9, activityBean.getProfileImage());
				callableStatement.setString(10, activityBean.getCoverImage());
				callableStatement.setString(11, activityBean.getStatus());
				callableStatement.setString(12, activityBean.getImageDescription());
				callableStatement.setString(13, activityBean.getImage());
				callableStatement.setInt(14, activityBean.getIsPrivate());
				callableStatement.setString(15, activityBean.getAddress());
				callableStatement.setString(16, activityBean.getUserProfileImageId());
				callableStatement.setString(17, dateTimeFormat.format(activityBean.getDate()));
			
				callableStatement.addBatch();
			}
			
			int[] isInserted = callableStatement.executeBatch();
			
			System.out.println("isInserted  ::::::::::   "+isInserted.length);
			
			return isInserted.length;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return 0;
	}
	
	public Boolean deleteTemUserActivity(String userIds) {

		Connection conn = null;
		Statement statement = null;
		JSONObject finalJson = new JSONObject();

		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "DELETE  FROM tbl_TempFriendsActivity WHERE UserID IN ("+userIds+")";
			statement = conn.createStatement();
			int isDeleted = statement.executeUpdate(sql);
			if (isDeleted != 0) {
				finalJson.put("status", true);
				finalJson.put("message", "User photos inserted successfully");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return false;
	}
	
public static JSONArray setUserActivity(List<ActivityBean> userActivityBeanList){
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = null;
			int count = 1;
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat timeFormat = new SimpleDateFormat("HH:mm");
			
			try{
			
			for (ActivityBean activityBean : userActivityBeanList) {

				json = new JSONObject();
				json.put("date", dateTimeFormat.format(activityBean.getDate()));
				json.put("userId", activityBean.getUserId());
				if(Utility.checkValidString(activityBean.getUserProfileImageId())){
					
					json.put("userProfileImageId", activityBean.getUserProfileImageId());
				}
				if (activityBean.getMeetingId() != null) {
					json.put("type", ActivityType.MEETING_TYPE.toString());
					json.put("meetingId", activityBean.getMeetingId());
					json.put("meetingMessage", activityBean.getMeetingMessage());
					json.put("meetingUserImageId", activityBean.getMeetingUserImageId());
				}else if (Utility.checkValidString(activityBean.getStatus())) {
					json.put("status", activityBean.getStatus());
					json.put("type", ActivityType.STATUS_TYPE.toString());
				}else if (Utility.checkValidString(activityBean.getImageDescription())) {
					json.put("imageDescription", activityBean.getImageDescription());
					json.put("imageId", activityBean.getImage());
					json.put("type", ActivityType.UPLOAD_TYPE.toString());
				}else if (Utility.checkValidString(activityBean.getAddress())) {
					json.put("address", activityBean.getAddress());
					json.put("type", ActivityType.LOCATION_TYPE.toString());
				}else if (Utility.checkValidString(activityBean.getFromDate())) {

					java.util.Date fromTime = dateTimeFormat.parse(activityBean.getFromDate());
					json.put("freeDate", dateFormat.format(fromTime));
					json.put("freeFromTime", timeFormat.format(fromTime));
					json.put("freeToTime", timeFormat.format(dateFormat.parse(activityBean.getToDate())));

					json.put("type", ActivityType.FREE_TIME_TYPE.toString());
				}else if (Utility.checkValidString(activityBean.getProfileImage())) {
					json.put("profileImageId", activityBean.getProfileImage());
					json.put("type", ActivityType.PROFILE_TYPE.toString());
				}else if (Utility.checkValidString(activityBean.getCoverImage())) {
					json.put("coverImageId", activityBean.getCoverImage());
					json.put("type", ActivityType.COVER_TYPE.toString());
				}else if (Utility.checkValidString(activityBean.getRegistrationDate())) {
					json.put("registrationDate", dateTimeFormat.format(activityBean.getDate()));
					json.put("type", ActivityType.JOIN_DATE_TYPE.toString());
				}
				
				jsonArray.put(json);
			 if(count == 10){
				 break;
			 }
				count++;
			}
			}catch(Exception e){
				e.printStackTrace();
			}
			return jsonArray;
	}

	@GET
	@Path("/getFriendActivity/{userId}/{offSetValue}")
	@Produces(MediaType.TEXT_PLAIN)
	public String setUserActivity(@PathParam("userId") Long userId, @PathParam("offSetValue") int offSetValue) {

		JSONArray jsonArray = new JSONArray();

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		JSONObject json = null;
		try {
			// System.out.println("userId: "+userId+" offSetValue :
			// "+offSetValue);
			conn = DataSourceConnection.getDBConnection();
			String friendActivityStoreProc = "{call usp_getfriendActivity(?,?)}";
			callableStatement = conn.prepareCall(friendActivityStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setInt(2, offSetValue * 10);

			ResultSet rs = callableStatement.executeQuery();

			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat timeFormat = new SimpleDateFormat("HH:mm");

			while (rs.next()) {

				json = new JSONObject();

				json.put("date", rs.getString("CreatedDateTime"));
				json.put("userId", rs.getLong("UserID"));
				json.put("userProfileImageId", rs.getString("UserProfileImageID"));
				// json.put("activityId", rs.getLong("UserActivityID"));
				Long meetingId = rs.getLong("MeetingID");
				if (meetingId != null && meetingId != 0l) {
					json.put("type", ActivityType.MEETING_TYPE.toString());
					json.put("meetingId", meetingId);
					json.put("meetingMessage", rs.getString("MeetingMessage"));
					json.put("meetingUserImageId",rs.getString("MeetingUserImageID"));
				}else if (Utility.checkValidString(rs.getString("StatusDescription"))) {
					json.put("status", rs.getString("StatusDescription"));
					json.put("type", ActivityType.STATUS_TYPE.toString());
				}else if (Utility.checkValidString(rs.getString("ImageDescription"))) {
					json.put("imageDescription", rs.getString("ImageDescription"));
					json.put("imageId", rs.getString("ImageID"));
					json.put("type", ActivityType.UPLOAD_TYPE.toString());
				}else if (Utility.checkValidString(rs.getString("Address"))) {
					json.put("address", rs.getString("Address"));
					json.put("type", ActivityType.LOCATION_TYPE.toString());
				}else if (Utility.checkValidString(rs.getString("FromDateTime"))) {
					java.util.Date fromTime = dateTimeFormat.parse(rs.getString("FromDateTime"));
					json.put("freeDate", dateFormat.format(fromTime));
					json.put("freeFromTime", timeFormat.format(fromTime));
					json.put("freeToTime", timeFormat.format(dateFormat.parse(rs.getString("ToDateTime"))));
					json.put("type", ActivityType.FREE_TIME_TYPE.toString());
				}else if (Utility.checkValidString(rs.getString("RegistrationDateTime"))) {
					json.put("registrationDate", rs.getString("CreatedDateTime"));
					json.put("type", ActivityType.JOIN_DATE_TYPE.toString());
				}else if (Utility.checkValidString(rs.getString("ProfileImageID"))) {
					json.put("profileImageId", rs.getString("ProfileImageID"));
					json.put("type", ActivityType.PROFILE_TYPE.toString());
				}else if (Utility.checkValidString(rs.getString("CoverImageID"))) {
					json.put("coverImageId", rs.getString("CoverImageID"));
					json.put("type", ActivityType.COVER_TYPE.toString());
				}

				jsonArray.put(json);
			}
			finalJson.put("userActivityArray", jsonArray);
			finalJson.put("isNextActivityExist", jsonArray.length() >= 10 ? true : false);
			finalJson.put("status", true);
			finalJson.put("message", "User activity fetched successfully");

			return finalJson.toString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return jsonArray.toString();
	}
}
