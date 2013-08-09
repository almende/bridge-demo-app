package com.almende.bridge.demoApp;

import java.util.ArrayList;
import java.util.HashMap;

import com.almende.bridge.types.PointOfInterest;
import com.almende.bridge.types.SitRep;
import com.almende.bridge.types.Task;
import com.almende.bridge.types.TeamStatus;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class DummyData {

    private SitRep mSitRep;
    private ArrayList<PointOfInterest> mControlPosts;
    private ArrayList<PointOfInterest> mIncidents;
    private ArrayList<PointOfInterest> mOthers;
    private HashMap<String, PointOfInterest> mTeams;
    private TeamStatus mTeamStatus;

    private Task mTask;

    private DummyData() {
        mControlPosts = getDefaultControlPosts();
        mIncidents = getDefaultIncidents();
        mOthers = getDefaultOthers();
        mTeams = getDefaultTeams();

        mTeamStatus = getDefaultTeamStatus();
        mTask = getDefaultTask();
    }

    private static DummyData instance = null;

    public static DummyData getInstance() {
        if (null != instance) {
            return instance;
        } else {
            return instance = new DummyData();
        }
    }

    public static SitRep getDefaultSitRep() {
        SitRep sitRep = new SitRep();
        sitRep.setControlPosts(getDefaultControlPosts());
        sitRep.setIncidents(getDefaultIncidents());
        sitRep.setTeams(getDefaultTeams());
        sitRep.setOthers(getDefaultOthers());
        return sitRep;
    }

    public static ArrayList<PointOfInterest> getDefaultControlPosts() {
        SitRep sitRep = new SitRep();
        ArrayList<PointOfInterest> controlPosts = new ArrayList<PointOfInterest>();
        // Ask-cs
        PointOfInterest pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.908061");
        pointOfInterest.setLon("4.452381");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_AZURE));
        controlPosts.add(pointOfInterest);
        pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.908820");
        pointOfInterest.setLon("4.479564");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_BLUE));
        controlPosts.add(pointOfInterest);
        return controlPosts;
    }

    public static ArrayList<PointOfInterest> getDefaultIncidents() {
        SitRep sitRep = new SitRep();
        ArrayList<PointOfInterest> incidents = new ArrayList<PointOfInterest>();
        // Dummy data
        // Maas tunnel
        PointOfInterest pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.89995");
        pointOfInterest.setLon("4.46825");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_ORANGE));
        pointOfInterest.setIcon("marker_incident");
        incidents.add(pointOfInterest);

        // euromast
        pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.90551");
        pointOfInterest.setLon("4.46662");
        pointOfInterest.setIcon("marker_incident");
        incidents.add(pointOfInterest);
        return incidents;

    }

    public static HashMap<String, PointOfInterest> getDefaultTeams() {
        HashMap<String, PointOfInterest> teams = new HashMap<String, PointOfInterest>();
        SitRep sitRep = new SitRep();
        // Dummy data
        // Ziekenhuis
        PointOfInterest pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.909801");
        pointOfInterest.setLon("4.466918");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_CYAN));
        pointOfInterest.setIcon("marker_team");
        teams.put("MEDIC", pointOfInterest);

        // Brandweer
        pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.929071");
        pointOfInterest.setLon("4.576623");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_RED));
        pointOfInterest.setIcon("marker_team");
        teams.put("FIREFIGHTERS", pointOfInterest);

        return teams;
    }

    public static ArrayList<PointOfInterest> getDefaultOthers() {
        SitRep sitRep = new SitRep();
        ArrayList<PointOfInterest> others = new ArrayList<PointOfInterest>();
        // Dummy data
        // Sense
        PointOfInterest pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.905629");
        pointOfInterest.setLon("4.456931");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_VIOLET));
        others.add(pointOfInterest);

        // random
        pointOfInterest = new PointOfInterest();
        pointOfInterest.setLat("51.928826");
        pointOfInterest.setLon("4.429569");
        pointOfInterest.setColor(String.valueOf(BitmapDescriptorFactory.HUE_YELLOW));
        others.add(pointOfInterest);
        return others;

    }

    public static Task getDefaultTask() {
        Task task = new Task();
        task.setAssigner("Bob Bobson");
        task.setAssignmentDate("06-08-2013");
        task.setLat("51.902791");
        task.setLon("4.468026");
        task.setStatus(Task.CONFIRMED);
        task.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam et lacus nulla. Proin nec augue nec arcu blandit ultrices"
                + " placerat eget purus. Morbi faucibus sapien nec vulputate tempus. Pellentesque molestie vel neque non scelerisque. Cras aliquam "
                + "id felis eu porttitor. Mauris eget nisl tristique, semper ligula id, tempus est. Proin dapibus semper tellus et tincidunt. Nam"
                + " augue lacus, tempor vel turpis vitae, lacinia consequat lorem. Phasellus tempus condimentum dolor quis facilisis. Lorem ipsum dolor"
                + " sit amet, consectetur adipiscing elit. Mauris mollis ipsum quis massa porttitor, eget pharetra nisi semper. Proin egestas rutrum tortor,"
                + " nec suscipit urna mattis id. Praesent commodo, nisi vel placerat pellentesque, enim lacus laoreet purus, a mattis risus elit at ipsum."
                + " Proin fringilla malesuada molestie. Donec sit amet enim fermentum turpis rutrum euismod eu vitae tortor.");
        return task;

    }

    public TeamStatus getDefaultTeamStatus() {
        TeamStatus teamStatus = new TeamStatus();
        teamStatus.setDeploymentStatus(TeamStatus.ASSIGNED);
        teamStatus.setTeamId("ALPHA");
        teamStatus.setTeamLeaderName("Alex Alpha");
        teamStatus.setLat("51.914152");
        teamStatus.setLon("4.473026");
        return teamStatus;
    }

    public SitRep getSitRep() {
        return mSitRep;
    }

    public void setSitRep(SitRep sitRep) {
        this.mSitRep = sitRep;
    }

    public ArrayList<PointOfInterest> getControlPosts() {
        return mControlPosts;
    }

    public void setmControlPosts(ArrayList<PointOfInterest> controlPosts) {
        this.mControlPosts = controlPosts;
    }

    public ArrayList<PointOfInterest> getmIncidents() {
        return mIncidents;
    }

    public void setIncidents(ArrayList<PointOfInterest> incidents) {
        this.mIncidents = incidents;
    }

    public ArrayList<PointOfInterest> getOthers() {
        return mOthers;
    }

    public void setOthers(ArrayList<PointOfInterest> others) {
        this.mOthers = others;
    }

    public HashMap<String, PointOfInterest> getTeams() {
        return mTeams;
    }

    public void setTeams(HashMap<String, PointOfInterest> teams) {
        this.mTeams = teams;
    }

    public TeamStatus getTeamStatus() {
        return mTeamStatus;
    }

    public void setTeamStatus(TeamStatus teamStatus) {
        this.mTeamStatus = teamStatus;
    }

    public Task getTask() {
        return mTask;
    }

    public void setTask(Task task) {
        this.mTask = task;
    }

}
