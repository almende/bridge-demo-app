package com.almende.bridge.demoApp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.eve.agent.AgentHost;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import de.greenrobot.event.EventBus;

public class BaseActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String	STATE_SELECTED_NAVIGATION_ITEM			= "selected_navigation_item";
	private final static int	CONNECTION_FAILURE_RESOLUTION_REQUEST	= 9000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
		
		if (servicesConnected()){
			EveService.mLocationClient = new LocationClient(this.getApplicationContext(), this, this);
		}
		
		setContentView(R.layout.activity_base);
		setupActionBar();
		
		Button button = (Button) findViewById(R.id.callButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.err.println("CallButton clicked!");
				AgentHost host = AgentHost.getInstance();
				try {
					BridgeDemoAgent agent = (BridgeDemoAgent) host
							.getAgent(EveService.DEMO_AGENT);
					agent.callRedirect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (EveService.mLocationClient != null) EveService.mLocationClient.connect();
    }
	
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
    	if (EveService.mLocationClient != null) EveService.mLocationClient.disconnect();
        super.onStop();
    }
	
	public void onEvent(StateEvent event) {
		System.err.println("Activity received StateEvent! "
				+ event.getAgentId() + ":" + event.getValue());
	}
	
	public void setupActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.title_activity_map)
				.setTabListener(
						new TabListener<MyMapFragment>(this, "map",
								MyMapFragment.class)));
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.title_activity_task)
				.setTabListener(
						new TabListener<TaskFragment>(this, "task",
								TaskFragment.class)));
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.title_activity_team)
				.setTabListener(
						new TabListener<TeamFragment>(this, "team",
								TeamFragment.class)));
		
	}
	
	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog	mDialog;
		
		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		
		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.err.println("Click!" + item.getItemId());
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.preferenceItem:
				System.err.println("Opening settings!");
				startActivity(new Intent(this, SettingsActivity.class));
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}
	
	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			
			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getFragmentManager(), "Location Updates");
			}
			return false;
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					connectionResult.getErrorCode(), this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);
			
			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getFragmentManager(), "Location Updates");
			}
		}
	}
	
	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
}

class TabListener<T extends Fragment> implements ActionBar.TabListener {
	private Fragment		mFragment;
	private final Activity	mActivity;
	private final String	mTag;
	private final Class<T>	mClass;
	
	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tag
	 *            The identifier tag for the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 */
	public TabListener(Activity activity, String tag, Class<T> clz) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;
	}
	
	/* The following are each of the ActionBar.TabListener callbacks */
	
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Check if the fragment is already initialized
		if (mFragment == null) {
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(R.id.container, mFragment, mTag);
		} else {
			// If it exists, simply attach it in order to show it
			ft.attach(mFragment);
		}
	}
	
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null) {
			// Detach the fragment, because another one is being attached
			ft.detach(mFragment);
		}
	}
	
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// User selected the already selected tab. Usually do nothing.
	}
}
