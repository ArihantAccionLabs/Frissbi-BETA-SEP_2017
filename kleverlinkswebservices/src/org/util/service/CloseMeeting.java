package org.util.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.kleverlinks.webservice.DataSourceConnection;

public class CloseMeeting {
	
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
		 CallableStatement callableStatement = null;
		try {
			conn = DataSourceConnection.getDBConnection();//
            String updateMeetingStorProc = "{call usp_closeMeetings(?,?)}";
            callableStatement = conn.prepareCall(updateMeetingStorProc);
            callableStatement.setTimestamp(1, new Timestamp(yesterdayTime.getTime().getTime()));
            callableStatement.setTimestamp(2, new Timestamp(todayTime.getTime().getTime()));

			int isUpdated = callableStatement.executeUpdate();
			
		    System.out.println("isUpdated  "+isUpdated);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		
	}
}
