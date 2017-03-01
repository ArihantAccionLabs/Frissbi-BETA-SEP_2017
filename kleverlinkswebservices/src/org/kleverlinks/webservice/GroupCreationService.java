package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.GroupBean;
import org.kleverlinks.bean.GroupInfoBean;
import org.mongo.dao.MongoDBJDBC;
import org.service.dto.NotificationInfoDTO;
import org.util.Utility;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

import com.mongodb.DBObject;

@Path("GroupCreationService")
public class GroupCreationService {
	
	@POST
    @Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public String groupCreation(String group){
		
		Connection conn = null;
		CallableStatement callableStatement = null;
		PreparedStatement preparedStatement = null;
		Long groupId = null;
		JSONObject responseJson = new JSONObject();
		
		try {
			JSONObject jsonObject = new JSONObject(group);
			GroupBean groupBean = new GroupBean(jsonObject);
			System.out.println("jsonObject  : "+jsonObject.toString());
			conn = DataSourceConnection.getDBConnection();
			String insertGroupStorPro = "{call usp_insertGroup(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertGroupStorPro);
			callableStatement.setLong(1, groupBean.getUserId());
			callableStatement.setString(2, groupBean.getGroupName());
			callableStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
			callableStatement.registerOutParameter(4, Types.INTEGER);
			callableStatement.registerOutParameter(5, Types.BIGINT);
			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(4);
			groupId = callableStatement.getLong(5);
			System.out.println("value "+value+" isError "+isError+" groupId "+groupId);
			if(value != 0 && groupId != null && groupId != 0l){
				groupBean.setGroupId(groupId);
			Boolean friendInserted =	addGroupMember(groupBean);
			
			if(friendInserted){
				JSONObject userObject = ServiceUtility.getUserDetailByUserId(groupBean.getUserId());
				String message = groupBean.getGroupName()+" is created by "+userObject.getString("fullName")+" on "+new SimpleDateFormat("yyyy-mm-dd hh:mm a").format(new Date());
				
			   NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
			   notificationInfoDTO.setSenderUserId(groupBean.getUserId());
			   notificationInfoDTO.setMessage(message);
			   notificationInfoDTO.setUserList(groupBean.getFriendList());
			   notificationInfoDTO.setNotificationType(NotificationsEnum.Group_CREATION.toString());
			   
			   NotificationService.sendGroupCreationNotification(notificationInfoDTO);
			   
				responseJson.put("status", true);
				responseJson.put("message", jsonObject.getString("groupName")+" group created successfully");
				responseJson.put("groupCreated", true);
				
				return responseJson.toString();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
			ServiceUtility.closeSatetment(preparedStatement);
		}
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");
		responseJson.put("groupCreated", false);
		
		return responseJson.toString();
	}
	
	@POST
    @Path("/leftGroupByMember")
	@Consumes(MediaType.APPLICATION_JSON)
	public String removeGroupMember(String deleteData){
	  JSONObject responseJson = new JSONObject();
      JSONObject jsonObject = new JSONObject(deleteData);
      Connection conn = null;
		PreparedStatement preparedStatement = null;	
		try{
			GroupBean groupBean = new GroupBean(jsonObject);
			
				
				String sql = "UPDATE tbl_FriendGroup SET IsActive=? WHERE  GroupID=? AND UserID=?";	
				conn = DataSourceConnection.getDBConnection();
				preparedStatement = conn.prepareStatement(sql);
				
				preparedStatement.setInt(1, 1);
				preparedStatement.setLong(2, groupBean.getGroupId());
				preparedStatement.setLong(3, groupBean.getUserId());
				
				if(preparedStatement.executeUpdate() != 0){
					responseJson.put("status", true);
					responseJson.put("message","Member is removed from group "+groupBean.getGroupName());
				}else{
					responseJson.put("status", false);
					responseJson.put("message","Oops something went wrong");
				}
			return responseJson.toString();
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(preparedStatement);
	}
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");
      return responseJson.toString();
	}
	
	
	
	@POST
    @Path("/addMember")
	@Consumes(MediaType.APPLICATION_JSON)
	public String updateGroupMember(String deleteData){
		JSONObject responseJson = new JSONObject();
		JSONObject jsonObject = new JSONObject(deleteData);
		GroupBean groupBean = getGroupAdmin(jsonObject.getLong("userId"));
		if(groupBean != null){
			groupBean.setFriendId(jsonObject.getLong("friendId"));
			
			addGroupMember(groupBean);
			if(addGroupMember(groupBean)){
				responseJson.put("status", true);
				responseJson.put("message","New memeber is added in group "+groupBean.getGroupName());
			}else{
				responseJson.put("status", false);
				responseJson.put("message","Something went wrong");
			}
		}else{
			responseJson.put("status", false);
			responseJson.put("message","You do'not have access to add member in this group ");
		}
		return responseJson.toString();
	}

	
	@POST
    @Path("/updateGroupAdmin")
	@Consumes(MediaType.APPLICATION_JSON)
	public String removeGroup(String groupData){
		
		JSONObject responseJson = new JSONObject();
		JSONObject jsonObject = new JSONObject(groupData);
		GroupBean groupBean = new GroupBean(jsonObject);
		Connection conn = null;
		CallableStatement callableStatement = null;
		try{
			conn = DataSourceConnection.getDBConnection();
			String updateStorPro = "{call usp_updateGroupAdmin(?,?,?,?)}";
			
			callableStatement = conn.prepareCall(updateStorPro);
			
			callableStatement.setLong(1, groupBean.getUserId());
			callableStatement.setLong(2, groupBean.getGroupId());
			callableStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
			callableStatement.registerOutParameter(4, Types.INTEGER);
			
			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(4);
			
	        System.out.println("value  :  "+value+"  isError : "+isError);
			
			if(value != 0){
				responseJson.put("status", true);
				responseJson.put("message","Left the group");
			}else{
				responseJson.put("status", true);
				responseJson.put("message","You are not a admin so you don't have access to remove the group");
			}
			return responseJson.toString();	
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeCallableSatetment(callableStatement);
	}	
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");
		return responseJson.toString();
	}

	@GET
	@Path("/getGroupInfo/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getGroupInfo(@PathParam("userId") Long userId){
		System.out.println("System  coming to method getGroupInfo :  "+userId);
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		JSONArray finalJsonArray = new JSONArray();
		JSONObject responseJson = new JSONObject();
		
		try{
			
			List<GroupInfoBean> groupInfoBeanList = getAllGroupByUserId(userId);
			DBObject dbObj = null;
			if(! groupInfoBeanList.isEmpty()){
				
				String sql = "SELECT FG.GroupID,U.UserID,U.FirstName,U.LastName,U.ProfileImageID FROM tbl_FriendGroup AS FG  "
						+"INNER JOIN tbl_users AS U ON FG.UserID = U.UserID WHERE FG.GroupID =? AND FG.IsActive=?";	
				
				conn = DataSourceConnection.getDBConnection();
				preparedStatement = conn.prepareStatement(sql);
				MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
		      	for (GroupInfoBean groupInfoBean : groupInfoBeanList) {
		      		
		      		System.out.println("groupInfoBean: "+groupInfoBean.getProfileImageId());
		      		
		      		JSONArray receiptionistArray = new JSONArray();					
		      		JSONObject outerJson = new JSONObject();
		      		
		      		preparedStatement.setLong(1, groupInfoBean.getGroupId());
		      		preparedStatement.setInt(2, 0);
		      		
					ResultSet rs = preparedStatement.executeQuery();
					outerJson.put("groupId", groupInfoBean.getGroupId());
					outerJson.put("groupName", groupInfoBean.getGroupName());
					outerJson.put("adminId", groupInfoBean.getUserId());
					outerJson.put("fullName", groupInfoBean.getFullName());
					if(Utility.checkValidString(groupInfoBean.getProfileImageId())){
						dbObj = mongoDBJDBC.getFile(groupInfoBean.getProfileImageId());
						if(dbObj != null){
							outerJson.put("image", new JSONObject(dbObj.toString()).getJSONObject("_id").getString("$oid"));
						}
					}
					
					while(rs.next()){
					
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("fullName", rs.getString("U.FirstName")+" "+rs.getString("U.LastName"));
						dbObj = null;
						if(Utility.checkValidString(rs.getString("U.ProfileImageID"))){
						 dbObj = mongoDBJDBC.getFile(rs.getString("U.ProfileImageID"));
						if(dbObj != null){
							jsonObject.put("image", new JSONObject(dbObj.toString()).getJSONObject("_id").getString("$oid"));
						}
						}
						jsonObject.put("userId", rs.getString("U.UserID"));
						
						receiptionistArray.put(jsonObject);
					}
					outerJson.put("receiptionistArray", receiptionistArray);
					finalJsonArray.put(outerJson);
		      	}
				responseJson.put("message","group fetched successfully");
				}else{
				   responseJson.put("message","You have no group");
				}
			responseJson.put("status", true);
			responseJson.put("groupArray", finalJsonArray);			
			return responseJson.toString();
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(preparedStatement);
	}
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");
		return responseJson.toString();
	}
	
	
	public List<GroupInfoBean> getAllGroupByUserId(Long userId){

		Connection conn = null;
		PreparedStatement preparedStatement = null;	
		List<GroupInfoBean> list = new ArrayList<>();
		try{
			String sql = "SELECT  DISTINCT(G.GroupID) , G.UserID ,G.GroupName,U.FirstName,U.LastName,U.ProfileImageID  "
					   + "FROM FrissDB.tbl_Group G INNER JOIN FrissDB.tbl_FriendGroup GF  ON G.GroupID=GF.GroupID "
				       + "INNER JOIN FrissDB.tbl_users U  ON G.UserID=U.UserID "
				       + "WHERE (G.UserID=? OR GF.UserID=?) AND G.IsActive=0 AND GF.IsActive=0";	
			
			
			conn = DataSourceConnection.getDBConnection();
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, userId);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while(rs.next()){
				GroupInfoBean groupInfoBean = new GroupInfoBean();
				groupInfoBean.setGroupId(rs.getLong("GroupID"));
				groupInfoBean.setUserId(rs.getLong("UserID"));
				groupInfoBean.setGroupName(rs.getString("GroupName"));
				groupInfoBean.setFullName(rs.getString("FirstName")+" "+rs.getString("LastName"));
				groupInfoBean.setProfileImageId(rs.getString("ProfileImageID"));
				
				list.add(groupInfoBean);
			}
			
			
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(preparedStatement);
	}
		
		return list;
	}
	
	
	public Boolean addGroupMember(GroupBean groupBean){
		Connection conn = null;
		PreparedStatement preparedStatement = null;	
		try{
			String sql = "INSERT INTO tbl_FriendGroup (GroupID,UserID,CreratedDateTime) VALUES(?,?,?)";	
			conn = DataSourceConnection.getDBConnection();
			preparedStatement = conn.prepareStatement(sql);
			if(groupBean.getFriendList().isEmpty()){
			
				preparedStatement.setLong(1, groupBean.getGroupId());
				preparedStatement.setLong(2, groupBean.getFriendId());
				preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
				if(preparedStatement.executeUpdate() != 0){
					return true;
				}
			}else{
				for(Long friendId : groupBean.getFriendList()){
					
					preparedStatement.setLong(1, groupBean.getGroupId());
					preparedStatement.setLong(2, friendId);
					preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
					
					preparedStatement.addBatch();
				}
				int[] friendInserted = preparedStatement.executeBatch();	
				if(friendInserted.length != 0){
					return true;
				}
			}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		ServiceUtility.closeConnection(conn);
		ServiceUtility.closeSatetment(preparedStatement);
	}
		return false;
	}
	
	public GroupBean getGroupAdmin(Long userId){
		
		Connection conn = null;
		PreparedStatement preparedStatement = null;	
		GroupBean groupBean = null;
		try{
		conn = DataSourceConnection.getDBConnection();
		String sql = "SELECT * FROM tbl_Group WHERE UserID=? AND IsActive=?";	
		preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setLong(1, userId);
		preparedStatement.setInt(2, 0);
		
	    ResultSet rs = preparedStatement.executeQuery();
		
	    while(rs.next()){
	    
	    	if(rs.getLong("GroupID") != 0l){
	    		groupBean = new GroupBean();
	    		groupBean.setGroupId(rs.getLong("GroupID"));
	    		groupBean.setUserId(rs.getLong("UserID"));
	    		groupBean.setGroupName(rs.getString("GroupName"));
	    	}
	    }
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(preparedStatement);	
		}
		return groupBean;
	}
}
