package com.almende.bridge.agents_old;

import java.net.URI;
import java.util.UUID;

import org.joda.time.DateTime;

import com.almende.bridge.agent.GenList;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Required;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.State;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class DemoGenerator extends Agent {
	State	myState	= getState();
	
	public void init() {
		myState = getState();
	}
	
	
	static final String[]	agentIds	= new String[] { "Fire Vehicle 1",
			"Fire Vehicle 2", "Fire Vehicle 3", "Fire Personnel 1",
			"Fire Personnel 2", "Fire Personnel 3", "Fire Personnel 4",
			"Fire Personnel 5", "Fire Personnel 6", "Fire Personnel 7",
			"Fire Personnel 8", "Fire Personnel 9", "Fire Personnel 10",
			"Medic Vehicle 1", "Medic Vehicle 2", "Medic Vehicle 3",
			"Medic Personnel 1", "Medic Personnel 2", "Medic Personnel 3",
			"Medic Personnel 4", "Medic Personnel 5", "Medic Personnel 6",
			"Medic Personnel 7", "Medic Personnel 8", "Medic Personnel 9",
			"Medic Personnel 10", "Police Vehicle 1", "Police Vehicle 2",
			"Police Vehicle 3", "Police Personnel 1", "Police Personnel 2",
			"Police Personnel 3", "Police Personnel 4", "Police Personnel 5",
			"Police Personnel 6", "Police Personnel 7", "Police Personnel 8",
			"Police Personnel 9", "Police Personnel 10" };
	
	public void reset(@Required(false) @Name("lat") String lat,@Required(false) @Name("lon") String lon) {
		// Removes all teams, tasks, resources
		// Recreates resources
		myState.remove("invalidCount");
		AgentHost host = getAgentHost();
		
		try {
			GenList teamList = (GenList) host.getAgent("list");
			ArrayNode list = teamList.getList();
			
			for (JsonNode item : list) {
				SimpleTaskAgent task = (SimpleTaskAgent) host.getAgent(item
						.textValue());
				if (task != null) {
					ArrayNode teams = task.getTeams();
					for (JsonNode team : teams) {
						host.deleteAgent(team.asText());
					}
					host.deleteAgent(task.getId());
				}
			}
			teamList.empty();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ArrayNode agent_list = null;
		try {
			GenList resList = (GenList) host.getAgent("agent_list");
			agent_list = resList.getList();
			resList.empty();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		GenList parent = null;
		for (String agentId : agentIds) {
			try {
				host.deleteAgent(agentId);
			} catch (Exception e) {
				System.err.println("Couldn't delete agent:" + agentId);
			}
			try {
				Resource agent = (Resource) host.createAgent(Resource.class,
						agentId);
				if (agent_list != null) {
					String url = agent.getUrls().get(0);
					for (JsonNode item : agent_list) {
						ObjectNode mapping = (ObjectNode) JOM.getInstance()
								.readTree(item.textValue());
						if (mapping.get("url").textValue().equals(url)) {
							System.out.println("Found old id, reusing:"
									+ mapping.get("id").textValue());
							agent.setGuid(mapping.get("id").textValue());
							break;
						}
					}
				}
				if (agent.getGuid() == null) {
					agent.setGuid(UUID.randomUUID().toString());
				}
				ObjectNode location = JOM.createObjectNode();
				location.put("time", DateTime.now().toString());

				if (lat != null && lon != null){
					location.put("lat", new Double(
							Double.parseDouble(lat) + (Math.random() * 0.004 - 0.002)).toString());
					location.put("lon", new Double(
							Double.parseDouble(lon) + (Math.random() * 0.004 - 0.002)).toString());
				} else {
					// 58.918127,5.600992 Stavanger harbor
					location.put("lat", new Double(
							58.918127 + (Math.random() * 0.004 - 0.002)).toString());
					location.put("lon", new Double(
							5.600992 + (Math.random() * 0.004 - 0.002)).toString());					
				}

				agent.setLocation(location);
				
				try {
					String parentId = agentId.substring(0,
							agentId.lastIndexOf(" ")) + " Parent";
					if (parent == null
							|| !parent.getId().equals(parentId)) {
						System.err.println("Need to create new parent:"
								+ parentId);
						if (host.hasAgent(parentId)){
							host.deleteAgent(parentId);
						}
						parent = host.createAgent(GenList.class,parentId);
						parent.add(agentId);
						System.err.println("Parent:"+parent.getFirstUrl());
						myState.put(parentId, parent.getFirstUrl().toString());
					}
				} catch (Exception e) {
					System.err.println("Couldn't store agent in parent:"
							+ agentId + ":" + e.getMessage());
					e.printStackTrace();
				}
				agent.setResType(agentId.substring(0,
						agentId.lastIndexOf(" ")));
				
			} catch (Exception e) {
				System.err.println("Couldn't generate agent:" + agentId + ":"
						+ e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
	public void populateTeam(@Name("teamUrl") String url,
			@Name("resourceType") String resType,
			@Name("amount") Integer amount,
			@Required(false) @Name("location") String location)
			throws Exception {
		AgentHost factory = AgentHost.getInstance();
		URI parentUrl = URI.create(myState.get(resType,String.class));
		GenList parent = factory.createAgentProxy(this, parentUrl, GenList.class);
		int count = 0;
		for (String agentId : parent.getAll()) {
			try {
				Resource agent = (Resource) factory.getAgent(agentId);
				if (agent.getCurrentTask() == null
						|| agent.getCurrentTask().equals("")) {
					count++;
					ObjectNode params = JOM.createObjectNode();
					params.put("resource", agent.getUrls().get(0));
					params.put("type", "potential");
					
					System.out.println(count + ": Example agent:" + agentId
							+ " added to team:" + url + " with url:"
							+ agent.getUrls().get(0));
					send(URI.create(url), "addMember", params);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (count >= amount) break;
		}
	}
	
	public ArrayNode getAllResources() {
		ArrayNode result = JOM.createArrayNode();
		AgentHost factory = AgentHost.getInstance();
		for (String agentId : agentIds) {
			ObjectNode elem = JOM.createObjectNode();
			
			try {
				Agent agent = factory.getAgent(agentId);
				elem.put("url", agent.getUrls().get(0));
				result.add(elem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void incInvalid() {
		Integer invalidCount = 0;
		if (myState.containsKey("invalidCount")) {
			invalidCount = myState.get("invalidCount",Integer.class);
		}
		invalidCount += 1;
		myState.put("invalidCount", invalidCount);
	}
	
	public Integer getInvalid() {
		Integer invalidCount = 0;
		if (myState.containsKey("invalidCount")) {
			invalidCount = myState.get("invalidCount",Integer.class);
		}
		return invalidCount;
	}
	
	@Override
	public String getDescription() {
		return "Agent to generate (and reset) demo";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
