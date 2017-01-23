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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.webservice.gcm.Message;
import org.kleverlinks.webservice.gcm.Result;
import org.kleverlinks.webservice.gcm.Sender;
import org.service.dto.UserDTO;
import org.util.service.ServiceUtility;

import com.sun.jersey.api.client.WebResource;

@Path("MeetingDetailsService")
public class MeetingDetails {
	
	

	@POST
	@Path("/insertMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String createTrackInJSON(String meetingInsertion) {
        JSONObject finalJson = new JSONObject();
		try {
			
			JSONObject meetingInsertionObject = new JSONObject(meetingInsertion);
			if(meetingInsertionObject.has("meetingId") && meetingInsertionObject.getInt("meetingId") != 0){
				int meetingId = meetingInsertionObject.getInt("meetingId");
				System.out.println(meetingInsertionObject.getInt("senderUserId")+"==<---SenderId====meetingId========================"+meetingId);
				ServiceUtility.deleteUserFromMeeting(meetingId , meetingInsertionObject.getInt("senderUserId"));
			}
			String meetingDate = meetingInsertionObject.getString("meetingDateTime");
			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm a");
			String durationTime = meetingInsertionObject.getString("duration");
			String []timeArray = durationTime.split(":");
			String duration = timeArray[0]+":"+timeArray[1]+":00";
			LocalDateTime senderFromDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(),ZoneId.systemDefault());
			LocalDateTime senderToDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(),ZoneId.systemDefault()).plusHours(Integer.parseInt(timeArray[0])).plusMinutes(Integer.parseInt(timeArray[1]));
			
			//Testing 
			
			if(!(meetingInsertionObject.has("meetingId") && meetingInsertionObject.getInt("meetingId") != 0)){
				
			  System.out.println(senderFromDateTime+"====doSomething==========Testing="+senderToDateTime);
	           	PreparedStatement pstmt = null;
				String sql = "SELECT * FROM tbl_MeetingDetails WHERE SenderUserID=? AND SenderFromDateTime BETWEEN ? AND ? ORDER BY MeetingID" ;
				 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			        Calendar now = Calendar.getInstance();
			        now.setTime(formatter.parse(meetingDate));
			        now.set(Calendar.HOUR, 0);
			        now.set(Calendar.MINUTE, 0);
			        now.set(Calendar.SECOND, 0);
			        now.set(Calendar.HOUR_OF_DAY, 0);
			        
			        Calendar tomorrow = Calendar.getInstance();
			        tomorrow.setTime(now.getTime());
			        tomorrow.add(Calendar.DATE, 1);
			      
				pstmt = DataSourceConnection.getDBConnection().prepareStatement(sql);
				pstmt.setInt(1, meetingInsertionObject.getInt("senderUserId"));
				pstmt.setString(2, sdf.format(now.getTime()));
				pstmt.setString(3, sdf.format(tomorrow.getTime()));

				ResultSet rs = pstmt.executeQuery();
				List<UserDTO> timePostedFriendList = new ArrayList<UserDTO>();
				while (rs.next()) {
					//System.out.println("MeetingID====="+rs.getInt("MeetingID")+"  SenderFromDateTime=="+rs.getTimestamp("SenderFromDateTime")+"===="+rs.getTimestamp("SenderToDateTime"));
				
					UserDTO userDto = new UserDTO();
					userDto.setMeetingId(rs.getInt("MeetingID"));
					userDto.setUserId(rs.getInt("SenderUserID"));
					LocalDateTime fromTime = LocalDateTime.ofInstant(rs.getTimestamp("SenderFromDateTime").toInstant(),ZoneId.systemDefault());
					LocalDateTime toTime = LocalDateTime.ofInstant(rs.getTimestamp("SenderToDateTime").toInstant(),ZoneId.systemDefault());
					userDto.setMeetingFromTime(fromTime);
					userDto.setMeetingToTime(toTime);
					userDto.setStartTime(Float.parseFloat(fromTime.getHour()+"."+fromTime.getMinute()));
					userDto.setEndTime(Float.parseFloat(toTime.getHour()+"."+toTime.getMinute()));
					userDto.setLatitude(rs.getString("latitude"));
					userDto.setLongitude(rs.getString("longitude"));
					
					timePostedFriendList.add(userDto);
					
				}
				
				//logic for avoiding the time collapse b/w meetings
				Float meetingStartTime = Float.parseFloat(senderFromDateTime.getHour()+"."+senderFromDateTime.getMinute());
				Float meetingEndTime = Float.parseFloat(senderToDateTime.getHour()+"."+senderToDateTime.getMinute());
	           
			 System.out.println(meetingStartTime+"====="+meetingEndTime+"    "+timePostedFriendList.size());
			for (UserDTO userDTO : timePostedFriendList) {
				if((userDTO.getStartTime() < meetingStartTime && meetingStartTime < userDTO.getEndTime()) || (userDTO.getStartTime() < meetingEndTime && meetingEndTime < userDTO.getEndTime())){
					
					finalJson.put("status", true);
					finalJson.put("meetingId" , userDTO.getMeetingId());
					finalJson.put("message", "Meeting from "+ senderFromDateTime.getHour()+":"+senderToDateTime.getMinute()+" to "+senderToDateTime.getHour()+":"+senderToDateTime.getMinute() +" is conflicting with meeting "+userDTO.getMeetingFromTime().getHour()+":"+userDTO.getMeetingFromTime().getMinute()+" to "+userDTO.getMeetingToTime().getHour()+":"+userDTO.getMeetingToTime().getMinute()+" on Date "+ senderFromDateTime.toLocalDate());
					
					return finalJson.toString();
							
				}
				
			}
		}//checking meetingId if block closed
			Connection connection = null;
			connection = DataSourceConnection.getDBConnection();
			CallableStatement callableStatement = null;
			int meetingId = 0;
			
