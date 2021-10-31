package modules.communication.json.beans;

/**
 * Bean holding the information regarding a single Module from the <i>deploy.json</i> deployment file
 * @author Marc Frincu
 * @since 2013
 */
public class ModuleItem {
	String[] type, paused;
	String ip, external, agent_uuid, archive, start_script;
	
	public String getAgent_uuid() {
		return agent_uuid;
	}

	public void setAgent_uuid(String agentUuid) {
		this.agent_uuid = agentUuid;
	}

	public String getStart_script() {
		return start_script;
	}

	public void setStart_script(String startScript) {
		start_script = startScript;
	}

	public String[] getPaused() {
		return paused;
	}

	public void setPaused(String[] paused) {
		this.paused = paused;
	}

	public String[] getType() {
		return type;
	}

	public void setType(String[] type) {
		this.type = type;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getExternal() {
		return external;
	}

	public void setExternal(String external) {
		this.external = external;
	}

	public String getArchive() {
		return archive;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}

	public ModuleItem() {
	}
	
}
