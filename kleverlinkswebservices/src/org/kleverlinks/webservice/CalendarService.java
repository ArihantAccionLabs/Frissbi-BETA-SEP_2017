package org.kleverlinks.webservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.util.service.ServiceUtility;

@Path("CalendarService")
public class CalendarService {
	
	@POST
	@Path("/getMeetingMonthWise")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String getMeetingMonthWise(String meetingDetails){
		
		System.out.println("meetingDetails================"+meetingDetails.toString());
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		JSONObject finalJson = new JSONObject();
		JSONArray jsonResultsArray = new JSONArray();
		List<LocalDate> localDateList = new ArrayList<>();
		try{
		    /* LocalDateTime localDateTime = LocalDateTime.now();
		    System.out.println(localDateTime.getYear()+"   "+(localDateTime.getMonth().getValue() == monthFirstDate.get(Calendar.MONTH)+1)+"   MONTH      "+(monthFirstDate.get(Calendar.YEAR)));
		    if(localDateTime.getYear() == monthFirstDate.get(Calendar.YEAR)){
		    }*/
		  /*   if((localDateTime.getMonth().getValue() == monthFirstDate.get(Calendar.MONTH)+1)){
		    	monthFirstDate = Calendar.getInstance();
		      } else {
		      }*/
			JSONObject meetingInfoJson = new JSONObject(meetingDetails);
			java.util.Date date = new SimpleDateFormat("yyyy-MM").parse(meetingInfoJson.getString("date"));
			
			Calendar monthFirstDate = Calendar.getInstance();  
			monthFirstDate.setTime(date); 
		    monthFirstDate.set(Calendar.DAY_OF_MONTH, 1);
			monthFirstDate.set(Calendar.HOUR, 0);
			monthFirstDate.set(Calendar.MINUTE, 0);
			monthFirstDate.set(Calendar.SECOND, 0);
			monthFirstDate.set(Calendar.HOUR_OF_DAY, 0);
			
			Calendar monthLastDate = Calendar.getInstance();
			monthLastDate.setTime(date);
			monthLastDate.add(Calendar.MONTH, 1);
			monthLastDate.set(Calendar.DAY_OF_MONTH, 1);
			monthLastDate.add(Calendar.DATE, -1);
			monthLastDate.set(Calendar.HOUR, 23);
			monthLastDate.set(Calendar.MINUTE, 59);
			monthLastDate.set(Calendar.SECOND, 00);
	
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm ");  
	        System.out.println("Today            : " + sdf.format(monthFirstDate.getTime()));  
	        System.out.println("Last Day of Month: " + sdf.format(monthLastDate.getTime()));
				conn = DataSourceConnection.getDBConnection();

				 String sql = "SELECT DISTINCT(M.MeetingID),M.SenderFromDateTime,M.SenderToDateTime,M.MeetingDescription,M.Latitude,M.Longitude,M.GoogleAddress,M.MeetingStatus "
				 		        + "FROM FrissDB.tbl_RecipientsDetails AS R "
								+ "INNER JOIN FrissDB.tbl_MeetingDetails AS M	"
								+ "ON R.MeetingID = M.MeetingID "
								+ "WHERE (M.SenderUserID = ? OR  (R.UserID=? AND R.Status = 1)) "
								+ "AND M.SenderFromDateTime BETWEEN ? AND ? ORDER BY M.SenderFromDateTime";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setLong(1, meetingInfoJson.getLong("userId"));
				pstmt.setLong(2, meetingInfoJson.getLong("userId"));
				pstmt.setTimestamp(3, new Timestamp(monthFirstDate.getTime().getTime()));
				pstmt.setTimestamp(4, new Timestamp(monthLastDate.getTime().getTime()));
				
				ResultSet rs = pstmt.executeQuery();
				DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("yyyy-M");
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
				while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					LocalDateTime senderFromDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderFromDateTime"));
					LocalDateTime senderToDateTime = ServiceUtility.convertStringToLocalDateTime(rs.getString("SenderToDateTime"));
					localDateList.add(senderFromDateTime.toLocalDate());
					jsonObject.put("meetingId" , rs.getLong("MeetingID"));
					jsonObject.put("month" , monthFormat.format(senderFromDateTime));
					jsonObject.put("date" , LocalDateTime.ofInstant(formatter.parse(rs.getString("SenderFromDateTime")).toInstant(), ZoneId.systemDefault()).toLocalDate());
					jsonObject.put("from" , senderFromDateTime.getHour() + ":" + senderFromDateTime.getMinute());
					jsonObject.put("to" , senderToDateTime.getHour() + ":" + senderToDateTime.getMinute());
					jsonObject.put("description" , rs.getString("MeetingDescription"));
					
					if(rs.getString("GoogleAddress") != null && ! rs.getString("GoogleAddress").trim().isEmpty()){
						
						jsonObject.put("isLocationSelected",true);
						jsonObject.put("address" , rs.getString("GoogleAddress"));
						jsonObject.put("latitude" , rs.getString("Latitude"));
						jsonObject.put("longitude" , rs.getString("Longitude"));
					}else{
						jsonObject.put("isLocationSelected",false);
					}
					   jsonObject.put("emailIdJsonArray", ServiceUtility.getEmailIdByMeetingId(jsonObject.getLong("meetingId")));
					   jsonObject.put("contactsJsonArray",  ServiceUtility.getContactByMeetingId(jsonObject.getLong("meetingId")));
					   jsonObject.put("friendsJsonArray", ServiceUtility.getReceptionistByMeetingId(jsonObject.getLong("meetingId") , meetingInfoJson.getLong("userId")).get("friendsArray"));
					   jsonObject.put("meetingStatus", rs.getString("MeetingStatus"));
					   
					jsonResultsArray.put(jsonObject);
				}
				finalJson.put("status", true);
				finalJson.put("message", "Success");
				finalJson.put("meetingArray", jsonResultsArray);
				finalJson.put("dateCountArray", dateCountMeetingArray(localDateList));
				return finalJson.toString();
		
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(pstmt);
		}
		finalJson.put("status", false);
		finalJson.put("message", "Oopse something went wrong");
        return finalJson.toString();
	}

	public JSONArray dateCountMeetingArray(List<LocalDate> localDateList){
		
		 Map<LocalDate, Integer> dupMap = new HashMap<LocalDate, Integer>(); 
		 JSONArray dateCountJsonArray = new JSONArray();
		
		 for (LocalDate localDate : localDateList) {
			 if(dupMap.containsKey(localDate)){
	                dupMap.put(localDate, dupMap.get(localDate)+1);
	            } else {
	                dupMap.put(localDate, 1);
	            } 
		 }
		 for (LocalDate localDate :  dupMap.keySet()) {
			
			 JSONObject   jsonObject = new JSONObject();
				jsonObject.put("date", localDate);
				jsonObject.put("count", dupMap.get(localDate));
				dateCountJsonArray.put(jsonObject); 
		 }
			//System.out.println("" + dateCountJsonArray.toString());
			
			return dateCountJsonArray;
	}
}