				String insertStoreProc = "{call usp_InsertMeetingDetails(?,?,?,?,?,?,?,?,?,?,?)}";
				callableStatement = connection.prepareCall(insertStoreProc);
				callableStatement.setInt(1, meetingInsertionObject.getInt("senderUserId"));
				callableStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
				callableStatement.setTimestamp(3, new Timestamp(formatter.parse(meetingDate).getTime()));
				callableStatement.setTimestamp(4, Timestamp.valueOf(senderToDateTime));
				callableStatement.setTime(5, java.sql.Time.valueOf(duration));
				callableStatement.setString(6, meetingInsertionObject.getString("meetingTitle"));
				callableStatement.setString(7, meetingInsertionObject.getString("latitude"));
				callableStatement.setString(8, meetingInsertionObject.getString("longitude"));
				callableStatement.setString(9, meetingInsertionObject.getString("address"));
				callableStatement.registerOutParameter(10, Types.INTEGER);
				callableStatement.registerOutParameter(11, Types.BIGINT);
				
				int value = callableStatement.executeUpdate();
				int isError = callableStatement.getInt(10);
				meetingId = callableStatement.getInt(11);
				System.out.println(isError+"meetingId >>>>>>>>>>>>>>>>>> " + meetingId);

				
				JSONArray jsonArrayData = meetingInsertionObject.getJSONArray("friendsIdJsonArray");
				//System.out.println(">>>>>>>>>>>>>" + jsonArrayData.length());

				connection.setAutoCommit(false);
				PreparedStatement ps = null;
				String query = "INSERT into tbl_RecipientsDetails(MeetingID,UserID,Status,RecipientFromDateTime,RecipientToDateTime,Latitude,Longitude,DestinationType,GoogleAddress) values(?,?,?,?,?,?,?,?,?)";
				ps = connection.prepareStatement(query);
				
