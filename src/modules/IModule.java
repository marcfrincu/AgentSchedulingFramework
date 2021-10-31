package modules;

public interface IModule {

	public static enum MODULE_TYPE {
		NOT_SET, /*no type set to this module - default behavior*/
		SCHEDULING, /*scheduling module*/
		NEGOTIATION, /*negotiation module*/
		HEALING, /*healing module*/
		SCHEDULING_ANALYZER, /*scheduling analyzer module*/
		EXECUTOR, /*executor module*/
		MONITOR /*monitoring module*/
	};
	
	public static enum MODULE_STATUS {
		PAUSED, /* module has not been started - default behavior*/
		RUNNING, /* module is running - set automatically when starting it */
		NOT_RESPONDING, /* module has failed to ping the DB for a certain time */
		READY_FOR_RUNNING, /* module has been marked for starting - usually happens for agent clones */
		WAITING_TO_RUN /* module is waiting to see if similar modules are running */
	}; 
	
	public static String EMPTY_SYMBOL = "-1";
	public static String BROADCAST_SYMBOL = "*";
	
	public boolean sendMessage(String fromId, 
						String toId, 
						String content, 
						String processingName);
	
	public void run(boolean paused, boolean fromRemote) throws Exception;	

	public String getWorkflowUuid();
	
	public MODULE_TYPE getModuleType();
	
	public void setId(String id);
	
	public String getId();
	
	public void setAddress(String address);
	
	public void setParentId(String parentId);
	
	public void setIsPaused(boolean paused);
	
	public boolean getIsPaused();
		
	public void setIsExternal(boolean external);
	
	public boolean getIsExternal();
	
	public void setArchive(String archive);
	
	public String getArchive();
	
	public void setStartScript(String scriptName);
	
	public String getStartScript();
	
	public void ping();
}
