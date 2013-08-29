package com.almende.bridge.agents_old;

import com.almende.bridge.types.SitRep;
import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;

@Access(AccessType.PUBLIC)
public class SitRepAgent extends Agent {
	
	public SitRep getSitRep(){
		return new SitRep();
	}
}