				  for (int i = 0; i < jsonArrayData.length(); i++) {
				     
					  ps.setInt(1, meetingId);
					  ps.setInt(2, jsonArrayData.getInt(i));
					  ps.setString(3, "0");
					  ps.setTimestamp(4, new java.sql.Timestamp(formatter.parse(meetingDate).getTime()));
					  ps.setTimestamp(5, Timestamp.valueOf(senderToDateTime));
					  ps.setString(6, meetingInsertionObject.getString("latitude"));
					  ps.setString(7, meetingInsertionObject.getString("longitude"));
				      ps.setInt(8, 0);
					  ps.setString(9, meetingInsertionObject.getString("address"));
	
					  ps.addBatch();
				  
				  }
				  int [] insertedRow = ps.executeBatch();
				  for (int i = 0; i < insertedRow.length; i++) {
					//System.out.println("insertedRow[i]========="+insertedRow[i]);
					
				}
				 connection.commit();
				// return Response.status(201).entity(i).build();
				 if (insertedRow != null) {
					// sending mails to meeting creator
						try {
							ServiceUtility.insertingAndSendingMails(meetingInsertionObject.getJSONArray("emailIdJsonArray") , meetingInsertionObject.getInt("senderUserId") , meetingId);
						} catch (Exception e) {
							e.printStackTrace();
							finalJson.put("status", false);
							finalJson.put("message", "Mail not sent");
							
							return finalJson.toString();
						}
						//Inserting the EmailIds of the user who is not on the Fissbi and sending the emails also 
						ServiceUtility.sendingMeetingCreationNotification(meetingInsertionObject, meetingId);
						
						//Inserting the contact number of the user who is not on the Fissbi 
						ServiceUtility.insertMeetingContactNumbers(meetingInsertionObject.getJSONArray("contactsJsonArray"), meetingInsertionObject.getInt("senderUserId"), meetingId);
				 }	 
				 
			
		finalJson.put("status", true);
		finalJson.put("message", "meeting inserted successfully");
		
		return finalJson.toString();
		} catch(Exception e){
			e.printStackTrace();
			
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		
		return finalJson.toString();
	}
	
	
	
	
	
	
