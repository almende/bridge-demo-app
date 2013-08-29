package com.almende.bridge.agents_old;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Required;
import com.fasterxml.jackson.databind.JsonNode;

@Access(AccessType.PUBLIC)
public class RateMeasurement extends Agent {
	static int count=0;
	static DateTime start=DateTime.now();
	
	public void notify(@Name("data") String data,
			@Required(false) @Name("producerId") String producer,
			@Required(false) @Name("itemId") String itemId,
			@Name("meta") JsonNode meta) throws Exception {
		count++;
	} 
	
	public Long rate(){
		long result = count/(new Interval(start,DateTime.now()).toDurationMillis()/1000);
		
		start = DateTime.now();
		count=0;
		return result;
	}
	
	@Override
	public String getDescription() {
		return "Rate gauge";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
