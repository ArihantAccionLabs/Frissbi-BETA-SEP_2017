package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.ActivityBean;
import org.kleverlinks.enums.ActivityType;
import org.mongo.dao.MongoDBJDBC;
import org.util.Utility;
import org.util.service.ServiceUtility;

import com.mongodb.DBObject;

@Path("UserActivityService")
public class UserActivityService {

	
	@POST
	@Path("/insertCoverImage")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String insertCoverImage(String imageFile) {
		JSONObject finalJson = new JSONObject();
		JSONObject coverJson = new JSONObject(imageFile);
		String mongoFileId = null;
		try {
			MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
			mongoFileId = mongoDBJDBC.insertCoverImageToMongoDb(coverJson);
			System.out.println("mongoFileId :   " + mongoFileId);
			if (mongoFileId != null) {
				coverJson.remove("file");
				coverJson.put("mongoFileId", mongoFileId);
				if (mongoDBJDBC.updateCoverImage(coverJson)) {

					finalJson.put("status", true);
					finalJson.put("message", "Cover image updated successfully");

					return finalJson.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
	}

	@POST
	@Path("/insertUserStatus")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String insertUserStatus(String userStatus) {

		JSONObject finalJson = new JSONObject();
		JSONObject userCurrentStatus = new JSONObject(userStatus);
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {

			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_insertUserStatus(?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userCurrentStatus.getLong("userId"));
			callableStatement.setString(2, userCurrentStatus.getString("description"));
			callableStatement.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
			callableStatement.registerOutParameter(4, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			int isError = callableStatement.getInt(4);
			System.out.println(isError + "  value  :" + value);
			if (value != 0) {
				finalJson.put("status", true);
				finalJson.put("message", "Cover image updated successfully");
			}
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
	}

	@GET
	@Path("/getUserActivity/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserActivity(@PathParam("userId") Long userId) {
		
		JSONObject finalJson = new JSONObject();
		List<ActivityBean> userActivityBeanList = new ArrayList<ActivityBean>();
		JSONArray jsonArray = new JSONArray();
		JSONObject json = null;
		try {
			JSONObject jsonObject = ServiceUtility.getUserImageId(userId);
			
			userActivityBeanList.addAll(getImageDocument(jsonObject));			
			userActivityBeanList.addAll(getUserMeetingActivity(userId));
			userActivityBeanList.addAll(getUserPostedActivity(userId));
			
			if(userActivityBeanList.size() > 10){
				int isInserted = insertUserActivityToTempTable(userActivityBeanList);
				if(isInserted != 0){
					setUserActivity(userActivityBeanList);
				}
			}else{
				setUserActivity(userActivityBeanList);
			}
			
			finalJson.put("status", true);
			finalJson.put("message", "User activity fetched successfully");
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}

	public 	JSONArray setUserActivity(List<ActivityBean> userActivityBeanList){
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = null;
				
			for (ActivityBean activityBean : userActivityBeanList) {
				
				json = new JSONObject();
				json.put("date", activityBean.getDate());
				json.put("activityId", activityBean.getActivityId());
				json.put("userId", activityBean.getUserId());
				if(activityBean.getMeetingId() != null){
					json.put("type", ActivityType.MEETING_TYPE.toString());
					json.put("meetingId", activityBean.getMeetingId());
					json.put("meetingMessage", activityBean.getMeetingMessage());
				}
				else if(Utility.checkValidString(activityBean.getStatus())){
				  json.put("status", activityBean.getStatus());
				  json.put("type", ActivityType.STATUS_TYPE.toString());
			  }
				else if(Utility.checkValidString(activityBean.getImageDescription())){
				  json.put("imageDescription", activityBean.getImageDescription());
				  json.put("image", activityBean.getImage());
				  json.put("type", ActivityType.UPLOAD_TYPE.toString());
			  }
				else if(Utility.checkValidString(activityBean.getAddress())){
				  json.put("address", activityBean.getAddress()); 
				  json.put("type", ActivityType.LOCATION_TYPE.toString());
			  }
				else if(Utility.checkValidString(activityBean.getFromDate())){
				  json.put("fromDate", activityBean.getFromDate()); 
				  json.put("toDate", activityBean.getToDate()); 
				  json.put("type", ActivityType.FREE_TIME_TYPE.toString());
			  }
				
				jsonArray.put(json);
				
			}
			return jsonArray;
	}
		
		
	public List<ActivityBean> getUserMeetingActivity(Long userId) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetUserMeetingActivity(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			ResultSet rs = callableStatement.executeQuery();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
            
			while (rs.next()) {
				userActivityBean = new ActivityBean();
				java.util.Date senderFromDateTime = df.parse(rs.getString("SenderFromDateTime"));
				java.util.Date senderToDateTime = df.parse(rs.getString("SenderToDateTime"));
				userActivityBean.setMeetingId(rs.getLong("MeetingID"));
				userActivityBean.setDate(senderFromDateTime);
				userActivityBean.setMeetingMessage("You have a meeting " + rs.getString("MeetingDescription") +" form "+new SimpleDateFormat("HH:mm").format(senderFromDateTime)+" to "+new SimpleDateFormat("HH:mm").format(senderToDateTime)+" at "+senderFromDateTime);
			
				userActivityBeanList.add(userActivityBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return userActivityBeanList;
	}

	public List<ActivityBean> getUserPostedActivity(Long userId) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		try {
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_GetUserPostedActivityByUserID(?)}";
			callableStatement = conn.prepareCall(selectStoreProcedue);
			callableStatement.setLong(1, userId);
			ResultSet rs = callableStatement.executeQuery();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			while (rs.next()) {
				userActivityBean = new ActivityBean();
				userActivityBean.setActivityId(rs.getLong("UserActivityID"));
				userActivityBean.setStatus(rs.getString("StatusDescription"));
				userActivityBean.setImageDescription(rs.getString("ImageDescription"));
				userActivityBean.setImage(rs.getString("Image"));
				userActivityBean.setFromDate(rs.getString("FromDateTime"));
				userActivityBean.setFromDate(rs.getString("Address"));
				userActivityBean.setIsPrivate(rs.getInt("IsPrivate"));
				userActivityBean.setDate(df.parse(rs.getString("CreatedDateTime")));
				
				userActivityBeanList.add(userActivityBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return userActivityBeanList;
	}
	
	public List<ActivityBean> getImageDocument(JSONObject jsonObject){
		
		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		
		if (jsonObject.has("profileImageId")) {
			MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
			DBObject profileDbObj = mongoDBJDBC.getFile(jsonObject.getString("profileImageId"));
			if (profileDbObj != null) {
				
				userActivityBean = new ActivityBean();
				JSONObject mongoJson = new JSONObject(profileDbObj.toString());
				userActivityBean.setProfileImage(mongoJson.getString("documentBytes"));
				try {
					userActivityBean.setDate(java.util.Date.from(Instant.parse(mongoJson.getJSONObject("createdDate").getString("$date"))));
				} catch (Exception e) {
					e.printStackTrace();
				}
				userActivityBeanList.add(userActivityBean);
			}
			DBObject coverDbObj = mongoDBJDBC.getFile(jsonObject.getString("coverImageId"));
			if (coverDbObj != null) {
				JSONObject mongoJson = new JSONObject(coverDbObj.toString());
				userActivityBean = new ActivityBean();
				userActivityBean.setCoverImage(mongoJson.getString("documentBytes"));
				try {
					userActivityBean.setDate(java.util.Date.from(Instant.parse(mongoJson.getJSONObject("createdDate").getString("$date"))));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				userActivityBeanList.add(userActivityBean);
			}
		}
		return userActivityBeanList;
	}
	
	public int insertUserActivityToTempTable(List<ActivityBean> activityBeanList) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		
		try {
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_insertUserActivityToTempTable(?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(selectStoreProcedue);
			
			for (ActivityBean activityBean : activityBeanList) {
				
				callableStatement.setLong(1, activityBean.getActivityId());
				callableStatement.setLong(2, activityBean.getUserId());
				callableStatement.setLong(3, activityBean.getMeetingId());
				callableStatement.setTimestamp(4, Timestamp.valueOf(activityBean.getFromDate()));
				callableStatement.setTimestamp(5, Timestamp.valueOf(activityBean.getToDate()));
				callableStatement.setString(6, activityBean.getStatus());
				callableStatement.setString(7, activityBean.getImageDescription());
				callableStatement.setString(8, activityBean.getImage());
				callableStatement.setInt(9, activityBean.getIsPrivate());
				callableStatement.setString(10, activityBean.getAddress());
				callableStatement.setTimestamp(11, new Timestamp(activityBean.getDate().getTime()));
			
				callableStatement.addBatch();
			}
			
			int[] isInserted = callableStatement.executeBatch();
			return isInserted.length;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return 0;
	}
}