/*
	@GET
	@Path("/insertMeetingDetails/{senderUserId}/{requestDateTime}/{senderFromDateTime}/{senderToDateTime}"
			+ "/{scheduledTimeSlot}/{meetingDescription}/{RecipientDetails}"
			+ "/{Latitude}/{Longitude}/{oLatitude}/{oLongitude}/"
			+ "{UserPreferredLocationId}/{GoogleAddress}/{DestinationType}/{geoDateTime}")
	@Produces(MediaType.TEXT_PLAIN)
	public String insertMeetingDetails(@PathParam("senderUserId") int senderUserId,
			@PathParam("requestDateTime") Timestamp requestDateTime,
			@PathParam("senderFromDateTime") Timestamp senderFromDateTime,
			@PathParam("senderToDateTime") Timestamp senderToDateTime,
			@PathParam("scheduledTimeSlot") Time scheduledTimeSlot,
			@PathParam("meetingDescription") String meetingDescription,
			@PathParam("RecipientDetails") String RecipientDetails, @PathParam("Latitude") String Latitude,
			@PathParam("Longitude") String Longitude, @PathParam("oLatitude") String oLatitude,
			@PathParam("oLongitude") String oLongitude,
			@PathParam("UserPreferredLocationId") int UserPreferredLocationId,
			@PathParam("GoogleAddress") String GoogleAddress, @PathParam("DestinationType") int DestinationType,
			@PathParam("geoDateTime") Timestamp geoDateTime) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		String meetingId = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			RecipientDetails = RecipientDetails.replace("@", "/");
			String insertStoreProc = "{call usp_InsertMeetingDetails(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, senderUserId);
			callableStatement.setTimestamp(2, requestDateTime);
			callableStatement.setTimestamp(3, senderFromDateTime);
			callableStatement.setTimestamp(4, senderToDateTime);
			callableStatement.setTime(5, scheduledTimeSlot);
			callableStatement.setString(6, meetingDescription);
			callableStatement.setString(7, RecipientDetails);
			callableStatement.setString(8, Latitude);
			callableStatement.setString(9, Longitude);
			callableStatement.setString(10, oLatitude);
			callableStatement.setString(11, oLongitude);
			callableStatement.setInt(12, UserPreferredLocationId);
			callableStatement.setString(13, GoogleAddress);
			callableStatement.setInt(14, DestinationType);
			callableStatement.setTimestamp(15, geoDateTime);
			callableStatement.registerOutParameter(16, Types.INTEGER);
			callableStatement.registerOutParameter(17, Types.BIGINT);
			int value = callableStatement.executeUpdate();

			int isError = callableStatement.getInt(16);
			meetingId = callableStatement.getInt(17) + "";
			System.out.println(isError + " Latitude===" + Latitude + " meetingId==" + meetingId + "==L=" + Longitude);
			if (isError == 0) {
				String recipientDetails = getRecipientDetailsByMeetingID(Integer.parseInt(meetingId));
				JSONArray jsonArray = new JSONArray(recipientDetails);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					int recipientId = Integer.parseInt(jsonObject.getString("UserId"));
					UserNotifications userNotifications = new UserNotifications();
					Date date = new Date();
					Timestamp timestamp = new Timestamp(date.getTime());
					String notificationId = userNotifications.insertUserNotifications(recipientId, senderUserId,
							NotificationsEnum.Meeting_Pending_Requests.ordinal() + 1, 0, timestamp);
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
								authenticateUser.getGCMDeviceRegistrationId(recipientId));
						String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);
						System.out.println(result);
					} catch (IOException e) {
						e.printStackTrace();
					}

					// sending mails to meeting creator
					try {
						UserDTO userDTO = ServiceUtility.getUserDetailsByUserId(senderUserId);
						if (userDTO != null) {
							System.out.println("userDTO===" + userDTO.toString());
							MyEmailer.SendMail(userDTO.getEmailId(), "Your meeting request  " + userDTO.getFullName(),
									"Your meeting request" + userDTO.getFullName());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (DestinationType == 1) {
					LocationDetails locationDetails = new LocationDetails();
					try {
						// Doing reverse geocoding
						String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + Latitude + ","
								+ Longitude + "&key=" + Constants.GCM_APIKEY;
						ClientConfig config = new DefaultClientConfig();
						Client client = Client.create(config);
						WebResource service = client.resource(url);
						JSONObject json = new JSONObject(getOutputAsString(service));
						JSONArray results = (JSONArray) json.get("results");
						JSONObject resultsObject = (JSONObject) results.get(0);
						String formattedAddress = (String) resultsObject.get("formatted_address");
						locationDetails.insertMeetingLocationDetails(Latitude, Longitude, formattedAddress,
								Integer.parseInt(meetingId));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		return meetingId;
	}
*/
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
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
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
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
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
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
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
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
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
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
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
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/getConflictedMeetingDetails/{meetingId}/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConflictedMeetingDetails(@PathParam("meetingId") int meetingId, @PathParam("userId") int userId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetConflicatedMeetingDetails_ByUserID(?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.setInt(2, userId);
			callableStatement.setTimestamp(3, fromDate);
			callableStatement.setTimestamp(4, toDate);
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
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/updateConflictedMeetingDetails/{meetingId}/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateConflictedMeetingDetails(@PathParam("meetingId") int meetingId, @PathParam("userId") int userId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate) {

		JSONArray jsonResultsArray = new JSONArray();
		String conflictedMeetingDetails = getConflictedMeetingDetails(meetingId, userId, fromDate, toDate);
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
		System.out.println("meetingId==========================" + meetingId);
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
		}
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return json.toString();
	}

	@GET
	@Path("/getPendingMeetingRequests/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPendingMeetingRequests(@PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetPendingMeetingRequests(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("RequestDateTime", rs.getString("RequestDateTime"));
				jsonObject.put("SenderFromDateTime", rs.getString("SenderFromDateTime"));
				jsonObject.put("SenderToDateTime", rs.getString("SenderToDateTime"));
				jsonObject.put("MeetingDescription", rs.getString("MeetingDescription"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/getMeetingDetailsByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByMeetingID(@PathParam("meetingId") int meetingId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetMeetingDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("SenderUserID", rs.getString("SenderUserID"));
				jsonObject.put("RequestDateTime", rs.getString("RequestDateTime"));
				jsonObject.put("SenderFromDateTime", rs.getString("SenderFromDateTime"));
				jsonObject.put("SenderToDateTime", rs.getString("SenderToDateTime"));
				jsonObject.put("LocationID", rs.getString("LocationID"));
				jsonObject.put("ScheduledTimeSlot", rs.getString("ScheduledTimeSlot"));
				jsonObject.put("MeetingDescription", rs.getString("MeetingDescription"));
				jsonObject.put("Latitude", rs.getString("Latitude"));
				jsonObject.put("Longitude", rs.getString("Longitude"));
				jsonObject.put("UserPreferredLocationID", rs.getString("UserPreferredLocationID"));
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return jsonResultsArray.toString();
	}

	@GET
	@Path("/getUserDetailsByMeetingID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserDetailsByMeetingID(@PathParam("meetingId") int meetingId, @PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserDetails_ByMeetingID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, meetingId);
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
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return jsonResultsArray.toString();
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
