package com.almende.bridge.agent;

import java.io.IOException;
import java.net.URI;

import com.almende.bridge.types.Location;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.transport.xmpp.XmppService;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class TeamMember extends Agent {
	
	public String getTeam(){
		if (getState().containsKey("Team")){
			return getState().get("Team",String.class);
		}
		return "";
	}

	public void setTeam(@Name("team") String team){
		getState().put("Team",team);
	}

	
	public String getXmppAccount(){
		if (getState().containsKey("XMPPAccount")){
			return getState().get("XMPPAccount", String.class);
		}
		return "";
	}
	public void setXmppAccount(@Name("XMPPAccount") String xmpp_account){
		getState().put("XMPPAccount", xmpp_account);
	}
	
	public String getXmppPassword(){
		if (getState().containsKey("XMPPPassword")){
			return getState().get("XMPPPassword", String.class);
		}
		return "";
	}
	public void setXmppPassword(@Name("XMPPPassword") String xmpp_password){
		getState().put("XMPPPassword", xmpp_password);
	}
	
	public Location getLocation(){
		return new Location(getState().get("latitude",String.class),getState().get("longitude",String.class));
	}
	public void setLocation(@Name("location") Location location){
		getState().put("latitude",location.getLatitude());
		getState().put("longitude",location.getLongitude());
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
	
	public void xmppConnect() throws Exception {
		AgentHost factory = getAgentHost();
		String username = getXmppAccount();
		String password = getXmppPassword();
		
		XmppService service = (XmppService) factory.getTransportService("xmpp");
		if (service != null) {
			service.connect(getId(), username, password, "Cloud");
		} else {
			throw new Exception("No XMPP service registered");
		}
	}
	
}
