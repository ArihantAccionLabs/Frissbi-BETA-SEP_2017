package org.kleverlinks.webservice;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import org.mongo.dao.MongoDBJDBC;
import org.util.Utility;
import org.util.service.ServiceUtility;

@Path("UserRegistrationService")
public class UserRegistration {
	private SecureRandom random = new SecureRandom();

	@POST
	@Path("/registerUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String registerUser(String registerationData) {
		JSONObject finalJson = new JSONObject();
		Connection conn = null;
		CallableStatement callableStatement = null;

		JSONObject jsonObject = new JSONObject(registerationData);
		try {
			AppUserBean appUserBean = new AppUserBean(jsonObject);
			
			
			if (appUserBean.getEmail() != null) {

				conn = DataSourceConnection.getDBConnection();
					AppUserBean existedUser = checkEmail(appUserBean.getEmail() , appUserBean.getContactno());
					if (existedUser != null) {
						
						updateDeviceRegId(existedUser.getUserId() , existedUser.getDeviceRegistrationId() , appUserBean.getDeviceRegistrationId());
						if(appUserBean.getIsGmailLogin()){
							finalJson.put("userId", existedUser.getUserId());
						}
						finalJson.put("userName", existedUser.getUsername());
						finalJson.put("status", true);
						finalJson.put("message", "Email or mobile number are already exist");
						return finalJson.toString();
					}
					String contactNumberVerificationCode = phoneVerificationCode();
					String emailVerificationCode = Utility.nextSessionId();
					String insertStoreProc = "{call usp_InsertUserMasterDetails(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
					callableStatement = conn.prepareCall(insertStoreProc);
					callableStatement.setString(1, appUserBean.getUsername());
					callableStatement.setString(2, appUserBean.getPassword());
					callableStatement.setString(3, appUserBean.getEmail());
					callableStatement.setString(4, appUserBean.getContactno());
					callableStatement.setDate(5, appUserBean.getDob() != null ?new java.sql.Date(appUserBean.getDob().getTime()):null);
					callableStatement.setString(6, appUserBean.getFirstName());
					callableStatement.setString(7, appUserBean.getLastName());
					callableStatement.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
					callableStatement.setString(9, contactNumberVerificationCode);
					callableStatement.setString(10, emailVerificationCode);
					callableStatement.setString(11, appUserBean.getDeviceRegistrationId());																			
					callableStatement.setInt(12, appUserBean.getIsGmailLogin() ? 0 : 1);
					callableStatement.registerOutParameter(13, Types.INTEGER);
					callableStatement.registerOutParameter(14, Types.INTEGER);
					int value = callableStatement.executeUpdate();

					int isError = callableStatement.getInt(13);
					Long userId = callableStatement.getLong(14);
					System.out.println("appUserBean     "+value+"   "+userId);
					
					if (value != 0 && userId != 0l) {
                        
						if(Utility.checkValidString(appUserBean.getImage())){
							
							JSONObject imageJson = new JSONObject();
							imageJson.put("userId", userId);
							imageJson.put("file", appUserBean.getImage());
							MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
					    	String mongoFileId = mongoDBJDBC.insertCoverImageToMongoDb(imageJson);
					    	if(mongoFileId != null){
					    		imageJson.remove("file");
					    		imageJson.put("mongoFileId", mongoFileId);
					    		mongoDBJDBC.updateProfileImage(imageJson);
					    	}
						}
						if(!appUserBean.getIsGmailLogin()){
							String lastName =  Utility.checkValidString(appUserBean.getLastName()) ? appUserBean.getLastName() : "";
							String message = "<p>HI " + appUserBean.getFirstName()+ " "+lastName+"</p>";
							message += "<p>Please click on the activation button below to start using FRISSBI</p>";
							message += "<a href=\"" + Constants.SERVER_URL
									+ "/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/"
									+ appUserBean.getEmail() + "/" + emailVerificationCode
									+ "\" target=\"_parent\"><button>Activate</button></a>";
							message += "<p> OR</p>";
							message += "<p>Copy &amp; paste the below URL into your browser and hit ENTER.</p>";
							message += "<p>" + Constants.SERVER_URL
									+ "/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/"
									+ appUserBean.getEmail() + "/" + emailVerificationCode + "</p>";
							message += "<p>";
							message += "_________________________________________________________________________________________________________________________________________________________________</p>";
							message += "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(c) Frissbi, product of Kleverlinks Network Pvt Ltd.<br />";
							message += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;www.friss.bi<br />";
							message += "</p>";
							
							EmailService.sendMail(appUserBean.getEmail(), "Frissbi Account Activation",message);
						}
						finalJson.put("status", true);
						finalJson.put("message", "User registration completed successfully");
						finalJson.put("userId", userId);
						finalJson.put("userName", appUserBean.getUsername());
						
						return finalJson.toString();
					}
				}
		} catch (SQLException se) {

			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
	}

	@POST
	@Path("/user-login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(String userCredential){
		
		JSONObject finalJson = new JSONObject();
		
		AppUserBean appUserBean = new AppUserBean(new JSONObject(userCredential));
		Connection conn = null;
		CallableStatement callableStatement = null;
		try{
		conn = DataSourceConnection.getDBConnection();
		String loginStoreProcedure = "{call usp_userLogin(?,?)}";
		callableStatement = conn.prepareCall(loginStoreProcedure);
		callableStatement.setString(1, appUserBean.getEmail());
		callableStatement.setString(2, appUserBean.getPassword());
		
		ResultSet rs = callableStatement.executeQuery();
		Long userId = null;
		int isEmailVerified = 0;
		while (rs.next()) {
			userId = rs.getLong("UserID");	
			isEmailVerified = rs.getInt("IsEmailVerified");	
		}
       if(userId != null && userId != 0l){
    	   if(isEmailVerified == 0){
    		   finalJson.put("status", false);
        	   finalJson.put("message", "Please verify your email"); 
        	   
        	   return finalJson.toString();
    	   }
    	   AppUserBean existedUser = checkEmail(appUserBean.getEmail() , appUserBean.getContactno());
			if (existedUser != null) {
				updateDeviceRegId(existedUser.getUserId() , existedUser.getDeviceRegistrationId() , appUserBean.getDeviceRegistrationId());
			}
    	   finalJson.put("userId", userId);
    	   finalJson.put("status", true);
    	   finalJson.put("message", "User login successfully");
       }else{
    	   finalJson.put("status", false);
    	   finalJson.put("message", "Your credential is incorrect");
       }
		return finalJson.toString();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		
		return finalJson.toString();
	}

	public String phoneVerificationCode() {
		int phoneverificationCode = new BigInteger(50, random).intValue();
		if (phoneverificationCode < 0) {
			phoneverificationCode *= -1;
		}
		return phoneverificationCode + "";
	}

	public boolean userNameExists(String userName) {

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName + "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				return true;
			}

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(stmt);
		}
		return false;
	}

