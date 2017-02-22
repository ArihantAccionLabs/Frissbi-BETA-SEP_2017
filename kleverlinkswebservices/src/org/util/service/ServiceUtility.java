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

import javax.ws.rs.core.MediaType;

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
import org.kleverlinks.bean.AppUserBean;
import org.kleverlinks.bean.MeetingLogBean;
import org.kleverlinks.enums.MeetingStatus;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.MyEmailer;
import org.service.dto.UserDTO;
import org.util.Utility;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ServiceUtility {

	public static JSONObject getUserDetailByUserId(Long userId) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_GetUserDetailsByUserID(?)}";
			callableStatement = conn.prepareCall(selectStoreProcedue);
			callableStatement.setLong(1, userId);
			ResultSet rs = callableStatement.executeQuery();;
	
			while (rs.next()) {
				jsonObject.put("email" , rs.getString("emailName"));
				jsonObject.put("fullName" , rs.getString("FirstName") + rs.getString("LastName"));
				jsonObject.put("userId" , rs.getInt("UserID"));
			}
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
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
			sql = "SELECT userId,emailName,FirstName,LastName FROM tbl_users WHERE userName ='" + userName + "'"
					+ " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setUserId(rs.getLong("userId"));
				userDTO.setFullName(rs.getString("FirstName") + "" + rs.getString("LastName") + "");
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

	public static AppUserBean getUserDetailsByUserNameAndEmail(String userName, String emailId , String contactNumber) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql;
		AppUserBean appUserBean = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT ContactNumber,UserName,EmailName FROM tbl_users WHERE userName=? OR EmailName=? OR ContactNumber=? limit 1";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
			pstmt.setString(2, emailId);
			pstmt.setString(3, contactNumber);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				appUserBean = new AppUserBean();
				appUserBean.setUsername(rs.getString("UserName"));
				appUserBean.setContactno(rs.getString("ContactNumber"));
				appUserBean.setEmail(rs.getString("EmailName"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			ServiceUtility.closeConnection(conn);
		    ServiceUtility.closeSatetment(pstmt);
		    }
		return appUserBean;

	}

	public static UserDTO getUserDetailsByUserId(Long userId, Long meetingId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT tbl_users.UserName,tbl_users.FirstName,tbl_users.LastName,tbl_MeetingDetails.SenderFromDateTime,tbl_MeetingDetails.SenderToDateTime,tbl_MeetingDetails.MeetingDescription   FROM tbl_users INNER JOIN tbl_MeetingDetails ON tbl_users.UserID=tbl_MeetingDetails.SenderUserID WHERE MeetingID=? AND tbl_users.UserID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, meetingId);
			pstmt.setLong(2, userId);
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

	public static UserDTO getMeetingDetailsById(Long meetingId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT SenderFromDateTime,SenderToDateTime FROM tbl_MeetingDetails WHERE MeetingID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, meetingId);
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
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
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

	public static int insertingAndSendingMails(JSONArray mailsArray, Long senderUserId, Long meetingId)
			throws Exception {
		try {
			Connection connection = null;
			connection = DataSourceConnection.getDBConnection();
			connection.setAutoCommit(false);
			PreparedStatement ps = null;
			String query = "INSERT into tbl_MeetingEmails(MeetingID,UserEmailID) values(?,?)";
			ps = connection.prepareStatement(query);

			for (int i = 0; i < mailsArray.length(); i++) {
				ps.setLong(1, meetingId);
				ps.setString(2, mailsArray.getString(i));

				ps.addBatch();
			}
			int[] insertedRow = ps.executeBatch();
			connection.commit();
			
			return insertedRow.length;
			/*System.out.println(" insertingAndSendingMails    insertedRow[i]=========" + insertedRow.length);
			for (int i = 0; i < mailsArray.length(); i++) {
				MyEmailer.SendMail(mailsArray.getString(i), "Your meeting request ", "");
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int insertMeetingContactNumbers(JSONArray contactArray, Long senderUserId, Long meetingId) {
		Connection connection = null;
		int[] insertedRow;
		PreparedStatement ps = null;
		try {
		
		connection = DataSourceConnection.getDBConnection();
		connection.setAutoCommit(false);
		String query = "INSERT into tbl_MeetingContacts(MeetingID,ContactNumber) values(?,?)";
		ps = connection.prepareStatement(query);

		for (int i = 0; i < contactArray.length(); i++) {
	       if(contactArray.getString(i) != null && ! contactArray.getString(i).trim().isEmpty()){
	    	   
	    	   ps.setLong(1, meetingId);
	    	   ps.setString(2, contactArray.getString(i).replaceAll("(\\d)\\s(\\d)", "$1$2"));
	    	   ps.addBatch();
	       }
		}
		insertedRow = ps.executeBatch();
		connection.commit();
		return insertedRow.length;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			closeConnection(connection);
			closeSatetment(ps);
		}
		return 0;
	}

	public static void deleteUserFromMeeting(JSONArray meetingArray , Long senderUserId)throws IOException, SQLException, PropertyVetoException {

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		String sql = "";
		try{
		connection = DataSourceConnection.getDBConnection();
		List<Long> meetingList = new ArrayList<>();
		
			if (meetingArray.length() > 1) {
				for (int i = 0; i < meetingArray.length(); i++) {
					meetingList.add(meetingArray.getLong(i));
				}
			} else
				meetingList.add(meetingArray.getJSONObject(0).getLong("meetingId"));
		
		for (Long meetingId : meetingList) {
			preparedStatement = null;
			if(! isMeetingCreatorRemoved(meetingId , senderUserId)){
				sql = "UPDATE  tbl_MeetingDetails SET MeetingStatus=? WHERE MeetingID=? AND SenderUserId=?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, MeetingStatus.CANCELLED.toString());
				preparedStatement.setLong(2, meetingId);
				preparedStatement.setLong(3, senderUserId);
			}else{
				sql = "UPDATE tbl_RecipientsDetails SET Status=2,ResponseDateTime=? WHERE MeetingID=? AND UserID=?";
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, new SimpleDateFormat("yyyy-dd-mm HH:mm:ss").format(new Date()));
				preparedStatement.setLong(2, meetingId);
				preparedStatement.setLong(3, senderUserId);
			}
			preparedStatement.executeUpdate();
		}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(connection);
			ServiceUtility.closeSatetment(preparedStatement);
		}
	}


	public static List<UserDTO> checkingMeetingConfliction(Long senderUserId, String meetingDate,LocalDateTime senderFromDateTime, LocalDateTime senderToDateTime) throws ParseException {
		Map<String , Date> map = getOneDayDate(meetingDate);
		try {
			CallableStatement callableStatement = null;
			Connection conn = null;
			conn = DataSourceConnection.getDBConnection();
			String selectStoreProcedue = "{call usp_CheckingConflicatedMeetings(?,?,?)}";
			System.out.println("from =="+new Timestamp(map.get("today").getTime())+"==="+new Timestamp(map.get("today").getTime()));
			callableStatement = conn.prepareCall(selectStoreProcedue);
			callableStatement.setLong(1, senderUserId);
			callableStatement.setTimestamp(2, new Timestamp(map.get("today").getTime()));
			callableStatement.setTimestamp(3, new Timestamp(map.get("tomorrow").getTime()));
			
			ResultSet rs = callableStatement.executeQuery();
			List<UserDTO> meetingList = new ArrayList<UserDTO>();
			List<UserDTO> conflictedMeetingList = new ArrayList<UserDTO>();
			while (rs.next()) {
				// System.out.println("MeetingID====="+rs.getInt("MeetingID")+"SenderFromDateTime=="+rs.getTimestamp("SenderFromDateTime")+"===="+rs.getTimestamp("SenderToDateTime"));
				 UserDTO userDto = new UserDTO();
				userDto.setMeetingId(rs.getLong("MeetingID"));
				userDto.setUserId(rs.getLong("SenderUserID"));
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
			Float meetingStartTime = Float.parseFloat(senderFromDateTime.getHour() + "." + senderFromDateTime.getMinute());
			Float meetingEndTime = Float.parseFloat(senderToDateTime.getHour() + "." + senderToDateTime.getMinute());

			for (UserDTO userDTO : meetingList) {
				//System.out.println(meetingStartTime + "=====" + meetingEndTime + "    " + userDTO.getStartTime()+"   "+userDTO.getEndTime()+"==="+((meetingStartTime < userDTO.getStartTime() && userDTO.getStartTime() < meetingEndTime)|| (meetingStartTime < userDTO.getEndTime()  && userDTO.getEndTime() < meetingEndTime)));
				if ((meetingStartTime <= userDTO.getStartTime() && userDTO.getStartTime() <= meetingEndTime)|| (meetingStartTime <= userDTO.getEndTime()  && userDTO.getEndTime() <= meetingEndTime)) {
				conflictedMeetingList.add(userDTO);
				}
			}
			 System.out.println("conflictedMeetingList====="+conflictedMeetingList.size());
			return conflictedMeetingList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<UserDTO>();
	}
	
	public static JSONArray getEmailIdByMeetingId(Long meetingId){
		
		 Connection conn = null;
		Statement statement = null;
		 JSONArray emailIdJsonArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT UserEmailID FROM tbl_MeetingEmails WHERE MeetingID="+meetingId;
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()){
				emailIdJsonArray.put(rs.getString("UserEmailID"));
			}
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return emailIdJsonArray;
	}
	
	
	public static JSONArray getContactByMeetingId(Long meetingId){
		
		 Connection conn = null;
		  Statement statement = null;
		 JSONArray contactsJsonArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			String sql = "SELECT ContactNumber FROM tbl_MeetingContacts WHERE MeetingID="+meetingId;
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()){
				contactsJsonArray.put(rs.getString("ContactNumber"));
			}
			
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return contactsJsonArray;
	}
	
	public static Integer getMeetingStatusByUserId(Long meetingId , Long userId){
		
		 Connection conn = null;
		 PreparedStatement statement = null;
		 int status = 0;
		try {
			conn = DataSourceConnection.getDBConnection();
			//"SELECT Status FROM tbl_RecipientsDetails WHERE tbl_RecipientsDetails.MeetingID="+meetingId+" AND tbl_RecipientsDetails.UserID="+userId;
			String sql ="SELECT Status FROM tbl_RecipientsDetails WHERE tbl_RecipientsDetails.MeetingID=? AND tbl_RecipientsDetails.UserID=?";
			statement = conn.prepareStatement(sql);
			statement.setLong(1, meetingId);
			statement.setLong(2, userId);
			ResultSet rs = statement.executeQuery();
			int count = 0;
			while(rs.next()){
				status = rs.getInt("Status");
				count++;
			}
			if(count == 0 && isMeetingCreatorRemoved(meetingId,userId)){ //Meeting creator is by default meeting accepted
				status = 1;
			}
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return status;
	}

	public static Boolean isMeetingCreatorRemoved(Long meetingId , Long userId){
		
		 Connection conn = null;
		 PreparedStatement statement = null;
		 String meetingStatus = "";
		 Boolean status = false;
		try {
			conn = DataSourceConnection.getDBConnection();
			//"SELECT Status FROM tbl_RecipientsDetails WHERE tbl_RecipientsDetails.MeetingID="+meetingId+" AND tbl_RecipientsDetails.UserID="+userId;
			String sql ="SELECT MeetingStatus FROM tbl_MeetingDetails WHERE tbl_MeetingDetails.MeetingID=? AND tbl_MeetingDetails.SenderUserID=?";
			statement = conn.prepareStatement(sql);
			statement.setLong(1, meetingId);
			statement.setLong(2, userId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				meetingStatus = rs.getString("MeetingStatus");
			}
			if(meetingStatus != null && meetingStatus.equals(MeetingStatus.CANCELLED.toString())){
				status = true;
			}
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(statement);
		}
		return status;
	}
	
	
	public static Map<String , JSONArray> getReceptionistByMeetingId(Long meetingId , Long userId){
		
		 Connection conn = null;
		 CallableStatement callableStatement = null;
		 JSONArray friendsArray = new JSONArray();
		 JSONArray userIdsArray = new JSONArray();
		 Map<String , JSONArray> map = new HashMap<>();
		try {
			conn = DataSourceConnection.getDBConnection();
			//SELECT tbl_users.firstName,tbl_users.lastName,tbl_RecipientsDetails.Status FROM tbl_RecipientsDetails INNER JOIN tbl_users ON  tbl_RecipientsDetails.UserID=tbl_users.UserID WHERE MeetingID="+meetingId;
			String storeProc = "{call usp_GetMeetingFriendsList_ByMeetingID(?)}"; 
			callableStatement = conn.prepareCall(storeProc);
			callableStatement.setLong(1, meetingId);
			ResultSet rs = callableStatement.executeQuery();
			while(rs.next()){
				if(!(userId == rs.getLong("UserID"))){
					friendsArray.put(rs.getString("firstName")+" "+rs.getString("lastName"));
					userIdsArray.put(rs.getLong("UserID"));
				}
			}
			map.put("friendsArray", friendsArray);
			map.put("userIdsArray", userIdsArray);
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return map;
	}
	public static JSONObject getReceiverDetailsByMeetingId(Long meetingId , Long userId){
		
		 Connection conn = null;
		 CallableStatement callableStatement = null;
		 JSONArray friendsArray = new JSONArray();
		 JSONObject finalJson = new JSONObject();
		 Long status = 0l;
		try {
			conn = DataSourceConnection.getDBConnection();
			//SELECT tbl_users.firstName,tbl_users.lastName,tbl_RecipientsDetails.Status FROM tbl_RecipientsDetails INNER JOIN tbl_users ON  tbl_RecipientsDetails.UserID=tbl_users.UserID WHERE MeetingID="+meetingId;
			String storeProc = "{call usp_GetUserDetails_ByMeetingID(?)}"; 
			callableStatement = conn.prepareCall(storeProc);
			callableStatement.setLong(1, meetingId);
			ResultSet rs = callableStatement.executeQuery();
			while(rs.next()){
					
				if(!(userId == rs.getLong("UserID"))){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("userId", rs.getLong("UserID"));
					jsonObject.put("fullName", rs.getString("firstName")+" "+rs.getString("lastName"));
					jsonObject.put("status", rs.getLong("Status"));
					friendsArray.put(jsonObject);
					
				} else {
					 status = rs.getLong("Status");
				}
			}
		   
			finalJson.put("friendsArray", friendsArray);
			finalJson.put("status", status);
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return finalJson;
	}
	
	
	
	public static MeetingLogBean getMeetingDetailsByMeetingId(Long meetingId){
		Connection conn = null;
		CallableStatement  callableStatement = null;
		MeetingLogBean meetingLogBean = new MeetingLogBean();
		try {
			conn = DataSourceConnection.getDBConnection();
			//usp_GetMeetingDetails_ByMeetingID
			String meetingDetailsStoreProc = "{call usp_GetMeetingDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(meetingDetailsStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.executeQuery();
			ResultSet rs = callableStatement.getResultSet();
			while(rs.next()){
					meetingLogBean.setSenderUserId(rs.getLong("SenderUserID"));
					meetingLogBean.setMeetingId(rs.getLong("MeetingID"));
					meetingLogBean.setFullName(rs.getString("FirstName") + rs.getString("LastName"));
					LocalDateTime fromTime = convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
					LocalDateTime toTime =   convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
					meetingLogBean.setDate(fromTime.toLocalDate());
					meetingLogBean.setToDate(toTime);
					meetingLogBean.setFromDate(fromTime);
					meetingLogBean.setStartTime(updateTime(fromTime.getHour(), fromTime.getMinute()));
					meetingLogBean.setEndTime(updateTime(toTime.getHour(), toTime.getMinute()));
					meetingLogBean.setDescription(rs.getString("MeetingDescription"));
					meetingLogBean.setLatitude(rs.getString("Latitude"));
					meetingLogBean.setLongitude(rs.getString("Longitude"));
					meetingLogBean.setAddress(rs.getString("GoogleAddress"));
			}
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return meetingLogBean;
	}
	
       public static JSONArray getReceptionistDetailsByMeetingId(Long meetingId){
		
		Connection conn = null;
		CallableStatement  callableStatement = null;
		JSONArray jsonArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			//usp_GetMeetingDetails_ByMeetingID
			String meetingDetailsStoreProc = "{call usp_GetRecipientDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(meetingDetailsStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();
			while(rs.next()){
			  if(rs.getLong("Status") == 1){
				  JSONObject jsonObject = new JSONObject();
				  jsonObject.put("userId" , rs.getLong("UserID"));
				  jsonObject.put("fullName" , rs.getString("firstName") + rs.getString("lastName"));
				  jsonObject.put("status" , rs.getLong("Status"));
				  jsonArray.put(jsonObject);
			  }
			}
		} catch(Exception  e){
			e.printStackTrace();
		}finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return jsonArray;
	}

       public static UserDTO getUserDetailsByMeetingIdAndUserId(Long meetingId , Long userId){
   		
   		Connection conn = null;
   		CallableStatement  callableStatement = null;
   		UserDTO userDto = new UserDTO();
   		try {
   			conn = DataSourceConnection.getDBConnection();
   			//usp_GetMeetingDetails_ByMeetingID
   			String userDetail= "{call usp_GetUserDetailsByMeetingId(?)}";
   			callableStatement = conn.prepareCall(userDetail);
   			callableStatement.setLong(1, meetingId);
   			callableStatement.execute();
   			ResultSet rs = callableStatement.getResultSet();
   			while(rs.next()){
   				userDto.setUserId(rs.getLong("UserID"));
   				userDto.setFullName(rs.getString("firstName") + rs.getString("lastName"));
   			}
   		} catch(Exception  e){
   			e.printStackTrace();
   		}finally {
   			ServiceUtility.closeConnection(conn);
   			ServiceUtility.closeCallableSatetment(callableStatement);
   		}
   		return userDto;
   	}
       public static String updateTime(int hours, int mins) {

    	    String timeSet = "";
    	    if (hours > 12) {
    	        hours -= 12;
    	        timeSet = "PM";
    	    } else if (hours == 0) {
    	        hours += 12;
    	        timeSet = "AM";
    	    } else if (hours == 12)
    	        timeSet = "PM";
    	    else
    	        timeSet = "AM";
    	    String minutes = "";
    	    String _hours = "";

    	    if (hours < 10) {
    	        _hours = "0" + hours;
    	    } else {
    	        _hours = String.valueOf(hours);
    	    }

    	    if (mins < 10)
    	        minutes = "0" + mins;
    	    else
    	        minutes = String.valueOf(mins);

    	    // Append in a StringBuilder
    	    String aTime = new StringBuilder().append(_hours).append(':').append(minutes).append(" ").append(timeSet).toString();
    	    return aTime;

    	}
   	private static String getOutputAsString(WebResource service) {
		return service.accept(MediaType.TEXT_PLAIN).get(String.class);
	}
       
       public static String getFulladdressBYLatLng(Double lat , Double lng){
    	   
    	String  formattedAddress = "";
    	String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lng+"&key="+Constants.GCM_APIKEY;;
   		ClientConfig config = new DefaultClientConfig();
   		Client client = Client.create(config);
   		WebResource service = client.resource(url);
   		
   		try {
   			JSONObject jsonObject = new JSONObject(
   					getOutputAsString(service));
   			JSONArray results = (JSONArray) jsonObject.get("results");
   			JSONObject resultsObject = (JSONObject) results.get(0);
   			  formattedAddress = (String) resultsObject.get("formatted_address");
   			System.out.println("Mid point location address is: "+formattedAddress );
   		} catch (JSONException e) {
   			e.printStackTrace();
   		}  
   		return formattedAddress;
       }
       
	public static JSONObject checkMeetingAddressUpdateByMeetingId(Long meetingId) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String meetingDetailsStoreProc = "{call usp_checkMeetingAddressUpdateByMeetingId(?)}";
			callableStatement = conn.prepareCall(meetingDetailsStoreProc);
			callableStatement.setLong(1, meetingId);
			ResultSet rs = callableStatement.executeQuery();
			while (rs.next()) {//
				jsonObject.put("updateCount", rs.getInt("UpdateCount"));
				jsonObject.put("senderFromDateTime", rs.getString("SenderFromDateTime"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return jsonObject;
	}
	
	public static JSONObject getUserImageId(Long userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		JSONObject jsonObject = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			sql = "SELECT ProfileImageId,BackGroundImageId FROM tbl_usertransactions WHERE UserID=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, userId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				if(Utility.checkValidString(rs.getString("ProfileImageId"))){
					jsonObject = new JSONObject();
					jsonObject.put("profileImageId", rs.getString("ProfileImageId"));
					jsonObject.put("backGroundImageId", rs.getString("BackGroundImageId"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(pstmt);
		}
		return jsonObject;

	}
}
