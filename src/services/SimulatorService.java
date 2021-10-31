package services;

import services.utils.DbSimulator;

/**
 * Responsible for simulator related operations
 * @author Marc Frincu
 * @since 2010
 *
 */
public class SimulatorService {

	/**
	 * Returns the time the given module has to fail
	 * @param agentUuid the agent UUID
	 * @param workflowId the workflow ID
	 * @param type the module type
	 * @return the time in milliseconds or -1 in case the module is not found
	 * @throws Exception
	 */
	public String getFailTime(String agentUuid, 
								String workflowId, 
								String type) throws Exception {
		return Long.toString(DbSimulator.getDb().getFailTime(agentUuid, workflowId, type));
	}
}
