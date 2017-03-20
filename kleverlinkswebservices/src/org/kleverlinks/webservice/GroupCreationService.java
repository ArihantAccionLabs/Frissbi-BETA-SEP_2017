package org.kleverlinks.webservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kleverlinks.bean.GroupBean;
import org.kleverlinks.bean.GroupInfoBean;
import org.mongo.dao.MongoDBJDBC;
import org.service.dto.NotificationInfoDTO;
import org.util.Utility;
import org.util.service.NotificationService;
import org.util.service.ServiceUtility;

@Path("GroupCreationService")
public class GroupCreationService {
	
	private static final String participantSql = "SELECT FG.GroupID,U.UserID,U.FirstName,U.LastName,U.ProfileImageID FROM tbl_FriendGroup AS FG  "
		                                         +"INNER JOIN tbl_users AS U ON FG.UserID = U.UserID WHERE FG.GroupID =? AND FG.IsActive=?";	
	
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
			String groupImageId = null;
			if(Utility.checkValidString(groupBean.getGroupImage())){
				MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
				groupImageId = mongoDBJDBC.insertFile(groupBean.getGroupImage());
			}
			
			conn = DataSourceConnection.getDBConnection();
			String insertGroupStorPro = "{call usp_insertGroup(?,?,?,?,?,?)}";
			callableStatement = conn.prepareCall(insertGroupStorPro);
			callableStatement.setLong(1, groupBean.getUserId());
			callableStatement.setString(2, groupBean.getGroupName());
			callableStatement.setString(3, groupImageId);
			callableStatement.setTimestamp(4, new Timestamp(new Date().getTime()));
			callableStatement.registerOutParameter(5, Types.INTEGER);
			callableStatement.registerOutParameter(6, Types.BIGINT);
			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(5);
			groupId = callableStatement.getLong(6);
			System.out.println("value "+value+" isError "+isError+" groupId "+groupId);
			if(value != 0 && groupId != null && groupId != 0l){
				groupBean.setGroupId(groupId);
			Boolean friendInserted =	addGroupMember(groupBean);
			
			if(friendInserted){
				JSONObject userObject = ServiceUtility.getUserDetailByUserId(groupBean.getUserId());
				String message = userObject.getString("fullName")+" added you to "+groupBean.getGroupName();
				
			   NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
			   notificationInfoDTO.setSenderUserId(groupBean.getUserId());
			   notificationInfoDTO.setGroupId(groupBean.getGroupId());
			   notificationInfoDTO.setMessage(message);
			   notificationInfoDTO.setUserList(groupBean.getFriendList());
			   notificationInfoDTO.setNotificationType(NotificationsEnum.ADDED_TO_GROUP.toString());
			   
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
    @Path("/removeOrExitGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	public String removeGroupMember(String deleteData){
		
		System.out.println(""+deleteData.toString());
		
	  JSONObject responseJson = new JSONObject();
      JSONObject jsonObject = new JSONObject(deleteData);
      Connection conn = null;
		PreparedStatement preparedStatement = null;	
		try{
			GroupBean groupBean = new GroupBean(jsonObject);
			System.out.println(""+groupBean.getGroupId()+"   "+groupBean.getUserId());
				
				String sql = "UPDATE tbl_FriendGroup SET IsActive=? WHERE  GroupID=? AND UserID=?";	
				conn = DataSourceConnection.getDBConnection();
				preparedStatement = conn.prepareStatement(sql);
				
				preparedStatement.setInt(1, 1);
				preparedStatement.setLong(2, groupBean.getGroupId());
				preparedStatement.setLong(3, groupBean.getUserId());
				int isUpdate= preparedStatement.executeUpdate();
				if(isUpdate != 0){
					responseJson.put("status", true);
					responseJson.put("message","Member is removed from group ");
				}else{
					responseJson.put("status", false);
					responseJson.put("message","Not able to update");
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
	public String updateGroupMember(String deleteData) {

		JSONObject responseJson = new JSONObject();
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		try {
			JSONObject jsonObject = new JSONObject(deleteData);
			GroupBean groupBean = new GroupBean(jsonObject);
			String sql = "SELECT GroupName FROM tbl_Group WHERE GroupID=? AND UserID=? AND IsActive=?";

			conn = DataSourceConnection.getDBConnection();
			preparedStatement = conn.prepareStatement(sql);

			preparedStatement.setLong(1, groupBean.getGroupId());
			preparedStatement.setLong(2, groupBean.getUserId());
			preparedStatement.setLong(3, 0);

			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				if (addGroupMember(groupBean)) {
					
					
					JSONObject userObject = ServiceUtility.getUserDetailByUserId(groupBean.getUserId());
					String message = userObject.getString("fullName")+" added you to "+groupBean.getGroupName();
					
				   NotificationInfoDTO notificationInfoDTO = new NotificationInfoDTO();
				   notificationInfoDTO.setSenderUserId(groupBean.getUserId());
				   notificationInfoDTO.setGroupId(groupBean.getGroupId());
				   notificationInfoDTO.setUserId(groupBean.getFriendId());
				   notificationInfoDTO.setMessage(message);
				   notificationInfoDTO.setNotificationType(NotificationsEnum.ADDED_TO_GROUP.toString());
				   
				   NotificationService.sendNewMemeberAddingNotification(notificationInfoDTO);
					
					
					
					responseJson.put("status", true);
					responseJson.put("message", "New memeber is added in group " + rs.getString("GroupName"));
				} else {
					responseJson.put("status", false);
					responseJson.put("message", "Something went wrong during adding the member");
				}
				return responseJson.toString();
			}

			responseJson.put("status", false);
			responseJson.put("message", "You are not a admin so can'nt modify group");

			return responseJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		responseJson.put("status", false);
		responseJson.put("message", "Oops something went wrong");
		return responseJson.toString();
	}

	
	@POST
    @Path("/updateOrDeleteGroupByAdmin")
	@Consumes(MediaType.APPLICATION_JSON)
	public String removeGroup(String groupData){
		
		JSONObject responseJson = new JSONObject();
		Connection conn = null;
		CallableStatement callableStatement = null;
		try{
			JSONObject jsonObject = new JSONObject(groupData);
			GroupBean groupBean = new GroupBean(jsonObject);
			
			conn = DataSourceConnection.getDBConnection();
			String updateStorPro = "{call usp_updateGroupAdmin(?,?,?,?,?)}";
			
			callableStatement = conn.prepareCall(updateStorPro);
			
			callableStatement.setLong(1, groupBean.getUserId());
			callableStatement.setLong(2, groupBean.getGroupId());
			callableStatement.setBoolean(3, groupBean.getIsGroupDeletion());
			callableStatement.setTimestamp(4, new Timestamp(new Date().getTime()));
			callableStatement.registerOutParameter(5, Types.INTEGER);
			
			int value = callableStatement.executeUpdate();
			int isError = callableStatement.getInt(5);
			
	        System.out.println("value  :  "+value+"  isError : "+isError);
			
			if(value != 0){
				responseJson.put("status", true);
				responseJson.put("message",groupBean.getIsGroupDeletion() ? "Group is Deleted" : "Left the group");
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
	@Produces(MediaType.TEXT_PLAIN)
	public String getGroupInfo(@PathParam("userId") Long userId) {
		System.out.println("System  coming to method getGroupInfo :  " + userId);
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		JSONArray finalJsonArray = new JSONArray();
		JSONObject responseJson = new JSONObject();

		try {

			List<GroupInfoBean> groupInfoBeanList = getAllGroupByUserId(userId);
			if (!groupInfoBeanList.isEmpty()) {

				conn = DataSourceConnection.getDBConnection();
				preparedStatement = conn.prepareStatement(participantSql);
				MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
				for (GroupInfoBean groupInfoBean : groupInfoBeanList) {

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
						outerJson.put("adminImage", mongoDBJDBC.getUriImage(groupInfoBean.getProfileImageId()));
					}
					if(Utility.checkValidString(groupInfoBean.getGroupImage())){
						outerJson.put("groupImage", mongoDBJDBC.getUriImage(groupInfoBean.getGroupImage()));
					}
					
					while (rs.next()) {

						JSONObject jsonObject = new JSONObject();
						jsonObject.put("fullName", rs.getString("FirstName") + " " + rs.getString("LastName"));
						if (Utility.checkValidString(rs.getString("ProfileImageID"))) {
								jsonObject.put("profileImage", mongoDBJDBC.getUriImage(rs.getString("ProfileImageID").trim()));
						}
						jsonObject.put("userId", rs.getString("UserID"));

						receiptionistArray.put(jsonObject);
					}
					outerJson.put("receiptionistArray", receiptionistArray);
					finalJsonArray.put(outerJson);
				}
				responseJson.put("message", "group fetched successfully");
			} else {
				responseJson.put("message", "You have no group");
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
	
		@GET
		@Path("/getGroupInfoByGroupId/{groupId}")
		@Produces(MediaType.TEXT_PLAIN)
		public String getGroupInfoById(@PathParam("groupId") Long groupId) {

		System.out.println("System  coming to method getGroupInfoById :  " + groupId);
		JSONObject responseJson = new JSONObject();
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		JSONArray receiptionistArray = new JSONArray();
		try {
			GroupInfoBean groupInfoBean = getGroupAdmin(groupId);
			
			if (groupInfoBean != null) {

				responseJson.put("groupId", groupInfoBean.getGroupId());
				responseJson.put("groupName", groupInfoBean.getGroupName());
				responseJson.put("adminId", groupInfoBean.getUserId());
				responseJson.put("fullName", groupInfoBean.getFullName());

				MongoDBJDBC mongoDBJDBC = new MongoDBJDBC();
				
				if(Utility.checkValidString(groupInfoBean.getProfileImageId())){
					responseJson.put("adminImage", mongoDBJDBC.getUriImage(groupInfoBean.getProfileImageId()));
				}
				if(Utility.checkValidString(groupInfoBean.getGroupImage())){
					responseJson.put("groupImage", mongoDBJDBC.getUriImage(groupInfoBean.getGroupImage()));
				}

				conn = DataSourceConnection.getDBConnection();
				preparedStatement = conn.prepareStatement(participantSql);

				preparedStatement.setLong(1, groupInfoBean.getGroupId());
				preparedStatement.setInt(2, 0);

				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next()) {

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("fullName", rs.getString("U.FirstName") + " " + rs.getString("U.LastName"));
					if (Utility.checkValidString(rs.getString("U.ProfileImageID"))) {
							jsonObject.put("profileImage",mongoDBJDBC.getUriImage(rs.getString("U.ProfileImageID").trim()));
					}
					jsonObject.put("userId", rs.getString("U.UserID"));

					receiptionistArray.put(jsonObject);
				}
				responseJson.put("receiptionistArray", receiptionistArray);
			}

			responseJson.put("status", true);
			responseJson.put("message", "Group info fetched successfully");
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
			String sql = "SELECT  DISTINCT(G.GroupID) , G.UserID ,G.GroupName,G.GroupImage,U.FirstName,U.LastName,U.ProfileImageID  "
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
				groupInfoBean.setGroupImage(rs.getString("GroupImage"));
				
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
	
	public GroupInfoBean getGroupAdmin( Long groupId){
		
		Connection conn = null;
		PreparedStatement preparedStatement = null;	
		GroupInfoBean groupInfoBean = null;
		try{
		conn = DataSourceConnection.getDBConnection();
		
		String sql = "SELECT  G.GroupID , G.UserID ,G.GroupName,G.GroupImage,U.FirstName,U.LastName,U.ProfileImageID "
					   + "FROM tbl_Group G INNER JOIN tbl_users U  ON G.UserID=U.UserID "
				       + "WHERE G.GroupID=? AND G.IsActive=?";	
		
		preparedStatement = conn.prepareStatement(sql);
		preparedStatement.setLong(1, groupId);
		preparedStatement.setInt(2, 0);
		
	    ResultSet rs = preparedStatement.executeQuery();
		
	    while(rs.next()){
	    	groupInfoBean = new GroupInfoBean();
			groupInfoBean.setGroupId(rs.getLong("GroupID"));
			groupInfoBean.setUserId(rs.getLong("UserID"));
			groupInfoBean.setGroupName(rs.getString("GroupName"));
			groupInfoBean.setFullName(rs.getString("FirstName")+" "+rs.getString("LastName"));
			groupInfoBean.setProfileImageId(rs.getString("ProfileImageID"));
			groupInfoBean.setGroupImage(rs.getString("GroupImage"));
	    }
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeSatetment(preparedStatement);	
		}
		return groupInfoBean;
	}
}
