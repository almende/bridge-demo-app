package com.almende.bridge.agent;

import com.almende.bridge.MonitoredTeam;
import com.almende.eve.rpc.annotation.Name;

public class MonitoredTeamAgent extends TeamAgent implements MonitoredTeam {

	public void init(){
		myState	= getState();
	}
	
	@Override
	public Integer getAmount(){
		return (Integer) myState.get("amount");
	}

	@Override
	public Boolean isComplete(){
		return toDo()<= 0;
	}

	@Override
	public Integer toDo() {
		return getAmount()-getAvailable().size();
	}

	@Override
	public void setAmount(@Name("amount") Integer amount) {
		myState.put("amount",amount);
	}
	
}
