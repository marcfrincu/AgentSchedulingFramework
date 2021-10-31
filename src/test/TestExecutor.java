package test;

import modules.Executor;
import settings.SystemSettings;
import agent.Agent;

public class TestExecutor {

	public static void main(String[] args) {
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");
		
		// Create the agent
		Agent a = Agent.getAgent(null);
		
		Executor e = Executor.getExecutorModule();
		// Set ID. This has to be done here
		e.setId(a.getId());
		
		a.setModule("executor", e);
		
		// Start the modules
		boolean result = (a.startModule("executor",false, false) == true) ? true : false;
		System.out.println("Executor module started: " + result);
	}
	
}
