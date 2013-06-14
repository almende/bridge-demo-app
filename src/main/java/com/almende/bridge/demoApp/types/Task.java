package com.almende.bridge.demoApp.types;

import java.io.Serializable;

public class Task implements Serializable {
	private static final long	serialVersionUID	= 6783092535568614883L;
	private String text;
	private String assigner;
	private String assignmentDate;
	private String status;
	private String lat;
	private String lon;
	
	public Task(){}
	
	public Task(String text, String assigner, String assignmentDate,
			String status,String lat, String lon) {
		this.text = text;
		this.assigner = assigner;
		this.assignmentDate = assignmentDate;
		this.status = status;
		this.lat=lat;
		this.lon=lon;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getAssigner() {
		return assigner;
	}
	public void setAssigner(String assigner) {
		this.assigner = assigner;
	}
	public String getAssignmentDate() {
		return assignmentDate;
	}
	public void setAssignmentDate(String assignmentDate) {
		this.assignmentDate = assignmentDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	
}
