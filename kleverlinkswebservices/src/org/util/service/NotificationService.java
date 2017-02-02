package org.util.service;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kleverlinks.webservice.AuthenticateUser;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.LocationDetails;
import org.kleverlinks.webservice.MeetingDetails;
import org.kleverlinks.webservice.MeetingStatus;
import org.kleverlinks.webservice.NotificationsEnum;
import org.kleverlinks.webservice.UserNotifications;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.MeetingLogBean;
import org.service.dto.NotificationInfoDTO;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class NotificationService {

	
	/*
	 * @Purpose -> Getting list of receptionist id , sender id  and sending smart reminder notification before 2 hours of the meeting
	 * 
	 */
	
	public static void sendSmartReminderNotification(int senderId , List<Integer> receptionIdList){
	
		for (Integer receptionId : receptionIdList) {
	
		UserNotifications userNotifications = new UserNotifications();
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		String notificationId = userNotifications.insertUserNotifications(receptionId, receptionId,
				NotificationsEnum.FRIEND_REQUEST_ACCEPTANCE.ordinal() + 1, 0, timestamp);
		JSONArray jsonArray = new JSONArray(
				userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)));
		if (jsonArray.length() > 0) {

			JSONObject json = jsonArray.getJSONObject(0);

			String notificationMessage = json.getString("NotificationMessage");
			String NotificationName = json.getString("NotificationName");
			Sender sender = new Sender(Constants.GCM_APIKEY);
			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
					.addData("message", notificationMessage).addData("NotificationName", NotificationName)
					.build();

			try {
				AuthenticateUser authenticateUser = new AuthenticateUser();
				JSONObject jsonObject = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(receptionId));
				String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
				Result result = sender.send(message, deviceRegistrationId, 1);

				System.out.println(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		}
	}
	
	/*
	 * @Purpose -> sending meeting creation suggestion notification among fiends who posted their free time on same date and their free time slot is matching minimum 1 hour 
	 * 
	 */
	
	public static void sendMeetingSuggestionNotification(int userId1 , String meassage){
		System.out.println("meassage===="+meassage);
		UserNotifications userNotifications = new UserNotifications();
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		String notificationId = userNotifications.insertUserNotifications(userId1, 0,
				NotificationsEnum.MEETING_REQUEST_ACCEPTANCE.ordinal() + 1, 0, timestamp);
		JSONArray jsonArray = new JSONArray(
				userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)));
		if (jsonArray.length() > 0) {

			JSONObject json = jsonArray.getJSONObject(0);

			//String notificationMessage = json.getString("NotificationMessage");
			String NotificationName = json.getString("NotificationName");
			Sender sender = new Sender(Constants.GCM_APIKEY);
			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
					.addData("message", meassage).addData("NotificationName", NotificationName)
					.build();

			try {
				AuthenticateUser authenticateUser = new AuthenticateUser();
				JSONObject jsonObject = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId1));
				String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
				Result result = sender.send(message, deviceRegistrationId, 1);
				System.out.println(result);
				System.out.println("Notification sent successfully");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	// Generic type method to send notification
	public static void sendNotification(List<Integer> userIds, int senderUserId, int notificationType, int meetingId) {

		for (Integer recipientId : userIds) {
			if (!(senderUserId == recipientId)) {

				UserNotifications userNotifications = new UserNotifications();
				Timestamp timestamp = new Timestamp(new Date().getTime());
				String notificationId = userNotifications.insertUserNotifications(recipientId, senderUserId,notificationType, 0, timestamp);
				JSONObject json = new JSONArray(userNotifications.getUserNotifications(0, Integer.parseInt(notificationId))).getJSONObject(0);
				if(json != null){
					
					String notificationMessage = json.getString("NotificationMessage");
			
				String NotificationName = json.getString("NotificationName");
				Sender sender = new Sender(Constants.GCM_APIKEY);
				Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
						.addData("message", notificationMessage).addData("NotificationName", NotificationName)
						.addData("meetingId", meetingId + "").build();

				try {
					AuthenticateUser authenticateUser = new AuthenticateUser();
					JSONObject jsonRegistrationId = new JSONObject(
							authenticateUser.getGCMDeviceRegistrationId(recipientId));
					if(jsonRegistrationId.has("DeviceRegistrationID")){
						
						String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);
						System.out.println(result);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				}
			}
		}
	}

	/*UserDTO userDTO = getUserDetailsByUserId(recipientId, meetingId);
			if (userDTO != null) {
				notificationMessage = "Dear " + userDTO.getFullName() + "you have cancelled the Meeting type : "
						+ userDTO.getDescription() + " on date " + userDTO.getMeetingFromTime().toLocalDate();
			}*/
	// Generic type method to send notification
	public static void sendNotificationToOneUser(int recipientId, int senderUserId, int notificationType,int meetingId) {
       try{
		UserNotifications userNotifications = new UserNotifications();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		String notificationId = userNotifications.insertUserNotifications(recipientId, senderUserId, notificationType,0, timestamp);

		JSONArray jsonArray = new JSONArray(
				userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)));
		if (jsonArray != null && jsonArray.length() > 0) {

			JSONObject json = jsonArray.getJSONObject(0);
			String notificationMessage = json.getString("NotificationMessage");
			String NotificationName = json.getString("NotificationName");
			Sender sender = new Sender(Constants.GCM_APIKEY);

			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
					.addData("message", notificationMessage).addData("NotificationName", NotificationName).build();
			
				AuthenticateUser authenticateUser = new AuthenticateUser();
				JSONObject jsonRegistrationId = new JSONObject(
						authenticateUser.getGCMDeviceRegistrationId(recipientId));
				String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
				if(deviceRegistrationId != null){
					Result result = sender.send(message, deviceRegistrationId, 1);
					System.out.println(result);
				}
			} 
             }catch (IOException e) {
				e.printStackTrace();
			}
	}
	

	public static void sendingMeetingCreationNotification(JSONObject meetingInsertionObject, int meetingId) {

		JSONArray friendsArray = meetingInsertionObject.getJSONArray("friendsIdJsonArray");
		List<Integer> userIds = new ArrayList<Integer>();
		for (int i = 0; i < friendsArray.length(); i++) {
			userIds.add(friendsArray.getInt(i));
		}
		// sending the meeting request to all memeber
		NotificationService.sendNotification(userIds, meetingInsertionObject.getInt("senderUserId"),NotificationsEnum.MEETING_PENDING_REQUESTS.ordinal() + 1, meetingId);
		LocationDetails locationDetails = new LocationDetails();
		try {
			// Doing reverse geocoding
			String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
					+ meetingInsertionObject.getString("latitude") + "," + meetingInsertionObject.getString("longitude")
					+ "&key=" + Constants.GCM_APIKEY;
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			WebResource service = client.resource(url);
			JSONObject json = new JSONObject(MeetingDetails.getOutputAsString(service));
			JSONArray results = (JSONArray) json.get("results");
			JSONObject resultsObject = (JSONObject) results.get(0);
			String formattedAddress = (String) resultsObject.get("formatted_address");
			locationDetails.insertMeetingLocationDetails(meetingInsertionObject.getString("latitude"),
					meetingInsertionObject.getString("longitude"), formattedAddress, meetingId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendNotification(JSONArray meetingArray, int senderUserId, int notificationType) {
		
    try{
    	
    	List<Integer> meetingIds = new ArrayList<>();
    	
    	  if(meetingArray.length() == 1){
    	  
    		  meetingIds.add(meetingArray.getJSONObject(0).getInt("meetingId"));
    	  } else {
    		
    		for (int i = 0; i < meetingArray.length(); i++) {
				
    			meetingIds.add(meetingArray.getInt(i));
			}  
    	  }
    	for (Integer meetingId : meetingIds) {
    		System.out.println("meetingIdssize===="+meetingIds.size()+"  "+(ServiceUtility.isMeetingCreatorRemoved(meetingId, senderUserId)));
		
		 String message = "Meeting was cancelled by ";
	     if(ServiceUtility.isMeetingCreatorRemoved(meetingId, senderUserId)){
	    	    Set<Integer> userList = new HashSet<>(); 
	    		MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId( meetingId);
	    		JSONArray friendsArray        =  ServiceUtility.getReceiverDetailsByMeetingId(meetingId ,senderUserId).getJSONArray("friendsArray");
	    	 String fullName = "";
	    	 
	    	   userList.add(meetingLogBean.getSenderUserId());
	    	   
	    	   if(meetingLogBean.getSenderUserId() == senderUserId){
	    		   fullName = meetingLogBean.getFullName(); 
	    	   }else{
				   for (int i = 0; i < friendsArray.length(); i++) {
					
					   if(friendsArray.getJSONObject(i).getInt("userId") == senderUserId){
						   fullName = meetingLogBean.getFullName();   
					   }
					   userList.add(friendsArray.getJSONObject(i).getInt("userId"));
				   }
	    		   
	    	   }
	    	   message +=  fullName+" which was on "+meetingLogBean.getDate()+" from "+meetingLogBean.getStartTime()+" to "+meetingLogBean.getEndTime();	
	    	  
	    	   NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
	    	   
	    	   notificationInfoDTO.setMessage(message);
	    	   notificationInfoDTO.setUserList(userList.stream().collect(Collectors.toList()));
	    	   notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_REJECTED.toString());
	    	   notificationInfoDTO.setMeetingId(meetingId);
	    	   System.out.println("userList SIZE============="+ notificationInfoDTO.getUserList().size());
	    	   sendMeetingCancelledNotification(notificationInfoDTO);
	     }
	   }
     }catch (Exception e) {
		e.printStackTrace();
	 }
		}
		
	public static void sendPendingMeetingRequest(NotificationInfoDTO notificationInfoDTO){
		
		Connection conn = null;
		CallableStatement callableStatement = null;

		try {
			conn = DataSourceConnection.getDBConnection();
			String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertNotificationStoreProc);
			
			System.out.println("UserListsize======================="+notificationInfoDTO.getUserList().size());
		 for(Integer userId : notificationInfoDTO.getUserList()){
			
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, notificationInfoDTO.getSenderUserId());
			callableStatement.setInt(3, notificationInfoDTO.getMeetingId());
			callableStatement.setString(4, notificationInfoDTO.getNotificationType());
			callableStatement.setString(5, notificationInfoDTO.getMessage());
			callableStatement.setTimestamp(6, new Timestamp(new Date().getTime()));
		
			int value = callableStatement.executeUpdate();
			 Sender sender = new Sender(Constants.GCM_APIKEY);
    		 Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true).addData("message", notificationInfoDTO.getMessage()).addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
    		 
    		 AuthenticateUser authenticateUser = new AuthenticateUser();
    		 JSONObject jsonRegistrationId = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId));
    		 if(jsonRegistrationId.has("DeviceRegistrationID")){
    			 
    			 String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
    			 Result result = sender.send(message, deviceRegistrationId, 1);
    			 System.out.println(result);
    		 }else{
    			System.out.println("DeviceRegistrationID does not exist "); 
    		 }
		}
	} catch(Exception e){
		e.printStackTrace();
	}
	}	
	
	
	
		public static void sendMeetingCancelledNotification(NotificationInfoDTO notificationInfoDTO){
			
			Connection conn = null;
			CallableStatement callableStatement = null;

			try {
				conn = DataSourceConnection.getDBConnection();
				String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?,?,?)}";
				callableStatement = conn.prepareCall(insertNotificationStoreProc);
				
				System.out.println("UserListsize======================="+notificationInfoDTO.getUserList().size());
			 for(Integer userId : notificationInfoDTO.getUserList()){
				
				callableStatement.setInt(1, userId);
				callableStatement.setInt(2, notificationInfoDTO.getSenderUserId());
				callableStatement.setInt(3, notificationInfoDTO.getMeetingId());
				callableStatement.setString(4, notificationInfoDTO.getNotificationType());
				callableStatement.setString(5, notificationInfoDTO.getMessage());
				callableStatement.setTimestamp(6, new Timestamp(new Date().getTime()));
			
				int value = callableStatement.executeUpdate();
				 Sender sender = new Sender(Constants.GCM_APIKEY);
	    		 Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true).addData("message", notificationInfoDTO.getMessage()).addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
	    		 
	    		 AuthenticateUser authenticateUser = new AuthenticateUser();
	    		 JSONObject jsonRegistrationId = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId));
	    		 if(jsonRegistrationId.has("DeviceRegistrationID")){
	    			 
	    			 String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
	    			 Result result = sender.send(message, deviceRegistrationId, 1);
	    			 System.out.println(result);
	    		 }else{
	    			System.out.println("DeviceRegistrationID does not exist "); 
	    		 }
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		}	
			public static void sendMeetingAlarmNotification(NotificationInfoDTO notificationInfoDTO , List<Integer> userList){
				
				Connection conn = null;
				CallableStatement callableStatement = null;

				try {
					conn = DataSourceConnection.getDBConnection();
					String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?)}";
					for (Integer userId : userList) {
						callableStatement = conn.prepareCall(insertNotificationStoreProc);
						callableStatement.setInt(1,userId);
						callableStatement.setInt(2, notificationInfoDTO.getSenderUserId());
						callableStatement.setInt(3, notificationInfoDTO.getMeetingId());
						callableStatement.setString(4, notificationInfoDTO.getNotificationType());
						callableStatement.setString(5, notificationInfoDTO.getMessage());
						callableStatement.setTimestamp(6, new Timestamp(new Date().getTime()));
						
						callableStatement.addBatch();
						
					/*	int value = callableStatement.executeUpdate();
						int isError = callableStatement.getInt(7);
						int notificationId = callableStatement.getInt(8);*/
					}
					int[] updateCounts = callableStatement.executeBatch();
					 if(updateCounts.length != 0){
						 
				      for (Integer userID : userList) {
				    	
							Sender sender = new Sender(Constants.GCM_APIKEY);
						    String notificationMessage = notificationInfoDTO.getMessage();
							String NotificationName = notificationInfoDTO.getNotificationType();
								Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
									.addData("message", notificationMessage).addData("NotificationName", NotificationName)
									.addData("meetingId", notificationInfoDTO.getMeetingId() + "").addData("jsonData", notificationInfoDTO.getJsonObject().toString()).build();
								AuthenticateUser authenticateUser = new AuthenticateUser();
								JSONObject jsonRegistrationId = new JSONObject(
										authenticateUser.getGCMDeviceRegistrationId(userID));
								if(jsonRegistrationId.has("DeviceRegistrationID")){
									String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
									Result result = sender.send(message, deviceRegistrationId, 1);
									System.out.println(result);
								}
					   }
					}
			} catch(Exception e){
				e.printStackTrace();
			}finally{
				ServiceUtility.closeConnection(conn);
				ServiceUtility.closeCallableSatetment(callableStatement);
			}
			
		}
		
}
