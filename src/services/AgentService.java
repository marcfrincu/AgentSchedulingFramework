package services;
import java.util.UUID;
import java.util.Vector;

import services.utils.DbAgent;

/**
 * Service responsible for agent related operations
 * @author Marc Frincu
 * @since 2010
 *
 */
public class AgentService {

	/**
	 * Creates a cluster of agents suited to solve this task
	 * @param taskId the task ID
	 * @param taskEET the task EET
	 * @param taskECT the task ECT
	 * @param taskInfo additional information on the task
	 * @param agentId the ID of the agent issuing the request
	 * @return the first agent in the cluster
	 * <port id="o1">agentID</port>
	 * <port id="o2">taskID</port>
	 * <port id="o3">groupID</port>
	 * @throws Exception 
	 */
	//TODO: call clustering algorithm here.
	// For now we grab every possible agent
	public String makeAgentCluster(String taskId,
							String taskEET,
							String taskECT,
							String taskInfo,
							String agentId
							) throws Exception {
		DbAgent db = DbAgent.getDb();
		Vector<DbAgent.Agent> agents = db.getAgents();
		
		// we encountered an error when querying the DB
		if (agents == null) {
			return "-1" + "#" + taskId;
		}
		// no agents found in the database
		if (agents.size() == 0) {
			return "-1" + "#" + taskId;
		}
		
		// create the agent group
		int i=0;
		String groupId = UUID.randomUUID().toString();
		while (i<agents.size()) {
			db.addAgentToGroup(groupId, agents.get(i).getId());
			i++;
		}
		// return the first agent in the group
		return agents.get(0).getId() + "#" + taskId + "#" + groupId;
	}
	
	/**
	 * Stores an offer for a given task from a certain agent 
	 * @param fromId the ID of the agent that issued the offer
	 * @param toId the ID of the requesting agent
	 * @param taskId the task for which the offer is made
	 * @param offer the offer of the agent
	 * @param negotiationUUID the UUID of this negotiation round
	 * @return the agent ID that issued the offer and a boolean value
	 * indicating whether this is the last offer or not
	 * <port id="o1">fromID</port>
	 * <port id="o2">true|false</port> 
	 * @throws Exception 
	 */
	public String storeOffer(String fromId, 
						String toId, 
						String taskId, 
						String offer,
						String negotiationUUID) throws Exception {
		DbAgent db = DbAgent.getDb();
		
		boolean ok = db.storeOffer(Integer.parseInt(fromId),
				toId.trim().length() == 0 ? 
						Integer.parseInt(fromId) : Integer.parseInt(toId), 
				Integer.parseInt(taskId),
				offer,
				negotiationUUID);
		
		return fromId + "#" + ok;
	}
	
	/**
	 * Returns information regarding the given agent ID
	 * @param agentId
	 * @param taskId
	 * @param groupId
	 * @return the information on the agent
	 * <port id="o1">agentID</port>
	 * <port id="o2">taskID</port>
	 * <port id="o3">groupID</port>
	 * @throws Exception 	
	 */
	public String getAgent(String agentId, 
				String taskId, 
				String groupId) throws Exception {
		DbAgent db = DbAgent.getDb();
		DbAgent.Agent agent = db.getAgentByGroup(Integer.parseInt(agentId),
							groupId);
		if (agent == null) {
			return "-1" + "#" + taskId + "#" + groupId;
		}
		return agent.getId() + "#" + taskId + "#" + groupId;
	}
	
	/**
	 * Returns the ID of the agent following the given agent ID from 
	 * the cluster of agents
	 * @param agentId
	 * @param taskId
	 * @param groupId
	 * @return the next agent's ID in a circular queue manner and whether
	 * it is first or not
	 * <port id="o1">agentID</port>
	 * <port id="o2">true|false</port>
	 * <port id="o3">taskID</port>
	 * <port id="o4">groupId</port>
	 * @throws Exception 
	 */
	public String getNextAgent(String agentId, 
					String taskId, 
					String groupId) throws Exception {
		DbAgent db = DbAgent.getDb();
		
		boolean isFirst = db.isFirst(Integer.parseInt(agentId), groupId);
		int id = db.getNextAgentId(Integer.parseInt(taskId), groupId);
		
		return id + "#" + isFirst + "#" + taskId + "#" + groupId; 
	}
	
	/**
	 * Returns the ID of the current agent based on its UUID.
	 * This method also
	 * @param agentUuid
	 * @return the current agent's ID
	 * <port id="o1">agentID</port>
	 * <port id="o2">offerUUID</port>
	 * @throws Exception 
	 */
	public String getAgentIDByUuid(String agentUuid) throws Exception {
		DbAgent db = DbAgent.getDb();
		int id = db.getAgentIdByUuid(agentUuid);
		
		return String.valueOf(id);
	}
	
