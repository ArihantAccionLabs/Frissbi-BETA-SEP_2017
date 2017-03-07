package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
					finalJson.put("message","Time is conflicting for " + conflictedTimeBean.getDescription().toUpperCase() + " on "
							+ conflictedTimeBean.getDate() + " from " + conflictedTimeBean.getStartTime() + " to "
							+ conflictedTimeBean.getEndTime());
					finalJson.put("status", true);
					finalJson.put("isTimePosted", false);
					
					return finalJson.toString();
				}
			}
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
			String insertStoreProc = "{call usp_InsertUserFreeTimes(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userFreeTimeBean.getUserId());
			callableStatement.setTimestamp(2, new Timestamp(userFreeTimeBean.getFreeFromTime().getTime()));
			callableStatement.setTimestamp(3, new Timestamp(userFreeTimeBean.getFreeToTime().getTime()));
			callableStatement.setString(4, userFreeTimeBean.getDescription());
			callableStatement.registerOutParameter(5, Types.INTEGER);
			 isIserted = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(5);
			
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
	
 private int deleteFreeTime(Long userId , Long userFreeTimeId){
		
		int isDeleted = 0;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try{
		String sql = "DELETE FROM tbl_UserFreeTimes WHERE UserID=? AND UserFreeTimeId=?";
		connection  =  DataSourceConnection.getDBConnection();
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setLong(1, userId);
		preparedStatement.setLong(2, userFreeTimeId);
		
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
		
		LocalDateTime localFromTime = LocalDateTime.ofInstant(userFreeTimeBean.getFreeFromTime().toInstant(), ZoneId.systemDefault());
		LocalDateTime localToTime = LocalDateTime.ofInstant(userFreeTimeBean.getFreeToTime().toInstant(), ZoneId.systemDefault());
		try {
			String sql = "SELECT UserFreeTimeID,UserID,FromDateTime,ToDateTime,Description FROM  tbl_UserFreeTimes WHERE userID = ? "
					+ "AND ((FromDateTime BETWEEN ? AND ?) OR (ToDateTime BETWEEN ? AND ?))";
			connection  =  DataSourceConnection.getDBConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, userFreeTimeBean.getUserId());
			preparedStatement.setTimestamp(2, Timestamp.valueOf(localFromTime));
			preparedStatement.setTimestamp(3, Timestamp.valueOf(localToTime.minusMinutes(1l)));
			preparedStatement.setTimestamp(4, Timestamp.valueOf(localFromTime.plusMinutes(1l)));
			preparedStatement.setTimestamp(5, Timestamp.valueOf(localToTime));
			
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
				 conflictedFreeTime.setUserFreeTimeId(rs.getLong("UserFreeTimeID"));
				 conflictedFreeTime.setDescription(rs.getString("Description"));
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
	
}
