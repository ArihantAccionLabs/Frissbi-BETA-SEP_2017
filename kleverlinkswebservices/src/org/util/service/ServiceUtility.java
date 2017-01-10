package org.util.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.kleverlinks.webservice.Constants;
import org.service.dto.UserDTO;

public class ServiceUtility {

	public static UserDTO getUserDetailsByUserId(Integer userId) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT emailName,firstName,lastName FROM tbl_users WHERE userId ='" + userId + "'" + " limit 1";
			rs = stmt.executeQuery(sql);
			userDTO = new UserDTO();
			while (rs.next()) {
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setFullName(rs.getString("firstName") + rs.getString("lastName"));
			}
			return userDTO;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static UserDTO getUserDetailsByUserName(String userName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		UserDTO userDTO = null;
		String sql;
		try {
			conn = getDBConnection();
			stmt = conn.createStatement();
			sql = "SELECT userId,emailName,firstName,lastName FROM tbl_users WHERE userName ='" + userName + "'" + " limit 1";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				userDTO = new UserDTO();
				userDTO.setEmailId(rs.getString("emailName"));
				userDTO.setUserId(rs.getInt("userId"));
				userDTO.setFullName(rs.getString("firstName")+"" + rs.getString("lastName")+"");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userDTO;

	}
	
	private static Connection getDBConnection() {

		Connection dbConnection = null;

		try {
			Class.forName(Constants.JDBC_DRIVER);

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
}
