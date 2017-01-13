package org.kleverlinks.webservice;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
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

import org.json.JSONObject;
import org.util.service.FreeTimeTracker;
@Path("AuthenticateUserService")
public class AuthenticateUser {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	private SecureRandom random = new SecureRandom();
	
	//Testing any method
	
	@GET
	@Path("/testMethod")
	@Produces(MediaType.TEXT_PLAIN)
	public void doSomething() throws Exception {
           System.out.println("doSomething===========");
           FreeTimeTracker.getUserFreeTime();
			
	}
	
	@GET  
    @Path("/authenticateUser/{userId}/{deviceRegistrationId}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String authenticateUser(@PathParam("userId") int userId,@PathParam("deviceRegistrationId") String deviceRegistrationId) {
		Connection conn = null;
		Statement stmt = null;
		boolean exists = false;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_usertransactions where userId ='" + userId + "'";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				exists = true;
			}
			
			if (exists){
				java.util.Date dateobj = new java.util.Date();
				java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
				sql = "UPDATE tbl_usertransactions SET LastLoginDateTime = '"+ sqlDateNow +"'where userId ='" + userId
						+ "'";
				stmt.executeUpdate(sql);
			}else{
				java.util.Date dateobj = new java.util.Date();
				java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
				sql = "INSERT INTO FrissDB.tbl_usertransactions ( UserID ,LoginStatus,LastLoginDateTime,LastLoginIpAddress ) VALUES  ( '" + userId+"' ,1 "
	                 +",'"+ sqlDateNow +"',null )";
				stmt.executeUpdate(sql);
			}

			CallableStatement cs = null;
			String storeProc = "{call usp_InsertUpdateUserGCMCode(?,?,?)}";
			cs = conn.prepareCall(storeProc);
			cs.setInt(1, userId);
			cs.setString(2, deviceRegistrationId);
			cs.registerOutParameter(3, Types.INTEGER);
			cs.executeUpdate();
			
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
		return "1";
	}

	@GET  
    @Path("/userAuthentication/{username}/{password}/{loginipaddress}/{deviceRegistrationId}/{emailAddress}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String userAuthentication(@PathParam("username") String userName, @PathParam("password") String password,
			@PathParam("loginipaddress") String loginIpAddress,@PathParam("deviceRegistrationId") String deviceRegistrationId
			,@PathParam("emailAddress") String emailAddress) {
		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		int userid = 0;
		String isEmailVerified = isEmailVerified(userName);
		if(isEmailVerified.equals("0")){
			return "-2";
		}
		try {
			conn =  DataSourceConnection.getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserAuthentication(?,?,?,?,?,?,?,?,?)}";
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, userName);
			callableStatement.setString(2, password);
			callableStatement.setTimestamp(3, sqlDateNow);
			callableStatement.setString(4, loginIpAddress);
			callableStatement.setString(5, emailAddress);
			callableStatement.registerOutParameter(6, Types.BIGINT);
			callableStatement.registerOutParameter(7, Types.VARCHAR);
			callableStatement.registerOutParameter(8, Types.BIT);
			callableStatement.registerOutParameter(9, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			userid = callableStatement.getInt(6);
			jsonObject.put("UserId",userid);
			jsonObject.put("UserName", callableStatement.getString(7));
			
			CallableStatement cs = null;
			String storeProc = "{call usp_InsertUpdateUserGCMCode(?,?,?)}";
			cs = conn.prepareCall(storeProc);
			cs.setInt(1, userid);
			cs.setString(2, deviceRegistrationId);
			cs.registerOutParameter(3, Types.INTEGER);
			value = cs.executeUpdate();
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
			conn = DataSourceConnection.getDBConnection();
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
			conn = DataSourceConnection.getDBConnection();
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
    @Path("/getUserAvatarPath/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserAvatarPath(@PathParam("userId") int userId ) {

		Connection conn = null;
		Statement stmt = null;
		String encodedString = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserAvatarPath(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				encodedString = rs.getString("AvatarPath");
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
		return encodedString;
	}
	@GET  
    @Path("/updateUserProfileSetting/{userId}/{firstName}/{lastName}/{gender}/{dateOfBirth}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateUserProfileSetting(@PathParam("userId") int userId,@PathParam("firstName") String firstName,
			@PathParam("lastName") String lastName,@PathParam("gender") String gender,
			@PathParam("dateOfBirth") Date dateOfBirth) {

		Connection conn = null;
		Statement stmt = null;
		String isError ="";
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserProfileSetting(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, firstName);
			callableStatement.setString(3, lastName);
			callableStatement.setString(4, gender);
			callableStatement.setTimestamp(5, new Timestamp(dateOfBirth.getTime()));
			int value = callableStatement.executeUpdate();
			isError = callableStatement.getInt(6) +"";
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
    @Path("/forgetPassword/{emailname}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String forgetPassword(@PathParam("emailname") String emailname ){
		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		String newPassword = nextSessionId().substring(0,7);
		try {
			conn = DataSourceConnection.getDBConnection();
			CallableStatement callableStatement = null;
			/*SMTPMailSender emailSender = new SMTPMailSender();
			emailSender
					.sendMessage(
							emailname,
							"Forgot Password",
							"Your new password is: "+ newPassword
									);*/
			MyEmailer.SendMail(emailname,"Forgot Password","Your new password is: "+ newPassword) ;
			
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
	
	@GET  
    @Path("/getGCMDeviceRegistrationId/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getGCMDeviceRegistrationId(@PathParam("userId") int userId ) {

		Connection conn = null;
		Statement stmt = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserGCMCode(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				jsonObject.put("UserID", rs.getString("UserID"));
				jsonObject.put("DeviceRegistrationID", rs.getString("DeviceRegistrationID"));
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
    @Path("/updateUserPassword/{userId}/{oldPassword}/{newPassword}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String updateUserPassword(@PathParam("userId") int userId,@PathParam("oldPassword") String oldPassword,@PathParam("newPassword") String newPassword ){
		Connection conn = null;
		Statement stmt = null;
		String invalid = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserPassword(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId );
			callableStatement.setString(2, oldPassword );
			callableStatement.setString(3, newPassword );
			callableStatement.registerOutParameter(4, Types.INTEGER);
			callableStatement.registerOutParameter(5, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			invalid = callableStatement.getInt(4) +"";
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
		return invalid;
	}
	
	@GET  
    @Path("/isEmailVerified/{UserName}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String isEmailVerified(@PathParam("UserName") String UserName ) {

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where UserName ='" + UserName
					+ "'" + " and IsEmailVerified = 1 and IsActive = 1 limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				return "1";
			}
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
		return "0" ;
	}
	public static void main(String args[]){
		AuthenticateUser authenticateUser = new AuthenticateUser();
		//authenticateUser.forgetPassword("dharmakolla85@gmail.com");
		
		//authenticateUser.userHoldFriendRequest(5,"dharma_kolla99@gmail.com","9902317358");
		java.util.Date dateobj = new java.util.Date();
		java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
		//System.out.println(authenticateUser.updateUserProfileSetting(40,"Dharmateja85","kolla85","M",sqlDateNow));
		System.out.println(authenticateUser.updateUserPassword(50, "chandu", "teja") );
		System.out.println(authenticateUser.authenticateUser(50, "test"));
	}
	
}