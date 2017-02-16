package org.kleverlinks.webservice;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kleverlinks.bean.MeetingLogBean;
import org.kleverlinks.enums.MeetingStatus;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.NotificationInfoDTO;
import org.service.dto.UserDTO;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

import com.sun.jersey.api.client.WebResource;

@Path("MeetingDetailsService")
public class MeetingDetails {

	/**
	 * @Author : Sunil Verma
	 * @Action : Meeting creation but if any meeting exist with same date and time then check conflict
	 * 
	 */

	
	@POST
	@Path("/insertMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertMeetingDetails(String meetingInsertion) {
		JSONObject finalJson = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		System.out.println("meetingInsertion==========" + meetingInsertion);
		try {
			
			 JSONObject meetingInsertionObject = new JSONObject(meetingInsertion);
			 Long senderUserId = meetingInsertionObject.getLong("senderUserId");
			 if(meetingInsertionObject.has("meetingIdsJsonArray")){
				ServiceUtility.deleteUserFromMeeting(meetingInsertionObject.getJSONArray("meetingIdsJsonArray") , senderUserId);
				NotificationService.sendNotification(meetingInsertionObject.getJSONArray("meetingIdsJsonArray"), senderUserId, NotificationsEnum.MEETING_REJECTED.ordinal());
			}
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
			String meetingDate = meetingInsertionObject.getString("meetingDateTime");
			String durationTime = meetingInsertionObject.getString("duration");
			String[] timeArray = durationTime.split(":");
			String duration = timeArray[0] + ":" + timeArray[1] + ":00";
			LocalDateTime senderFromDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(),ZoneId.systemDefault());
			LocalDateTime senderToDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(), ZoneId.systemDefault()).plusHours(Integer.parseInt(timeArray[0])).plusMinutes(Integer.parseInt(timeArray[1]));

			//System.out.println(formatter.parse(meetingDate)+"   "+"senderFromDateTime==="+senderFromDateTime+"===="+senderToDateTime);

			if (!meetingInsertionObject.has("meetingIdsJsonArray")) {
				List<UserDTO> userDTOList = null;
				userDTOList = ServiceUtility.checkingMeetingConfliction(meetingInsertionObject.getLong("senderUserId"),
						meetingDate, senderFromDateTime, senderToDateTime);
				if (!userDTOList.isEmpty()) {
					
					if(userDTOList.size() > 1){
						for (UserDTO userDTO : userDTOList) {
							jsonArray.put(userDTO.getMeetingId());
						}
						finalJson.put("message", "Conflicting with multiple meetings");
					}else{
						UserDTO userDTO = userDTOList.get(0);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("from", userDTO.getMeetingFromTime().getHour() + ":"+ userDTO.getMeetingFromTime().getMinute());
						jsonObject.put("to",userDTO.getMeetingToTime().getHour() + ":" + userDTO.getMeetingToTime().getMinute());
						jsonObject.put("meetingId", userDTO.getMeetingId());
						finalJson.put("message", "Conflicting with meeting : "+userDTO.getDescription()+" on date "+ userDTO.getMeetingFromTime().toLocalDate());
					
						jsonArray.put(jsonObject);
					}
					finalJson.put("isInserted", false);
					finalJson.put("status", true);
					finalJson.put("meetingIdsJsonArray", jsonArray);
					
					System.out.println("finalJson====="+finalJson.toString());
					
					return finalJson.toString();
				}
			}
			Connection connection = null;
			CallableStatement callableStatement = null;
			connection = DataSourceConnection.getDBConnection();
			Long meetingId = 0l;
			String insertStoreProc = "{call usp_InsertMeetingDetails(?,?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = connection.prepareCall(insertStoreProc);
			callableStatement.setLong(1, senderUserId);
			callableStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
			callableStatement.setTimestamp(3,  Timestamp.valueOf(senderFromDateTime));
			callableStatement.setTimestamp(4, Timestamp.valueOf(senderToDateTime));
			callableStatement.setTime(5, java.sql.Time.valueOf(duration));
			callableStatement.setString(6, meetingInsertionObject.getString("meetingTitle"));
			if(meetingInsertionObject.getBoolean("isLocationSelected")){
				callableStatement.setString(7, meetingInsertionObject.getString("latitude"));
				callableStatement.setString(8, meetingInsertionObject.getString("longitude"));
				callableStatement.setString(9, meetingInsertionObject.getString("address"));
			}else{
				callableStatement.setString(7, null);
				callableStatement.setString(8, null);
				callableStatement.setString(9, null);
			}
			callableStatement.setString(10, MeetingStatus.ACTIVE.toString());
			callableStatement.registerOutParameter(11, Types.INTEGER);
			callableStatement.registerOutParameter(12, Types.BIGINT);

			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(11);
			meetingId = callableStatement.getLong(12);
			System.out.println(isError + "meetingId >>>>>>>>>>>>>>>>>> " + meetingId+"  value=="+value);

