package test;

import modules.Negotiation;
import settings.SystemSettings;
import agent.Agent;

/**
 * Tests the negotiating module
 * @author Marc Frincu
 * @since 2010
 *
 */
public class TestNegotiator {

	public static void main(String args[]) {
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");

		// Create the agent
		Agent a = Agent.getAgent(null);
		
		// Create the Negotiation module
		Negotiation neg = Negotiation.getNegotiationModule();
		// Set ID. This has to be done here
		neg.setId(a.getId());
		
		// Add the module to the agent
		a.setModule("negotiation", neg);
		
		// Start the modules
		boolean result = (a.startModule("negotiation",false, false) == true) ? true : false;
		System.out.println("Negotiation module started: " + result);
		
	}
}
