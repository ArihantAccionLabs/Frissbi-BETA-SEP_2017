package org.util.service;

import java.io.Serializable;

public class Payload<T extends Object> implements Serializable{

	private Boolean response = false;
	private String message;
	private T jsonObject;
	
	public Boolean getResponse() {
		return response;
	}
	public void setResponse(Boolean response) {
		this.response = response;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getJsonObject() {
		return jsonObject;
	}
	public void setJsonObject(T jsonObject) {
		this.jsonObject = jsonObject;
	}

}
