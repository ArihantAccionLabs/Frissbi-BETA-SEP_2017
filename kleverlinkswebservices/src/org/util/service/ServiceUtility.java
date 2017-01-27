package org.util.service;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kleverlinks.webservice.AuthenticateUser;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.LocationDetails;
import org.kleverlinks.webservice.MeetingDetails;
import org.kleverlinks.webservice.MyEmailer;
import org.kleverlinks.webservice.NotificationsEnum;
import org.kleverlinks.webservice.UserNotifications;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.UserDTO;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ServiceUtility {

	public static UserDTO getUserDetailsByUserId(Integer userId) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT emailName,firstName,lastName FROM tbl_users WHERE userId ='" + userId + "'" + " limit 1";
			rs = stmt.executeQuery(sql);
			userDTO = new UserDTO();
			while (rs.next()) {
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setFullName(rs.getString("firstName") + rs.getString("lastName"));
			}
			return userDTO;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		}
		return null;
	}

	public static UserDTO getUserDetailsByUserName(String userName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT userId,emailName,firstName,lastName FROM tbl_users WHERE userName ='" + userName + "'"
					+ " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setUserId(rs.getInt("userId"));
				userDTO.setFullName(rs.getString("firstName") + "" + rs.getString("lastName") + "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// closing db resources
		finally{		
			ServiceUtility.closeConnection(conn);
		    ServiceUtility.closeSatetment(stmt);
		}
		return userDTO;

	}

	public static UserDTO getUserDetailsByUserNameAndEmail(String userName, String emailId) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT userId , userName , EmailName  FROM tbl_users WHERE userName ='" + userName
					+ "' OR EmailName ='" + emailId + "'" + " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setUserId(rs.getInt("userId"));
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setUserName(rs.getString("userName"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// closing db resources
		finally{
			ServiceUtility.closeConnection(conn);
		    ServiceUtility.closeSatetment(stmt);
		    }
		return userDTO;

	}

	public static UserDTO getUserDetailsByUserId(int userId, int meetingId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT tbl_users.UserName,tbl_users.FirstName,tbl_users.LastName,tbl_MeetingDetails.SenderFromDateTime,tbl_MeetingDetails.SenderToDateTime,tbl_MeetingDetails.MeetingDescription   FROM tbl_users INNER JOIN tbl_MeetingDetails ON tbl_users.UserID=tbl_MeetingDetails.SenderUserID WHERE MeetingID=? AND tbl_users.UserID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, meetingId);
			pstmt.setInt(2, userId);
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setUserName(rs.getString("UserName"));
				userDTO.setFullName(rs.getString("FirstName") + " " + rs.getString("LastName"));
				userDTO.setMeetingFromTime(convertStringToLocalDateTime(rs.getString("SenderFromDateTime")));
				userDTO.setMeetingToTime(convertStringToLocalDateTime(rs.getString("SenderToDateTime")));
				userDTO.setDescription(rs.getString("MeetingDescription"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// closing db resources
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(pstmt);
		return userDTO;

	}

	public static UserDTO getMeetingDetailsById(int meetingId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT SenderFromDateTime,SenderToDateTime FROM tbl_MeetingDetails WHERE MeetingID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, meetingId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				userDTO = new UserDTO();
				LocalDateTime fromTime = convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime toTime =   convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				userDTO.setStartTime(Float.parseFloat(fromTime.getHour() + "." + fromTime.getMinute()));
				userDTO.setEndTime(Float.parseFloat(toTime.getHour() + "." + toTime.getMinute()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		// closing db resources
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(pstmt);
		}
		return userDTO;

	}
	
	public static LocalDateTime convertStringToLocalDateTime(String date) {
		LocalDateTime fromTime = null;
		try {

			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			fromTime = LocalDateTime.ofInstant(formatter.parse(date).toInstant(), ZoneId.systemDefault());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fromTime;
	}

	public static Map<String , Date> getOneDayDate(String date){
		
		System.out.println("date=============="+date);
		Map<String , Date> map = new HashMap<String , Date>();
		try{
		DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar now = Calendar.getInstance();
		now.setTime(formatter.parse(date));
		now.set(Calendar.HOUR, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);

		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(now.getTime());
		tomorrow.set(Calendar.HOUR, 23);
		tomorrow.set(Calendar.MINUTE, 59);
		tomorrow.set(Calendar.SECOND, 00);
		//tomorrow.add(Calendar.DATE, 1);
		
		map.put("today", now.getTime());
		map.put("tomorrow", tomorrow.getTime());
		//System.out.println("map=================="+map.toString());
		}catch (Exception e) {
		e.printStackTrace();
		}
		return map;
	}
	
	
	public static void closeConnection(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public static void closeSatetment(Statement statement) {
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public static void closeCallableSatetment(CallableStatement callableStatement) {
		try {
			if (callableStatement != null)
				callableStatement.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public static int calculateTimeBetweenLatLng(float lat1, float lng1, float lat2, float lng2) {
		final String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + lat1 + "," + lng1
				+ "&destinations=" + lat2 + "," + lng2 + "&mode=driving&key=" + Constants.GOOGLE_DISTANCE_MATRIX_APIKEY;
		final HttpClient httpclient = org.apache.http.impl.client.HttpClientBuilder.create().build();
		final HttpPost httppost = new HttpPost(url);
		final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("action", "getjson"));

		int timeToBeTaken = 0;
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
		}
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (IOException e) {
		}
		String json_string = null;
		try {
			json_string = EntityUtils.toString(response.getEntity());

			final JSONObject jsonObject = new JSONObject(json_string);
			System.out.println("====" + jsonObject.getJSONArray("rows").length());
			if (jsonObject.getJSONArray("rows").length() > 0) {
				timeToBeTaken = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements")
						.getJSONObject(0).getJSONObject("duration").getInt("value");

				System.out.println(">>>>>>>>>>>>>>>" + timeToBeTaken);

				return timeToBeTaken;
			}
		} catch (Exception e) {
		}
		return timeToBeTaken;
	}

	public static void insertingAndSendingMails(JSONArray mailsArray, int senderUserId, int meetingId)
			throws Exception {
		try {
			Connection connection = null;
			connection = DataSourceConnection.getDBConnection();
			connection.setAutoCommit(false);
			PreparedStatement ps = null;
			String query = "INSERT into tbl_MeetingEmails(MeetingID,SenderUserID,UserEmailID) values(?,?,?)";
			ps = connection.prepareStatement(query);

			for (int i = 0; i < mailsArray.length(); i++) {
				ps.setInt(1, meetingId);
				ps.setInt(2, senderUserId);
				ps.setString(3, mailsArray.getString(i));

				ps.addBatch();
			}
			int[] insertedRow = ps.executeBatch();
			connection.commit();
			System.out.println(" insertingAndSendingMails    insertedRow[i]=========" + insertedRow.length);
			/*
			 * for (int i = 0; i < insertedRow.length; i++) {
			 * 
			 * }
			 */
			for (int i = 0; i < mailsArray.length(); i++) {
				MyEmailer.SendMail(mailsArray.getString(i), "Your meeting request ", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertMeetingContactNumbers(JSONArray contactArray, int senderUserId, int meetingId) {
		try {
			
		
		Connection connection = null;
		connection = DataSourceConnection.getDBConnection();
		connection.setAutoCommit(false);
		PreparedStatement ps = null;
		String query = "INSERT into tbl_MeetingContacts(MeetingID,SenderUserID,ContactNumber) values(?,?,?)";
		ps = connection.prepareStatement(query);

		for (int i = 0; i < contactArray.length(); i++) {

			ps.setInt(1, meetingId);
			ps.setInt(2, senderUserId);
			ps.setString(3, contactArray.getString(i));
			ps.addBatch();

		}
		int[] insertedRow = ps.executeBatch();
		connection.commit();
	/*	for (int i = 0; i < insertedRow.length; i++) {
			System.out.println("insertedRow[i]=========" + insertedRow[i]);

		}*/
		} catch (Exception e) {
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
		sendNotification(userIds, meetingInsertionObject.getInt("senderUserId"),
				NotificationsEnum.Meeting_Pending_Requests.ordinal() + 1, meetingId);
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

	public static void deleteUserFromMeeting(JSONArray meetingArray , int senderUserId , Boolean isMeetingCreator)
			throws IOException, SQLException, PropertyVetoException {

		Connection connection = null;
		connection = DataSourceConnection.getDBConnection();
		PreparedStatement preparedStatement = null;
		String sql = "";
		if(isMeetingCreator){
			sql = "UPDATE  tbl_RecipientsDetails SET isSenderRemoved=1 WHERE MeetingID=? AND UserID=?";
		}else{
			sql = "UPDATE tbl_RecipientsDetails SET Status=2 WHERE MeetingID=? AND UserID=?";
		}
		for (int i = 0; i < meetingArray.length(); i++) {
			
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setInt(1, meetingArray.getJSONObject(i).getInt("meetingId"));
		preparedStatement.setInt(2, senderUserId);
        
		preparedStatement.addBatch();
		}
		
		int [] updatedRow = preparedStatement.executeBatch();
		System.out.println("updatedRow================"+updatedRow.toString());
		/*preparedStatement = null;
		sql = null;
		sql = "SELECT UserID FROM tbl_RecipientsDetails WHERE MeetingID=?";
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setInt(1, meetingId);
	
		ResultSet rs = preparedStatement.executeQuery();
		List<Integer> userIds = new ArrayList<Integer>();
		while (rs.next()) {
			userIds.add(rs.getInt("UserID"));
		}
		System.out.println("<<<<<<<<<<<<<<<<<<" + userIds.toString());
		// sending notification
		sendNotification(userIds, senderUserId, NotificationsEnum.Meeting_Rejected.ordinal() + 1, meetingId);
		sendNotificationToOneUser(senderUserId, 0, NotificationsEnum.Meeting_Rejected.ordinal() + 1, meetingId);*/
	}

	// Generic type method to send notification
	public static void sendNotification(List<Integer> userIds, int senderUserId, int notificationType, int meetingId) {

		for (Integer recipientId : userIds) {
			if (!(senderUserId == recipientId)) {

				UserNotifications userNotifications = new UserNotifications();
				Timestamp timestamp = new Timestamp(new Date().getTime());
				String notificationId = userNotifications.insertUserNotifications(recipientId, senderUserId,
						notificationType, 0, timestamp);
				JSONObject json = new JSONArray(
						userNotifications.getUserNotifications(0, Integer.parseInt(notificationId))).getJSONObject(0);
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

	// Generic type method to send notification
	public static void sendNotificationToOneUser(int recipientId, int senderUserId, int notificationType,
			int meetingId) {
       try{
		UserNotifications userNotifications = new UserNotifications();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		String notificationId = userNotifications.insertUserNotifications(recipientId, senderUserId, notificationType,
				0, timestamp);

		JSONArray jsonArray = new JSONArray(
				userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)));
		if (jsonArray != null && jsonArray.length() > 0) {

			JSONObject json = jsonArray.getJSONObject(0);
			String notificationMessage = json.getString("NotificationMessage");
			String NotificationName = json.getString("NotificationName");
			Sender sender = new Sender(Constants.GCM_APIKEY);

			/*UserDTO userDTO = getUserDetailsByUserId(recipientId, meetingId);
			if (userDTO != null) {
				notificationMessage = "Dear " + userDTO.getFullName() + "you have cancelled the Meeting type : "
						+ userDTO.getDescription() + " on date " + userDTO.getMeetingFromTime().toLocalDate();
			}*/
			Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
					.addData("message", notificationMessage).addData("NotificationName", NotificationName).build();
			
				AuthenticateUser authenticateUser = new AuthenticateUser();
				JSONObject jsonRegistrationId = new JSONObject(
						authenticateUser.getGCMDeviceRegistrationId(recipientId));
				String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
				Result result = sender.send(message, deviceRegistrationId, 1);
				System.out.println(result);
			} 
             }catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static List<UserDTO> checkingMeetingConfliction(int senderUserId, String meetingDate,
		LocalDateTime senderFromDateTime, LocalDateTime senderToDateTime) throws ParseException {
		Map<String , Date> map = getOneDayDate(meetingDate);
		try {
			//usp_CheckingConflicatedMeetings
			CallableStatement callableStatement = null;
			Connection conn = null;
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_CheckingConflicatedMeetings(?,?,?)}";
			System.out.println("from =="+new Timestamp(map.get("today").getTime())+"==="+new Timestamp(map.get("today").getTime()));
			callableStatement = conn.prepareCall(selectStoreProcedue);
			callableStatement.setInt(1, senderUserId);
			callableStatement.setTimestamp(2, new Timestamp(map.get("today").getTime()));
			callableStatement.setTimestamp(3, new Timestamp(map.get("tomorrow").getTime()));
			
			ResultSet rs = callableStatement.executeQuery();;
			List<UserDTO> meetingList = new ArrayList<UserDTO>();
			List<UserDTO> conflictedMeetingList = new ArrayList<UserDTO>();
			while (rs.next()) {
				 System.out.println("MeetingID====="+rs.getInt("MeetingID")+"SenderFromDateTime=="+rs.getTimestamp("SenderFromDateTime")+"===="+rs.getTimestamp("SenderToDateTime"));
				UserDTO userDto = new UserDTO();
				userDto.setMeetingId(rs.getInt("MeetingID"));
				userDto.setUserId(rs.getInt("SenderUserID"));
				LocalDateTime fromTime = convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime toTime =   convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				userDto.setMeetingFromTime(fromTime);
				userDto.setMeetingToTime(toTime);
				userDto.setStartTime(Float.parseFloat(fromTime.getHour() + "." + fromTime.getMinute()));
				userDto.setEndTime(Float.parseFloat(toTime.getHour() + "." + toTime.getMinute()));
				userDto.setLatitude(rs.getString("latitude"));
				userDto.setLongitude(rs.getString("longitude"));
				userDto.setDescription(rs.getString("MeetingDescription"));
				
				meetingList.add(userDto);
			}
           System.out.println("meetingList====="+meetingList.size());
			// logic for avoiding the time collapse b/w meetings
			Float meetingStartTime = Float
					.parseFloat(senderFromDateTime.getHour() + "." + senderFromDateTime.getMinute());
			Float meetingEndTime = Float.parseFloat(senderToDateTime.getHour() + "." + senderToDateTime.getMinute());

			System.out.println(meetingStartTime + "=====" + meetingEndTime + "    " + meetingList.size());
			for (UserDTO userDTO : meetingList) {
				if ((userDTO.getStartTime() <= meetingStartTime && meetingStartTime < userDTO.getEndTime())|| (userDTO.getStartTime() <= meetingEndTime && meetingEndTime < userDTO.getEndTime())) {
					conflictedMeetingList.add(userDTO);
				}
			}
			 System.out.println(conflictedMeetingList.toString()+" <-conList     meetingList====="+meetingList.size());
			return conflictedMeetingList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<UserDTO>();
	}
	
	public static JSONObject getEmailIdByMeetingId(int meetingId){
		
		 Connection conn = null;
		Statement statement = null;
		 JSONObject finalJson = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT UserEmailID FROM tbl_MeetingEmails WHERE MeetingID="+meetingId;
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			JSONArray emailIdsArray = new JSONArray();
			while(rs.next()){
				emailIdsArray.put(rs.getString("UserEmailID"));
			}
			finalJson.put("emails", emailIdsArray);
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return finalJson;
	}
	
	
	public static JSONObject getContactByMeetingId(int meetingId){
		
		 Connection conn = null;
		Statement statement = null;
		 JSONObject finalJson = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT ContactNumber FROM tbl_MeetingContacts WHERE MeetingID="+meetingId;
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			JSONArray emailIdsArray = new JSONArray();
			while(rs.next()){
				emailIdsArray.put(rs.getString("ContactNumber"));
			}
			finalJson.put("contacts", emailIdsArray);
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return finalJson;
	}
	
	public static JSONObject getMeetingDetailsByUserId(int userId){
		
		 Connection conn = null;
		 Statement statement = null;
		 JSONObject finalJson = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			
			
			String sql ="" ;
			
			ResultSet rs = statement.executeQuery(sql);
			JSONArray friendsArray = new JSONArray();
			while(rs.next()){
				friendsArray.put(rs.getString("firstName")+" "+rs.getString("lastName"));
				friendsArray.put(rs.getInt("Status"));
			}
			finalJson.put("friendsArray", friendsArray);
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return finalJson;
	}
	
	

	public static JSONObject getReceptionistByMeetingId(int meetingId){
		
		 Connection conn = null;
		 Statement statement = null;
		 JSONObject finalJson = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT tbl_users.firstName,tbl_users.lastName,tbl_RecipientsDetails.Status FROM tbl_RecipientsDetails INNER JOIN tbl_users ON  tbl_RecipientsDetails.UserID=tbl_users.UserID WHERE MeetingID="+meetingId;
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			JSONArray friendsArray = new JSONArray();
			while(rs.next()){
				friendsArray.put(rs.getString("firstName")+" "+rs.getString("lastName"));
				friendsArray.put(rs.getInt("Status"));
			}
			finalJson.put("friendsArray", friendsArray);
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return finalJson;
	}
	
}
