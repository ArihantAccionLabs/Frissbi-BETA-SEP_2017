package org.kleverlinks.webservice;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("AuthenticateUserService")
public class AuthenticateUser {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://frissdb.cloudapp.net/FrissDB";

	// Database credentials
	static final String USER = "Friss_App_User";
	static final String PASS = "FrissApp2015!";
	
	private SecureRandom random = new SecureRandom();

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

	public boolean authenticateUser(String userName, String password) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " and password ='" + password + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				return true;
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
		return false;
	}

	@GET  
    @Path("/userAuthentication/{username}/{password}/{loginipaddress}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String userAuthentication(@PathParam("username") String userName, @PathParam("password") String password,
			@PathParam("loginipaddress") String loginIpAddress) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserAuthentication(?,?,?,?,?,?,?,?)}";
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, userName);
			callableStatement.setString(2, password);
			callableStatement.setTimestamp(3, sqlDateNow);
			callableStatement.setString(4, loginIpAddress);
			callableStatement.registerOutParameter(5, Types.BIGINT);
			callableStatement.registerOutParameter(6, Types.VARCHAR);
			callableStatement.registerOutParameter(7, Types.BIT);
			callableStatement.registerOutParameter(8, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			jsonObject.put("UserId",callableStatement.getInt(5));
			jsonObject.put("UserName", callableStatement.getString(6));
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
	
	@GET  
    @Path("/userHoldFriendRequest/{actionUserId}/{emailAddressRequest}/{contactNumberRequest}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String userHoldFriendRequest(@PathParam("actionUserId") int actionUserId, @PathParam("emailAddressRequest") String emailAddressRequest,
			@PathParam("contactNumberRequest") String contactNumberRequest) {
		Connection conn = null;
		Statement stmt = null;
		String isError ="";
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			String insertStoreProc = "{call usp_InsertHoldFriendRequest(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, actionUserId);
			callableStatement.setString(2, emailAddressRequest);
			callableStatement.setString(3, contactNumberRequest);
			callableStatement.setTimestamp(4, sqlDateNow);
			callableStatement.registerOutParameter(5, Types.INTEGER);
			callableStatement.executeUpdate();
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
    @Path("/getUserDetailsByUserID/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserDetailsByUserID(@PathParam("userId") int userId ) {

		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserDetails_ByUserID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				jsonObject.put("FirstName", rs.getString("FirstName"));
				jsonObject.put("LastName", rs.getString("LastName"));
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
	@GET  
    @Path("/forgetPassword/{emailname}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String forgetPassword(@PathParam("emailname") String emailname ){
		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		String newPassword = nextSessionId().substring(0,7);
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			SMTPMailSender emailSender = new SMTPMailSender();
			emailSender
					.sendMessage(
							emailname,
							"Reset Password",
							"Your new password is: "+ newPassword
									);
			
			String insertStoreProc = "{call usp_UpdatePassword_ByEmailAddress(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, newPassword );
			callableStatement.setString(2, emailname );
			callableStatement.registerOutParameter(3, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(3) +"";
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
	
	public String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}
	
	public static void main(String args[]){
		AuthenticateUser authenticateUser = new AuthenticateUser();
		//authenticateUser.forgetPassword("dharmakolla85@gmail.com");
		
		authenticateUser.userHoldFriendRequest(5,"dharma_kolla99@gmail.com","9902317358");
	}
}

