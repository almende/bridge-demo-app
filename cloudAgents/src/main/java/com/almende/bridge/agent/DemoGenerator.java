package com.almende.bridge.agent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.almende.bridge.types.Location;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class DemoGenerator extends Agent {
	
	public ArrayNode getAllResources() throws JSONRPCException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		AgentHost host = getAgentHost();
		ArrayNode result = JOM.createArrayNode();
		GenList agentList = (GenList) host.getAgent("demolist");
		ArrayNode list = agentList.getList();
		
		for (JsonNode item : list) {
			try {
				Agent agent = host.getAgent(item.textValue());
				if (!agent.getType().equals("Team")){
					ObjectNode elem = JOM.createObjectNode();
					elem.put("url", agent.getUrls().get(0));
					result.add(elem);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void clearDemo() throws JSONRPCException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		AgentHost host = getAgentHost();
		GenList agentList = (GenList) host.getAgent("demolist");
		ArrayNode list = agentList.getList();
		
		for (JsonNode item : list) {
			host.deleteAgent(item.textValue());
		}
		agentList.empty();
	}
	
	public void resetDemo() throws JSONRPCException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException{
		String demoData = getState().get("demoData",String.class);
		if (demoData != null){
			clearDemo();
			loadDemo(JOM.getInstance().readTree(demoData));
		} else {
			throw new JSONRPCException("DemoData was not set, please load new data through loadDemo()!");
		}
	}
	public JsonNode getDemoData() throws JsonProcessingException, IOException{
		String demoData = getState().get("demoData",String.class);
		if (demoData != null){
			return JOM.getInstance().readTree(demoData);
		}
		return null;
	}
	
	public void loadDemo(@Name("data") JsonNode dom)
			throws JsonProcessingException, IOException, JSONRPCException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {
		
		
		AgentHost host = getAgentHost();
		GenList agentList = (GenList) host.getAgent("demolist");
		
		
		ArrayNode teams = (ArrayNode) dom.get("teams");
		for (JsonNode team : teams) {
			String teamId = team.get("id").textValue();
			Team teamAgent = null;
			if (!host.hasAgent(teamId)) {
				teamAgent = host.createAgent(Team.class, teamId);
			} else {
				teamAgent = (Team) host.getAgent(teamId);
			}
			agentList.add(teamId);
			
			JsonNode leader = team.get("leader");
			String leaderId = leader.get("id").textValue();
			TeamLeader leaderAgent = null;
			if (!host.hasAgent(leaderId)) {
				leaderAgent = host.createAgent(TeamLeader.class, leaderId);
			} else {
				leaderAgent = (TeamLeader) host.getAgent(leaderId);
			}
			leaderAgent.setTeam(teamAgent.getFirstUrl().toString());
			leaderAgent.setXmppAccount(leader.get("xmppAccount").textValue());
			leaderAgent.setXmppPassword(leader.get("xmppPassword").textValue());
			leaderAgent.setLocation((Location) JOM.getInstance()
					.readerForUpdating(new Location("", ""))
					.readValue(leader.get("location")));
			teamAgent.setLeader(leaderAgent.getFirstUrl().toString());
			agentList.add(leaderId);
			
			ArrayNode members = (ArrayNode) team.get("members");
			for (JsonNode member : members) {
				String memberId = member.get("id").textValue();
				TeamMember memberAgent = null;
				if (!host.hasAgent(memberId)) {
					memberAgent = host.createAgent(TeamMember.class, memberId);
				} else {
					memberAgent = (TeamMember) host.getAgent(memberId);
				}
				memberAgent.setTeam(teamAgent.getFirstUrl().toString());
				memberAgent.setXmppAccount(member.get("xmppAccount")
						.textValue());
				memberAgent.setXmppPassword(member.get("xmppPassword")
						.textValue());
				memberAgent.setLocation((Location) JOM.getInstance()
						.readerForUpdating(new Location("", ""))
						.readValue(member.get("location")));
				teamAgent.addMember(memberAgent.getFirstUrl().toString());
				agentList.add(memberId);
			}
		}
		getState().put("demoData", JOM.getInstance().writeValueAsString(dom));
		
	}
}
