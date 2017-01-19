package org.util.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.AuthenticateUser;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.NotificationsEnum;
import org.kleverlinks.webservice.UserNotifications;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;

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
				NotificationsEnum.Friend_Request_Acceptance.ordinal() + 1, 0, timestamp);
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
				NotificationsEnum.Meeting_Request_Acceptance.ordinal() + 1, 0, timestamp);
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
	
}
