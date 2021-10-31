package modules.communication.json.beans;

/**
 * Bean holding information about a task. Used for creating the JSON object
 * @author Marc Frincu
 *
 */
public class Task {

	private int id;
	private int original_agent_id;
	private String description;
	private String executable_location;
	private String dependencies;
	private int position_on_resource_queue;
	private long submission_time_local;
	private long submission_time_total;
	private long estimated_execution_time_on_resource;
	private long estimated_completion_time_on_resource;
	private boolean locked;
	private  int assigned_resource_id;
	private String uuid;
	private boolean istemporary;
	private String submiter_agent_uuid;

	public Task() {
		
	}
	
	/**
	 * Default constructor
	 */
	public Task (
			int id,
			int original_agent_id,
			String description,
			String executable_location,
			String dependencies,
			int position_on_resource_queue,
			long submission_time_local,
			long submission_time_total,
			long estimated_execution_time_on_resource,
			long estimated_completion_time_on_resource,
			boolean locked,
		    int assigned_resource_id,
			String uuid,
			boolean istemporary,
			String submiter_agent_id
			) {
		this.id = id;
		this.original_agent_id = original_agent_id;
		this.description = description;
		this.executable_location = executable_location;
		this.dependencies = dependencies;
		this.position_on_resource_queue = position_on_resource_queue;
		this.submission_time_local = submission_time_local;
		this.submission_time_total = submission_time_total;
		this.estimated_execution_time_on_resource = estimated_execution_time_on_resource;
		this.estimated_completion_time_on_resource = estimated_completion_time_on_resource;
		this.locked = locked;
		this.assigned_resource_id = assigned_resource_id;
		this.uuid = uuid;
		this.istemporary = istemporary;
		this.submiter_agent_uuid = submiter_agent_id;
	}
	
	/**
	 * The ID of the task - local ID
	 */
	public int getId() {
		return this.id;
	}
	/**
	 * The ID of the original agent - local ID
	 */
	public int getOriginal_agent_id() {
		return this.original_agent_id;
	}
	/**
	 * The description of the task
	 */
	public String getDescription() {
		return this.description;
	}
	/**
	 * The location of the executable
	 */
	public String getExecutable_location() {
		return this.executable_location;
	}
	/**
	 * The list of dependency applications/libraries
	 */
	public String getDependencies() {
		return this.dependencies;
	}
	/**
	 * The position on the resource queue
	 */
	public int getPosition_on_resource_queue() {
		return this.position_on_resource_queue;
	}
	/**
	 * The time when the task was submitted to the currently assigned queue
	 */
	public long getSubmission_time_local() {
		return this.submission_time_local;
	}
	/**
	 * The time when the task was first submitted to the system
	 */
	public long getSubmission_time_total() {
		return this.submission_time_total;
	}
	/**
	 * The estimated execution time on the assigned resource
	 */
	public long getEstimated_execution_time_on_resource() {
		return this.estimated_execution_time_on_resource;
	}
	/**
	 * The estimated completion time on the assigned resource
	 */
	public long getEstimated_completion_time_on_resource() {
		return this.estimated_completion_time_on_resource;
	}
	/**
	 * Specifies whether the task is locked or not for processing
	 */
	public boolean isLocked() {
		return this.locked;
	}
	/**
	 * The ID of the assigned resource - local ID
	 */
	public int getAssigned_resource_id() {
		return this.assigned_resource_id;
	}
    /**
     * The UUID of the task  - global ID
     */
	public String getUuid() {
		return this.uuid;
	}
	/**
	 * Specifies whether the task is assigned only temporary or not
	 */
	public boolean isIstemporary() {
		return this.istemporary;
	}

	/**
	 * The UUID of the agent that sent the task
	 * @return the UUID of the sender agent
	 */
	public String getSubmiter_agent_uuid() {
		return submiter_agent_uuid;
	}

	
	public void setId(int id) {
		this.id = id;
	}

	public void setOriginal_agent_id(int originalAgentId) {
		original_agent_id = originalAgentId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExecutable_location(String executableLocation) {
		executable_location = executableLocation;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public void setPosition_on_resource_queue(int positionOnResourceQueue) {
		position_on_resource_queue = positionOnResourceQueue;
	}

	public void setSubmission_time_local(long submissionTimeLocal) {
		submission_time_local = submissionTimeLocal;
	}

	public void setSubmission_time_total(long submissionTimeTotal) {
		submission_time_total = submissionTimeTotal;
	}

	public void setEstimated_execution_time_on_resource(
			long estimatedExecutionTimeOnResource) {
		estimated_execution_time_on_resource = estimatedExecutionTimeOnResource;
	}

	public void setEstimated_completion_time_on_resource(
			long estimatedCompletionTimeOnResource) {
		estimated_completion_time_on_resource = estimatedCompletionTimeOnResource;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setAssigned_resource_id(int assignedResourceId) {
		assigned_resource_id = assignedResourceId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setIstemporary(boolean istemporary) {
		this.istemporary = istemporary;
	}
	
	public void setSubmiter_agent_uuid(String submiterAgentUuid) {
		submiter_agent_uuid = submiterAgentUuid;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("id: ");
		sb.append(this.id);
		sb.append("\n");
		sb.append("original_agent_id: ");
		sb.append(this.original_agent_id);
		sb.append("\n");
		sb.append("description: ");
		sb.append(this.description);
		sb.append("\n");
		sb.append("executable_location: ");
		sb.append(this.executable_location);
		sb.append("\n");
		sb.append("dependencies: ");
		sb.append(this.dependencies);
		sb.append("\n");
		sb.append("position_on_resource_queue: ");
		sb.append(this.position_on_resource_queue);
		sb.append("\n");
		sb.append("submission_time_local: ");
		sb.append(this.submission_time_local);
		sb.append("\n");
		sb.append("submission_time_total: ");
		sb.append(this.submission_time_total);
		sb.append("\n");
		sb.append("estimated_execution_time_on_resource: ");
		sb.append(this.estimated_execution_time_on_resource);
		sb.append("\n");
		sb.append("estimated_completion_time_on_resource: ");
		sb.append(this.estimated_completion_time_on_resource);
		sb.append("\n");
		sb.append("locked: "); 
		sb.append(this.locked);
		sb.append("\n");
		sb.append("original_agent_id: ");
		sb.append(this.original_agent_id);
		sb.append("\n");
		sb.append("uuid: ");
		sb.append(this.uuid);
		sb.append("\n");
		sb.append("istemporary: "); 
		sb.append(this.istemporary);
		sb.append("\n");
		sb.append("submiter_agent_id: "); 
		sb.append(this.submiter_agent_uuid);
		
		return sb.toString();
	}
	
}
