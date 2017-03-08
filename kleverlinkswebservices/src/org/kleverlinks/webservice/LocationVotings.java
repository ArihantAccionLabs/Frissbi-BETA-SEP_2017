package org.kleverlinks.webservice;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

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
import org.util.service.ServiceUtility;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@Path("LocationVotingsService")
public class LocationVotings {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	@GET  
    @Path("/insertLocationVotings/{meetingId}/{latitude}/{longitude}/{userVotings}")
    @Produces(MediaType.TEXT_PLAIN)
	public String insertLocationVotings(@PathParam("meetingId") Long meetingId,
			@PathParam("latitude") String latitude, @PathParam("longitude") String longitude,
			@PathParam("userVotings") String userVotings ){
		Connection conn = null;
		Statement stmt = null;
		String isError="";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUserLocationVotings(?,?,?,?,?)}";
			if (userVotings!= null ){
			userVotings = userVotings.replace("@", "/");
			}
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.setString(2, latitude);
			callableStatement.setString(3, longitude);
			callableStatement.setString(4, userVotings);
			callableStatement.registerOutParameter(5, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getLong(5)+"";

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
    @Path("/getUserFeasibleLocations/{meetingId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String getUserFeasibleLocations(@PathParam("meetingId") Long meetingId
			){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserFeasibleLocations(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserLocationVotingID", rs.getString("UserLocationVotingID"));
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("FeasibleLatitude", rs.getString("FeasibleLatitude"));
				jsonObject.put("FeasibleLongitude", rs.getString("FeasibleLongitude"));
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

	private static String getOutputAsString(WebResource service) {
		return service.accept(MediaType.TEXT_PLAIN).get(String.class);
	}
	@GET  
    @Path("/getUserFeasibleLocationVotingCount/{meetingId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String getUserFeasibleLocationVotingCount(@PathParam("meetingId") Long meetingId
			){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserFeasibleLocationVotings(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserLocationVotingID", rs.getString("UserLocationVotingID"));
				jsonObject.put("UserVoteCount", rs.getString("UserVoteCount"));
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
    @Path("/getUserFeasibleLocationVotingsByUserID/{meetingId}/{userId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String getUserFeasibleLocationVotingsByUserID(@PathParam("meetingId") Long meetingId,
			@PathParam("userId") Long userId){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserFeasibleLocationVotings_ByUserID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.setLong(2, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserLocationVotingID", rs.getString("UserLocationVotingID"));
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
    @Path("/getUserLocationMeetingID/{userLocationVotingID}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserLocationMeetingID(@PathParam("userLocationVotingID") Long userLocationVotingID
			) {

		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserLocationMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userLocationVotingID);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				jsonObject.put("MeetingID", rs.getString("MeetingID"));
				jsonObject.put("FeasibleLatitude", rs.getString("FeasibleLatitude"));
				jsonObject.put("FeasibleLongitude", rs.getString("FeasibleLongitude"));
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
		return jsonObject.toString();
	}

	public static void main(String args[]){
		LocationVotings locationVotings = new LocationVotings();
		String votingCount = locationVotings.getUserFeasibleLocationVotingCount(36l);
		System.out.println(votingCount);
		String locations = locationVotings.getUserFeasibleLocationVotingsByUserID(36l, 10l);
		System.out.println(locations);
	}
}
