package com.almende.bridge.demoApp.agent;

import java.io.IOException;

import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.types.Task;
import com.almende.bridge.demoApp.util.BusProvider;
import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

@Access(AccessType.PUBLIC)
public class BridgeDemoAgent extends Agent {
	private static final String	VERSION	= "3";
	private static final String	TASK	= "CurrentTask";
	
	public static String getBaseVersion() {
		return VERSION;
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}
	
	public void initTask() throws JSONRPCException, IOException {
//		Task task = new Task("Hi there, this is your new task: Enjoy!", "Ludo",
//				"2013-06-14 11:51:05", "Not acknowledged","58.92","5.58");
//		getState().put(TASK, JOM.getInstance().writeValueAsString(task));
		getState().remove(TASK);
		
		getScheduler().createTask(new JSONRequest("updateTask",JOM.createObjectNode()), 60000);
	}
	public void updateTask() throws JsonProcessingException {
		Task task = new Task("And this is another task!!", "Ludo",
				"2013-06-14 13:35:05", "Not acknowledged","58.9173","5.5851");
		getState().put(TASK, JOM.getInstance().writeValueAsString(task));
		BusProvider.getBus().post(new StateEvent(getId(),"taskUpdated"));
	}
	public Task getTask() throws JsonProcessingException, IOException{
		ObjectReader taskReader = JOM.getInstance().reader(Task.class);
		
		if (getState().containsKey(TASK)){
			String task = getState().get(TASK, String.class);
			System.err.println("Found task:"+task);
			return taskReader.readValue(task);
		} else {
			return null;
		}
	}
	
}
