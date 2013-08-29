package com.almende.bridge.agents_old;

import java.net.URI;
import java.util.Iterator;

import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.State;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class Resource extends Agent {
	private State	myState	= getState();
	
	public void init() {
		myState = getState();
	}
	
	public ArrayNode getBridgeTasks() {
		if (myState.containsKey("tasks")) {
			try {
				return (ArrayNode) JOM.getInstance().readTree((String)myState.get("tasks"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JOM.createArrayNode();
	}
	
	public void addBridgeTask(@Name("taskUrl") String url) {
		ArrayNode tasks = getBridgeTasks();
		ArrayNode oldTasks = null;
		ObjectMapper om = JOM.getInstance();
		if (tasks == null) {
			tasks = JOM.createArrayNode();
		} else {
			oldTasks = tasks.deepCopy();
		}
		tasks.add(url);
		
		try {
			if (!myState.putIfUnchanged("tasks", om.writeValueAsString(tasks),
					om.writeValueAsString(oldTasks))) {
				System.err
						.println("Have to recursively retry adding a Bridge Task");
				addBridgeTask(url); // recursive retry
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void delBridgeTask(@Name("taskUrl") String url) {
		ArrayNode tasks = getBridgeTasks();
		ArrayNode oldTasks = tasks.deepCopy();
		ObjectMapper om = JOM.getInstance();
		Iterator<JsonNode> iter = tasks.iterator();
		while (iter.hasNext()) {
			String task = iter.next().textValue();
			if (task.equals(url)) {
				iter.remove();
			}
		}
		try {
			if (!myState.putIfUnchanged("tasks", om.writeValueAsString(tasks),
					om.writeValueAsString(oldTasks))) {
				System.err
						.println("Have to recursively retry updating a Bridge Task");
				delBridgeTask(url); // recursive retry
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getDeploymentStatus() {
		// @formatter:off
		/*
		 * o atBase-Available
		 * o enroute-Unavailable
		 * o on-scene-Unavailable
		 * o returning-Unavailable
		 * o Resource Maintenance
		 * o Out of Service
		 * o Depleted
		 * o Available
		 * o Committed
		 * o In Transit
		 * o At incident (ROSS)
		 * o Assigned
		 * o In Camp
		 * o Reassignment
		 * o Return Transit
		 * o Returned
		 * o Demobilized
		 */
		// @formatter:on
		if (myState.containsKey("currentTask")) {
			return "Assigned";
		}
		return "atBase-Available";
	}
	
	public void setLocation(@Name("location") ObjectNode location) {
		/*
		 * ObjectNode location = JOM.createObjectNode();
		 * location.put("time",DateTime.now().toString());
		 * location.put("lat","52.1"); location.put("lon","5.6");
		 */
		try {
			myState.put("location",
					JOM.getInstance().writeValueAsString(location));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ObjectNode getLocation() {
		if (myState.containsKey("location")) {
			try {
				return (ObjectNode) JOM.getInstance().readTree(
						(String) myState.get("location"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public ObjectNode getGoal() {
		if (myState.containsKey("currentTask")) {
			try {
				String url = getCurrentTask();
				return send(URI.create(url), "getLocation", null, JOM.getSimpleType(ObjectNode.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public String getTaskDescription() {
		if (myState.containsKey("currentTask")) {
			try {
				String url = getCurrentTask();
				if (!url.isEmpty()) return send(URI.create(url), "getTaskDescription", null, JOM.getSimpleType(String.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void stopCurrentTask() {
		myState.remove("currentTask");
	}
	
	public void currentTask(@Name("taskUrl") String url) {
		ObjectNode params = JOM.createObjectNode();
		params.put("resourceType", getResType());
		try {
			String teamUrl = send(URI.create(url),"getTeam",params,JOM.getSimpleType(String.class));
			params = JOM.createObjectNode();
			params.put("resource", getUrls().get(0));
			params.put("type", "available");
			send(URI.create(teamUrl),"chgMember",params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		myState.put("currentTask", url);
	}
	
	public String getCurrentTask() {
		return (String) myState.get("currentTask");
	}
	
	public ObjectNode requestStatus() {
		ObjectNode status = JOM.createObjectNode();
		status.put("name", getId());
		status.put("id", getGuid());
		status.put("type", getResType());
		String deploymentStatus = getDeploymentStatus();
		if (deploymentStatus != null) {
			status.put("deploymentStatus", deploymentStatus);
		}
		ObjectNode location = getLocation();
		if (location != null) status.put("current", location);
		
		ObjectNode goal = getGoal();
		if (goal != null) status.put("goal", goal);
		String taskDescription = getTaskDescription();
		if (taskDescription != null) status.put("task", taskDescription);
		return status;
	}
	
	public void setGuid(@Name("guid") String guid) {
		getState().put("guid", guid);
		ObjectNode params = JOM.createObjectNode();
		ObjectNode item = JOM.createObjectNode();
		item.put("url", getUrls().get(0));
		item.put("id", getGuid());
		
		try {
			params.put("val", JOM.getInstance().writeValueAsString(item));
			send(URI.create("local://agent_list"),"add",params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getGuid() {
		return (String) getState().get("guid");
	}
	
	public void setResType(@Name("resType") String resType) {
		getState().put("resType", resType);
	}
	
	public String getResType() {
		return (String) getState().get("resType");
	}
	
	@Override
	public String getDescription() {
		return "Resource:" + getId() + " / " + getGuid();
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
