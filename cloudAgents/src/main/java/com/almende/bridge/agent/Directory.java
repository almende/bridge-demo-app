package com.almende.bridge.agent;

import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;

@Access(AccessType.PUBLIC)
public class Directory extends Agent {
	
	public void set(@Name("key") String key, @Name("value") String value) {
		getState().put(key, value);
	}
	
	public String get(@Name("key") String key) {
		return getState().get(key, String.class);
	}
}
