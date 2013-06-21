package com.almende.bridge;

import com.almende.eve.agent.AgentInterface;
import com.almende.eve.rpc.annotation.Name;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Team extends AgentInterface {
	public void addMember(@Name("resource") String url,@Name("type") String type) throws Exception;	
	public void delMember(@Name("resource") String url,@Name("type") String type) throws Exception;	
	public void chgMember(@Name("resource") String url,@Name("type") String type) throws Exception;	
	public String getMemberType(@Name("resource") String url);
	public ObjectNode getAllMembers();	
	public ArrayNode getAvailable();
	public ArrayNode getRejects();
	public ArrayNode getPotentials();
}
