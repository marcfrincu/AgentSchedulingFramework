package modules;

import java.net.SocketException;
import java.util.Vector;

import modules.communication.CommunicationAMQPHealing;
import modules.communication.Message;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.AgentModule;
import modules.communication.json.beans.ModuleItem;
import network.MulticastClient;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.AgentService;
import services.TraceService;
import settings.SystemSettings;
import utils.install.Deploy;
import utils.remote.Executor;

/**
 * Module that handles the self-healing of the agent platform. More precisely
 * this implies the management of the modules NOTE: In order to make the module
 * relocation work public certificates need to be placed on all resources
 * (.ssh/authorized_hosts) together with the private keys.
 * 
 * @author Marc Frincu
 * @since 2010
 */
public class Healing extends BasicModule {

	private static Logger logger = Logger.getLogger(Healing.class.getPackage()
			.getName());

	private static Healing healing = null;
	Vector<AgentModule> agentModules = null;
	private MulticastClient mcClient = null;
	Executor executor = null;
	boolean platformNotFullyOperational = false;

	private Healing() {
		this.agentModules = new Vector<AgentModule>();
		this.as = new AgentService();
		this.trs = new TraceService();

		Healing.logger.setLevel(Level.DEBUG);
		this.type = MODULE_TYPE.HEALING;

		this.executor = new Executor();
	}

	/**
	 * Each module is built as a singleton.
	 * 
	 * @return a reference to the <i>Healing</i> object
	 */
	public static Healing getHealingModule() {
		if (Healing.healing == null) {
			Healing.healing = new Healing();
			return Healing.healing;
		} else
			return Healing.healing;
	}

	@Override
	public boolean sendMessage(String fromId, String toId, String content,
			String processingName) {

		return false;
	}

	@Override
	public void ping() {
		/*
		 * this.com.sendMessage(new Message(this.uuid, Healing.BROADCAST_SYMBOL,
		 * JSONHandler.makeAgentModule( this.uuid, Healing.EMPTY_SYMBOL,
		 * Healing.EMPTY_SYMBOL, MODULE_TYPE.HEALING.toString(),
		 * this.status.toString(), System.currentTimeMillis()),
		 * Message.TYPE.AGENT_MODULE_REGISTRATION_REQUEST.toString(),
		 * Message.TYPE.AGENT_MODULE_REGISTRATION_REQUEST),
		 * CommunicationAMQPHealing.EXCHANGE_REGISTRATION_NAME);
		 */
		this.pingHealers();
	}

	/**
	 * Ping all healing agents. This is used for healing agents to know of each
	 * other's existence
	 */
	protected void pingHealers() {
		this.com.sendMessage(new Message(this.uuid, Healing.BROADCAST_SYMBOL,
				JSONHandler.makeAgentModule(this.uuid, this.parentUuid, this
						.getWorkflowUuid(), MODULE_TYPE.HEALING.toString(),
						this.status.toString(), System.currentTimeMillis()),
				Message.TYPE.AGENT_MODULE_HEALING_REGISTRATION_REQUEST
						.toString(),
				Message.TYPE.AGENT_MODULE_HEALING_REGISTRATION_REQUEST),
				CommunicationAMQPHealing.EXCHANGE_REGISTRATION_HEALER_NAME);
	}

	@Override
	public void bindCommunicator() {
		this.com = new CommunicationAMQPHealing(this.uuid);
	}

