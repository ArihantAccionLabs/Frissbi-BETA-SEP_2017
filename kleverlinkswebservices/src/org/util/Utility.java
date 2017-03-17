package org.util;

import java.sql.CallableStatement;
import java.sql.Connection;
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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.kleverlinks.bean.ActivityBean;
import org.kleverlinks.enums.FriendStatusEnum;
import org.kleverlinks.webservice.DataSourceConnection;
import org.mongo.dao.MongoDBJDBC;
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
	
public static Set<String> getFriendList( Long userId){
		
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
}
