package utils.install;

import java.io.IOException;
import java.util.Vector;

import settings.SystemSettings;
import utils.remote.Executor;

import modules.IModule;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.Deployment;
import modules.communication.json.beans.ModuleItem;

/***
 * This class is used to deploy the platform on various nodes. It requires the <i>deploy.json</i>
 * configuration file
 * @author Marc Frincu
 * @since 2013
 */
public class Deploy {

	/**
	 * Main entry point
	 * @param args the <i>deploy.json</i> configuration file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("deploy.json file required to install platform agents");
			System.exit(0);
		}
		
		try {
			Deployment deploy = (Deployment)JSONHandler.getDeployment(args[0]);
			Executor executor = new Executor();
			for (ModuleItem mod: deploy.getModules()) {
				executor.runCommands(Deploy.generateCommands(mod));
			}
			
			
		} catch (IOException e) {
			System.out.println("Fatal error: " + e.getMessage());
			System.exit(0);
		}
	}
	
	/**
	 * Generates the list of commands used for starting the remote agent module(s)
	 * @param mod the {@link ModuleItem} POJO holding the data needed to start it
	 * @return the list of commands
	 */
	public static Vector<String> generateCommands(ModuleItem mod) {
		Vector<String> cmds = new Vector<String>();
		
		final String remoteHost = SystemSettings.getSystemSettings().getSsh_username() + 
									"@" + 
									mod.getIp();
		
		String ssh = "ssh " + remoteHost;
		boolean localhost = false;		
		if (mod.getIp().trim().compareToIgnoreCase("localhost") == 0 || mod.getIp().compareTo("127.0.0.1") == 0) {
			localhost = true;
			ssh = "";
		}
		
		
		final String remoteDir = mod.getAgent_uuid();
				
		//TODO the archive should be taken from HDFS only
		
		//create remote directory
		cmds.add(ssh + " mkdir " + remoteDir);
		if (!localhost) {
			//copy archive remotely
			cmds.add("scp " + mod.getArchive() + " " + remoteHost + ":" + remoteDir);		
		}
		else {
			//copy archive locally
			cmds.add("cp " + mod.getArchive() + " " + remoteDir);
		}			
		//extract it
		cmds.add(ssh + " tar -xpvf " + remoteDir + "/" + mod.getArchive() + " -C " + remoteDir);
		//delete the archive file
		cmds.add(ssh + " rm " + remoteDir + "/" + mod.getArchive());
		//create the new token
		cmds.add(ssh + " echo " + remoteDir + " > " + remoteDir + "/agent.token");
		//start the application
		cmds.add(ssh + " chmod 777 " + remoteDir + "/" + mod.getStart_script());
		for (int i=0; i<mod.getType().length; i++) {
			if (mod.getExternal().compareToIgnoreCase("false") == 0) { //if it is part of the platform distribution
				cmds.add(ssh + " " + remoteDir + "/" + mod.getStart_script() + " " + 
									remoteDir + "/" + " "  +
									mod.getType()[i] + " " +
									mod.getPaused()[i] + " " +
									IModule.EMPTY_SYMBOL + " " +
									mod.getIp() + " " +
									"true" + " " +
									mod.getExternal() + " " +
									mod.getArchive() + " " + 
									mod.getStart_script() + " &");
			}
			else { //if it is an external application
				cmds.add(ssh + " " + remoteDir + "/" + mod.getStart_script()  + " &");
			}
		}
		// TODO: create binary script and start bash remotely to execute it
		// Advantage: does not need to copy anything on the remote server			
		return cmds;
	}
}