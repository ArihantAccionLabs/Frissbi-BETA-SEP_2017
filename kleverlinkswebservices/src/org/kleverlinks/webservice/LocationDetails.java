package org.kleverlinks.webservice;

import java.io.ByteArrayInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javafx.geometry.Point2D;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Path("LocationDetailsService")
public class LocationDetails {
	
		// JDBC driver name and database URL
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	@GET
	@Path("/calculateMidpointOfParticipiants/{noOfParticipants}/{latitudeValues}/{longitudeValues}")
	@Produces(MediaType.TEXT_PLAIN)
	public String calculateMidpointOfParticipiants(
			@PathParam("noOfParticipants") int noOfParticipants,
			@PathParam("latitudeValues") String latitudeValues,
			@PathParam("longitudeValues") String longitudeValues) {

		JSONObject jsonObject = new JSONObject();
		ArrayList<Point2D> Point2Ds = new ArrayList<Point2D>();
		double midLatitude = 0.0;
		double midLongitude = 0.0;
		String formattedAddress ="";
		StringTokenizer latitudeStringTokenizer = new StringTokenizer(latitudeValues, ":");
		StringTokenizer longitudeStringTokenizer = new StringTokenizer(longitudeValues, ":");
		ArrayList<Double> doubleLatitudeValues = new ArrayList<Double>(noOfParticipants);
		ArrayList<Double> doubleLongitudeValues = new ArrayList<Double>(noOfParticipants);
		while(latitudeStringTokenizer.hasMoreTokens() ){
			double latitude = Double.parseDouble(latitudeStringTokenizer.nextToken());
			System.out.println("latitude: " + latitude);
			doubleLatitudeValues.add(latitude);
		}
		while(longitudeStringTokenizer.hasMoreTokens() ){
			double longitude = Double.parseDouble(longitudeStringTokenizer.nextToken());
			System.out.println("longitude: " + longitude);
			doubleLongitudeValues.add(longitude);
		}
		for (int i = 0; i < noOfParticipants; i++) {
			double latitude = doubleLatitudeValues.get(i);
			double longitude = doubleLongitudeValues.get(i);
			Point2D e = new Point2D(latitude, longitude);
			Point2Ds.add(e);
		}
		QuickHull qh = new QuickHull();
		ArrayList<Point2D> p = qh.quickHull(Point2Ds);
		System.out
				.println("The Points in the Convex hull using Quick Hull are: ");
		for (int i = 0; i < p.size(); i++)
			System.out.println("(" + p.get(i).getX() + ", " + p.get(i).getY()
					+ ")\n");
		Point2D centroid = qh.findCentroid(Point2Ds);
		System.out.println("Middle latitude value of all locations is: "
				+ centroid.getX() + "\n");
		System.out.println("Middle longitude value of all locations is: "
				+ centroid.getY() + "\n");
		midLatitude = centroid.getX();
		midLongitude = centroid.getY();

		// Doing reverse geocoding
		String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ midLatitude
				+ ","
				+ midLongitude
				+ "&key="+Constants.GCM_APIKEY;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);

		try {
			JSONObject json = new JSONObject(getOutputAsString(service));
			JSONArray results = (JSONArray) json.get("results");
			JSONObject resultsObject = (JSONObject) results.get(0);
			formattedAddress = (String) resultsObject
					.get("formatted_address");
			System.out.println("Mid point location address is: "
					+ formattedAddress);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			jsonObject.put("latitude", midLatitude);
			jsonObject.put("longitude", midLongitude);
			jsonObject.put("formattedAddress", formattedAddress);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject.toString();
	}

	@GET
	@Path("/calculateMidpointForMeeting/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String calculateMidpointForMeeting(
			@PathParam("meetingId") int meetingId
			) {
		MeetingDetails meetingDetails = new MeetingDetails();
		boolean isGeoMagic = true;
		ArrayList<Point2D> Point2Ds = new ArrayList<Point2D>();
		JSONArray jsonArray = new JSONArray( meetingDetails.getMeetingDetailsByMeetingID(meetingId));
		JSONObject meetingDetailsByMeetingId = jsonArray.getJSONObject(0);
		if (!meetingDetailsByMeetingId.get("DestinationType").equals("3")){
			isGeoMagic = false;
			return "false";
		}
		Double latitude = Double.parseDouble( meetingDetailsByMeetingId.get("olatitude").toString());
		Double longitude =  Double.parseDouble( meetingDetailsByMeetingId.get("oLongitude").toString());
		Point2D point = new Point2D(latitude, longitude);
		Point2Ds.add( point);
		String recipientDetailsByMeetingId = meetingDetails.getRecipientDetailsByMeetingID(meetingId);
		JSONArray jsonResultsArray = new JSONArray(recipientDetailsByMeetingId);
		for ( int i=0;i<jsonResultsArray.length();i++){
			JSONObject jsonObject = (JSONObject)jsonResultsArray.get(i);
			if (!jsonObject.get("DestinationType").equals("3")){
				isGeoMagic = false;
				return "false";
			}
			if(jsonObject.get("Status").equals("1") && jsonObject.get("DestinationType").equals("3") ){
				double lat = Double.parseDouble((String)jsonObject.get("oLatitude"));
				double lng = Double.parseDouble((String)jsonObject.get("oLongitude"));
				point = new Point2D(lat, lng);
				Point2Ds.add( point);
			}
		}
		
		QuickHull qh = new QuickHull();
		ArrayList<Point2D> p = qh.quickHull(Point2Ds);
		JSONObject jsonObject = new JSONObject();
		Double midLatitude =0.0;
		Double midLongitude=0.0;
		String formattedAddress ="";
		
		Point2D centroid = qh.findCentroid(Point2Ds);
		midLatitude = centroid.getX();
		midLongitude = centroid.getY();

		// Doing reverse geocoding
		String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ midLatitude
				+ ","
				+ midLongitude
				+ "&key="+Constants.GCM_APIKEY;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);

		try {
			JSONObject json = new JSONObject(getOutputAsString(service));
			JSONArray results = (JSONArray) json.get("results");
			if( results.length()!=0 ){
			JSONObject resultsObject = (JSONObject) results.get(0);
			formattedAddress = (String) resultsObject
					.get("formatted_address");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			jsonObject.put("latitude", midLatitude);
			jsonObject.put("longitude", midLongitude);
			jsonObject.put("formattedAddress", formattedAddress);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject.toString();
	}
	
	@GET  
    @Path("/insertMeetingLocationDetails/{latitude}/{longitude}/{destinationAddress}/{meetingId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String insertMeetingLocationDetails(
			@PathParam("latitude") String latitude, @PathParam("longitude") String longitude,
			@PathParam("destinationAddress") String destinationAddress,@PathParam("meetingId") int meetingId
			 ){
		Connection conn = null;
		Statement stmt = null;
		String isError="";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertMeetingLocationDetails(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, latitude);
			callableStatement.setString(2, longitude);
			callableStatement.setString(3, destinationAddress);
			callableStatement.setInt(4, meetingId);
			callableStatement.registerOutParameter(5, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(5)+"";

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
	private static String getOutputAsString(WebResource service) {
		return service.accept(MediaType.TEXT_PLAIN).get(String.class);
	}
	
	public static void main(String args[]){
		LocationDetails locationDetails = new LocationDetails();
		locationDetails.calculateMidpointForMeeting(45);
	}
}
