package com.almende.bridge.demoApp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.event.StateEvent;
import com.almende.bridge.types.TeamStatus;
import com.almende.eve.agent.AgentHost;

import de.greenrobot.event.EventBus;

public class TeamFragment extends Fragment {
	private View view = null;
    private final String TAG = "TeamFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_team, container, false);
        
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        renderTeam();

        Button button = (Button) view.findViewById(R.id.call_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.err.println("CallButton clicked!");
                AgentHost host = AgentHost.getInstance();
                try {
                    BridgeDemoAgent agent = (BridgeDemoAgent) host.getAgent(EveService.DEMO_AGENT);
                    agent.callRedirect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void renderTeam() {
        
        try {
            BridgeDemoAgent agent = (BridgeDemoAgent) AgentHost.getInstance().getAgent(
                    EveService.DEMO_AGENT);
            TeamStatus team = null;
            if (agent != null) {
                team = agent.getTeamStatus();
            }
            ToggleButton stateButton = (ToggleButton) view.findViewById(R.id.team_state_button);
            if (team == null) {
                TextView tv = (TextView) view.findViewById(R.id.team_id);
                tv.setText(R.string.team_id_text);

                tv = (TextView) view.findViewById(R.id.team_leader);
                tv.setText(R.string.team_leader);

                tv = (TextView) view.findViewById(R.id.team_status);
                tv.setText(R.string.team_status);

                stateButton.setVisibility(View.GONE);
            } else {

                TextView tv = (TextView) view.findViewById(R.id.team_id);
                tv.setText(team.getTeamId());

                tv = (TextView) view.findViewById(R.id.team_leader);
                tv.setText(team.getTeamLeaderName());

                tv = (TextView) view.findViewById(R.id.team_status);
                tv.setText(team.getDeploymentStatus());

                if (team.getDeploymentStatus().equals(TeamStatus.WITHDRAWN)) {
                    stateButton.setChecked(false);
                } else {
                    stateButton.setChecked(true);
                }
                stateButton.setVisibility(Button.VISIBLE);
                stateButton.setEnabled(true);
                stateButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        v.setEnabled(false);
                        BridgeDemoAgent agent;
                        try {
                            agent = (BridgeDemoAgent) AgentHost.getInstance().getAgent(
                                    EveService.DEMO_AGENT);
                            TeamStatus team = agent.getTeamStatus();
                            if (((ToggleButton) v).isChecked()) {
                                team.setDeploymentStatus(TeamStatus.ACTIVE);
                            } else {
                                team.setDeploymentStatus(TeamStatus.WITHDRAWN);
                            }
                            agent.updateTeamStatus(team);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong in renderTeam()!");
            e.printStackTrace();
        }
    }

    public void onEventMainThread(StateEvent event) {
        System.err.println(TAG + " received StateEvent! " + event.getAgentId() + ":"
                + event.getValue());
        if (((event.getValue().equals("teamStatusUpdated") || event.getValue().equals(
                "newTeamStatus")) || (event.getValue().equals("taskUpdated") || event.getValue()
                .equals("newTask"))) && event.getAgentId().equals(EveService.DEMO_AGENT)) {
            renderTeam();
        }
    }
}
