package services.utils;

import java.sql.ResultSet;
import java.util.Vector;

import settings.SystemSettings;

/**
 * Handles resource specific operations on the database
 * @author Marc Frincu
 * @since 2010
 *
 */
//TODO: use stored procedures
public class DbResource extends Db {
	
	private static DbResource db = null;
	
	protected DbResource(String database, String username, String password)
			throws Exception {
		super(database, username, password);
	}
	
	public static DbResource getDb() throws Exception {
		if (DbResource.db == null) {
			DbResource.db = new DbResource(SystemSettings.getSystemSettings().getDb_name(),
					SystemSettings.getSystemSettings().getDb_user(),
					SystemSettings.getSystemSettings().getDb_password());
			return DbResource.db;
		}
		else
			return DbResource.db;
	}
	
	/**
	 * Adds a new resource to the database
	 * @param uri
	 * @param description
	 * @param available
	 * @param service_uri
	 * @return the resource ID or -1 in case of failure
	 */
	public int addResource(/*int id,*/
					String uri,
					String description,
					boolean available/*,
					boolean locked*/) {
		try {
			this.executeStatement(
					"INSERT INTO resource (" +
					"uri, " +
					"description, " +
					"available) " +
					" VALUES (" + 
					uri + ", " +
					"\'" + description + "\', " +
					"\'" + Boolean.valueOf(available).toString() + "\'" +
					");"
					);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}
	
	/**
	 * Returns data on a given resource
	 * @param resourceId
	 * @return a <i>DbResource.Resource</i> object or null in case of error
	 */
	public DbResource.Resource getResource(int resourceId) {
		try {
			DbResource.Resource resource = null;
			final ResultSet rs = this.getQuery(
					"SELECT id, uri, description, " +
					"available, locked " +
					"FROM resource WHERE id=" + resourceId + ";");
			if (rs.next()) {
				resource = new DbResource.Resource(
						rs.getInt(1),
						rs.getString(2),
						rs.getString(3),
						rs.getBoolean(4),
						rs.getBoolean(5)
						);
				
			}
			return resource;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the next resource ID using a circular queue approach
	 * @param resourceId
	 * @param agentId
	 * @return the next resource's ID or -1 in case of error (resourceId not found
	 * and no resources included)
	 */
	public int getNextResourceId(int resourceId, int agentId) {
		try {
			final ResultSet rs = this.getQuery(
					"SELECT id " +
					"FROM resource WHERE available=true AND locked=false AND agent_id=" +
					agentId	+
					" ORDER BY id ASC;");
			int i = 0, firstId = -1;
			
			while (rs.next()) {
				// if we are at the first entry store the ID of the first resource
				if (i == 0) {
					firstId = rs.getInt(1);
				}
				// if we found the resource
				if (resourceId == rs.getInt(1)) {
					// return the ID of the first resource if our task the last
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
	 * Returns all the resources stored in the database.
	 * Only the ones that are not locked and available is returned
	 * @param agentId
	 * @return the vector of resources or null in case of error
	 */
	public Vector<DbResource.Resource> getResources(int agentId) {
		Vector<DbResource.Resource> resources = new Vector<DbResource.Resource>();
		try {
			DbResource.Resource resource = null;
			final ResultSet rs = this.getQuery(
					"SELECT id, uri, description, " +
					"available, locked FROM resource " +
					"WHERE available=true AND locked=false AND agent_id=" + agentId + ";");
			while (rs.next()) {
				resource = new DbResource.Resource(
						rs.getInt(1),
						rs.getString(2),
						rs.getString(3),
						rs.getBoolean(4),
						rs.getBoolean(5)
						);
				resources.add(resource);
			}
			return resources;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a list of pairs <taskId,positionInQueue>
	 * that contains the task IDs belonging to a given resource 
	 * @param resourceId
	 * @return a hashtable or null in case of error
	 */
	public Vector<String> getTasksByResource(int resourceId) {
		Vector<String> data = new Vector<String>();
		
		try {
			final ResultSet rs = this.getQuery(
					"SELECT task_id, position_in_queue FROM resource_tasks" +
					" WHERE resource_id=" + resourceId + 
					" ORDER BY position_in_queue ASC;"
					);
			while (rs.next()) {
				data.add(rs.getInt(1)+"-"+rs.getInt(2));
			}
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * Returns the next task ID attached to this resource. 
	 * If the current task is the last one then -1 will be returned
	 * @param taskId
	 * @param resourceId
	 * @return the next task Id or -1 in case of error or no more tasks available
	 */
	public int getNextTaskIdAssignedToResource(int taskId, int resourceId) {
		try {
			final ResultSet rs = this.getQuery(
					" SELECT task_id " +
					" FROM resource_tasks " +
					" WHERE resource_id=" + resourceId +
					" ORDER BY position_in_queue ASC;");
			int i = 0, firstId = -1;
			
 			while (rs.next()) {
				// if we are at the first entry store the ID of the first task
				if (i == 0) {
					firstId = rs.getInt(1);
				}
				// if we found the task
				if (resourceId == rs.getInt(1)) {
					// return the ID of the first task if our task the last
					if (rs.isLast()) {
						return -1;
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
	 * Assigns a task to a resource on a given position
	 * @param taskId
	 * @param resourceId
	 * @param positionInQueue
	 * @return true if the assignment succeeded, false otherwise
	 */
	public boolean assignTaskToResource(int taskId, 
			int resourceId, 
			int positionInQueue) {
		try {
			this.executeStatement("DELETE FROM resource_tasks WHERE task_id=" + taskId + ";");
			this.executeStatement("INSERT INTO resource_tasks (task_id, " +
				"resource_id, position_in_queue) VALUES (" +
				taskId + "," + resourceId + "," + positionInQueue +
				");");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Locks a resource in use
	 * @param resourceId
	 * @return true if the lock succeeded, false otherwise
	 */
	public boolean lockResource(int resourceId) {
		try {
			this.executeStatement(
					"UPDATE resource SET locked=true" +
					" WHERE id=" + resourceId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unlocks a resource in use
	 * @param resourceId
	 * @return true if the unlock succeeded, false otherwise
	 */
	public boolean unlockResource(int resourceId) {
		try {
			this.executeStatement(
					"UPDATE resource SET locked=false" +
					" WHERE id=" + resourceId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}
	
	/**
	 * Makes a resource available
	 * @param resourceId
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean makeResourceAvailable(int resourceId) {
		try {
			this.executeStatement(
					"UPDATE resource SET available=true" +
					" WHERE id=" + resourceId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Makes a resource unavailable for use
	 * @param resourceId
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean makeResourceUnavailable(int resourceId) {
		try {
			this.executeStatement(
					"UPDATE resource SET available=false" +
					" WHERE id=" + resourceId + ";"
					);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}
	
	/**
	 * Checks whether a resource is the first one or not in the list.
	 * In order to make this operation resources are ordered by their IDs
	 * @param resourceId
	 * @return true if the resource is the first in the list, false otherwise (errors included)
	 */
	public boolean isFirst(int resourceId) {
		try {
			final ResultSet rs = this.getQuery(
					"SELECT id " +
					"FROM resource ORDER BY id ASC;");
			int i = 0;
			while (rs.next()) {
				if (resourceId == rs.getInt(1) && i == 0) {
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
	 * Retrieves a resource ID by URI
	 * @param uri the resource URI
	 * @return the resource ID or -1 in case of failure
	 */
	public int getResourceIdByURI(String uri) {
		try {
			final String result = this.getFirst(
					"SELECT id " +
					"FROM resource WHERE uri='" + uri + "';");
			if (result != null) {
				return Integer.parseInt(result);
			}
			else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Stores information corresponding to an item inside the <i>resource</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class Resource {
		//database information
		int id;
		String uri;
		String description;
		boolean available;
		boolean locked;
				
		public Resource (int id,
					String uri,
					String description,
					boolean available,
					boolean locked
				) {
			this.id = id;
			this.uri = uri;
			this.description = description;
			this.available = available;
			this.locked = locked;
		}

		public int getId() {
			return this.id;
		}

		public String getUri() {
			return this.uri;
		}

		public String getDescription() {
			return this.description;
		}

		public boolean isAvailable() {
			return this.available;
		}

		public boolean isLocked() {
			return this.locked;
		}		
	}
	
	/**
	 * Stores information corresponding to an item inside the <i>resource_tasks</i> table
	 * @author Marc Frincu
	 * @since 2010
	 *
	 */
	public class ResourceTasks {
		// database information
		int id;
		int task_id;
		int resource_id;
		int position_in_queue;
		
		public ResourceTasks (int id,
						int task_id,
						int resource_id,
						int position_in_queue
				) {
			this.id = id;
			this.task_id = task_id;
			this.resource_id = resource_id;
			this.position_in_queue = position_in_queue;
		}

		public int getId() {
			return this.id;
		}

		public int getTask_id() {
			return this.task_id;
		}

		public int getResource_id() {
			return this.resource_id;
		}

		public int getPosition_in_queue() {
			return this.position_in_queue;
		}		
	}
}
