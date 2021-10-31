package modules.communication.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import net.sf.json.JSONObject;
import modules.communication.json.beans.AgentModule;
import modules.communication.json.beans.Deployment;
import modules.communication.json.beans.Message;
import modules.communication.json.beans.PlatformInfo;
import modules.communication.json.beans.Task;
import modules.communication.json.beans.Winner;

/**
 * Holds methods for creating JSON messages
 * @author Marc Frincu
 * @since 2010
 */
public class JSONHandler {

	public static Deployment getDeployment(String filename) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		final StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) 
			sb.append(line+"\r\n");
		final JSONObject jsonObject = JSONObject.fromObject(sb.toString());
		return (Deployment) JSONObject.toBean(jsonObject,
				Deployment.class);
	}
	
	/**
	 * Creates a JSON representation of a task
	 * @param id
	 * @param original_agent_id
	 * @param description
	 * @param executable_location
	 * @param dependencies
	 * @param position_on_resource_queue
	 * @param submission_time_local
	 * @param submission_time_total
	 * @param estimated_execution_time_on_resource
	 * @param estimated_completion_time_on_resource
	 * @param locked
	 * @param assigned_resource_id
	 * @param uuid
	 * @param istemporary
	 * @return the JSON
	 */
	public static String makeTask(
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
		final Task t = new Task(
				id,
				original_agent_id,
				description,
				executable_location,
				dependencies,
				position_on_resource_queue,
				submission_time_local,
				submission_time_total,
				estimated_execution_time_on_resource,
				estimated_completion_time_on_resource,
				locked,
			    assigned_resource_id,
				uuid,
				istemporary,
				submiter_agent_id
				);
		final JSONObject obj = JSONObject.fromObject(t);

		return obj.toString(); 
	}
	
	public static String makeAgentModule(String uuid,
			String agentParentId,
			String workflowID,
			String moduleType,
			String status,
			long lastPing) {
		final AgentModule am = new AgentModule(uuid,
											agentParentId,
											workflowID,
											moduleType,
											status,
											lastPing);
		final JSONObject obj = JSONObject.fromObject(am);

		return obj.toString(); 
	}
	
	/**
	 * Retrieves the task bean from a JSON string
	 * @param request
	 * @return the <i>Task</i> message
	 */
	public static Task getTask(String request) {
		final JSONObject jsonObject = JSONObject.fromObject(request);
		return (Task) JSONObject.toBean(jsonObject,
				Task.class);
	}
	
	/**
	 * Retrieves the agent module bean from a JSON string
	 * @param request
	 * @return the <i>AgentModule</i> message
	 */
	public static AgentModule getAgentModule(String request) {
		final JSONObject jsonObject = JSONObject.fromObject(request);
		return (AgentModule) JSONObject.toBean(jsonObject,
				AgentModule.class);
	}

	/**
	 * Retrieves the platform info bean from a JSON string
	 * @param request
	 * @return the <i>PlatformInfo</i> message
	 */
	public static PlatformInfo getPlatformInfo(String request) {
		final JSONObject jsonObject = JSONObject.fromObject(request);
		return (PlatformInfo) JSONObject.toBean(jsonObject,
				PlatformInfo.class);
	}
	
	/**
	 * Creates a JSON representation for sending winner task UUIDs
	 * @param taskUuid
	 * @return the JSON
	 */
	public static String makeWinnerMessage(String taskUuid) {
		final Winner win = new Winner(taskUuid);
		final JSONObject obj = JSONObject.fromObject(win);
		return obj.toString();
	}
	
	/**
	 * Retrieves the winner data from a JSON string
	 * @param request
	 * @return the <i>Winner</i> message
	 */
	public static Winner getWinnerMessage(String request) {
		final JSONObject jsonObject = JSONObject.fromObject(request);
		return (Winner) JSONObject.toBean(jsonObject,
				Winner.class);
	}
	
	/**
	 * Creates a JSON representation for sending messages between queues
	 * @param fromId
	 * @param toId
	 * @param task
	 * @param processingName
	 * @return the JSON
	 */
	public static String makeTaskMessage(String fromId,
									String toId,
									String content,
									String processingName) {
		final Message msg = new Message(fromId, toId, content, processingName);
		final JSONObject obj = JSONObject.fromObject(msg);
		return obj.toString();
	}
	
	/**
	 * Retrieves the message bean from a JSON string
	 * @param request
	 * @return the <i>Message</i> object
	 */
	public static Message getMessage(String request) {
		final JSONObject jsonObject = JSONObject.fromObject(request);
		return (Message) JSONObject.toBean(jsonObject,
				Message.class);
	}
}
