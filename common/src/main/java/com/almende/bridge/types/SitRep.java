package com.almende.bridge.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SitRep implements Serializable {
	private static final long	serialVersionUID	= 164969052047884356L;

	private ArrayList<PointOfInterest> controlPosts = new ArrayList<PointOfInterest>();
	private ArrayList<PointOfInterest> incidents = new ArrayList<PointOfInterest>();
	private ArrayList<PointOfInterest> others = new ArrayList<PointOfInterest>();
	private HashMap<String, PointOfInterest> teams = new HashMap<String,PointOfInterest>();

	public ArrayList<PointOfInterest> getControlPosts() {
	    
		return controlPosts;
	}

	public void setControlPosts(ArrayList<PointOfInterest> controlPosts) {
		this.controlPosts = controlPosts;
	}

	public ArrayList<PointOfInterest> getIncidents() {
		return incidents;
	}

	public void setIncidents(ArrayList<PointOfInterest> incidents) {
		this.incidents = incidents;
	}

	public ArrayList<PointOfInterest> getOthers() {
	    return others;
	}

	public void setOthers(ArrayList<PointOfInterest> others) {
		this.others = others;
	}

	public HashMap<String, PointOfInterest> getTeams() {
		return teams;
	}

	public void setTeams(HashMap<String, PointOfInterest> teams) {

		this.teams = teams;
	}

	public class PointOfInterest implements Serializable{
		private static final long	serialVersionUID	= -931849620637717905L;
		private String lat;
		private String lon;
		private String color;
		private String icon;
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
		public String getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color = color;
		}
		public void setIcon(String icon){
		    this.icon = icon;
		}
		public String getIcon(){
		    return icon;
		}
	}

    public boolean eq(SitRep other) {
        return(
        this.controlPosts.equals(other.controlPosts) && 
        this.incidents.equals(other.incidents) &&
        this.others.equals(other.others) &&
        this.teams.equals(other.teams));
    }
}
