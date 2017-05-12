package org.kleverlinks.webservice;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.kleverlinks.bean.AppUserBean;
import org.kleverlinks.bean.CredentialBean;
import org.util.Utility;
import org.util.service.ServiceUtility;
@Path("AuthenticateUserService")
public class AuthenticateUser {

	private SecureRandom random = new SecureRandom();
	// Testing any method

	@GET
	@Path("/testMethod")
	@Produces(MediaType.TEXT_PLAIN)
	public String doSomething() throws Exception {
		System.out.println("This project is running");
		
		//EmailService.sendMail("sunil@thrymr.net", "Testing email", "hiii");
		
	return "Youproject is running";	
	}

	
	@GET  
    @Path("/authenticateUser/{userId}/{deviceRegistrationId}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String authenticateUser(@PathParam("userId") Long userId,@PathParam("deviceRegistrationId") String deviceRegistrationId) {
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
				sql = "INSERT INTO tbl_usertransactions ( UserID ,LoginStatus,LastLoginDateTime,LastLoginIpAddress ) VALUES  ( '" + userId+"' ,1 "
	                 +",'"+ sqlDateNow +"',null )";
				stmt.executeUpdate(sql);
			}

			CallableStatement cs = null;
			String storeProc = "{call usp_InsertUpdateUserGCMCode(?,?,?)}";
			cs = conn.prepareCall(storeProc);
			cs.setLong(1, userId);
			cs.setString(2, deviceRegistrationId);
			cs.registerOutParameter(3, Types.INTEGER);
			cs.executeUpdate();
			
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return jsonObject.toString();
	}
	
	@GET  
    @Path("/userHoldFriendRequest/{actionUserId}/{emailAddressRequest}/{contactNumberRequest}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String userHoldFriendRequest(@PathParam("actionUserId") int actionUserId, @PathParam("emailAddressRequest") String emailAddressRequest,
			@PathParam("contactNumberRequest") String contactNumberRequest) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		String isError ="";
		try {
			conn = DataSourceConnection.getDBConnection();
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		return isError;
	}
	@GET  
    @Path("/getUserDetailsByUserID/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserDetailsByUserID(@PathParam("userId") Long userId ) {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject =	ServiceUtility.getUserDetailByUserId(userId);
			if(jsonObject != null){
				return jsonObject.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		jsonObject.put("status", false);
		jsonObject.put("message", "Oops Something went wrong");
		return jsonObject.toString();
	}
	@GET  
    @Path("/getUserAvatarPath/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserAvatarPath(@PathParam("userId") int userId ) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		String encodedString = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetUserProfileImage(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				encodedString = rs.getString("ProfileImageId");
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		return encodedString;
	}
	@POST
    @Path("/updateUserProfileSetting")
	@Produces(MediaType.APPLICATION_JSON)
	public String updateUserProfileSetting(String userSettingData){
        System.out.println("userSettingData ======  "+userSettingData.toString());
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		try {
			JSONObject jsonObject = new JSONObject(userSettingData);
			AppUserBean appUserBean  = new AppUserBean(jsonObject);
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_UpdateUserProfileSetting(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, appUserBean.getUserId());
			callableStatement.setString(2, appUserBean.getFirstName());
			callableStatement.setString(3, appUserBean.getLastName());
			callableStatement.setString(4, appUserBean.getContactno());
			callableStatement.setDate(5, appUserBean.getDob() != null ?new java.sql.Date(appUserBean.getDob().getTime()):null);
			int isUpdate = callableStatement.executeUpdate();
			if(isUpdate != 0){
				jsonObject.put("status", true);
				jsonObject.put("message", "User setting updated successfully");
				return jsonObject.toString();
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
	   }
		finalJson.put("status", false);
		finalJson.put("message", "Oops Something went wrong");
		
		return finalJson.toString();
	}
	@POST  
    @Path("/forgotPassword")  
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public String forgetPassword(String credential){
		
		JSONObject finalJson = new JSONObject();
		CredentialBean credentialBean = new CredentialBean(new JSONObject(credential));
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {
				JSONObject jsonObject = Utility.checkUserEmailVerification(credentialBean.getEmail());
				if(jsonObject != null){
				
				String sql = "{call usp_resetPassword(?,?,?,?)}";
				conn = DataSourceConnection.getDBConnection();	
				callableStatement = conn.prepareCall(sql);	
				callableStatement.setInt(1, 0);
				callableStatement.setString(2, credentialBean.getPassword());
				callableStatement.setString(3, jsonObject.getString("emailVerificationCode"));
				callableStatement.setString(4, credentialBean.getEmail());
				
			   int isUpdate =	callableStatement.executeUpdate();
				
			   if(isUpdate != 0){

					String message = "<p>HI " + jsonObject.getString("userName")+"</p>";
					message += "<p>Please click on the activation button below to start using FRISSBI</p>";
					message += "<a href=\"" + Constants.SERVER_URL
							+ "/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/"
							+ credentialBean.getEmail() + "/" + jsonObject.getString("emailVerificationCode")
							+ "\" target=\"_parent\"><button>Activate</button></a>";
					message += "<p> OR</p>";
					message += "<p>Copy &amp; paste the below URL into your browser and hit ENTER.</p>";
					message += "<p>" + Constants.SERVER_URL
							+ "/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/"
							+ credentialBean.getEmail() + "/" + jsonObject.getString("emailVerificationCode") + "</p>";
					message += "<p>";
					message += "_________________________________________________________________________________________________________________________________________________________________</p>";
					message += "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(c) Frissbi, product of Kleverlinks Network Pvt Ltd.<br />";
					message += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;www.friss.bi<br />";
					message += "</p>";
					
					EmailService.sendMail(credentialBean.getEmail(), "Frissbi Forget Password",message);
					
					finalJson.put("status", true);
					finalJson.put("message", "User reset password successfully");
					
					return finalJson.toString();
			   }
			}else{
				finalJson.put("status", false);
				finalJson.put("message", "EmailId doesn't exist");
				
				return finalJson.toString();
			}
			} catch(Exception e){
				e.printStackTrace();
			} finally {
				ServiceUtility.closeConnection(conn);
				ServiceUtility.closeCallableSatetment(callableStatement);
			}
			finalJson.put("status", false);
			finalJson.put("message", "Oops Something went wrong");
			
			return finalJson.toString();
	}
	
	public String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}
	
	@GET  
    @Path("/getGCMDeviceRegistrationId/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getGCMDeviceRegistrationId(@PathParam("userId") Long userId ) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetDeviceRegistrationId(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();
			while(rs.next()){
				System.out.println(""+rs.getString("UserID")+"  "+rs.getString("DeviceRegistrationID"));
				jsonObject.put("UserID", rs.getString("UserID"));
				jsonObject.put("DeviceRegistrationID", rs.getString("DeviceRegistrationID"));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		return jsonObject.toString();
	}
	
	@GET  
    @Path("/updateUserPassword/{userId}/{oldPassword}/{newPassword}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String updateUserPassword(@PathParam("userId") int userId,@PathParam("oldPassword") String oldPassword,@PathParam("newPassword") String newPassword ){
		Connection conn = null;
		CallableStatement callableStatement = null;
		String invalid = "";
		try {
			conn = DataSourceConnection.getDBConnection();
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
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
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
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(stmt);
		return "0" ;
	}

}