	@Override
	public void postMessageProcessingOperations() {
		try {
			// for all modules that have registered to this healing agent
			// check last ping time
			int j = 0, k = 0;
			AgentModule agentModule = null, agentModule2 = null;
			Healing.logger.debug(this.agentModules.size() + " modules "
					+ "registered to healing agent " + this.uuid);

			boolean failedModule = false;

			while (j < this.agentModules.size()) {
				agentModule = this.agentModules.get(j);

				Healing.logger.debug(System.currentTimeMillis()
						+ " "
						+ agentModule.getLastPing()
						+ " "
						+ SystemSettings.getSystemSettings()
								.getHealing_ping_timeout());

				// if the module has stopped working
				if ((System.currentTimeMillis() - agentModule.getLastPing() > SystemSettings
						.getSystemSettings().getHealing_ping_timeout())
						|| ((agentModule.getLastPing() > System
								.currentTimeMillis()) && (Math.abs(-agentModule
								.getLastPing()
								+ System.currentTimeMillis() + 3 * 60 * 1000) > SystemSettings
								.getSystemSettings().getHealing_ping_timeout()))) {
					// pause it
					this.as.setModuleStatus(agentModule.getUuid(), agentModule
							.getWorkflowID(), agentModule.getModuleType(),
							MODULE_STATUS.NOT_RESPONDING.toString());

					failedModule = true;
					if (!this.platformNotFullyOperational) {
						// add trace
						this.trs.addTrace(this.uuid, this.getWorkflowUuid(),
								this.type.toString(),
								"platform not fully operational", System
										.currentTimeMillis());

						this.platformNotFullyOperational = true;
					}

					Healing.logger.info("Module " + agentModule.getUuid() + "/"
							+ agentModule.getWorkflowID() + "/"
							+ agentModule.getModuleType() + " not responding");

					// select another similar agent to take its place
					k = 0;
					boolean found = false;
					// if our failed module is not an EXECUTOR then we can
					// search for idle ones.
					// we assume that executors do not have idle siblings since
					// they should run
					// on the same node
					if (agentModule.getModuleType().compareTo(
							MODULE_TYPE.EXECUTOR.toString()) != 0
							&& agentModule.getStatus().compareTo(
									MODULE_STATUS.PAUSED.toString()) != 0) {
						while (k < this.agentModules.size() && !found) {
							if (k != j) {
								agentModule2 = this.agentModules.get(k);
								if (agentModule2.getStatus().compareTo(
										MODULE_STATUS.PAUSED.toString()) == 0) {
									if (agentModule2.getModuleType().compareTo(
											agentModule.getModuleType()) == 0) {
										if (agentModule2.getAgentParentId()
												.compareTo(
														agentModule.getUuid()) == 0
												|| agentModule
														.getAgentParentId()
														.compareTo(
																agentModule2
																		.getUuid()) == 0
												|| (agentModule2
														.getAgentParentId()
														.compareTo(
																agentModule
																		.getAgentParentId()) == 0 && agentModule
														.getAgentParentId()
														.compareTo(
																Healing.EMPTY_SYMBOL) != 0)) {
											if (System.currentTimeMillis()
													- agentModule.getLastPing() > SystemSettings
													.getSystemSettings()
													.getHealing_ping_timeout()) {
												// pause the non responding
												// agent. This is a security
												// measure
												// so that we don't end up with
												// several identical modules
												// running
												this.com
														.sendMessage(
																new Message(
																		this.uuid,
																		Healing.BROADCAST_SYMBOL,
																		JSONHandler
																				.makeAgentModule(
																						agentModule
																								.getUuid(),
																						agentModule
																								.getAgentParentId(),
																						agentModule
																								.getWorkflowID(),
																						agentModule
																								.getModuleType(),
																						MODULE_STATUS.NOT_RESPONDING
																								.toString(),
																						agentModule
																								.getLastPing()),
																		Message.TYPE.AGENT_MODULE_PAUSE_REQUEST_NO_REPLY
																				.toString(),
																		Message.TYPE.AGENT_MODULE_PAUSE_REQUEST_NO_REPLY),
																CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME);

												this.trs
														.addTrace(
																this.uuid,
																this
																		.getWorkflowUuid(),
																this.type
																		.toString(),
																"start idle module wake-up",
																System
																		.currentTimeMillis());
												// activate replacement module
												this.com
														.sendMessage(
																new Message(
																		this.uuid,
																		Healing.BROADCAST_SYMBOL,
																		JSONHandler
																				.makeAgentModule(
																						agentModule2
																								.getUuid(),
																						agentModule2
																								.getAgentParentId(),
																						agentModule2
																								.getWorkflowID(),
																						agentModule2
																								.getModuleType(),
																						MODULE_STATUS.READY_FOR_RUNNING
																								.toString(),
																						agentModule2
																								.getLastPing()),
																		Message.TYPE.AGENT_MODULE_ACTIVATION_REQUEST
																				.toString(),
																		Message.TYPE.AGENT_MODULE_ACTIVATION_REQUEST),
																CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME);
												found = true;
												// remove non responding module
												// only if
												// new one has been found
												this.as.removeModuleFromHealer(
														this.uuid,
														am.getUuid(),
														am.getWorkflowID(),
														am.getModuleType());
												this.agentModules.remove(j);
											}
										}
									}
								}
							}
							k++;
						}
					}

					if (agentModule.getStatus().compareTo(
							MODULE_STATUS.PAUSED.toString()) == 0) {
						this.as.removeModuleFromHealer(this.uuid, am.getUuid(),
								am.getWorkflowID(), am.getModuleType());
						this.agentModules.remove(j);
						continue;
					}

					// send multicast to discover available resources to deploy
					// the new modules
					// TODO: quick hack to skip re-deployment of agent healing
					// modules.
					// This needs to be fixed in the future
					if (!found
							&& agentModule.getModuleType().compareTo(
									MODULE_TYPE.HEALING.toString()) != 0
							&& agentModule.getStatus().compareTo(
									MODULE_STATUS.PAUSED.toString()) != 0) {
						Healing.logger
								.info("Could not find clones for module: "
										+ agentModule.getUuid()
										+ "/"
										+ agentModule.getWorkflowID()
										+ "/"
										+ agentModule.getModuleType()
										+ ". Trying to multicast for searching available resources");
						// send ping message
						try {
							this.mcClient = new MulticastClient();

							this.mcClient.sendPing();
							// start thread for waiting pong message
							this.mcClient.start();

							// sleep a little in order to give the resources a
							// chance to pong us
							Thread.sleep(2000);

							// stop and kill thread
							this.mcClient.interrupt();
							this.mcClient.join();

							Healing.logger.debug("Received "
									+ this.mcClient.getMessages().size()
									+ " replies after multicast call");

							if (this.mcClient.getMessages().size() > 0) {
								// pause the non responding agent. This is a
								// security measure
								// so that we don't end up with several
								// identical modules running
								this.com
										.sendMessage(
												new Message(
														this.uuid,
														Healing.BROADCAST_SYMBOL,
														JSONHandler
																.makeAgentModule(
																		agentModule
																				.getUuid(),
																		agentModule
																				.getAgentParentId(),
																		agentModule
																				.getWorkflowID(),
																		agentModule
																				.getModuleType(),
																		MODULE_STATUS.NOT_RESPONDING
																				.toString(),
																		agentModule
																				.getLastPing()),
														Message.TYPE.AGENT_MODULE_PAUSE_REQUEST_NO_REPLY
																.toString(),
														Message.TYPE.AGENT_MODULE_PAUSE_REQUEST_NO_REPLY),
												CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME);

								ModuleItem mod = new ModuleItem();
								mod.setAgent_uuid(am.getUuid());
								mod
										.setType(new String[] { am
												.getModuleType() });
								mod.setArchive(this.as.getModuleArchive(am
										.getUuid(), am.getWorkflowID(), am
										.getModuleType()));
								mod.setExternal(this.as.getModuleIsExternal(am
										.getUuid(), am.getWorkflowID(), am
										.getModuleType()));
								mod.setStart_script(this.as
										.getModuleStartScript(am.getUuid(), am
												.getWorkflowID(), am
												.getModuleType()));
								mod.setPaused(new String[] { "FALSE" });

								// if the failed module is an EXECUTOR then we
								// need to restart it
								// on the same machine as the one where it
								// failed
								if (agentModule.getModuleType().compareTo(
										MODULE_TYPE.EXECUTOR.toString()) == 0) {
									// TODO think if we can put the executor in
									// the same category as others
									// i.e., start it anywhere possible not on
									// the same machine
									String failedResource = agentModule
											.getWorkflowID();
									for (MulticastClient.Message message : this.mcClient
											.getMessages()) {
										if (message.getIp().compareTo(
												failedResource) == 0) {
											// add trace
											this.trs
													.addTrace(
															this.uuid,
															this
																	.getWorkflowUuid(),
															this.type
																	.toString(),
															"start deploy module "
																	+ agentModule
																			.getModuleType(),
															System
																	.currentTimeMillis());

											this.trs.addTrace(this.uuid, this
													.getWorkflowUuid(),
													this.type.toString(),
													"start executing deployer",
													System.currentTimeMillis());

											mod.setIp(message.getIp());
											executor.runCommands(Deploy
													.generateCommands(mod));

											this.trs.addTrace(this.uuid, this
													.getWorkflowUuid(),
													this.type.toString(),
													"end executing deployer",
													System.currentTimeMillis());

											// remove non responding module only
											// if
											// new one has been found
											this.as.removeModuleFromHealer(
													this.uuid, am.getUuid(), am
															.getWorkflowID(),
													am.getModuleType());
											this.agentModules.remove(j);
											break;
										}
									}
								} else { // non healing module => start it on
											// the first resource available
									for (MulticastClient.Message message : this.mcClient
											.getMessages()) {
										Healing.logger
												.info("Trying to start module on: "
														+ message.getIp());
										this.trs
												.addTrace(
														this.uuid,
														this.getWorkflowUuid(),
														this.type.toString(),
														"start deploy module "
																+ agentModule
																		.getModuleType(),
														System
																.currentTimeMillis());

										this.trs.addTrace(this.uuid, this
												.getWorkflowUuid(), this.type
												.toString(),
												"start executing task", System
														.currentTimeMillis());

										mod.setIp(message.getIp());
										executor.runCommands(Deploy
												.generateCommands(mod));

										this.trs.addTrace(this.uuid, this
												.getWorkflowUuid(), this.type
												.toString(),
												"end executing task", System
														.currentTimeMillis());

										// remove non responding module only if
										// new one has been found
										this.as.removeModuleFromHealer(
												this.uuid, am.getUuid(), am
														.getWorkflowID(), am
														.getModuleType());
										this.agentModules.remove(j);
										break;
									}
								}
							}

							if (this.mcClient.getMessages().size() == 0) {
								j++;
							}

							// close multicast socket connection
							// this.mcClient.close();

						} catch (SocketException se) {
							Healing.logger.error(se.getMessage());
							continue;
						}
					}
				} else {
					j++;
				}
			}

			if (platformNotFullyOperational && !failedModule) {
				// add trace
				this.trs.addTrace(this.uuid, this.getWorkflowUuid(), this.type
						.toString(), "platform fully operational", System
						.currentTimeMillis());
				platformNotFullyOperational = false;
			}
		} catch (Exception e) {
			Healing.logger.fatal("Error handling module(s) recovery: "
					+ e.getMessage());
			System.exit(0);
		}
	}

