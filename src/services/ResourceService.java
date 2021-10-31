package services;
import java.util.Vector;

import services.utils.DbResource;

/**
 * Service responsible for resource related operations
 * @author Marc Frincu
 * @since 2010
 *
 */
public class ResourceService {

	/**
	 * Returns the first resource from the list of resources
	 * @param agentId the ID of the agent (executor module) this resource is assigned to
	 * @return the resource ID or -1 in case of error or of no available resources
	 * <port id="o1">resourceID|-1</port>
	 * @throws Exception
	 */
	public String getFirstResourceId(String agentId) throws Exception {
		DbResource db = DbResource.getDb();
		Vector<DbResource.Resource> resources = db.getResources(Integer.parseInt(agentId));
		if (resources != null) {
			if (resources.size() == 0) {
				return "-1";
			}
			else
				return String.valueOf(resources.get(0).getId());
		}
		return "-1";
	}

	
	/**
	 * Returns information on the given resource ID
	 * @param resourceId the resource ID
	 * @return the information on the resource or "-1" in case 
	 * of non-existing resource
	 * <port id="o1">resourceID</port>
	 * @throws Exception 
	 */
	public String getResource(String resourceId) throws Exception {
		DbResource db = DbResource.getDb();
		DbResource.Resource resource = db.getResource(Integer.parseInt(resourceId));
		// if we found ne resource return "-1"
		if (resource == null) {
			return "-1";
		}
		return String.valueOf(resource.getId());
	}

	/**
	 * Returns the ID of the resource following the given resource ID
	 * @param resourceId the resource ID
	 * @param agentId the ID of the agent (executor module) this resource is assigned to
	 * @return the next resource ID in a circular queue manner 
	 * and whether it is first or not
	 * <port id="o1">getResourceID</port>
	 * <port id="o2">true|false</port>
	 * @throws Exception 
	 */
	public String getNextResourceId(String resourceId, String agentId) throws Exception {
		DbResource db = DbResource.getDb();
		int id = db.getNextResourceId(Integer.parseInt(resourceId),
									Integer.parseInt(agentId)
									);
		boolean isFirst = db.isFirst(id);
		
		return String.valueOf(id) + "#" + String.valueOf(isFirst);
	}

	/**
	 * Returns the list of tasks attached to a given resource
	 * @param resourceId the resource ID
	 * @return the list of tasks and their positions as pairs ID-POS
	 * separated by |
	 * <port id="o1">
	 * <taskId>ID</taskId><position>pos</position>
	 * [...]
	 * </port>
	 * <port id="o2">resourceId</port>
	 * @throws Exception
	 */
	public String getResourceTasks(String resourceId) throws Exception {
		DbResource db = DbResource.getDb();
		Vector<String> data = db.getTasksByResource(
				Integer.parseInt(resourceId));
		
		
		String position, taskId;
		StringBuilder sb = new StringBuilder();
		for (String s : data) {
			taskId = s.split("-")[0];
			position = s.split("-")[1];
			sb.append(taskId);
			sb.append("-");
			sb.append(position);
			sb.append("|");			
		}
		return sb.toString() + "#" + resourceId;
	}
	
	/**
	 * Computes the Estimated Completion Time of a task on a resource 
	 * @param taskId the task ID
	 * @param eet the task's Estimated Execution Time on the old resource
	 * @param wt the task's Waiting Time on this resource
	 * @param resourceId the resource ID this resource's ID
	 * @param positionInQueue the position in this resource's queue
	 * @return
	 * <port id="o1">taskID</port>
	 * <port id="o2">resourceID</port>
	 * <port id="o3">positionInQueue</port>
	 * <port id="04">EET</port>
	 * <port id="o5">ECT</port>
	 * @throws Exception 
	 */
	public String computeECT(String taskId, String eet, String wt, 
			String resourceId,
			String positionInQueue) throws Exception {
		
		positionInQueue = positionInQueue.trim().length() == 0 ? "0" : positionInQueue;
		wt = wt.trim().length() == 0 ? "0" : wt;
		
		DbResource db = DbResource.getDb();
		DbResource.Resource resource = db.getResource(Integer.parseInt(resourceId));
		
		// if we found no resource return "-1"
		if (resource == null) {
			resourceId = "-1";
		}
				
		if (!resource.isAvailable()) {
			return taskId + "#" + resourceId + "#" + positionInQueue + "#" +
			 Long.MAX_VALUE + "#" + Long.MAX_VALUE;
		}


		//TODO more realistic EET computation based on existing EET needed 
		long newEet = (long)(Math.random() * 100);
		return taskId + "#" + resourceId + "#" + positionInQueue + "#" +
			 newEet + "#" + (wt+newEet);
	}
	
	/**
	 * Assigns a task to a resource queue
	 * @param taskId the task ID
	 * @param resourceId the resource ID
	 * @param positionInQueue the position in the queue
	 * @return true if the assignment was completed, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception 
	 */
	public String assignResource(String taskId, 
						String resourceId, 
						String positionInQueue) throws Exception {
		DbResource db = DbResource.getDb();
		boolean isAssigned = db.assignTaskToResource(Integer.parseInt(taskId), 
								Integer.parseInt(resourceId),
								Integer.parseInt(positionInQueue));
		return String.valueOf(isAssigned);
	}
	
	/**
	 * Retrieves a resource ID by URI
	 * @param uri the resource URI
	 * @return 
	 * <port id="o1">resourceID|-1</port>
	 * @throws Exception
	 */
	public String getResourceIdByURI(String uri) throws Exception {
		DbResource db = DbResource.getDb();
		
		return Integer.toString(db.getResourceIdByURI(uri));
	}
}