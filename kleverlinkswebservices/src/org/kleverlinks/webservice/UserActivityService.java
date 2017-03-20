package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			//System.out.println("mongoFileId :   " + mongoFileId);
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
		//	System.out.println(isError + "  value  :" + value);
			if (value != 0) {
				finalJson.put("status", true);
				finalJson.put("message", "Status updated successfully");
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
		try {
			JSONObject jsonObject = ServiceUtility.getUserImageId(userId);
			userActivityBeanList.addAll(getUserDetails(jsonObject));			
			userActivityBeanList.addAll(getUserMeetingActivity(userId));
			userActivityBeanList.addAll(getUserPostedActivity(userId));
			
			Utility.sortList(userActivityBeanList);
			
			System.out.println("size  :   "+userActivityBeanList.size());
			
			if(userActivityBeanList.size() > 10){
				int isInserted = insertUserActivityToTempTable(userActivityBeanList , userId);
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
	

		public JSONArray setUserActivity(List<ActivityBean> userActivityBeanList){
		
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

				if (activityBean.getMeetingId() != null) {
					json.put("type", ActivityType.MEETING_TYPE.toString());
					json.put("meetingId", activityBean.getMeetingId());
					json.put("meetingMessage", activityBean.getMeetingMessage());
				}
				if (Utility.checkValidString(activityBean.getStatus())) {
					json.put("status", activityBean.getStatus());
					json.put("type", ActivityType.STATUS_TYPE.toString());
				}
				if (Utility.checkValidString(activityBean.getImageDescription())) {
					json.put("imageDescription", activityBean.getImageDescription());
					json.put("imageId", activityBean.getImage());
					json.put("type", ActivityType.UPLOAD_TYPE.toString());
				}
				if (Utility.checkValidString(activityBean.getAddress())) {
					json.put("address", activityBean.getAddress());
					json.put("type", ActivityType.LOCATION_TYPE.toString());
				}
				if (Utility.checkValidString(activityBean.getFromDate())) {
					java.util.Date fromTime = dateTimeFormat.parse(activityBean.getFromDate());
					json.put("freeDate", dateFormat.format(fromTime));
					json.put("freeFromTime", timeFormat.format(fromTime));
					json.put("freeToTime", timeFormat.format(dateTimeFormat.parse(activityBean.getToDate())));

					json.put("type", ActivityType.FREE_TIME_TYPE.toString());
				}
				if (Utility.checkValidString(activityBean.getProfileImage())) {
					json.put("profileImageId", activityBean.getProfileImage());
					json.put("type", ActivityType.PROFILE_TYPE.toString());
				}
				if (Utility.checkValidString(activityBean.getCoverImage())) {
					json.put("coverImageId", activityBean.getCoverImage());
					json.put("type", ActivityType.COVER_TYPE.toString());
				}
				if (Utility.checkValidString(activityBean.getRegistrationDate())) {
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
	@Path("/getUserActivity/{userId}/{offSetValue}")
	@Produces(MediaType.TEXT_PLAIN)
	public String setUserActivity(@PathParam("userId") Long userId , @PathParam("offSetValue") int offSetValue){
	
	JSONArray jsonArray = new JSONArray();
		
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		JSONObject json = null;
		try {
			//System.out.println("userId:   "+userId+"  offSetValue  :  "+offSetValue);
			conn = DataSourceConnection.getDBConnection();
			String insertFrissbiLocationStoreProc = "{call usp_getUserActivity(?,?)}";
			callableStatement = conn.prepareCall(insertFrissbiLocationStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setInt(2, offSetValue*10);
			
			ResultSet rs = callableStatement.executeQuery();
			
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat timeFormat = new SimpleDateFormat("HH:mm");
			
			while(rs.next()){
				
				json = new JSONObject();
			
				json.put("date", rs.getString("CreatedDateTime"));
				json.put("userId", rs.getLong("UserID"));
				//json.put("activityId", rs.getLong("UserActivityID"));
				Long meetingId = rs.getLong("MeetingID"); 
				if(meetingId != null && meetingId != 0l){
					json.put("type", ActivityType.MEETING_TYPE.toString());
					json.put("meetingId", meetingId);
					json.put("meetingMessage", rs.getString("MeetingMessage"));
				} if(Utility.checkValidString(rs.getString("StatusDescription"))){
				  json.put("status", rs.getString("StatusDescription"));
				  json.put("type", ActivityType.STATUS_TYPE.toString());
			  } if(Utility.checkValidString(rs.getString("ImageDescription"))){
				  json.put("imageDescription", rs.getString("ImageDescription"));
				  json.put("imageId",rs.getString("ImageID"));
				  json.put("type", ActivityType.UPLOAD_TYPE.toString());
			  } if(Utility.checkValidString(rs.getString("Address"))){
				  json.put("address", rs.getString("Address")); 
				  json.put("type", ActivityType.LOCATION_TYPE.toString());
			  } if(Utility.checkValidString(rs.getString("FromDateTime"))){
				  java.util.Date fromTime = dateTimeFormat.parse(rs.getString("FromDateTime"));
				  json.put("freeDate", dateFormat.format(fromTime)); 
				  json.put("freeFromTime",timeFormat.format(fromTime)); 
				  json.put("freeToTime",timeFormat.format(dateTimeFormat.parse(rs.getString("ToDateTime")))); 
				  json.put("type", ActivityType.FREE_TIME_TYPE.toString());
			  }if(Utility.checkValidString(rs.getString("RegistrationDateTime"))){
				  json.put("registrationDate", rs.getString("CreatedDateTime")); 
				  json.put("type", ActivityType.JOIN_DATE_TYPE.toString());
			  } if(Utility.checkValidString(rs.getString("ProfileImageID"))){
				  json.put("profileImageId", rs.getString("ProfileImageID")); 
				  json.put("type", ActivityType.PROFILE_TYPE.toString());
			  }if(Utility.checkValidString(rs.getString("CoverImageID"))){
				  json.put("coverImageId", rs.getString("CoverImageID")); 
				  json.put("type", ActivityType.COVER_TYPE.toString());
			  }	  
					  
				jsonArray.put(json);
			}
			finalJson.put("userActivityArray", jsonArray);
			finalJson.put("isNextActivityExist", jsonArray.length() >= 10 ? true:false);
			finalJson.put("status", true);
			finalJson.put("message", "User activity fetched successfully");		
			
			return finalJson.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return jsonArray.toString();
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
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
            String fullName = "";
            String others = "";
			while (rs.next()) {
				userActivityBean = new ActivityBean();
				java.util.Date senderFromDateTime = df.parse(rs.getString("SenderFromDateTime"));
				java.util.Date senderToDateTime = df.parse(rs.getString("SenderToDateTime"));
				userActivityBean.setUserId(userId);
				userActivityBean.setMeetingId(rs.getLong("MeetingID"));
				userActivityBean.setDate(df.parse(rs.getString("RequestDateTime")));
			
				JSONArray usersArray = ServiceUtility.getReceptionistDetailsByMeetingId(userActivityBean.getMeetingId());
				Set<Long> usersSet = new HashSet<Long>();
				for (int i = 0; i < usersArray.length(); i++) {
					usersSet.add(usersArray.getJSONObject(i).getLong("userId"));
					fullName = usersArray.getJSONObject(i).getString("fullName");
				}
				if(usersSet.size() > 1){
					 others = " and "+ (usersSet.size()-1)+" others ";
				}
				
				userActivityBean.setMeetingMessage("You have a meeting " + rs.getString("MeetingDescription") +" with "+fullName +others+" from "+new SimpleDateFormat("HH:mm").format(senderFromDateTime)+" to "+new SimpleDateFormat("HH:mm").format(senderToDateTime)+" on "+dateFormat.format(senderFromDateTime));
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
				userActivityBean.setUserId(userId);
				userActivityBean.setActivityId(rs.getLong("UserActivityID"));
				userActivityBean.setStatus(rs.getString("StatusDescription"));
				userActivityBean.setImageDescription(rs.getString("ImageDescription"));
				userActivityBean.setImage(rs.getString("ImageID"));
				userActivityBean.setFromDate(rs.getString("FromDateTime"));
				userActivityBean.setToDate(rs.getString("ToDateTime"));
				userActivityBean.setAddress(rs.getString("Address"));
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
	
	public List<ActivityBean> getUserDetails(JSONObject jsonObject){
		
		ActivityBean userActivityBean = null;
		List<ActivityBean> userActivityBeanList = new ArrayList<>();
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
		if (jsonObject.has("profileImageId")) {
			DBObject profileDbObj = mongoDBJDBC.getFile(jsonObject.getString("profileImageId"));
			if (profileDbObj != null) {
				
				userActivityBean = new ActivityBean();
				userActivityBean.setUserId(jsonObject.getLong("userId"));
				JSONObject mongoJson = new JSONObject(profileDbObj.toString());
				userActivityBean.setProfileImage(jsonObject.getString("profileImageId"));
				try {
					userActivityBean.setDate(java.util.Date.from(Instant.parse(mongoJson.getJSONObject("createdDate").getString("$date"))));
				} catch (Exception e) {
					e.printStackTrace();
				}
				userActivityBeanList.add(userActivityBean);
			}
		}	if (jsonObject.has("coverImageId")) {
			DBObject coverDbObj = mongoDBJDBC.getFile(jsonObject.getString("coverImageId"));
			if (coverDbObj != null) {
				JSONObject mongoJson = new JSONObject(coverDbObj.toString());
				userActivityBean = new ActivityBean();
				userActivityBean.setUserId(jsonObject.getLong("userId"));
				userActivityBean.setCoverImage(jsonObject.getString("coverImageId"));
				try {
					userActivityBean.setDate(java.util.Date.from(Instant.parse(mongoJson.getJSONObject("createdDate").getString("$date"))));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				userActivityBeanList.add(userActivityBean);
			}
		}
		
		if (jsonObject.has("registrationDateTime")) {
			userActivityBean = new ActivityBean();
			userActivityBean.setUserId(jsonObject.getLong("userId"));
			try {
				userActivityBean.setDate(dateTimeFormat.parse(jsonObject.getString("registrationDateTime")));
			} catch (Exception e) {
				e.printStackTrace();
			}
			userActivityBean.setRegistrationDate(jsonObject.getString("registrationDateTime"));
			
			userActivityBeanList.add(userActivityBean);
		}
		return userActivityBeanList;
	}
	
	public int insertUserActivityToTempTable(List<ActivityBean> activityBeanList , Long userId) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			deleteTemUserActivity(userId);
			
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_insertUserActivityToTempTable(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(selectStoreProcedue);
			
			for (ActivityBean activityBean : activityBeanList) {

				callableStatement.setLong(1, activityBean.getUserId());
				if (activityBean.getMeetingId() != null)
					callableStatement.setLong(2, activityBean.getMeetingId());
				else
					callableStatement.setLong(2, 0l);
				callableStatement.setString(3, activityBean.getMeetingMessage());
				if (Utility.checkValidString(activityBean.getFromDate()))
					callableStatement.setTimestamp(4, Timestamp.valueOf(activityBean.getFromDate()));
				else
					callableStatement.setTimestamp(4, null);
				if (Utility.checkValidString(activityBean.getToDate()))
					callableStatement.setTimestamp(5, Timestamp.valueOf(activityBean.getToDate()));
				else
					callableStatement.setTimestamp(5, null);
				if (Utility.checkValidString(activityBean.getRegistrationDate()))
					callableStatement.setTimestamp(6, Timestamp.valueOf(activityBean.getRegistrationDate()));
				else
					callableStatement.setTimestamp(6, null);
				callableStatement.setString(7, activityBean.getProfileImage());
				callableStatement.setString(8, activityBean.getCoverImage());
				callableStatement.setString(9, activityBean.getStatus());
				callableStatement.setString(10, activityBean.getImageDescription());
				callableStatement.setString(11, activityBean.getImage());
				callableStatement.setInt(12, activityBean.getIsPrivate());
				callableStatement.setString(13, activityBean.getAddress());
				callableStatement.setString(14, dateTimeFormat.format(activityBean.getDate()));
			
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
	
	@POST
	@Path("/insertUserPhotos")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String insertUserPhotos(String userPhotos) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();

		try {
			JSONObject jsonObject = new JSONObject(userPhotos);
			MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_insertUserPhotos(?,?,?,?)}";
			callableStatement = conn.prepareCall(selectStoreProcedue);

			callableStatement.setLong(1, jsonObject.getLong("userId"));
			callableStatement.setString(2, mongoDBJDBC.insertImageToMongoDb(jsonObject));
			callableStatement.setString(3, jsonObject.getString("description"));
			callableStatement.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));

			int isInserted = callableStatement.executeUpdate();
			if (isInserted != 0) {
				finalJson.put("status", true);
				finalJson.put("message", "User photos inserted successfully");
				return finalJson.toString();
			}
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
	@Path("/getImage/{imageId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getImage(@PathParam("imageId") String imageId) {

		JSONObject finalJson = new JSONObject();
		try {
			MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
		    mongoDBJDBC.getUriImage(imageId);
		    
		    finalJson.put("uriImage",  mongoDBJDBC.getUriImage(imageId));
		    finalJson.put("status", true);
			finalJson.put("message", "User photos fetched successfully");
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}
	

	public Boolean deleteTemUserActivity(Long userId) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();

		try {
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_deleteTempUserActivity(?)}";
			callableStatement = conn.prepareCall(selectStoreProcedue);

			callableStatement.setLong(1, userId);

			int isDeleted = callableStatement.executeUpdate();
			if (isDeleted != 0) {
				finalJson.put("status", true);
				finalJson.put("message", "User photos inserted successfully");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return false;
	}
	
}
