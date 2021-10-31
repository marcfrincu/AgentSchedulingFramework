package modules.communication.json.beans;

/**
 * Bean holding information about an agent module
 * @author Marc Frincu
 * @since 2010
 */
public class AgentModule {

	String uuid, workflowID, moduleType, status, agentParentId;

	long lastPing;
	
	public AgentModule() {
		
	}
	
	public AgentModule(
			String uuid,
			String agentParentId,
			String workflowID,
			String moduleType,
			String status,
			long lastPing) {
		this.uuid = uuid;
		this.workflowID = workflowID;
		this.moduleType = moduleType;
		this.status = status;
		this.lastPing = lastPing;
		this.agentParentId = agentParentId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setWorkflowID(String workflowID) {
		this.workflowID = workflowID;
	}

	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setAgentParentId(String agentParentId) {
		this.agentParentId = agentParentId;
	}

	public void setLastPing(long lastPing) {
		this.lastPing = lastPing;
	}
	
	public String getAgentParentId() {
		return this.agentParentId;
	}
	
	public String getUuid() {
		return this.uuid;
	}

	public String getWorkflowID() {
		return this.workflowID;
	}

	public String getModuleType() {
		return this.moduleType;
	}

	public String getStatus() {
		return this.status;
	}

	public long getLastPing() {
		return this.lastPing;
	}
		
}
