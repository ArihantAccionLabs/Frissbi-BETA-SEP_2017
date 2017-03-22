package org.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.kleverlinks.bean.ActivityBean;
import org.kleverlinks.enums.FriendStatusEnum;
import org.kleverlinks.webservice.DataSourceConnection;
import org.util.service.ServiceUtility;

public class Utility {

	public static Boolean checkValidString(String string) {
		return string != null && !string.trim().isEmpty();
	}
	
public static Map<String , Date> getOneDayDate(String date){
		
		System.out.println("date=============="+date);
		Map<String , Date> map = new HashMap<String , Date>();
		try{
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar now = Calendar.getInstance();
		now.setTime(formatter.parse(date));
		now.set(Calendar.HOUR, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);

		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(now.getTime());
		tomorrow.set(Calendar.HOUR, 23);
		tomorrow.set(Calendar.MINUTE, 59);
		tomorrow.set(Calendar.SECOND, 00);
		//tomorrow.add(Calendar.DATE, 1);
		
		map.put("today", now.getTime());
		map.put("tomorrow", tomorrow.getTime());
		//System.out.println("map=================="+map.toString());
		}catch (Exception e) {
		e.printStackTrace();
		}
		return map;
	}


	public static void  sortList(List<ActivityBean> userActivityBeanList) {
		try{
	     Collections.sort(userActivityBeanList, (p1, p2) -> p2.getDate().compareTo(p1.getDate()));
		}catch (Exception e) {
			e.printStackTrace();
		}
	    }
	
public static Set<String> getFriendUserIdInString( Long userId){
		
		Connection conn = null;
		CallableStatement callableStatement = null;
		Set<String> acceptedUserIds = new HashSet<String>();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_getFriendList(?,?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.setString(2, FriendStatusEnum.ACCEPTED.toString());
			
			ResultSet rs = callableStatement.executeQuery();

			while(rs.next()){
				acceptedUserIds.add(rs.getString("SenderUserID"));
				acceptedUserIds.add(rs.getString("ReceiverUserID"));
			}
			acceptedUserIds.remove(userId+"");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		
		return acceptedUserIds;
	}


public static Set<Long> getFriendUserIdInLong( Long userId){
	
	Connection conn = null;
	CallableStatement callableStatement = null;
	Set<Long> acceptedUserIds = new HashSet<Long>();
	try {
		conn = DataSourceConnection.getDBConnection();
		String insertStoreProc = "{call usp_getFriendList(?,?)}";
		callableStatement = conn.prepareCall(insertStoreProc);
		callableStatement.setLong(1, userId);
		callableStatement.setString(2, FriendStatusEnum.ACCEPTED.toString());
		
		ResultSet rs = callableStatement.executeQuery();

		while(rs.next()){
			acceptedUserIds.add(rs.getLong("SenderUserID"));
			acceptedUserIds.add(rs.getLong("ReceiverUserID"));
		}
		acceptedUserIds.remove(userId);
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
	}
	
	return acceptedUserIds;
}

public static String getFriendStatusByFriendListId(Long friendListId){
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	String requestStatus = "";
	try {
		conn = DataSourceConnection.getDBConnection();
		String sql = "SELECT RequestStatus FROM tbl_userfriendlist WHERE UserFriendListID=?";
		pstmt = conn.prepareStatement(sql);
		pstmt.setLong(1, friendListId);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			requestStatus = rs.getString("RequestStatus");
			
				if((requestStatus.equals(FriendStatusEnum.WAITING.toString()))){
					requestStatus = FriendStatusEnum.CONFIRM.toString();
				}else if(requestStatus.equals(FriendStatusEnum.ACCEPTED.toString())){
					requestStatus = FriendStatusEnum.FRIENDS.toString();
				}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}finally{
	ServiceUtility.closeConnection(conn);
	ServiceUtility.closeSatetment(pstmt);
	}
	return requestStatus;
	
}

public static  JSONObject getGroupInfoById(Long groupId){
	
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	JSONObject jsonObject = new JSONObject();
	try {
		conn = DataSourceConnection.getDBConnection();
		String sql = "SELECT FG.GroupID,FG.GroupName,FG.GroupImage,U.UserID,U.FirstName,U.LastName FROM tbl_Group AS FG  "
				+"INNER JOIN tbl_users AS U ON FG.UserID = U.UserID WHERE FG.GroupID =? AND FG.IsActive=?";	
		pstmt = conn.prepareStatement(sql);
		pstmt.setLong(1, groupId);
		pstmt.setLong(2, 0);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			jsonObject.put("groupName", rs.getString("GroupName"));
			jsonObject.put("groupId", rs.getLong("GroupID"));
			jsonObject.put("groupImageId", rs.getString("GroupImage"));
			jsonObject.put("adminName", rs.getString("FirstName")+" "+rs.getString("LastName"));
		}
	} catch (Exception e) {
		e.printStackTrace();
	}finally{
	ServiceUtility.closeConnection(conn);
	ServiceUtility.closeSatetment(pstmt);
	}
	return jsonObject;
}


}
