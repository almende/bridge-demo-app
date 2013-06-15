package com.almende.bridge.demoApp;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.types.Task;
import com.almende.bridge.demoApp.util.BusProvider;
import com.almende.eve.agent.AgentHost;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

/**
 * The map view of the Bridge app.
 * 
 */
public class MyMapFragment extends MapFragment {
	Marker	mTask	= null;
	
	public MyMapFragment() {
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);
		System.err.println("Initializing the map!");
		UiSettings settings = getMap().getUiSettings();
		
		getMap().setMyLocationEnabled(true);
		settings.setAllGesturesEnabled(true);
		settings.setMyLocationButtonEnabled(true);
		
		BusProvider.getBus().register(this);
		System.err.println("Map registered for event bus.");
		return root;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setMapOverlays();
	}
	
	public void setMapOverlays() {
		
		try {
			AgentHost host = AgentHost.getInstance();
			BridgeDemoAgent agent = (BridgeDemoAgent) host
					.getAgent(EveService.DEMO_AGENT);
			Task task = agent.getTask();
			LatLng myLoc=null;
			LatLng taskLoc = null;

			//TODO: this is production, for demonstration/development, get simulated location from agent!
			Location location = getMap().getMyLocation();
			if (location != null) {
				myLoc = new LatLng(location.getLatitude(),location.getLongitude());
			}
			if (mTask != null) {
				mTask.remove();
			}

			if (task != null && !task.getStatus().equals(Task.COMPLETE)) {
				Double lat = Double.valueOf(task.getLat());
				Double lon = Double.valueOf(task.getLon());
				taskLoc = new LatLng(lat,lon);
				mTask = getMap().addMarker(
						new MarkerOptions().position(taskLoc));
				
				getMap().setOnMarkerClickListener(new OnMarkerClickListener() {
					
					@Override
					public boolean onMarkerClick(Marker marker) {
						if (marker.equals(mTask)) {
							getActivity().getActionBar()
									.setSelectedNavigationItem(1);
							return true;
						}
						
						return false;
					}
				});
			} 
			zoomToInclude(myLoc,taskLoc);
		} catch (Exception e) {
			
			System.err.println("Failed to add Task Marker");
			e.printStackTrace();
			
		}
	}
	
	private void zoomToInclude(LatLng loc1, LatLng loc2) {
		if (loc1 == null && loc2 != null) {
			loc1 = loc2;
			loc2 = null;
		}
		if (loc1 == null) {
			//TODO: Default location from Stavanger demo! Put in agent or config?
			getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(58.9173,5.5851), 15));
		}
		if (loc2 == null) {
			getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(loc1, 15));
		} else {
			LatLngBounds bounds = new LatLngBounds.Builder().include(loc1)
					.include(loc2).build();
			getMap().animateCamera(
					CameraUpdateFactory.newLatLngBounds(bounds, 35));
		}
	}
	
	@Subscribe
	public void onEvent(StateEvent event) {
		System.err.println("MapFragment received StateEvent! "
				+ event.getAgentId() + ":" + event.getValue());
		if (event.getValue().equals("taskUpdated")
				&& event.getAgentId().equals(EveService.DEMO_AGENT)) {
			setMapOverlays();
		}
		if (event.getValue().equals("agentsUp")) {
			setMapOverlays();
		}
	}
}
