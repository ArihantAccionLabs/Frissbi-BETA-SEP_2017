package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.MeetingBean;
import org.kleverlinks.bean.MeetingLogBean;
import org.kleverlinks.enums.MeetingStatus;
import org.service.dto.NotificationInfoDTO;
import org.service.dto.UserDTO;
import org.util.Utility;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

@Path("MeetingDetailsService")
public class MeetingDetails {

	/**
	 * @Author : Sunil Verma
	 * @Action : Meeting creation but if any meeting exist with same date and time then check conflict
	 * 
	 */
	@POST
	@Path("/insertMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertMeetingDetails(String meetingInsertion) {
		JSONObject finalJson = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		System.out.println("meetingInsertion==========" + meetingInsertion);
		try {

			JSONObject meetingInsertionObject = new JSONObject(meetingInsertion);
			MeetingBean meetingCreationBean = new MeetingBean(meetingInsertionObject);

			if (!meetingCreationBean.getMeetingIdList().isEmpty()) {

				ServiceUtility.deleteUserFromMeeting(meetingCreationBean.getMeetingIdList(),meetingCreationBean.getSenderUserId());
				NotificationService.sendNotification(meetingCreationBean.getMeetingIdList(),meetingCreationBean.getSenderUserId(), NotificationsEnum.MEETING_REJECTED.ordinal());

			} else {

				List<UserDTO> userDTOList = null;
				userDTOList = ServiceUtility.checkingMeetingConfliction(meetingCreationBean);
				if (!userDTOList.isEmpty()) {

					if (userDTOList.size() > 1) {
						for (UserDTO userDTO : userDTOList) {
							jsonArray.put(userDTO.getMeetingId());
						}
						finalJson.put("message", "Conflicting with multiple meetings");
					} else {
						UserDTO userDTO = userDTOList.get(0);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("from", userDTO.getMeetingFromTime().getHour() + ":"
								+ userDTO.getMeetingFromTime().getMinute());
						jsonObject.put("to",
								userDTO.getMeetingToTime().getHour() + ":" + userDTO.getMeetingToTime().getMinute());
						jsonObject.put("meetingId", userDTO.getMeetingId());
						finalJson.put("message", "Conflicting with meeting : " + userDTO.getDescription() + " on date "
								+ userDTO.getMeetingFromTime().toLocalDate());

						jsonArray.put(jsonObject);
					}
					finalJson.put("isInserted", false);
					finalJson.put("status", true);
					finalJson.put("meetingIdsJsonArray", jsonArray);

					System.out.println("finalJson=====" + finalJson.toString());

					return finalJson.toString();
				}
			}
			Connection connection = null;
			CallableStatement callableStatement = null;
			connection = DataSourceConnection.getDBConnection();
			Long meetingId = 0l;
			String insertStoreProc = "{call usp_InsertMeetingDetails(?,?,?,?,?,?,?,?,?,?,?,?)}";
			callableStatement = connection.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingCreationBean.getSenderUserId());
			callableStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
			callableStatement.setTimestamp(3, Timestamp.valueOf(meetingCreationBean.getSenderFromDateTime()));
			callableStatement.setTimestamp(4, Timestamp.valueOf(meetingCreationBean.getSenderToDateTime()));
			callableStatement.setTime(5, java.sql.Time.valueOf(meetingCreationBean.getDuration()));
			callableStatement.setString(6, meetingCreationBean.getMeetingTitle());
			if (meetingCreationBean.getIsLocationSelected()) {
				callableStatement.setString(7, meetingInsertionObject.getString("latitude"));
				callableStatement.setString(8, meetingInsertionObject.getString("longitude"));
				callableStatement.setString(9, meetingInsertionObject.getString("address"));
			} else {
				callableStatement.setString(7, null);
				callableStatement.setString(8, null);
				callableStatement.setString(9, null);
			}
			callableStatement.setString(10, MeetingStatus.ACTIVE.toString());
			callableStatement.registerOutParameter(11, Types.INTEGER);
			callableStatement.registerOutParameter(12, Types.BIGINT);

			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(11);
			meetingId = callableStatement.getLong(12);
			System.out.println(isError + "meetingId >>>>>>>>>>>>>>>>>> " + meetingId + "  value==" + value);

			if (isError == 0 && value != 0) {

				connection.setAutoCommit(false);
				PreparedStatement ps = null;
				String query = "INSERT into tbl_RecipientsDetails(MeetingID,UserID,Status,RecipientFromDateTime,RecipientToDateTime,Latitude,Longitude,DestinationType,GoogleAddress) values(?,?,?,?,?,?,?,?,?)";
				ps = connection.prepareStatement(query);

				for (Long friendId : meetingCreationBean.getFriendsIdList()) {

					ps.setLong(1, meetingId);
					ps.setLong(2, friendId);
					ps.setString(3, "0");
					ps.setTimestamp(4, null);
					ps.setTimestamp(5, null);
					ps.setString(6, null);
					ps.setString(7, null);
					ps.setLong(8, 0);
					ps.setString(9, null);

					ps.addBatch();

				}
				int[] insertedRow = ps.executeBatch();
				connection.commit();
				if (insertedRow.length != 0) {

					NotificationService.sendingMeetingCreationNotification(meetingCreationBean, meetingId);
					MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
					JSONObject userDetail = ServiceUtility.getUserDetailByUserId(meetingCreationBean.getSenderUserId());
					if (!meetingCreationBean.getEmailIdList().isEmpty()) {
						int insertedEmails = ServiceUtility.insertingMails(meetingCreationBean, meetingId);
						if (insertedEmails != 0) {
							sendingEmail(meetingCreationBean.getEmailIdList(), userDetail, meetingLogBean);
						}
					}
					if (!meetingCreationBean.getContactList().isEmpty()) {
						int insertedContact = ServiceUtility.insertContactNumbers(meetingCreationBean,
								meetingId);
						if (insertedContact != 0) {
							sendMeetingSms(meetingCreationBean.getContactList(), userDetail, meetingLogBean);
						}
					}
				}

				finalJson.put("status", true);
				finalJson.put("isInserted", true);
				finalJson.put("isLocationSelected", meetingInsertionObject.getBoolean("isLocationSelected"));
				finalJson.put("meetingId", meetingId);//
				finalJson.put("message", "meeting inserted successfully");

				return finalJson.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		finalJson.put("status", false);
		finalJson.put("isInserted", false);
		finalJson.put("message", "Oops something went wrong");

		return finalJson.toString();
	}
	

	/**
	 * @Author : Sunil Verma
	 * @Action : Getting meeting detail according to the user id
	 * 
	 */
	
	@POST
	@Path("/getMeetingDetailsByUserID")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getMeetingDetailsByUserID(String meetingDate) {

		JSONObject finalJson = new JSONObject();
		try {
			JSONObject meetingDateJsonObject = new JSONObject(meetingDate);
			System.out.println("meetingDateJsonObject  =============="+meetingDateJsonObject.toString());
			Map<String , Date> map = Utility.getOneDayDate(meetingDateJsonObject.getString("date"));
			
			finalJson.put("status", true);
			finalJson.put("message", "Success");
			finalJson.put("meetingArray", ServiceUtility.getMeetingArray(meetingDateJsonObject.getLong("userId"), map.get("today"), map.get("tomorrow")));
			return finalJson.toString();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	   }

	
	/**
	 * @Author : Sunil Verma
	 * @Action : Accept or reject meeting but if we accept then check the date & time if any conflict then 
	 *           inform to user if they persist to accept then remove previous meeting and accept new meeting   
	 * 
	 */
	@POST
	@Path("/getConflictedMeetingDetails")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getConflictedMeetingDetails(String meetingDetails) {
		
		System.out.println("==============="+meetingDetails.toString());
        JSONObject meetingDetailsJsonObject = new JSONObject(meetingDetails);
        Long meetingId = meetingDetailsJsonObject.getLong("meetingId");
		JSONArray jsonResultsArray = new JSONArray();
		JSONObject finalJson = new JSONObject();
		CallableStatement callableStatement = null;
		Connection conn = null;
		try {
			 if(meetingDetailsJsonObject.has("meetingIdsJsonArray")){
				 List<Long> meetingIdList = new ArrayList<Long>();
				  
				if (meetingDetailsJsonObject.has("meetingIdsJsonArray")) {
					if (meetingDetailsJsonObject.getJSONArray("meetingIdsJsonArray").length() == 1) {

						meetingIdList.add(meetingDetailsJsonObject.getJSONArray("meetingIdsJsonArray").getJSONObject(0).getLong("meetingId"));
					} else {
						for (int i = 0; i < meetingDetailsJsonObject.getJSONArray("meetingIdsJsonArray").length(); i++) {
							meetingIdList.add(meetingDetailsJsonObject.getJSONArray("meetingIdsJsonArray").getLong(i));
						}

					}
				}
					ServiceUtility.deleteUserFromMeeting(meetingIdList , meetingDetailsJsonObject.getLong("userId"));
					NotificationService.sendNotification(meetingIdList, meetingDetailsJsonObject.getLong("userId"), NotificationsEnum.MEETING_REJECTED.ordinal());
			
			 }else if(! meetingDetailsJsonObject.has("isRejected")){
				
			     Map<String , Date> map = Utility.getOneDayDate(meetingDetailsJsonObject.getString("meetingDate"));
			    conn = DataSourceConnection.getDBConnection();
				String selectStoreProcedue = "{call usp_CheckingConflicatedMeetings(?,?,?)}";
				//System.out.println("from =="+new Timestamp(map.get("today").getTime())+"==="+new Timestamp(map.get("today").getTime()));
				callableStatement = conn.prepareCall(selectStoreProcedue);
				callableStatement.setLong(1, meetingDetailsJsonObject.getLong("userId"));
				callableStatement.setTimestamp(2, new Timestamp(map.get("today").getTime()));
				callableStatement.setTimestamp(3, new Timestamp(map.get("tomorrow").getTime()));
				
				ResultSet rs = callableStatement.executeQuery();;
				List<UserDTO> meetingList = new ArrayList<UserDTO>();
				List<UserDTO> conflictedMeetingList = new ArrayList<UserDTO>();
				UserDTO meetingDto = new UserDTO();
				meetingDto = ServiceUtility.getMeetingDetailsById(meetingId);
				
				
			while (rs.next()) {
				
				   LocalDateTime fromTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				   LocalDateTime toTime =   ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
					UserDTO userDto = new UserDTO();
					userDto.setMeetingId(rs.getLong("MeetingID"));
					userDto.setUserId(rs.getLong("SenderUserID"));
					userDto.setMeetingFromTime(fromTime);
					userDto.setMeetingToTime(toTime);
					userDto.setStartTime(Float.parseFloat(fromTime.getHour() + "." + fromTime.getMinute()));
					userDto.setEndTime(Float.parseFloat(toTime.getHour() + "." + toTime.getMinute()));
					if(rs.getString("latitude") != null && rs.getString("longitude") != null && rs.getString("MeetingDescription") != null){
						
						userDto.setLatitude(rs.getString("latitude"));
						userDto.setLongitude(rs.getString("longitude"));
						userDto.setDescription(rs.getString("MeetingDescription"));
						userDto.setIsLocationSelected(true);
					}else{
						userDto.setIsLocationSelected(false);
					}
				
				meetingList.add(userDto);
			}
			// logic for avoiding the time collapse b/w meetings
			for (UserDTO userDTO : meetingList) {
				if ((userDTO.getStartTime() <= meetingDto.getStartTime() && meetingDto.getStartTime() < userDTO.getEndTime())|| (userDTO.getStartTime() <= meetingDto.getEndTime() && meetingDto.getEndTime() < userDTO.getEndTime())) {
					conflictedMeetingList.add(userDTO);
				}
			}
			if(! conflictedMeetingList.isEmpty()){
				
				if(conflictedMeetingList.size() > 1){
					for (UserDTO userDTO : conflictedMeetingList) {
						jsonResultsArray.put(userDTO.getMeetingId());
					}
					finalJson.put("message", "Conflicting with  meetings");
				}else{
					
					   UserDTO userDTO = conflictedMeetingList.get(0);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("from", userDTO.getMeetingFromTime().getHour() + ":"+ userDTO.getMeetingFromTime().getMinute());
						jsonObject.put("to",userDTO.getMeetingToTime().getHour() + ":" + userDTO.getMeetingToTime().getMinute());
						jsonObject.put("meetingId", userDTO.getMeetingId());
						jsonResultsArray.put(jsonObject);
					finalJson.put("message", "Conflicting with meeting : "+userDTO.getDescription()+" on date "+ userDTO.getMeetingFromTime().toLocalDate());
				}
				finalJson.put("isAccepted", false);
				finalJson.put("status", true);
			
				finalJson.put("meetingIdsJsonArray", jsonResultsArray);	
					return finalJson.toString();
			}
         }
			 MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
			 NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
			 
			 if(meetingDetailsJsonObject.has("isAccepted") && meetingDetailsJsonObject.getBoolean("isAccepted")){
				 if(meetingRequestUpdate(meetingId , meetingDetailsJsonObject.getLong("userId") , meetingDetailsJsonObject.getString("meetingStatus") , false) != 0 ){
					 
					 notificationInfoDTO.setSenderUserId(meetingDetailsJsonObject.getLong("userId"));
					 notificationInfoDTO.setMeetingId(meetingId);
					 notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_REQUEST_ACCEPTANCE.toString());
					 notificationInfoDTO.setMeetingLogBean(meetingLogBean);
					 
					 NotificationService.sendMeetingAcceptRejectNotification(notificationInfoDTO);
					 
					 finalJson.put("isAccepted", true);
					 finalJson.put("status", true);
					 finalJson.put("message", "Meeting accepted successfully");
					 return finalJson.toString();
					 
			 }
			 }else if(meetingDetailsJsonObject.has("isRejected") && meetingDetailsJsonObject.getBoolean("isRejected")){
				 Boolean isMeetingCreator = meetingLogBean.getSenderUserId() == meetingDetailsJsonObject.getLong("userId") ? true : false;
				 
	        	if(meetingRequestUpdate(meetingId , meetingDetailsJsonObject.getLong("userId") , meetingDetailsJsonObject.getString("meetingStatus") , isMeetingCreator) != 0 ){
	        		
					 notificationInfoDTO.setSenderUserId(meetingDetailsJsonObject.getLong("userId"));
					 notificationInfoDTO.setMeetingId(meetingId);
					 notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_REJECTED.toString());
					 notificationInfoDTO.setMeetingLogBean(meetingLogBean);
					 NotificationService.sendMeetingAcceptRejectNotification(notificationInfoDTO);
	        		
	        		finalJson.put("message", "Meeting rejected successfully");
					finalJson.put("isRejected", true);
					finalJson.put("status", true);
					return finalJson.toString();
				}
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oopse something went wrong");
		return finalJson.toString();
	}
	
	public int meetingRequestUpdate(Long meetingId , Long userId , String status , Boolean isMeetingCreator){
		Connection conn = null;
		CallableStatement callableStatement = null;
		int updatedRow = 0;
		try{
			conn = DataSourceConnection.getDBConnection();
		  	String updateMeetingSqlProc = "{call usp_UpdateMeetingAddress(?,?,?,?,?,?)}";
		  	callableStatement = conn.prepareCall(updateMeetingSqlProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.setLong(2, userId);
			callableStatement.setBoolean(3, isMeetingCreator);
			callableStatement.setString(4, status);
			callableStatement.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
		  	
			updatedRow =	callableStatement.executeUpdate();
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeCallableSatetment(callableStatement);
			ServiceUtility.closeConnection(conn);
		}
		System.out.println("MeetingRequestUpdate============================="+updatedRow);
		return updatedRow;
	}

	
	/**
	 * @Author : Sunil Verma
	 * @Action : Get all pending meeting by userId  who is in active state
	 * 
	 */
	@GET
	@Path("/getPendingMeetingRequests/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPendingMeetingRequests(@PathParam("userId") Long userId) {
        
		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject finalJson = new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetPendingMeetingRequests(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, userId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while (rs.next()) {
				
				MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId( rs.getLong("MeetingID"));
				
				if(meetingLogBean.getMeetingId() != null){
					
				JSONObject jsonObject = new JSONObject();
				LocalDateTime senderFromDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("meetingId" , rs.getLong("MeetingID"));
				jsonObject.put("meetingSenderId" , rs.getLong("SenderUserID"));
				jsonObject.put("date" , LocalDateTime.ofInstant(formatter.parse(rs.getString("SenderFromDateTime")).toInstant(), ZoneId.systemDefault()).toLocalDate());
				jsonObject.put("from" , senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to" , senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description" , rs.getString("MeetingDescription"));
				//jsonObject.put("meetingStatus" , rs.getLong("Status")); 
				
				if(rs.getString("Latitude") != null && ! rs.getString("Latitude").trim().isEmpty()){
					
					jsonObject.put("isLocationSelected",true);
					jsonObject.put("address" , rs.getString("GoogleAddress")+"");
					jsonObject.put("latitude" , rs.getString("Latitude"));
					jsonObject.put("longitude" , rs.getString("Longitude"));
				}else{
					jsonObject.put("isLocationSelected",false);
				}
				
				jsonResultsArray.put(jsonObject);
			 }
			}
			ServiceUtility.closeCallableSatetment(callableStatement);
			  JSONArray meetingArray = new JSONArray();
			 for (int i = 0; i < jsonResultsArray.length(); i++) {
				   JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
				   jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(jsonObject.getLong("meetingId")));
				   jsonObject.put("contactsJsonArray",  ServiceUtility.getContactByMeetingId(jsonObject.getLong("meetingId")));
				   jsonObject.put("friendsJsonArray", ServiceUtility.getReceptionistByMeetingId(jsonObject.getLong("meetingId") , userId).get("friendsArray"));
				   jsonObject.put("status", ServiceUtility.getMeetingStatusByUserId(jsonObject.getLong("meetingId") , userId));
				   
				   meetingArray.put(jsonObject);
			}
			finalJson.put("status", true);
			finalJson.put("message", "Pending Meeting Requests list fetched successfully");
			finalJson.put("meeting_array", meetingArray);
			return finalJson.toString();

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oops something went wrong");
		return finalJson.toString();
	}
	/**
	 * @Author : Sunil Verma
	 * @Action : Get meeting details by meetingId
	 * 
	 */
	@GET
	@Path("/getMeetingDetailsByMeetingID/{meetingId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingDetailsByMeetingID(@PathParam("meetingId") Long  meetingId) {

		Connection conn = null;
		CallableStatement callableStatement = null;
		JSONObject jsonObject = new JSONObject();
		try {
			conn = DataSourceConnection.getDBConnection();
			String insertStoreProc = "{call usp_GetMeetingDetails_ByMeetingID(?)}";
			callableStatement = conn.prepareCall(insertStoreProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.execute();
			ResultSet rs = callableStatement.getResultSet();

			while (rs.next()) {
				LocalDateTime senderFromDateTime = ServiceUtility
						.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
				LocalDateTime senderToDateTime = ServiceUtility
						.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
				jsonObject.put("description", rs.getString("MeetingDescription"));
				jsonObject.put("latitude", rs.getString("Latitude"));
				jsonObject.put("longitude", rs.getString("Longitude"));
				jsonObject.put("address", rs.getString("GoogleAddress"));
				jsonObject.put("date",ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime")).toLocalDate());
				jsonObject.put("from", senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
				jsonObject.put("to", senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
				jsonObject.put("description", rs.getString("MeetingDescription"));
			}
			jsonObject.put("status", true);
			jsonObject.put("message", "Meeting details fetched successfully");
			return jsonObject.toString();

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Something went wrong");
		return jsonObject.toString();
	}
	
	/**
	 * @Author : Sunil Verma
	 * @Action : Get meeting details by meetingId
	 * 
	 */
	@GET
	@Path("/getUserDetailsByMeetingID/{meetingId}/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserDetailsByMeetingID(@PathParam("meetingId") Long meetingId, @PathParam("userId") Long userId) {

		JSONObject jsonObject = new JSONObject();
		try {
			MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
			if(meetingLogBean.getMeetingId() != null){
				
				JSONObject meetingJson = ServiceUtility.checkMeetingAddressUpdateByMeetingId(meetingId);
				LocalDateTime meetingDateTime = ServiceUtility.convertStringToLocalDateTime(meetingJson.getString("senderFromDateTime"));
				LocalDateTime currentDateTime = LocalDateTime.now().plusHours(2);
				
				if(currentDateTime.isAfter(meetingDateTime)){
					 jsonObject.put("updateCount", meetingJson.getInt("updateCount"));	
				}
				
				 jsonObject.put("meetingId" , meetingLogBean.getMeetingId());
				 jsonObject.put("meetingSenderId" , meetingLogBean.getSenderUserId());
				 jsonObject.put("date" , meetingLogBean.getDate());
				 jsonObject.put("from" , meetingLogBean.getFromDate().getHour()+":"+meetingLogBean.getFromDate().getMinute());
				 jsonObject.put("to" , meetingLogBean.getToDate().getHour()+":"+meetingLogBean.getToDate().getMinute());
				 jsonObject.put("description" , meetingLogBean.getDescription());
				 if(meetingLogBean.getSenderUserId() != null && meetingLogBean.getSenderUserId() == userId){
					 jsonObject.put("meetingStatus" , 1); 
				 }
				if(meetingLogBean.getAddress() != null && !meetingLogBean.getAddress().trim().isEmpty()){
					
					jsonObject.put("isLocationSelected",true);
					jsonObject.put("address" , meetingLogBean.getAddress());
					jsonObject.put("latitude" , meetingLogBean.getLatitude());
					jsonObject.put("longitude" , meetingLogBean.getLongitude());
				}else{
					jsonObject.put("isLocationSelected",false);
				}
			 }else{
				jsonObject.put("status", true);
				jsonObject.put("message", "Meeting suspended"); 
				jsonObject.put("isMeetingExisted", false); 
				
				return jsonObject.toString();
			 }
			jsonObject.put("isMeetingExisted", true); 
			
			 JSONObject friendsObject = ServiceUtility.getReceiverDetailsByMeetingId(meetingId , userId);
			 JSONArray friendsArray = friendsObject.getJSONArray("friendsArray");
			 if(friendsArray.length() == 0 && meetingLogBean != null){
				 JSONObject jsonObject2 = new JSONObject();
				 jsonObject2.put("userId", meetingLogBean.getSenderUserId());
				 jsonObject2.put("fullName", meetingLogBean.getFullName());
				 jsonObject2.put("status", "1");
				 
				 friendsArray.put(jsonObject2);
			 }
			 if(! jsonObject.has("meetingStatus")){
				 jsonObject.put("meetingStatus" , friendsObject.get("status"));
			 }
			 jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(meetingId));
			 jsonObject.put("contactsJsonArray", ServiceUtility.getContactByMeetingId(meetingId));
			 jsonObject.put("friendsJsonArray", friendsArray);
		
			 
			 jsonObject.put("status", true);
			jsonObject.put("message", "Meeting Requests list fetched successfully");
		   return jsonObject.toString();	
		}catch (Exception e) {
			e.printStackTrace();
		}
		jsonObject.put("status", false);
		jsonObject.put("message", "Oops something went wrong");
		return jsonObject.toString();
	}
	
	
	@POST
	@Path("/storeMeetingLatLngByUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getMeetingSummary(String meetingDetails){
		
		System.out.println("meetingDetails============"+meetingDetails);
		
		JSONObject finalJson = new JSONObject();
		JSONObject meetingUserDetail = new JSONObject(meetingDetails);
			
		Long userId = meetingUserDetail.getLong("userId");
		Long meetingId = meetingUserDetail.getLong("meetingId");
		 Connection conn = null;
		 CallableStatement callableStatement = null;
		 int updatedRow = 0;
	     MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
	     try{
		  	conn = DataSourceConnection.getDBConnection();
		  	Boolean isMeetingCreator = (meetingLogBean.getMeetingId() != null && meetingLogBean.getSenderUserId().equals(userId)) ? true : false ;
		  	System.out.println("isMeetingCreator  :  "+isMeetingCreator);
		  	
		  	String updateMeetingSqlProc = "{call usp_UpdateMeetingAddress(?,?,?,?,?,?)}";
		  	callableStatement = conn.prepareCall(updateMeetingSqlProc);
			callableStatement.setLong(1, meetingId);
			callableStatement.setLong(2, userId);
			callableStatement.setString(3, meetingUserDetail.getString("latitude"));
			callableStatement.setString(4, meetingUserDetail.getString("longitude"));
			callableStatement.setBoolean(5, isMeetingCreator);
			callableStatement.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
		  	
			updatedRow =	callableStatement.executeUpdate();
			
			System.out.println("updatedRow == "+updatedRow);
			if(updatedRow == 0){
				finalJson.put("status", false);
				finalJson.put("message", "Something went wrong");
				return finalJson.toString();
			}
	     } catch (Exception e) {
			e.printStackTrace();
		  }finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		 }
	    try{
	     String countAddrStoreProc  = "{call usp_countMeetingAddress}"; 
	     conn = DataSourceConnection.getDBConnection();
         callableStatement = conn.prepareCall(countAddrStoreProc);
         callableStatement.setLong(1, meetingId);
	     
         ResultSet rs = callableStatement.executeQuery();
        
		 
		 List<UserDTO> userAddressList = new ArrayList<UserDTO>();
		 UserDTO userDTO = null;
		 while(rs.next()){
			 
			 if(Utility.checkValidString(rs.getString("Latitude"))){
				 
				 userDTO = new UserDTO();
				 userDTO.setMeetingId(rs.getLong("MeetingID"));
				 userDTO.setLatitude(rs.getString("Latitude"));
				 userDTO.setLongitude(rs.getString("Longitude"));
				 
				 userAddressList.add(userDTO) ;
			 }
		 }
		 Set<Long> userIdSet = new HashSet<>();
		// LocalDateTime localDateTime = LocalDateTime.now().plusHours(2);
		// System.out.println(meetingLogBean.getFromDate().plusMinutes(2)+"================="+meetingLogBean.getFromDate().plusMinutes(2).isBefore(localDateTime)+"   localDateTime=    "+localDateTime);
		 //&& meetingLogBean.getFromDate().plusMinutes(2).isBefore(localDateTime)
		 System.out.println(" testing updated adress "+(userAddressList.size() >= 2 ));
		 
		 if(userAddressList.size() >= 2 ){
			 
		  JSONObject jsonObject	 = Geomagic.calculateMidPointLatLng(userAddressList);
		  JSONArray jsonArray = GoogleSearchPlaces.getGoogleSearchPlaces(jsonObject.getDouble("lat") , jsonObject.getDouble("lng") , meetingLogBean.getDescription());
		  Boolean isLocationsaved = GoogleSearchPlaces.storeFrissbiLocationsTemporary(jsonArray , meetingId);
		  if(isLocationsaved){
			  JSONArray frissbiLocationArray =  GoogleSearchPlaces.getFrissbiLocation(meetingId, 0); 
			  
			  System.out.println("frissbiLocationArray==================="+frissbiLocationArray.length());
			  JSONArray friendsIdArray  = ServiceUtility.getReceptionistDetailsByMeetingId(meetingId);
			  
			  if(friendsIdArray.length() != 0){
				  
				 for (int i = 0; i < friendsIdArray.length(); i++) {
					 if(friendsIdArray.getJSONObject(i).getInt("status") == 0){
						 userIdSet.add(friendsIdArray.getJSONObject(i).getLong("userId"));
					 }
				 }
				 userIdSet.add(meetingLogBean.getSenderUserId());
				 
				 String message = "Please share you location for the meeting "+meetingLogBean.getDescription()+" on "+meetingLogBean.getDate()+" from "+meetingLogBean.getStartTime()+" to "+meetingLogBean.getEndTime();
				 
				 NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				 notificationInfoDTO.setUserList(userIdSet.stream().collect(Collectors.toList()));
				 notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_LOCATION_SUGGESTION.toString());
				 notificationInfoDTO.setMeetingId(meetingId);
				 notificationInfoDTO.setMessage(message);
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("frissbiLocationArray", frissbiLocationArray);
				 if(frissbiLocationArray.length() <= 5){
					 jsonObject2.put("isNextLocationExist", false);
					}else{
						jsonObject2.put("isNextLocationExist", true);
					}
				 jsonObject2.put("isLocationUpdate", false);
				 notificationInfoDTO.setJsonObject(jsonObject2);
				 
				 NotificationService.sendMeetingNotification(notificationInfoDTO);
			  }
		  }
		  
		 }
			 finalJson.put("status", true);
			 finalJson.put("message", "Address updated successfully");
			 return finalJson.toString(); 
		 
	     } catch(Exception e){
	    	 e.printStackTrace();
	     }finally{
	    	 ServiceUtility.closeConnection(conn);
	    	 ServiceUtility.closeCallableSatetment(callableStatement);
	     }
		return finalJson.toString();
	}

	@POST
	@Path("/updateMeetingAddress")
	@Consumes(MediaType.APPLICATION_JSON)
	public String updateMeetingAddress(String addressDetails){
		
		JSONObject addressJsonObject = new JSONObject(addressDetails);
        System.out.println("addressJsonObject==========================="+addressJsonObject.toString());
        
		JSONObject finalJson = new JSONObject();
		
		Connection connection = null;
		CallableStatement callableStatement = null;
		try{
			Long meetingId = addressJsonObject.getLong("meetingId");
			JSONObject jsonObject = ServiceUtility.checkMeetingAddressUpdateByMeetingId(meetingId);
			Integer updateCount = jsonObject.getInt("updateCount");
			
			 if(! addressJsonObject.getBoolean("isFromMeetingSummary")){
				 if(updateCount != 0){
					finalJson.put("status", true);	
					finalJson.put("isLocationUpdate", true);	
					finalJson.put("meetingId", meetingId);	
					finalJson.put("message", "Location is already decided");	
					return finalJson.toString(); 
				 }
			 }else{
				if(updateCount == 2){
					finalJson.put("meetingId", meetingId);	
					finalJson.put("isLocationUpdate", true);	
					finalJson.put("meetingId", meetingId);	
					finalJson.put("message", "Location is already decided");	
					return finalJson.toString();
				}
			 }
			System.out.println("updateCount==================="+updateCount);
				
				connection = DataSourceConnection.getDBConnection();
				String insertStoreProc = "{call usp_UpdateMeetingAddress(?,?,?,?,?,?)}";
				callableStatement = connection.prepareCall(insertStoreProc);
				callableStatement.setLong(1, meetingId);
				callableStatement.setString(2, addressJsonObject.getString("latitude"));
				callableStatement.setString(3,  addressJsonObject.getString("longitude"));
				callableStatement.setString(4, addressJsonObject.getString("address"));
				callableStatement.setLong(5, (updateCount+1));
				callableStatement.registerOutParameter(6, Types.INTEGER);
				
				int value = callableStatement.executeUpdate();
				int isError = callableStatement.getInt(6);
				
				if(value != 0){
					
					
					MeetingLogBean meetingLogBean = ServiceUtility.getMeetingDetailsByMeetingId(meetingId);
					JSONArray usersArray = ServiceUtility.getReceptionistDetailsByMeetingId( meetingId);
					
					Set<Long> usersSet = new HashSet<Long>();
					for (int i = 0; i < usersArray.length(); i++) {
						usersSet.add(usersArray.getJSONObject(i).getLong("userId"));
					}
					System.out.println("usersSet==============="+usersSet.size());
					if(! usersSet.isEmpty()){
						
						if(meetingLogBean.getSenderUserId() != null){
							usersSet.add(meetingLogBean.getSenderUserId());
						}
						
						jsonObject.put("isLocationUpdate", true);
						jsonObject.put("address" , meetingLogBean.getAddress());
						jsonObject.put("latitude" , meetingLogBean.getLatitude());
						jsonObject.put("longitude" , meetingLogBean.getLongitude());
						String message = "For Meeting "+meetingLogBean.getDescription() +" location is confirmed which is on "+meetingLogBean.getDate()+" from "+meetingLogBean.getStartTime()+" to "+meetingLogBean.getEndTime();
						
						NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
						notificationInfoDTO.setUserList(usersSet.stream().collect(Collectors.toList()));
						notificationInfoDTO.setMeetingId(meetingLogBean.getMeetingId());
						notificationInfoDTO.setNotificationType(NotificationsEnum.MEETING_LOCATION_SUGGESTION.toString());
						notificationInfoDTO.setMessage(message);
						notificationInfoDTO.setJsonObject(jsonObject);
						
						NotificationService.sendMeetingNotification(notificationInfoDTO);
					}
					finalJson.put("status", true);	
					finalJson.put("meetingId", meetingId);	
					finalJson.put("message", "Meeting address updated and selected");	
					return finalJson.toString();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(connection);
			ServiceUtility.closeCallableSatetment(callableStatement);
		}
		finalJson.put("status", false);	
		finalJson.put("message", "Something went wrong ");	
		return finalJson.toString();
	}
	
	
	
    @GET	
	@Path("/getFrissbiLocations/{meetingId}/{offSetValue}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMoreFrissbiLocations(@PathParam("meetingId") Long meetingId , @PathParam("offSetValue") int offSetValue){
		
		JSONObject finalJson = new JSONObject();
		try{
			JSONArray  frissbiLocations = GoogleSearchPlaces.getFrissbiLocation(meetingId , offSetValue);
			
			if(frissbiLocations.length() <= 5){
				finalJson.put("isNextLocationExist", false);
			}else{
				finalJson.put("isNextLocationExist", true);
			}
			finalJson.put("frissbiLocationArray", frissbiLocations);
			finalJson.put("status", true);	
			finalJson.put("message", "Frissbi location fetched successfully");	
		 return finalJson.toString();
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		finalJson.put("status", false);	
		finalJson.put("message", "Something went wrong ");	
		return finalJson.toString();
	}
	
	public void sendMeetingSms(List<String> contactList, JSONObject userDetail, MeetingLogBean meetingLogBean) {

		try {
		String contactNumbers = contactList.stream().collect(Collectors.joining(","));

		String message = "";
		message = " You have a meeting " + meetingLogBean.getDescription() + " on " + meetingLogBean.getDate()
				+ " from " + meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime() + " created by "
				+ userDetail.getString("fullName") + " Please click this url " + "https://alerts.solutionsinfini.com "
				+ " to install Frissbi App";

			SmsService smsService = new SmsService();
			smsService.sendSms(contactNumbers, message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendingEmail(List<String> emailIdList, JSONObject userDetail, MeetingLogBean meetingLogBean) {

		try {
		String emailTo = emailIdList.stream().collect(Collectors.joining(", "));

		String htmlMessage = "";
		htmlMessage = " You have a meeting " + meetingLogBean.getDescription() + " on " + meetingLogBean.getDate()
				+ " from " + meetingLogBean.getStartTime() + " to " + meetingLogBean.getEndTime() + " created by "
				+ userDetail.getString("fullName") + " Please click this url " + "https://alerts.solutionsinfini.com "
				+ " to install Frissbi App";


			EmailService.SendMail(emailTo, "Frissbi Meeting", htmlMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*//logic for checking the source and destination  distance wrt Time
	if(! timePostedFriendList.isEmpty()){
		
		Float startSubTime = 0f;
		Float endSubTime = 0f;
		 
		java.util.Map<String , UserDTO> map = new HashMap<String , UserDTO>();
		
		for (UserDTO userDTO : timePostedFriendList) {
			
			if(meetingStartTime > userDTO.getEndTime()){
				if(startSubTime == 0f){
					startSubTime = userDTO.getEndTime();
					map.put("startDto", userDTO)	;
				}else if(startSubTime < userDTO.getEndTime()){
					startSubTime = userDTO.getEndTime();
					map.put("startDto", userDTO)	;
				}
				
			}
			
			if(meetingEndTime < userDTO.getStartTime()){
				
				if(endSubTime == 0f){
					endSubTime = userDTO.getStartTime();
					map.put("endDto", userDTO)	;
				}else if(endSubTime < userDTO.getStartTime()){
					endSubTime = userDTO.getStartTime();
					map.put("endDto", userDTO)	;
				}
			}
		}
		
		//logic for the checking the checking the source and destination  distance wrt Time
		if(! map.isEmpty()){
			if(map.get("startDto") != null){
			int startTimeToBeTaken = ServiceUtility.calculateTimeBetweenLatLng(Float.parseFloat(map.get("startDto").getLatitude()), Float.parseFloat(map.get("startDto").getLatitude()), Float.parseFloat(meetingInsertionObject.getString("latitude")), Float.parseFloat(meetingInsertionObject.getString("longitude")));
			
			if(startTimeToBeTaken != 0 && startTimeToBeTaken > map.get("startDto").getMeetingFromTime().getHour()+map.get("startDto").getMeetingFromTime().getMinute()){
			
				finalJson.put("status", true);
				finalJson.put("message", "Meeting from "+ senderFromDateTime.getHour()+":"+senderToDateTime.getMinute()+" to "+senderToDateTime.getHour()+":"+senderToDateTime.getMinute() +" will take more time "+map.get("startDto").getMeetingFromTime().getHour()+":"+map.get("startDto").getMeetingFromTime().getMinute()+" to "+map.get("startDto").getMeetingFromTime().getHour()+":"+map.get("startDto").getMeetingFromTime().getMinute()+" on Date "+ senderFromDateTime.toLocalDate());
				
				return finalJson.toString();	
				
			}
			
			}
			if(map.get("endDto") != null){
				
				int endTimeToBeTaken = ServiceUtility.calculateTimeBetweenLatLng(Float.parseFloat(map.get("endDto").getLatitude()), Float.parseFloat(map.get("endDto").getLatitude()), Float.parseFloat(meetingInsertionObject.getString("latitude")), Float.parseFloat(meetingInsertionObject.getString("longitude")));
				
				if(endTimeToBeTaken != 0 && endTimeToBeTaken > map.get("endDto").getMeetingToTime().getHour()+map.get("endDto").getMeetingToTime().getMinute()){
					
					finalJson.put("status", true);
					finalJson.put("message", "Meeting from "+ senderFromDateTime.getHour()+":"+senderToDateTime.getMinute()+" to "+senderToDateTime.getHour()+":"+senderToDateTime.getMinute() +" will take more time "+map.get("endDto").getMeetingFromTime().getHour()+":"+map.get("endDto").getMeetingFromTime().getMinute()+" to "+map.get("endDto").getMeetingFromTime().getHour()+":"+map.get("endDto").getMeetingFromTime().getMinute()+" on Date "+ senderFromDateTime.toLocalDate());
					
					return finalJson.toString();	
					
				}
			}
		}
		
	}*/
	
}
