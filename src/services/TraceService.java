package services;

import services.utils.DbTrace;

/**
 * Service for storing application traces.
 * @author Marc Frincu
 * @since 2010
 *
 */
public class TraceService {

	public boolean addTrace(String agentUuid, String workflowId, String moduleType, 
			String message, long time) throws Exception {
		return DbTrace.getDb().addTrace(agentUuid, workflowId, moduleType, message, time);
	}
}
