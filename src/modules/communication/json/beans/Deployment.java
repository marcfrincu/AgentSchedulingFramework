package modules.communication.json.beans;

/**
 * Bean holding the information from the <i>deploy.json</i> platform deployment file
 * @author Marc Frincu
 * @since 2013
 */
public class Deployment {
	String certificate;
	ModuleItem[] modules;
	
	
	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public ModuleItem[] getModules() {
		return modules;
	}

	public void setModules(ModuleItem[] modules) {
		this.modules = modules;
	}

	public Deployment() {
	}
}
