package com.almende.bridge.agents_old;

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
public class TeamAgent extends Agent implements Team {
	State							myState	= getState();
	static final public String[]	TYPES	= new String[] { "available",
			"potential", "rejects"			};
	ObjectMapper					om		= JOM.getInstance();
	
	public void create() {
		State myState = getState();
		myState.put("available", "[]");
		myState.put("potential", "[]");
		myState.put("rejects", "[]");
	}
	
	public void addMember(@Name("resource") String url,
			@Name("type") String type) throws Exception {
		ArrayNode resources = (ArrayNode) om.readTree((String) myState
				.get(type));
		if (resources != null) {
			ArrayNode newResources = resources.deepCopy();
			newResources.add(url);
			if (!myState.putIfUnchanged(type,
					om.writeValueAsString(newResources),
					om.writeValueAsString(resources))) {
				// recursive retry!
				System.err
						.println("warning: retry put resources, hit race condition....");
				addMember(url, type);
			}
		} else {
			throw new Exception("Unknown type:" + type + " given");
		}
	}
	
	public void delMember(@Name("resource") String url,
			@Name("type") String type) throws Exception {
		ArrayNode resources = (ArrayNode) om.readTree((String) myState
				.get(type));
		if (resources != null) {
			ArrayNode newResources = resources.deepCopy();
			Iterator<JsonNode> iter = newResources.iterator();
			while (iter.hasNext()) {
				JsonNode item = iter.next();
				if (item.textValue().equals(url)) {
					iter.remove();
				}
			}
			if (!myState.putIfUnchanged(type,
					om.writeValueAsString(newResources),
					om.writeValueAsString(resources))) {
				// recursive retry!
				System.err
						.println("warning: retry delMember, hit race condition....");
				delMember(url, type);
			}
		} else {
			throw new Exception("Unknown type:" + type + " given");
		}
	}
	
	@Override
	public void chgMember(@Name("resource") String url,
			@Name("type") String type) throws Exception {
		String oldType = getMemberType(url);
		if (oldType != null) delMember(url, getMemberType(url));
		addMember(url, type);
	}
	
	public String getMemberType(@Name("resource") String url) {
		return getMemberType(url, "none");
	}
	
	@Access(AccessType.UNAVAILABLE)
	private String getMemberType(String url, String skipType) {
		try {
			for (String type : TYPES) {
				if (type.equals(skipType)) continue;
				for (JsonNode elem : (ArrayNode) om.readTree((String) myState
						.get(type))) {
					if (elem.textValue().equals(url)) return type;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Couldn't find old membertype:"+url);
		return null;
	}
	
	public ObjectNode getAllMembers() {
		State myState = getState();
		ObjectNode res = JOM.createObjectNode();
		for (String type : TYPES) {
			try {
				res.put(type, JOM.getInstance().readTree((String)myState.get(type)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	public ArrayNode getter(@Name("type") String type) {
		try {
			return (ArrayNode) om.readTree((String) getState().get(type));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return om.createArrayNode();
	}
	
	public ArrayNode getAvailable() {
		return getter("available");
	}
	
	public ArrayNode getRejects() {
		return getter("rejects");
	}
	
	public ArrayNode getPotentials() {
		return getter("potential");
	}
	
	@Override
	public String getDescription() {
		return "Team storage, keeping track of who is available or not.";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
