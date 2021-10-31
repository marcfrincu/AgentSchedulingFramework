package modules.communication;

/**
 * Holds the information related with a message sent using one of the queues
 * @author Marc Frincu
 * @since 2010
 *
 */
public class Message {
	/**
	 * The task types
	 */
	public static enum TYPE {NEW_TASK, 
							RESCHEDULING_TASK, 
							BID_RESPONSE, 
							BID_REQUEST, 
							BID_WINNER,
							AGENT_MODULE_ACTIVATION_REQUEST,
							AGENT_MODULE_ACTIVATION_REPLY,
							AGENT_MODULE_REGISTRATION_REQUEST,
							AGENT_MODULE_REGISTRATION_REPLY,
							AGENT_MODULE_CHECK_SIMILAR_REQUEST,
							AGENT_MODULE_CHECK_SIMILAR_REPLY,
							PLATFORM_INFO,
							SCHEDULING_POLICY_CHANGE,
							AGENT_MODULE_PAUSE_REQUEST_NO_REPLY,
							AGENT_MODULE_HEALING_REGISTRATION_REQUEST,
							DIRECT_MSG_TO_SCHEDULER}
	
	private String fromId, toId, content, processingName;
	private Message.TYPE type; 
	
	/**
	 * Creates a message
	 * @param fromId the ID of the sender agent
	 * @param toId the ID of the receiver agent or * if broadcast is used
	 * @param content the content of the message
	 * @param processingName the name of the atom as described by the 
	 * @param type
	 */
	public Message (String fromId, 
					String toId, 
					String content, 
					String processingName,
					Message.TYPE type) {
		this.fromId = fromId;
		this.toId = toId;
		this.content = content;
		this.processingName = processingName;
		this.type = type;
	}

	public Message.TYPE getType() {
		return this.type;
	}
	
	public String getFromId() {
		return this.fromId;
	}

	public String getToId() {
		return this.toId;
	}

	public String getContent() {
		return this.content;
	}
	
	public String getProcessingName() {
		return this.processingName;
	}
}
