package org.util.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.kleverlinks.enums.MeetingStatus;
import org.kleverlinks.webservice.DataSourceConnection;
import org.service.dto.MeetingLogBean;

public class TrackMeetingTime {

	public static void getMeetingListBetweenTime(){
		
		List<MeetingLogBean> meetingList = new ArrayList<MeetingLogBean>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
		LocalDateTime fromTime = LocalDateTime.now().plusHours(2);
		System.out.println("fromTime===2 hour "+ formatter.format(fromTime));
		
		 Connection conn = null;
		 CallableStatement callableStatement = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			String storeProc = "{call usp_GetMeetingList(?,?)}"; 
			callableStatement = conn.prepareCall(storeProc);
			callableStatement.setTimestamp(1 , Timestamp.valueOf(fromTime));
			callableStatement.setTimestamp(2 , Timestamp.valueOf(fromTime));
			
			ResultSet rs = callableStatement.executeQuery();
		   while(rs.next()){
			   System.out.println("Latitude<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+rs.getString("Latitude"));
			   if(rs.getString("Latitude") == null || rs.getString("Latitude") == ""){
				   
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
				   
				   meetingList.add(meetingLogBean);
			  }
		   }
		   if(! meetingList.isEmpty()){
				  
			   NotificationService.sendMeetingAlarmNotification(meetingList);
		   }
		   sendMeetingNotificationBeforeOneHour();
		
	  } catch (Exception e) {
		e.printStackTrace();
	  }finally {
		   ServiceUtility.closeConnection(conn);
		   ServiceUtility.closeCallableSatetment(callableStatement);
	}
	}
		public static void sendMeetingNotificationBeforeOneHour(){
			
			List<MeetingLogBean> addressMeetingList = new ArrayList<MeetingLogBean>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
			LocalDateTime fromTime = LocalDateTime.now().plusHours(1);
			System.out.println("fromTime 1 hour ==="+ formatter.format(fromTime));
			 Connection conn = null;
			 CallableStatement callableStatement = null;
			try {
				conn = DataSourceConnection.getDBConnection();
				String storeProc = "{call usp_GetMeetingList(?,?)}"; 
				callableStatement = conn.prepareCall(storeProc);
				callableStatement.setTimestamp(1 , Timestamp.valueOf(fromTime));
				callableStatement.setTimestamp(2 , Timestamp.valueOf(fromTime));
				
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
				   System.out.println("addressMeetingList==============="+addressMeetingList.size());
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
		
		@SuppressWarnings("resource")
		public static void completingMeetingAfterTimeOut(){
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Calendar yesterdayTime = Calendar.getInstance();
			yesterdayTime.set(Calendar.HOUR, 0);
			yesterdayTime.set(Calendar.MINUTE, 0);
			yesterdayTime.set(Calendar.SECOND, 0);
			yesterdayTime.set(Calendar.HOUR_OF_DAY, 0);
			yesterdayTime.add(Calendar.DATE, -1);
			
			Calendar todayTime = Calendar.getInstance();
			todayTime.setTime(yesterdayTime.getTime());
			todayTime.set(Calendar.HOUR, 23);
			todayTime.set(Calendar.MINUTE, 59);
			todayTime.set(Calendar.SECOND, 00);
			
			System.out.println("yesterDay : "+sdf.format(yesterdayTime.getTime())+" todayTime : "+sdf.format(todayTime.getTime()));
			
			 Connection conn = null;
			 PreparedStatement preparedStatement = null;
			try {
				conn = DataSourceConnection.getDBConnection();
                String selectSql = "SELECT MeetingID FROM tbl_MeetingDetails  WHERE MeetingStatus!=? AND SenderToDateTime BETWEEN ? AND ?" ;
                preparedStatement = conn.prepareStatement(selectSql);
                preparedStatement.setString(1, MeetingStatus.CANCELLED.toString());
                preparedStatement.setTimestamp(2, new Timestamp(yesterdayTime.getTime().getTime()));
                preparedStatement.setTimestamp(3, new Timestamp(todayTime.getTime().getTime()));

				ResultSet rs = preparedStatement.executeQuery();
				List<Integer> meetingIdList = new ArrayList<Integer>();
				while(rs.next()){
					meetingIdList.add(rs.getInt("MeetingID"));
				}
				preparedStatement = null;
				String updateSql = "UPDATE tbl_MeetingDetails SET MeetingStatus=? WHERE MeetingID=?" ;
				if(! meetingIdList.isEmpty()){
					preparedStatement = conn.prepareStatement(updateSql);
					for (Integer meetingId : meetingIdList) {
						preparedStatement.setString(1, MeetingStatus.COMPLETED.toString());
						preparedStatement.setInt(2, meetingId);
						preparedStatement.addBatch();
					}
					   int []updateRow =  preparedStatement.executeBatch();
					System.out.println("updateRow================"+updateRow.length);    
				}
			    System.out.println();
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				ServiceUtility.closeConnection(conn);
				ServiceUtility.closeSatetment(preparedStatement);
			}
			
		}
}
