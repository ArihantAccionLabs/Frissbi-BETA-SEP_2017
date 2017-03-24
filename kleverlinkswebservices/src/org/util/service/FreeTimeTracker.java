package org.util.service;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.kleverlinks.bean.UserFreeTimeBean;
import org.kleverlinks.webservice.DataSourceConnection;
import org.kleverlinks.webservice.NotificationsEnum;
import org.service.dto.NotificationInfoDTO;
import org.util.Utility;

/*
 * @Author -> Sunil Verma
 * @Purpose -> Tracking free time slot in hours for the users who posted their free time  ,getting friend list and sending notification 
 * 
 */

public class FreeTimeTracker {

	public static void getUserFreeTime() {
     
		List<Long> userIdList = new ArrayList<Long>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
		LocalDateTime fromTime = LocalDateTime.now().minusHours(2);
		LocalDateTime toTime = fromTime.plusDays(2);

		System.out.println("fromTime==="+ formatter.format(fromTime)+"  toTime  "+formatter.format(toTime));
		
		String sql = "SELECT UserID,FromDateTime,ToDateTime FROM tbl_UserActivity where FromDateTime BETWEEN ? AND ?";
		 
		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DataSourceConnection.getDBConnection();

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, formatter.format(fromTime));
			pstmt.setString(2, formatter.format(toTime));

			ResultSet rs = pstmt.executeQuery();

			// get All users who posted their free time on current date + 48
		
			while (rs.next()) {
				userIdList.add(rs.getLong("UserID"));
			}
			
			System.out.println("userIdList=================="+userIdList.size());
			
			for (Long userId : userIdList) {  // getting friendlist for every users
				Set<Long> friendList = new HashSet<>();
				sql = null;
				pstmt = null;
				rs = null;
				sql = "SELECT * FROM  tbl_userfriendlist WHERE SenderUserID=? OR ReceiverUserID=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setLong(1, userId);
				pstmt.setLong(2, userId);

				rs = pstmt.executeQuery();
				
				while (rs.next()) { // gettting friendlist
					if (rs.getLong("SenderUserID") != 0 && rs.getLong("ReceiverUserID") != 0) {
						friendList.add(rs.getLong("SenderUserID"));
						friendList.add(rs.getLong("ReceiverUserID"));
					}
				}
				System.out.println("friendList=================="+friendList.size());
				if (friendList.size() > 2) { // if friendlist is more than 2
					
					List<UserFreeTimeBean> timePostedFriendList = timePostedFriendList(friendList.stream().collect(Collectors.toList()));// calling method who will provide friend list who posted
					   System.out.println("timePostedFriendList=="+timePostedFriendList.size());
					List<UserFreeTimeBean> checkingFreePostedDateMatchingList = checkingFreePostedDateMatching(timePostedFriendList);	// timePostedFriendList(friendList);
			        System.out.println("checkingFreePostedDateMatchingList=="+checkingFreePostedDateMatchingList.size());
			      checkingFreeTimeSlot(checkingFreePostedDateMatchingList);// checking their free time slot is matching or not

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
			}
	}

	// checking friend list who posted his time
	public static List<UserFreeTimeBean> timePostedFriendList(List<Long> friendList)
			throws SQLException, IOException, PropertyVetoException {

		List<UserFreeTimeBean> timePostedFriendList = new ArrayList<UserFreeTimeBean>();
		for (Long integer : friendList) {

			String sql = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			sql = "SELECT U.FirstName , U.LastName , U.UserID , UA.FromDateTime , UA.ToDateTime   FROM tbl_users AS U INNER JOIN  tbl_UserActivity AS UA ON U.UserID= UA.UserID WHERE UA.UserID=?";
			Connection conn = DataSourceConnection.getDBConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, integer);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				if(Utility.checkValidString(rs.getString("FromDateTime"))){
					
				UserFreeTimeBean userFreeTimeBean = new UserFreeTimeBean();
				userFreeTimeBean.setUserId(rs.getLong("UserID"));
				userFreeTimeBean.setFirstName(rs.getString("FirstName"));
				userFreeTimeBean.setLastName(rs.getString("LastName"));
				// userFreeTimeBean.setStartTime();
              
				System.out.println("FromDateTime  :  "+rs.getString("FromDateTime"));
				
				LocalDateTime fromTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("FromDateTime"));
				LocalDateTime toTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("ToDateTime"));
				userFreeTimeBean.setFreeFromTime(fromTime);
				userFreeTimeBean.setFreeToTime(toTime);
				userFreeTimeBean.setStartTime(Float.parseFloat(fromTime.getHour()+"."+fromTime.getMinute()));
				userFreeTimeBean.setEndTime(Float.parseFloat(toTime.getHour()+"."+toTime.getMinute()));

