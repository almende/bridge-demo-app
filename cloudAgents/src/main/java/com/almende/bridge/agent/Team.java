package com.almende.bridge.agent;

import java.util.ArrayList;

import com.almende.bridge.types.Task;
import com.almende.bridge.types.TeamStatus;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.annotation.ThreadSafe;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;

@ThreadSafe(true)
@Access(AccessType.PUBLIC)
public class Team extends Agent {
	
	public Task getTask() {
		if (getState().containsKey("Task")){
			return getState().get("Task",Task.class);
		}
		return null;
	}
	
	public void setTask(@Name("task") Task task) {
		getState().put("Task", task);
	}
	
	public TeamStatus getTeamStatus(){
		TeamStatus result = new TeamStatus();
		//TODO: fill in status
		
		return result;
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
