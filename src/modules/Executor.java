package modules;

import modules.communication.CommunicationAMQPExecutor;
import modules.communication.Message;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.AgentService;
import services.ResourceService;
import services.TaskService;
import services.TraceService;

/**
 * Module used for executing tasks on resource
 * @author Marc Frincu
 * @since 2010
 *
 */
 public class Executor extends BasicModule {

	private static Logger logger = Logger.getLogger(Executor.class
			.getPackage().getName());
	
	private ResourceService rs = null;
	private TaskService ts = null;
	
	private static Executor executor = null;
	
	private Executor() {
		this.as = new AgentService();
		this.ts = new TaskService();
		this.trs = new TraceService();
		this.rs = new ResourceService();
		Executor.logger.setLevel(Level.DEBUG);
		this.type = MODULE_TYPE.EXECUTOR;
	}
	
	/**
	 * Each module is built as a singleton.
	 * @return a reference to the <i>Executor</i> object
	 */
	public static Executor getExecutorModule() {
		if (Executor.executor == null) {
			Executor.executor = new Executor();
			return Executor.executor;
		}
		else
			return Executor.executor;
	}
	


	@Override
	public void bindCommunicator() {
		this.com = new CommunicationAMQPExecutor(this.uuid);		
	}


	@Override
	public void postMessageProcessingOperations() {
		try {
			if (this.status == MODULE_STATUS.RUNNING) {
				String resourceId = this.rs.getResourceIdByURI(this.address);
				String agentId = this.as.getAgentIDByUuid(this.uuid);
				String[] parts = this.ts.getFirstTaskInResourceQueue(agentId, resourceId).split("#");
				
				if (parts[0].compareToIgnoreCase("-1") != 0) {
					Executor.logger.info("Executing task: " + parts[0] + 
							" on resource: " + resourceId + " (Queue position:" + parts[1] + ")");
					
					this.ts.executeTask(parts[0]);
					//TODO: actually execute some tasks on resources
				}
				else {
					Executor.logger.error("Could not find any tasks belonging to agent: " + this.uuid +
							" on resource:" + resourceId);
				}
			}
		}
		catch (Exception e) {
			Executor.logger.fatal("Error handling runtime module operation: " + e.getMessage());
			System.exit(0);
		}
	}

	@Override
	public void preMessageProcessingOperations() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processModuleDependentMessages(Message msg) {
		return;
		
	}

	@Override
	public void sleepFor() {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean sendMessage(String fromId, String toId, String content,
			String processingName) {
		// TODO Auto-generated method stub
		return false;
	}
}
