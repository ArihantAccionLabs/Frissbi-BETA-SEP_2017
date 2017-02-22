package org.kleverlinks.webservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.kleverlinks.bean.FreeTimePostBean;
import org.kleverlinks.bean.UserFreeTimeBean;
import org.util.Utility;
import org.util.service.ServiceUtility;

@Path("PostFreeTimeService")
public class PostFreeTime {

	@POST
	@Path("/postTime")
	@Consumes(MediaType.APPLICATION_JSON)
	public String postFreeTime(String postFreeTime){
		
		System.out.println("postFreeTime  :    "+postFreeTime.toString());
		JSONObject postFreeTimeJson = new JSONObject(postFreeTime);
		JSONObject finalJson = new JSONObject();
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		FreeTimePostBean conflictedTimeBean = null;
		
		try {
			UserFreeTimeBean userFreeTimeBean = new UserFreeTimeBean(postFreeTimeJson);
			
			conflictedTimeBean = checkFreeTimeConfliction(userFreeTimeBean);
			if(conflictedTimeBean == null){
				
			String sql = "Insert into tbl_UserFreeTimes(UserID , FromDateTime , ToDateTime , Description) VALUES(?,?,?,?)";
			connection  =  DataSourceConnection.getDBConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, userFreeTimeBean.getUserId());
			preparedStatement.setTimestamp(2, new Timestamp(userFreeTimeBean.getFreeFromTime().getTime()));
			preparedStatement.setTimestamp(3, new Timestamp(userFreeTimeBean.getFreeToTime().getTime()));
			preparedStatement.setString(4, userFreeTimeBean.getDescription());
			
		 int isIserted = 	preparedStatement.executeUpdate();
			
		 if(isIserted != 0){
			 finalJson.put("status", true);
			 finalJson.put("message", "Free time posted successfully");
		     finalJson.put("isTimePosted", true);
		     
		     return finalJson.toString();
		 }
		}else{
			 finalJson.put("status", true);
			 finalJson.put("message", "Time is conflicting for "+ conflictedTimeBean.getDescription().toUpperCase()+" on "+conflictedTimeBean.getDate()+" from "+conflictedTimeBean.getStartTime()+" "+conflictedTimeBean.getEndTime());
		     finalJson.put("isTimePosted", false);
		     
		     return finalJson.toString();	
		}
		}catch (Exception e) {
          e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(connection);
			ServiceUtility.closeSatetment(preparedStatement);
		}
		 finalJson.put("status", false);
		 finalJson.put("message", "Oops something went wrong");
	     finalJson.put("isTimePosted", false);
		return "";
	}
	
	public FreeTimePostBean checkFreeTimeConfliction(UserFreeTimeBean userFreeTimeBean){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		FreeTimePostBean conflictedFreeTime = null;
		try {
			String sql = "SELECT UserID,FromDateTime,ToDateTime,Description FROM  tbl_UserFreeTimes WHERE userID = ? "
					+ "AND ((FromDateTime BETWEEN ? AND ?) OR (ToDateTime BETWEEN ? AND ?))";
			connection  =  DataSourceConnection.getDBConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, userFreeTimeBean.getUserId());
			preparedStatement.setTimestamp(2, new Timestamp(userFreeTimeBean.getFreeFromTime().getTime()));
			preparedStatement.setTimestamp(3, new Timestamp(userFreeTimeBean.getFreeToTime().getTime()));
			preparedStatement.setTimestamp(4, new Timestamp(userFreeTimeBean.getFreeFromTime().getTime()));
			preparedStatement.setTimestamp(5, new Timestamp(userFreeTimeBean.getFreeToTime().getTime()));
			
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
}