	@GET
	@Path("/existencestatus/{userId}/{email}")
	@Produces(MediaType.TEXT_PLAIN)
	public String existenceStatusForUsername_Email(@PathParam("userId") Long userId, @PathParam("email") String email) {

		System.out.println(email + "   userId ======" + userId);
		System.out.println("existenceStatusForUsername_Email      :  ");
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		int ifExists = 0;

		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetExistenceStatusFor_UserName_Email(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setString(2, email);
			callableStatement.registerOutParameter(3, Types.BIT);
			callableStatement.execute();
			ifExists = callableStatement.getInt(3);
			System.out.println("ifExists    :  " + ifExists);

			if (ifExists != 0) {
				finalJson.put("message", email + " already exist");
				finalJson.put("isEmailIdExist", true);
			} else {
				finalJson.put("message", email + " not exist");
				finalJson.put("isEmailIdExist", false);
			}
			finalJson.put("status", true);
			return finalJson.toString();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}

	@POST
	@Path("/updateProfilePic/{userId}/{avatarPath}")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateProfilePic(@PathParam("userId") Long userId, @PathParam("avatarPath") String avatarPath) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		String isError = "";
		try {
			conn = DataSourceConnection.getDBConnection();
			// BufferedImage image = null;
			// byte[] imageInByte =
			// avatarPath.getBytes(Charset.forName("UTF-8"));
			// byte[] imageByte;
			System.out.println("length is:" + avatarPath.length() + "  " + avatarPath);
			avatarPath = avatarPath.replace("@", "/");

			// byte[] imageByteArray = new Base64().decode(avatarPath);

			// Write Image into File system - Make sure you update the path
			String name = "c:/Users/frissbijava/Downloads/Images/" + "User-" + userId + ".jpg";
			FileOutputStream imageOutFile = new FileOutputStream(name);
			imageOutFile.write(avatarPath.getBytes());
			imageOutFile.close();

			// System.out.println(avatarPath);
			// try {
			// BASE64Decoder decoder = new BASE64Decoder();
			// imageByte = decoder.decodeBuffer(avatarPath);
			// ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			// image = ImageIO.read(bis);
			// bis.close();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// CompressJPEGFile compressJPEGFile = new CompressJPEGFile();
			// String imagePath = compressJPEGFile.compressImage(avatarPath);
			// String name =
			// "c:/Users/frissbijava/Downloads/Images/"+"User-"+userId+".jpg";
			// File outputfile = new File(name);
			// ImageIO.write(image, "jpg", outputfile);
			// InputStream in = new ByteArrayInputStream(imageInByte);
			// BufferedImage bImageFromConvert = ImageIO.read(in);

			// ImageIO.write(bImageFromConvert, "jpg", new File(name));
			String insertStoreProc = "{call usp_UpdateUserTransactionDetails(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setString(2, name);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.executeUpdate();
			isError = callableStatement.getInt(3) + "";
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return isError;
	}

	@GET
	@Path("/verifyemailaddress/{email}/{verificationcode}")
	@Produces(MediaType.TEXT_PLAIN)
	public String verifyEmailAddress(@PathParam("email") String email,
			@PathParam("verificationcode") String verificationCode) {
		Connection conn = null;
		CallableStatement callableStatement = null;
		String txt = "";
		try {
			JSONObject jsonObject = Utility.checkUserEmailVerification(email);
			if(jsonObject != null){
             if(jsonObject.getInt("isEmailVerified") == 1){
            	 return "You already successfully verfied your email account. Login to frissbi with your emailId and password"; 
             }
			if (jsonObject.getString("emailVerificationCode").equals(verificationCode)) {
				conn = DataSourceConnection.getDBConnection();
			    String storeProc = "{call usp_UpdateEmailVerification(?)}"  ; 
				callableStatement = conn.prepareCall(storeProc);
				callableStatement.setString(1, email);
				
			   int	value = callableStatement.executeUpdate();
               if(value != 0){
            	   txt = "You have successfully verfied your email account. Login to frissbi with your emailId and password";
               }
			} else {
				txt = "There was some problem in verifying your email account";
			}
		}else{
			txt = "This email account is not belong to you";
		}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		return txt;
	}

	@GET
	@Path("/getUsername/{userid}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUsername(@PathParam("userid") Long userID) {
		Connection conn = null;
		Statement stmt = null;
		String username = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userName from tbl_users where userID = " + userID + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				username = rs.getString("userName");
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return username;
	}

	@GET
	@Path("/getUserId/{userName}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserId(@PathParam("userName") String userName) {
		Connection conn = null;
		Statement stmt = null;
		String userId = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where userName = '" + userName + "'";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				userId = rs.getString("userID");
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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return userId;
	}

	@GET
	@Path("/testMethod")
	@Produces(MediaType.TEXT_PLAIN)
	public String doSomething(){
		System.out.println("test Method  :  ");
	return "ok";	
	}
	
	
	@POST
	@Path("/insertProfileImage")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String insertProfileImage(String imageFile) {

		JSONObject finalJson = new JSONObject();
		String mongoFileId = null;
		try {
			JSONObject imageJson = new JSONObject(imageFile);
			MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
			mongoFileId =  mongoDBJDBC.insertImageToMongoDb(imageJson);
			if (mongoFileId != null) {
				imageJson.remove("file");
				imageJson.put("mongoFileId", mongoFileId);
				
				System.out.println("mongoFileId  :  "+mongoFileId);
				
				if (mongoDBJDBC.updateProfileImage(imageJson)) {
					finalJson.put("status", true);
					finalJson.put("message", "image updated successfully");
					
					return finalJson.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
	}

	@GET
	@Path("/getUserImage/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserImage(@PathParam("userId") Long userId) {
        System.out.println("userId     "+userId);
		Connection conn = null;
		CallableStatement callableStatement = null;
		String encodedString = "";
		JSONObject finalJson = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetUserProfile(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				encodedString = rs.getString("ProfileImageId");
			}
			if(encodedString != null && ! encodedString.trim().isEmpty()){
				
				finalJson.put("image", encodedString);
				finalJson.put("status", true);
				finalJson.put("message", "Username exist");
				return finalJson.toString();
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		} 
		finalJson.put("status", false);
		finalJson.put("message", "Oopse something went wrong");
		return finalJson.toString();
	}

	private AppUserBean checkEmail(String emailId,String contactNumber) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql;
		AppUserBean appUserBean = null;
		try {
			conn = DataSourceConnection.getDBConnection();
			if(Utility.checkValidString(contactNumber)){
				sql = "SELECT UserID,DeviceRegistrationID,UserName FROM tbl_users WHERE EmailName=? OR ContactNumber=? limit 1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, emailId);
				pstmt.setString(2, contactNumber);
			}else{
				sql = "SELECT UserID,DeviceRegistrationID,UserName FROM tbl_users WHERE EmailName=? limit 1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, emailId);	
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				appUserBean = new AppUserBean();
				appUserBean.setUsername(rs.getString("UserName"));
				appUserBean.setUserId(rs.getLong("UserID"));
				appUserBean.setDeviceRegistrationId(rs.getString("DeviceRegistrationID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		return appUserBean;
	}
	private void updateDeviceRegId(Long userId , String existedDeviceRegId , String deviceRegId) {
		
		Connection conn = null;
		CallableStatement callableStatement = null;
		try {
			
			if(Utility.checkValidString(deviceRegId)  && !deviceRegId.trim().equals(existedDeviceRegId)){
			
			conn = DataSourceConnection.getDBConnection();
	
		String storeProc = "{call usp_InsertUpdateUserDeviceRegId(?,?,?)}";
		callableStatement = conn.prepareCall(storeProc);
		callableStatement.setLong(1, userId);
		callableStatement.setString(2, deviceRegId);
		callableStatement.registerOutParameter(3, Types.INTEGER);
		int deviceRegIdUpdate = callableStatement.executeUpdate();
		
		System.out.println("deviceRegistrationId   ==  "+deviceRegIdUpdate);
		
		
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
	}
}