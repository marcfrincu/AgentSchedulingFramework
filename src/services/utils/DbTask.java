package services.utils;

import java.sql.ResultSet;
import java.util.Vector;

import settings.SystemSettings;

/**
 * Handles task specific operations on the database
 * @author Marc Frincu
 * @since 2010
 *
 */
//TODO: use stored procedures
public class DbTask extends Db {

	private static DbTask db = null;

	public static enum TASK_STATUS {SUBMITTED, /* received from the negotiator */
									RESOLVED, /* sent back to the negotiator */
									ASSIGNED, /* assigned to a resource */
									LOCKED_SUBMITTED, /* 
														in use by the scheduler as a result 
														of a negotiation bid request 
													*/
									LOCKED_ASSIGNED, /* in use by the scheduler */
									LOCKED_SUBMITTED_LOCAL,
									LOCKED_ASSIGNED_LOCAL,
									LOCKED_EXECUTING
									}; 
	
	protected DbTask(String database, String username, String password)
			throws Exception {
		super(database, username, password);
	}
	
	public static DbTask getDb() throws Exception {
		if (DbTask.db == null) {
			DbTask.db = new DbTask(SystemSettings.getSystemSettings().getDb_name(),
					SystemSettings.getSystemSettings().getDb_user(),
					SystemSettings.getSystemSettings().getDb_password());
			return DbTask.db;
		}
		else
			return DbTask.db;
	}
	
