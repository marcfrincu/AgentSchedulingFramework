package test;

import modules.Healing;
import agent.Agent;
import settings.SystemSettings;

/**
 * Tests the healing module
 * @author Marc Frincu
 * @since 2010
 */
public class TestHealing {

	public static void main(String args[]) {
		
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("system.properties");
		
		// Create the agent
		Agent a = Agent.getAgent(null);
		
		// Create the Scheduling module
		Healing healing = Healing.getHealingModule();
		// Set ID. This has to be done here
		healing.setId(a.getId());
		
		// Add the module to the agent
		a.setModule("healing", healing);
		
		// Start the module
		boolean result = (a.startModule("healing",false, false) == true) ? true : false;
		System.out.println("Healing module started: " + result);
	}
}
