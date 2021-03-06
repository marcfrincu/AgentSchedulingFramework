package runner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import agent.Agent;

import settings.SystemSettings;

import modules.Executor;
import modules.Healing;
import modules.IModule;
import modules.Scheduling;
import modules.Negotiation;

/**
 * Class used for starting modules
 * @author Marc Frincu
 * @since 2010
 *
 */
public class RunModule {

	private static Logger logger = Logger.getLogger(RunModule.class
			.getPackage().getName());

	static String[] globalargs;
	/**
	 * Initializes an agent and starts the desired module
	 * @param args : 
	 * args[0] module type; 
	 * args[1] true|false (PAUSED|READY_TO_RUN);
	 * args[2] parentUUID; 
	 * args[3] IP|URI of this host 
	 * args[4] true|false (started from remote or not)
	 * args[5] true|false (external module or part of platform)
	 * args[6] pathToArchive (HDFS or local)
	 * args[7] scriptToStartModule
	 */
	public static void main(String[] args) {
		
		if (args.length != 8) {
			RunModule.logger.fatal("Invalid number of arguments. Exiting");
			System.exit(0);
		}
		
		RunModule.globalargs = new String[args.length];
		for (int i=0; i<args.length; i++) {
			RunModule.globalargs[i] = args[i];
		}
		
		PropertyConfigurator.configure("logging.properties");
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");
		
		//new Thread(
		//	new Runnable() {
		//		public void run() {
					new RunModule().setupAgent(RunModule.globalargs[0], Boolean.parseBoolean(RunModule.globalargs[1]), RunModule.globalargs[2], RunModule.globalargs[3], Boolean.parseBoolean(RunModule.globalargs[4]), Boolean.parseBoolean(RunModule.globalargs[5]), RunModule.globalargs[6], RunModule.globalargs[7]);
		//		}
		//	}				
		//).start();
	}
	
	/**
	 * Starts an agent with list of modules attached to it. All modules run in a single process.
	 * @param mods the a module type
	 * @param readyn the state corresponding to the module : true|false (PAUSED|READY_TO_RUN)
	 * @param parentUuid the parent UUID
	 * @param ip the IP of the machine the agent runs
	 * @param fromRemote true|false (started from remote, e.g., ssh, or not)
	 */
	public void setupAgent(String mod, boolean ready, String parentUuid, String ip, boolean fromRemote, boolean external, String archive, String startScript) {
		
		// Create the agent
		Agent a = Agent.getAgent(parentUuid.compareToIgnoreCase(IModule.EMPTY_SYMBOL) == 0 ? IModule.EMPTY_SYMBOL : parentUuid);
		
		IModule module = null;
		
		if (mod.compareToIgnoreCase("SCHEDULING") == 0) {
			module = Scheduling.getSchedulingModule();
		}
		else if (mod.compareToIgnoreCase("NEGOTIATION") == 0) {
			module = Negotiation.getNegotiationModule();
		}
		else if (mod.compareToIgnoreCase("HEALING") == 0) {
			module = Healing.getHealingModule();
		}
		else if (mod.compareToIgnoreCase("EXECUTOR") == 0) {
			module = Executor.getExecutorModule();
		}
		else {
			RunModule.logger.fatal("Invalid module type: " + mod + ". Exiting");
			System.exit(0);
		}
		
		// Set the ID of this module
		module.setId(a.getId()); // UUID generated by the agent
		// Set the parent ID of this module
		module.setParentId(parentUuid.compareTo("-1") == 0 ? IModule.EMPTY_SYMBOL : parentUuid);
		// Set the IP/URI of this host
		module.setAddress(ip);
		// Set is paused value
		module.setIsPaused(!ready);
		// Set the archive path
		module.setArchive(archive);
		// Set the start script name
		module.setStartScript(startScript);
		
		// Attach the module to the agent
		a.setModule(mod, module);
		
		// Start the module
		boolean result = (a.startModule(mod, 
										ready,
										fromRemote));
		
		RunModule.logger.info(mod + " module started: " + result);
	}
}
