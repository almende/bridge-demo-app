package com.almende.bridge.demoApp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.types.Task;
import com.almende.bridge.demoApp.util.BusProvider;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.scheduler.ClockSchedulerFactory;
import com.almende.eve.state.FileStateFactory;
import com.almende.eve.transport.xmpp.XmppService;
import com.squareup.otto.Subscribe;

public class EveService extends Service {
	
	public static final String	DEMO_AGENT	= "bridgeDemoApp";
	public static final int		NEWTASKID	= 0;
	private static AgentHost	host;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static void initHost(final Context ctx) {
		System.err.println("Init HOST called!!!!!!!!!!!!!!!!!!!!!!!!11");
		new Thread(new Runnable() {
			public void run() {
				
				BridgeDemoAgent.setContext(ctx);
				host = AgentHost.getInstance();
				try {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("AppContext", ctx);
					params.put("path", ctx.getFilesDir().getAbsolutePath()
							+ "/.eveagents");
					host.setStateFactory(new FileStateFactory(params));
					
				} catch (Exception e) {
					System.err
							.println("Couldn't start AndroidIntentStateFactory!");
					e.printStackTrace();
				}
				String hostUrl = "openid.almende.org";
				int port = 5222;
				String serviceName = hostUrl;
				XmppService xmppService = new XmppService(host, hostUrl, port,
						serviceName);
				host.addTransportService(xmppService);
				
				host.setSchedulerFactory(new ClockSchedulerFactory(host,
						new HashMap<String, Object>()));
				
				System.err.println("AgentHost started!");
				
				BridgeDemoAgent agent = null;
				try {
					if (host.hasAgent(DEMO_AGENT)
							&& host.getAgent(DEMO_AGENT) != null) {
						Agent test = host.getAgent(DEMO_AGENT);
						if (!"BridgeDemoAgent".equals(test.getType())
								|| !test.getVersion().equals(
										BridgeDemoAgent.getBaseVersion())) {
							System.err.println("Warning: replacing agent "
									+ DEMO_AGENT + " with new code version "
									+ BridgeDemoAgent.getBaseVersion());
							host.deleteAgent(DEMO_AGENT);
						}
						agent = (BridgeDemoAgent) test;
					} else {
						if (host.hasAgent(DEMO_AGENT)) {
							host.deleteAgent(DEMO_AGENT);
						}
						agent = host.createAgent(BridgeDemoAgent.class,
								DEMO_AGENT);
					}
				} catch (Exception e) {
					System.err.println("Failed to find/create agent:"
							+ DEMO_AGENT);
					e.printStackTrace();
				}
				System.err.println("Agent created!");
				
				if (agent != null) {
					agent.reconnect();
				} else {
					System.err
							.println("Agent is still null, not reconnecting.");
				}
				
				if (agent != null) {
					try {
						agent.initTask();
						BusProvider.getBus().post(
								new StateEvent(null, "agentsUp"));
					} catch (Exception e) {
						System.err.println("Failed to init agent.");
						e.printStackTrace();
					}
				} else {
					System.err
							.println("Agent is still null, not setting up task");
				}
			}
		}).start();
	}
	
	/**
	 * Starts the service.
	 * 
	 * @see super#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initHost(this.getApplication());
		
		BusProvider.getBus().register(this);
		return START_STICKY;
	}
	
	@Subscribe
	public void onStateEvent(StateEvent event) {
		if (event.getValue().equals("newTask")
				&& event.getAgentId().equals(EveService.DEMO_AGENT)) {
			try {
				NewTaskNotification(event.getAgentId());
			} catch (Exception e) {
				System.err.println("Failed to produce notification!");
				e.printStackTrace();
			}
		}
		if (event.getValue().equals("taskUpdated")
				&& event.getAgentId().equals(EveService.DEMO_AGENT)) {
			rmNotification();
		}
		if (event.getValue().equals("settingsUpdated")) {
			try {
				((BridgeDemoAgent) host.getAgent(DEMO_AGENT)).reconnect();
			} catch (Exception e) {
				System.err
						.println("Failed to get Agent to handler settingsUpdated event");
				e.printStackTrace();
			}
		}
	}
	
	public void NewTaskNotification(String agentId) throws JSONRPCException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, IOException {
		
		BridgeDemoAgent agent = (BridgeDemoAgent) host.getAgent(DEMO_AGENT);
		Task task = agent.getTask();
		if (task != null) {
			String task_text = task.getText();
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent(this, BaseActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
					0);
			
			// Build notification
			// Actions are just fake
			Notification noti = new Notification.Builder(this)
					.setContentTitle("New task received!")
					.setContentText(task_text).setSmallIcon(R.drawable.icon)
					.setDefaults(Notification.DEFAULT_ALL)
					.setContentIntent(pIntent).build();
			
			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			
			notificationManager.notify(NEWTASKID, noti);
		}
	}
	
	public void rmNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(NEWTASKID);
	}
}
