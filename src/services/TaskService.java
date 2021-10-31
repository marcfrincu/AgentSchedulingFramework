package services;
import java.util.Vector;

import services.utils.DbTask;
import services.utils.DbTask.TASK_STATUS;

/**
 * Service responsible for task related operations
 * @author Marc Frincu
 * @since 2010
 *
 */
public class TaskService {

	/**
	 * @param agentId the agent ID
	 * @param description the task description
	 * @param executableLocation the executable/archive location
	 * @param dependencies the library location
	 * @param uuid the task UUID
	 * @param status the task status as a string. Should match the <i>DbTask.TASK_STATUS</i>
	 * @param submiter_agent_uuid the submitting agent ID
	 * @return the taskId or -1 in case of errors while inserting the task
	 * @throws Exception
	 */	
	public String addTask(String agentId,
				String description,
				String executableLocation,
				String dependencies,/*
				int assigned_resource_id,
				int position_on_resource_queue,
				long submission_time_local,
				long submission_time_total,
				long estimated_execution_time_on_resource,
				long estimated_completion_time_on_resource,
				boolean locked*/
				String uuid,
				String status,
				String submiter_agent_uuid) throws Exception {
		DbTask db = DbTask.getDb();
		int taskId = db.addTask(Integer.parseInt(agentId),
					description, 
					executableLocation, 
					dependencies, 
					uuid, 
					TASK_STATUS.valueOf(status), 
					submiter_agent_uuid);
		return String.valueOf(taskId);
	}
	
