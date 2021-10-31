package modules.communication.json.beans;

/**
 * Bean holding the information about a message. Used for creating JSON messages
 * @author Marc Frincu
 * @since 2010
 *
 */
public class Message {
	private String fromId;
	private String toId;
	private String content;
	private String processingName;
	
	public Message() {
		
	}
	
	public Message (String fromId, 
					String toId, 
					String content, 
					String processingName) {
		this.fromId = fromId;
		this.toId = toId;
		this.content = content;
		this.processingName = processingName;
	}

	/**
	 * The ID of the sender agent
	 * @return the Id of the sender agent
	 */
	public String getFromId() {
		return this.fromId;
	}

	/**
	 * The ID of the receiver agent. Can be * for broadcasting messages
	 * @return the Id of the receiver agent
	 */
	public String getToId() {
		return this.toId;
	}

	/**
	 * The task sent between agents
	 * @return the content of the message
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * The name of the processing
	 * @return the name of the processing operation
	 */
	public String getProcessingName() {
		return this.processingName;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}
	
	public void setToId(String toId) {
		this.toId = toId;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setProcessingName(String processingName) {
		this.processingName = processingName;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("fromId: ");
		sb.append(this.fromId);
		sb.append("\n");
		sb.append("toId: ");
		sb.append(this.toId);
		sb.append("\n");
		sb.append("content: ");
		sb.append(this.content);
		sb.append("\n");
		sb.append("processingName: ");
		sb.append(this.processingName);
		
		return sb.toString();
	}
}
