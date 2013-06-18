package com.almende.bridge.demoApp.agent;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.almende.bridge.demoApp.R;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.types.Task;
import com.almende.bridge.demoApp.util.BusProvider;
import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.transport.xmpp.XmppService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

@Access(AccessType.PUBLIC)
public class BridgeDemoAgent extends Agent {
	private static final String	VERSION	= "4";
	private static final String	TASK	= "CurrentTask";
	private static Context		context	= null;
	
	public static void setContext(Context context) {
		BridgeDemoAgent.context = context;
	}
	
	public static String getBaseVersion() {
		return VERSION;
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}
	
	public void initTask() throws JSONRPCException, IOException {
		// Task task = new Task("Hi there, this is your new task: Enjoy!",
		// "Ludo",
		// "2013-06-14 11:51:05", Task.NOTACK,"58.92","5.58");
		// getState().put(TASK, JOM.getInstance().writeValueAsString(task));
		getState().remove(TASK);
		
		getScheduler().createTask(
				new JSONRequest("updateTask", JOM.createObjectNode()), 60000);
	}
	
	public void updateTask() throws JsonProcessingException {
		Task task = new Task("And this is another task!!", "Ludo",
				"2013-06-14 13:35:05", Task.NOTACK, "58.9173", "5.5851");
		setTask(task);
		BusProvider.getBus().post(new StateEvent(getId(), "newTask"));
	}
	
	public Task getTask() throws JsonProcessingException, IOException {
		ObjectReader taskReader = JOM.getInstance().reader(Task.class);
		
		if (getState().containsKey(TASK)) {
			String task = getState().get(TASK, String.class);
			System.err.println("Found task:" + task);
			return taskReader.readValue(task);
		} else {
			return null;
		}
	}
	
	public void setTask(Task task) throws JsonProcessingException {
		getState().put(TASK, JOM.getInstance().writeValueAsString(task));
	}
	
	public void delTask() {
		getState().remove(TASK);
	}
	
	public void reconnect() {
		try {
			XmppService xmppService = (XmppService) getAgentFactory()
					.getTransportService("xmpp");
			xmppService.disconnect(getId());
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			System.err.println("Prefs:"+prefs.getAll());
			String username = prefs.getString(context.getString(R.string.xmppUsername_key),
					"unset");
			String password = prefs.getString(context.getString(R.string.xmppPassword_key),
					"unset");
			xmppService.connect(getId(), username, password);
		} catch (Exception e) {
			System.err.println("Failed to (re-)connection XMPP connection");
			e.printStackTrace();
		}
	}
}
