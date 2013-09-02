package com.almende.bridge.agent;

import java.net.ProtocolException;
import java.net.URI;
import java.util.ArrayList;

import com.almende.bridge.types.Location;
import com.almende.bridge.types.Task;
import com.almende.bridge.types.TeamStatus;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.annotation.ThreadSafe;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;

@ThreadSafe(true)
@Access(AccessType.PUBLIC)
public class Team extends Agent {
	
	public Task getTask() {
		if (getState().containsKey("Task")){
			return getState().get("Task",Task.class);
		}
		return null;
	}
	
	public void setTask(@Name("task") Task task) throws ProtocolException, JSONRPCException {
		getState().put("Task", task);
		send(URI.create(getLeader()),"triggerTask");
		for (String member : getMembers()){
			send(URI.create(member),"triggerTask");	
		}
	}
	
	public void setTeamStatus(@Name("status") TeamStatus status) throws ProtocolException, JSONRPCException{
		getState().put("Status", status);
		send(URI.create(getLeader()),"triggerTeamStatus");
		for (String member : getMembers()){
			send(URI.create(member),"triggerTeamStatus");
		}
	}
	public TeamStatus getTeamStatus() throws ProtocolException, JSONRPCException{
		if (getState().containsKey("Status")){
			return getState().get("Status",TeamStatus.class);	
		}
		TeamStatus newStatus = new TeamStatus();
		newStatus.setTeamId(getId());
		newStatus.setDeploymentStatus(TeamStatus.UNASSIGNED);
		
		if (!getLeader().isEmpty()){
			newStatus.setTeamLeaderName(send(URI.create(getLeader()),"getId",null,String.class));
		}
		
		return newStatus;
	}
	
	public Location getLocation() throws ProtocolException, JSONRPCException{
		return send(URI.create(getLeader()),"getLocation",null,Location.class);
	}
	
	public String getLeader() {
		if (getState().containsKey("Leader")){
			return getState().get("Leader",String.class);	
		}
		return "";
	}
	
	public void setLeader(@Name("leader") String leader) {
		getState().put("Leader",leader);
	}
	
	public ArrayList<String> getMembers() {
		ArrayList<String> result = new ArrayList<String>(0);
		if (getState().containsKey("Members")){
			result = getState().get(result,"Members");
		}
		return result; 
	}
	
	public void setMembers(@Name("members") ArrayList<String> members) {
		getState().put("Members", members);
	}
	
	public void addMember(@Name("member") String member) {
		ArrayList<String> members = new ArrayList<String>(0);
		if (getState().containsKey("Members")){
			members = getState().get(members,"members");
		}
		if (members == null){
			members = new ArrayList<String>();
		}
		members.add(member);
		getState().put("Members", members);
	}
}