			if (isError == 0) {

				JSONArray jsonArrayData = meetingInsertionObject.getJSONArray("friendsIdJsonArray");
				// System.out.println(">>>>>>>>>>>>>" + jsonArrayData.length());

				connection.setAutoCommit(false);
				PreparedStatement ps = null;
				String query = "INSERT into tbl_RecipientsDetails(MeetingID,UserID,Status,RecipientFromDateTime,RecipientToDateTime,Latitude,Longitude,DestinationType,GoogleAddress) values(?,?,?,?,?,?,?,?,?)";
				ps = connection.prepareStatement(query);

				for (int i = 0; i < jsonArrayData.length(); i++) {

					ps.setLong(1, meetingId);
					ps.setLong(2, jsonArrayData.getLong(i));
					ps.setString(3, "0");
					ps.setTimestamp(4, null);
					ps.setTimestamp(5, null);
					ps.setString(6, null);
					ps.setString(7, null);
					ps.setLong(8, 0);
					ps.setString(9, null);

					ps.addBatch();

				}
				int[] insertedRow = ps.executeBatch();
				/*for (int i = 0; i < insertedRow.length; i++) {
					 System.out.println("insertedRow[i]========="+insertedRow[i]);
				}*/
				connection.commit();
				// return Response.status(201).entity(i).build();
				if (insertedRow.length != 0) {
					try {
						ServiceUtility.insertingAndSendingMails(meetingInsertionObject.getJSONArray("emailIdJsonArray"),senderUserId, meetingId);
					} catch (Exception e) {
						e.printStackTrace();
						finalJson.put("status", false);
						finalJson.put("isInserted", false);
						finalJson.put("message", "Mail not sent");

						return finalJson.toString();
					}
					NotificationService.sendingMeetingCreationNotification(meetingInsertionObject, meetingId);
					// Inserting the contact number of the user who is not on the Fissbi
					ServiceUtility.insertMeetingContactNumbers(meetingInsertionObject.getJSONArray("contactsJsonArray"),senderUserId, meetingId);
				}

				finalJson.put("status", true);
				finalJson.put("isInserted", true);
				finalJson.put("isLocationSelected", meetingInsertionObject.getBoolean("isLocationSelected"));
				finalJson.put("meetingId", meetingId);//
				finalJson.put("message", "meeting inserted successfully");

				return finalJson.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		finalJson.put("status", false);
		finalJson.put("isInserted", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
	}

	public static String getOutputAsString(WebResource service) {
		return service.accept(MediaType.TEXT_PLAIN).get(String.class);
	}

	@GET
	@Path("/getRecipientXMLByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRecipientXMLByMeetingID(@PathParam("meetingId") int meetingId) {

		Connection conn = null;
		Statement stmt = null;
		String recipientXML = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientXML_ByMeetingID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.registerOutParameter(2, Types.VARCHAR);
			int value = callableStatement.executeUpdate();
			recipientXML = callableStatement.getString(2);
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);		}
		return recipientXML;
	}

	@GET
	@Path("/updateRecipientXMLByMeetingID/{meetingId}/{recipientDetails}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateRecipientXMLByMeetingID(@PathParam("meetingId") int meetingId,
			@PathParam("recipientDetails") String recipientDetails) {

		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			recipientDetails = recipientDetails.replace("@", "/");
			String insertStoreProc = "{call usp_UpdateRecipientXML_ByMeetingID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.setString(2, recipientDetails);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3) + "";
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);		}
		return isError;
	}

	@GET
	@Path("/updateRecipientXML/{meetingId}/{recipientId}/{fromDate}/{toDate}/{status}/{GoogleAddress}/{geoDateTime}/{Latitude}/{Longitude}/{oLatitude}/{oLongitude}/{DestinationType}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateRecipientXML(@PathParam("meetingId") Long  meetingId, @PathParam("recipientId") Long recipientId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate,
			@PathParam("status") int status, @PathParam("GoogleAddress") String GoogleAddress,
			@PathParam("geoDateTime") Timestamp geoDateTime, @PathParam("Latitude") String Latitude,
			@PathParam("Longitude") String Longitude, @PathParam("oLatitude") String oLatitude,
			@PathParam("oLongitude") String oLongitude, @PathParam("DestinationType") int DestinationType) {

		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			boolean updateFlag = false;
			String recipientDetailsByMeetingId = getRecipientDetailsByMeetingID(meetingId);
			JSONArray jsonResultsArray = new JSONArray(recipientDetailsByMeetingId);
			String xmlString = "<Recipients>";
			for (int i = 0; i < jsonResultsArray.length(); i++) {
				JSONObject jsonObject = (JSONObject) jsonResultsArray.get(i);
				xmlString += "<RecipientID>";
				xmlString += jsonObject.getString("UserId");
				xmlString += "</RecipientID>";
				if (jsonObject.getString("UserId").equals(recipientId + "")) {
					updateFlag = true;
					xmlString += "<Status>";
					xmlString += status;
					xmlString += "</Status>";
					xmlString += "<RecipientFromDateTime>";
					xmlString += fromDate;
					xmlString += "</RecipientFromDateTime>";
					xmlString += "<RecipientToDateTime>";
					xmlString += toDate;
					xmlString += "</RecipientToDateTime>";
					xmlString += "<GoogleAddress>";
					xmlString += GoogleAddress;
					xmlString += "</GoogleAddress>";
					xmlString += "<GeoDateTime>";
					xmlString += geoDateTime;
					xmlString += "</GeoDateTime>";
					xmlString += "<Latitude>";
					xmlString += Latitude;
					xmlString += "</Latitude>";
					xmlString += "<Longitude>";
					xmlString += Longitude;
					xmlString += "</Longitude>";
					xmlString += "<olatitude>";
					xmlString += oLatitude;
					xmlString += "</olatitude>";
					xmlString += "<oLongitude>";
					xmlString += oLongitude;
					xmlString += "</oLongitude>";
					xmlString += "<DestinationType>";
					xmlString += DestinationType;
					xmlString += "</DestinationType>";
				} else {
					xmlString += "<Status>";
					xmlString += jsonObject.getString("Status");
					xmlString += "</Status>";
					xmlString += "<RecipientFromDateTime>";
					xmlString += jsonObject.getString("RecipientFromDateTime");
					xmlString += "</RecipientFromDateTime>";
					xmlString += "<RecipientToDateTime>";
					xmlString += jsonObject.getString("RecipientToDateTime");
					xmlString += "</RecipientToDateTime>";
					xmlString += "<GoogleAddress>";
					xmlString += jsonObject.getString("GoogleAddress");
					xmlString += "</GoogleAddress>";
					// xmlString+= "<GeoDateTime>";
					// xmlString+= jsonObject.getString("GeoDateTime");
					// xmlString+= "</GeoDateTime>";
					xmlString += "<Latitude>";
					xmlString += jsonObject.getString("Latitude");
					xmlString += "</Latitude>";
					xmlString += "<Longitude>";
					xmlString += jsonObject.getString("Longitude");
					xmlString += "</Longitude>";
					xmlString += "<olatitude>";
					xmlString += jsonObject.getString("olatitude");
					xmlString += "</olatitude>";
					xmlString += "<oLongitude>";
					xmlString += jsonObject.getString("oLongitude");
					xmlString += "</oLongitude>";
					xmlString += "<DestinationType>";
					xmlString += jsonObject.getString("DestinationType");
					xmlString += "</DestinationType>";
				}

				xmlString += "<ResponseDateTime>";
				xmlString += jsonObject.getString("ResponseDateTime");
				xmlString += "</ResponseDateTime>";
				xmlString += "<UserPreferredLocationID>";
				xmlString += jsonObject.getString("UserPreferredLocationID");
				xmlString += "</UserPreferredLocationID>";
				// xmlString+= "<DestinationType>";
				// xmlString+= jsonObject.getString("DestinationType");
				// xmlString+= "</DestinationType>";

			}
			if (!updateFlag) {
				xmlString += "<RecipientID>";
				xmlString += recipientId;
				xmlString += "</RecipientID>";
				xmlString += "<Status>";
				xmlString += status;
				xmlString += "</Status>";
				xmlString += "<RecipientFromDateTime>";
				xmlString += fromDate;
				xmlString += "</RecipientFromDateTime>";
				xmlString += "<RecipientToDateTime>";
				xmlString += toDate;
				xmlString += "</RecipientToDateTime>";
			}
			xmlString += "</Recipients>";
			String insertStoreProc = "{call usp_UpdateRecipientXML_ByMeetingID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.setString(2, xmlString);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3) + "";

			if (isError.equals("0")) {
				String meetingDetails = getMeetingDetailsByMeetingID(meetingId);
				JSONArray jsonArray = new JSONArray(meetingDetails);
                Long senderUserId = 0l;
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					senderUserId = Long.parseLong(jsonObject.getString("SenderUserID"));
				}
				UserNotifications userNotifications = new UserNotifications();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				if (status == 1) {
					String notificationId = userNotifications.insertUserNotifications(senderUserId, recipientId,
							NotificationsEnum.MEETING_REQUEST_ACCEPTANCE.ordinal() + 1l, 0l, timestamp);
					JSONObject json = new JSONArray(
							userNotifications.getUserNotifications(0l, Long.parseLong(notificationId)))
									.getJSONObject(0);
					String notificationMessage = json.getString("NotificationMessage");
					String NotificationName = json.getString("NotificationName");
					Sender sender = new Sender(Constants.GCM_APIKEY);
					Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
							.addData("message", notificationMessage).addData("NotificationName", NotificationName)
							.addData("meetingId", meetingId + "").build();

					try {
						AuthenticateUser authenticateUser = new AuthenticateUser();
						JSONObject jsonRegistrationId = new JSONObject(
								authenticateUser.getGCMDeviceRegistrationId(senderUserId));
						String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (status == 2) {
					String notificationId = userNotifications.insertUserNotifications(senderUserId, recipientId,
							NotificationsEnum.MEETING_REJECTED.ordinal() + 1l, 0l, timestamp);
					JSONObject json = new JSONArray(
							userNotifications.getUserNotifications(0l, Long.parseLong(notificationId)))
									.getJSONObject(0);
					String notificationMessage = json.getString("NotificationMessage");
					String NotificationName = json.getString("NotificationName");
					Sender sender = new Sender(Constants.GCM_APIKEY);
					Message message = new Message.Builder().timeToLive(3).delayWhileIdle(true).dryRun(true)
							.addData("message", notificationMessage).addData("NotificationName", NotificationName)
							.addData("meetingId", meetingId + "").build();

					try {
						AuthenticateUser authenticateUser = new AuthenticateUser();
						JSONObject jsonRegistrationId = new JSONObject(
								authenticateUser.getGCMDeviceRegistrationId(senderUserId));
						String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);		}
		return isError;
	}

	@GET
	@Path("/getRecipientDetailsByUserID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRecipientDetailsByUserID(@PathParam("meetingId") int meetingId, @PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientDetails_ByUserID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setLong(2, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("ResponseDateTime", rs.getString("ResponseDateTime"));
				jsonObject.put("Status", rs.getString("Status"));
				if (rs.getString("AvatarPath") == null) {
					jsonObject.put("AvatarPath", "");
				} else {
					jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
				}
				jsonObject.put("RecipientFromDateTime", rs.getString("RecipientFromDateTime"));
				jsonObject.put("RecipientToDateTime", rs.getString("RecipientToDateTime"));
				jsonObject.put("Latitude", rs.getString("Latitude"));
				jsonObject.put("Longitude", rs.getString("Longitude"));
				jsonObject.put("oLatitude", rs.getString("oLatitude"));
				jsonObject.put("oLongitude", rs.getString("oLongitude"));
				jsonObject.put("UserPreferredLocationID", rs.getString("UserPreferredLocationID"));
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
				jsonObject.put("GeoDateTime", rs.getString("GeoDateTime"));
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);		}
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/getRecipientDetailsByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRecipientDetailsByMeetingID(@PathParam("meetingId") Long  meetingId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("ResponseDateTime", rs.getString("ResponseDateTime"));
				jsonObject.put("Status", rs.getString("Status"));
				if (rs.getString("AvatarPath") == null) {
					jsonObject.put("AvatarPath", "");
				} else {
					jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
				}
				jsonObject.put("RecipientFromDateTime", rs.getString("RecipientFromDateTime"));
				jsonObject.put("RecipientToDateTime", rs.getString("RecipientToDateTime"));
				jsonObject.put("Latitude", rs.getString("Latitude"));
				jsonObject.put("Longitude", rs.getString("Longitude"));
				jsonObject.put("oLatitude", rs.getString("oLatitude"));
				jsonObject.put("oLongitude", rs.getString("oLongitude"));
				jsonObject.put("UserPreferredLocationID", rs.getString("UserPreferredLocationID"));
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
				// jsonObject.put("GeoDateTime", rs.getString("GeoDateTime"));
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);		}
		return jsonResultsArray.toString();
	}

	/**
	 * @Author : Sunil Verma
	 * @Action : Getting meeting detail according to the meeting id
	 * 
	 */
	
	@POST
	@Path("/getMeetingDetailsByUserID")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getMeetingDetailsByUserID(String meetingDate) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		try {
			JSONObject meetingDateJsonObject = new JSONObject(meetingDate);
			System.out.println("meetingDateJsonObject  =============="+meetingDateJsonObject.toString());
			Map<String , Date> map = ServiceUtility.getOneDayDate(meetingDateJsonObject.getString("date"));
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetMeetingDetails_ByUserID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingDateJsonObject.getLong("userId"));
			callableStatement.setTimestamp(2, new Timestamp(map.get("today").getTime()));
			callableStatement.setTimestamp(3, new Timestamp(map.get("tomorrow").getTime()));
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");				
				LocalDateTime senderFromDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("meetingId" , rs.getLong("MeetingID"));
				//jsonObject.put("meetingSenderId" , rs.getInt("SenderUserID"));
				jsonObject.put("date" , LocalDateTime.ofInstant(formatter.parse(rs.getString("SenderFromDateTime")).toInstant(), ZoneId.systemDefault()).toLocalDate());
				jsonObject.put("from" , senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to" , senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description" , rs.getString("MeetingDescription"));
				
				if(rs.getString("Latitude") != null && ! rs.getString("Latitude").trim().isEmpty()){
					
					jsonObject.put("isLocationSelected",true);
					jsonObject.put("address" , rs.getString("GoogleAddress"));
					jsonObject.put("latitude" , rs.getString("Latitude"));
					jsonObject.put("longitude" , rs.getString("Longitude"));
				}else{
					jsonObject.put("isLocationSelected",false);
				}
		
				jsonResultsArray.put(jsonObject);
			}
			  JSONArray meetingArray = new JSONArray();
				 for (int i = 0; i < jsonResultsArray.length(); i++) {
					   JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
					   jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(jsonObject.getLong("meetingId")));
					   jsonObject.put("contactsJsonArray",  ServiceUtility.getContactByMeetingId(jsonObject.getLong("meetingId")));
					   jsonObject.put("friendsJsonArray", ServiceUtility.getReceptionistByMeetingId(jsonObject.getLong("meetingId") , meetingDateJsonObject.getLong("userId")).get("friendsArray"));
					   jsonObject.put("status", ServiceUtility.getMeetingStatusByUserId(jsonObject.getLong("meetingId") , meetingDateJsonObject.getLong("userId")));
					   
					   meetingArray.put(jsonObject);
				}
			
			finalJson.put("status", true);
			finalJson.put("message", "Success");
			finalJson.put("meetingArray", jsonResultsArray);
			return finalJson.toString();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	   }

	
	/**
	 * @Author : Sunil Verma
	 * @Action : Accept or reject meeting but if we accept then check the date & time if any conflict then 
	 *           inform to user if they persist to accept then remove previous meeting and accept new meeting   
	 * 
	 */
	@POST
	@Path("/getConflictedMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getConflictedMeetingDetails(String meetingDetails) {
		
		System.out.println("==============="+meetingDetails.toString());
        JSONObject meetingDetailsJsonObject = new JSONObject(meetingDetails);
        Long meetingId = meetingDetailsJsonObject.getLong("meetingId");
        
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		CallableStatement callableStatement = null;
		Connection conn = null;
		try {
			 if(meetingDetailsJsonObject.has("meetingIdsJsonArray")){
				 
					ServiceUtility.deleteUserFromMeeting(meetingDetailsJsonObject.getJSONArray("meetingIdsJsonArray") , meetingDetailsJsonObject.getLong("userId"));
					NotificationService.sendNotification(meetingDetailsJsonObject.getJSONArray("meetingIdsJsonArray"), meetingDetailsJsonObject.getLong("userId"), NotificationsEnum.MEETING_REJECTED.ordinal());
			
			 }else if(! meetingDetailsJsonObject.has("isRejected")){
				
			     Map<String , Date> map = ServiceUtility.getOneDayDate(meetingDetailsJsonObject.getString("meetingDate"));
			    conn = DataSourceConnection.getDBConnection();
				String selectStoreProcedue = "{call usp_CheckingConflicatedMeetings(?,?,?)}";
				System.out.println("from =="+new Timestamp(map.get("today").getTime())+"==="+new Timestamp(map.get("today").getTime()));
				callableStatement = conn.prepareCall(selectStoreProcedue);
				callableStatement.setLong(1, meetingDetailsJsonObject.getLong("userId"));
				callableStatement.setTimestamp(2, new Timestamp(map.get("today").getTime()));
				callableStatement.setTimestamp(3, new Timestamp(map.get("tomorrow").getTime()));
				
				ResultSet rs = callableStatement.executeQuery();;
				List<UserDTO> meetingList = new ArrayList<UserDTO>();
				List<UserDTO> conflictedMeetingList = new ArrayList<UserDTO>();
				UserDTO meetingDto = new UserDTO();
				meetingDto = ServiceUtility.getMeetingDetailsById(meetingId);
			while (rs.next()) {
				
				   LocalDateTime fromTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				   LocalDateTime toTime =   ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
					UserDTO userDto = new UserDTO();
					userDto.setMeetingId(rs.getLong("MeetingID"));
					userDto.setUserId(rs.getLong("SenderUserID"));
					userDto.setMeetingFromTime(fromTime);
					userDto.setMeetingToTime(toTime);
					userDto.setStartTime(Float.parseFloat(fromTime.getHour() + "." + fromTime.getMinute()));
					userDto.setEndTime(Float.parseFloat(toTime.getHour() + "." + toTime.getMinute()));
					if(rs.getString("latitude") != null && rs.getString("longitude") != null && rs.getString("MeetingDescription") != null){
						
						userDto.setLatitude(rs.getString("latitude"));
						userDto.setLongitude(rs.getString("longitude"));
						userDto.setDescription(rs.getString("MeetingDescription"));
						userDto.setIsLocationSelected(true);
					}else{
						userDto.setIsLocationSelected(false);
					}
				
				meetingList.add(userDto);
			}
			// logic for avoiding the time collapse b/w meetings
			for (UserDTO userDTO : meetingList) {
				//System.out.println(userDTO.getStartTime()+"="+userDTO.getEndTime()+"==="+meetingDto.getStartTime()+"==="+meetingDto.getEndTime());
				if ((userDTO.getStartTime() <= meetingDto.getStartTime() && meetingDto.getStartTime() < userDTO.getEndTime())|| (userDTO.getStartTime() <= meetingDto.getEndTime() && meetingDto.getEndTime() < userDTO.getEndTime())) {
					conflictedMeetingList.add(userDTO);
				}
			}
			if(! conflictedMeetingList.isEmpty()){
				
				if(conflictedMeetingList.size() > 1){
					for (UserDTO userDTO : conflictedMeetingList) {
						jsonResultsArray.put(userDTO.getMeetingId());
					}
					finalJson.put("message", "Conflicting with  meetings");
				}else{
					
					   UserDTO userDTO = conflictedMeetingList.get(0);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("from", userDTO.getMeetingFromTime().getHour() + ":"+ userDTO.getMeetingFromTime().getMinute());
						jsonObject.put("to",userDTO.getMeetingToTime().getHour() + ":" + userDTO.getMeetingToTime().getMinute());
						jsonObject.put("meetingId", userDTO.getMeetingId());
						jsonResultsArray.put(jsonObject);
					finalJson.put("message", "Conflicting with meeting : "+userDTO.getDescription()+" on date "+ userDTO.getMeetingFromTime().toLocalDate());
				}
				finalJson.put("isAccepted", false);
				finalJson.put("status", true);
			
				finalJson.put("meetingIdsJsonArray", jsonResultsArray);	
					return finalJson.toString();
			}
         }
			 //accepting the meeting request by updating the tbl_reception table
			 System.out.println(meetingDetailsJsonObject.has("isRejected") +"<-Rejected  Accepted -> "+meetingDetailsJsonObject.has("isAccepted"));
			 MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
			 
			 if(meetingDetailsJsonObject.has("isAccepted") && meetingDetailsJsonObject.getBoolean("isAccepted")){
				 if(meetingRequestUpdate(meetingId , meetingDetailsJsonObject.getLong("userId") , meetingDetailsJsonObject.getString("meetingStatus") , false) != 0 ){
					 
					 NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
					 notificationInfoDTO.setSenderUserId(meetingDetailsJsonObject.getLong("userId"));
					 notificationInfoDTO.setMeetingId(meetingId);
					 notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_REQUEST_ACCEPTANCE.toString());
					 notificationInfoDTO.setMeetingLogBean(meetingLogBean);
					 
					 NotificationService.sendMeetingAcceptRejectNotification(notificationInfoDTO);
					 
					 finalJson.put("isAccepted", true);
					 finalJson.put("status", true);
					 finalJson.put("message", "Meeting accepted successfully");
					 return finalJson.toString();
					 
			 }
			 }else if(meetingDetailsJsonObject.has("isRejected") && meetingDetailsJsonObject.getBoolean("isRejected")){
				 Boolean isMeetingCreator = meetingLogBean.getSenderUserId() == meetingDetailsJsonObject.getLong("userId") ? true : false;
				 
	        	if(meetingRequestUpdate(meetingId , meetingDetailsJsonObject.getLong("userId") , meetingDetailsJsonObject.getString("meetingStatus") , isMeetingCreator) != 0 ){
	        		NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
					 notificationInfoDTO.setSenderUserId(meetingDetailsJsonObject.getLong("userId"));
					 notificationInfoDTO.setMeetingId(meetingId);
					 notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_REJECTED.toString());
					 notificationInfoDTO.setMeetingLogBean(meetingLogBean);
					 NotificationService.sendMeetingAcceptRejectNotification(notificationInfoDTO);
	        		
	        		finalJson.put("message", "Meeting rejected successfully");
					finalJson.put("isRejected", true);
					finalJson.put("status", true);
					return finalJson.toString();
				}
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oopse something went wrong");
		return finalJson.toString();
	}
	
	public int meetingRequestUpdate(Long meetingId , Long userId , String status , Boolean isMeetingCreator){
		Connection conn = null;
		PreparedStatement pstmt = null;
		int updatedRow = 0;
		String sql = "";
		try{
			conn = DataSourceConnection.getDBConnection();
			
			if(isMeetingCreator){
				
			 sql = "UPDATE tbl_MeetingDetails SET MeetingStatus=? WHERE MeetingID=? AND SenderUserID=?"; 
			 pstmt = conn.prepareStatement(sql);
			 pstmt.setString(1, MeetingStatus.CANCELLED.toString());
			 pstmt.setLong(2, meetingId);
			 pstmt.setLong(3, userId);
				
				updatedRow =	pstmt.executeUpdate();

		   }else{
			sql = "UPDATE tbl_RecipientsDetails SET Status=?,ResponseDateTime=? WHERE MeetingID=? AND UserID=?"; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setString(2, new SimpleDateFormat("yyyy-dd-mm HH:mm:ss").format(new Date()));
			pstmt.setLong(3, meetingId);
			pstmt.setLong(4, userId);
				
		    updatedRow =	pstmt.executeUpdate();
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("MeetingRequestUpdate============================="+updatedRow);
		return updatedRow;
	}

	
	@GET
	@Path("/updateConflictedMeetingDetails/{meetingId}/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateConflictedMeetingDetails(@PathParam("meetingId") Long  meetingId, @PathParam("userId") Long  userId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate) throws JSONException, ParseException {

		JSONArray jsonResultsArray = new JSONArray();
		String conflictedMeetingDetails = getConflictedMeetingDetails(meetingId+"");
		jsonResultsArray = new JSONArray(conflictedMeetingDetails);
		String isError = "";
		for (int i = 0; i < jsonResultsArray.length(); i++) {
			JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
			isError = updateRecipientXML(Long .parseLong(jsonObject.getString("MeetingID")), userId, null, null, 3,
					null, null, null, null, null, null, 0);
		}
		return isError;
	}

	
	/**
	 * @Author : Sunil Verma
	 * @Action : Get all pending meeting by userId  who is in active state
	 * 
	 */
	@GET
	@Path("/getPendingMeetingRequests/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPendingMeetingRequests(@PathParam("userId") Long userId) {
        
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetPendingMeetingRequests(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			while (rs.next()) {
				
				MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId( rs.getLong("MeetingID"));
				
				if(meetingLogBean.getMeetingId() != null){
					
				JSONObject jsonObject = new JSONObject();
				LocalDateTime senderFromDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("meetingId" , rs.getLong("MeetingID"));
				jsonObject.put("meetingSenderId" , rs.getLong("SenderUserID"));
				jsonObject.put("date" , LocalDateTime.ofInstant(formatter.parse(rs.getString("SenderFromDateTime")).toInstant(), ZoneId.systemDefault()).toLocalDate());
				jsonObject.put("from" , senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to" , senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description" , rs.getString("MeetingDescription"));
				//jsonObject.put("meetingStatus" , rs.getLong("Status")); 
				
				if(rs.getString("Latitude") != null && ! rs.getString("Latitude").trim().isEmpty()){
					
					jsonObject.put("isLocationSelected",true);
					jsonObject.put("address" , rs.getString("GoogleAddress")+"");
					jsonObject.put("latitude" , rs.getString("Latitude"));
					jsonObject.put("longitude" , rs.getString("Longitude"));
				}else{
					jsonObject.put("isLocationSelected",false);
				}
				
				jsonResultsArray.put(jsonObject);
			 }
			}
			ServiceUtility.closeCallableSatetment(callableStatement);
			  JSONArray meetingArray = new JSONArray();
			 for (int i = 0; i < jsonResultsArray.length(); i++) {
				   JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
				   jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(jsonObject.getLong("meetingId")));
				   jsonObject.put("contactsJsonArray",  ServiceUtility.getContactByMeetingId(jsonObject.getLong("meetingId")));
				   jsonObject.put("friendsJsonArray", ServiceUtility.getReceptionistByMeetingId(jsonObject.getLong("meetingId") , userId).get("friendsArray"));
				   jsonObject.put("status", ServiceUtility.getMeetingStatusByUserId(jsonObject.getLong("meetingId") , userId));
				   
				   meetingArray.put(jsonObject);
			}
			finalJson.put("status", true);
			finalJson.put("message", "Pending Meeting Requests list fetched successfully");
			finalJson.put("meeting_array", meetingArray);
			return finalJson.toString();

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}
	/**
	 * @Author : Sunil Verma
	 * @Action : Get meeting details by meetingId
	 * 
	 */
	@GET
	@Path("/getMeetingDetailsByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByMeetingID(@PathParam("meetingId") Long  meetingId) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetMeetingDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				LocalDateTime senderFromDateTime = ServiceUtility
						.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility
						.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("description", rs.getString("MeetingDescription"));
				jsonObject.put("latitude", rs.getString("Latitude"));
				jsonObject.put("longitude", rs.getString("Longitude"));
				jsonObject.put("address", rs.getString("GoogleAddress"));
				jsonObject.put("date",ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime")).toLocalDate());
				jsonObject.put("from", senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to", senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description", rs.getString("MeetingDescription"));
			}
			jsonObject.put("status", true);
			jsonObject.put("message", "Meeting details fetched successfully");
			return jsonObject.toString();

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Something went wrong");
		return jsonObject.toString();
	}
	
	/**
	 * @Author : Sunil Verma
	 * @Action : Get meeting details by meetingId
	 * 
	 */
	@GET
	@Path("/getUserDetailsByMeetingID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserDetailsByMeetingID(@PathParam("meetingId") Long meetingId, @PathParam("userId") Long userId) {

		JSONObject jsonObject = new JSONObject();
		try {
			MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
			if(meetingLogBean.getMeetingId() != null){
				
				JSONObject meetingJson = ServiceUtility.checkMeetingAddressUpdateByMeetingId(meetingId);
				LocalDateTime meetingDateTime = ServiceUtility.convertStringToLocalDateTime(meetingJson.getString("senderFromDateTime"));
				LocalDateTime currentDateTime = LocalDateTime.now().plusHours(2);
				
				if(currentDateTime.isAfter(meetingDateTime)){
					 jsonObject.put("updateCount", meetingJson.getInt("updateCount"));	
				}
				
				 jsonObject.put("meetingId" , meetingLogBean.getMeetingId());
				 jsonObject.put("meetingSenderId" , meetingLogBean.getSenderUserId());
				 jsonObject.put("date" , meetingLogBean.getDate());
				 jsonObject.put("from" , meetingLogBean.getFromDate().getHour()+":"+meetingLogBean.getFromDate().getMinute());
				 jsonObject.put("to" , meetingLogBean.getToDate().getHour()+":"+meetingLogBean.getToDate().getMinute());
				 jsonObject.put("description" , meetingLogBean.getDescription());
				 if(meetingLogBean.getSenderUserId() != null && meetingLogBean.getSenderUserId() == userId){
					 jsonObject.put("meetingStatus" , 1); 
				 }
				if(meetingLogBean.getAddress() != null && !meetingLogBean.getAddress().trim().isEmpty()){
					
					jsonObject.put("isLocationSelected",true);
					jsonObject.put("address" , meetingLogBean.getAddress());
					jsonObject.put("latitude" , meetingLogBean.getLatitude());
					jsonObject.put("longitude" , meetingLogBean.getLongitude());
				}else{
					jsonObject.put("isLocationSelected",false);
				}
			 }else{
				jsonObject.put("status", true);
				jsonObject.put("message", "Meeting suspended"); 
				jsonObject.put("isMeetingExisted", false); 
				
				return jsonObject.toString();
			 }
			jsonObject.put("isMeetingExisted", true); 
			
			 JSONObject friendsObject = ServiceUtility.getReceiverDetailsByMeetingId(meetingId , userId);
			 JSONArray friendsArray = friendsObject.getJSONArray("friendsArray");
			 if(friendsArray.length() == 0 && meetingLogBean != null){
				 JSONObject jsonObject2 = new JSONObject();
				 jsonObject2.put("userId", meetingLogBean.getSenderUserId());
				 jsonObject2.put("fullName", meetingLogBean.getFullName());
				 jsonObject2.put("status", "1");
				 
				 friendsArray.put(jsonObject2);
			 }
			 if(! jsonObject.has("meetingStatus")){
				 jsonObject.put("meetingStatus" , friendsObject.get("status"));
			 }
			 jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(meetingId));
			 jsonObject.put("contactsJsonArray", ServiceUtility.getContactByMeetingId(meetingId));
			 jsonObject.put("friendsJsonArray", friendsArray);
		
			 
			 jsonObject.put("status", true);
			jsonObject.put("message", "Meeting Requests list fetched successfully");
		   return jsonObject.toString();	
		}catch (Exception e) {
			e.printStackTrace();
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Oops something went wrong");
		return jsonObject.toString();
	}
	
	
	@POST
	@Path("/storeMeetingLatLngByUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getMeetingSummary(String meetingDetails){
		
		System.out.println("meetingDetails============"+meetingDetails);
		
		JSONObject finalJson = new JSONObject();
		JSONObject meetingUserDetail = new JSONObject(meetingDetails);
			
		Long userId = meetingUserDetail.getLong("userId");
		Long meetingId = meetingUserDetail.getLong("meetingId");
		 Connection conn = null;
		 PreparedStatement pstmt = null;
		 int updatedRow = 0;
	     String sql = "" ;
	     MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
	     try{
		  	conn = DataSourceConnection.getDBConnection();
			if(meetingLogBean.getMeetingId() != null && meetingLogBean.getSenderUserId().equals(userId))
			{		
					 sql = "UPDATE tbl_MeetingDetails SET Latitude=?,Longitude=? WHERE MeetingID=? AND SenderUserID=?"; 
					 pstmt = conn.prepareStatement(sql);
					 pstmt.setString(1, meetingUserDetail.getString("latitude"));
					 pstmt.setString(2, meetingUserDetail.getString("longitude"));
					 pstmt.setLong(3, meetingId);
					 pstmt.setLong(4, userId);
						
				updatedRow = pstmt.executeUpdate();
						
			} else {
				sql = "UPDATE tbl_RecipientsDetails SET Latitude=?,Longitude=?,ResponseDateTime=? WHERE MeetingID=? AND UserID=?"; 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, meetingUserDetail.getString("latitude"));
				pstmt.setString(2, meetingUserDetail.getString("longitude"));
				pstmt.setString(3, new SimpleDateFormat("yyyy-dd-mm HH:mm:ss").format(new Date()));
				pstmt.setLong(4, meetingId);
				pstmt.setLong(5, userId);
				
				updatedRow =	pstmt.executeUpdate();
			}
			
			System.out.println("updatedRow == "+updatedRow);
			if(updatedRow == 0){
				finalJson.put("status", false);
				finalJson.put("message", "Something went wrong");
				return finalJson.toString();
			}
	     } catch (Exception e) {
			e.printStackTrace();
		  }finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		 }
	    try{

	     sql =	" SELECT M.MeetingID,M.Latitude,M.Longitude FROM tbl_MeetingDetails  AS M "
	     		+ " WHERE  M.MeetingID=? AND M.Latitude IS NOT NULL "
	     		+ " UNION ALL "
	     		+ " SELECT  R.MeetingID ,R.Latitude,R.Longitude "
	     		+ "FROM tbl_RecipientsDetails AS R "
	     		+ " WHERE  R.MeetingID=? AND R.Latitude IS NOT NULL";	
	     
	     conn = DataSourceConnection.getDBConnection();
	     pstmt = conn.prepareStatement(sql);
		 pstmt.setLong(1, meetingId);
		 pstmt.setLong(2, meetingId);
		 
		 ResultSet rs = pstmt.executeQuery();
		 
		 List<UserDTO> userAddressList = new ArrayList<UserDTO>();
		 
		 while(rs.next()){
			 
			 UserDTO userDTO = new UserDTO();
			 userDTO.setMeetingId(rs.getLong("MeetingID"));
			 userDTO.setLatitude(rs.getString("Latitude"));
			 userDTO.setLongitude(rs.getString("Longitude"));
			 
			 userAddressList.add(userDTO) ;
		 }
		 Set<Long> userIdSet = new HashSet<>();
		// LocalDateTime localDateTime = LocalDateTime.now().plusHours(2);
		// System.out.println(meetingLogBean.getFromDate().plusMinutes(2)+"================="+meetingLogBean.getFromDate().plusMinutes(2).isBefore(localDateTime)+"   localDateTime=    "+localDateTime);
		 //&& meetingLogBean.getFromDate().plusMinutes(2).isBefore(localDateTime)
		 System.out.println(" testing updated adress "+(userAddressList.size() >= 2 ));
		 
		 if(userAddressList.size() >= 2 ){
			 
		  JSONObject jsonObject	 = Geomagic.calculateMidPointLatLng(userAddressList);
		  JSONArray jsonArray = GoogleSearchPlaces.getGoogleSearchPlaces(jsonObject.getDouble("lat") , jsonObject.getDouble("lng") , meetingLogBean.getDescription());
		  Boolean isLocationsaved = GoogleSearchPlaces.storeFrissbiLocationsTemporary(jsonArray , meetingId);
		  if(isLocationsaved){
			  JSONArray frissbiLocationArray =  GoogleSearchPlaces.getFrissbiLocation(meetingId, 0); 
			  
			  System.out.println("frissbiLocationArray==================="+frissbiLocationArray.length());
			  JSONArray friendsIdArray  = ServiceUtility.getReceptionistDetailsByMeetingId(meetingId);
			  
			  if(friendsIdArray.length() != 0){
				  
				 for (int i = 0; i < friendsIdArray.length(); i++) {
					 if(friendsIdArray.getJSONObject(i).getInt("status") == 0){
						 userIdSet.add(friendsIdArray.getJSONObject(i).getLong("userId"));
					 }
				 }
				 userIdSet.add(meetingLogBean.getSenderUserId());
				 
				 String message = "Please share you location for the meeting "+meetingLogBean.getDescription()+" on "+meetingLogBean.getDate()+" from "+meetingLogBean.getStartTime()+" to "+meetingLogBean.getEndTime();
				 
				 NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				 notificationInfoDTO.setUserList(userIdSet.stream().collect(Collectors.toList()));
				 notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_LOCATION_SUGGESTION.toString());
				 notificationInfoDTO.setMeetingId(meetingId);
				 notificationInfoDTO.setMessage(message);
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("frissbiLocationArray", frissbiLocationArray);
				 if(frissbiLocationArray.length() <= 5){
					 jsonObject2.put("isNextLocationExist", false);
					}else{
						jsonObject2.put("isNextLocationExist", true);
					}
				 jsonObject2.put("isLocationUpdate", false);
				 notificationInfoDTO.setJsonObject(jsonObject2);
				 
				 NotificationService.sendMeetingNotification(notificationInfoDTO);
			  }
		  }
		  
		 }else{
			 finalJson.put("status", true);
			 finalJson.put("message", "Address updated successfully");
			 return finalJson.toString(); 
		 }
	     } catch(Exception e){
	    	 e.printStackTrace();
	     }finally{
	    	 ServiceUtility.closeConnection(conn);
	    	 ServiceUtility.closeSatetment(pstmt);
	     }
		return finalJson.toString();
	}

	@POST
	@Path("/updateMeetingAddress")
	@Consumes(MediaType.APPLICATION_JSON)
	public String updateMeetingAddress(String addressDetails){
		
		JSONObject addressJsonObject = new JSONObject(addressDetails);
        System.out.println("addressJsonObject==========================="+addressJsonObject.toString());
        
		JSONObject finalJson = new JSONObject();
		
		Connection connection = null;
		CallableStatement callableStatement = null;
		try{
			Long meetingId = addressJsonObject.getLong("meetingId");
			JSONObject jsonObject = ServiceUtility.checkMeetingAddressUpdateByMeetingId(meetingId);
			Integer updateCount = jsonObject.getInt("updateCount");
			
			 if(! addressJsonObject.getBoolean("isFromMeetingSummary")){
				 if(updateCount != 0){
					finalJson.put("status", true);	
					finalJson.put("isLocationUpdate", true);	
					finalJson.put("meetingId", meetingId);	
					finalJson.put("message", "Location is already decided");	
					return finalJson.toString(); 
				 }
			 }else{
				if(updateCount == 2){
					finalJson.put("meetingId", meetingId);	
					finalJson.put("isLocationUpdate", true);	
					finalJson.put("meetingId", meetingId);	
					finalJson.put("message", "Location is already decided");	
					return finalJson.toString();
				}
			 }
			System.out.println("updateCount==================="+updateCount);
				
				connection = DataSourceConnection.getDBConnection();
				String insertStoreProc = "{call usp_UpdateMeetingAddress(?,?,?,?,?,?)}";
				callableStatement = connection.prepareCall(insertStoreProc);
				callableStatement.setLong(1, meetingId);
				callableStatement.setString(2, addressJsonObject.getString("latitude"));
				callableStatement.setString(3,  addressJsonObject.getString("longitude"));
				callableStatement.setString(4, addressJsonObject.getString("address"));
				callableStatement.setLong(5, (updateCount+1));
				callableStatement.registerOutParameter(6, Types.INTEGER);
				
				int value = callableStatement.executeUpdate();
				int isError = callableStatement.getInt(6);
				
				if(isError == 0 && value != 0){
					
					
					MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
					JSONArray usersArray = ServiceUtility.getReceptionistDetailsByMeetingId( meetingId);
					
					Set<Long> usersSet = new HashSet<Long>();
					for (int i = 0; i < usersArray.length(); i++) {
						usersSet.add(usersArray.getJSONObject(i).getLong("userId"));
					}
					System.out.println("usersSet==============="+usersSet.size());
					if(! usersSet.isEmpty()){
						
						if(meetingLogBean.getSenderUserId() != null){
							usersSet.add(meetingLogBean.getSenderUserId());
						}
						
						jsonObject.put("isLocationUpdate", true);
						jsonObject.put("address" , meetingLogBean.getAddress());
						jsonObject.put("latitude" , meetingLogBean.getLatitude());
						jsonObject.put("longitude" , meetingLogBean.getLongitude());
						String message = "For Meeting "+meetingLogBean.getDescription() +" location is confirmed which is on "+meetingLogBean.getDate()+" from "+meetingLogBean.getStartTime()+" to "+meetingLogBean.getEndTime();
						
						NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
						notificationInfoDTO.setUserList(usersSet.stream().collect(Collectors.toList()));
						notificationInfoDTO.setMeetingId(meetingLogBean.getMeetingId());
						notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_LOCATION_SUGGESTION.toString());
						notificationInfoDTO.setMessage(message);
						notificationInfoDTO.setJsonObject(jsonObject);
						
						NotificationService.sendMeetingNotification(notificationInfoDTO);
					}
					finalJson.put("status", true);	
					finalJson.put("meetingId", meetingId);	
					finalJson.put("message", "Meeting address updated and selected");	
					return finalJson.toString();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(connection);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);	
		finalJson.put("message", "Something went wrong ");	
		return finalJson.toString();
	}
	
	
	
    @GET	
	@Path("/getFrissbiLocations/{meetingId}/{offSetValue}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMoreFrissbiLocations(@PathParam("meetingId") Long meetingId , @PathParam("offSetValue") int offSetValue){
		
		JSONObject finalJson = new JSONObject();
		try{
			JSONArray  frissbiLocations = GoogleSearchPlaces.getFrissbiLocation(meetingId , offSetValue);
			
			if(frissbiLocations.length() <= 5){
				finalJson.put("isNextLocationExist", false);
			}else{
				finalJson.put("isNextLocationExist", true);
			}
			finalJson.put("frissbiLocationArray", frissbiLocations);
			finalJson.put("status", true);	
			finalJson.put("message", "Frissbi location fetched successfully");	
		 return finalJson.toString();
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);	
		finalJson.put("message", "Something went wrong ");	
		return finalJson.toString();
	}
	
	
	/*//logic for checking the source and destination  distance wrt Time
	if(! timePostedFriendList.isEmpty()){
		
		Float startSubTime = 0f;
		Float endSubTime = 0f;
		 
		java.util.Map<String , UserDTO> map = new HashMap<String , UserDTO>();
		
		for (UserDTO userDTO : timePostedFriendList) {
			
			if(meetingStartTime > userDTO.getEndTime()){
				if(startSubTime == 0f){
					startSubTime = userDTO.getEndTime();
					map.put("startDto", userDTO)	;
				}else if(startSubTime < userDTO.getEndTime()){
					startSubTime = userDTO.getEndTime();
					map.put("startDto", userDTO)	;
				}
				
			}
			
			if(meetingEndTime < userDTO.getStartTime()){
				
				if(endSubTime == 0f){
					endSubTime = userDTO.getStartTime();
					map.put("endDto", userDTO)	;
				}else if(endSubTime < userDTO.getStartTime()){
					endSubTime = userDTO.getStartTime();
					map.put("endDto", userDTO)	;
				}
			}
		}
		
		//logic for the checking the checking the source and destination  distance wrt Time
		if(! map.isEmpty()){
			if(map.get("startDto") != null){
			int startTimeToBeTaken = ServiceUtility.calculateTimeBetweenLatLng(Float.parseFloat(map.get("startDto").getLatitude()), Float.parseFloat(map.get("startDto").getLatitude()), Float.parseFloat(meetingInsertionObject.getString("latitude")), Float.parseFloat(meetingInsertionObject.getString("longitude")));
			
			if(startTimeToBeTaken != 0 && startTimeToBeTaken > map.get("startDto").getMeetingFromTime().getHour()+map.get("startDto").getMeetingFromTime().getMinute()){
			
				finalJson.put("status", true);
				finalJson.put("message", "Meeting from "+ senderFromDateTime.getHour()+":"+senderToDateTime.getMinute()+" to "+senderToDateTime.getHour()+":"+senderToDateTime.getMinute() +" will take more time "+map.get("startDto").getMeetingFromTime().getHour()+":"+map.get("startDto").getMeetingFromTime().getMinute()+" to "+map.get("startDto").getMeetingFromTime().getHour()+":"+map.get("startDto").getMeetingFromTime().getMinute()+" on Date "+ senderFromDateTime.toLocalDate());
				
				return finalJson.toString();	
				
			}
			
			}
			if(map.get("endDto") != null){
				
				int endTimeToBeTaken = ServiceUtility.calculateTimeBetweenLatLng(Float.parseFloat(map.get("endDto").getLatitude()), Float.parseFloat(map.get("endDto").getLatitude()), Float.parseFloat(meetingInsertionObject.getString("latitude")), Float.parseFloat(meetingInsertionObject.getString("longitude")));
				
				if(endTimeToBeTaken != 0 && endTimeToBeTaken > map.get("endDto").getMeetingToTime().getHour()+map.get("endDto").getMeetingToTime().getMinute()){
					
					finalJson.put("status", true);
					finalJson.put("message", "Meeting from "+ senderFromDateTime.getHour()+":"+senderToDateTime.getMinute()+" to "+senderToDateTime.getHour()+":"+senderToDateTime.getMinute() +" will take more time "+map.get("endDto").getMeetingFromTime().getHour()+":"+map.get("endDto").getMeetingFromTime().getMinute()+" to "+map.get("endDto").getMeetingFromTime().getHour()+":"+map.get("endDto").getMeetingFromTime().getMinute()+" on Date "+ senderFromDateTime.toLocalDate());
					
					return finalJson.toString();	
					
				}
			}
		}
		
	}*/
	
}
