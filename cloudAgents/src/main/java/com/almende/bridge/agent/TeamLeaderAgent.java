package com.almende.bridge.agent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.bridge.types.Task;
import com.almende.bridge.types.TeamStatus;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.monitor.Poll;
import com.almende.eve.monitor.Push;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.State;
import com.almende.eve.transport.xmpp.XmppService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class TeamLeaderAgent extends Agent {
	private static final Logger	LOG		= Logger.getLogger(TeamLeaderAgent.class
												.getName());
	private static final String	MOBILE	= "Smack";
	private State				myState	= null;
	
	
	public void create(){
		subscribeMonitor();
	}
	
	public void init() {
		myState = getState();
	}
	
	public Task getTask() {
		return myState.get("task", Task.class);
	}
	
	public void setTask(@Name("task") Task task) {
		myState.put("task", task);
		try {
			getEventsFactory().trigger("taskUpdated");
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Can't issue event trigger!", e);
		}
	}
	
	public TeamStatus getStatus() {
		return myState.get("status", TeamStatus.class);
	}
	
	public void setStatus(@Name("status") TeamStatus status) {
		myState.put("status", status);
		try {
			getEventsFactory().trigger("statusUpdated");
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Can't issue event trigger!", e);
		}
	}
	
	public void xmppConnect(@Name("username") String username,
			@Name("password") String password) throws Exception {
		AgentHost factory = getAgentHost();
		
		XmppService service = (XmppService) factory.getTransportService("xmpp");
		if (service != null) {
			service.connect(getId(), username, password, "Cloud");
		} else {
			throw new Exception("No XMPP service registered");
		}
	}
	
	public void subscribeMonitor() {
		System.err.println("Calling subscribeMonitor()");
		List<String> urls = getUrls();
		URI myUrl = getFirstUrl();
		for (String item : urls) {
			try {
				System.out.println("Url:" + item);
				if (item.startsWith("xmpp")) {
					myUrl = new URI(item);
					break;
				}
			} catch (URISyntaxException e) {
			}
		}
		if (myUrl.getScheme().startsWith("xmpp")) {
			String username = myUrl.toString().split("@")[0].substring(4);
			String host = myUrl.toString().split("@")[1].replaceAll("[:/].*",
					"");
			
			URI mobileUri = URI.create(myUrl.getScheme() + username + "@" + host
					+ "/" + MOBILE);
			
			String monitorId = getResultMonitorFactory().create(mobileUri,
					"getTeamStatus", JOM.createObjectNode(), "wrapTeamStatus",
					new Poll(600000),
					new Push().onInterval(10000).onEvent("taskUpdated"));
			System.out.println("Monitor id:"
					+ monitorId
					+ " -> "
					+ getResultMonitorFactory().getMonitorById(monitorId)
							.toString());
		} else {
			System.err.println("SubscribeMonitor: XMPP not yet initialized?");
		}
	}
	
	public void wrapTeamStatus(@Name("result") String teamStatus)
			throws IOException {
		setStatus(TypeUtil.inject(TeamStatus.class,
				JOM.getInstance().readTree(teamStatus)));
	}
	
	public String callOtherAgent(@Name("url") String url,
			@Name("method") String method, @Name("params") ObjectNode params)
			throws IOException, JSONRPCException, Exception {
		Object resp = send(URI.create(url), method, params,
				JOM.getSimpleType(Object.class));
		System.out.println("callOtherAgent url=" + url + " method=" + method
				+ ", params=" + params.toString() + ", resp=" + resp);
		if (resp != null){
			return resp.toString();
		} else {
			return "";
		}
	}
	
	public void xmppDisconnect() throws Exception {
		AgentHost factory = getAgentHost();
		XmppService service = (XmppService) factory.getTransportService("xmpp");
		if (service != null) {
			service.disconnect(getId());
		} else {
			throw new Exception("No XMPP service registered");
		}
	}
	
}
