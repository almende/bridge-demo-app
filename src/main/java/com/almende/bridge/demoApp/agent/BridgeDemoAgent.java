package com.almende.bridge.demoApp.agent;

import java.io.IOException;
import java.net.URI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.almende.bridge.demoApp.R;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.types.Task;
import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.transport.AsyncCallback;
import com.almende.eve.transport.xmpp.XmppService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.greenrobot.event.EventBus;

@Access(AccessType.PUBLIC)
public class BridgeDemoAgent extends Agent {
	private static final String	VERSION	= "5";
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
		getState().remove(TASK);
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
	
	public void setTask(@Name("task") Task task,@Name("newTask") boolean newTask) throws JsonProcessingException {
		getState().put(TASK, JOM.getInstance().writeValueAsString(task));
		if (newTask){
			EventBus.getDefault().post(new StateEvent(getId(), "newTask"));
		}
	}
	
	public void delTask() {
		getState().remove(TASK);
	}
	
	public void reconnect() {
		try {
			XmppService xmppService = (XmppService) getAgentHost()
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
	
	public void callRedirect(){
		ObjectNode params = JOM.createObjectNode();
		params.put("address","+31624495602");
		params.put("url", "http://ask70.ask-cs.nl/~ask/askfastdemo/redirect?phone=0107421239");
		params.put("adapterID", "fe8aeeb0-3fb3-11e2-be8a-00007f000001");
		params.put("publicKey", "askfast1@ask-cs.com");
		params.put("privateKey", "47cdebf0-7131-11e2-8945-060dc6d9dd94");
		try {
			sendAsync(URI.create("http://ask-charlotte.appspot.com/rpc"),"outboundCall",params,new AsyncCallback<Void>(){
				public void onSuccess(Void result) {}
				public void onFailure(Exception exception) {}},Void.class);
		} catch (Exception e) {
			System.err.println("Failed to call outboundCall.");
			e.printStackTrace();
		}
	}
}
