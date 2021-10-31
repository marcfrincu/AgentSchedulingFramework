package services.utils;

import settings.SystemSettings;

public class DbSimulator extends Db {

	private static DbSimulator db = null;
	
	protected DbSimulator(String database, String username, String password)
	throws Exception {
		super(database, username, password);
	}
	
	public static DbSimulator getDb() throws Exception {
		if (DbSimulator.db == null) {
			DbSimulator.db = new DbSimulator(SystemSettings.getSystemSettings().getDb_name(),
					SystemSettings.getSystemSettings().getDb_user(),
					SystemSettings.getSystemSettings().getDb_password());
			return DbSimulator.db;
		}
		else
			return DbSimulator.db;
	}

	/**
	 * Returns the time the given module has to fail
	 * @param agentUuid the agent UUID
	 * @param workflowId the workflow ID
	 * @param type the module type
	 * @return the time in milliseconds or -1 in case the module is not found
	 * @throws Exception
	 */
	public long getFailTime(String agentUuid, String workflowId, String type) throws Exception {
		String result = this.getFirst("SELECT failtime FROM simulator WHERE " +
				" agent_uuid='" + agentUuid + "' AND " +
				" workflow_id='" + workflowId + "' AND " + 
				" type='" + type + "';");
		
		return result == null ? -1 : Long.parseLong(result);
	}
}