package com.almende.bridge.agent;

import java.util.ArrayList;
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

@Access(AccessType.PUBLIC)
public class GenList extends Agent {
	State	myState	= getState();
	
	public void init(){
		myState	= getState();
	}
	
	public void empty(){
		myState.remove("items");
	}
	
	public void del(@Name("val") String value){
		ArrayNode list = getList();
		ArrayNode oldList = null;
		ObjectMapper om = JOM.getInstance();
		if (list == null) {
			list = JOM.createArrayNode();
		} else {
			oldList = list.deepCopy();
		}
		Iterator<JsonNode> iter = list.iterator();
		while(iter.hasNext()){
			JsonNode item = iter.next();
			if (item.textValue().equals(value)){
				iter.remove();
				break;
			}
		}
		
		try {
			if (!myState.putIfUnchanged("items", om.writeValueAsString(list),
					om.writeValueAsString(oldList))) {
				System.err
						.println("Have to recursively retry deleting an item");
				del(value); // recursive retry
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void add(@Name("val") String value){
		ArrayNode list = getList();
		ArrayNode oldList = null;
		ObjectMapper om = JOM.getInstance();
		if (list == null) {
			list = JOM.createArrayNode();
		} else {
			oldList = list.deepCopy();
		}
		list.add(value);
		
		try {
			if (!myState.putIfUnchanged("items", om.writeValueAsString(list),
					om.writeValueAsString(oldList))) {
				System.err
						.println("Have to recursively retry adding an item");
				add(value); // recursive retry
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String[] getAll(){
		ArrayNode list = getList();
		ArrayList<String> result = new ArrayList<String>(list.size());
		Iterator<JsonNode> iter = list.iterator();
		while (iter.hasNext()){
			result.add(iter.next().textValue());
		}
		String[] ret = new String[result.size()];
		return result.toArray(ret);
	}
	public ArrayNode getList(){
		if (myState.containsKey("items")) {
			try {
				return (ArrayNode) JOM.getInstance().readTree((String)myState.get("items"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JOM.createArrayNode();
	}
	
	@Override
	public String getDescription() {
		return "Simple agent keeping track of a list of items, allowing cleanup operations";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
