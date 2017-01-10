package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("FriendListService")
public class UserFriendList {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://frissdb.cloudapp.net/FrissDB";

	// Database credentials
	static final String USER = "Friss_App_User";
	static final String PASS = "FrissApp2015!";

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

	@GET  
    @Path("/sendFriendRequest/{username1}/{username2}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String sendFriendRequest(@PathParam("username1") String userName1,
			@PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;

		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName1
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
			}

			sql = "SELECT userID from tbl_users where username ='" + userName2
					+ "'" + " limit 1";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId2 = rs.getInt("userid");
			}
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is sending
			//friend request to user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 0);
			callableStatement.setInt(4, userId1);
			callableStatement.setTimestamp(5, sqlDateNow);
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				return "1";
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
		return "0";
	}

	@GET  
    @Path("/approveFriendRequest/{username1}/{username2}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String approveFriendRequest(@PathParam("username1") String userName1, @PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName1
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
			}

			sql = "SELECT userID from tbl_users where username ='" + userName2
					+ "'" + " limit 1";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId2 = rs.getInt("userid");
			}

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is approving
			//friend request of user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 1);
			callableStatement.setInt(4, userId2);
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, sqlDateNow);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				return "1";
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
		return "0";
	}
	
	@GET  
    @Path("/rejectFriendRequest/{username1}/{username2}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String rejectFriendRequest(@PathParam("username1") String userName1, @PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName1
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
			}

			sql = "SELECT userID from tbl_users where username ='" + userName2
					+ "'" + " limit 1";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId2 = rs.getInt("userid");
			}

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is rejecting 
			//friend request of user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 2);
			callableStatement.setInt(4, userId2);
			callableStatement.setDate(5, null);
			callableStatement.setTimestamp(6, sqlDateNow);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				return "1";
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
		return "0";
	}
	
	@GET  
    @Path("/unFriendRequest/{username1}/{username2}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String unFriendRequest(@PathParam("username1") String userName1, @PathParam("username2") String userName2) {
		Connection conn = null;
		Statement stmt = null;
		int userId1 = 0;
		int userId2 = 0;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName1
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId1 = rs.getInt("userid");
			}

			sql = "SELECT userID from tbl_users where username ='" + userName2
					+ "'" + " limit 1";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId2 = rs.getInt("userid");
			}

			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			// put entry into the database as user1 is sending
			//unfriend request to user2
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUpdateUserFriendship(?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId1);
			callableStatement.setInt(2, userId2);
			callableStatement.setInt(3, 3);
			callableStatement.setInt(4, userId1);
			callableStatement.setTimestamp(5, sqlDateNow);
			callableStatement.setDate(6, null);
			callableStatement.registerOutParameter(7, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			if (value == 1) {
				System.out.println("Stored procedure executed");
				return "1";
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
		return "0";
	}
	
	@GET  
    @Path("/friendsList/{username}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String friendsList( @PathParam("username") String userName){
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where UserID1 ='"+ userId + "' and requestStatus = 1";
					
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID2"));
			}
			
			sql = "Select * from tbl_userfriendlist  where UserID2 ='"+ userId +  "' and requestStatus = 1";
			
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}
			
			Iterator<Integer> iterator = userIds.iterator();
			while(iterator.hasNext()){
				userId = iterator.next();
				sql = "Select * from tbl_users where UserID = '"+ userId +"'";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					jsonResultsArray.put(jsonObject);
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
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/pendingForApprovalList/{username}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String pendingForApprovalList( @PathParam("username") String userName){
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where UserID2 ='"+ userId +  "' and requestStatus = 0";
			
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}
			
			Iterator<Integer> iterator = userIds.iterator();
			while(iterator.hasNext()){
				userId = iterator.next();
				sql = "Select * from tbl_users where UserID = '"+ userId +"'";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					jsonResultsArray.put(jsonObject);
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
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/blockedFriendsList/{username}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String blockedFriendsList( @PathParam("username") String userName){
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where ActionUserID ='"+ userId +  "' and requestStatus = 2";
			
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}
			
			Iterator<Integer> iterator = userIds.iterator();
			while(iterator.hasNext()){
				userId = iterator.next();
				sql = "Select * from tbl_users where UserID = '"+ userId +"'";
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					jsonResultsArray.put(jsonObject);
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
		return jsonResultsArray.toString();
	}
	@GET  
    @Path("/search/{search_criteria}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String search(@PathParam("search_criteria") String search_criteria ){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "Select * from tbl_users  where FirstName like '%"+ search_criteria +
					"%' or LastName like '%"+ search_criteria +
					"%' or UserName like '%"+ search_criteria + "%'" ;
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
				jsonObject.put("EmailName", rs.getString("EmailName"));
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
    @Path("/searchResults/{userId}/{search_criteria}/{resultsPage}/{pageNo}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String searchResults(@PathParam("userId") int userId,@PathParam("search_criteria") String search_criteria,
			@PathParam("resultsPage") int resultsPage,@PathParam("pageNo") int pageNo){
		Connection conn = null;
		Statement stmt = null;
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetSearchBoxResults(?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, search_criteria);
			callableStatement.setInt(3, resultsPage);
			callableStatement.setInt(4, pageNo);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserId", rs.getString("UserId"));
				jsonObject.put("UserName", rs.getString("UserName"));
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
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
    @Path("/searchFriends/{userName}/{search_criteria}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String searchFriends(@PathParam("userName") String userName,@PathParam("search_criteria") String search_criteria ){
		Connection conn = null;
		Statement stmt = null;
		int userId = 0;
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getInt("userId");
			}
			sql = "Select * from tbl_userfriendlist  where UserID1 ='"+ userId + "' and requestStatus = 1";
					
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID2"));
			}
			
			sql = "Select * from tbl_userfriendlist  where UserID2 ='"+ userId +  "' and requestStatus = 1";
			
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userIds.add(rs.getInt("UserID1"));
			}
			
			Iterator<Integer> iterator = userIds.iterator();
			while(iterator.hasNext()){
				userId = iterator.next();
				sql = "Select * from tbl_users where UserID = '"+ userId +"' and ( FirstName like '%"+ search_criteria +
						"%' or LastName like '%"+ search_criteria +
						"%' or UserName like '%"+ search_criteria + "%')" ;
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("UserId", rs.getString("UserId"));
					jsonObject.put("UserName", rs.getString("UserName"));
					jsonObject.put("FirstName", rs.getString("FirstName"));
					jsonObject.put("LastName", rs.getString("LastName"));
					jsonObject.put("EmailName", rs.getString("EmailName"));
					jsonResultsArray.put(jsonObject);
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
		return jsonResultsArray.toString();
	}
	
	@GET  
    @Path("/friendStatus/{userID1}/{userID2}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String friendStatus( @PathParam("userID1") int userID1,@PathParam("userID2") int userID2){
		Connection conn = null;
		Statement stmt = null;
		String requestStatus = "-1";
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT RequestStatus from tbl_userfriendlist where userID1 ='" + userID1
					+ "'and userID2 ='" +userID2  + "' limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				requestStatus = rs.getInt("RequestStatus") +"";
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
		return requestStatus;
	}
	public void test() {
		//sendFriendRequest("dharmakolla96", "dharmakolla98");
		//approveFriendRequest("dharmakolla96","dharmakolla98");
		//rejectFriendRequest("dharmakolla123","dharmakolla23");
		//unFriendRequest("dharmakolla123","dharmakolla23");
		
		//search("a");
		//friendsList("dharmakolla96");
//		friendsList("dharmakolla98");
		
		//searchFriends("dharmakolla98", "a");
		//friendStatus(41,40);
		searchResults(40, "a", 8, 1);
	}

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;

		UserFriendList friendList = new UserFriendList();
		friendList.test();

	}
}
