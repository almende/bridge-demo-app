package com.almende.bridge.types;

public class Location {
	private String	latitude	= null;
	private String	longitude	= null;
	private String	time		= null;
	
	public Location(String latitude, String longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public Location(String latitude, String longitude, String time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
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
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
}