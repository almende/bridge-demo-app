package com.almende.bridge.agent;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.almende.bridge.types.Location;
import com.almende.bridge.types.SitRep;
import com.almende.bridge.types.Task;
import com.almende.bridge.types.TeamStatus;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.agent.AgentSignal;
import com.almende.eve.monitor.Poll;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.transport.xmpp.XmppService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class TeamMember extends Agent {
	private static final String	MOBILE			= "Smack";
	private static final URI	DIRECTORY		= URI.create("local:yellow");
	private static final URI	SITREP			= URI.create("local:sitRep");
	
	@Override
	public void signalAgent(AgentSignal<?> event) throws JSONRPCException, IOException{
		super.signalAgent(event);
		if (AgentSignal.SETSCHEDULERFACTORY.equals(event.getEvent())){
			getScheduler().createTask(new JSONRequest("pokePhone",JOM.createObjectNode()), 10000);
		}
	}
	
	public ObjectNode requestStatus() throws ProtocolException,
			JSONRPCException {
		ObjectNode status = JOM.createObjectNode();
		status.put("name", getId());
		status.put("id", getGuid());
		status.put("type", getResType());
		String deploymentStatus = getDeploymentStatus();
		if (deploymentStatus != null) {
			status.put("deploymentStatus", deploymentStatus);
		}
		Location location = getLocation();
		if (location != null) status.put("current", JOM.getInstance()
				.valueToTree(location));
		
		Location goal = getGoal();
		if (goal != null) status.put("goal", JOM.getInstance()
				.valueToTree(goal));
		String taskDescription = getTaskDescription();
		if (taskDescription != null) status.put("task", taskDescription);
		return status;
	}
	
	public void triggerTask() throws IOException {
		getEventsFactory().trigger("taskUpdated");
	}
	
	public Task getTask() throws ProtocolException, JSONRPCException {
		return send(URI.create(getTeam()), "getTask", null, Task.class);
	}
	
	public void triggerTeamStatus() throws IOException {
		getEventsFactory().trigger("teamStatusUpdated");
	}
	
	public TeamStatus getTeamStatus() throws ProtocolException,
			JSONRPCException {
		return send(URI.create(getTeam()), "getTeamStatus", null,
				TeamStatus.class);
	}
	
	public Location getGoal() throws ProtocolException, JSONRPCException {
		Task task = getTask();
		if (task != null) {
			return new Location(task.getLat(), task.getLon(),
					task.getAssignmentDate());
		} else {
			return null;
		}
	}
	
	public String getTaskDescription() throws ProtocolException,
			JSONRPCException {
		Task task = getTask();
		if (task != null) {
			return task.getTitle();
		} else {
			return null;
		}
	}
	
	public String getDeploymentStatus() throws ProtocolException,
			JSONRPCException {
		TeamStatus teamStatus = send(URI.create(getTeam()), "getTeamStatus",
				null, TeamStatus.class);
		if (teamStatus != null) {
			return teamStatus.getDeploymentStatus();
		} else {
			return null;
		}
	}
	
	public String getResType() {
		if (getState().containsKey("ResType")) {
			return getState().get("ResType", String.class);
		}
		return "";
	}
	
	public void setResType(@Name("resType") String resType) {
		getState().put("ResType", resType);
	}
	
	public String getGuid() {
		if (getState().containsKey("Guid")) {
			return getState().get("Guid", String.class);
		}
		return "";
	}
	
	public void setGuid(@Name("guid") String guid) {
		getState().put("Guid", guid);
		ObjectNode params = JOM.createObjectNode();
		params.put("key", guid);
		params.put("value", getFirstUrl().toString());
		try {
			send(DIRECTORY, "set", params);
		} catch (Exception e) {
			System.err.println("Couldn't register agent to directory!");
			e.printStackTrace();
		}
	}
	
	public String getTeam() {
		if (getState().containsKey("Team")) {
			return getState().get("Team", String.class);
		}
		return "";
	}
	
	public void setTeam(@Name("team") String team) {
		getState().put("Team", team);
	}
	
	public String getXmppAccount() {
		if (getState().containsKey("XMPPAccount")) {
			return getState().get("XMPPAccount", String.class);
		}
		return "";
	}
	
	public void setXmppAccount(@Name("XMPPAccount") String xmpp_account) {
		getState().put("XMPPAccount", xmpp_account);
	}
	
	public String getXmppPassword() {
		if (getState().containsKey("XMPPPassword")) {
			return getState().get("XMPPPassword", String.class);
		}
		return "";
	}
	
	public void setXmppPassword(@Name("XMPPPassword") String xmpp_password) {
		getState().put("XMPPPassword", xmpp_password);
	}
	
	public SitRep getSitRep() throws ProtocolException, JSONRPCException {
		ObjectNode params = JOM.createObjectNode();
		params.put("team", getTeam());
		return send(SITREP, "getSitRep", params, SitRep.class);
	}
	
	public Location getLocation() {
		return new Location(getState().get("latitude", String.class),
				getState().get("longitude", String.class));
	}
	
	public void setLocation(@Name("location") Location location) {
		getState().put("latitude", location.getLatitude());
		getState().put("longitude", location.getLongitude());
	}
	
	public String callOtherAgent(@Name("url") String url,
			@Name("method") String method, @Name("params") ObjectNode params)
			throws IOException, JSONRPCException, Exception {
		Object resp = send(URI.create(url), method, params,
				Object.class);
		System.out.println("callOtherAgent url=" + url + " method=" + method
				+ ", params=" + params.toString() + ", resp=" + resp);
		if (resp != null) {
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
	
	public void xmppConnect() throws IllegalStateException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, JSONRPCException, IOException {
		AgentHost factory = getAgentHost();
		String username = getXmppAccount();
		String password = getXmppPassword();
		
		XmppService service = (XmppService) factory.getTransportService("xmpp");
		if (service != null) {
			service.connect(getId(), username, password, "Cloud");
			service.disconnect(getId());
			service.connect(getId(), username, password, "Cloud");
			
		} else {
			throw new IllegalStateException("No XMPP service registered");
		}
	}
	
	protected URI getPhoneUri() {
		List<String> urls = getUrls();
		URI myUrl = getFirstUrl();
		for (String item : urls) {
			try {
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
			
			URI mobileUri = URI.create(myUrl.getScheme() + username + "@"
					+ host + "/" + MOBILE);
			return mobileUri;
		}
		return null;
	}
	
	public void pokePhone() {
		try {
			System.out.println("Poking my phone:"+getId());
			xmppConnect();
			Thread.sleep(100);
			sendAsync(getPhoneUri(), "poke", JOM.createObjectNode(), null,
					Void.class);
		} catch (Exception e) {
			System.out.println("Couldn't poke phone, not online? "
					+ e.getLocalizedMessage());
		}
	}
	
	public void setupMonitoring() {
		
		System.err.println("Calling subscribeMonitor()");
		
		String monitorId = getResultMonitorFactory().create("locationMonitor",
				getPhoneUri(), "getLocation", JOM.createObjectNode(),
				"wrapLocation", new Poll(15000));
		
		System.out.println("Monitor id:"
				+ monitorId
				+ " -> "
				+ getResultMonitorFactory().getMonitorById(monitorId)
						.toString());
	}
	
	public void wrapLocation(@Name("result") String location)
			throws JSONRPCException, JsonProcessingException, IOException {
		// LOG.warning("Received location:" + location);
		Location loc = JOM.getInstance().readValue(location, Location.class);
		if (loc != null) {
			setLocation(loc);
		}
	}
	
}
