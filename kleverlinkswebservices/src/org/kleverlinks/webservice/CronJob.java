package org.kleverlinks.webservice;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class CronJob implements org.quartz.Job {

	@SuppressWarnings("resource")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("hello cron executing==========at " + new Date());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd hh:mm:ss a");
		LocalDateTime fromTime = LocalDateTime.now();
		LocalDateTime toTime = fromTime.plusHours(2);
	
		String sql = "SELECT * FROM tbl_MeetingDetails where SenderFromDateTime BETWEEN '" + formatter.format(fromTime) + "' AND '"+formatter.format(toTime)+"'";
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
			   int meetingId = rs.getInt("MeetingID"); 
			   int senderId    = rs.getInt("SenderUserID"); 
			   System.out.println(senderId+"  while outer======================================"+meetingId);
			   
			   rs   = null;
			   stmt = null;
			   sql  = "SELECT * FROM tbl_RecipientsDetails WHERE MeetingID="+meetingId; 
			   stmt = conn.createStatement();
			   rs   = stmt.executeQuery(sql);
			   
			   while(rs.next()){
				   
				   int recipientId = rs.getInt("UserID"); 
				   System.out.println("  while inner ======================================"+recipientId);
				   UserNotifications userNotifications = new UserNotifications();
				   Date date = new Date();
				   Timestamp timestamp = new Timestamp(date.getTime());
				   String notificationId = userNotifications.insertUserNotifications(recipientId, senderId, NotificationsEnum.Meeting_Pending_Requests.ordinal()+1, 0, timestamp);
				   JSONObject json =new JSONArray( userNotifications.getUserNotifications(0, Integer.parseInt(notificationId))).getJSONObject(0);
				   String notificationMessage = json.getString("NotificationMessage");
				   String NotificationName = json.getString("NotificationName");
				   Sender sender = new Sender(Constants.GCM_APIKEY);
				   Message message = new Message.Builder()
						   .timeToLive(3)
						   .delayWhileIdle(true)
						   .dryRun(true)
						   .addData("message",notificationMessage )
						   .addData("NotificationName",NotificationName )
						   .addData("meetingId",meetingId+"" )
						   .build();
				   
				   try {
					   AuthenticateUser authenticateUser = new AuthenticateUser();
					   JSONObject jsonRegistrationId = new JSONObject ( authenticateUser.getGCMDeviceRegistrationId(recipientId));
					   String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
					   Result result = sender.send(message, deviceRegistrationId, 1);
					   System.out.println(result);
				   } catch (IOException e) {
					   // TODO Auto-generated catch block
					   e.printStackTrace();
				   }
			   }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
