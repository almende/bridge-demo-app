package com.almende.bridge.agents_old;

import com.almende.eve.rpc.annotation.Name;

public interface MonitoredTeam extends Team {
	public void setAmount(@Name("amount") Integer amount);
	public Boolean isComplete();
	public Integer getAmount();
	public Integer toDo();
}
