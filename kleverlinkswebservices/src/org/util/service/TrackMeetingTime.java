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

import org.kleverlinks.bean.MeetingLogBean;
import org.kleverlinks.enums.MeetingStatus;
import org.kleverlinks.webservice.DataSourceConnection;
import org.util.Utility;

public class TrackMeetingTime {

	public static void getMeetingListBetweenTime(){//isReminderSent is the field ->0 no reminder sent , 1-> reminder sent
		
		List<MeetingLogBean> anyPlaceMeetingList = new ArrayList<MeetingLogBean>();//No address
		List<MeetingLogBean> myPlaceMeetingList = new ArrayList<MeetingLogBean>();//address is selected 
		List<MeetingLogBean> onlineMeetingList = new ArrayList<MeetingLogBean>();//address is selected 
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
		LocalDateTime fromTime = LocalDateTime.now().plusMinutes(3);
		LocalDateTime toTime = LocalDateTime.now().plusHours(2);
		LocalDateTime beforOneHour = LocalDateTime.now().plusHours(1);
		System.out.println("now === "+ formatter.format(fromTime)+"  toTime == "+ formatter.format(toTime));
		
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
				   
				   MeetingLogBean meetingLogBean = new MeetingLogBean();
				   meetingLogBean.setSenderUserId(rs.getLong("SenderUserID"));
				   meetingLogBean.setMeetingId(rs.getLong("MeetingID"));
				   meetingLogBean.setFullName(rs.getString("FirstName") + rs.getString("LastName"));
				   LocalDateTime fromTime1 = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				   LocalDateTime toTime1 =   ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				   meetingLogBean.setDate(fromTime1.toLocalDate());
				   meetingLogBean.setStartTime(ServiceUtility.updateTime(fromTime1.getHour(), fromTime1.getMinute()));
				   meetingLogBean.setEndTime(ServiceUtility.updateTime(toTime1.getHour(), toTime1.getMinute()));
				   meetingLogBean.setFromDate(fromTime1);
				   meetingLogBean.setDescription(rs.getString("MeetingDescription"));
				   meetingLogBean.setMeetingType(rs.getString("MeetingType"));
				   
				   if(Utility.checkValidString(rs.getString("GoogleAddress")) && meetingLogBean.getFromDate().isBefore(beforOneHour)){
					   
					   meetingLogBean.setLatitude(rs.getString("Latitude"));
					   meetingLogBean.setLongitude(rs.getString("Longitude"));
					   meetingLogBean.setAddress(rs.getString("GoogleAddress"));
					   
					   myPlaceMeetingList.add(meetingLogBean);
				   }else{
					   if(meetingLogBean.getMeetingType().equals("ONLINE") && meetingLogBean.getFromDate().isBefore(beforOneHour)){
						   onlineMeetingList.add(meetingLogBean);
					   }else{
						   anyPlaceMeetingList.add(meetingLogBean);
					   }
				   }
			
		   }
		   System.out.println("onlineMeetingList ====   "+onlineMeetingList.size()+"      anyPlaceMeetingList ========   "+anyPlaceMeetingList.size()+"  myPlaceMeetingList===  "+ myPlaceMeetingList.size());
		   if(! anyPlaceMeetingList.isEmpty()){
			   NotificationService.sendMeetingAlarmNotification(anyPlaceMeetingList);
		   }
		   if(! myPlaceMeetingList.isEmpty()){
			   NotificationService.sendMeetingAlarmNotification(myPlaceMeetingList);
		   }
		   if(! onlineMeetingList.isEmpty()){
			   NotificationService.sendMeetingAlarmNotification(onlineMeetingList);
		   }
		 //  sendMeetingNotificationBeforeOneHour();
		
	  } catch (Exception e) {
		e.printStackTrace();
	  }finally {
		   ServiceUtility.closeConnection(conn);
		   ServiceUtility.closeCallableSatetment(callableStatement);
	}
	}
	/*	public static void sendMeetingNotificationBeforeOneHour(){
			
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
						   meetingLogBean.setSenderUserId(rs.getLong("SenderUserID"));
						   meetingLogBean.setMeetingId(rs.getLong("MeetingID"));
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
		}*/
		
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
				List<Long> meetingIdList = new ArrayList<Long>();
				while(rs.next()){
					meetingIdList.add(rs.getLong("MeetingID"));
				}
				preparedStatement = null;
				String updateSql = "UPDATE tbl_MeetingDetails SET MeetingStatus=? WHERE MeetingID=?" ;
				if(! meetingIdList.isEmpty()){
					preparedStatement = conn.prepareStatement(updateSql);
					for (Long meetingId : meetingIdList) {
						preparedStatement.setString(1, MeetingStatus.COMPLETED.toString());
						preparedStatement.setLong(2, meetingId);
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
