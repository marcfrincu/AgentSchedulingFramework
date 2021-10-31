package modules.negotiation;

import java.util.Vector;

import modules.communication.Message;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.Task;

/**
 * Simple implementation of the <i>modules.IStrategy<:i> interface
 * @author Marc Frincu
 * @since 2013
 *
 */

public class DefaultStrategy implements IStrategy {

	/**
	 * Selects the best bid response for a given task UUID
	 * @param taskUUID the task UUID
	 * @param bids the list of received bids
	 * @return the best bid Message or null if no bids exist
	 */
	@Override
	public Message selectBestBid(String taskUUID, Vector<Message> bids) {
		Task task = null, bestTask = null;
		if (bids == null || bids.size() == 0) {
			return null;
		}
		Message bestMsg = bids.get(0);
		for (Message msg : bids) {
			task = JSONHandler.getTask(msg.getContent());
			bestTask = JSONHandler.getTask(bestMsg.getContent());
			if (task.getEstimated_completion_time_on_resource() <
				bestTask.getEstimated_completion_time_on_resource()) {
				bestMsg = msg;
			}
		}
		
		return bestMsg;
	}
}