	/**
	 * Computes the UUID used to identify this
	 * negotiation round  
	 * @return the UUID 
	 * <port id="o1">negotiationUUID</port>
	 */
	public String newNegotiation() {
		return UUID.randomUUID().toString() + Math.random();
	}
	
	/**
	 * Returns the received agent UD
	 * @param agentId
	 * @return
	 * <port id="o1">agentId</port>
	 */
	public String storeTemporaryAgent(String agentId) {
		return agentId;
	}
	
	/**
	 * Packs an offer for a task
	 * @param taskUuid
	 * @param offer
	 * @return
	 * <port id="o1">taskId|offer</port>
	 */
	public String packOffer(String taskUuid, String offer) {
		StringBuilder sb = new StringBuilder();
		sb.append(taskUuid);
		sb.append("@");
		sb.append(offer);
		return sb.toString();
	}
	
	/**
	 * Unpacks an offer for a task
	 * @param fromAgentId
	 * @param packedOffer
	 * @return
	 * <port id="o1">fromAgentId</port>
	 * <port id="o2">taskUuid</port>
	 * <port id="32">offer</port>
	 */
	public String unpackOffer(String fromAgentId, String packedOffer) {
		String[] parts = packedOffer.split("@");
		return fromAgentId + "#" +
		parts[0] + "#" + parts[1];
	}
	
	/**
	 * Checks the negotiation status of a given task
	 * @param agentUuid the agent UUID
	 * @param taskId the task ID
	 * @return
 	 * <port id="o1">taskId</port>
	 * <port id="o2">status: 0|1|2|3</port>
 
	 * @throws Exception
	 */
	public String checkNegotiationStatus(String agentUuid, 
									String taskId) throws Exception {
		DbAgent db = DbAgent.getDb();
		int status = db.getNegotiationStatus(agentUuid, taskId);
		return taskId + "#" + status;
	}
	
