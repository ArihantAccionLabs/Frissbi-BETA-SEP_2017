package org.kleverlinks.webservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.service.dto.UserDTO;
import org.util.service.ServiceUtility;


@Path("UserRegistrationService")
public class UserRegistration {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private SecureRandom random = new SecureRandom();

	@GET  
    @Path("/registerUser/{username}/{password}/{email}/{contactno}/{date}/{firstname}/{lastname}/{isGmaillogin}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String registerUser(@PathParam("username") String userName,@PathParam("password") String password, @PathParam("email") String email,
			@PathParam("contactno") String contactNo, @PathParam("date") Date date, @PathParam("firstname") String firstName,
			@PathParam("lastname") String lastName,@PathParam("isGmaillogin") int isGmaillogin) {
		Connection conn = null;
		try {
			conn = getDBConnection();
			
			  if(ServiceUtility.getUserDetailsByUserName(userName) != null){
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
				if(isGmaillogin == 0){
				String message = "<p>HI "+userName+"/"+firstName+",</p>";
		        message += "<p>Please click on the activation button below to start using FRISSBI</p>";
		        message +="<a href=\""+Constants.SERVER_URL+"/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/" +email+"/"+ emailVerificationCode+"\" target=\"_parent\"><button>Activate</button></a>";
		        message += "<p> OR</p>";
		        message +="<p>Copy &amp; paste the below URL into your browser and hit ENTER.</p>";
		        message +="<p>"+Constants.SERVER_URL+"/kleverlinkswebservices/rest/UserRegistrationService/verifyemailaddress/" +email+"/"+ emailVerificationCode+"</p>";
		        message +="<p>";
		        message +="_________________________________________________________________________________________________________________________________________________________________</p>";
		        message +="<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(c) Frissbi, product of Kleverlinks Network Pvt Ltd.<br />";
		        message +="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;www.friss.bi<br />";
		        message +="</p>";
				
				MyEmailer.SendMail(email,"Frissbi Account Activation","Your new password is: "+ message) ;
				
			    send_sms smsObj = new send_sms();
				smsObj.setparams("http://alerts.sinfini.com/","sms","A90334690d3a2b371990936619d8df4b1","SIDEMO");
		        //smsObj.send_sms(contactNo+"", "Your Frissbi account sms verification code is: "+ contactNumberVerificationCode , "");
				}
				return userId+"";
			}
		} catch (SQLException se) {
			
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
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
	public String existenceStatusForUsername_Email(@PathParam("username") String user_Name,
			@PathParam("email") String email) {

		Connection conn = null;
		Statement stmt = null;
		String ifExists = "";
		if(user_Name.equals("null")){
			user_Name = null;
		}
		if(email.equals("null")){
			email = null;
		}
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetExistenceStatusFor_UserName_Email(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, user_Name);
			callableStatement.setString(2, email);
			callableStatement.registerOutParameter(3, Types.BIT);
			callableStatement.execute();
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
	
	@POST  
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
//			BufferedImage image = null;
//			byte[] imageInByte = avatarPath.getBytes(Charset.forName("UTF-8"));
//	        byte[] imageByte;
	        System.out.println("length is:"+ avatarPath.length()+"  "+avatarPath);
	        avatarPath = avatarPath.replace("@", "/");
	        
	       //byte[] imageByteArray = new Base64().decode(avatarPath); 
	        
            // Write Image into File system - Make sure you update the path
	        String name = "c:/Users/frissbijava/Downloads/Images/"+"User-"+userId+".jpg";
            FileOutputStream imageOutFile = new FileOutputStream(name);
            imageOutFile.write(avatarPath.getBytes() );
            imageOutFile.close();
            
            //System.out.println(avatarPath);
//	        try {
//	            BASE64Decoder decoder = new BASE64Decoder();
//	            imageByte = decoder.decodeBuffer(avatarPath);
//	            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
//	            image = ImageIO.read(bis);
//	            bis.close();
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
			//CompressJPEGFile compressJPEGFile = new CompressJPEGFile();
			//String imagePath = compressJPEGFile.compressImage(avatarPath);
//	        String name = "c:/Users/frissbijava/Downloads/Images/"+"User-"+userId+".jpg";
//	        File outputfile = new File(name);
//	        ImageIO.write(image, "jpg", outputfile);
//	        InputStream in = new ByteArrayInputStream(imageInByte);
			//BufferedImage bImageFromConvert = ImageIO.read(in);

			//ImageIO.write(bImageFromConvert, "jpg", new File(name));
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
	
	@GET  
    @Path("/checkUserEmailVerification/{username}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String checkUserEmailVerification(@PathParam("username") String username
			) {

		Connection conn = null;
		Statement stmt = null;
		String isEmailVerified="";
		try {
			conn = getDBConnection();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_CheckUserEmailVerification(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setString(1, username);
			callableStatement.executeUpdate();
			ResultSet rs = callableStatement.getResultSet();
			while(rs.next()){
				isEmailVerified = rs.getString("IsEmailVerified");
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
		return isEmailVerified;
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
	
	@GET  
    @Path("/getUserId/{userName}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String getUserId(@PathParam("userName") String userName){
		Connection conn = null;
		Statement stmt = null;
		String userId = null;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT userID from tbl_users where userName = '" + userName
					+ "'";
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
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return userId;
	}
	
	@GET  
    @Path("/insertImage/{userId}/{encodedString}")  
    @Produces(MediaType.TEXT_PLAIN)
	public String insertImage(@PathParam("userId") int userId,@PathParam("encodedString") String encodedString) {

		Connection conn = null;
		Statement stmt = null;
		String isError ="";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_UpdateUserTransactionDetails(?,?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.setString(2, encodedString);
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
				image = ImageIO.read(new File("c:/Users/knpl004/Downloads/kleverlinks/kleverlinkswebservices/src/org/kleverlinks/webservice/images/charminar.jpg"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        String type="jpg";
	        try {
	            ImageIO.write(image, type, bos);
	            byte[] imageBytes = bos.toByteArray();

	            //BASE64Encoder encoder = new BASE64Encoder();
	            //imageString = encoder.encode(imageBytes);
	            String encodedString = new Base64().encodeToString(imageBytes);
	            //updateProfilePic(50,encodedString.toString());
	            //String testimage ="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";
	            //updateProfilePic(50,testimage);

	            bos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		
		
//	        Bitmap test = new Bitmap();
//	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//	        byte[] imageBytes = baos.toByteArray();
//	        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
		
		
//		byte[] imageInByte;
//		BufferedImage originalImage;
//		try {
//			originalImage = ImageIO.read(new File(
//					"c:/Users/knpl004/Downloads/kleverlinks/kleverlinkswebservices/src/org/kleverlinks/webservice/images/Image.jpg"));
//			// convert BufferedImage to byte array
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ImageIO.write(originalImage, "jpg", baos);
//			baos.flush();
//			imageInByte = baos.toByteArray();
//			baos.close();
//			System.out.println(imageInByte.toString());
//			updateProfilePic(50,imageInByte.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	        //updateProfilePic(50,imageString);
		//System.out.println(imageString);
		// emailIdExists("dharmateja5@kleverlinks.com");
	}
	@GET  
    @Path("/getUserImage/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserImage(@PathParam("userId") int userId) {

		Connection conn = null;
		Statement stmt = null;
		String encodedString ="";
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			CallableStatement callableStatement = null;
			String insertStoreProc = "{call usp_GetUserImage(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setInt(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while(rs.next()){
				encodedString = rs.getString("codedstring");
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
	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;

		UserRegistration userRegistration = new UserRegistration();
		//userRegistration.test();
		//System.out.println(userRegistration.checkUserEmailVerification("dharmakolla96"));
		System.out.println(userRegistration.existenceStatusForUsername_Email("Chandu", null));

	}
}