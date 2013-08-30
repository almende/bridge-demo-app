package com.almende.bridge.types;

import java.io.Serializable;

public class TeamStatus implements Serializable {
    private static final long serialVersionUID = 6531452193120113009L;

    public static final String UNASSIGNED = "Unassigned";
    public static final String ASSIGNED = "Assigned";
    public static final String ACTIVE = "Active";
    public static final String WITHDRAWN = "Withdrawn";
    public static final String POST = "Post";

    private String teamId = "";
    private String teamLeaderName = "";
    private String deploymentStatus = "Unknown";
    private String lat = "";
    private String lon = "";

    public TeamStatus() {
    }

    public TeamStatus(String id, String leaderName) {
        this.teamId = id;
        this.teamLeaderName = leaderName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamLeaderName() {
        return teamLeaderName;
    }

    public void setTeamLeaderName(String teamLeaderName) {
        this.teamLeaderName = teamLeaderName;
    }

    public String getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(String deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
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

    public boolean eq(TeamStatus team) {
        if (team.getTeamId().equals(teamId) && team.getTeamLeaderName().equals(teamLeaderName)
                && team.getDeploymentStatus().equals(deploymentStatus) && team.getLat().equals(lat)
                && team.getLon().equals(lon)) {
            return true;
        } else {
            return false;
        }

    }
}
