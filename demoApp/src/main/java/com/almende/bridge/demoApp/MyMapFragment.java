package com.almende.bridge.demoApp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.types.SitRep;
import com.almende.bridge.types.SitRep.PointOfInterest;
import com.almende.bridge.types.Task;
import com.almende.eve.agent.AgentHost;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.greenrobot.event.EventBus;

/**
 * The map view of the Bridge app.
 * 
 */
public class MyMapFragment extends MapFragment {
    Marker mTask = null;

    public MyMapFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getMap() != null) {
            UiSettings settings = getMap().getUiSettings();

            getMap().setMyLocationEnabled(true);
            settings.setAllGesturesEnabled(true);
            settings.setMyLocationButtonEnabled(true);
            setMapOverlays();
        }

    }

    /**
     * Add pointOfInterest to the map and to the bound object
     * 
     * @param pointsOfInterest
     *            list of to be added pointsOfInterest object
     * @param bounds
     *            existing Builder object containing positions to be bound
     * @return inputed bounds including new bounds from added points
     */
    private LatLngBounds.Builder addPointsToMap(List<SitRep.PointOfInterest> pointsOfInterest,
            LatLngBounds.Builder bounds) {
        for (PointOfInterest pointOfInterest : pointsOfInterest) {
            LatLng position = new LatLng(Double.parseDouble(pointOfInterest.getLat()),
                    Double.parseDouble(pointOfInterest.getLon()));
            bounds.include(position);
            BitmapDescriptor marker = getMarkerStyle(pointOfInterest);
            getMap().addMarker(new MarkerOptions().position(position).icon(marker));
        }
        return bounds;
    }

    private LatLngBounds.Builder addPointsToMap(
            HashMap<String, SitRep.PointOfInterest> pointsOfInterest, LatLngBounds.Builder bounds) {
        for (Map.Entry<String, SitRep.PointOfInterest> entry : pointsOfInterest.entrySet()) {
            String label = entry.getKey();
            PointOfInterest pointOfInterest = entry.getValue();
            LatLng position = new LatLng(Double.parseDouble(pointOfInterest.getLat()),
                    Double.parseDouble(pointOfInterest.getLon()));
            bounds.include(position);

            BitmapDescriptor marker = getMarkerStyle(pointOfInterest);

            getMap().addMarker(new MarkerOptions().position(position).icon(marker).title(label));
        }
        return bounds;
    }

    private BitmapDescriptor getMarkerStyle(PointOfInterest pointOfInterest) {
        BitmapDescriptor marker = null;
        if (pointOfInterest.getIcon() != null && !pointOfInterest.getIcon().equals("")) {
            int resourceId = getActivity().getResources().getIdentifier(pointOfInterest.getIcon(),
                    "drawable", getActivity().getPackageName());

            if (resourceId != 0) {
                marker = BitmapDescriptorFactory.fromResource(resourceId);
            }
        }

        if (marker == null) {
            try {
                marker = BitmapDescriptorFactory.defaultMarker(Float.parseFloat(pointOfInterest
                        .getColor()));
            } catch (Exception e) {
                marker = BitmapDescriptorFactory.defaultMarker();
            }
        }

        return marker;
    }

    public void setMapOverlays() {

        try {
            AgentHost host = AgentHost.getInstance();
            BridgeDemoAgent agent = (BridgeDemoAgent) host.getAgent(EveService.DEMO_AGENT);
            Task task = null;
            SitRep sitRep = null;
            LatLng myLoc = null;
            LatLng taskLoc = null;
            if (agent != null) {
                task = agent.getTask();
                sitRep = agent.getSitRep();
            }

            LatLngBounds.Builder bounds = new LatLngBounds.Builder();

            bounds = addPointsToMap(sitRep.getControlPosts(), bounds);
            bounds = addPointsToMap(sitRep.getIncidents(), bounds);
            bounds = addPointsToMap(sitRep.getOthers(), bounds);
            bounds = addPointsToMap(sitRep.getTeams(), bounds);

            // TODO: this is production, for demonstration/development, get
            // simulated location from agent!

            // getMyLocation is @deprecated and always returns null
            // Location location = getMap().getMyLocation();
            // if (location != null) {
            // myLoc = new LatLng(location.getLatitude(),
            // location.getLongitude());
            // }
            if (mTask != null) {
                mTask.remove();
            }

            if (task != null && !task.getStatus().equals(Task.COMPLETE)) {
                Double lat = Double.valueOf(task.getLat());
                Double lon = Double.valueOf(task.getLon());
                taskLoc = new LatLng(lat, lon);
                mTask = getMap().addMarker(new MarkerOptions().position(taskLoc));

                getMap().setOnMarkerClickListener(new OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.equals(mTask)) {
                            getActivity().getActionBar().setSelectedNavigationItem(1);
                            return true;
                        }

                        return false;
                    }
                });
            }
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
            // zoomToInclude(myLoc, taskLoc);
        } catch (Exception e) {

            System.err.println("Failed to add Task Marker");
            e.printStackTrace();

        }
    }

    public void onEventMainThread(StateEvent event) {
        System.err.println("MapFragment received StateEvent! " + event.getAgentId() + ":"
                + event.getValue());

        if ((event.getValue().equals("taskUpdated") || event.getValue().equals("newTask"))
                && event.getAgentId().equals(EveService.DEMO_AGENT)) {
            if (getMap() != null) {
                setMapOverlays();
            }
        }
        if (event.getValue().equals("agentsUp")) {
            if (getMap() != null) {
                setMapOverlays();
            }
        }

        if (event.getValue().equals("teamMoved")) {
            if (getMap() != null) {
                setMapOverlays();
            }
        }
    }
}