	/**
	 * Adds a new task in the database
	 * @param original_agent_id
	 * @param description
	 * @param executable_location
	 * @param dependencies
	 * @return the ID of the newly introduced task or -1 in case of failure
	 */
	public int addTask(/*int id,*/
			int original_agent_id,
			String description,
			String executable_location,
			String dependencies,/*
			int assigned_resource_id,
			int position_on_resource_queue,
			long submission_time_local,
			long submission_time_total,
			long estimated_execution_time_on_resource,
			long estimated_completion_time_on_resource,
			boolean locked*/
			String uuid,
			TASK_STATUS status,
			String submiter_agent_uuid
			) {
		final long crtTime = System.currentTimeMillis();
		try {
			this.executeStatement(
					"INSERT INTO task (" +
					"original_agent_id, " +
					"description, " +
					"executable_location, " +
					"dependencies, " +
					"submission_time_local, " +
					"submission_time_total," +
					"uuid," +
					"status," +
					"submiter_agent_uuid)" +
					" VALUES (" + 
					original_agent_id + ", " +
					"\'" + description + "\', " +
					"\'" + executable_location + "\', " +
					"\'" + dependencies + "\', " +
					crtTime + ", " +
					crtTime + ", " +
					"\'" + uuid + "\', " +
					"\'" + status.toString() + "\', " +
					"\'" + submiter_agent_uuid + "\'" +
					");"
					);
			String id = this.getFirst("SELECT max(id) FROM task;");
			this.executeStatement(
					"INSERT INTO task_handling_agent (" +
					"task_id, agent_id)" + 
					" VALUES (" + Integer.parseInt(id) + 
					"," + original_agent_id +
					");");
			return Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Changes the agent handling a task
	 * @param taskId
	 * @param agentId
	 * @return true if the modification succeeded, false otherwise
	 */
	public boolean changeTaskAgent(int taskId,
								int agentId) {
		try {
			this.executeStatement(
					"UPDATE task_handling_agent SET agent_id=" +
					agentId + " WHERE task_id=" + taskId + ";"
					);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Returns data on a given task
	 * @param taskId
	 * @return a <i>DbTask.Task</i> object or null in case of failure
	 */
	public DbTask.Task getTask(int taskId) {

		try {
			DbTask.Task task = null;
			final ResultSet rs = this.getQuery(
					"SELECT id, original_agent_id, description, " +
					"executable_location, dependencies, assigned_resource_id, " +
					"position_on_resource_queue, submission_time_local, " +
					"submission_time_total, estimated_execution_time_on_resource, " +
					"estimated_completion_time_on_resource, uuid, status," +
					"submiter_agent_uuid " +
					"FROM task WHERE id=" + taskId + ";");
			if (rs.next()) {
				task = new DbTask.Task(
						rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getLong(8),
						rs.getLong(9),
						rs.getLong(10),
						rs.getLong(11),
						rs.getString(12),
						rs.getString(13),
						rs.getString(14)
				);
				
			}
			return task;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns all the tasks stored in the database.
	 * Only the tasks not locked are returned
	 * @return the task vector or null in case of failure
	 */
	public Vector<DbTask.Task> getTasks(int agentId) {
		Vector<DbTask.Task> tasks = new Vector<DbTask.Task>();
		try {
			DbTask.Task task = null;
			final ResultSet rs = this.getQuery(
					"SELECT t.id, t.original_agent_id, t.description, " +
					"t.executable_location, t.dependencies, t.assigned_resource_id, " +
					"t.position_on_resource_queue, t.submission_time_local, " +
					"t.submission_time_total, t.estimated_execution_time_on_resource, " +
					"t.estimated_completion_time_on_resource, t.uuid, t.status," +
					"t.submiter_agent_uuid " +
					"FROM task t, task_handling_agent a WHERE " +
					" (status='" + TASK_STATUS.ASSIGNED.toString() + "" +
					"' OR status='" + TASK_STATUS.SUBMITTED.toString() + "') AND " +
					"t.id=a.task_id AND a.agent_id=" + agentId +
					" ORDER BY t.id ASC;");
			while (rs.next()) {
				task = new DbTask.Task(
						rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getLong(8),
						rs.getLong(9),
						rs.getLong(10),
						rs.getLong(11),
						rs.getString(12),
						rs.getString(13),
						rs.getString(14)
						);
				tasks.add(task);
			}
			return tasks;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the first task in a list of tasks ordered by Estimated
	 * Execution Times.
	 * Only the tasks not locked are returned
	 * @param agentId
	 * @param ascending true if the tasks are to be sorted ascending, false otherwise
	 * @return the task vector or null in case of failure
	 */
	public DbTask.Task getTasksOrderedByEET(int agentId, boolean ascending) {
		Vector<DbTask.Task> tasks = new Vector<DbTask.Task>();
		try {
			DbTask.Task task = null;
			final ResultSet rs = this.getQuery(
					"SELECT t.id, t.original_agent_id, t.description, " +
					"t.executable_location, t.dependencies, t.assigned_resource_id, " +
					"t.position_on_resource_queue, t.submission_time_local, " +
					"t.submission_time_total, t.estimated_execution_time_on_resource, " +
					"t.estimated_completion_time_on_resource, t.uuid, t.status," +
					"t.submiter_agent_uuid " +
					"FROM task t, task_handling_agent a WHERE " +
					" (status='" + TASK_STATUS.ASSIGNED.toString() + "" +
					"' OR status='" + TASK_STATUS.SUBMITTED.toString() + "') AND " +
					"t.id=a.task_id AND a.agent_id=" + agentId +
					" ORDER BY t.estimated_execution_time_on_resource ASC;");
			while (rs.next()) {
				task = new DbTask.Task(
						rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getLong(8),
						rs.getLong(9),
						rs.getLong(10),
						rs.getLong(11),
						rs.getString(12),
						rs.getString(13),
						rs.getString(14)
						);
				tasks.add(task);
			}
			return tasks.size() == 0 ? null : tasks.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the first task in a list of tasks ordered ascending by their positions in queue
	 * Only the first task not locked and assigned is returned 
	 * @param agentId
	 * @param resourceId
	 * @return the task or null in case of failure
	 */
	public DbTask.Task getFirstTaskOrderedByPositionInQueue(int agentId, int resourceId) {
		Vector<DbTask.Task> tasks = new Vector<DbTask.Task>();
		try {
			DbTask.Task task = null;
			final ResultSet rs = this.getQuery(
					"SELECT t.id, t.original_agent_id, t.description, " +
					"t.executable_location, t.dependencies, t.assigned_resource_id, " +
					"t.position_on_resource_queue, t.submission_time_local, " +
					"t.submission_time_total, t.estimated_execution_time_on_resource, " +
					"t.estimated_completion_time_on_resource, t.uuid, t.status," +
					"t.submiter_agent_uuid " +
					"FROM task t, task_handling_agent a WHERE " +
					" (status='" + TASK_STATUS.ASSIGNED.toString() + "" +
					"') AND " +
					"t.id=a.task_id AND a.agent_id=" + agentId +
					" AND t.assigned_resource_id=" + resourceId + 
					" ORDER BY t.position_on_resource_queue ASC;");
			while (rs.next()) {
				task = new DbTask.Task(
						rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getLong(8),
						rs.getLong(9),
						rs.getLong(10),
						rs.getLong(11),
						rs.getString(12),
						rs.getString(13),
						rs.getString(14)
						);
				tasks.add(task);
			}
			return tasks.size() == 0 ? null : tasks.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a task given its UUID
	 * @return the Task data 
	 */
	public DbTask.Task getTaskIdByUUID(String uuid) {
		try {
			DbTask.Task task = null;
			final ResultSet rs = this.getQuery(
					"SELECT id, original_agent_id, description, " +
					"executable_location, dependencies, assigned_resource_id, " +
					"position_on_resource_queue, submission_time_local, " +
					"submission_time_total, estimated_execution_time_on_resource, " +
					"estimated_completion_time_on_resource, uuid, status," +
					"submiter_agent_uuid " +
					"FROM task WHERE uuid='" + uuid + "';");
			if (rs.next()) {
				task = new DbTask.Task(
						rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getLong(8),
						rs.getLong(9),
						rs.getLong(10),
						rs.getLong(11),
						rs.getString(12),
						rs.getString(13),
						rs.getString(14)
				);
				
			}
			return task;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Removes a temporary task from the database
	 * @param taskId
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean deleteTemporaryTask(int taskId) {
		try {
			String result = this.getFirst("SELECT count(*) FROM task " +
					" WHERE status='SUBMITTED' AND id=" + taskId + ";");
			if (Integer.parseInt(result) == 0) {
				return false;
			}
			this.executeStatement("DELETE FROM task WHERE status='" + 
					TASK_STATUS.SUBMITTED.toString() + "' " +
					" AND id=" + taskId + ";");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Removes a task from the database
	 * @param taskId
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean deleteTask(int taskId) {
		try {
			String result = this.getFirst("SELECT count(*) FROM task " +
					" WHERE id=" + taskId + ";");
			if (Integer.parseInt(result) == 0) {
				return false;
			}
			this.executeStatement("DELETE FROM task WHERE " +
					" id=" + taskId + ";");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns information on a task based on the agent handling it
	 * @param agentId
	 * @return a <i>DbTask.Task</i> object or null in case of failure
	 */
	public DbTask.Task getTaskByAgent1(int agentId) {
		try {
			DbTask.Task task = null;
			final ResultSet rs = this.getQuery(
					"SELECT a.id, a.original_agent_id, a.description, " +
					"a.executable_location, a.dependencies, a.assigned_resource_id, " +
					"a.position_on_resource_queue, a.submission_time_local, " +
					"a.submission_time_total, a.estimated_execution_time_on_resource, " +
					"a.estimated_completion_time_on_resource, " +
					"a.uuid, a.status, a.submiter_agent_uuid " +
					"FROM task a, task_handling_agent b WHERE b.agent_id=" + agentId + 
					" AND a.id=b.task_id;");
			if (rs.next()) {
				task = new DbTask.Task(
						rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getInt(6),
						rs.getInt(7),
						rs.getLong(8),
						rs.getLong(9),
						rs.getLong(10),
						rs.getLong(11),
						rs.getString(12),
						rs.getString(13),
						rs.getString(14)
						);
				
			}
			return task;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Checks whether a task is the first one or not in the list.
	 * In order to make this operation tasks are ordered by their IDs
	 * @param taskId
	 * @return true if the task is the first in the list, false otherwise (errors included)
	 */
	public boolean isFirst(int taskId) {
		try {
			final ResultSet rs = this.getQuery(
					"SELECT id " +
					"FROM task ORDER BY id ASC;");
			int i = 0;
			while (rs.next()) {
				if (taskId == rs.getInt(1) && i == 0) {
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
	 * Returns the next task ID using a circular queue approach
	 * @param taskId
	 * @param agentId
	 * @return the next task's ID or -1 in case of error (taskId not found
	 * and no tasks in list included)
	 */
	public int getNextTaskId(int taskId, int agentId) {
		try {
			final ResultSet rs = this.getQuery(
					" SELECT t.id, t.status " +
					" FROM task t, task_handling_agent a " +
					" WHERE a.agent_id=" + agentId +
					" AND t.id=a.task_id " +
					" ORDER BY t.id ASC;");
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
	 * Assigns a task to the given resource if and only if
	 * the new resource is better than the current one
	 * @param taskId
	 * @param resourceId
	 * @param positionInQueue
	 * @param ect
	 * @param eet
	 * @return true if the assignment succeeded, false otherwise
	 */
	public boolean assignMin(int taskId, 
			int resourceId, 
			int positionInQueue, 
			long ect, 
			long eet) {
		
		try {
			final ResultSet rs = this.getQuery(
					"SELECT assigned_resource_id, " +
					"position_on_resource_queue, submission_time_local, " +
					"estimated_execution_time_on_resource, " +
					"estimated_completion_time_on_resource " +
					"FROM task WHERE id=" + taskId + ";");
			if (rs.next()) {
				// if the ECT is better or the task is unassigned
				if (ect < rs.getLong(5) || 
						(rs.getInt(1) == 0 && rs.getInt(2) == 0)) {
					this.executeStatement(
							"UPDATE task SET assigned_resource_id=" + resourceId +
							", position_on_resource_queue=" + positionInQueue + 
							", estimated_execution_time_on_resource=" + eet +
							", estimated_completion_time_on_resource=" + ect +
							" WHERE id=" + taskId + ";"
							);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unassigns a task from a resource by settings the resource ID
	 * and the position on the queue to the default 0
	 * @param taskId
	 * @return true if the un-assignment succeeded, false otherwise
	 */
	public boolean unassignTask(String taskId) {
		try {
			this.executeStatement("UPDATE task SET assigned_resource_id=0," +
					"position_on_resource_queue=0 WHERE id=" + taskId + ";");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Locks a task in use
	 * @param taskId
	 * @return true if the lock succeeded, false otherwise
	 */
	public boolean lockTask(int taskId) {
		try {
			String res = this.getFirst("SELECT status from task WHERE id=" + taskId);
			
			String status = TASK_STATUS.LOCKED_SUBMITTED.toString();
			if (res.compareTo(TASK_STATUS.ASSIGNED.toString()) == 0) {
				status = TASK_STATUS.LOCKED_ASSIGNED.toString();
			}
				
			this.executeStatement(
					"UPDATE task SET status='" + status + "'" +
					" WHERE id=" + taskId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unlocks a task in use
	 * @param taskId
	 * @return true if the unlock succeeded, false otherwise
	 */
	public boolean unlockTask(int taskId) {
		try {
			String res = this.getFirst("SELECT status from task WHERE id=" + taskId);
			
			String status = TASK_STATUS.SUBMITTED.toString();
			if (res.compareTo(TASK_STATUS.LOCKED_ASSIGNED.toString()) == 0) {
				status = TASK_STATUS.ASSIGNED.toString();
			}
			this.executeStatement(
					"UPDATE task SET status='" + status + "'" +
					" WHERE id=" + taskId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}
	
	/**
	 * Locks a task in use at local level only
	 * @param taskId
	 * @return true if the lock succeeded, false otherwise
	 */
	public boolean localLockTask(int taskId) {
		try {
			String res = this.getFirst("SELECT status from task WHERE id=" + taskId);
			
			String status = TASK_STATUS.LOCKED_SUBMITTED_LOCAL.toString();
			if (res.compareTo(TASK_STATUS.LOCKED_ASSIGNED.toString()) == 0) {
				status = TASK_STATUS.LOCKED_ASSIGNED_LOCAL.toString();
			}
				
			this.executeStatement(
					"UPDATE task SET status='" + status + "'" +
					" WHERE id=" + taskId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unlocks a task in use at local level
	 * @param taskId
	 * @return true if the unlock succeeded, false otherwise
	 */
	public boolean localUnlockTask(int taskId) {
		try {
			String res = this.getFirst("SELECT status from task WHERE id=" + taskId);
			
			String status = TASK_STATUS.LOCKED_SUBMITTED.toString();
			if (res.compareTo(TASK_STATUS.LOCKED_ASSIGNED_LOCAL.toString()) == 0) {
				status = TASK_STATUS.LOCKED_ASSIGNED.toString();
			}
			this.executeStatement(
					"UPDATE task SET status='" + status + "'" +
					" WHERE id=" + taskId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}
	
	/**
	 * Changes the status of a task
	 * @param taskId
	 * @param status
	 * @return true if the change was successful, false otherwise
	 */
	public boolean setStatus(int taskId, TASK_STATUS status) {
		try {
			this.executeStatement(
					"UPDATE task SET status='" + status.toString() + "'" +
					" WHERE id=" + taskId + ";"
					);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns the status of a given task
	 * @param taskId
	 * @return the task status or -1 in case of error
	 */
	public String getStatus(int taskId) {
		String res = "-1";
		try {
			res = this.getFirst("SELECT status FROM task WHERE id=" + taskId);
		
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return res;
	}
	
	/**
	 * Counts the number of not locked tasks
	 * @param agentId the ID of the agent handling the tasks
	 * @return the number of unlocked tasks
	 */
	public int countNotLockedTasks(int agentId) {
		int count = 0;
		
		try {
			count = Integer.parseInt(
						this.getFirst(
								"SELECT COUNT(*) FROM task t, task_handling_agent a" +
								" WHERE a.agent_id=" + agentId + " AND a.task_id=t.id AND " +
								" (t.status='SUBMITTED' OR t.status='RESOLVED' OR t.status='ASSIGNED');"
						)
					);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * Stores information corresponding to an item inside the 
	 * <i>task</i> table.
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class Task {
		//database information
		int id;
		int original_agent_id;
		String description;
		String executable_location;
		String dependencies;
		int assigned_resource_id;
		int position_on_resource_queue;
		long submission_time_local;
		long submission_time_total;
		long estimated_execution_time_on_resource;
		long estimated_completion_time_on_resource;
		String uuid;
		String status;
		String submiter_agent_uuid;
		
		public Task (int id,
					int original_agent_id,
					String description,
					String executable_location,
					String dependencies,
					int assigned_resource_id,
					int position_on_resource_queue,
					long submission_time_local,
					long submission_time_total,
					long estimated_execution_time_on_resource,
					long estimated_completion_time_on_resource,
					String uuid,
					String status,
					String submiter_agent_uuid
					) {
			this.id = id;
			this.original_agent_id = original_agent_id;
			this.description = description;
			this.executable_location = executable_location;
			this.dependencies = dependencies;
			this.assigned_resource_id = assigned_resource_id;
			this.position_on_resource_queue = position_on_resource_queue;
			this.submission_time_local = submission_time_local;
			this.submission_time_total = submission_time_total;
			this.estimated_execution_time_on_resource = estimated_execution_time_on_resource;
			this.estimated_completion_time_on_resource = estimated_completion_time_on_resource;
			this.status = status;
			this.uuid = uuid;
			this.submiter_agent_uuid = submiter_agent_uuid;
		}
		
		public int getId() {
			return this.id;
		}

		public int getOriginal_agent_id() {
			return this.original_agent_id;
		}

		public String getDescription() {
			return this.description;
		}

		public String getExecutable_location() {
			return this.executable_location;
		}

		public String getDependencies() {
			return this.dependencies;
		}

		public int getAssigned_resource_id() {
			return this.assigned_resource_id;
		}

		public int getPosition_on_resource_queue() {
			return this.position_on_resource_queue;
		}

		public long getSubmission_time_local() {
			return this.submission_time_local;
		}

		public long getSubmission_time_total() {
			return this.submission_time_total;
		}

		public long getEstimated_execution_time_on_resource() {
			return this.estimated_execution_time_on_resource;
		}

		public long getEstimated_completion_time_on_resource() {
			return this.estimated_completion_time_on_resource;
		}

		public String getStatus() {
			return this.status;
		}
		
		public String getUuid() {
			return this.uuid;
		}
		
		public String getSubmiterAgentUuid() {
			return this.submiter_agent_uuid;
		}
		
	}
	
	/**
	 * Stores information corresponding to an item inside the 
	 * <i>task_handling_agent</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class TaskHandlingAgent {
		//database information
		int id;
		int task_id;
		int agent_id;
		
		public TaskHandlingAgent(int id,
							int task_id,
							int agent_id) {
			this.id = id;
			this.task_id = task_id;
			this.agent_id = agent_id;
		}

		public int getId() {
			return this.id;
		}

		public int getTask_id() {
			return this.task_id;
		}

		public int getAgent_id() {
			return this.agent_id;
		}
	
		
	}
	
}
