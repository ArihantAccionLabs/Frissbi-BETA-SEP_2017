package org.kleverlinks.bean;

import org.json.JSONObject;

public class FriendBean {

	private Long userId;
	private Long freindId;
	private String status;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getFreindId() {
		return freindId;
	}
	public void setFreindId(Long freindId) {
		this.freindId = freindId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	
	@Override
	public String toString() {
		return "FriendBean [userId=" + userId + ", freindId=" + freindId + ", status=" + status + "]";
	}
	public void toFriendBean(JSONObject friendJson){
		
      this.setUserId(friendJson.getLong("userId"));
      this.setFreindId(friendJson.getLong("friendId"));
      if(friendJson.has("status")){
    	  this.setStatus(friendJson.getString("status"));
      }
	}
}