	/**
	 * Returns the first task from the list of tasks assigned to an agent
	 * @param agentId
	 * @return the task ID or -1 in case of error or of no tasks assigned to the agent
	 * <port id="o1">taskID|-1</port>
	 * @throws Exception
	 */
	public String getFirstTaskId(String agentId) throws Exception {
		DbTask db = DbTask.getDb();
		Vector<DbTask.Task> tasks = db.getTasks(Integer.parseInt(agentId));
		if (tasks != null) {
			if (tasks.size() == 0) {
				return "-1";
			}
			else
				return String.valueOf(tasks.get(0).getId());
		}
		return "-1";
	}
	
	
	/**
	 * Returns information on the given task ID
	 * @param taskId the task ID
	 * @return the information on the task or "-1" in case of non-existing task
	 * <port id="o1">taskID</port> 
	 * @throws Exception 
	 */
	public String getTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		DbTask.Task task = db.getTask(Integer.parseInt(taskId));
		// if we found no task return "-1"
		if (task == null) {
			return "-1";
		}
		return String.valueOf(task.getId());
	}

	/**
	 * Returns the ID of the task following the given task ID
	 * @param taskId the task ID
	 * @param agentId the ID of the agent handling this task
	 * @return the next task ID in a circular queue manner and whether 
	 * it is first or not
	 * <port id="o1">nextTaskID</port>
	 * <port id="o2">true|false</port>
	 * @throws Exception 
	 */
	public String getNextTaskId(String taskId, String agentId) throws Exception {
		DbTask db = DbTask.getDb();
		int id = db.getNextTaskId(Integer.parseInt(taskId),
									Integer.parseInt(agentId));
		boolean isFirst = false;
		
		if (taskId.compareTo("-1") == 0) {
			return taskId + "#" + agentId;
		}
		
		// skip tasks with certain statuses
		while ((isFirst = db.isFirst(id)) == false &&
				(db.getStatus(id).compareTo(DbTask.TASK_STATUS.RESOLVED.toString()) == 0 ||
					db.getStatus(id).compareTo(DbTask.TASK_STATUS.LOCKED_ASSIGNED.toString()) == 0 ||
					db.getStatus(id).compareTo(DbTask.TASK_STATUS.LOCKED_SUBMITTED.toString()) == 0 ||
					db.getStatus(id).compareTo(DbTask.TASK_STATUS.LOCKED_ASSIGNED_LOCAL.toString()) == 0 ||
					db.getStatus(id).compareTo(DbTask.TASK_STATUS.LOCKED_SUBMITTED_LOCAL.toString()) == 0 ||
					db.getStatus(id).compareTo(DbTask.TASK_STATUS.LOCKED_EXECUTING.toString()) == 0
				)) {
			System.out.println(String.valueOf(id) + "#" + String.valueOf(isFirst));
			id = db.getNextTaskId(id, Integer.parseInt(agentId));
		}
		
		return String.valueOf(id) + "#" + String.valueOf(isFirst);
	}
	
	/**
	 * Checks whether a task is assigned or not to a resource
	 * @param taskId the task ID
	 * @return the task ID, whether it is the first or not and whether 
	 * it is assigned or not
	 * <port id="o1">taskID</port>
	 * <port id="o2">true|false</port>
	 * <port id="o3">true|false</port>
	 * @throws Exception 
	 */
	public String checkAssignedTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		DbTask.Task task = db.getTask(Integer.parseInt(taskId));
		//TODO check null
		boolean isFirst = db.isFirst(Integer.parseInt(taskId));
		boolean isAssigned = (task.getAssigned_resource_id() == -1) ? true : false;
	
		return task.getId() + "#" + isFirst + "#" + isAssigned;
	}

	/**
	 * Unassigns a task from a resource
	 * @param taskId the task ID
	 * @return the task ID
	 * <port id="o1">taskID</port>
	 * @throws Exception 
	 */
	public String unassignTask(String taskId) throws Exception {
		throw new UnsupportedOperationException(
				"Method unassignTask is not yet implemented");
	}

	/**
	 * Checks whether a certain task must be relocated or not
	 * @param taskId the task ID
	 * @return
	 * <port id="o1">taskID</port>
	 * <port id="o2">true|false</port> can move?
	 * <port id="o3">assignedResourceID</port>
	 * <port id="o4">EET</port>
	 * <port id="o5">ECT</port>
	 * <port id="o6">TWT</port>
	 * <port id="o7">info</port>
	 * <port id="o8">UUID</port>
	 * <port id="o9">true|false</port> is temporary?
	 * <port id="o10">submiterAgentUuid</port>
	 * <port id="o11">originalAgentUuid</port>
	 * <port id="o12">executable</port>
	 * <port id="o13">dependencies</port>
	 * @throws Exception 
	 */
	public String isTimeToMove(String taskId) throws Exception {
		final long crtTime = System.currentTimeMillis();
		//TODO: change the condition
		final long treshold = 10000;
		
		DbTask db = DbTask.getDb();
		DbTask.Task task = db.getTask(Integer.parseInt(taskId));
		
		//TODO check null
		boolean isTimeToMove = false; 
		if (crtTime - task.getSubmission_time_local() > treshold) {
			isTimeToMove = true;
		}
		
		boolean isTemporary = false;
		if (task.getStatus().compareTo(DbTask.TASK_STATUS.SUBMITTED.toString()) == 0 ||
			task.getStatus().compareTo(DbTask.TASK_STATUS.LOCKED_SUBMITTED.toString()) == 0 ||
			task.getStatus().compareTo(DbTask.TASK_STATUS.LOCKED_SUBMITTED_LOCAL.toString()) == 0
			) {
			isTemporary = true;
		}
		
		if (task.getStatus().compareTo(DbTask.TASK_STATUS.LOCKED_EXECUTING.toString()) == 0) {
			isTimeToMove = false;
			isTemporary = false;
		}
		
		return task.getId() + "#" + 
				isTimeToMove + "#" + 
				task.getAssigned_resource_id() + "#" +
				task.getEstimated_execution_time_on_resource() + "#" +
				task.getEstimated_completion_time_on_resource() + "#" +
				task.getSubmission_time_total() + "#" +
				task.getDescription() + "#" +
				task.getUuid() + "#" + 
				isTemporary + "#" +
				task.getSubmiterAgentUuid() + "#" + 
				task.getOriginal_agent_id() + "#" + 
				task.getExecutable_location() + "#" + 
				task.getDependencies() + " #";
	}
	
	/**
	 * Computes the position of a task on a queue based on its total waiting time 
	 * @param taskId
	 * @param taskPosList
	 * @return
	 * <port id="o1">tasId</port>
	 * <port id="o2">current EET</port>
	 * <port id="o3">waiting time on new queue</port>
	 * <port id="o4">assigned position</port>
	 * @throws Exception
	 */
	public String getPosition(String taskId, String taskPosList) throws Exception {
		String[] taskPosElem = taskPosList.split("|");
		DbTask db = DbTask.getDb();
		DbTask.Task task = null, ourTask = null;
		int t = 0/*, p = 0*/;
		
		ourTask = db.getTask(Integer.parseInt(taskId));
		if (ourTask == null) {
			return "-1";
		}
		
		long wt = 0;
		int pos = 0;
		boolean error;
		for (int i=0;i<taskPosElem.length; i++) {
			error = false;
			try {
				t = Integer.parseInt(taskPosElem[i].split("-")[0]);
				//p = Integer.parseInt(taskPosElem[i].split("-")[1]);
			}
			catch(Exception e) {
				error = true;
			}
			if (error) {
				task = db.getTask(t);
				if (task != null) {
					// we found a position
					if (task.getSubmission_time_total() 
							> ourTask.getSubmission_time_total()) {
						return ourTask.getId() + "#" +
						ourTask.getEstimated_execution_time_on_resource() + "#" +
						wt + "#" +
						task.getPosition_on_resource_queue();
					}
					else {
						wt += task.getEstimated_execution_time_on_resource();
					}
					pos = task.getPosition_on_resource_queue();
				}
			}
			
		}
		
		return ourTask.getId() + "#" +
		ourTask.getEstimated_execution_time_on_resource() + "#" +
		wt + "#" +
		(pos + 1);
	}
	
	/**
	 * Sets a task to be solved by a certain resource if the provided ECT 
	 * is smaller then the existing one
	 * @param taskId
	 * @param resourceId
	 * @param positionInQueue
	 * @param ect
	 * @param eet
	 * @return the task ID, the newly allocated resource ID, the position
	 * in the new queue and the former resource ID
	 * <port id="o1">taskID</port>
	 * <port id="o2">newResourceID</port>
	 * <port id="o3">positionInQueue</port>
	 * <port id="o4">formerResourceID</port>
	 * <port id="o5">ECT</port>
	 * <port if="o6">taskUuid</port>
	 * @throws Exception 
	 */
	public String assignMin(String taskId, 
					String resourceId, 
					String positionInQueue, 
					String ect, 
					String eet) throws Exception {
		
		DbTask db = DbTask.getDb();
		DbTask.Task task = db.getTask(Integer.parseInt(taskId));
		//TODO check null
		// if ECT and EET are null do not update simply return available info
		if (ect == null && eet == null) {
			return task.getId() + "#" +
			task.getAssigned_resource_id() + "#" +
			task.getPosition_on_resource_queue() + "#" +
			task.getAssigned_resource_id() + "#" +
			task.getEstimated_completion_time_on_resource() + "#" +
			task.getUuid();

		}
		System.out.println(taskId + " " + resourceId + " " + positionInQueue + " " + ect + " " + eet);
		boolean updated = db.assignMin(Integer.parseInt(taskId),
					Integer.parseInt(resourceId),
					Integer.parseInt(positionInQueue),
					Long.parseLong(ect),
					Long.parseLong(eet));
		if (updated) {
			return task.getId() + "#" +
				resourceId + "#" + 
				positionInQueue + "#" +
				task.getAssigned_resource_id() + "#" +
				ect + "#" +
				task.getUuid();
		}
		else
		return task.getId() + "#" +
				task.getAssigned_resource_id() + "#" +
				task.getPosition_on_resource_queue() + "#" +
				task.getAssigned_resource_id() + "#" +
				task.getEstimated_completion_time_on_resource() + "#" +
				task.getUuid();
	}
	
	/**
	 * Locks a task
	 * @param taskId
	 * @return true if the lock succeeded, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception 
	 */
	public String lockTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		boolean locked = db.lockTask(Integer.parseInt(taskId));
		
		return String.valueOf(locked);
	}

	/**
	 * Unlocks a task
	 * @param taskId
	 * @return true if the unlock succeeded, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception 
	 */
	public String unlockTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		boolean locked = db.unlockTask(Integer.parseInt(taskId));
		
		return String.valueOf(locked);
		
	}
	
	/**
	 * Locks a task at local level
	 * @param taskId
	 * @return true if the lock succeeded, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception 
	 */
	public String localLockTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		boolean locked = db.localLockTask(Integer.parseInt(taskId));
		
		return String.valueOf(locked);
	}

	/**
	 * Unlocks a task at local level
	 * @param taskId
	 * @return true if the unlock succeeded, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception 
	 */
	public String localUnlockTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		boolean locked = db.localUnlockTask(Integer.parseInt(taskId));
		
		return String.valueOf(locked);
		
	}
	
	/**
	 * Removes a temporary task
	 * @param taskId
	 * @return true if the operation succeeded, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception
	 */
	public String deleteTemporaryTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		boolean removed = db.deleteTemporaryTask(Integer.parseInt(taskId));
		
		return String.valueOf(removed);
	}
	
	/**
	 * Removes a task
	 * @param taskId
	 * @return true if the operation succeeded, false otherwise
	 * <port id="o1">true|false</port>
	 * @throws Exception
	 */
	public String deleteTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		boolean removed = db.deleteTask(Integer.parseInt(taskId));
		
		return String.valueOf(removed);
	}
	
	/**
	 * Packs a task's info used required when negotiating
	 * @param taskUuid
	 * @param EET
	 * @param ECT
	 * @param TWT
	 * @param info
	 * @return
	 * <port id="o1">taskUuid-EET-ECT-TWT-info</port>
	 */
	public String packTask(String taskUuid,
			String EET,
			String ECT,
			String TWT,
			String info) {
		StringBuilder sb = new StringBuilder();
		sb.append(taskUuid);
		sb.append("@");
		sb.append(EET);
		sb.append("@");
		sb.append(ECT);
		sb.append("@");
		sb.append(TWT);
		sb.append("@");
		sb.append(info);
		return sb.toString() + "#" + taskUuid;
	}
	
	/**
	 * Returns task information based on its UUID
	 * @param taskUuid
	 * @return
	 * <port id="o1">taskId</port>
	 * @throws Exception 
	 */
	public String getTaskByUuid(String taskUuid) throws Exception {
		DbTask db = DbTask.getDb();
		DbTask.Task task = db.getTaskIdByUUID(taskUuid);
		if (task == null) {
			return "-1";
		}
		return String.valueOf(task.getId());
	}
	
	/**
	 * Sets the status of a task to RESOLVED
	 * @param taskId
	 * @return 
	 * <port id="o1">taskId</port>
	 * @throws Exception
	 */
	public String resolveTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		
		db.setStatus(Integer.parseInt(taskId), TASK_STATUS.RESOLVED);
		
		return taskId;
	}
	
	/**
	 * Marks a task for execution
	 * @param taskId the task ID
	 * @return 
	 * <port id="o1">taskId</port>
	 * @throws Exception
	 */
	public String executeTask(String taskId) throws Exception {
		DbTask db = DbTask.getDb();
		
		db.setStatus(Integer.parseInt(taskId), TASK_STATUS.LOCKED_EXECUTING);
		
		return taskId;
	}
	
	/**
	 * Sets the status of a task to ASSIGNED
	 * @param taskId
	 * @param agentId
	 * @return
	 * <port id="o1">taskId</port> 
	 * @throws Exception
	 */
	public String assignTask(String taskId, String agentId) throws Exception {
		DbTask db = DbTask.getDb();
		
		db.setStatus(Integer.parseInt(taskId), TASK_STATUS.ASSIGNED);
		db.changeTaskAgent(Integer.parseInt(taskId), Integer.parseInt(agentId));
		
		return taskId;		
	}
	
	/**
	 * Sorts the tasks descending by Estimated Execution Times
	 * and returns the ID of the first task in the list
	 * @param agentId the ID of the agent handling the tasks to be returned 
	 * @return
	 * <port id="o1">taskId</port>
	 * <port id="o2">resourceId</port>
	 * <port id="o3">positionInResourceQueue</port>
	 * @throws Exception
	 */
	public String sortTasksDescendingByEET(String agentId) throws Exception {
		DbTask db = DbTask.getDb();
		
		DbTask.Task task = db.getTasksOrderedByEET(Integer.parseInt(agentId), false);
		
		return task == null ? "-1" : task.getId() + "#" + task.getAssigned_resource_id() + "#" + 
									task.getPosition_on_resource_queue();
	}

	/**
	 * Retrieves the first assigned task from the resource queue
	 * and returns the ID of the first task in the list
	 * @param agentId the ID of the agent handling the tasks to be returned
	 * @param resourceId the ID of the resource 
	 * @return
	 * <port id="o1">taskId</port>
	 * <port id="o3">positionInResourceQueue</port>
	 * @throws Exception
	 */
	public String getFirstTaskInResourceQueue(String agentId, String resourceId) throws Exception {
		DbTask db = DbTask.getDb();
		
		DbTask.Task task = db.getFirstTaskOrderedByPositionInQueue(Integer.parseInt(agentId), 
															Integer.parseInt(resourceId));
		
		return task == null ? "-1" : task.getId() + "#" + 
									task.getPosition_on_resource_queue();
	}
	
	
	/**
	 * Checks whether there are any unlocked tasks left
	 * @param agentId the ID of the agent handling the tasks
	 * @return
	 * <port id="o1">number of unlocked tasks</port>
	 * @throws Exception
	 */
	public String checkAllTasksAreLocked(String agentId) throws Exception {
		DbTask db = DbTask.getDb();

		return db.countNotLockedTasks(Integer.parseInt(agentId)) + "";
	}
}

