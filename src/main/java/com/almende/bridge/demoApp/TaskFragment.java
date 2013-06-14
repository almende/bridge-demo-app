package com.almende.bridge.demoApp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.types.Task;
import com.almende.bridge.demoApp.util.BusProvider;
import com.almende.bridge.demoApp.util.SystemUiHider;
import com.almende.eve.agent.AgentHost;
import com.squareup.otto.Subscribe;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class TaskFragment extends Fragment {
	private View	view	= null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_task, container, false);
		
		BusProvider.getBus().register(this);
		
		renderTask();
		return view;
	}
	
	public void renderTask() {
		try {
			BridgeDemoAgent agent = (BridgeDemoAgent) AgentHost.getInstance()
					.getAgent(EveService.DEMO_AGENT);
			Task task = agent.getTask();
			
			TextView tv = (TextView) view.findViewById(R.id.task_text);
			tv.setText(task.getText());
			
			tv = (TextView) view.findViewById(R.id.task_assigner);
			tv.setText(task.getAssigner());
			
			tv = (TextView) view.findViewById(R.id.task_assignment_date);
			tv.setText(task.getAssignmentDate());
			
			tv = (TextView) view.findViewById(R.id.task_status);
			tv.setText(task.getStatus());
			
		} catch (Exception e) {
			System.err.println("Couldn't update task activity.");
			e.printStackTrace();
		}
	}
	
	@Subscribe
	public void onEvent(StateEvent event) {
		System.err.println("TaskFragment received StateEvent! "+event.getAgentId()+":"+event.getValue());
		if (event.getValue().equals("taskUpdated") && event.getAgentId().equals(EveService.DEMO_AGENT)) {
			renderTask();
		}
	}
}
