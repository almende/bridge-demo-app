package com.almende.bridge.agent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.almende.bridge.types.Location;
import com.almende.bridge.types.PointOfInterest;
import com.almende.bridge.types.SitRep;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Access(AccessType.PUBLIC)
public class SitRepAgent extends Agent {
	
	private HashMap<String,PointOfInterest> getTeams(String exeptTeamUrl) throws JSONRPCException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException{
		AgentHost host = getAgentHost();
		HashMap<String,PointOfInterest> result = new HashMap<String,PointOfInterest>();
		GenList agentList = (GenList) host.getAgent("demolist");
		ArrayNode list = agentList.getList();
		
		for (JsonNode item : list) {
			try {
				Agent agent = host.getAgent(item.textValue());
				if (agent.getType().equals("Team") && !agent.getUrls().contains(exeptTeamUrl)){
					Team team = (Team)agent;
					PointOfInterest poi = new PointOfInterest();
					Location loc = team.getLocation();
					poi.setLat(loc.getLatitude());
					poi.setLon(loc.getLongitude());
					poi.setLabel(team.getId());
					
					result.put(team.getId(),poi);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public SitRep getSitRep(@Name("team") String teamUrl) throws JSONRPCException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException{
		SitRep sitRep = new SitRep();
		sitRep.setTeams(getTeams(teamUrl));
		
		return sitRep;
	}
}
