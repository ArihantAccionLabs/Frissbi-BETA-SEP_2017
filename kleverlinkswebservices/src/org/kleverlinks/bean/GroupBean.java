package org.kleverlinks.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class GroupBean {

	private Long userId;
	
	private Long groupId;
	
	private Long friendId;
	
	private String groupName;
	
	private List<Long> friendList = new ArrayList<>();
	
    public GroupBean(){
	}
	
	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getFriendId() {
		return friendId;
	}

	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}

	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<Long> getFriendList() {
		return friendList;
	}
	public void setFriendList(List<Long> friendList) {
		this.friendList = friendList;
	}
	public GroupBean(JSONObject jsonObject) {
		super();
		this.userId = jsonObject.getLong("userId");
		
		if(jsonObject.has("groupId")){
			this.groupId = jsonObject.getLong("groupId");
		}
		if(jsonObject.has("groupName")){
			this.groupName = jsonObject.getString("groupName");
		}
		if(jsonObject.has("friendList")){
			for(int i=0 ; i<jsonObject.getJSONArray("friendList").length() ; i++){
				friendList.add(jsonObject.getJSONArray("friendList").getLong(i));
			}
		}
	}
}