				timePostedFriendList.add(userFreeTimeBean);
				}
			}
		}
		System.out.println("timePostedFriendList====="+timePostedFriendList.size());
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
			LocalDate selectedDate = userFreeTimeBean.getFreeFromTime().toLocalDate();

			if (currentDate.equals(selectedDate)) {
				todayPostedDateMatchingFriendList.add(userFreeTimeBean);
			} else if (tomorrowDate.equals(selectedDate)) {
				tomorrowPostedDateMatchingFriendList.add(userFreeTimeBean);
			} else if (afterTomorrowDate.equals(selectedDate)) {
				afterTomorrowPostedDateMatchingFriendList.add(userFreeTimeBean);
			}

		}

		if (todayPostedDateMatchingFriendList.size() > tomorrowPostedDateMatchingFriendList.size()
				&& todayPostedDateMatchingFriendList.size() > afterTomorrowPostedDateMatchingFriendList.size()) {
			return todayPostedDateMatchingFriendList;
		} else if (tomorrowPostedDateMatchingFriendList.size() > todayPostedDateMatchingFriendList.size()
				&& tomorrowPostedDateMatchingFriendList.size() > afterTomorrowPostedDateMatchingFriendList.size()) {
			return tomorrowPostedDateMatchingFriendList;
		} else {
			return afterTomorrowPostedDateMatchingFriendList;
		}

	}

	public static void checkingFreeTimeSlot(List<UserFreeTimeBean> freePostedDateMatchingFriendList) {
	
		Float arrays[][] = new Float[freePostedDateMatchingFriendList.size()][freePostedDateMatchingFriendList.size()];
		for (int i = 0; i < freePostedDateMatchingFriendList.size(); i++) {
			
			for (int j = 0; j < freePostedDateMatchingFriendList.size(); j++) {
				arrays[i][j] = getMatrix(freePostedDateMatchingFriendList.get(i),freePostedDateMatchingFriendList.get(j));
			}
		}
		
		UndirectedGraph<Integer, DefaultEdge> g = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		for (int i = 0; i < freePostedDateMatchingFriendList.size(); i++) {

			g.addVertex(i);
		}
		int minimumOverLap = 1;
		for (int i = 0; i < freePostedDateMatchingFriendList.size(); i++) {

			for (int j = 0; j < freePostedDateMatchingFriendList.size(); j++) {

				if (arrays[i][j] >= minimumOverLap) {
					g.addEdge(i,j);
				}
			}
		}
		
		BronKerboschCliqueFinder<Integer, DefaultEdge> bronKerboschCliqueFinder = new BronKerboschCliqueFinder<Integer, DefaultEdge>(g);	
		System.out.println("==============" + bronKerboschCliqueFinder.getAllMaximalCliques().toString());
		
		for (Object object : bronKerboschCliqueFinder.getAllMaximalCliques()) {
			
			String array= removeBracket(object.toString());
			
			String [] arr = array.split(",");
			List<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < arr.length; i++) {
				list.add(Integer.parseInt(arr[i].trim()));
			}
			if(list.size() > 2){
				Float min = 25f;
				int index1 = -1;
				int index2 = -1;
				for (int i = 0; i < list.size(); i++) {
					for (int j = i+1; j < list.size(); j++) {
						if(arrays[list.get(i)][list.get(j)] < min){
							min = arrays[list.get(i)][list.get(j)];
							index1 = list.get(i);
							index2 = list.get(j);
						}
					}
				}
				
				getFreeSlotToMeetingCreation(index1 , index2 , freePostedDateMatchingFriendList , list);//calling this method to send the notification
			}
		}

	}

	public static Float getMatrix(UserFreeTimeBean user1, UserFreeTimeBean user2) {

		if (user1.getUserId().equals(user2.getUserId())) {
			return -1f;
		}

		if (user1.getStartTime() < user2.getEndTime() && user2.getStartTime() < user1.getEndTime()) {
			return user2.getEndTime() - user1.getStartTime() > user1.getEndTime() - user2.getStartTime()
					? user1.getEndTime() - user2.getStartTime() : user2.getEndTime() - user1.getStartTime();
		} else {
			return 0f;
		}
	}
  /*
   * @Author -> Sunil Verma
   * @Work -> To get the start and end time  slot and sending notification to app users
   * 
   */
	
	
	public static void getFreeSlotToMeetingCreation(Integer index1 , Integer index2 , List<UserFreeTimeBean> beanList , List<Integer> indexList){
		
		Collections.sort(indexList);
		
		Float startFreeSlot = 0f;
		Float endFreeSlot = 0f;
		Float e2s1 = beanList.get(index2).getEndTime() - beanList.get(index1).getStartTime(); 
		Float e1s2 = beanList.get(index1).getEndTime() - beanList.get(index2).getStartTime(); 
        
        
	   if(e2s1 > e1s2){
		   startFreeSlot = beanList.get(index2).getStartTime();
		   endFreeSlot = beanList.get(index1).getEndTime();
	   }else{
		   startFreeSlot = beanList.get(index1).getStartTime();
		   endFreeSlot = beanList.get(index2).getEndTime();
	   }
	   
	   System.out.println("startFreeSlot==="+startFreeSlot+"  endFreeSlot===="+endFreeSlot+"===="+indexList.toString());
	   
	   //sending the notification from here
	   for (int i = 0; i < indexList.size(); i++) {
		   String message = "On date "+ beanList.get(0).getFreeFromTime().toLocalDate()+" from "+startFreeSlot+" to "+endFreeSlot +" You and your friends ";
		 
		   int counter = 1;
		   for (Integer integer : indexList) {
			   if(indexList.get(i) != integer){
				  
				   if(counter == 1){
					   
					   message += beanList.get(integer).getFirstName()+" "+beanList.get(integer).getLastName() +", ";
				   }else{
					   message += beanList.get(integer).getFirstName()+" "+beanList.get(integer).getLastName();
				   }
				   counter++;
			   }
			  
			   if(counter > 2){
				   break;
			   }
		   }
		   message += " and " + (indexList.size()-counter) +" others are free . You want to meet him";
		   
		   NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
		   notificationInfoDTO.setUserId(beanList.get(indexList.get(i)).getUserId());
		   notificationInfoDTO.setMessage(message);
		   notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_SUGGESTION.toString());
		   
		   NotificationService.sendMeetingSuggestionNotification(notificationInfoDTO);//sending notification
	  }
	   
	}

	public static String removeBracket(String value){
		
		String trimmedValue = value.trim();
		trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 1);
		return trimmedValue;
	}

}
