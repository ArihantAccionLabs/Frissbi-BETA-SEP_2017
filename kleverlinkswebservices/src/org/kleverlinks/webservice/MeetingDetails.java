package org.kleverlinks.webservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import sun.misc.BASE64Encoder;

@Path("MeetingDetailsService")
public class MeetingDetails {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://frissdb.cloudapp.net/FrissDB";

	// Database credentials
	static final String USER = "Friss_App_User";
	static final String PASS = "FrissApp2015!";

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
    @Path("/updateRecipientXML/{meetingId}/{recipientId}/{fromDate}/{toDate}/{status}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateRecipientXML(@PathParam("meetingId") int meetingId,@PathParam("recipientId") int recipientId,
			@PathParam("fromDate") Timestamp fromDate, @PathParam("toDate") Timestamp toDate, @PathParam("status") int status) {

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
			String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Recipients>";
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
				}
				
				xmlString+= "<ResponseDateTime>";
				xmlString+= jsonObject.getString("ResponseDateTime");
				xmlString+= "</ResponseDateTime>";
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
				xmlString+= "<UserPreferredLocationID>";
				xmlString+= jsonObject.getString("UserPreferredLocationID");
				xmlString+= "</UserPreferredLocationID>";
				xmlString+= "<GoogleAddress>";
				xmlString+= jsonObject.getString("GoogleAddress");
				xmlString+= "</GoogleAddress>";
				xmlString+= "<DestinationType>";
				xmlString+= jsonObject.getString("DestinationType");
				xmlString+= "</DestinationType>";
				xmlString+= "<GeoDateTime>";
				xmlString+= jsonObject.getString("GeoDateTime");
				xmlString+= "</GeoDateTime>";
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
				jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
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
				jsonObject.put("AvatarPath", rs.getString("AvatarPath"));
//				String imageString = null;
//		        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		        BufferedImage image=null;
//				try {
//					image = ImageIO.read(new File(rs.getString("AvatarPath")));
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//		        String type="jpg";
//		        try {
//		            ImageIO.write(image, type, bos);
//		            byte[] imageBytes = bos.toByteArray();
//
//		            BASE64Encoder encoder = new BASE64Encoder();
//		            imageString = encoder.encode(imageBytes);
//
//		            bos.close();
//		        } catch (IOException e) {
//		            e.printStackTrace();
//		        }
//		        jsonObject.put("imageString", imageString);
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

	private static Connection getDBConnection() {

		Connection dbConnection = null;

		try {

			Class.forName(JDBC_DRIVER);

		} catch (ClassNotFoundException e) {

			System.out.println(e.getMessage());

		}

		try {

			dbConnection = DriverManager.getConnection(DB_URL, USER, PASS);
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
		//System.out.println(meetingDetails.getRecipientDetailsByMeetingID(45));
		//meetingDetails.updateRecipientXML(51,50,fromTimestamp,toTimestamp,1);
		System.out.println(meetingDetails.getRecipientDetailsByMeetingID(50));
	}
}
