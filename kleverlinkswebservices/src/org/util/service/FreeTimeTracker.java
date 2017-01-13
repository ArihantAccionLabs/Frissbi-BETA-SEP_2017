package org.util.service;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kleverlinks.bean.UserFreeTimeBean;
import org.kleverlinks.webservice.DataSourceConnection;

public class FreeTimeTracker {

	public static void getUserFreeTime() {

		List<UserFreeTimeBean> userIdList = new ArrayList<UserFreeTimeBean>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd hh:mm:ss a");
		LocalDateTime fromTime = LocalDateTime.now();
		LocalDateTime toTime = fromTime.plusDays(2);
		System.out.println("fromTime======" + formatter.format(fromTime) + "=============" + formatter.format(toTime));

		String sql = "SELECT * FROM tbl_UserFreeTimes where FromDateTime BETWEEN ? AND ?";
		// formatter.format(fromTime)+ "' AND '" + formatter.format(toTime) +
		// "'";
		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DataSourceConnection.getDBConnection();

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, formatter.format(fromTime));
			pstmt.setString(2, formatter.format(toTime));

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				UserFreeTimeBean userFreeTimeBean = new UserFreeTimeBean();
				userFreeTimeBean.setUserId(rs.getInt("UserID"));
				userFreeTimeBean.setFreeFromTime(rs.getTimestamp("FromDateTime"));
				userFreeTimeBean.setFreeToTime(rs.getTimestamp("ToDateTime"));
				userFreeTimeBean.setDescription(rs.getString("Description"));

				System.out.println("userFreeTimeBean===" + userFreeTimeBean.toString());
				userIdList.add(userFreeTimeBean);
			}
			for (UserFreeTimeBean userFreeTimeBean : userIdList) {
				List<Integer> friendList = new ArrayList<>();
				sql = null;
				pstmt = null;
				rs = null;
				System.out.println("==================" + userFreeTimeBean.getUserId());
				sql = "SELECT * FROM  tbl_userfriendlist WHERE UserID1=? OR UserID2=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, userFreeTimeBean.getUserId());
				pstmt.setInt(2, userFreeTimeBean.getUserId());

				rs = pstmt.executeQuery();

				while (rs.next()) {

					System.out.println("" + rs.getInt("UserID1") + "====" + rs.getInt("UserID2") + "====");
					if (rs.getInt("UserID1") != 0 && !friendList.contains(rs.getInt("UserID1"))) {
						friendList.add(rs.getInt("UserID1"));
					}
				}

				if (friendList.size() > 2) {

					// calling method who will provide friend list who posted
					//timePostedFriendList(friendList);
					// checking their free time slot is matching or not
					checkingFreePostedDateMatching(timePostedFriendList(friendList));
				}
				/*for (Integer integer : friendList) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + integer);
					
					
				}*/
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// checking friend list who posted his time
	public static List<UserFreeTimeBean> timePostedFriendList(List<Integer> friendList)
			throws SQLException, IOException, PropertyVetoException {

		List<UserFreeTimeBean> timePostedFriendList = new ArrayList<UserFreeTimeBean>();
		for (Integer integer : friendList) {

			String sql = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			sql = "SELECT * FROM  tbl_UserFreeTimes WHERE UserID=?";
			Connection conn = DataSourceConnection.getDBConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, integer);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				UserFreeTimeBean userFreeTimeBean = new UserFreeTimeBean();
				userFreeTimeBean.setUserId(rs.getInt("UserID"));
				userFreeTimeBean.setFreeFromTime(rs.getTimestamp("FromDateTime"));
				userFreeTimeBean.setFreeToTime(rs.getTimestamp("ToDateTime"));
				userFreeTimeBean.setDescription(rs.getString("Description"));

				System.out.println("userFreeTimeBean===" + userFreeTimeBean.toString());
				timePostedFriendList.add(userFreeTimeBean);
			}
		}
		return timePostedFriendList;
	}

	public static List<UserFreeTimeBean> checkingFreePostedDateMatching(List<UserFreeTimeBean> timePostedFriendList) {
		List<UserFreeTimeBean> freePostedDateMatchingFriendList = new ArrayList<UserFreeTimeBean>();
		int counter = 1;
		for (UserFreeTimeBean userFreeTimeBean : timePostedFriendList) {
			Date date = userFreeTimeBean.getFreeFromTime();
			if (counter != 1) {
				if (date.equals(userFreeTimeBean.getFreeFromTime())) {
					freePostedDateMatchingFriendList.add(userFreeTimeBean);
				}
			}
		}

		return freePostedDateMatchingFriendList;
	}
	
	public static List<UserFreeTimeBean> checkingFreeTimeSlot(List<UserFreeTimeBean> freePostedDateMatchingFriendList) {
		List<UserFreeTimeBean> freeTimeSlotFriendList = new ArrayList<UserFreeTimeBean>();
		for (UserFreeTimeBean userFreeTimeBean : freePostedDateMatchingFriendList) {
			
		}

		return freeTimeSlotFriendList;
	}
	
}
