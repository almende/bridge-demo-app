package com.almende.bridge.types;

import java.io.Serializable;

public class Task implements Serializable {
	private static final long	serialVersionUID	= 6783092535568614883L;
	public static final String	NOTCONFIRMED		= "not confirmed";
	public static final String	CONFIRMED			= "confirmed";
	public static final String	COMPLETE			= "completed";
	public static final String	POSTPONED			= "postponed";
	
	private String				text;
	private String				assigner;
	private String				assignmentDate;
	private String				status;
	private String				lat;
	private String				lon;
	
	public Task() {
	}
	
	public Task(String text, String assigner, String assignmentDate,
			String status, String lat, String lon) {
		this.text = text;
		this.assigner = assigner;
		this.assignmentDate = assignmentDate;
		this.status = status;
		this.lat = lat;
		this.lon = lon;
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
	
	public boolean compareField(String left, String right) {
		boolean result = false;
		if (left == null && right == null) result = true;
		if (left != null && left.equals(right)) result = true;
		return result;
	}
	
	public boolean eq(Task other) {
		return 
				(compareField(text,other.text) &&
				 compareField(assigner,other.assigner) &&
				 compareField(assignmentDate,other.assignmentDate) &&
				 compareField(lat,other.lat) &&
				 compareField(lon,other.lon));
	}
}