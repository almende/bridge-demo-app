package com.almende.bridge.demoApp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.demoApp.util.SystemUiHider;
import com.almende.bridge.types.Task;
import com.almende.eve.agent.AgentHost;

import de.greenrobot.event.EventBus;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class TaskFragment extends Fragment {
    private View view = null;
    private Task task = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_task, container, false);

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        renderTask();
        return view;
    }

    public void renderTask() {
        try {
            BridgeDemoAgent agent = (BridgeDemoAgent) AgentHost.getInstance().getAgent(
                    EveService.DEMO_AGENT);
            if (agent != null) {
                task = agent.getTask();
            }

            if (task == null || task.getStatus().equals(Task.COMPLETE)) {
                TextView tv = (TextView) view.findViewById(R.id.task_remoteTitle);
                tv.setText(R.string.task_remoteTitle);

                tv = (TextView) view.findViewById(R.id.task_text);
                tv.setText(R.string.task_text);
                
                tv = (TextView) view.findViewById(R.id.task_assigner);
                tv.setText(R.string.task_assigner);

                tv = (TextView) view.findViewById(R.id.task_assignment_date);
                tv.setText(R.string.task_assignment_date);

                tv = (TextView) view.findViewById(R.id.task_status);
                tv.setText(R.string.task_status);

                Button btn = (Button) view.findViewById(R.id.ackButton);
                btn.setVisibility(Button.GONE);
                btn = (Button) view.findViewById(R.id.completeButton);
                btn.setVisibility(Button.GONE);
            } else {

                TextView tv = (TextView) view.findViewById(R.id.task_remoteTitle);
                tv.setText(task.getTitle());

                tv = (TextView) view.findViewById(R.id.task_text);
                tv.setText(task.getText());
                
                tv = (TextView) view.findViewById(R.id.task_assigner);
                tv.setText(task.getAssigner());

                tv = (TextView) view.findViewById(R.id.task_assignment_date);
                tv.setText(task.getAssignmentDate());

                tv = (TextView) view.findViewById(R.id.task_status);
                tv.setText(task.getStatus());

                if (task.getStatus().equals(Task.NOTCONFIRMED)) {
                    Button btn = (Button) view.findViewById(R.id.ackButton);
                    btn.setVisibility(Button.VISIBLE);

                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            BridgeDemoAgent agent;
                            try {
                                agent = (BridgeDemoAgent) AgentHost.getInstance().getAgent(
                                        EveService.DEMO_AGENT);
                                task.setStatus(Task.CONFIRMED);
                                agent.updateTaskStatus(task);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    btn = (Button) view.findViewById(R.id.completeButton);
                    btn.setVisibility(Button.GONE);
                } else {
                    Button btn = (Button) view.findViewById(R.id.ackButton);
                    btn.setVisibility(Button.GONE);

                    btn = (Button) view.findViewById(R.id.completeButton);
                    btn.setVisibility(Button.VISIBLE);

                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            BridgeDemoAgent agent;
                            try {
                                agent = (BridgeDemoAgent) AgentHost.getInstance().getAgent(
                                        EveService.DEMO_AGENT);
                                task.setStatus(Task.COMPLETE);
                                agent.updateTaskStatus(task);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Couldn't update task activity.");
            e.printStackTrace();
        }
    }

    public void onEventMainThread(StateEvent event) {
        System.err.println("TaskFragment received StateEvent! " + event.getAgentId() + ":"
                + event.getValue());
        if ((event.getValue().equals("taskUpdated") || event.getValue().equals("newTask"))
                && event.getAgentId().equals(EveService.DEMO_AGENT)) {
            renderTask();
        }
    }
}
