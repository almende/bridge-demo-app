package com.almende.bridge.agent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.almende.eve.monitor.Poll;
import com.almende.eve.monitor.Push;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class TeamLeader extends TeamMember {
	private static final String	MOBILE	= "Smack";

	public void setupMonitoring() {
		super.setupMonitoring();
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
			
			URI mobileUri = URI.create(myUrl.getScheme() + username + "@" + host + "/"
					+ MOBILE);
			
			String monitorId = getResultMonitorFactory().create("teamStatusMonitor",
					mobileUri, "getTeamStatus", JOM.createObjectNode(), "wrapTeamStatus",
					new Poll(600000), new Push().onEvent("taskUpdated"));
			
			System.out.println("Monitor id:"
					+ monitorId
					+ " -> "
					+ getResultMonitorFactory().getMonitorById(monitorId)
							.toString());
		}
	}
	
	public void wrapTeamStatus(@Name("result") String teamStatus) throws JSONRPCException, JsonProcessingException, IOException{
		ObjectNode params = JOM.createObjectNode();
		params.put("status", JOM.getInstance().readTree(teamStatus));
		send(URI.create(getTeam()),"setTeamStatus",params);
	}
}
