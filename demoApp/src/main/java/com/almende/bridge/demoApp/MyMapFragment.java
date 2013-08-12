package com.almende.bridge.demoApp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.types.PointOfInterest;
import com.almende.bridge.types.SitRep;
import com.almende.bridge.types.Task;
import com.almende.eve.agent.AgentHost;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
public class MyMapFragment extends MapFragment implements LocationListener,
        OnConnectionFailedListener, ConnectionCallbacks {
    private final String TAG = "MyMapFragment";
    Marker mTask = null;
    private volatile Location mLocation;
    private LocationClient mLocationClient;
    private GoogleMap mMap;
    private boolean mSuccesfullySetBounds;

    public MyMapFragment() {
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();
        }
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            UiSettings settings = mMap.getUiSettings();
            mMap.setMyLocationEnabled(true);
            settings.setAllGesturesEnabled(true);
            settings.setMyLocationButtonEnabled(true);
            setMapOverlays();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
        if (mMap != null) {
            mMap.setMyLocationEnabled(false);
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
    private LatLngBounds.Builder addPointsToMap(List<PointOfInterest> pointsOfInterest,
            LatLngBounds.Builder bounds) {
        for (PointOfInterest pointOfInterest : pointsOfInterest) {
            LatLng position = new LatLng(Double.parseDouble(pointOfInterest.getLat()),
                    Double.parseDouble(pointOfInterest.getLon()));
            if (position != null) {
                bounds.include(position);
                BitmapDescriptor marker = getMarkerStyle(pointOfInterest);
                mMap.addMarker(new MarkerOptions().position(position).icon(marker));
            }
        }
        return bounds;
    }

    private LatLngBounds.Builder addPointsToMap(HashMap<String, PointOfInterest> pointsOfInterest,
            LatLngBounds.Builder bounds) {
        for (Map.Entry<String, PointOfInterest> entry : pointsOfInterest.entrySet()) {
            String label = entry.getKey();
            PointOfInterest pointOfInterest = entry.getValue();
            LatLng position = new LatLng(Double.parseDouble(pointOfInterest.getLat()),
                    Double.parseDouble(pointOfInterest.getLon()));

            if (position != null) {
                bounds.include(position);

                BitmapDescriptor marker = getMarkerStyle(pointOfInterest);

                mMap.addMarker(new MarkerOptions().position(position).icon(marker).title(label));
            }
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
            LatLng taskLoc = null;
            if (agent != null) {
                task = agent.getTask();
                sitRep = agent.getSitRep();
            }

            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            getMap().clear();
            bounds = addPointsToMap(sitRep.getControlPosts(), bounds);
            bounds = addPointsToMap(sitRep.getIncidents(), bounds);
            bounds = addPointsToMap(sitRep.getOthers(), bounds);
            bounds = addPointsToMap(sitRep.getTeams(), bounds);

            if (mTask != null) {
                mTask.remove();
            }

            if (task != null && !task.getStatus().equals(Task.COMPLETE)) {
                Double lat = Double.valueOf(task.getLat());
                Double lon = Double.valueOf(task.getLon());
                taskLoc = new LatLng(lat, lon);
                mTask = mMap.addMarker(new MarkerOptions().position(taskLoc).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.marker_task)));

                mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.equals(mTask)) {
                            getActivity().getActionBar().setSelectedNavigationItem(1);
                            return true;
                        }

                        return false;
                    }
                });

                if (mLocation != null) {
                    bounds.include(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
                }

                bounds.include(taskLoc);
            }
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
                mSuccesfullySetBounds = true;
            } catch (Exception e) {
                // map not ready retry in thread until successful;
                setBoundsRetry(bounds);

            }
        } catch (Exception e) {

        }
    }

    public void onEventMainThread(StateEvent event) {
        System.err.println("MapFragment received StateEvent! " + event.getAgentId() + ":"
                + event.getValue());

        if ((event.getValue().equals("taskUpdated") || event.getValue().equals("newTask"))
                && event.getAgentId().equals(EveService.DEMO_AGENT)) {
            if (mMap != null) {
                setMapOverlays();
            }
        }
        if (event.getValue().equals("agentsUp")) {
            if (mMap != null) {
                setMapOverlays();
            }
        }

        if (event.getValue().equals("teamMoved")) {
            if (mMap != null) {
                setMapOverlays();
            }
        }
        if (event.getValue().equals("newSitRep")) {
            if (mMap != null) {
                setMapOverlays();
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Location previousLocation = mLocation;
        mLocation = location;
        if (previousLocation == null) {
            setMapOverlays();
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(getActivity().getApplicationContext(), this, // ConnectionCallbacks
                    this); // OnConnectionFailedListener

        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationRequest request = LocationRequest
                .create()
                .setInterval(30000)
                // 30
                // seconds
                .setFastestInterval(10000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setSmallestDisplacement(50); // trigger onLocationChange every 50
                                              // meters
        mLocationClient.requestLocationUpdates(request, this);

    }

    @Override
    public void onDisconnected() {
        // Do nothing

    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // Don nothing

    }

    /**
     * Method for fixing the bug where the apps map won't zoom in after starting the app when it has
     * previously been closed with the back button
     * 
     * @param bounds
     */
    private void setBoundsRetry(final LatLngBounds.Builder bounds) {
        new Thread() {
            @Override
            public void run() {
                mSuccesfullySetBounds = false;
                int count = 0;

                // wait 1 second once;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                // Do until successful or passing the count threshold
                while (!mSuccesfullySetBounds && count < 10) {
                    count++;
                    Log.d(TAG, "try " + count + " to apply bounds");
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getMap().animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
                                mSuccesfullySetBounds = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                mSuccesfullySetBounds = false;
                            }
                        }
                    };
                    synchronized (runnable) {
                        try {
                            getActivity().runOnUiThread(runnable);
                            // try again in 3 seconds
                            runnable.wait(3000);
                        } catch (InterruptedException e) {
                        }
                    }

                }

            };
        }.start();
    }
}
