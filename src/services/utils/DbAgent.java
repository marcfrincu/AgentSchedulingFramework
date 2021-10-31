package services.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import settings.SystemSettings;

/**
 * Handles agent specific operations on the database
 * @author Marc Frincu
 * @since 2010
 *
 */
//TODO: use stored procedures
public class DbAgent extends Db {
	
	private static DbAgent db = null;

	protected DbAgent(String database, String username, String password)
			throws Exception {
		super(database, username, password);
		// TODO Auto-generated constructor stub
	}
	
	public static DbAgent getDb() throws Exception {
		if (DbAgent.db == null) {
			DbAgent.db = new DbAgent(SystemSettings.getSystemSettings().getDb_name(),
					SystemSettings.getSystemSettings().getDb_user(),
					SystemSettings.getSystemSettings().getDb_password());
			return DbAgent.db;
		}
		else
			return DbAgent.db;
	}

	/**
	 * Adds a new agent and registers it
	 * @param uuid the ID of the agent as described by the agent token
	 * @param idOriginalAgent -1 or the agentID that cloned this agent
	 * @param resourceDiscoveryId the ID of the resource discovery entry
	 * that will provide links to the URIs for accessing this agent's tasks
	 * and resources
	 * @param description
	 * @param active
	 * @return the ID of the agent or -1 in case of failure
	 */
	public int addAndRegisterAgent(String uuid,
									int idOriginalAgent,
									int resourceDiscoveryId,
									String description,
									boolean active) {
		try {
			this.executeStatement("INSERT INTO agent " +
					" (clone_of_agent_id, " +
					"description, " +
					"resource_discovery_id," +
					"available," +
					"uuid) " +
					"VALUES " +
					"(" + idOriginalAgent +
					",\'" + description + "\'," +
					resourceDiscoveryId + "," +
					"\'" + Boolean.valueOf(active).toString() + "\'," +
					"\'" + uuid + "\'" +
					");");
			final String id = this.getFirst("SELECT id FROM agent " +
					"WHERE clone_of_agent_id=" +
					idOriginalAgent + 
					" AND description=\'" + description + "\'" +
					" AND resource_discovery_id=" + resourceDiscoveryId + 
					";");
			this.executeStatement("INSERT INTO agent_registration " +
					"(agent_id) " +
					"VALUES " +
					"(" + Integer.parseInt(id) + 
					");");
			return Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Adds a new resource discovery entry
	 * @param resource_uri
	 * @param task_uri
	 * @return true if operation succeeded or false otherwise
	 */
	public boolean addResourceDiscovery(String resource_uri,
									String task_uri) {
		
		try {
			this.executeStatement("INSERT INTO resource_discovery " +
					" (resource_uri, task_uri)" +
					" VALUES (" +
					"\'" + resource_uri + "\'," +
					"\'" + task_uri + "\'" +
					");");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns data on a given agent
	 * @param agentId
	 * @return a <i>DbAgent.Agent</i> object or null in case of error
	 */
	public DbAgent.Agent getAgent(int agentId) {
		try {
			DbAgent.Agent agent = null;
			final ResultSet rs = this.getQuery(
					"SELECT id, uuid, clone_of_agent_id," +
					"description, resource_discovery_id," +
					"available" +
					"FROM agent WHERE id=" + agentId + ";");
			if (rs.next()) {
				agent = new DbAgent.Agent(
						rs.getInt(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getBoolean(6)
						);
				
			}
			return agent;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the next agent ID using a circular queue approach
	 * @param taskId
	 * @return the next agent's ID or -1 in case of error (agentId not found included)
	 */
	public int getNextAgentId(int taskId, String groupId) {
		try {
			final ResultSet rs = this.getQuery(
					"SELECT a.id " +
					"FROM agent a, agent_group g " +
					"WHERE g.group_id=\'" + groupId +
					"\' AND g.agent_id=a.id " +
					"ORDER BY id ASC;");
			int i = 0, firstId = -1;
			
			while (rs.next()) {
				// if we are at the first entry store the ID of the first task
				if (i == 0) {
					firstId = rs.getInt(1);
				}
				// if we found the task
				if (taskId == rs.getInt(1)) {
					// return the ID of the first task if our task the last
					if (rs.isLast()) {
						return firstId;
					}
					// if not last return the ID of the one following it
					else {
						rs.next();
						return rs.getInt(1);
					}
				}
				i++;
			}
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Returns data on an agent based on the group it belongs to
	 * @param agentId
	 * @param groupId
	 * @return a <i>DbTask.Task</i> object or null in case of error
	 */
	public DbAgent.Agent getAgentByGroup(int agentId, String groupId) {
		try {
			DbAgent.Agent agent = null;
			final ResultSet rs = this.getQuery(
					"SELECT a.id, a.uuid, a.clone_of_agent_id, " +
					"a.description, a.resource_discovery_id, " +
					"a.available " +
					"FROM agent a, agent_group b WHERE a.id=" + agentId + "" +
					" AND a.id=b.agent_id AND b.group_id=\'" + groupId +
					"\';");
			if (rs.next()) {
				agent = new DbAgent.Agent(
						rs.getInt(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getBoolean(6)
						);
				
			}
			return agent;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the list of all agents in the database
	 * @return a <i>Vector</i> of <i>Agent</i> objects
	 */
	public Vector<DbAgent.Agent> getAgents() {
		Vector<DbAgent.Agent> agents = new Vector<DbAgent.Agent>();
		try {
			DbAgent.Agent agent = null;
			final ResultSet rs = this.getQuery(
					"SELECT id, uuid, clone_of_agent_id, " +
					"description, resource_discovery_id, " +
					"available " +
					"FROM agent;");
			while (rs.next()) {
				agent = new DbAgent.Agent(
						rs.getInt(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getBoolean(6)
						);
				agents.add(agent);
			}
			return agents;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * Returns a list of agents based on their group
	 * @param groupId
	 * @return a <i>Vector</i> of <i>Agent</i> objects
	 */
	public Vector<DbAgent.Agent> getAgentsByGroup(int groupId) {
		Vector<DbAgent.Agent> agents = new Vector<DbAgent.Agent>();
		try {
			DbAgent.Agent agent = null;
			final ResultSet rs = this.getQuery(
					"SELECT a.id, a.uuid, a.clone_of_agent_id," +
					"a.description, a.resource_discovery_id," +
					"a.available" +
					"FROM agent a, agent_group b WHERE a.id=b.agent_id " +
					"AND a.group_id=\'" + groupId +
					"\';");
			while (rs.next()) {
				agent = new DbAgent.Agent(
						rs.getInt(1),
						rs.getString(2),
						rs.getInt(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getBoolean(6)
						);
				agents.add(agent);
			}
			return agents;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Adds an agent to a group
	 * @param groupId
	 * @param agentId
	 * @return true if the operation succeeded, false otherwise 
	 */
	public boolean addAgentToGroup(String groupId, int agentId) {
		try {
			this.executeStatement("DELETE FROM agent_group WHERE agent_id=" + agentId + ";");
			this.executeStatement("INSERT INTO agent_group" +
					"(agent_id, group_id) " +
					"VALUES " +
					"(" + agentId + ",\'" +
					groupId +
					"\');");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Stores an agent's offer
	 * @param requestingAgentId
	 * @param biddingAgentId
	 * @param taskId
	 * @param value
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean storeOffer(int requestingAgentId,
							int biddingAgentId,
							int taskId,
							String value,
							String uuid) {
		try {
			this.executeStatement("INSERT INTO offer " +
					"(requesting_agent_id, " +
					"bidding_agent_id," +
					" task_id," +
					" value," +
					" uuid)" +
					" VALUES (" +
					requestingAgentId + "," +
					biddingAgentId + "," +
					taskId + "," + 
					"\'" + value + 
					"\'," +
					"\'" + uuid + "\'" +
					");");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Checks whether an agent is the first one or not in the list.
	 * In order to make this operation agents are ordered by their IDs
	 * @param agentId
	 * @param groupId
	 * @return true if the agent is the first in the list, 
	 * false otherwise (errors included)
	 */
	public boolean isFirst(int agentId, String groupId) {
		try {
			final ResultSet rs = this.getQuery(
					"SELECT a.id " +
					"FROM agent a, agent_group g " +
					"WHERE g.group_id=\'" + groupId + "\' " +
					"ORDER BY a.id ASC;");
			int i = 0;
			while (rs.next()) {
				if (agentId == rs.getInt(1) && i == 0) {
					return true;
				}
				i++;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Activates an agent
	 * @param agentId
	 * @return true if the activation succeeded, false otherwise
	 */
	public boolean activateAgent(int agentId) {
		try {
			this.executeStatement(
					"UPDATE agent SET available=true" +
					" WHERE id=" + agentId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * De-activates an agent
	 * @param agentId
	 * @return true if the de-activation succeeded, false otherwise
	 */
	public boolean deactivateAgent(int agentId) {
		try {
			this.executeStatement(
					"UPDATE agent SET available=false" +
					" WHERE id=" + agentId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}
	
	/**
	 * Returns the URI of the resource provider service attached to this agent
	 * @param agentUuid
	 * @return the URI or null in case of error
	 */
	public String getResourceUri(String agentUuid) {
		String uri;
		try {
			uri = this.getFirst(
					"SELECT r.resource_uri FROM resource_discovery r, " +
					" agent a " +
					" WHERE a.uuid=\'" + agentUuid + 
					"\' AND a.resource_discovery_id=r.id" + ";"
					);
			return uri;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the URI of the task provider service attached to this agent
	 * @param agentUuid
	 * @return the URI or null in case of error
	 */
	public String getTaskUri(String agentUuid) {
		String uri;
		try {
			uri = this.getFirst(
					"SELECT r.task_uri FROM resource_discovery r, " +
					" agent a " +
					" WHERE a.uuid=" + agentUuid + 
					" AND a.resource_discovery_id=r.id" + ";"
					);
			return uri;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Verifies whether a given agent is registred or not
	 * @param agentUuid
	 * @return true if the agent is registered, false otherwise (error included)
	 */
	public boolean checkAgentIsRegistred(String agentUuid) {
		try {
			String result = this.getFirst("SELECT count(*) FROM agent a, " +
					"agent_registration r " +
					" WHERE a.id=r.agent_id AND a.uuid=\'" + agentUuid + "\';");
			if (Integer.parseInt(result) == 1) {
				return true;
			}
			else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns the ID of an agent based on its token Uuuid
	 * @param agentUuid
	 * @return the agent ID or -1 in case of error or when no agent is found
	 */
	public int getAgentIdByUuid(String agentUuid) {
		try {
			String no = this.getFirst("SELECT count(*) FROM agent " +
					"WHERE uuid=\'" + agentUuid + "\';");
			if (Integer.parseInt(no) == 0) {
				return -1;
			}
			String id = this.getFirst("SELECT id FROM agent " +
					"WHERE uuid=\'" + agentUuid + "\';");
			return Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Returns the negotiation status for a given task.
	 * The status can be: 0 - not started, 1 - started, 2 - completed no relocation
	 * 3 - completed with relocation
	 * @param agentUuid the agent UUID
	 * @param taskId the task ID
	 * @return the negotiation status or -1 in case of errors
	 */
	public int getNegotiationStatus(String agentUuid,
										String taskId) {
		String status;
		try {
			status = this.getFirst("SELECT status FROM negotiation_status " + 
								"WHERE agent_id='" + agentUuid + "' AND " +
								"task_id=" + taskId);
			return Integer.parseInt(status);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Updates the last response time of a given module belonging to a specified agent
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the type of the module as defined by <i>modules.IModule.MODULE_TYPE</i>
	 * @return true if the ping succeeded, false otherwise
	 */
	public boolean ping(String agentUuid, String wfId, String type) {
		try {
			this.executeStatement("UPDATE agent_module SET last_ping=" + System.currentTimeMillis() + 
					" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
							" AND type=\'" + type + "\';");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the last ping time of a given agent's module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID
	 * @param type the module type
	 * @return the last ping time in milliseconds or -1 in case of error
	 */
	public long getLastPingTime(String agentUuid, String wfId, String type) {
		try {
			final String lastPing = this.getFirst("SELECT last_ping FROM agent_module " +
					" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
					" AND type=\'" + type + "\';");
			return lastPing == null ? -1 : Long.parseLong(lastPing);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Adds a new module to an agent
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @param isPaused true if the module is to be paused
	 * @param isExternal true if the module is a custom application not part of the framework
	 * @param startScript the path to the script starting the module. Usually part of the <i>archiveFile</i>
	 * @param archiveFile the path to the archive. This needs to be accessible to the Healing agents through CA
	 * @return true if the module has been added, false otherwise
	 */
	public boolean addModule(String agentUuid, String wfId, String type, boolean isPaused, boolean isExternal, String startScript, String archiveFile) {
		try {
			final int count = Integer.parseInt(this.getFirst("SELECT COUNT(*) FROM agent_module WHERE " +
					"agent_uuid=\'" + agentUuid + "\'" +
					" AND workflow_id=\'" + wfId + "\'" +
					" AND type=\'" + type + "\'"));
			if (count > 0) {
				return false;
			}
			
			this.executeStatement("INSERT INTO agent_module (last_ping, agent_uuid, workflow_id, type, paused, external, start_script, archive)" +
					" VALUES (" + System.currentTimeMillis() + 
					",\'" + agentUuid +  
					"\',\'" + wfId + "\'," +
					"\'" + type + "\'," +
					"\'" + Boolean.valueOf(isPaused).toString() + "\'," +
					"\'" + Boolean.valueOf(isExternal).toString() + "\'," +
					"\'" + startScript + "\'," +
					"\'" + archiveFile + "\'"
					+ ");");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Retrieves the status of a module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @return the module's status or <i>-1</i> in case of error
	 */
	public String getModuleStatus(String agentUuid, 
							String wfId, 
							String type) {
		try {
			final String status = this.getFirst("SELECT status FROM agent_module " +
				" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
				" AND type=\'" + type + "\';");
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}
	
	/**
	 * Retrieves the archive location of a module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @return the module's archive location or <i>-1</i> in case of error
	 */
	public String getModuleArchive(String agentUuid, 
							String wfId, 
							String type) {
		try {
			return this.getFirst("SELECT archive FROM agent_module " +
				" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
				" AND type=\'" + type + "\';");
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}

	/**
	 * Retrieves the script used to start the execution of a module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @return the module's start script or <i>-1</i> in case of error
	 */
	public String getModuleStartScript(String agentUuid, 
							String wfId, 
							String type) {
		try {
			return this.getFirst("SELECT start_script FROM agent_module " +
				" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
				" AND type=\'" + type + "\';");
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}
	}
	
	/**
	 * Retrieves the archive location of a module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @return true or false depending on whether the module is an external application or not
	 */
	public boolean getModuleIsExternal(String agentUuid, 
							String wfId, 
							String type) {
		try {
			return Boolean.parseBoolean(this.getFirst("SELECT archive FROM agent_module " +
				" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
				" AND type=\'" + type + "\';"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	
	
	/**
	 * Retrieves the database ID of a module
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @return the module's database ID or <i>-1</i> in case of error
	 */
	public int getModuleDbId(String agentUuid, 
							String wfId, 
							String type) {
		try {
			final int status = Integer.parseInt(this.getFirst("SELECT id FROM agent_module " +
			" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
			" AND type=\'" + type + "\';"));
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
			}
	}
	
	/**
	 * Returns the list of modules associated with the given agent
	 * @param uuid the agent UUID
	 * @return the vector of associated modules or NULL in case none exists
	 */
	public Vector<AgentModule> getAgentModules(String uuid) {
		Vector<AgentModule> modules = new Vector<AgentModule>();
		
		AgentModule module = null;

		ResultSet rs;
		try {
			rs = this.getQuery(
					"SELECT m.id, m.agent_uuid, m.workflow_id," +
					"m.last_ping, m.type," +
					"m.status, , m.paused, m.external, m.start_script, m.archive " +
					"FROM agent a, agent_module m WHERE a.uuid=m.agent_uuid;");
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			while (rs.next()) {
				module = new DbAgent.AgentModule(
						rs.getString(1),
						rs.getString(2),
						rs.getString(3),
						rs.getLong(4),
						rs.getString(5),
						rs.getString(6),
						rs.getBoolean(7),
						rs.getBoolean(8),
						rs.getString(9),
						rs.getString(10)
						);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		return modules;
	}
	
	/**
	 * 
	 * @param agentUuid the agent UUID
	 * @param wfId the workflow ID. Can be <i>-1</i> in case no workflow is attached to it
	 * @param type the module type
	 * @param status the new module status
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean setModuleStatus(String agentUuid, 
						String wfId, 
						String type, 
						String status) {
		try {
			this.executeStatement("UPDATE agent_module SET status=\'" + status + "\'" + 
					" WHERE agent_uuid=\'" + agentUuid +"\' AND workflow_id=\'" + wfId + "\'" +
					" AND type=\'" + type + "\';");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Attaches a module to a specific healer. This information can be later used when recovering a healing agent
	 * @param healingModuleUuuid the UUID of the healing agent
	 * @param moduleUuid the UUID of the module linked to the healer
	 * @param wfId the workflow ID of the module
	 * @param type the type of the module as specified by <i>modules.IModule.MODULE_TYPE</i>
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean addModuleToHealer(String healingModuleUuuid,
										String moduleUuid,
										String wfId,
										String type) {
		int healerId = this.getAgentIdByUuid(healingModuleUuuid);
		int moduleId = this.getModuleDbId(moduleUuid, wfId, type);
		
		try {
			this.removeModuleFromHealer(healingModuleUuuid, moduleUuid, wfId, type);
			this.executeStatement("INSERT INTO healing_groups (healing_agent_id, module_id) VALUES (" + healerId + 
					", " + moduleId + ");");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Removes a module from a specific healer. This information can be later used when recovering a healing agent
	 * @param healingModuleUuuid the UUID of the healing agent
	 * @param moduleUuid the UUID of the module linked to the healer
	 * @param wfId the workflow ID of the module
	 * @param type the type of the module as specified by <i>modules.IModule.MODULE_TYPE</i>
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean removeModuleFromHealer(String healingModuleUuuid,
										String moduleUuid,
										String wfId,
										String type) {
		int healerId = this.getAgentIdByUuid(healingModuleUuuid);
		int moduleId = this.getModuleDbId(moduleUuid, wfId, type);
		
		try {
			this.executeStatement("DELETE FROM healing_groups WHERE healing_agent_id=" + healerId + " AND " + 
					" module_id=" + moduleId + ";");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns similar modules with the one belonging to <i>agentId</i>
	 * @param agentId the agent ID
	 * @param agentUuid the agent UUID
	 * @param workflowUuid the workflow UUID
	 * @param moduleType the module type
	 * @return the list of modules or null in case of error
	 */
	public Vector<DbAgent.AgentModule> getFellowModules(int agentId,
														String agentUuid,
														String workflowUuid,
														String moduleType) {
		Vector<DbAgent.AgentModule> modules = new Vector<AgentModule>();
		ResultSet rs = null;
		try {
			rs = this.getQuery(
					"SELECT m.agent_uuid, m.workflow_id, m.type, m.paused, m.external, m.start_script, m.archive " +
					"FROM agent_module m, agent a, agent b " + 
					"WHERE " +
					"b.id=\'" + agentId + "\' " +
					"AND " +
					"( " +
					"a.clone_of_agent_id=b.id " + 
					"OR " + 
					"(a.clone_of_agent_id=b.clone_of_agent_id AND NOT b.clone_of_agent_id=-1) " + 
					"OR " +
					"(a.id=b.clone_of_agent_id AND NOT b.clone_of_agent_id=-1) " +
					") " +
					"AND " +
					"m.type=\'" + moduleType + "\'" +
					"AND " + 
					"m.agent_uuid=b.uuid " +
					"AND "+ 
					"m.status=\'PAUSED\' " +
					" AND NOT m.agent_uuid=\'" + agentUuid + "\' " +
					" AND NOT m.workflow_id=\'" + workflowUuid + "\' "
					);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		AgentModule module = null;
		try {
			while (rs.next()) {
				module = new DbAgent.AgentModule(
						"",
						rs.getString(1),
						rs.getString(2),
						-1L,
						rs.getString(3),
						"",
						rs.getBoolean(4),
						rs.getBoolean(5),
						rs.getString(6),
						rs.getString(7)
						);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		
		return modules;
	}
	
	/**
	 * Stores information corresponding to an item inside the <i>agent</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class Agent {
		int id;
		String uuid;
		int clone_of_agent_id;
		String description;
		int resource_discovery_id;
		boolean available;
		
		public Agent(int id,
					String uuid,
					int clone_of_agent_id,
					String description,
					int resource_discovery_id,
					boolean available
				) {
			this.id = id;
			this.uuid = uuid;
			this.clone_of_agent_id = clone_of_agent_id;
			this.description = description;
			this.resource_discovery_id = resource_discovery_id;
			this.available = available;
		}

		public String getUuid() {
			return this.uuid;
		}
		
		public int getId() {
			return this.id;
		}

		public int getClone_of_agent_id() {
			return this.clone_of_agent_id;
		}

		public String getDescription() {
			return this.description;
		}

		public int getResource_discovery_id() {
			return this.resource_discovery_id;
		}

		public boolean isAvailable() {
			return this.available;
		}
	}

	/**
	 * Stores information corresponding to an item inside the 
	 * <i>agent_task_group</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class AgentTaskGroup {
		int id;
		int requesting_agent_id;
		int task_id;

		public AgentTaskGroup(int id,
						int requesting_agent_id,
						int task_id
				) {
			this.id = id;
			this.requesting_agent_id = requesting_agent_id;
			this.task_id = task_id;
		}

		public int getId() {
			return this.id;
		}

		public int getRequesting_agent_id() {
			return this.requesting_agent_id;
		}

		public int getTask_id() {
			return this.task_id;
		}
		
		
	}
	
	/**
	 * Stores information corresponding to an item inside the 
	 * <i>agent_group</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class AgentGroup {
		int id;
		int group_id;
		int agent_id;
		
		public AgentGroup(int id,
						int group_id,
						int agent_id
				) {
			this.id = id;
			this.group_id = group_id;
			this.agent_id = agent_id;
		}

		public int getId() {
			return this.id;
		}

		public int getGroup_id() {
			return this.group_id;
		}

		public int getAgent_id() {
			return this.agent_id;
		}
		
		
	}
	
	/**
	 * Stores information corresponding to an item inside the 
	 * <i>offer</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class Offer {
		int id;
		int requesting_agent_id;
		int bidding_agent_id;
		int task_id;
		String value;
		
		public Offer(int id,
					int requesting_agent_id,
					int bidding_agent_id,
					int task_id,
					String value
				) {
			this.id = id;
			this.requesting_agent_id = requesting_agent_id;
			this.bidding_agent_id = bidding_agent_id;
			this.task_id = task_id;
			this.value = value;
		}

		public int getId() {
			return this.id;
		}

		public int getRequesting_agent_id() {
			return this.requesting_agent_id;
		}

		public int getBidding_agent_id() {
			return this.bidding_agent_id;
		}

		public int getTask_id() {
			return this.task_id;
		}

		public String getValue() {
			return this.value;
		}
	}
	
	/**
	 * Stores information corresponding to an item inside the 
	 * <i>agent_registration</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class AgentRegistration {
		int id;
		int agent_id;
		
		public AgentRegistration(int id,
								int agent_id) {
			this.id = id;
			this.agent_id = agent_id;
		}

		public int getId() {
			return this.id;
		}

		public int getAgent_id() {
			return this.agent_id;
		}
	}
	
	/**
	 * Stores information corresponding to an item inside the 
	 * <i>resource_discovery</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class ResourceDiscovery {
		int id;
		String resource_uri;
		String task_uri; 
		
		public ResourceDiscovery(int id,
							String resource_uri,
							String task_uri
				) {
			this.id = id;
			this.resource_uri = resource_uri;
			this.task_uri = task_uri;
		}

		public int getId() {
			return this.id;
		}

		public String getResource_uri() {
			return this.resource_uri;
		}

		public String getTask_uri() {
			return this.task_uri;
		}
	}

	public class AgentModule {
		String id;
		long last_ping;
		String agent_uuid;
		String workflow_id;
		String type;
		boolean paused;
		boolean external;
		String start_script;
		String archive;		
		String status;
		
		public AgentModule(String id,	
							String agent_uuid,
							String workflow_id,
							long last_ping,
							String type,
							String status,
							boolean paused,
							boolean external,
							String start_script,
							String archive) {
			this.id = id;
			this.agent_uuid = agent_uuid;
			this.workflow_id = workflow_id;
			this.last_ping = last_ping;
			this.type = type;
			this.status = status;
			this.paused = paused;
			this.external = external;
			this.start_script = start_script;
			this.archive = archive;
		}

		public boolean isPaused() {
			return paused;
		}

		public boolean isExternal() {
			return external;
		}

		public String getStart_script() {
			return start_script;
		}

		public String getArchive() {
			return archive;
		}

		public String getId() {
			return this.id;
		}

		public String getAgent_uuid() {
			return this.agent_uuid;
		}

		public String getWorkflow_id() {
			return this.workflow_id;
		}

		public long getLast_ping() {
			return this.last_ping;
		}

		public String getType() {
			return this.type;
		}

		public String getStatus() {
			return this.status;
		}
	}
	
}
