package modules;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.AgentService;
import services.TraceService;
import settings.SystemSettings;
import modules.communication.ICommunicationAMQP;
import modules.communication.Message;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.AgentModule;

/**
 * Class implementing the functionality of a basic module.
 * Actual modules need to extend it in order to properly function
 * @author Marc Frincu
 * @since 2013
 */
public abstract class BasicModule implements IModule {

	protected static Logger logger = Logger.getLogger(BasicModule.class
			.getPackage().getName());	
	
	protected AgentService as = null;
	protected TraceService trs = null;
	
	protected MODULE_TYPE type;
	protected MODULE_STATUS status = MODULE_STATUS.PAUSED;

	protected String parentUuid = BasicModule.EMPTY_SYMBOL;

	protected boolean isPaused, isExternal;
	protected String archive, startScript, address;
	
	protected ICommunicationAMQP com = null;
	protected String uuid = null;

	protected boolean paused = false;

	
	@Override
	public void setArchive(String archive) {
		this.archive = archive;		
	}

	@Override
	public void setIsExternal(boolean external) {
		this.isExternal = external;
	}

	@Override
	public void setIsPaused(boolean paused) {
		this.isPaused = paused;
	}

	@Override
	public void setStartScript(String scriptName) {
		this.startScript = scriptName;
	}
	
	@Override
	public String getArchive() {
		return this.archive;
	}

	@Override
	public boolean getIsExternal() {
		return this.isExternal;
	}

	@Override
	public boolean getIsPaused() {
		return this.isPaused;
	}

	@Override
	public String getStartScript() {
		return this.startScript;
	}
	
	@Override
	public MODULE_TYPE getModuleType () {
		return this.type;
	}
	
	/**
	 * Retrieves the ID of the workflow associated with this module
	 * return the workflow ID
	 */
	@Override
	public String getWorkflowUuid() {
		return BasicModule.EMPTY_SYMBOL;
	}
	
	@Override
	public void setParentId(String uuid) {
		this.parentUuid = uuid;
	}
	
	
	/**
	 * Sets the ID of this module. This is usually identical with the agent's ID
	 * @param id the ID
	 */
	@Override
	public void setId (String id) {
		this.uuid = id;
		this.bindCommunicator();
	}
	
	@Override
	public String getId() {
		return this.uuid;
	}
	
	public void setAddress (String address) {
		this.address = address;
	}	
	
