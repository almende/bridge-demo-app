package com.almende.bridge.agent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

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
	
	private static final String	XMPPDOMAIN	= "openid.almende.org";
	private static final String	MANAGEMENT	= "http://openid.almende.org:8080/cape_mgmt/agents/management/";
	
	public ArrayNode getAllResources() throws JSONRPCException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, IOException {
		AgentHost host = getAgentHost();
		ArrayNode result = JOM.createArrayNode();
		GenList agentList = (GenList) host.getAgent("demolist");
		ArrayNode list = agentList.getList();
		
		for (JsonNode item : list) {
			try {
				Agent agent = host.getAgent(item.textValue());
				if (!agent.getType().equals("Team")) {
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
	
	public void pokeAll() throws JSONRPCException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException{
		ArrayNode resources= getAllResources();
		for (JsonNode resource : resources){
			try {
				send(URI.create(resource.get("url").textValue()),"pokePhone",JOM.createObjectNode());
			} catch (Exception e){
				e.printStackTrace();
			}
		}
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
	
	public void resetDemo() throws Exception {
		String demoData = getState().get("demoData", String.class);
		if (demoData != null) {
			clearDemo();
			loadDemo(JOM.getInstance().readTree(demoData));
		} else {
			throw new JSONRPCException(
					"DemoData was not set, please load new data through loadDemo()!");
		}
	}
	
	public JsonNode getDemoData() throws JsonProcessingException, IOException {
		String demoData = getState().get("demoData", String.class);
		if (demoData != null) {
			return JOM.getInstance().readTree(demoData);
		}
		return null;
	}
	
	public void loadDemo(@Name("data") JsonNode dom) throws Exception {
		
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
			
			ObjectNode leader = (ObjectNode) team.get("leader");
			
			TeamMember leaderAgent = setupAgent(TeamLeader.class, leader, teamAgent);
			teamAgent.setLeader(leaderAgent.getFirstUrl().toString());
			leaderAgent.xmppConnect();
			leaderAgent.pokePhone();
			
			ArrayNode members = (ArrayNode) team.get("members");
			for (JsonNode member : members) {
				TeamMember memberAgent = setupAgent(TeamMember.class, (ObjectNode) member, teamAgent);
				teamAgent.addMember(memberAgent.getFirstUrl().toString());
				memberAgent.xmppConnect();
				memberAgent.pokePhone();
			}
		}
		getState().put("demoData", JOM.getInstance().writeValueAsString(dom));
		
	}
	
	private TeamMember setupAgent(Class<? extends TeamMember> type, ObjectNode member,Team teamAgent) throws NoSuchAlgorithmException, JSONRPCException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ClassNotFoundException{
		MessageDigest md = MessageDigest.getInstance("MD5");
		AgentHost host = getAgentHost();
		GenList agentList = (GenList) host.getAgent("demolist");
		
		String memberId = member.get("id").textValue();
		TeamMember memberAgent = null;
		if (!host.hasAgent(memberId)) {
			memberAgent = host.createAgent(type, memberId);
		} else {
			memberAgent = (TeamMember) host.getAgent(memberId);
		}
		memberAgent.setTeam(teamAgent.getFirstUrl().toString());
		memberAgent.setResType(member.get("type").textValue());
		memberAgent.setXmppAccount(member.get("xmppAccount")
				.textValue());
		memberAgent.setXmppPassword(member.get("xmppPassword")
				.textValue());
		memberAgent.setLocation((Location) JOM.getInstance()
				.readerForUpdating(new Location("", ""))
				.readValue(member.get("location")));
		if (member.has("guid")) {
			memberAgent.setGuid(member.get("guid").textValue());
		} else {
			String guid = UUID.randomUUID().toString();
			memberAgent.setGuid(guid);
			((ObjectNode) member).put("guid", guid);
		}
		
		agentList.add(memberId);
		
		try {
			ObjectNode params = JOM.createObjectNode();
			params.put("username", memberAgent.getXmppAccount());
			params.put("domain", XMPPDOMAIN);
			md.reset();
			md.update(memberAgent.getXmppPassword().getBytes("UTF-8"));
			byte[] digest = md.digest();
			
			String md5Password = Base64.encodeBase64String(digest);
			
			System.err.println("registering password:"+memberAgent.getXmppPassword()+":"+md5Password);
			params.put(
					"password", "{MD5}"+md5Password);
			params.put("givenname", memberAgent.getId());
			params.put("surname", memberAgent.getGuid());
			send(URI.create(MANAGEMENT), "registerAgent", params);
		} catch (Exception e) {
			System.err
					.println("Warning: agent already registered for XMPP?"
							+ memberAgent.getId());
		}
		return memberAgent;
	}
	
}
