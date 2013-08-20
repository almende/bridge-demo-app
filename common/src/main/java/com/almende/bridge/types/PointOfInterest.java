package com.almende.bridge.types;

import java.io.Serializable;

public class PointOfInterest implements Serializable {
    private static final long serialVersionUID = -931849620637717905L;
    private String lat;
    private String lon;
    private String label;
    private String color;
    private String icon;

    public PointOfInterest() {
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

    public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}