package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.FreeTimePostBean;
import org.kleverlinks.bean.UserFreeTimeBean;
import org.util.Utility;
import org.util.service.ServiceUtility;

@Path("TimeLineService")
public class TimeLineService {

	@POST
	@Path("/postTime")
	@Consumes(MediaType.APPLICATION_JSON)
	public String postFreeTime(String postFreeTime) {

		System.out.println("postFreeTime  :    " + postFreeTime.toString());
		JSONObject postFreeTimeJson = new JSONObject(postFreeTime);
		JSONObject finalJson = new JSONObject();
		FreeTimePostBean conflictedTimeBean = null;
		int isIserted = 0;

		try {
			UserFreeTimeBean userFreeTimeBean = new UserFreeTimeBean(postFreeTimeJson);

			if (userFreeTimeBean.getIsConflicted()) {
				if(deleteFreeTime(userFreeTimeBean.getUserId() , userFreeTimeBean.getUserFreeTimeId()) != 0){
					 isIserted = insertFreeTime(userFreeTimeBean);	
				}
			}else{
				conflictedTimeBean = checkFreeTimeConfliction(userFreeTimeBean);
				System.out.println("conflictedTimeBean  "+conflictedTimeBean);
				if (conflictedTimeBean == null) {
					isIserted = insertFreeTime(userFreeTimeBean);
				} else {
					finalJson.put("userFreeTimeId", conflictedTimeBean.getUserFreeTimeId());
					finalJson.put("message","Time is conflicting  on "
							+ conflictedTimeBean.getDate() + " from " + conflictedTimeBean.getStartTime() + " to "
							+ conflictedTimeBean.getEndTime());
					finalJson.put("status", true);
					finalJson.put("isTimePosted", false);
					
					return finalJson.toString();
				}
			}
			System.out.println("isIserted  :  "+isIserted);
			if(isIserted != 0){
				finalJson.put("status", true);
				finalJson.put("isTimePosted", true);
				finalJson.put("message", "Free time posted successfully");
				return finalJson.toString();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		finalJson.put("isTimePosted", false);
		return finalJson.toString();
	}

	private int insertFreeTime(UserFreeTimeBean userFreeTimeBean){
		
		int isIserted = 0;
		Connection conn = null;
		CallableStatement callableStatement = null;
		
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_InsertUserFreeTimes(?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userFreeTimeBean.getUserId());
			callableStatement.setTimestamp(2, Timestamp.valueOf(userFreeTimeBean.getFreeFromTime()));
			callableStatement.setTimestamp(3,Timestamp.valueOf(userFreeTimeBean.getFreeToTime()));
			callableStatement.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			 isIserted = callableStatement.executeUpdate();
			 
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
	}
		return isIserted;
	}
	
 private int deleteFreeTime(Long userId , Long userActivityId){
		
		int isDeleted = 0;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try{
		String sql = "DELETE FROM tbl_UserActivity WHERE UserID=? AND UserActivityID=?";
		connection  =  DataSourceConnection.getDBConnection();
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setLong(1, userId);
		preparedStatement.setLong(2, userActivityId);
		
		isDeleted = preparedStatement.executeUpdate();
	} catch (Exception e) {
		e.printStackTrace();
	}finally{
		ServiceUtility.closeConnection(connection);
		ServiceUtility.closeSatetment(preparedStatement);
	}
		return isDeleted;
	}
	
	
	private FreeTimePostBean checkFreeTimeConfliction(UserFreeTimeBean userFreeTimeBean){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		FreeTimePostBean conflictedFreeTime = null;
		
		try {
			String sql = "SELECT UserActivityID,UserID,FromDateTime,ToDateTime FROM  tbl_UserActivity WHERE userID = ? "
					+ "AND ((FromDateTime BETWEEN ? AND ?) OR (ToDateTime BETWEEN ? AND ?))";
			connection  =  DataSourceConnection.getDBConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, userFreeTimeBean.getUserId());
			preparedStatement.setTimestamp(2, Timestamp.valueOf(userFreeTimeBean.getFreeFromTime()));
			preparedStatement.setTimestamp(3, Timestamp.valueOf(userFreeTimeBean.getFreeToTime().minusMinutes(1l)));
			preparedStatement.setTimestamp(4, Timestamp.valueOf(userFreeTimeBean.getFreeFromTime().plusMinutes(1l)));
			preparedStatement.setTimestamp(5, Timestamp.valueOf(userFreeTimeBean.getFreeToTime()));
			
		   ResultSet rs = 	preparedStatement.executeQuery();
			
		 while(rs.next()){
			 
			 if(Utility.checkValidString(rs.getString("FromDateTime"))){
				 
				 conflictedFreeTime = new FreeTimePostBean();
				 
				 LocalDateTime fromTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("FromDateTime"));
				 LocalDateTime toTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("ToDateTime"));
				 conflictedFreeTime.setDate(fromTime.toLocalDate());
				 conflictedFreeTime.setStartTime(ServiceUtility.updateTime(fromTime.getHour(), fromTime.getMinute()));
				 conflictedFreeTime.setEndTime(ServiceUtility.updateTime(toTime.getHour(), toTime.getMinute()));
				 conflictedFreeTime.setUserId(rs.getLong("UserID"));
				 conflictedFreeTime.setUserFreeTimeId(rs.getLong("UserActivityID"));
			 }
		 }
		 
		}catch (Exception e) {
          e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(connection);
			ServiceUtility.closeSatetment(preparedStatement);
		}
		return conflictedFreeTime;
	}
	
	
	@GET
	@Path("/getOneWeekMeetingInfo/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetails(@PathParam("userId") Long userId) {

		JSONObject finalJson = new JSONObject();

		try {

			Calendar currentDate = Calendar.getInstance();

			Calendar furtherDate = Calendar.getInstance();

			furtherDate.set(Calendar.HOUR, 0);
			furtherDate.set(Calendar.MINUTE, 0);
			furtherDate.set(Calendar.SECOND, 0);
			furtherDate.set(Calendar.HOUR_OF_DAY, 0);

			furtherDate.setTime(furtherDate.getTime());
			furtherDate.add(Calendar.DATE, 7);
			furtherDate.set(Calendar.HOUR, 23);
			furtherDate.set(Calendar.MINUTE, 59);
			furtherDate.set(Calendar.SECOND, 00);

			finalJson.put("meetingArray",ServiceUtility.getMeetingArray(userId, currentDate.getTime(), furtherDate.getTime()));
			finalJson.put("status", true);
			finalJson.put("message", "meeting fetched successfully");

			System.out.println("" + finalJson);
			return finalJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
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
}

