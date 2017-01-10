package org.kleverlinks.webservice;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.security.SecureRandom;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kleverlinks.webservice.images.CompressJPEGFile;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


@Path("UserRegistrationService")
public class UserRegistration {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://frissdb.cloudapp.net/FrissDB";

	// Database credentials
	static final String USER = "Friss_App_User";
	static final String PASS = "FrissApp2015!";

	private SecureRandom random = new SecureRandom();

	@GET  
    @Path("/registerUser/{username}/{password}/{email}/{contactno}/{date}/{firstname}/{lastname}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String registerUser(@PathParam("username") String userName,@PathParam("password") String password, @PathParam("email") String email,
			@PathParam("contactno") String contactNo, @PathParam("date") Date date, @PathParam("firstname") String firstName,
			@PathParam("lastname") String lastName) {

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " limit 1";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				return "-2";
			}
			java.util.Date dateobj = new java.util.Date();
			java.sql.Timestamp sqlDateNow = new Timestamp(dateobj.getTime());
			String contactNumberVerificationCode = phoneVerificationCode(); 
			String emailVerificationCode = nextSessionId();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_InsertUserMasterDetails(?,?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, userName);
			callableStatement.setString(2, password);
			callableStatement.setString(3, email);
			callableStatement.setString(4, contactNo);
			callableStatement.setDate(5, date);
			callableStatement.setString(6, firstName);
			callableStatement.setString(7, lastName);
			callableStatement.setTimestamp(8, sqlDateNow);
			callableStatement.setString(9, contactNumberVerificationCode);
			callableStatement.setString(10, emailVerificationCode);
			callableStatement.registerOutParameter(11, Types.INTEGER);
			callableStatement.registerOutParameter(12, Types.INTEGER);
			int value = callableStatement.executeUpdate();

			int isError = callableStatement.getInt(11);
			int userId = callableStatement.getInt(12);

			if (isError == 0) {
				System.out
						.println("Stored procedure executed and inserted data");
				SMTPMailSender emailSender = new SMTPMailSender();
				emailSender
						.sendMessage(
								email,
								"Frissbi - Confirm your email address",
								"Please click on this "
										+ "frisbbi email verification link to confirm the address: http://192.168.1.15:8080/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/" +email+"/"+ emailVerificationCode );
				send_sms smsObj = new send_sms();
				smsObj.setparams("http://alerts.sinfini.com/","sms","A90334690d3a2b371990936619d8df4b1","SIDEMO");
		        smsObj.send_sms(contactNo+"", "Your Frissbi account sms verification code is: "+ contactNumberVerificationCode , "");
				return userId+"";
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
		return "-1";
	}

	public String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}
	
	public String phoneVerificationCode() {
		int phoneverificationCode = new BigInteger(50, random).intValue();
		if ( phoneverificationCode < 0){
			phoneverificationCode *=-1;
		}
		return phoneverificationCode+"";
	}

	public boolean userNameExists(String userName) {

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where username ='" + userName
					+ "'" + " limit 1";
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
    @Path("/existencestatus/{username}/{email}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String existenceStatusForUsername_Email(@PathParam("username") String userName,
			@PathParam("email") String email) {

		Connection conn = null;
		Statement stmt = null;
		String ifExists = "";
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetExistenceStatusFor_UserName_Email(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, userName);
			callableStatement.setString(2, email);
			callableStatement.registerOutParameter(3, Types.BIT);
			int value = callableStatement.executeUpdate();
			ifExists = callableStatement.getInt(3)+"";
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
		return ifExists;
	}
	
	@GET  
    @Path("/updateProfilePic/{userId}/{avatarPath}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String updateProfilePic(@PathParam("userId") int userId,
			@PathParam("avatarPath") String avatarPath) {

		Connection conn = null;
		Statement stmt = null;
		String isError = "";
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			BufferedImage image = null;
	        byte[] imageByte;
	        avatarPath = avatarPath.replace("@", "/");
	        try {
	            BASE64Decoder decoder = new BASE64Decoder();
	            imageByte = decoder.decodeBuffer(avatarPath);
	            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
	            image = ImageIO.read(bis);
	            bis.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
			//CompressJPEGFile compressJPEGFile = new CompressJPEGFile();
			//String imagePath = compressJPEGFile.compressImage(avatarPath);
	        String name = "c:/Users/knpl004/Downloads/Images/"+"User-"+userId+".jpg";
	        File outputfile = new File(name);
	        ImageIO.write(image, "jpg", outputfile);
			String insertStoreProc = "{call usp_UpdateUserTransactionDetails(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, name);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.executeUpdate();
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
    @Path("/verifyemailaddress/{email}/{verificationcode}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String verifyEmailAddress(@PathParam("email") String email,
			@PathParam("verificationcode") String verificationCode) {

		Connection conn = null;
		Statement stmt = null;
		String dbVerificationCode = "";
		String txt="";
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUpdateVerificationCode(?,?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, email);
			callableStatement.setInt(2, 0);
			callableStatement.registerOutParameter(3, Types.VARCHAR);
			callableStatement.registerOutParameter(4, Types.INTEGER);
			int value = callableStatement.executeUpdate();
			dbVerificationCode = callableStatement.getString(3);
			
			if(dbVerificationCode.equals(verificationCode) ){
				System.out.println("Successfully verified your email account");
				txt = "You have successfully verfied your email account. Login to frissbi with your username and password";
				callableStatement = null;
				insertStoreProc = "{call usp_GetUpdateVerificationCode(?,?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setString(1, email);
				callableStatement.setInt(2, 1);
				callableStatement.registerOutParameter(3, Types.VARCHAR);
				callableStatement.registerOutParameter(4, Types.INTEGER);
				value = callableStatement.executeUpdate();
				
			}else{
				txt = "There was some problem in verifying your email account";
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
		return txt;
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

	@GET  
    @Path("/getUsername/{userid}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String getUsername(@PathParam("userid") int userID){
		Connection conn = null;
		Statement stmt = null;
		String username = null;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userName from tbl_users where userID = " + userID
					+ " limit 1";
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
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return username;
	}
	
	public void test() {
		Date date = new Date(2015 - 02 - 01);
//		registerUser("xyz123", "xyz123", "a@a.com",
//				"8470017317" , date, "Dharmateja", "Kolla");
		existenceStatusForUsername_Email("dharmakolla50",
				null);
		 String imageString = null;
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        BufferedImage image=null;
			try {
				image = ImageIO.read(new File("c:/Users/knpl004/Downloads/kleverlinks/kleverlinkswebservices/src/org/kleverlinks/webservice/images/Image.jpg"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        String type="jpg";
	        try {
	            ImageIO.write(image, type, bos);
	            byte[] imageBytes = bos.toByteArray();

	            BASE64Encoder encoder = new BASE64Encoder();
	            imageString = encoder.encode(imageBytes);

	            bos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		updateProfilePic(50,imageString);
		System.out.println(imageString);
		// emailIdExists("dharmateja5@kleverlinks.com");
	}

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;

		UserRegistration userRegistration = new UserRegistration();
		userRegistration.test();

	}
}