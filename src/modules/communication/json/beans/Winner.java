package modules.communication.json.beans;

public class Winner {
	private String uuid;
	
	public Winner() {
		
	}
	
	public Winner(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * The UUID of the task
	 * @return the UUID of the task
	 */
	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("uuid: ");
		sb.append(this.uuid);
		sb.append("\n");
		
		return sb.toString();
	}
}
