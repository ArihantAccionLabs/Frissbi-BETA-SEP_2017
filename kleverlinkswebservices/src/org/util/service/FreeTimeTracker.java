package org.util.service;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.kleverlinks.bean.UserFreeTimeBean;
import org.kleverlinks.webservice.DataSourceConnection;

public class FreeTimeTracker {

	public static void getUserFreeTime() {

		List<UserFreeTimeBean> userIdList = new ArrayList<UserFreeTimeBean>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd hh:mm:ss a");
		LocalDateTime fromTime = LocalDateTime.now();
		LocalDateTime toTime = fromTime.plusDays(2);

		String sql = "SELECT * FROM tbl_UserFreeTimes where FromDateTime BETWEEN ? AND ?";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DataSourceConnection.getDBConnection();

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, formatter.format(fromTime));
			pstmt.setString(2, formatter.format(toTime));

			ResultSet rs = pstmt.executeQuery();

			// get All users who posted their free time on current date + 48
			// hours till
			while (rs.next()) {
				UserFreeTimeBean userFreeTimeBean = new UserFreeTimeBean();
				userFreeTimeBean.setUserId(rs.getInt("UserID"));
				userFreeTimeBean.setFreeFromTime(rs.getTimestamp("FromDateTime"));
				userFreeTimeBean.setFreeToTime(rs.getTimestamp("ToDateTime"));
				userFreeTimeBean.setDescription(rs.getString("Description"));

				// System.out.println("userFreeTimeBean===" +
				// userFreeTimeBean.toString());
				userIdList.add(userFreeTimeBean);
			}
			// getting friendlist for every users
			int i = 1;
			for (UserFreeTimeBean userFreeTimeBean : userIdList) {
				Set<Integer> friendList = new HashSet<>();
				sql = null;
				pstmt = null;
				rs = null;
				sql = "SELECT * FROM  tbl_userfriendlist WHERE UserID1=? OR UserID2=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, userFreeTimeBean.getUserId());
				pstmt.setInt(2, userFreeTimeBean.getUserId());

				rs = pstmt.executeQuery();

				// gettting friendlist
				while (rs.next()) {
					// System.out.println("====================" +
					// rs.getInt("UserID1"));
					if (rs.getInt("UserID1") != 0 && rs.getInt("UserID2") != 0) {
						friendList.add(rs.getInt("UserID1"));
						friendList.add(rs.getInt("UserID2"));
					}
				}
				// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+friendList.toString());
				// if friendlist is more than 2
				if (friendList.size() > 2) {

					// calling method who will provide friend list who posted
					List<UserFreeTimeBean> timePostedFriendList = timePostedFriendList(
							friendList.stream().collect(Collectors.toList()));
					// System.out.println(">>>>>>timePostedFriendList>>>>>>>>"+timePostedFriendList.size());
					// timePostedFriendList(friendList);
					List<UserFreeTimeBean> checkingFreePostedDateMatchingList = checkingFreePostedDateMatching(
							timePostedFriendList);
					 //System.out.println(">>>>>>checkingFreePostedDateMatchingList>>>>>>>>"+checkingFreePostedDateMatchingList.size());
					// checking their free time slot is matching or not
					Map<String, List<Integer>> map = checkingFreeTimeSlot(checkingFreePostedDateMatchingList);

					map.forEach((k, v) -> System.out.println(
							"Key>>>>>>>>>>>>>>>>>> : " + k + " Value>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> : " + v));
				}
				if (i == 1) {
					break;
				}
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
				// userFreeTimeBean.setStartTime();

				LocalDateTime fromTime = LocalDateTime.ofInstant(userFreeTimeBean.getFreeFromTime().toInstant(),
						ZoneId.systemDefault());
				LocalDateTime toTime = LocalDateTime.ofInstant(userFreeTimeBean.getFreeToTime().toInstant(),
						ZoneId.systemDefault());
				userFreeTimeBean.setStartTime(fromTime.getHour());
				userFreeTimeBean.setEndTime(toTime.getHour());
				// System.out.println("userFreeTimeBean===" +
				// userFreeTimeBean.toString());
				timePostedFriendList.add(userFreeTimeBean);
			}
		}
		return timePostedFriendList;
	}

	// checking friend list who have posted his time at the same Date
	public static List<UserFreeTimeBean> checkingFreePostedDateMatching(List<UserFreeTimeBean> timePostedFriendList) {
		List<UserFreeTimeBean> todayPostedDateMatchingFriendList = new ArrayList<UserFreeTimeBean>();
		List<UserFreeTimeBean> tomorrowPostedDateMatchingFriendList = new ArrayList<UserFreeTimeBean>();
		List<UserFreeTimeBean> afterTomorrowPostedDateMatchingFriendList = new ArrayList<UserFreeTimeBean>();
		for (UserFreeTimeBean userFreeTimeBean : timePostedFriendList) {
			LocalDate currentDate = LocalDateTime.now().toLocalDate();
			LocalDate tomorrowDate = LocalDateTime.now().toLocalDate().plusDays(1);
			LocalDate afterTomorrowDate = LocalDateTime.now().toLocalDate().plusDays(2);
			LocalDate selectedDate = userFreeTimeBean.getFreeFromTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			
			if (currentDate.equals(selectedDate)) {
				todayPostedDateMatchingFriendList.add(userFreeTimeBean);
			} else if (tomorrowDate.equals(selectedDate)) {
				tomorrowPostedDateMatchingFriendList.add(userFreeTimeBean);
			} else if (afterTomorrowDate.equals(selectedDate)) {
				afterTomorrowPostedDateMatchingFriendList.add(userFreeTimeBean);
			}

		}
		
		if(todayPostedDateMatchingFriendList.size() > tomorrowPostedDateMatchingFriendList.size() && todayPostedDateMatchingFriendList.size() > afterTomorrowPostedDateMatchingFriendList.size()){
		    //System.out.println(">>>>>>todayPostedDateMatchingFriendList>>>>>>>>>"+todayPostedDateMatchingFriendList.size());
			return todayPostedDateMatchingFriendList;
		}else if(tomorrowPostedDateMatchingFriendList.size() > todayPostedDateMatchingFriendList.size() && tomorrowPostedDateMatchingFriendList.size() > afterTomorrowPostedDateMatchingFriendList.size()){
			return tomorrowPostedDateMatchingFriendList;
		}else{
			return afterTomorrowPostedDateMatchingFriendList;
		}
		
	}

