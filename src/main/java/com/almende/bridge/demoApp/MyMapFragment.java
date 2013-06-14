package com.almende.bridge.demoApp;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

/**
 * The map view of the Bridge app.
 * 
 */
public class MyMapFragment extends MapFragment {
	Marker	mTask	= null;
	
	public MyMapFragment(){
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);
		System.err.println("Initializing the map!");
		UiSettings settings = getMap().getUiSettings();
		
		getMap().setMyLocationEnabled(true);
		settings.setAllGesturesEnabled(true);
		settings.setMyLocationButtonEnabled(true);
		
		setTaskMarker();
		BusProvider.getBus().register(this);
		System.err.println("Map registered for event bus.");
		return root;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		System.err.println("on View created!");

		super.onViewCreated(view, savedInstanceState);
	}
	
	public void setTaskMarker() {
		System.err.println("SetTaskMarker!");

		try {
			AgentHost host = AgentHost.getInstance();
			BridgeDemoAgent agent = (BridgeDemoAgent) host
					.getAgent(EveService.DEMO_AGENT);
			Task task = agent.getTask();
			
			if (mTask != null) {
				mTask.remove();
			}
			
			Double lat = Double.valueOf(task.getLat());
			Double lon = Double.valueOf(task.getLon());
			mTask = getMap().addMarker(
					new MarkerOptions().position(new LatLng(lat, lon)));
			getMap().moveCamera(
					CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16));
				
			getMap().setOnMarkerClickListener(new OnMarkerClickListener() {
				
				@Override
				public boolean onMarkerClick(Marker marker) {
					if (marker.equals(mTask)) {
						getActivity().getActionBar().setSelectedNavigationItem(
								1);
						return true;
					}
					
					return false;
				}
				
			});
		} catch (Exception e) {
			
			System.err.println("Failed to add Task Marker");
			e.printStackTrace();
			
		}
	}
	
	@Subscribe
	public void onEvent(StateEvent event) {
		System.err.println("MapFragment received StateEvent! "+event.getAgentId()+":"+event.getValue());
		if (event.getValue().equals("taskUpdated") && event.getAgentId().equals(EveService.DEMO_AGENT)) {
			setTaskMarker();
		}
		if (event.getValue().equals("agentsUp")){
			setTaskMarker();
		}
	}
}
