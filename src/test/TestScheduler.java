/*package test;

import modules.Scheduling;
import settings.SystemSettings;
import agent.Agent;

*//**
 * Tests the scheduling module
 * @author Marc Frincu
 * @since 2010
 *//*
public class TestScheduler {
	
	public static void main(String args[]) throws Exception {
		
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");

		// Create the agent
		Agent a = Agent.getAgent(null);
		
		// Create the Scheduling module
		Scheduling sched = Scheduling.getSchedulingModule();
		// Set ID. This has to be done here
		sched.setId(a.getId());
		
		// Add the module to the agent
		a.setModule("scheduling", sched);
		
		// Start the module
		boolean result = (a.startModule("scheduling", false, false) == true) ? true : false;
		System.out.println("Scheduling module started: " + result);
	}
}
*/