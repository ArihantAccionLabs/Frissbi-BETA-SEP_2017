package org.kleverlinks.webservice;

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
import org.json.JSONObject;

@Path("LocationVotingsService")
public class LocationVotings {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://frissdb.cloudapp.net/FrissDB";

	// Database credentials
	static final String USER = "Friss_App_User";
	static final String PASS = "FrissApp2015!";
	
	@GET  
    @Path("/insertLocationVotings/{meetingId}/{latitude}/{longitude}/{userVotings}")
    @Produces(MediaType.TEXT_PLAIN)
	public String insertLocationVotings(@PathParam("meetingId") int meetingId,
			@PathParam("latitude") String latitude, @PathParam("longitude") String longitude,
			@PathParam("userVotings") String userVotings ){
		Connection conn = null;
		Statement stmt = null;
		String isError="";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUserLocationVotings(?,?,?,?,?)}";
			if (userVotings!= null ){
			userVotings = userVotings.replace("@", "/");
			}
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.setString(2, latitude);
			callableStatement.setString(3, longitude);
			callableStatement.setString(4, userVotings);
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
	
	@GET  
    @Path("/getUserFeasibleLocations/{meetingId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String getUserFeasibleLocations(@PathParam("meetingId") int meetingId
			){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserFeasibleLocations(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
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
	
	@GET  
    @Path("/updateUserLocationVotings/{userId}/{userLocationVotingId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String updateUserLocationVotings(@PathParam("userId") int userId,
			@PathParam("userLocationVotingId") String userLocationVotingId ){
		Connection conn = null;
		Statement stmt = null;
		String value="";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserLocationVoting(?,?)}";
			userLocationVotingId = userLocationVotingId.replace("@", "/");
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, userLocationVotingId);
			value = callableStatement.executeUpdate()+"";

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
		return value;
	}
	
	@GET  
    @Path("/getUserFeasibleLocationVotingCount/{meetingId}")
    @Produces(MediaType.TEXT_PLAIN)
	public String getUserFeasibleLocationVotingCount(@PathParam("meetingId") int meetingId
			){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserFeasibleLocationVotings(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
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
	public String getUserFeasibleLocationVotingsByUserID(@PathParam("meetingId") int meetingId,
			@PathParam("userId") int userId){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserFeasibleLocationVotings_ByUserID(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, meetingId);
			callableStatement.setInt(2, userId);
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
		LocationVotings locationVotings = new LocationVotings();
		//String votingCount = locationVotings.getUserFeasibleLocationVotingCount(36);
		//System.out.println(votingCount);
		String locations = locationVotings.getUserFeasibleLocationVotingsByUserID(36, 10);
		System.out.println(locations);
	}
}
