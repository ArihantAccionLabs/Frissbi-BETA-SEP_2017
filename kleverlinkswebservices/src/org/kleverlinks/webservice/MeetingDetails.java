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
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.MeetingLogBean;
import org.service.dto.NotificationInfoDTO;
import org.service.dto.UserDTO;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

import com.sun.jersey.api.client.WebResource;

@Path("MeetingDetailsService")
public class MeetingDetails {

	@POST
	@Path("/insertMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertMeetingDetails(String meetingInsertion) {
		JSONObject finalJson = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		System.out.println("meetingInsertion==========" + meetingInsertion);
		try {
			JSONObject meetingInsertionObject = new JSONObject(meetingInsertion);
			int senderUserId = meetingInsertionObject.getInt("senderUserId");
			 if(meetingInsertionObject.has("meetingIdsJsonArray")){
				ServiceUtility.deleteUserFromMeeting(meetingInsertionObject.getJSONArray("meetingIdsJsonArray") , senderUserId);
				//sending Notification
				System.out.println("NotificationsEnum.Meeting_Rejected.ordinal()+1==="+NotificationsEnum.Meeting_Rejected);
				NotificationService.sendNotification(meetingInsertionObject.getJSONArray("meetingIdsJsonArray"), senderUserId, NotificationsEnum.Meeting_Rejected.ordinal());
			}
			String meetingDate = meetingInsertionObject.getString("meetingDateTime");
			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm a");
			String durationTime = meetingInsertionObject.getString("duration");
			String[] timeArray = durationTime.split(":");
			String duration = timeArray[0] + ":" + timeArray[1] + ":00";
			LocalDateTime senderFromDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(),ZoneId.systemDefault());
			LocalDateTime senderToDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(), ZoneId.systemDefault()).plusHours(Integer.parseInt(timeArray[0])).plusMinutes(Integer.parseInt(timeArray[1]));