	@Override
	public void preMessageProcessingOperations() {
		return;
	}

	AgentModule am = null;

	@Override
	public void processModuleDependentMessages(Message msg) {
		try {
			boolean foundModule = false;
			switch (msg.getType()) {
				case AGENT_MODULE_REGISTRATION_REQUEST:
					foundModule = false;
	
					am = JSONHandler.getAgentModule(msg.getContent());
					if (am == null) {
						Healing.logger.error("Invalid JSON for AgentModule");
						break;
					}
					
					if (am.getUuid().compareTo(this.uuid) == 0
							&& am.getWorkflowID().compareTo(this.getWorkflowUuid()) == 0
							&& am.getModuleType().compareTo(this.type.toString()) == 0) {
						Healing.logger.debug("Registration to itself. Skipping");
						break;
					}
	
					for (AgentModule amod : this.agentModules) {
						Healing.logger.debug(amod.getUuid() + "/" + am.getUuid()
								+ " " + amod.getWorkflowID() + "/"
								+ am.getWorkflowID() + " " + amod.getStatus() + "/"
								+ am.getStatus());
						if (amod.getUuid().compareTo(am.getUuid()) == 0
								&& amod.getWorkflowID().compareTo(
										am.getWorkflowID()) == 0
								&& amod.getModuleType().compareTo(
										am.getModuleType()) == 0) {
							if (amod.getLastPing() < am.getLastPing()) {
								amod.setLastPing(am.getLastPing());
								amod.setStatus(am.getStatus());
							}
							amod.setLastPing(System.currentTimeMillis());
							foundModule = true;
						}
					}
					if (!foundModule) {
						this.agentModules.add(am);
					}
					break;
				case AGENT_MODULE_HEALING_REGISTRATION_REQUEST:
	
					am = JSONHandler.getAgentModule(msg.getContent());
					if (am == null) {
						Healing.logger.error("Invalid JSON for AgentModule");
						break;
					}
	
					if (am.getUuid().compareTo(this.uuid) == 0
							&& am.getWorkflowID().compareTo(this.getWorkflowUuid()) == 0
							&& am.getModuleType().compareTo(this.type.toString()) == 0) {
						Healing.logger
								.debug("Registration (healer) to itself. Skipping");
						break;
					}
	
					for (AgentModule amod : this.agentModules) {
						if (amod.getUuid().compareTo(am.getUuid()) == 0
								&& amod.getWorkflowID().compareTo(
										am.getWorkflowID()) == 0
								&& amod.getModuleType().compareTo(
										am.getModuleType()) == 0) {
							amod.setLastPing(am.getLastPing());
							amod.setStatus(am.getStatus());
							foundModule = true;
						}
					}
					if (!foundModule) {
						Healing.logger
								.debug("Adding new healing module for monitoring");
						this.as.addModuleToHealer(this.uuid, am.getUuid(), am
								.getWorkflowID(), am.getModuleType());
						this.agentModules.add(am);
					}
					break;
			}
		} catch (Exception e) {
			Healing.logger.fatal("Error handling healing specific messages: "
					+ e.getMessage());
			System.exit(0);
		}

	}

	@Override
	public void sleepFor() {
		try {
			Thread.sleep(SystemSettings.getSystemSettings()
					.getScheduling_idle_time());
		} catch (InterruptedException e) {
			Healing.logger
					.fatal("Error attempting to sleep: " + e.getMessage());
		}
	}

}
