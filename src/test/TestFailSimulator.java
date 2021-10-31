package test;

import settings.SystemSettings;
import simulator.Failures;

/**
 * Class used for testing the functionality of the fail simulator
 * @author Marc Frincu
 * @since 2010
 *
 */
public class TestFailSimulator {

	public static void main(String args[]) throws Exception {

		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");

		Failures.failModules(100);
		//Failures.failHealers(10);
	}
}
