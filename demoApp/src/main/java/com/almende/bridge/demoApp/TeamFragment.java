package com.almende.bridge.demoApp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.almende.bridge.demoApp.agent.BridgeDemoAgent;
import com.almende.bridge.demoApp.util.SystemUiHider;
import com.almende.eve.agent.AgentHost;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class TeamFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_team, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

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
}
