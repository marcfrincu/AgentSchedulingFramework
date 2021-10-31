package modules.negotiation;

import java.util.Vector;

import modules.communication.Message;

/**
 * Interface for the bid selection policy. Extend it to implement your own policy.
 * The message's body contains a <i>modules.communication.json.beans.Task</i> object
 * @author Marc Frincu
 * @since 2013
 */

public interface IStrategy {
	public Message selectBestBid(String taskUUID, Vector<Message> bids);
}
