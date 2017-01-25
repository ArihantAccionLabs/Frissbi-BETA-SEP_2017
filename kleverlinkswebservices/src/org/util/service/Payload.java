package org.util.service;

import java.io.Serializable;
import java.util.List;

public class Payload<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Boolean status = false;
	private String message;
	private T jsonObject;
	private List<T> listObject;
	
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
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
	public List<T> getListObject() {
		return listObject;
	}
	public void setListObject(List<T> listObject) {
		this.listObject = listObject;
	}
}