	/**
	 * Adds an agent module to the database 
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @param isPaused true if the module is to be paused
	 * @param isExternal true if the module is a custom application not part of the framework
	 * @param startScript the path to the script starting the module. Usually part of the <i>archiveFile</i>
	 * @param archiveFile the path to the archive. This needs to be accessible to the Healing agents through CA
	 * @return true if the insertion succeeded, false otherwise
	 * @throws Exception
	 */
	public String addModule(String agentUuid, 
							String wfId, 
							String type,
							String isPaused,
							String isExternal,							
							String archiveFile,
							String startScript) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Boolean.toString(db.addModule(agentUuid, wfId, type, Boolean.getBoolean(isPaused), Boolean.getBoolean(isExternal), startScript, archiveFile));
	}
	
	/**
	 * Pings a module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @return true if the ping succeeded, false otherwise
	 * @throws Exception
	 */
	public String pingModule(String agentUuid, 
								String wfId, 
								String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Boolean.toString(db.ping(agentUuid, wfId, type));
	}
	
	/**
	 * Retrieves the last ping time of a module. Can be used to check whether the module is
	 * still active or not
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @return the last ping time in milliseconds
	 * @throws Exception
	 */
	public String getLastPingTime(String agentUuid, 
									String wfId, 
									String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Long.toString(db.getLastPingTime(agentUuid, wfId, type));
	}

	/**
	 * Retrieves the module status.
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @return the module status
	 * @throws Exception
	 */
	public String getModuleStatus(String agentUuid, 
								String wfId, 
								String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return db.getModuleStatus(agentUuid, wfId, type);
	}
	
	/**
	 * Retrieves the archive location of the module.
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @return the archive location
	 * @throws Exception
	 */
	public String getModuleArchive(String agentUuid, 
								String wfId, 
								String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return db.getModuleArchive(agentUuid, wfId, type);
	}
	
	/**
	 * Retrieves the module starting script.
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @return the starting script
	 * @throws Exception
	 */
	public String getModuleStartScript(String agentUuid, 
								String wfId, 
								String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return db.getModuleStartScript(agentUuid, wfId, type);
	}
	
	/**
	 * Returns the external status of the module. A module is external if it is not part of 
	 * this platform but a different application linked to ours
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @return true or false depending on whether the module is part of our platform or not
	 * @throws Exception
	 */
	public String getModuleIsExternal(String agentUuid, 
								String wfId, 
								String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Boolean.toString(db.getModuleIsExternal(agentUuid, wfId, type));
	}
		
	/**
	 * Sets the module status
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type as specified by the <i>dbAgent.MODULE_TYPE</i>
	 * @param status the new module status as specified by the 
	 * <i>modules.IModule.MODULE_STATUS</i>
	 * @return true if the operation succeeded, false otherwise
	 * @throws Exception
	 */
	public String setModuleStatus(String agentUuid, 
									String wfId, 
									String type, 
									String status) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Boolean.toString(db.setModuleStatus(agentUuid, wfId, type, status));
	}
	
	/**
	 * Adds a module to a healer. This happens when a new module has registered to the healer
	 * @param healingModuleUuuid the healing module UUID
	 * @param moduleUuid the module's UUID
	 * @param wfId the workflow ID of the module
	 * @param type the module type as specified by <i>modules.IModule.MODULE_TYPE</i>
	 * @return true if the operation succeeded, false otherwise
	 * @throws Exception
	 */
	public String addModuleToHealer(String healingModuleUuuid,
									String moduleUuid,
									String wfId,
									String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Boolean.toString(db.addModuleToHealer(healingModuleUuuid, moduleUuid, wfId, type));
	}
	
	/**
	 * Removes a module from a healer. This happens when a module has failed and a new one was started
	 * @param healingModuleUuuid the healing module UUID
	 * @param moduleUuid the module's UUID
	 * @param wfId the workflow ID of the module
	 * @param type the module type as specified by <i>modules.IModule.MODULE_TYPE</i>
	 * @return true if the operation succeeded, false otherwise
	 * @throws Exception
	 */
	public String removeModuleFromHealer(String healingModuleUuuid,
									String moduleUuid,
									String wfId,
									String type) throws Exception {
		DbAgent db = DbAgent.getDb();
		return Boolean.toString(db.removeModuleFromHealer(healingModuleUuuid, moduleUuid, wfId, type));
	}
	
	/**
	 * Returns the list of modules attached to the given agent 
	 * @param uuid the agent UUID
	 * @return
	 * <module id="" agent_uuid="" workflow_id="" last_ping="" type="" status=""/>
	 * [...]
	 * @throws Exception 
	 */
	/*public String getAgentModules(String uuid) throws Exception {
		final Vector<DbAgent.AgentModule> modules = DbAgent.getDb().getAgentModules(uuid);
		if (modules == null) {
			return "-1";
		}
		StringBuilder response = new StringBuilder();
		for (DbAgent.AgentModule module : modules) {
			response.append(module.getId());
			response.append(":");
			response.append(module.getAgent_uuid());
			response.append(":");
			response.append(module.getWorkflow_id());
			response.append(":");
			response.append(module.getLast_ping());
			response.append(":");
			response.append(module.getType());
			response.append(":");
			response.append(module.getStatus());
			
			response.append("#");
		}
		
		return response.toString().substring(0, response.toString().length()-1);
	}*/
	
	/**
	 * Returns the list of modules similar with the one attached to <i>agentId</i>
	 * @param agentId the agent ID
	 * @param agentUuid the agent UUID
	 * @param workflowUuid the workflow UUID
	 * @param moduleType the module type
	 * @return
	 * <module id="" agent_uuid="" workflow_id="" last_ping="" type="" status=""/>
	 * @throws Exception
	 */
	/*public String getAgentModules(String agentId, 
								String agentUuid,
								String workflowUuid,
								String moduleType) throws Exception {
		final Vector<DbAgent.AgentModule> modules = DbAgent.getDb().getFellowModules(
															Integer.parseInt(agentId), 
															agentUuid, 
															workflowUuid, 
															moduleType);
		if (modules == null) {
			return "-1";
		}
		StringBuilder response = new StringBuilder();
		for (DbAgent.AgentModule module : modules) {
			response.append(module.getId());
			response.append(":");
			response.append(module.getAgent_uuid());
			response.append(":");
			response.append(module.getWorkflow_id());
			response.append(":");
			response.append(module.getLast_ping());
			response.append(":");
			response.append(module.getType());
			response.append(":");
			response.append(module.getStatus());
			
			response.append("#");
		}
		
		return response.toString().substring(0, response.toString().length()-1);
	}*/

	
	/**
	 * Returns the list of agents 
	 * @return
	 * <agent id="" clone_of_agent_id="" description="" resource_discovery_id="" available="" uuid=""/>
	 * [...]
	 * @throws Exception 
	 */
	/*public String getAgents() throws Exception {
		final Vector<DbAgent.Agent> agents = DbAgent.getDb().getAgents();
		if (agents == null) {
			return "-1";
		}

		StringBuilder response = new StringBuilder();
		for (DbAgent.Agent agent : agents) {
			response.append(agent.getId());
			response.append(":");
			response.append(agent.getClone_of_agent_id());
			response.append(":");
			response.append(agent.getDescription());
			response.append(":");
			response.append(agent.getResource_discovery_id());
			response.append(":");
			response.append(agent.isAvailable());
			response.append(":");
			response.append(agent.getUuid());
			
			response.append("#");
		}
		
		return response.toString().substring(0, response.toString().length()-1);
	}*/
}
