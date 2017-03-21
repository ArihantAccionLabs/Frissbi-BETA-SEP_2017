package org.kleverlinks.bean;

import org.json.JSONObject;

public class LocationBean {

	private Long userId;
	private String address;
	private String latitude ;
	private String longitude ;
	
	public LocationBean(){}
	
	public LocationBean(JSONObject jsonObject) {
		this.userId = jsonObject.getLong("userId");
		this.address = jsonObject.getString("address");
		this.latitude = jsonObject.getString("latitude");
		this.longitude = jsonObject.getString("longitude");
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
