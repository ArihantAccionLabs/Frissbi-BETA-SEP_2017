package org.kleverlinks.webservice;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import javax.ws.rs.GET;
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
import org.service.dto.UserDTO;
import org.util.service.ServiceUtility;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@Path("MeetingDetailsService")
public class MeetingDetails {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	@GET  
    @Path("/insertMeetingDetails/{senderUserId}/{requestDateTime}/{senderFromDateTime}/{senderToDateTime}"
    		+ "/{scheduledTimeSlot}/{meetingDescription}/{RecipientDetails}"
    		+ "/{Latitude}/{Longitude}/{oLatitude}/{oLongitude}/"
    		+ "{UserPreferredLocationId}/{GoogleAddress}/{DestinationType}/{geoDateTime}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String insertMeetingDetails(@PathParam("senderUserId") int senderUserId,
			@PathParam("requestDateTime") Timestamp requestDateTime, @PathParam("senderFromDateTime") Timestamp senderFromDateTime,
			@PathParam("senderToDateTime") Timestamp senderToDateTime,@PathParam("scheduledTimeSlot") Time scheduledTimeSlot,
			@PathParam("meetingDescription") String meetingDescription,
			@PathParam("RecipientDetails") String RecipientDetails,@PathParam("Latitude") String Latitude,
			@PathParam("Longitude") String Longitude,@PathParam("oLatitude") String oLatitude,
			@PathParam("oLongitude") String oLongitude,@PathParam("UserPreferredLocationId") int UserPreferredLocationId,
			@PathParam("GoogleAddress") String GoogleAddress,
			@PathParam("DestinationType") int DestinationType,
			@PathParam("geoDateTime") Timestamp geoDateTime
			) {

		Connection conn = null;
		Statement stmt = null;
		String meetingId = "";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
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
			meetingId = callableStatement.getInt(17)+"";
			System.out.println(isError +" Latitude==="+Latitude+" meetingId=="+meetingId+"==L="+Longitude);
			if ( isError  == 0){
				String recipientDetails =getRecipientDetailsByMeetingID(Integer.parseInt(meetingId));
				JSONArray jsonArray = new JSONArray(recipientDetails);
				for ( int i=0;i<jsonArray.length(); i++){
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					int recipientId = Integer.parseInt(jsonObject.getString("UserId"));
					UserNotifications userNotifications = new UserNotifications();
					Date date = new Date();
					Timestamp timestamp = new Timestamp(date.getTime());
					String notificationId = userNotifications.insertUserNotifications(recipientId, senderUserId, NotificationsEnum.Meeting_Pending_Requests.ordinal()+1, 0, timestamp);
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
					
					//sending mails to meeting creator
					try{
					  UserDTO userDTO = ServiceUtility.getUserDetailsByUserId(senderUserId);
					  if(userDTO != null){
						  System.out.println("userDTO==="+userDTO.toString());
						  MyEmailer.SendMail(userDTO.getEmailId(), "Your meeting request  "+userDTO.getFullName(), "Your meeting request"+userDTO.getFullName());
					  }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if ( DestinationType == 1){
					LocationDetails locationDetails = new LocationDetails();
					try {
						//Doing reverse geocoding
						String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+Latitude+","+Longitude+"&key="+Constants.GCM_APIKEY;
						ClientConfig config = new DefaultClientConfig();
						Client client = Client.create(config);
						WebResource service = client.resource(url);
						JSONObject json = new JSONObject(
								getOutputAsString(service));
						JSONArray results = (JSONArray) json.get("results");
						JSONObject resultsObject = (JSONObject) results.get(0);
						String  formattedAddress = (String) resultsObject
								.get("formatted_address");
						locationDetails.insertMeetingLocationDetails(Latitude, Longitude, formattedAddress, Integer.parseInt(meetingId));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return meetingId;
	}
	private static String getOutputAsString(WebResource service) {
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
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientXML_ByMeetingID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.registerOutParameter(2, Types.VARCHAR);
			int value = callableStatement.executeUpdate();
			recipientXML = callableStatement.getString(2);
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return recipientXML;
	}
	@GET  
    @Path("/updateRecipientXMLByMeetingID/{meetingId}/{recipientDetails}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateRecipientXMLByMeetingID(@PathParam("meetingId") int meetingId,@PathParam("recipientDetails") String recipientDetails) {

		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			recipientDetails = recipientDetails.replace("@", "/");
			String insertStoreProc = "{call usp_UpdateRecipientXML_ByMeetingID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.setString(2, recipientDetails);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3)+"";

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return isError;
	}
	
	@GET  
    @Path("/updateRecipientXML/{meetingId}/{recipientId}/{fromDate}/{toDate}/{status}/{GoogleAddress}/{geoDateTime}/{Latitude}/{Longitude}/{oLatitude}/{oLongitude}/{DestinationType}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateRecipientXML(@PathParam("meetingId") int meetingId,@PathParam("recipientId") int recipientId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate, @PathParam("status") int status,
			@PathParam("GoogleAddress") String GoogleAddress,
			@PathParam("geoDateTime") Timestamp geoDateTime,@PathParam("Latitude") String Latitude,
			@PathParam("Longitude") String Longitude,@PathParam("oLatitude") String oLatitude,
			@PathParam("oLongitude") String oLongitude,@PathParam("DestinationType") int DestinationType) {

		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			boolean updateFlag = false;
			String recipientDetailsByMeetingId = getRecipientDetailsByMeetingID(meetingId);
			JSONArray jsonResultsArray = new JSONArray(recipientDetailsByMeetingId);
			String xmlString = "<Recipients>";
			for ( int i=0;i<jsonResultsArray.length();i++){
				JSONObject jsonObject = (JSONObject)jsonResultsArray.get(i);
				xmlString+= "<RecipientID>";
				xmlString+= jsonObject.getString("UserId");
				xmlString+= "</RecipientID>";
				if(jsonObject.getString("UserId").equals(recipientId+"")){
					updateFlag = true;
					xmlString+= "<Status>";
					xmlString+= status;
					xmlString+= "</Status>";
					xmlString+= "<RecipientFromDateTime>";
					xmlString+= fromDate;
					xmlString+= "</RecipientFromDateTime>";
					xmlString+= "<RecipientToDateTime>";
					xmlString+= toDate;
					xmlString+= "</RecipientToDateTime>";
					xmlString+= "<GoogleAddress>";
					xmlString+= GoogleAddress ;
					xmlString+= "</GoogleAddress>";
					xmlString+= "<GeoDateTime>";
					xmlString+= geoDateTime;
					xmlString+= "</GeoDateTime>";
					xmlString+= "<Latitude>";
					xmlString+= Latitude;
					xmlString+= "</Latitude>";
					xmlString+= "<Longitude>";
					xmlString+= Longitude;
					xmlString+= "</Longitude>";
					xmlString+= "<olatitude>";
					xmlString+= oLatitude;
					xmlString+= "</olatitude>";
					xmlString+= "<oLongitude>";
					xmlString+= oLongitude;
					xmlString+= "</oLongitude>";
					xmlString+= "<DestinationType>";
					xmlString+= DestinationType;
					xmlString+= "</DestinationType>";
				}
				else{
					xmlString+= "<Status>";
					xmlString+= jsonObject.getString("Status");
					xmlString+= "</Status>";
					xmlString+= "<RecipientFromDateTime>";
					xmlString+= jsonObject.getString("RecipientFromDateTime");
					xmlString+= "</RecipientFromDateTime>";
					xmlString+= "<RecipientToDateTime>";
					xmlString+= jsonObject.getString("RecipientToDateTime");
					xmlString+= "</RecipientToDateTime>";
					xmlString+= "<GoogleAddress>";
					xmlString+= jsonObject.getString("GoogleAddress");
					xmlString+= "</GoogleAddress>";
					//xmlString+= "<GeoDateTime>";
					//xmlString+= jsonObject.getString("GeoDateTime");
					//xmlString+= "</GeoDateTime>";
					xmlString+= "<Latitude>";
					xmlString+= jsonObject.getString("Latitude");
					xmlString+= "</Latitude>";
					xmlString+= "<Longitude>";
					xmlString+= jsonObject.getString("Longitude");
					xmlString+= "</Longitude>";
					xmlString+= "<olatitude>";
					xmlString+= jsonObject.getString("olatitude");
					xmlString+= "</olatitude>";
					xmlString+= "<oLongitude>";
					xmlString+= jsonObject.getString("oLongitude");
					xmlString+= "</oLongitude>";
					xmlString+= "<DestinationType>";
					xmlString+= jsonObject.getString("DestinationType");
					xmlString+= "</DestinationType>";
				}
				
				xmlString+= "<ResponseDateTime>";
				xmlString+= jsonObject.getString("ResponseDateTime");
				xmlString+= "</ResponseDateTime>";
				xmlString+= "<UserPreferredLocationID>";
				xmlString+= jsonObject.getString("UserPreferredLocationID");
				xmlString+= "</UserPreferredLocationID>";
				//xmlString+= "<DestinationType>";
				//xmlString+= jsonObject.getString("DestinationType");
				//xmlString+= "</DestinationType>";
				
			}
			if(!updateFlag){
				xmlString+= "<RecipientID>";
				xmlString+= recipientId ;
				xmlString+= "</RecipientID>";
				xmlString+= "<Status>";
				xmlString+= status;
				xmlString+= "</Status>";
				xmlString+= "<RecipientFromDateTime>";
				xmlString+= fromDate;
				xmlString+= "</RecipientFromDateTime>";
				xmlString+= "<RecipientToDateTime>";
				xmlString+= toDate;
				xmlString+= "</RecipientToDateTime>";
			}
			xmlString+= "</Recipients>";
			String insertStoreProc = "{call usp_UpdateRecipientXML_ByMeetingID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.setString(2, xmlString);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3)+"";
			
			if ( isError.equals( "0")){
				String meetingDetails = getMeetingDetailsByMeetingID(meetingId);
				JSONArray jsonArray = new JSONArray(meetingDetails);
				int senderUserId=0 ;
				for (int i=0; i<jsonArray.length();i++ ){
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					senderUserId = Integer.parseInt( jsonObject.getString("SenderUserID") );
				}
				UserNotifications userNotifications = new UserNotifications();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				if ( status == 1){
				String notificationId = userNotifications.insertUserNotifications(senderUserId,recipientId, NotificationsEnum.Meeting_Request_Acceptance.ordinal()+1, 0, timestamp);
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
					JSONObject jsonRegistrationId = new JSONObject ( authenticateUser.getGCMDeviceRegistrationId(senderUserId));
					String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
					Result result = sender.send(message, deviceRegistrationId, 1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}else if ( status == 2){
					String notificationId = userNotifications.insertUserNotifications(senderUserId,recipientId, NotificationsEnum.Meeting_Rejected.ordinal()+1, 0, timestamp);
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
						JSONObject jsonRegistrationId = new JSONObject ( authenticateUser.getGCMDeviceRegistrationId(senderUserId));
						String deviceRegistrationId = jsonRegistrationId.getString("DeviceRegistrationID");
						Result result = sender.send(message, deviceRegistrationId, 1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return isError;
	}
	
	@GET  
    @Path("/getRecipientDetailsByUserID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRecipientDetailsByUserID(@PathParam("meetingId") int meetingId,@PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientDetails_ByUserID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("ResponseDateTime", rs.getString("ResponseDateTime"));
				jsonObject.put("Status", rs.getString("Status"));
				if ( rs.getString("AvatarPath") == null ){
					jsonObject.put("AvatarPath", "");
				}else{
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
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
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
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetRecipientDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("ResponseDateTime", rs.getString("ResponseDateTime"));
				jsonObject.put("Status", rs.getString("Status"));
				if ( rs.getString("AvatarPath") == null ){
					jsonObject.put("AvatarPath", "");
				}else{
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
				//jsonObject.put("GeoDateTime", rs.getString("GeoDateTime"));
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/getMeetingDetailsByUserID/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByUserID(@PathParam("userId") int userId, @PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate ) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetMeetingDetails_ByUserID(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setTimestamp(2, fromDate);
			callableStatement.setTimestamp(3, toDate);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
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
				if (rs.getString("GoogleAddress") == null){
					jsonObject.put("GoogleAddress", "");
				}else{
				jsonObject.put("GoogleAddress", rs.getString("GoogleAddress"));
				}
				jsonObject.put("DestinationType", rs.getString("DestinationType"));
				jsonResultsArray.put(jsonObject);
			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/getConflictedMeetingDetails/{meetingId}/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConflictedMeetingDetails(@PathParam("meetingId") int meetingId,@PathParam("userId") int userId, @PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate ) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
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

			while(rs.next()){
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
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/updateConflictedMeetingDetails/{meetingId}/{userId}/{fromDate}/{toDate}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateConflictedMeetingDetails(@PathParam("meetingId") int meetingId,@PathParam("userId") int userId, @PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate ) {

		JSONArray jsonResultsArray = new JSONArray();
		String conflictedMeetingDetails = getConflictedMeetingDetails(meetingId,userId,fromDate,toDate);
		jsonResultsArray = new JSONArray(conflictedMeetingDetails);
		String isError = "";
		for(int i=0;i<jsonResultsArray.length();i++){
			JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
			isError = updateRecipientXML(Integer.parseInt( jsonObject.getString("MeetingID")),userId,null,null,3,null,null,null,null,null,null,0);
		}
		return isError;
	}
	@GET  
    @Path("/getMeetingSummary/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingSummary(@PathParam("meetingId") int meetingId ) {
      System.out.println("meetingId=========================="+meetingId);
		Connection conn = null;
		Statement stmt = null;
		JSONObject json = new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetMeetingSummaryDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();
			String SenderFromDateTime="";
			String SenderToDateTime="";
			String ScheduledTimeSlot ="";
			String MeetingDescription="";
			String Latitude ="";
			String Longitude ="";
			String DestinationAddress="";

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				if ( rs.getString("AvatarPath") == null ){
					jsonObject.put("AvatarPath", "");
				}else{
				jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
				}
				SenderFromDateTime = rs.getString("SenderFromDateTime");
				SenderToDateTime = rs.getString("SenderToDateTime");
				ScheduledTimeSlot= rs.getString("ScheduledTimeSlot");
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
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
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
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetPendingMeetingRequests(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
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
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/getMeetingDetailsByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByMeetingID(@PathParam("meetingId") int meetingId ) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetMeetingDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("SenderUserID", rs.getString("SenderUserID"));
				jsonObject.put("RequestDateTime", rs.getString("RequestDateTime"));
				jsonObject.put("SenderFromDateTime", rs.getString("SenderFromDateTime"));
				jsonObject.put("SenderToDateTime", rs.getString("SenderToDateTime"));
				jsonObject.put("LocationID", rs.getString("LocationID"));
				jsonObject.put("ScheduledTimeSlot", rs.getString("ScheduledTimeSlot"));
				jsonObject.put("MeetingDescription", rs.getString("MeetingDescription"));
				jsonObject.put("RecipientDetails", rs.getString("RecipientDetails"));
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
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return jsonResultsArray.toString();
	}

	@GET  
    @Path("/getUserDetailsByMeetingID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserDetailsByMeetingID(@PathParam("meetingId") int meetingId,@PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserDetails_ByMeetingID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
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
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return jsonResultsArray.toString();
	}
	private static Connection getDBConnection() {

		Connection dbConnection = null;
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(Constants.DB_URL, Constants.USER, Constants.PASS);
			return dbConnection;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return dbConnection;
	}
	
	public static void main(String args[]){
		MeetingDetails meetingDetails = new MeetingDetails();
		String meetingdetails = meetingDetails.getRecipientDetailsByUserID(42,3);
		//System.out.println("meetingdetails: "+ meetingdetails);
		Date date = new Date();
		Timestamp fromTimestamp = new Timestamp(date.getTime() - 1500000000);
		Timestamp toTimestamp = new Timestamp( date.getTime() +100000000 );
		//Timestamp fromTimestamp = new Timestamp(2015, 06, 06, 12, 00, 00, 00);
		//Timestamp toTimestamp = new Timestamp(2015, 06, 28, 12, 00, 00, 00);
		//Timestamp timeStamp = new Timestamp
		//meetingDetails.getMeetingDetailsByUserID(42, fromTimestamp, toTimestamp);
		System.out.println(meetingDetails.getRecipientDetailsByMeetingID(45));
		//meetingDetails.updateRecipientXML(51,50,fromTimestamp,toTimestamp,1);
		//System.out.println(meetingDetails.getRecipientDetailsByMeetingID(52));
		//System.out.println(meetingDetails.getMeetingDetailsByUserID(77,fromTimestamp,toTimestamp) );
		//System.out.println(meetingDetails.getPendingMeetingRequests(50));
		//System.out.println(meetingDetails.getMeetingSummary(84));
	}
	
	//Testing method
	
	public void testMethod(){
		
	}
	
	
}
