package com.almende.bridge.agent;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.bridge.types.Task;
import com.almende.bridge.types.TeamStatus;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.State;
import com.almende.eve.transport.xmpp.XmppService;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class TeamLeaderAgent extends Agent {
	private static final Logger LOG = Logger.getLogger(TeamLeaderAgent.class.getName());
	private State myState = null;
	
	public void init(){
		myState=getState();
	}
		
	public Task getTask() {
		return myState.get("task", Task.class);
	}
	public void setTask(@Name("task") Task task) {
		myState.put("task", task);
		try {
			getEventsFactory().trigger("taskUpdated");
		} catch (IOException e) {
			LOG.log(Level.WARNING,"Can't issue event trigger!",e);
		}
	}
	public TeamStatus getStatus() {
		return myState.get("status",TeamStatus.class);
	}
	public void setStatus(@Name("status") TeamStatus status) {
		myState.put("status", status);
		try {
			getEventsFactory().trigger("statusUpdated");
		} catch (IOException e) {
			LOG.log(Level.WARNING,"Can't issue event trigger!",e);
		}
	}
	
	public void xmppConnect(@Name("username") String username, 
			@Name("password") String password) throws Exception {
		AgentHost factory = getAgentHost();
		
		XmppService service = (XmppService) factory.getTransportService("xmpp");
		if (service != null) {
			service.connect(getId(), username, password,"Cloud");
		}
		else {
			throw new Exception("No XMPP service registered");
		}
	}
	
	public String callOtherAgent(@Name("url") String url, @Name("method") String method, 
			@Name("params") ObjectNode params) 
			throws IOException, JSONRPCException, Exception {
		String resp = send(URI.create(url), method, params, JOM.getSimpleType(String.class));
		System.out.println("callOtherAgent url="+ url+" method=" + method  + ", params=" + params.toString() + ", resp=" +  resp);
		return resp;
	}
	
	public void xmppDisconnect() throws Exception {
		AgentHost factory = getAgentHost();
		XmppService service = (XmppService) factory.getTransportService("xmpp");
		if (service != null) {
			service.disconnect(getId());
		}
		else {
			throw new Exception("No XMPP service registered");
		}
	}

}
