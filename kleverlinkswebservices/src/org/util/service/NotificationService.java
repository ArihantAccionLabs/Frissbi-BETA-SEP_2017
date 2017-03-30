package org.util.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.MeetingAcceptedUserBean;
import org.kleverlinks.bean.MeetingBean;
import org.kleverlinks.bean.MeetingLogBean;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.NotificationsEnum;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Message.Builder;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.NotificationInfoDTO;
import org.service.dto.UserDTO;
import org.util.Utility;

public class NotificationService {

	/*
	 * @Purpose -> sending meeting creation suggestion notification among fiends
	 * who posted their free time on same date and their free time slot is
	 * matching minimum 1 hour
	 * 
	 */
	public static void sendMeetingSuggestionNotification(NotificationInfoDTO notificationInfoDTO) {
		
	    insertNotification(notificationInfoDTO);
	
		Sender sender = new Sender(Constants.GCM_APIKEY);
		Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
				.addData("userId", notificationInfoDTO.getUserId()+"").addData("message", notificationInfoDTO.getMessage()).addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
		try {
			String deviceRegistrationId = getDeviceRegistrationId(notificationInfoDTO.getUserId());
			if (Utility.checkValidString(deviceRegistrationId)) {
				Result result = sender.send(message, deviceRegistrationId, 1);
				System.out.println(result);
				System.out.println("Notification sent successfully");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
     }

	public static void sendMeetingAcceptRejectNotification(NotificationInfoDTO notificationInfoDTO) {

		MeetingLogBean meetingLogBean = notificationInfoDTO.getMeetingLogBean();

		List<Long> userList = new ArrayList<Long>();
		String message = "";

		if (meetingLogBean.getMeetingId() != null) {

			if (notificationInfoDTO.getNotificationType()
					.equals(NotificationsEnum.MEETING_REQUEST_ACCEPTANCE.toString())) {

				UserDTO userDTO = ServiceUtility.getUserDetailsByMeetingIdAndUserId(notificationInfoDTO.getMeetingId(),
						notificationInfoDTO.getSenderUserId());

				if (userDTO.getFullName() != null && !userDTO.getFullName().trim().isEmpty()) {

					message = userDTO.getFullName().toUpperCase() +" Accepted your metting "+ meetingLogBean.getDescription();
					userList.add(meetingLogBean.getSenderUserId());
				}
			} else {
				if (meetingLogBean.getSenderUserId().equals(notificationInfoDTO.getSenderUserId())) {

					JSONObject jsonObject = ServiceUtility.getReceiverDetailsByMeetingId(
							notificationInfoDTO.getMeetingId(), notificationInfoDTO.getSenderUserId());

					JSONArray userIdsArray = jsonObject.getJSONArray("friendsArray");
					System.out.println((userIdsArray.length() != 0) + " klength=========" + userIdsArray.length()
							+ "   " + userIdsArray.getJSONObject(0).getInt("userId"));
					if (userIdsArray.length() != 0) {
						for (int i = 0; i < userIdsArray.length(); i++) {
							userList.add(userIdsArray.getJSONObject(i).getLong("userId"));
						}
						message = "Meeting " + meetingLogBean.getDescription() + " is cancelled by "
								+ meetingLogBean.getFullName().toUpperCase() + " which is on on date "
								+ meetingLogBean.getDate() + " from " + meetingLogBean.getStartTime() + " to "
								+ meetingLogBean.getEndTime();
					}
				} else {
					UserDTO userDTO = ServiceUtility.getUserDetailsByMeetingIdAndUserId(
							notificationInfoDTO.getMeetingId(), notificationInfoDTO.getSenderUserId());
					message =   userDTO.getFullName().toUpperCase()+" Rejected your meeting "+meetingLogBean.getDescription() /*+ " has been rejected by "
							+ userDTO.getFullName().toUpperCase() + " which is on on date " + meetingLogBean.getDate()
							+ " from " + meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime()*/;

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

	public static void sendingMeetingCreationNotification(MeetingBean meetingBean, Long meetingId) {

		NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();

		MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
		if (meetingLogBean != null) {

			String message = meetingLogBean.getDescription() + " meeting with "
					+ meetingLogBean.getFullName() ;/*+ " on " + meetingLogBean.getDate() + " from "
					+ meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime();*/

			notificationInfoDTO.setMessage(message);
			notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_PENDING_REQUESTS.toString());
			notificationInfoDTO.setMeetingId(meetingId);
			notificationInfoDTO.setSenderUserId(meetingBean.getSenderUserId());
			notificationInfoDTO.setUserList(meetingBean.getFriendsIdList());

			NotificationService.sendMeetingNotification(notificationInfoDTO);
		}
	}

	public static void sendNotification(List<Long> meetingIds, Long senderUserId, int notificationType) {

		try {
			NotificationInfoDTO notificationInfoDTO = null;
			for (Long meetingId : meetingIds) {

				if (ServiceUtility.isMeetingCreatorRemoved(meetingId, senderUserId)) {
					Set<Long> userList = new HashSet<Long>();
					MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
					JSONArray friendsArray = ServiceUtility.getReceiverDetailsByMeetingId(meetingId, senderUserId).getJSONArray("friendsArray");
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
								userList.add(friendsArray.getJSONObject(i).getLong("userId"));
							}
						}
					}
					if (!userList.isEmpty()) {
						String message = fullName + " cancelled the "+meetingLogBean.getDescription()+" meeting " /*+ meetingLogBean.getDate() + " from "
								+ meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime()*/;
						notificationInfoDTO = new NotificationInfoDTO();
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

	public static void sendPendingMeetingRequest(NotificationInfoDTO notificationInfoDTO) {

		Connection conn = null;
		CallableStatement callableStatement = null;

		try {
			conn = DataSourceConnection.getDBConnection();
			String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertNotificationStoreProc);

			System.out.println("UserListsize=======================" + notificationInfoDTO.getUserList().size());
			for (Long userId : notificationInfoDTO.getUserList()) {

				callableStatement.setLong(1, userId);
				callableStatement.setLong(2, notificationInfoDTO.getSenderUserId());
				callableStatement.setLong(3, notificationInfoDTO.getMeetingId());
				callableStatement.setString(4, notificationInfoDTO.getNotificationType());
				callableStatement.setString(5, notificationInfoDTO.getMessage());
				callableStatement.setTimestamp(6, new Timestamp(new Date().getTime()));

				int value = callableStatement.executeUpdate();
				Sender sender = new Sender(Constants.GCM_APIKEY);
				Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
						.addData("message", notificationInfoDTO.getMessage())
						.addData("NotificationName", notificationInfoDTO.getNotificationType()).build();

				String deviceRegistrationId = getDeviceRegistrationId(userId);
				if (Utility.checkValidString(deviceRegistrationId)) {
					Result result = sender.send(message, deviceRegistrationId, 1);
					System.out.println(result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendMeetingLocationConfirmation(NotificationInfoDTO notificationInfoDTO) {

		try {
			int value = insertBatchNotification(notificationInfoDTO);
			System.out.println("value===============" + value);
			if (value != 0) {

				Builder message = null;
				Sender sender = new Sender(Constants.GCM_APIKEY);
					message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
							.addData("meetingId", notificationInfoDTO.getMeetingId() + "")
							.addData("message", notificationInfoDTO.getMessage())
							.addData("NotificationName", notificationInfoDTO.getNotificationType())
							.addData("locationSuggestionJson", notificationInfoDTO.getJsonObject().toString());
							
					for (MeetingAcceptedUserBean meetingAcceptedUserBean : notificationInfoDTO.getMeetingAcceptedUserBeanList()) {

					String deviceRegistrationId = getDeviceRegistrationId(meetingAcceptedUserBean.getUserId());
					if (Utility.checkValidString(deviceRegistrationId)) {
						
						message.addData("userId", meetingAcceptedUserBean.getUserId()+"");
						message.addData("profileImageId", meetingAcceptedUserBean.getProfileImageId());
						message.addData("meessage", getFriends(notificationInfoDTO.getMeetingAcceptedUserBeanList() , meetingAcceptedUserBean.getUserId()));
						
						Result result = sender.send(message.build(), deviceRegistrationId, 1);
						System.out.println(result);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static String getFriends(List<MeetingAcceptedUserBean> meetingAcceptedUserBeans , Long userId){
		String names = "";
		int count = 1;
		for (MeetingAcceptedUserBean meetingAcceptedUserBean : meetingAcceptedUserBeans) {
			
			if(!(meetingAcceptedUserBean.getUserId().longValue() == userId.longValue())){
				
				if(count == meetingAcceptedUserBeans.size()){
					names += meetingAcceptedUserBean.getFullName();
				}else{
					names += meetingAcceptedUserBean.getFullName()+", ";
				}
			}
		}
		return names;
	}
	
	public static void sendMeetingNotificationBeforeTwoHour(NotificationInfoDTO notificationInfoDTO) {

		try {
			int value = insertBatchNotification(notificationInfoDTO);
			System.out.println("value===============" + value);
			if (value != 0) {

				Builder builder = null;
				Sender sender = new Sender(Constants.GCM_APIKEY);
				if (notificationInfoDTO.getJsonObject() != null) {
					builder = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
							.addData("meetingId", notificationInfoDTO.getMeetingId() + "")
							.addData("meetingMessage", notificationInfoDTO.getMessage())
							.addData("NotificationName", notificationInfoDTO.getNotificationType())
							.addData("locationSuggestionJson", notificationInfoDTO.getJsonObject().toString())	;
				} 
				for (Long userId : notificationInfoDTO.getUserList()) {

					String deviceRegistrationId = getDeviceRegistrationId(userId);
					if (Utility.checkValidString(deviceRegistrationId)) {
						
						builder.addData("meetingFriendsArray", getFriendsJsonArray(notificationInfoDTO.getMeetingAcceptedUserBeanList() , userId).toString());
						
						Result result = sender.send(builder.build(), deviceRegistrationId, 1);
						System.out.println(result);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public static JSONArray getFriendsJsonArray(List<MeetingAcceptedUserBean> meetingAcceptedUserBeanList, Long userId) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = null;

		for (MeetingAcceptedUserBean meetingAcceptedUserBean : meetingAcceptedUserBeanList) {
			if (userId == meetingAcceptedUserBean.getUserId()) {

				jsonObject = new JSONObject();
				jsonObject.put("userId", meetingAcceptedUserBean.getUserId());
				jsonObject.put("fullName", meetingAcceptedUserBean.getFullName());
				jsonObject.put("profileImageId", meetingAcceptedUserBean.getProfileImageId());

				jsonArray.put(jsonObject);
			}

		}
		return jsonArray;
	}
	
	
	public static void sendMeetingNotification(NotificationInfoDTO notificationInfoDTO) {

		try {
			int value = insertBatchNotification(notificationInfoDTO);
			System.out.println("value===============" + value);
			if (value != 0) {

				Message message = null;
				Sender sender = new Sender(Constants.GCM_APIKEY);
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
				for (Long userId : notificationInfoDTO.getUserList()) {

					String deviceRegistrationId = getDeviceRegistrationId(userId);
					if (Utility.checkValidString(deviceRegistrationId)) {
						Result result = sender.send(message, deviceRegistrationId, 1);
						System.out.println(result);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendMeetingAlarmNotification(List<MeetingLogBean> addressMeetingList) {

		Boolean isLocationSelected = false;

		System.out.println("sendMeetingAlarmNotification=====" + addressMeetingList.size());
		for (MeetingLogBean meetingCreatorLogBean : addressMeetingList) {

			JSONArray jsonArray = ServiceUtility.getReceptionistDetailsByMeetingId(meetingCreatorLogBean.getMeetingId());
			Set<Long> userIdSet = new HashSet<>();

			String fullName = "";
			for (int i = 0; i < jsonArray.length(); i++) {
				if (jsonArray.getJSONObject(i).getInt("userId") != 0) {
					userIdSet.add(jsonArray.getJSONObject(i).getLong("userId"));
					fullName = jsonArray.getJSONObject(i).getString("fullName");
				}
			}
			if (meetingCreatorLogBean.getSenderUserId() != null) {
				userIdSet.add(meetingCreatorLogBean.getSenderUserId());
			}

			NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
			System.out.println(meetingCreatorLogBean.getSenderUserId()  +"  userIdSet=========" + userIdSet.toString());
			if (!userIdSet.isEmpty()) {

				notificationInfoDTO.setMeetingId(meetingCreatorLogBean.getMeetingId());
				notificationInfoDTO.setSenderUserId(0l);
				notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_SUMMARY.toString());
				notificationInfoDTO.setMeetingLogBean(meetingCreatorLogBean);
				notificationInfoDTO.setUserList(userIdSet.stream().collect(Collectors.toList()));
				JSONObject finalJson = new JSONObject();

				finalJson.put("meetingId", meetingCreatorLogBean.getMeetingId());
				notificationInfoDTO.setJsonObject(finalJson);

				try {
					System.out.println("size=================" + notificationInfoDTO.getUserList().size());
					Integer updateCounts = insertBatchNotification(notificationInfoDTO);
					if (updateCounts != null && updateCounts != 0) {

						for (Long userID : notificationInfoDTO.getUserList()) {
							String messages = "You have ";
							if (notificationInfoDTO.getMeetingLogBean() != null
									&& notificationInfoDTO.getMeetingLogBean().getMeetingId() != null) {

								if (notificationInfoDTO.getMeetingLogBean().getSenderUserId() != userID) {
									messages += notificationInfoDTO.getMeetingLogBean().getDescription() + " meeting with "
											+ notificationInfoDTO.getMeetingLogBean().getFullName() + " and "
											+ (notificationInfoDTO.getUserList().size() - 1) 
											+" in the next 1 hour";
											
											/*" others on "
											+ notificationInfoDTO.getMeetingLogBean().getDate() + " from "
											+ notificationInfoDTO.getMeetingLogBean().getStartTime() + " to "
											+ notificationInfoDTO.getMeetingLogBean().getEndTime();*/
								} else {
									messages += notificationInfoDTO.getMeetingLogBean().getDescription() + " meeting with "
											+ fullName 
											+ " and "
											+ (notificationInfoDTO.getUserList().size() - 1) 
											+" in the next 1 hour";
											
											/* " on " + notificationInfoDTO.getMeetingLogBean().getDate()
											+ " from " + notificationInfoDTO.getMeetingLogBean().getStartTime() + " to "
											+ notificationInfoDTO.getMeetingLogBean().getEndTime();*/
								}
							}
							Sender sender = new Sender(Constants.GCM_APIKEY);
							String NotificationName = notificationInfoDTO.getNotificationType();
							if (meetingCreatorLogBean.getLatitude() != null
									&& !meetingCreatorLogBean.getLatitude().trim().isEmpty()) {
								isLocationSelected = true;
							}
							Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
									.addData("message", messages).addData("NotificationName", NotificationName)
									.addData("meetingId", notificationInfoDTO.getMeetingId() + "")
									.addData("isLocationSelected", isLocationSelected.toString()).build();
						
							String deviceRegistrationId = getDeviceRegistrationId(userID);
							if (Utility.checkValidString(deviceRegistrationId)) {
								Result result = sender.send(message, deviceRegistrationId, 1);
								System.out.println(result);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void sendNotificationToInformAddress(MeetingLogBean meetingLogBean, List<Long> userList,
			String otherUserName) {

		System.out.println("sendNotificationToInformAddress==================");

		String messages = "You have a meeting ";
		for (Long userId : userList) {

			if (meetingLogBean.getSenderUserId() != userId) {
				messages += meetingLogBean.getDescription() + " with " + meetingLogBean.getFullName() + " and "
						+ (userList.size() - 1) + " others on " + meetingLogBean.getDate() + " from " + meetingLogBean
						+ " to " + meetingLogBean.getEndTime();
			} else {
				messages += meetingLogBean.getDescription() + " with " + otherUserName + " on "
						+ meetingLogBean.getDate() + " from " + meetingLogBean.getStartTime() + " to "
						+ meetingLogBean.getEndTime();
			}

			NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
			notificationInfoDTO.setUserList(userList);
			notificationInfoDTO.setMeetingId(meetingLogBean.getMeetingId());
			notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_SUMMARY.toString());
			notificationInfoDTO.setMessage(messages);
			sendMeetingNotification(notificationInfoDTO);

		}
	}

	public static Integer insertBatchNotification(NotificationInfoDTO notificationInfoDTO) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertNotificationStoreProc);
			for (Long userId : notificationInfoDTO.getUserList()) {
				callableStatement.setLong(1, userId);
				if(notificationInfoDTO.getSenderUserId() != null){
					callableStatement.setLong(2, notificationInfoDTO.getSenderUserId());
				}else{
					callableStatement.setLong(2, 0);
				}
				if(notificationInfoDTO.getGroupId() != null){
					callableStatement.setLong(3, notificationInfoDTO.getGroupId());
				}else{
					callableStatement.setLong(3, 0);
				}
				if(notificationInfoDTO.getUserFriendListId() != null){
					callableStatement.setLong(4, notificationInfoDTO.getUserFriendListId());
				}else{
					callableStatement.setLong(4, 0);
				}
				if(notificationInfoDTO.getMeetingId() != null){
					callableStatement.setLong(5, notificationInfoDTO.getMeetingId());
				}else{
					callableStatement.setLong(5, 0);
				}
				callableStatement.setString(6, notificationInfoDTO.getNotificationType());
				callableStatement.setString(7, notificationInfoDTO.getMessage());
				callableStatement.setTimestamp(8, new Timestamp(new Date().getTime()));

				callableStatement.addBatch();
			}
			int[] updateCounts = callableStatement.executeBatch();
			return updateCounts.length;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return 0;
	}
	
	public static Integer insertNotification(NotificationInfoDTO notificationInfoDTO) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		int value = 0;
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertNotificationStoreProc = "{call usp_InsertNotification(?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertNotificationStoreProc);
			callableStatement.setLong(1, notificationInfoDTO.getUserId());
			if(notificationInfoDTO.getSenderUserId() != null){
				callableStatement.setLong(2, notificationInfoDTO.getSenderUserId());	
			}else{
				callableStatement.setLong(2, 0l);
			}
			
			
			if(notificationInfoDTO.getGroupId() != null){
				callableStatement.setLong(3, notificationInfoDTO.getGroupId());
			}else{
				callableStatement.setLong(3, 0);
			}
			if(notificationInfoDTO.getUserFriendListId() != null){
				callableStatement.setLong(4, notificationInfoDTO.getUserFriendListId());
			}else{
				callableStatement.setLong(4, 0);
			}
			
			if(notificationInfoDTO.getMeetingId() != null){
				callableStatement.setLong(5, notificationInfoDTO.getMeetingId());
			}else{
				callableStatement.setLong(5, 0);
			}
			callableStatement.setString(6, notificationInfoDTO.getNotificationType());
			callableStatement.setString(7, notificationInfoDTO.getMessage());
			callableStatement.setTimestamp(8, new Timestamp(new Date().getTime()));
			value = callableStatement.executeUpdate();
			  return value;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return value;
	}
	
	public static void sendFriendNotification(NotificationInfoDTO notificationInfoDTO) {
		try {
			int insertedNotification = insertNotification(notificationInfoDTO);
			System.out.println("insertedNotification=============="+insertedNotification);
			if (insertedNotification != 0) {

				Sender sender = new Sender(Constants.GCM_APIKEY);
				Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true).addData("friendUserId", notificationInfoDTO.getSenderUserId()+"").addData("message", notificationInfoDTO.getMessage()).addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
				String deviceRegistrationId = getDeviceRegistrationId(notificationInfoDTO.getUserId());
				if (Utility.checkValidString(deviceRegistrationId)) {
					Result result = sender.send(message, deviceRegistrationId, 1);
					System.out.println(result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendNewMemeberAddingNotification(NotificationInfoDTO notificationInfoDTO) {
		try {
			int insertedNotification = insertNotification(notificationInfoDTO);
			System.out.println("insertedNotification=============="+insertedNotification);
			Sender sender = new Sender(Constants.GCM_APIKEY);
			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true).addData("groupId", notificationInfoDTO.getGroupId()+"").addData("message", notificationInfoDTO.getMessage()).addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
			String deviceRegistrationId  = "";
			Result result = null;
			if (insertedNotification != 0) {

					
				 deviceRegistrationId = getDeviceRegistrationId(notificationInfoDTO.getUserId());
				if (Utility.checkValidString(deviceRegistrationId)) {
					 result = sender.send(message, deviceRegistrationId, 1);
					System.out.println(result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void sendGroupCreationNotification(NotificationInfoDTO notificationInfoDTO) {
		try {
			int insertedNotification = insertBatchNotification(notificationInfoDTO);
			System.out.println(notificationInfoDTO.getUserList().size()+"  insertedNotification=============="+insertedNotification);
			Sender sender = new Sender(Constants.GCM_APIKEY);
			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true).addData("groupId", notificationInfoDTO.getGroupId()+"").addData("message", notificationInfoDTO.getMessage()).addData("NotificationName", notificationInfoDTO.getNotificationType()).build();
			String deviceRegistrationId  = "";
			Result result = null;
			if (insertedNotification != 0) {

				for (Long userId : notificationInfoDTO.getUserList()) {
					
					System.out.println("userId   :::   "+userId);
					
				 deviceRegistrationId = getDeviceRegistrationId(userId);
				if (Utility.checkValidString(deviceRegistrationId)) {
					 result = sender.send(message, deviceRegistrationId, 1);
					System.out.println("result  :   "+result);
				}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getDeviceRegistrationId(Long userId){
		Connection conn = null;
		CallableStatement callableStatement = null;
		String deviceRegistrationID = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetDeviceRegistrationId(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();
			while(rs.next()){
				deviceRegistrationID = rs.getString("DeviceRegistrationID");
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		
		System.out.println("deviceRegistrationID    :   "+deviceRegistrationID);
		
		return deviceRegistrationID;
	}
}