	@Override
	public final void run(boolean paused, boolean fromRemote) throws Exception {
		BasicModule.logger.setLevel(Level.DEBUG);
		int i = 0;
		Message msg = null;
		AgentModule am = null;
		Message msgRsp = null;
		boolean foundSimilar = false;
		long lastTimeCheckedSimilar = System.currentTimeMillis();
		long crtTime;

		// if the module should be started
		if (!paused) {
			// change the module status to running
			this.as.setModuleStatus(this.uuid, 
											BasicModule.EMPTY_SYMBOL, 
											this.type.toString(), 
											MODULE_STATUS.READY_FOR_RUNNING.toString());
			this.status = MODULE_STATUS.READY_FOR_RUNNING;
		}
		
		if (fromRemote) {
			// add trace
			this.trs.addTrace(this.uuid, 
					this.getWorkflowUuid(), 
					this.type.toString(), 
					"end deploy module", 
					System.currentTimeMillis());
		}
		
		long tic = 0;
		while (true) {
			tic = System.currentTimeMillis();
			
			// if module status was set to NOT_RESPONSING during execution it means
			// the module has failed to ping due to overload
			if (this.status == MODULE_STATUS.NOT_RESPONDING) {
				this.status = MODULE_STATUS.PAUSED;
				this.as.setModuleStatus(this.uuid, 
						this.getWorkflowUuid(), 
						this.type.toString(), 
						MODULE_STATUS.PAUSED.toString());
			}
			
			// if paused do not start module
			if (this.status == MODULE_STATUS.PAUSED) {
				this.sleepFor();				
				continue;
			}

			if (this.status == MODULE_STATUS.READY_FOR_RUNNING) {
				msgRsp = new Message(this.uuid,
						BasicModule.BROADCAST_SYMBOL,
						JSONHandler.makeAgentModule(this.uuid, 
								this.parentUuid,
								this.getWorkflowUuid(),
								this.type.toString(),
								this.status.toString(),
								System.currentTimeMillis()),
						Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REQUEST.toString(),
						Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REQUEST
				);
				this.com.sendMessage(msgRsp, 
						ICommunicationAMQP.EXCHANGE_SIMILAR_REQUEST_NAME);
				foundSimilar = false;
				lastTimeCheckedSimilar = System.currentTimeMillis();
				// add trace
				this.trs.addTrace(this.uuid, 
						this.getWorkflowUuid(), 
						this.type.toString(), 
						"start similar check", 
						System.currentTimeMillis());
				this.as.setModuleStatus(this.uuid, 
						this.getWorkflowUuid(), 
						this.type.toString(), 
						MODULE_STATUS.WAITING_TO_RUN.toString());
				this.status = MODULE_STATUS.WAITING_TO_RUN;
			}

			// update last ping time
			this.as.pingModule(this.uuid, 
					BasicModule.EMPTY_SYMBOL, 
					this.type.toString()
				);
			
			crtTime = System.currentTimeMillis();
			
			if (this.status == MODULE_STATUS.WAITING_TO_RUN && 
				!foundSimilar && 
				crtTime - lastTimeCheckedSimilar > 
				SystemSettings.getSystemSettings().getSimilar_search_timeout()) {
				BasicModule.logger.info("Search for similar agents has reached time limit." +
						" No similar agent found. Starting agent " +
						this.uuid + "/" + this.getWorkflowUuid() + "/" +
						this.type);
				this.as.setModuleStatus(this.uuid, 
						this.getWorkflowUuid(), 
						this.type.toString(), 
						MODULE_STATUS.RUNNING.toString());
				this.status = MODULE_STATUS.RUNNING;
				// add trace
				this.trs.addTrace(this.uuid, 
						this.getWorkflowUuid(), 
						this.type.toString(), 
						"end similar check", 
						System.currentTimeMillis());				
			}
						
			this.preMessageProcessingOperations();
						
			this.com.onMessage();
			synchronized (ICommunicationAMQP.msgs) {
				msg = null;
				i=0;
				BasicModule.logger.debug("Message vector size: " + 
						ICommunicationAMQP.msgs.size());
				while (i < ICommunicationAMQP.msgs.size()) {
					msg = ICommunicationAMQP.msgs.remove(i);
					
					BasicModule.logger.info("Agent (" + this.uuid
							+ ") processing message: "
							+ msg.getProcessingName() + " FROM "
							+ msg.getFromId() + " TO " + msg.getToId()
							+ " CONTENT " + msg.getContent() +
							" Message.TYPE " + msg.getType().toString());
					
					switch (msg.getType()) {
						case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
							am = JSONHandler.getAgentModule(msg.getContent());
							if (am == null) {
								BasicModule.logger.error("Invalid JSON for AgentModule");
								break;
							}
			
							if (this.uuid.compareTo(am.getUuid()) == 0 &&
									am.getModuleType().compareTo(this.type.toString()) == 0 &&
									am.getWorkflowID().compareTo(this.getWorkflowUuid()) == 0)
								continue;
												
							if (am.getAgentParentId().compareTo(this.uuid) == 0 
									||
								(
									am.getAgentParentId().compareTo(this.parentUuid) == 0 
									&&
									am.getUuid().compareTo(this.uuid) != 0
									&& 
									am.getWorkflowID().compareTo(this.getWorkflowUuid()) != 0
									&&
									am.getModuleType().compareTo(this.type.toString()) == 0
								)
									||
								am.getUuid().compareTo(this.parentUuid) == 0
								) {
								msgRsp = new Message(this.uuid,
										BasicModule.BROADCAST_SYMBOL,
										JSONHandler.makeAgentModule(this.uuid, 
												this.parentUuid,
												this.getWorkflowUuid(),
												this.type.toString(),
												this.status.toString(),
												System.currentTimeMillis()),
										Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY.toString(),
										Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY
								);
								
								this.com.sendMessage(msgRsp, 
										ICommunicationAMQP.EXCHANGE_SIMILAR_RESPONSE_NAME
											);
								break;
							}
						case AGENT_MODULE_CHECK_SIMILAR_REPLY:
							am = JSONHandler.getAgentModule(msg.getContent());
							if (am == null) {
								BasicModule.logger.error("Invalid JSON for AgentModule");
								continue;
							}
							
							if (am.getStatus().compareTo(MODULE_STATUS.RUNNING.toString()) != 0) {
								break;
							}
							
							if ((am.getAgentParentId().compareTo(this.uuid) == 0 &&
									am.getModuleType().compareTo(this.type.toString()) == 0)
									||
								(
									am.getAgentParentId().compareTo(this.parentUuid) == 0 
									&&
									am.getUuid().compareTo(this.uuid) != 0
									&& 
									am.getWorkflowID().compareTo(this.getWorkflowUuid()) != 0
									&&
									am.getModuleType().compareTo(this.type.toString()) == 0
								)
									||
								(am.getUuid().compareTo(this.parentUuid) == 0 && 
										am.getModuleType().compareTo(this.type.toString()) == 0)
								)  {
								this.as.setModuleStatus(this.uuid, 
										this.getWorkflowUuid(), 
										this.type.toString(), 
										MODULE_STATUS.PAUSED.toString());
								this.status = MODULE_STATUS.PAUSED;
								msgRsp = new Message(this.uuid,
										BasicModule.BROADCAST_SYMBOL,
										JSONHandler.makeAgentModule(this.uuid, 
												this.parentUuid,
												this.getWorkflowUuid(),
												this.type.toString(),
												MODULE_STATUS.PAUSED.toString(),
												System.currentTimeMillis()),
										Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY.toString(),
										Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY
								);
								
								this.com.sendMessage(msgRsp, 
										ICommunicationAMQP.EXCHANGE_SIMILAR_RESPONSE_NAME
											);
								foundSimilar = true;
								// add trace
								this.trs.addTrace(this.uuid, 
										this.getWorkflowUuid(), 
										this.type.toString(), 
										"end similar check", 
										System.currentTimeMillis());

							}
							break;					
						case AGENT_MODULE_ACTIVATION_REQUEST :
							am = JSONHandler.getAgentModule(msg.getContent());
							if (am == null) {
								BasicModule.logger.error("Invalid JSON for AgentModule");
								continue;
							}
							
							if (msg.getProcessingName().compareTo( Message.TYPE.AGENT_MODULE_PAUSE_REQUEST_NO_REPLY.toString()) == 0) {
								if (this.uuid.compareTo(am.getUuid()) == 0 &&
										am.getModuleType().compareTo(this.type.toString()) == 0 &&
										am.getWorkflowID().compareTo(this.getWorkflowUuid()) == 0) {
									//add trace
									this.trs.addTrace(this.uuid, 
											this.getWorkflowUuid(), 
											this.type.toString(), 
											"(Wrongly) stopping running module", 
											System.currentTimeMillis());
									this.as.setModuleStatus(
											am.getUuid(),
											am.getWorkflowID(), 
											am.getModuleType(), 
											MODULE_STATUS.NOT_RESPONDING.toString());
									this.status = MODULE_STATUS.NOT_RESPONDING;									
								}
							}	
							
							// if this is the module intended for activation
							if (this.uuid.compareTo(am.getUuid()) == 0 &&
									am.getModuleType().compareTo(this.type.toString()) == 0 &&
									am.getWorkflowID().compareTo(this.getWorkflowUuid()) == 0 &&
									am.getStatus().compareTo(MODULE_STATUS.READY_FOR_RUNNING.toString()) == 0) {
								this.as.setModuleStatus(
										am.getUuid(),
										am.getWorkflowID(), 
										am.getModuleType(), 
										am.getStatus());
								BasicModule.logger.info("Module belonging to agent: " +
										am.getUuid() +"/" + am.getWorkflowID() + "/" +
										am.getModuleType() +
										" marked for run");
								this.status = MODULE_STATUS.READY_FOR_RUNNING;
								// add trace
								this.trs.addTrace(this.uuid, 
										this.getWorkflowUuid(), 
										this.type.toString(), 
										"end idle module wake-up", 
										System.currentTimeMillis());
							}
							break;
							default:
								try {
									this.processModuleDependentMessages(msg);
								}
								catch (Exception e) {
									BasicModule.logger.fatal("Error handling module specific messages: " + e.getMessage());
									System.exit(0);
								}
								break;
					}
				}
			}
			
			if (this.status == MODULE_STATUS.RUNNING) {
				this.ping();
				this.postMessageProcessingOperations();
			}
			
			this.sleepFor();
			
			this.trs.addTrace(this.uuid,  
					this.getWorkflowUuid(),
                    this.type.toString(),
                    "Iteration time: " + (System.currentTimeMillis() - tic),
					System.currentTimeMillis());
		}
	
	}
	
	@Override
	public void ping() {
		this.com.sendMessage(new Message(this.uuid,
				BasicModule.BROADCAST_SYMBOL,
				JSONHandler.makeAgentModule(
						this.uuid,
						this.parentUuid, 
						this.getWorkflowUuid(),
						this.type.toString(),
						this.status.toString(), 
						System.currentTimeMillis()),
				Message.TYPE.AGENT_MODULE_REGISTRATION_REQUEST.toString(),
				Message.TYPE.AGENT_MODULE_REGISTRATION_REQUEST), 
				ICommunicationAMQP.EXCHANGE_REGISTRATION_NAME);
	}
	
	/**
	 * Set the sleep time. It usually varies from one module to another
	 */
	public abstract void sleepFor();
	
	/**
	 * Bind the module to the module specific <i>ICommunicationAMQP</i> implementation
	 */
	public abstract void bindCommunicator(); 
	
	
	/**
	 * Implements a switch case for processing module specific messages
	 */
	public abstract void processModuleDependentMessages(Message msg);
	
	/**
	 * Does some operations prior to the message processing phase
	 */
	public abstract void preMessageProcessingOperations();
	
	/**
	 * Does some operations after the message processing phase
	 */	
	public abstract void postMessageProcessingOperations();
	
}
