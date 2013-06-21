package com.almende.bridge.agent;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.State;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SimpleTaskAgent extends Agent {
	State						myState	= getState();
	static final Set<String>	TYPES	= new HashSet<String>();
	AgentHost					factory	= AgentHost.getInstance();
	
	public void init() {
		myState = getState();
	}
	
	public SimpleTaskAgent() {
		if (TYPES.size() == 0) {
			TYPES.addAll(Arrays.asList(new String[] { "Fire Vehicle",
					"Fire Personnel", "Medic Vehicle", "Medic Personnel",
					"Police Vehicle", "Police Personnel" }));
		}
	}
	
	public void create() {
		ObjectNode params = JOM.createObjectNode();
		params.put("val", getId());
		try {
			send(URI.create("local://list"), "add", params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void prepare(@Name("data") ObjectNode data) throws Exception {
		// @formatter:off
		/*
		 * {
		 * 		"messageID":<string>,
		 * 		"IncidentDescription":<string>,
		 * 		"resources":[
		 * 			{
		 * 				"resourceType":<string>,
		 * 				"resourceID":<string>,
		 * 				"amountString":<string>,
		 * 				"anticipatedFunction":<string>,
		 * 				"assignmentInstructions":<string>,
		 * 				"scheduleType":<string>,
		 * 				"scheduleDate":<string>,
		 * 				"scheduleLocation":<string>,
		 * 			}
		 * 		]
		 * }
		 */
		// @formatter:on
		myState.put("data", JOM.getInstance().writeValueAsString(data));
		
		ArrayNode resources = (ArrayNode) data.get("resources");
		if (resources.size() > 0) {
			if (data.has("incidentDescription")) {
				myState.put("incidentDescription",
						data.get("incidentDescription").textValue());
			}
			myState.put("taskDescription",
					resources.get(0).get("anticipatedFunction").textValue());
			myState.put("taskLocation", resources.get(0)
					.get("scheduleLocation").textValue());
			myState.put("taskDueDate", resources.get(0).get("scheduleDate")
					.textValue());
			
			for (JsonNode res : resources) {
				ObjectNode resource = (ObjectNode) res;
				if (resource.has("resourceID")
						&& !resource.get("resourceID").textValue().equals("")) {
					String resID = resource.get("resourceID").textValue();
					// TODO, find specific agent
					GenList resList = (GenList) factory.getAgent("agent_list");
					String url = null;
					for (JsonNode item : resList.getList()) {
						ObjectNode record = (ObjectNode) JOM.getInstance()
								.readTree(item.asText());
						if (record.get("id").textValue().equals(resID)) {
							url = record.get("url").textValue();
							break;
						}
					}
					if (url != null) {
						try {
							// Add myself to Resource's tasklist:
							ObjectNode params = JOM.createObjectNode();
							params.put("taskUrl", "local://" + getId());
							send(URI.create(url), "addBridgeTask", params);
							send(URI.create(url), "currentTask", params);
						} catch (Exception e1) {
							
							e1.printStackTrace();
						}
					} else {
						System.err
								.println("Couln't find requested agent!, delete myself"
										+ resID);
						ObjectNode params = JOM.createObjectNode();
						params.put("val", getId());
						send(URI.create("local://list"), "del", params);
						send(URI.create("local://demo"), "incInvalid",
								JOM.createObjectNode());
						
						AgentHost.getInstance().deleteAgent(getId());
					}
				} else {
					// resource type, prepare team and force acceptance
					String resType = resource.get("resourceType").textValue();
					if (!TYPES.contains(resType)) {
						System.err
								.println("Warning: Unknown resource type given:'"
										+ resType + "' known:" + TYPES);
					}
					if (resource.get("amountString").asInt() > 0) {
						MonitoredTeamAgent agent = (MonitoredTeamAgent) getAgentFactory()
								.createAgent(MonitoredTeamAgent.class,
										"team_" + resType + "_" + getId());
						agent.setAmount(resource.get("amountString").asInt());
						ObjectNode params = JOM.createObjectNode();
						// params.put("amount", agent.getAmount() * 2);
						params.put("amount", agent.getAmount());
						params.put("resourceType", resType);
						params.put("teamUrl", "local://" + agent.getId());
						send(URI.create("local://demo"), "populateTeam", params);
						
						// Add myself to Resource's tasklist:
						params = JOM.createObjectNode();
						params.put("taskUrl", "local://" + getId());
						
						ArrayNode potentials = agent.getPotentials();
						for (JsonNode pot : potentials) {
							send(URI.create(pot.textValue()), "addBridgeTask",
									params);
						}
						
						myState.put("team_"
								+ resource.get("resourceType").textValue(),
								agent.getId());
						
					}
				}
			}
		} else {
			System.err
					.println("Warning: incorrect task received, doesn't contain any resources!");
		}
		forceAcceptance();
	}
	
	public String getTeam(@Name("resourceType") String type) {
		return "local://" + ((String) myState.get("team_" + type));
	}
	
	public ArrayNode getTeams() {
		ArrayNode result = JOM.createArrayNode();
		for (String type : TYPES) {
			if (myState.containsKey("team_" + type)) {
				result.add((String) myState.get("team_" + type));
			}
		}
		return result;
	}
	
	public ObjectNode getLocation() {
		ObjectNode location = JOM.createObjectNode();
		String[] loc = ((String) myState.get("taskLocation")).split(" ");
		location.put("lat", loc[0]);
		location.put("lon", loc[1]);
		location.put("time", (String) myState.get("taskDueDate"));
		return location;
	}
	
	public void forceAcceptance() {
		AgentHost factory = AgentHost.getInstance();
		try {
			for (JsonNode team : getTeams()) {
				String agentId = team.textValue();
				MonitoredTeamAgent agent = (MonitoredTeamAgent) factory
						.getAgent(agentId);
				ObjectNode params = JOM.createObjectNode();
				params.put("taskUrl", "local://" + getId());
				if (agent.toDo() > 0) {
					ArrayNode potentials = agent.getPotentials();
					if (potentials.size() > 0) {
						String url = potentials.get(
								(int) Math.random() * potentials.size())
								.textValue();
						send(URI.create(url), "currentTask", params);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		getScheduler().createTask(new JSONRequest("forceAcceptance", null),
				1000);
	}
	
	public String getTaskDescription() {
		return (String) myState.get("taskDescription");
	}
	
	@Override
	public String getDescription() {
		return "Bridge task:" + myState.get("data").toString();
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
