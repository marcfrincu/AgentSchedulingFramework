package agent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

import modules.IModule;

import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import services.AgentService;
import services.utils.DbAgent;
import settings.SystemSettings;

/**
 * Class representing an agent
 * @author Marc Frincu
 * @since 2010
 */

/*
 *  HowTo: erase RabbitMQ queues
 *  sudo /etc/init.d/rabbitmq-server stop
 *  sudo find /var/lib/rabbitmq/ -mindepth 1 -delete
 *  sudo /etc/init.d/rabbitmq-server start
 */
public class Agent {

	//TODO: get info on the load of the system by checking the executor. It can 
	// provide info on the average load
	// send message from scheduler to executor and wait for answer. If message received
	// adjust ect accordingly, otherwise if timed out skip 
	
	private Hashtable<String, IModule> modules = null;
	private String uuid = null;
	private String parentUuid = null;
	
	DbAgent db = null;

	private static Agent agent = null;
	
	private static Logger logger = Logger.getLogger(Agent.class
			.getPackage().getName());

	/**
	 * Retrieves the ID of the agent
	 * @return the agent ID
	 */
	public String getId() {
		return this.uuid;
	}
	
	/**
	 * returns a reference to the given module
	 * @param name the module name
	 * @return the reference to the module or null if the module does not exist
	 */
	public IModule getModule(String name) {
		return this.modules.get(name);
	}

	/**
	 * Default constructor
	 * @throws Exception
	 */
	private Agent(String parentUuid) throws Exception {
		this.parentUuid = parentUuid;
		if (!this.checkToken()) {
			//this.uuid = UUID.randomUUID().toString();
			//this.registerAgent();		
			Agent.logger.fatal("Could not create agent");
			throw new Exception("Invalid agent token. Please register");
		}
		this.modules = new Hashtable<String, IModule>();
		
		//PropertyConfigurator.configure("settings/logging.properties");
	}
	
	/**
	 * Agents are singletons
	 * @param parentUuid parent agent UUID, or null in case there is no parent
	 * @return returns the <i>Agent</i> reference
	 */
	public static Agent getAgent(String parentUuid) {
		if (Agent.agent == null || 
				(Agent.agent != null && 
						parentUuid != null && 
						Agent.agent.parentUuid.compareTo(parentUuid) != 0
				) ||
				(Agent.agent != null && 
						parentUuid == null && 
						Agent.agent.parentUuid != null
				)
			) {
			try {
				Agent.agent = new Agent(parentUuid);
			} catch (Exception e) {
				Agent.logger.fatal("Error when creating a new agent: " + 
						e.getMessage());
				System.exit(0);
			}
		}
		return Agent.agent;
	}
	
	/**
	 * Adds a new module to this agent. In case the module already exists
	 * it will be over written and a warning will be issued
	 * @param name the name of the new module
	 * @param module the module
	 */
	public void setModule(String name, IModule module) {
		if (this.modules.put(name, module) != null) {
			Agent.logger.warn("Module: " + name + " already exists." +
					" Module overwritten.");
		}
		
		// insert agent module in DB
		final AgentService as = new AgentService();
		try {
			if (!Boolean.getBoolean(as.addModule(this.uuid, 
					module.getWorkflowUuid(),
					module.getModuleType().toString(),
					Boolean.valueOf(module.getIsPaused()).toString(), 
					Boolean.valueOf(module.getIsExternal()).toString(), 
					module.getArchive(),
					module.getStartScript()))) {
				Agent.logger.error("Error inserting module. Module could already exist.");
			}
		} catch (Exception e) {
			Agent.logger.fatal("Could not insert module in DB." +
					" Message: " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Starts a given module
	 * @param name the name of the module to be started
	 * @param paused true if the module is in sleep mode, false otherwise
	 * @param fromRemote true if the module has been started from a remote host, false otherwise
	 * @return true if the module has been successfully started, 
	 * false otherwise
	 */
	public boolean startModule(String name, boolean paused, boolean fromRemote) {
		IModule mod = this.modules.get(name);
		if (mod == null) {
			return false;
		}
		else {
			try {
				Agent.logger.info("Starting module " + name);
				mod.run(paused, fromRemote);
			} catch (Exception e) {
				Agent.logger.fatal("Error executing agent: " + this.uuid + 
						". Message: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
	
	/**
	 * Grabs the agent ID from a token and verifies whether 
	 * the agent is registered or not
	 * @return true if the agent token has been verified, 
	 * false otherwise
	 */
	private boolean checkToken() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(
									SystemSettings.getSystemSettings().getAgentToken())
									);
			String line = br.readLine().trim();
			if (line.length() > 0) {
				this.db = DbAgent.getDb();
				System.out.println(line);
				if (this.db.checkAgentIsRegistred(line)) {
					this.uuid = line;
					return true;
				}
				else {
					Agent.logger.fatal("Agent is not registered. " +
							"Please register or verify token");
					return false;
				}
				
			}
		} catch (FileNotFoundException e) {
			Agent.logger.info("Could not find token file. Generating default token");
			return false;
		} catch (IOException e) {
			Agent.logger.warn("Error reading the token file. Generating default " +
					"token");
			return false;
		} catch (Exception e) {
			Agent.logger.warn("Error connecting to the agent DB. " +
					"Could not verify agent token against already existing ones");
			return false;
		}
		return false;
	}
	
	/**
	 * Registers a new agent
	 * @return true if the registration has been successful, false otherwise
	 */
	private boolean registerAgent() {	
		try {
			FileOutputStream fo = new FileOutputStream(
										SystemSettings.getSystemSettings().getAgentToken(), false
														);
			PrintStream file = new PrintStream(fo);
			file.println(this.uuid);
			file.close();
			fo.close();
			
			DbAgent db = DbAgent.getDb();
			int parentId = db.getAgentIdByUuid(this.parentUuid);
			db.addAndRegisterAgent(this.uuid, 
									parentId, 
									2, 
									"n/a", 
									true);
			
		}
		catch(PSQLException psqle) {
			Agent.logger.fatal("Could not register agent. Message: " + 
					psqle.getMessage());
			return false;
		}
		catch(Exception ioe) {
			Agent.logger.fatal("Could not register agent. Message: " + 
					ioe.getMessage());
			return false;
		}
		
		Agent.logger.info("Agent registered. Token UUID: " + this.uuid);
		return true;
	}
}
