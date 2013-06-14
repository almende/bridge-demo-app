package com.almende.bridge.demoApp;

import android.app.Application;
import android.content.Intent;

public class DemoApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		this.startService(new Intent(this, EveService.class));
	}
}
