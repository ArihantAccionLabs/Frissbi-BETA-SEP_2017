package org.util.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.NotificationsEnum;
import org.service.dto.MeetingLogBean;
import org.service.dto.NotificationInfoDTO;

public class TrackMeetingTime {

	public static void getMeetingListBetweenTime(){
		
		List<MeetingLogBean> meetingList = new ArrayList<MeetingLogBean>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
		LocalDateTime fromTime = LocalDateTime.now().plusHours(2);
		LocalDateTime toTime = fromTime.plusMinutes(1);

		//System.out.println("fromTime===2 hour "+ formatter.format(fromTime)+"  toTime  "+formatter.format(toTime));
		
		 Connection conn = null;
		 CallableStatement callableStatement = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			String storeProc = "{call usp_GetMeetingList(?,?)}"; 
			callableStatement = conn.prepareCall(storeProc);
			callableStatement.setTimestamp(1 , Timestamp.valueOf(fromTime));
			callableStatement.setTimestamp(2 , Timestamp.valueOf(toTime));
			
			ResultSet rs = callableStatement.executeQuery();
		   while(rs.next()){
			   
			   if(rs.getString("Latitude") == null && rs.getString("Latitude").trim().isEmpty()){
				   
				   MeetingLogBean meetingLogBean = new MeetingLogBean();
				   meetingLogBean.setSenderUserId(rs.getInt("SenderUserID"));
				   meetingLogBean.setMeetingId(rs.getInt("MeetingID"));
				   meetingLogBean.setFullName(rs.getString("FirstName") + rs.getString("LastName"));
				   LocalDateTime fromTime1 = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				   LocalDateTime toTime1 =   ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				   meetingLogBean.setToDate(toTime);
				   meetingLogBean.setStartTime(ServiceUtility.updateTime(fromTime1.getHour(), fromTime1.getMinute()));
				   meetingLogBean.setEndTime(ServiceUtility.updateTime(toTime1.getHour(), toTime1.getMinute()));
				   meetingLogBean.setDescription(rs.getString("MeetingDescription"));
				   meetingLogBean.setLatitude(rs.getString("Latitude"));
				   meetingLogBean.setLongitude(rs.getString("Longitude"));
				   meetingLogBean.setAddress(rs.getString("GoogleAddress"));
				   
				   meetingList.add(meetingLogBean);
			  }
		   }
			
		   ServiceUtility.closeConnection(conn);
		   ServiceUtility.closeCallableSatetment(callableStatement);
		   rs.close();
		   
		  // System.out.println("meetingList size========="+meetingList.size());
		   
		   if(! meetingList.isEmpty()){
			    conn = DataSourceConnection.getDBConnection();
				String receiverStorProc = "{call usp_GetRecipientDetails_ByMeetingID(?)}"; 
				callableStatement = conn.prepareCall(receiverStorProc);
				
				for (MeetingLogBean meetingLogBean : meetingList) {
					
					callableStatement.setInt(1, meetingLogBean.getMeetingId());
					
					rs = callableStatement.executeQuery();
					List<Integer> userList = new ArrayList<>();
					
					while(rs.next()){
						if(rs.getInt("Status") == 1 || rs.getInt("Status") == 0){
							userList.add(rs.getInt("UserID"));
						}
					}
					if(! userList.isEmpty()){
						userList.add(meetingLogBean.getSenderUserId());
						NotificationService.sendNotificationToInformAddress(meetingLogBean, userList);
					}
				}
		   } 
		   
		  // sendMeetingNotificationBeforeOneHour();
		 
	  } catch (Exception e) {
		e.printStackTrace();
	  }
	}
		public static void sendMeetingNotificationBeforeOneHour(){
			
			List<MeetingLogBean> addressMeetingList = new ArrayList<MeetingLogBean>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
			LocalDateTime fromTime = LocalDateTime.now().plusHours(1);
			LocalDateTime toTime = fromTime.plusMinutes(30);
			System.out.println("fromTime 1 hour ==="+ formatter.format(fromTime)+"  toTime  "+formatter.format(toTime));
			 Connection conn = null;
			 CallableStatement callableStatement = null;
			try {
				conn = DataSourceConnection.getDBConnection();
				String storeProc = "{call usp_GetMeetingList(?,?)}"; 
				callableStatement = conn.prepareCall(storeProc);
				callableStatement.setTimestamp(1 , Timestamp.valueOf(fromTime));
				callableStatement.setTimestamp(2 , Timestamp.valueOf(toTime));
				
				ResultSet rs = callableStatement.executeQuery();
				   while(rs.next()){
					   
					   if(rs.getString("Latitude") != null && ! rs.getString("Latitude").trim().isEmpty()){
							   
						   MeetingLogBean meetingLogBean = new MeetingLogBean();
						   meetingLogBean.setSenderUserId(rs.getInt("SenderUserID"));
						   meetingLogBean.setMeetingId(rs.getInt("MeetingID"));
						   meetingLogBean.setFullName(rs.getString("FirstName") + rs.getString("LastName"));
						   LocalDateTime fromTime1 = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
						   LocalDateTime toTime1 =   ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
						   meetingLogBean.setDate(fromTime1.toLocalDate());
						   meetingLogBean.setStartTime(ServiceUtility.updateTime(fromTime1.getHour(), fromTime1.getMinute()));
						   meetingLogBean.setEndTime(ServiceUtility.updateTime(toTime1.getHour(), toTime1.getMinute()));
						   meetingLogBean.setDescription(rs.getString("MeetingDescription"));
						   meetingLogBean.setLatitude(rs.getString("Latitude"));
						   meetingLogBean.setLongitude(rs.getString("Longitude"));
						   meetingLogBean.setAddress(rs.getString("GoogleAddress"));
						   
						   addressMeetingList.add(meetingLogBean);
			      }
		   }
				   if(! addressMeetingList.isEmpty()){
					  
					   NotificationService.sendMeetingAlarmNotification(addressMeetingList);
				   }
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				ServiceUtility.closeConnection(conn);
				ServiceUtility.closeCallableSatetment(callableStatement);
			}
		}
}
