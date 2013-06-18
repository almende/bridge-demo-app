package com.almende.bridge.demoApp;

import android.app.Activity;
import android.os.Bundle;

import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.util.BusProvider;

public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}
	
	@Override
	protected void onPause() {
		BusProvider.getBus().post(new StateEvent(EveService.DEMO_AGENT,"settingsUpdated"));
		super.onPause();
	}
}