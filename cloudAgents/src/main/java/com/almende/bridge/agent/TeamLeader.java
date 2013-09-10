package com.almende.bridge.agent;

import java.io.IOException;
import java.net.URI;

import com.almende.eve.monitor.Poll;
import com.almende.eve.monitor.Push;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class TeamLeader extends TeamMember {

	public void setupMonitoring() {
		super.setupMonitoring();
					
			String monitorId = getResultMonitorFactory().create("teamStatusMonitor",
					getPhoneUri(), "getTeamStatus", JOM.createObjectNode(), "wrapTeamStatus",
					new Poll(600000), new Push().onEvent("taskUpdated"));
			
			System.out.println("Monitor id:"
					+ monitorId
					+ " -> "
					+ getResultMonitorFactory().getMonitorById(monitorId)
							.toString());
			
			monitorId = getResultMonitorFactory().create("taskMonitor",
					getPhoneUri(), "getTask", JOM.createObjectNode(), "wrapTask",
					new Poll(600000), new Push().onEvent("taskUpdated"));
			
			System.out.println("Monitor id:"
					+ monitorId
					+ " -> "
					+ getResultMonitorFactory().getMonitorById(monitorId)
							.toString());

	}
	
	public void wrapTeamStatus(@Name("result") String teamStatus) throws JSONRPCException, IOException{
		ObjectNode params = JOM.createObjectNode();
		params.put("status", JOM.getInstance().readTree(teamStatus));
		send(URI.create(getTeam()),"setTeamStatus",params);
	}
	public void wrapTask(@Name("result") String task) throws IOException, JSONRPCException {
		ObjectNode params = JOM.createObjectNode();
		params.put("task", JOM.getInstance().readTree(task));
		send(URI.create(getTeam()),"setTask",params);	}
}
