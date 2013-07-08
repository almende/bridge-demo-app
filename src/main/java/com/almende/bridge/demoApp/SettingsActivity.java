package com.almende.bridge.demoApp;

import android.app.Activity;
import android.os.Bundle;

import com.almende.bridge.demoApp.event.StateEvent;

import de.greenrobot.event.EventBus;

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
		EventBus.getDefault().post(new StateEvent(EveService.DEMO_AGENT,"settingsUpdated"));
		super.onPause();
	}
}