/*	public static List<UserFreeTimeBean> sortAccordingToSatrtTime(
			List<UserFreeTimeBean> freePostedDateMatchingFriendList) {
		   Collections.sort(freePostedDateMatchingFriendList, (p1, p2) -> p1.getStartTime().compareTo(p2.getStartTime()));
		return freePostedDateMatchingFriendList;
	}*/

	/*
	 * Date in = new Date(); LocalDateTime ldt =
	 * LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()); Date out
	 * = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	 */
	// checking friend list who have posted his time on the same Date and some
	// common hours
	public static Map<String, List<Integer>> checkingFreeTimeSlot(List<UserFreeTimeBean> freePostedDateMatchingFriendList) {
		Map<String, List<Integer>> map = new HashMap<>();
	      System.out.println("checkingFreeTimeSlot============="+freePostedDateMatchingFriendList.size());
           List<List<UserFreeTimeBean>> lastList = new ArrayList<List<UserFreeTimeBean>>();
          /* for(int i = 0; i < freePostedDateMatchingFriendList.size(); i++){
        	   lastList.add(freePostedDateMatchingFriendList);
           }*/
           int arrays[][] = new int[freePostedDateMatchingFriendList.size()][freePostedDateMatchingFriendList.size()]; 
           for (int i = 0; i < freePostedDateMatchingFriendList.size(); i++) {
            //   List<UserFreeTimeBean> list = lastList.get(i);
               for (int j = 0; j < freePostedDateMatchingFriendList.size(); j++) {
                   //arrays[i][j] = freePostedDateMatchingFriendList.get(i).get(j);
            	   arrays[i][j]  =  getMatrix(freePostedDateMatchingFriendList.get(i) , freePostedDateMatchingFriendList.get(j));
               }
           }
           for(int i=0; i<arrays.length; i++) {
               for(int j=0; j<arrays[i].length; j++) {
                   System.out.println("Values at arr["+i+"]["+j+"] is "+arrays[i][j]);
               }
               
               
           }

            UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
           //BronKerboschCliqueFinder.getAllMaximalCliques();
            
            for (int i = 0; i < freePostedDateMatchingFriendList.size(); i++) {
				
            	g.addVertex(freePostedDateMatchingFriendList.get(i).getUserId().toString());
			}
            int minimumOverLap = 1;
            for (int i = 0; i < freePostedDateMatchingFriendList.size(); i++) {
				
            	for (int j = 0; j < freePostedDateMatchingFriendList.size(); j++) {
            		
                	if(arrays[i][j] >= minimumOverLap){
                		g.addEdge(freePostedDateMatchingFriendList.get(i).getUserId().toString(), freePostedDateMatchingFriendList.get(j).getUserId().toString());
                	}
    			}
			}
            BronKerboschCliqueFinder bronKerboschCliqueFinder = new BronKerboschCliqueFinder(g);
            bronKerboschCliqueFinder.getAllMaximalCliques();
            System.out.println("==="+bronKerboschCliqueFinder.getAllMaximalCliques().toString());
		return map;
	}

	public static int getMatrix(UserFreeTimeBean user1 , UserFreeTimeBean user2){
	 
		if(user1.getUserId().equals(user2.getUserId())){
			return -1;
		}
		
		if(user1.getStartTime() < user2.getEndTime() && user2.getStartTime() < user1.getEndTime() ){
			return user2.getEndTime() - user1.getStartTime() > user1.getEndTime() - user2.getStartTime() ? user1.getEndTime() - user2.getStartTime() :user2.getEndTime() -  user1.getStartTime();
		}else {
			return 0;
		}
		
	}
	
	

}
