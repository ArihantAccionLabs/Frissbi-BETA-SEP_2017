package org.util.service;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.AuthenticateUser;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.NotificationsEnum;
import org.kleverlinks.webservice.UserNotifications;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.MeetingLogBean;
import org.service.dto.NotificationInfoDTO;
import org.service.dto.UserDTO;

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
	
	public static void sendMeetingAcceptRejectNotification(NotificationInfoDTO notificationInfoDTO) {

		MeetingLogBean meetingLogBean = notificationInfoDTO.getMeetingLogBean();

		List<Integer> userList = new ArrayList<Integer>();
		String message = "";
		
		if (meetingLogBean.getMeetingId() != null) {
			
			if (notificationInfoDTO.getNotificationType().equals(NotificationsEnum.MEETING_REQUEST_ACCEPTANCE.toString())) {
				
				UserDTO userDTO = ServiceUtility.getUserDetailsByMeetingIdAndUserId(notificationInfoDTO.getMeetingId(),notificationInfoDTO.getSenderUserId());
				
				if (userDTO.getFullName() != null && !userDTO.getFullName().trim().isEmpty()) {
					
					message = "Your meeting request " + meetingLogBean.getDescription() + " has been accepted by "
							+ userDTO.getFullName().toUpperCase() + " which is on date " + meetingLogBean.getDate()
							+ " from " + meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime();

					userList.add(meetingLogBean.getSenderUserId());
				}
				} else {
					if (meetingLogBean.getSenderUserId().equals(notificationInfoDTO.getSenderUserId())) {

						JSONObject jsonObject = ServiceUtility.getReceiverDetailsByMeetingId(notificationInfoDTO.getMeetingId(), notificationInfoDTO.getSenderUserId());

						JSONArray userIdsArray = jsonObject.getJSONArray("friendsArray");
						System.out.println((userIdsArray.length() != 0)+" klength========="+userIdsArray.length()+"   "+userIdsArray.getJSONObject(0).getInt("userId"));
						if (userIdsArray.length() != 0) {
							for (int i = 0; i < userIdsArray.length(); i++) {
								userList.add(userIdsArray.getJSONObject(i).getInt("userId"));
							}
							message = "Meeting " + meetingLogBean.getDescription() + " is cancelled by "
									+ meetingLogBean.getFullName().toUpperCase() + " which is on on date "
									+ meetingLogBean.getDate() + " from " + meetingLogBean.getStartTime() + " to "
									+ meetingLogBean.getEndTime();
						}
					} else {
						UserDTO userDTO = ServiceUtility.getUserDetailsByMeetingIdAndUserId(notificationInfoDTO.getMeetingId(),notificationInfoDTO.getSenderUserId());
						message = "Your meeting request " + meetingLogBean.getDescription() + " has been rejected by "
								+ userDTO.getFullName().toUpperCase() + " which is on on date "
								+ meetingLogBean.getDate() + " from " + meetingLogBean.getStartTime() + " to "
								+ meetingLogBean.getEndTime();

						userList.add(meetingLogBean.getSenderUserId());
						notificationInfoDTO.setUserList(userList);
					}
				}
				notificationInfoDTO.setMessage(message);
				notificationInfoDTO.setUserList(userList);
				notificationInfoDTO.setUserId(meetingLogBean.getSenderUserId());

				sendMeetingNotification(notificationInfoDTO);
			}
	}

	public static void sendingMeetingCreationNotification(JSONObject meetingInsertionObject, int meetingId) {

		JSONArray friendsArray = meetingInsertionObject.getJSONArray("friendsIdJsonArray");
		List<Integer> userIds = new ArrayList<Integer>();
		for (int i = 0; i < friendsArray.length(); i++) {
			userIds.add(friendsArray.getInt(i));
		}

		NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();

		MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
		if (meetingLogBean != null) {

			String message = "You have a meeting " + meetingLogBean.getDescription() + " hosting "
					+ meetingLogBean.getFullName() + " on " + meetingLogBean.getDate() + " from "
					+ meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime();

			notificationInfoDTO.setMessage(message);
			notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_PENDING_REQUESTS.toString());
			notificationInfoDTO.setMeetingId(meetingId);
			notificationInfoDTO.setSenderUserId(meetingInsertionObject.getInt("senderUserId"));
			notificationInfoDTO.setUserList(userIds);

			NotificationService.sendMeetingNotification(notificationInfoDTO);
		}
	}

	public static void sendNotification(JSONArray meetingArray, int senderUserId, int notificationType) {

		try {
			List<Integer> meetingIds = new ArrayList<>();

			if (meetingArray.length() > 1) {
				for (int i = 0; i < meetingArray.length(); i++) {
					meetingIds.add(meetingArray.getInt(i));
				}
			} else {
				meetingIds.add(meetingArray.getJSONObject(0).getInt("meetingId"));
			}
			for (Integer meetingId : meetingIds) {
				System.out.println("meetingIdssize====" + meetingIds.size() + "  "
						+ (ServiceUtility.isMeetingCreatorRemoved(meetingId, senderUserId)));

				String message = "Meeting was cancelled by ";
				if (ServiceUtility.isMeetingCreatorRemoved(meetingId, senderUserId)) {
					Set<Integer> userList = new HashSet<Integer>();
					MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
					JSONArray friendsArray = ServiceUtility.getReceiverDetailsByMeetingId(meetingId, senderUserId)
							.getJSONArray("friendsArray");
					String fullName = "";

					if (meetingLogBean.getSenderUserId() != null) {
						userList.add(meetingLogBean.getSenderUserId());
					}

					if (meetingLogBean.getSenderUserId() != null && meetingLogBean.getSenderUserId() == senderUserId) {
						fullName = meetingLogBean.getFullName();
					} else {
						for (int i = 0; i < friendsArray.length(); i++) {

							Integer userId = friendsArray.getJSONObject(i).getInt("userId");
							if (userId != null && userId != 0) {
								if (friendsArray.getJSONObject(i).getInt("userId") == senderUserId) {
									fullName = meetingLogBean.getFullName();
								}
								userList.add(friendsArray.getJSONObject(i).getInt("userId"));
							}
						}
					}
					if (!userList.isEmpty()) {
						message += fullName + " which was on " + meetingLogBean.getDate() + " from "
								+ meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime();
						NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
						notificationInfoDTO.setMessage(message);
						notificationInfoDTO.setUserList(userList.stream().collect(Collectors.toList()));
						notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_REJECTED.toString());
						notificationInfoDTO.setMeetingId(meetingId);
						System.out.println("userList SIZE=============" + notificationInfoDTO.getUserList().size());
						sendMeetingNotification(notificationInfoDTO);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void sendPendingMeetingRequest(NotificationInfoDTO notificationInfoDTO){
		
		Connection conn = null;
		CallableStatement callableStatement = null;

		try {
			conn = DataSourceConnection.getDBConnection();
			String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?)}";
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
	
	public static void sendMeetingNotification(NotificationInfoDTO notificationInfoDTO) {

		try {
			int value = insertNotification(notificationInfoDTO);
			System.out.println("value==============="+value);
			if (value != 0) {

				Message message = null;
				Sender sender = new Sender(Constants.GCM_APIKEY);
				for (Integer userId : notificationInfoDTO.getUserList()) {

					if (notificationInfoDTO.getJsonObject() != null) {
						message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
								.addData("meetingId", notificationInfoDTO.getMeetingId() + "")
								.addData("message", notificationInfoDTO.getMessage())
								.addData("NotificationName", notificationInfoDTO.getNotificationType())
								.addData("locationSuggestionJson", notificationInfoDTO.getJsonObject().toString())
								.build();
					} else {
						message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
								.addData("meetingId", notificationInfoDTO.getMeetingId() + "")
								.addData("message", notificationInfoDTO.getMessage())
								.addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
					}

					AuthenticateUser authenticateUser = new AuthenticateUser();
					JSONObject jsonRegistrationId = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userId));
					System.out.println("DeviceRegistrationID============" + (jsonRegistrationId.has("DeviceRegistrationID")));
					if (jsonRegistrationId.has("DeviceRegistrationID")) {

						String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);
						System.out.println(result);
					} else {
						System.out.println("DeviceRegistrationID does not exist ");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			public static void sendMeetingAlarmNotification(List<MeetingLogBean> addressMeetingList){
				
				for (MeetingLogBean meetingCreatorLogBean : addressMeetingList) {
				
				JSONArray  jsonArray = ServiceUtility.getReceptionistDetailsByMeetingId(meetingCreatorLogBean.getMeetingId()) ;
				Set<Integer> userIdSet = new HashSet<>();
				
				String fullName = "";
				for (int i = 0; i < jsonArray.length(); i++) {
					if(jsonArray.getJSONObject(i).getInt("userId") != 0){
						userIdSet.add(jsonArray.getJSONObject(i).getInt("userId"));
						fullName = jsonArray.getJSONObject(i).getString("fullName");
					}
				}
				userIdSet.add(meetingCreatorLogBean.getSenderUserId());
				if(meetingCreatorLogBean.getSenderUserId() != null){
					userIdSet.add(meetingCreatorLogBean.getSenderUserId());
				}
				
				NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				if(!userIdSet.isEmpty()){
					
					notificationInfoDTO.setMeetingId(meetingCreatorLogBean.getMeetingId());
					notificationInfoDTO.setSenderUserId(0);
					notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_SUMMARY.toString());
					notificationInfoDTO.setMeetingLogBean(meetingCreatorLogBean);
					notificationInfoDTO.setUserList(userIdSet.stream().collect(Collectors.toList()));
					JSONObject finalJson = new JSONObject();
					
					finalJson.put("meetingId", meetingCreatorLogBean.getMeetingId());
					finalJson.put("date", meetingCreatorLogBean.getDate());
					finalJson.put("from", meetingCreatorLogBean.getStartTime());
					finalJson.put("to", meetingCreatorLogBean.getEndTime());
					finalJson.put("description", meetingCreatorLogBean.getDescription());
					if(meetingCreatorLogBean.getLatitude() != null && meetingCreatorLogBean.getLatitude().trim().isEmpty()){
						
						finalJson.put("address", meetingCreatorLogBean.getAddress());
						finalJson.put("latitude", meetingCreatorLogBean.getLatitude());
						finalJson.put("longitude", meetingCreatorLogBean.getLongitude());
						finalJson.put("isLocationSelected", true);
					}else{
						finalJson.put("isLocationSelected", false);
					}
					
					finalJson.put("friendsJsonArray", ServiceUtility.getReceptionistDetailsByMeetingId((meetingCreatorLogBean.getMeetingId())));
					
					notificationInfoDTO.setJsonObject(finalJson);

				try {
					Integer updateCounts = insertNotification(notificationInfoDTO);
					 if(updateCounts != null && updateCounts != 0){
						 
				      for (Integer userID : notificationInfoDTO.getUserList()) {
				    	  String messages = "You have meeting ";
							if(notificationInfoDTO.getMeetingLogBean() != null && notificationInfoDTO.getMeetingLogBean().getMeetingId() != null){
								
								if(notificationInfoDTO.getMeetingLogBean().getSenderUserId() != userID) {
									messages += notificationInfoDTO.getMeetingLogBean().getDescription() +" with "+notificationInfoDTO.getMeetingLogBean().getFullName()+" and "+(notificationInfoDTO.getUserList().size()-1)+" others on "+notificationInfoDTO.getMeetingLogBean().getDate() +" from "+notificationInfoDTO.getMeetingLogBean().getStartTime()+" to "+notificationInfoDTO.getMeetingLogBean().getEndTime();
								} else {
									messages  += notificationInfoDTO.getMeetingLogBean().getDescription() +" with "+fullName+" on "+notificationInfoDTO.getMeetingLogBean().getDate() +" from "+notificationInfoDTO.getMeetingLogBean().getStartTime()+" to "+notificationInfoDTO.getMeetingLogBean().getEndTime();
								}
							}
							Sender sender = new Sender(Constants.GCM_APIKEY);
							String NotificationName = notificationInfoDTO.getNotificationType();
							Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true).addData("message", messages).addData("NotificationName", NotificationName).addData("meetingId", notificationInfoDTO.getMeetingId() + "").addData("jsonData", notificationInfoDTO.getJsonObject().toString()).build();
							AuthenticateUser authenticateUser = new AuthenticateUser();
							JSONObject jsonRegistrationId = new JSONObject(authenticateUser.getGCMDeviceRegistrationId(userID));
							if(jsonRegistrationId.has("DeviceRegistrationID")){
								String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
								Result result = sender.send(message, deviceRegistrationId, 1);
								System.out.println(result);
							}
					   }
					}
			} catch(Exception e){
				e.printStackTrace();
			}
			}
			}
		}
		
	public static void sendNotificationToInformAddress(MeetingLogBean meetingLogBean, List<Integer> userList , String otherUserName) {

		System.out.println("sendNotificationToInformAddress==================");

		String messages = "You have a meeting " ;
		for (Integer userId : userList) {

			if(meetingLogBean.getSenderUserId() != userId) {
				messages += meetingLogBean.getDescription() +" with "+meetingLogBean.getFullName()+" and "+(userList.size()-1)+" others on "+meetingLogBean.getDate() +" from "+meetingLogBean+" to "+meetingLogBean.getEndTime();
			} else {
				messages  += meetingLogBean.getDescription() +" with "+otherUserName+" on "+meetingLogBean.getDate() +" from "+meetingLogBean.getStartTime()+" to "+meetingLogBean.getEndTime();
			}
			
			NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
			notificationInfoDTO.setUserList(userList);
			notificationInfoDTO.setMeetingId(meetingLogBean.getMeetingId());
			notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_SUMMARY.toString());
            notificationInfoDTO.setMessage(messages);
			sendMeetingNotification(notificationInfoDTO);

		}
	}
		
	public static Integer insertNotification(NotificationInfoDTO notificationInfoDTO) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		int[] updateCounts;
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertNotificationStoreProc);
			for (Integer userId : notificationInfoDTO.getUserList()) {
				callableStatement.setInt(1, userId);
				callableStatement.setInt(2, 0);
				callableStatement.setInt(3, notificationInfoDTO.getMeetingId());
				callableStatement.setString(4, notificationInfoDTO.getNotificationType());
				callableStatement.setString(5, notificationInfoDTO.getMessage());
				callableStatement.setTimestamp(6, new Timestamp(new Date().getTime()));

				callableStatement.addBatch();

				/*
				 * int value = callableStatement.executeUpdate(); int isError =
				 * callableStatement.getInt(7); int notificationId =
				 * callableStatement.getInt(8);
				 */
			}
			updateCounts = callableStatement.executeBatch();
			return updateCounts.length;

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return null;
	}
}
