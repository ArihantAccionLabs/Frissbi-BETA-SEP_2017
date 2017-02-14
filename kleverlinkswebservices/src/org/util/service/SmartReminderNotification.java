package org.util.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.kleverlinks.webservice.DataSourceConnection;

public class SmartReminderNotification {

	/*
	 * @Author -> Sunil Verma
	 * @Purpose -> First to check on current date is there any meeting if yes then send the smart reminder notification to every member of that meeting 
	 */
	
	public static void checkingMeetingOnCurrentDate(){
		

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
		LocalDateTime fromTime = LocalDateTime.now();
		LocalDateTime toTime = fromTime.plusHours(2);
	    System.out.println("fromTime=="+formatter.format(fromTime)+" toTime "+formatter.format(toTime));
		String sql = "SELECT * FROM tbl_MeetingDetails WHERE SenderFromDateTime BETWEEN ? AND ?";
		Connection conn = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			PreparedStatement pstmt = null;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, formatter.format(fromTime));
			pstmt.setString(2, formatter.format(toTime));
			
			ResultSet rs = pstmt.executeQuery();
			List<Long> recipientIdList = new ArrayList<>();
			 Long senderId = 0l;
			while(rs.next()){
			   Long meetingId = rs.getLong("MeetingID"); 
			   senderId    = rs.getLong("SenderUserID"); 
			   System.out.println(senderId+"  recipientId=================="+meetingId);
			   pstmt = null;
			   rs = null;
			   sql  = "SELECT * FROM tbl_RecipientsDetails WHERE MeetingID=?"; 
			   pstmt = conn.prepareStatement(sql);
			   pstmt.setLong(1, meetingId);
			   rs   = pstmt.executeQuery();
			   
			   while(rs.next()){
				   
				   Long recipientId = rs.getLong("UserID"); 
				   recipientIdList.add(recipientId);
				   System.out.println("recipientId=================="+recipientId);
				
			   }
			}
			
			NotificationService.sendSmartReminderNotification(senderId ,recipientIdList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