			/* If meeting id is not there means we need to check that is there
			 any meeting is there at same date And time on both table in
			 tbl_RecipientsDetails & in tbl_MeetingDetails  */
			if (!meetingInsertionObject.has("meetingIdsJsonArray")) {
				List<UserDTO> userDTOList = null;
				userDTOList = ServiceUtility.checkingMeetingConfliction(meetingInsertionObject.getInt("senderUserId"),
						meetingDate, senderFromDateTime, senderToDateTime);
				if (!userDTOList.isEmpty()) {
					
					if(userDTOList.size() > 1){
						for (UserDTO userDTO : userDTOList) {
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("from", userDTO.getMeetingFromTime().getHour() + ":"+ userDTO.getMeetingFromTime().getMinute());
							jsonObject.put("to",userDTO.getMeetingToTime().getHour() + ":" + userDTO.getMeetingToTime().getMinute());
							jsonObject.put("meetingId", userDTO.getMeetingId());
							
							jsonArray.put(jsonObject);
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
					return finalJson.toString();
				}
			}
			Connection connection = null;
			connection = DataSourceConnection.getDBConnection();
			CallableStatement callableStatement = null;
			int meetingId = 0;

			String insertStoreProc = "{call usp_InsertMeetingDetails(?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = connection.prepareCall(insertStoreProc);
			callableStatement.setInt(1, senderUserId);
			callableStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
			callableStatement.setTimestamp(3, new Timestamp(formatter.parse(meetingDate).getTime()));
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
			callableStatement.registerOutParameter(10, Types.INTEGER);
			callableStatement.registerOutParameter(11, Types.BIGINT);

			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(10);
			meetingId = callableStatement.getInt(11);
			System.out.println(isError + "meetingId >>>>>>>>>>>>>>>>>> " + meetingId+"  value=="+value);

			if (isError == 0) {

				JSONArray jsonArrayData = meetingInsertionObject.getJSONArray("friendsIdJsonArray");
				// System.out.println(">>>>>>>>>>>>>" + jsonArrayData.length());

				connection.setAutoCommit(false);
				PreparedStatement ps = null;
				String query = "INSERT into tbl_RecipientsDetails(MeetingID,UserID,Status,RecipientFromDateTime,RecipientToDateTime,Latitude,Longitude,DestinationType,GoogleAddress) values(?,?,?,?,?,?,?,?,?)";
				ps = connection.prepareStatement(query);

				for (int i = 0; i < jsonArrayData.length(); i++) {

					ps.setInt(1, meetingId);
					ps.setInt(2, jsonArrayData.getInt(i));
					ps.setString(3, "0");
					ps.setTimestamp(4, null);
					ps.setTimestamp(5, null);
					ps.setString(6, null);
					ps.setString(7, null);
					ps.setInt(8, 0);
					ps.setString(9, null);

					ps.addBatch();

				}
				int[] insertedRow = ps.executeBatch();
				/*for (int i = 0; i < insertedRow.length; i++) {
					 System.out.println("insertedRow[i]========="+insertedRow[i]);
				}*/
				connection.commit();
				// return Response.status(201).entity(i).build();
				if (insertedRow != null) {
					// sending mails to meeting creator
					try {
						ServiceUtility.insertingAndSendingMails(meetingInsertionObject.getJSONArray("emailIdJsonArray"),
								senderUserId, meetingId);
					} catch (Exception e) {
						e.printStackTrace();
						finalJson.put("status", false);
						finalJson.put("isInserted", false);
						finalJson.put("message", "Mail not sent");

						return finalJson.toString();
					}
					// Inserting the EmailIds of the user who is not on the
					// Fissbi and sending the emails also
					NotificationService.sendingMeetingCreationNotification(meetingInsertionObject, meetingId);

					// Inserting the contact number of the user who is not on
					// the Fissbi
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
			callableStatement.setInt(1, meetingId);
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
			callableStatement.setInt(1, meetingId);
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
	public String updateRecipientXML(@PathParam("meetingId") int meetingId, @PathParam("recipientId") int recipientId,
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
			callableStatement.setInt(1, meetingId);
			callableStatement.setString(2, xmlString);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3) + "";

			if (isError.equals("0")) {
				String meetingDetails = getMeetingDetailsByMeetingID(meetingId);
				JSONArray jsonArray = new JSONArray(meetingDetails);
				int senderUserId = 0;
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					senderUserId = Integer.parseInt(jsonObject.getString("SenderUserID"));
				}
				UserNotifications userNotifications = new UserNotifications();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				if (status == 1) {
					String notificationId = userNotifications.insertUserNotifications(senderUserId, recipientId,
							NotificationsEnum.Meeting_Request_Acceptance.ordinal() + 1, 0, timestamp);
					JSONObject json = new JSONArray(
							userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)))
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
							NotificationsEnum.Meeting_Rejected.ordinal() + 1, 0, timestamp);
					JSONObject json = new JSONArray(
							userNotifications.getUserNotifications(0, Integer.parseInt(notificationId)))
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
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, meetingId);
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
	public String getRecipientDetailsByMeetingID(@PathParam("meetingId") int meetingId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
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

	@GET
	@Path("/getMeetingDetailsByUserID/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByUserID(@PathParam("userId") int userId, @PathParam("fromDate") Timestamp fromDate,
			@PathParam("toDate") Timestamp toDate) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetMeetingDetails_ByUserID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setTimestamp(2, fromDate);
			callableStatement.setTimestamp(3, toDate);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("RequestDateTime", rs.getString("RequestDateTime"));
				jsonObject.put("SenderFromDateTime", rs.getString("SenderFromDateTime"));
				jsonObject.put("SenderToDateTime", rs.getString("SenderToDateTime"));
				jsonObject.put("LocationID", rs.getString("LocationID"));
				jsonObject.put("ScheduledTimeSlot", rs.getString("ScheduledTimeSlot"));
				jsonObject.put("MeetingDescription", rs.getString("MeetingDescription"));
				jsonObject.put("Latitude", rs.getString("Latitude"));
				jsonObject.put("Longitude", rs.getString("Longitude"));
				jsonObject.put("olatitude", rs.getString("olatitude"));
				jsonObject.put("oLongitude", rs.getString("oLongitude"));
				jsonObject.put("UserPreferredLocationID", rs.getString("UserPreferredLocationID"));
				jsonObject.put("GeoDateTime", rs.getString("GeoDateTime"));
				if (rs.getString("GoogleAddress") == null) {
					jsonObject.put("GoogleAddress", "");
				} else {
					jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				}
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
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

	@POST
	@Path("/getConflictedMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getConflictedMeetingDetails(String meetingDetails) {
		
		System.out.println("==============="+meetingDetails.toString());
        JSONObject meetingDetailsJsonObject = new JSONObject(meetingDetails);
        int meetingId = meetingDetailsJsonObject.getInt("meetingId");
        
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		CallableStatement callableStatement = null;
		Connection conn = null;
		try {
			 if(meetingDetailsJsonObject.has("isAccepted") && meetingDetailsJsonObject.getBoolean("isAccepted")){
			     Map<String , Date> map = ServiceUtility.getOneDayDate(meetingDetailsJsonObject.getString("meetingDate"));
			    conn = DataSourceConnection.getDBConnection();
				String selectStoreProcedue = "{call usp_CheckingConflicatedMeetings(?,?,?)}";
				System.out.println("from =="+new Timestamp(map.get("today").getTime())+"==="+new Timestamp(map.get("today").getTime()));
				callableStatement = conn.prepareCall(selectStoreProcedue);
				callableStatement.setInt(1, meetingDetailsJsonObject.getInt("userId"));
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
					userDto.setMeetingId(rs.getInt("MeetingID"));
					userDto.setUserId(rs.getInt("SenderUserID"));
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
             System.out.println("=========================="+meetingList.size());
			// logic for avoiding the time collapse b/w meetings
			for (UserDTO userDTO : meetingList) {
				
				System.out.println(userDTO.getStartTime()+"="+userDTO.getEndTime()+"==="+meetingDto.getStartTime()+"==="+meetingDto.getEndTime());
				
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
			
			//accepting the meeting request by updating the tbl_reception table
			if(MeetingRequestUpdate(meetingId , meetingDetailsJsonObject.getInt("userId") , meetingDetailsJsonObject.getInt("meetingStatus")) != 0 ){
				
				//sending meeting notification to creator of meeting  //NotificationsEnum.Meeting_Pending_Requests.ordinal() + 1
				NotificationService.sendNotificationToOneUser(meetingDto.getUserId() , meetingDetailsJsonObject.getInt("userId") , NotificationsEnum.Friend_Request_Acceptance.ordinal()+1 , meetingDto.getMeetingId());
				
				finalJson.put("isAccepted", true);
				finalJson.put("status", true);
				finalJson.put("message", "Meeting accepted successfully");
				return finalJson.toString();
			}
        }else if(meetingDetailsJsonObject.has("isRejected") && meetingDetailsJsonObject.getBoolean("isRejected")){
        	if(MeetingRequestUpdate(meetingId , meetingDetailsJsonObject.getInt("userId") , meetingDetailsJsonObject.getInt("meetingStatus")) != 0 ){
        		//ServiceUtility.sendNotificationToOneUser(meetingDto.getUserId() , meetingDetailsJsonObject.getInt("userId") , NotificationsEnum.Friend_Request_Acceptance.ordinal()+1 , meetingDto.getMeetingId());
        		System.out.println("if=======================");
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
	
	public int MeetingRequestUpdate(int meetingId , int userId , int status){
		
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		int updatedRow = 0;
		try{
			String sql = "UPDATE tbl_RecipientsDetails SET Status=?,ResponseDateTime=? WHERE MeetingID=? AND UserID=?"; 
			conn = DataSourceConnection.getDBConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, status);
			pstmt.setInt(2, meetingId);
			pstmt.setInt(3, userId);
			pstmt.setString(4, new SimpleDateFormat("yyyy-dd-mm HH:mm:ss").format(new Date()));
		 updatedRow =	pstmt.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("MeetingRequestUpdate============================="+updatedRow);
		return updatedRow;
	}
	
	@GET
	@Path("/updateConflictedMeetingDetails/{meetingId}/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateConflictedMeetingDetails(@PathParam("meetingId") int meetingId, @PathParam("userId") int userId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate) throws JSONException, ParseException {

		JSONArray jsonResultsArray = new JSONArray();
		String conflictedMeetingDetails = getConflictedMeetingDetails(meetingId+"");
		jsonResultsArray = new JSONArray(conflictedMeetingDetails);
		String isError = "";
		for (int i = 0; i < jsonResultsArray.length(); i++) {
			JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
			isError = updateRecipientXML(Integer.parseInt(jsonObject.getString("MeetingID")), userId, null, null, 3,
					null, null, null, null, null, null, 0);
		}
		return isError;
	}

	@GET
	@Path("/getMeetingSummary/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingSummary(@PathParam("meetingId") int meetingId) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject json = new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetMeetingSummaryDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();
			String SenderFromDateTime = "";
			String SenderToDateTime = "";
			String ScheduledTimeSlot = "";
			String MeetingDescription = "";
			String Latitude = "";
			String Longitude = "";
			String DestinationAddress = "";

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				if (rs.getString("AvatarPath") == null) {
					jsonObject.put("AvatarPath", "");
				} else {
					jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
				}
				SenderFromDateTime = rs.getString("SenderFromDateTime");
				SenderToDateTime = rs.getString("SenderToDateTime");
				ScheduledTimeSlot = rs.getString("ScheduledTimeSlot");
				MeetingDescription = rs.getString("MeetingDescription");
				Latitude = rs.getString("Latitude");
				Longitude = rs.getString("Longitude");
				DestinationAddress = rs.getString("DestinationAddress");
				jsonResultsArray.put(jsonObject);
			}
			json.put("Recipients", jsonResultsArray);
			json.put("SenderFromDateTime", SenderFromDateTime);
			json.put("SenderToDateTime", SenderToDateTime);
			json.put("ScheduledTimeSlot", ScheduledTimeSlot);
			json.put("MeetingDescription", MeetingDescription);
			json.put("Latitude", Latitude);
			json.put("Longitude", Longitude);
			json.put("DestinationAddress", DestinationAddress);
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);		
		}
		return json.toString();
	}

	@GET
	@Path("/getPendingMeetingRequests/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPendingMeetingRequests(@PathParam("userId") int userId) {
        
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetPendingMeetingRequests(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				LocalDateTime senderFromDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("meetingId" , rs.getInt("MeetingID"));
				jsonObject.put("meetingSenderId" , rs.getInt("SenderUserID"));
				jsonObject.put("date" , LocalDateTime.ofInstant(formatter.parse(rs.getString("SenderFromDateTime")).toInstant(), ZoneId.systemDefault()).toLocalDate());
				jsonObject.put("from" , senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to" , senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description" , rs.getString("MeetingDescription"));
				//jsonObject.put("meetingStatus" , rs.getInt("Status")); 
				
				if(rs.getString("Latitude") != null && rs.getString("Latitude").trim().isEmpty()){
					
					jsonObject.put("isLocationSelected",true);
					jsonObject.put("address" , rs.getString("GoogleAddress"));
					jsonObject.put("latitude" , rs.getString("Latitude"));
					jsonObject.put("longitude" , rs.getString("Longitude"));
				}else{
					jsonObject.put("isLocationSelected",false);
				}
				
				jsonResultsArray.put(jsonObject);
			}
			ServiceUtility.closeCallableSatetment(callableStatement);
			  JSONArray meetingArray = new JSONArray();
			 for (int i = 0; i < jsonResultsArray.length(); i++) {
				   JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
				   jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(jsonObject.getInt("meetingId")));
				   jsonObject.put("contactsJsonArray",  ServiceUtility.getContactByMeetingId(jsonObject.getInt("meetingId")));
				   jsonObject.put("friendsJsonArray", ServiceUtility.getReceptionistByMeetingId(jsonObject.getInt("meetingId") , userId).get("friendsArray"));
				   jsonObject.put("status", ServiceUtility.getMeetingStatusByUserId(jsonObject.getInt("meetingId") , userId));
				   
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

	@GET
	@Path("/getMeetingDetailsByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByMeetingID(@PathParam("meetingId") int meetingId) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetMeetingDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				LocalDateTime senderFromDateTime = ServiceUtility
						.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility
						.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("description", rs.getString("MeetingDescription"));
				jsonObject.put("Latitude", rs.getString("Latitude"));
				jsonObject.put("Longitude", rs.getString("Longitude"));
				jsonObject.put("address", rs.getString("GoogleAddress"));
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
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

	@GET
	@Path("/getUserDetailsByMeetingID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserDetailsByMeetingID(@PathParam("meetingId") int meetingId, @PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			//callableStatement.setInt(1, userId);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();
			JSONArray jsonResultsArray = new JSONArray();
			while (rs.next()) {
				
				LocalDateTime senderFromDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("date", LocalDateTime.ofInstant(formatter.parse(rs.getString("SenderFromDateTime")).toInstant(), ZoneId.systemDefault()).toLocalDate());
				jsonObject.put("from", senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to", senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description", rs.getString("MeetingDescription"));
				jsonObject.put("meetingStatus", rs.getString("Status"));
				
				jsonResultsArray.put(jsonObject);
			}
			
			jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(meetingId));
			jsonObject.put("contactsJsonArray", ServiceUtility.getContactByMeetingId(meetingId));
			jsonObject.put("friendsJsonArray", ServiceUtility.getReceptionistByMeetingId(meetingId , userId).get("friendsArray"));
			
			jsonObject.put("status", true);
			jsonObject.put("message", "Meeting Requests list fetched successfully");
		   return jsonObject.toString();	
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);	
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Oops something went wrong");
		return jsonObject.toString();
	}
	
	@POST
	@Path("/getMeetingSummary")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getMeetingSummary(String meetingDetails){
		
		System.out.println("meetingDetails============"+meetingDetails);
		
		JSONObject finalJson = new JSONObject();
		JSONObject meetingUserDetail = new JSONObject(meetingDetails);
	 try {
		int userId = meetingUserDetail.getInt("userId");
		int meetingId = meetingUserDetail.getInt("meetingId");
		if(meetingUserDetail.getBoolean("isLocationSelected")){
			
			MeetingLogBean meetingCreatorLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId) ;
			JSONArray  jsonArray = ServiceUtility.getReceptionistDetailsByMeetingId(meetingId) ;
			Set<Integer> userIdSet = new HashSet<>();
			for (int i = 0; i < jsonArray.length(); i++) {
				if(jsonArray.getJSONObject(i).getInt("userId") != 0){
					userIdSet.add(jsonArray.getJSONObject(i).getInt("userId"));
				}
			}
			userIdSet.add(userId);
			if(meetingCreatorLogBean.getUserId() != null){
				userIdSet.add(meetingCreatorLogBean.getUserId());
			}
			System.out.println(userIdSet.toString()+" listc :   "+userIdSet.size());
			if(!userIdSet.isEmpty()){
				
				String message = "You have meeting "+meetingCreatorLogBean.getDescription() +" with "+meetingCreatorLogBean.getFullName()+" on "+meetingCreatorLogBean.getDate() +" from "+meetingCreatorLogBean.getFrom()+" to "+meetingCreatorLogBean.getTo();
				
				NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				notificationInfoDTO.setMeetingId(meetingId);
				notificationInfoDTO.setSenderUserId(0);
				notificationInfoDTO.setNotificationType(NotificationsEnum.Meeting_Summary.toString());
				notificationInfoDTO.setMessage(message);
				
				NotificationService.sendMeetingAlarmNotification(notificationInfoDTO, userIdSet.stream().collect(Collectors.toList())); 
			}
			/*finalJson.put("meetingID", meetingId);
			finalJson.put("date", meetingCreatorLogBean.getDate());
			finalJson.put("from", meetingCreatorLogBean.getFrom());
			finalJson.put("to", meetingCreatorLogBean.getTo());
			finalJson.put("description", meetingCreatorLogBean.getDescription());
			finalJson.put("address", meetingCreatorLogBean.getAddress());
			finalJson.put("latitude", meetingCreatorLogBean.getLatitude());
			finalJson.put("longitude", meetingCreatorLogBean.getLongitude());
			finalJson.put("friendsAcceptedArray", jsonArray);
			finalJson.put("status", true);
			finalJson.put("message", "Data fetched successfully");*/
			
			finalJson.put("isLocationSelected", meetingUserDetail.getBoolean("isLocationSelected"));
			return finalJson.toString();
		}
	 } catch (Exception e) {
		e.printStackTrace();
	}
		finalJson.put("status", true);
		finalJson.put("message", "Location does not exist ");
		finalJson.put("isLocationSelected", meetingUserDetail.getBoolean("isLocationSelected"));
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
