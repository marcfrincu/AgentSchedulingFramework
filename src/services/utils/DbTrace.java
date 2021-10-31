package services.utils;

import settings.SystemSettings;

/**
 * Class responsible for handling the platform traces stored inside the database
 * @author Marc Frincu
 * @since 2010
 *
 */
public class DbTrace extends Db{

	private static DbTrace db = null;

	protected DbTrace(String database, String username, String password)
			throws Exception {
		super(database, username, password);
		// TODO Auto-generated constructor stub
	}
	
	public static DbTrace getDb() throws Exception {
		if (DbTrace.db == null) {
			DbTrace.db = new DbTrace(SystemSettings.getSystemSettings().getDb_name(),
					SystemSettings.getSystemSettings().getDb_user(),
					SystemSettings.getSystemSettings().getDb_password());
			return DbTrace.db;
		}
		else
			return DbTrace.db;
	}
	
	/**
	 * Stores a trace in the databse
	 * @param agentUuid the agent UUID
	 * @param workflowId the workflow ID
	 * @param moduleType the module type as specified by <i>IModule.MODULE_TYPE</i> enum
	 * @param message the message associated with this trace
	 * @param time the moment this trace was produced in milliseconds 
	 * @return true if the trace has been successfully inserted, false otherwise
	 */
	public boolean addTrace(String agentUuid, String workflowId, String moduleType, 
			String message, long time) {
		try {
			this.executeStatement("INSERT INTO trace (agent_uuid, workflow_id,module_type," +
					"msg_description, msg_time) VALUES (" +
					"\'"+agentUuid+"\'," +
					"\'"+workflowId+"\'," +
					"\'"+moduleType+"\'," +
					"\'"+message+"\'," +
					time +
					");");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
