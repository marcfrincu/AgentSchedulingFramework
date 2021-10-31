package modules;

import modules.communication.Message;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.PlatformInfo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.AgentService;

/**
 * Module used for analyzing the platform data
 * 
 * @author Marc Frincu
 * @since 2010
 */
//TODO implement all functionality
public class SchedulingAnalyzer extends BasicModule {

	private static Logger logger = Logger.getLogger(SchedulingAnalyzer.class
			.getPackage().getName());
	
		private static SchedulingAnalyzer analyzer = null;
	
		
	private SchedulingAnalyzer() {
		this.as = new AgentService();
		
		SchedulingAnalyzer.logger.setLevel(Level.DEBUG);
		this.type = MODULE_TYPE.SCHEDULING_ANALYZER;
	}

	
	/**
	 * Each module is built as a singleton.
	 * @return a reference to the <i>SchedulingAnalyzer</i> object
	 */
	public static SchedulingAnalyzer getSchedulingAnalyzerModule() {
		if (SchedulingAnalyzer.analyzer == null) {
			SchedulingAnalyzer.analyzer = new SchedulingAnalyzer();
			return SchedulingAnalyzer.analyzer;
		}
		else
			return SchedulingAnalyzer.analyzer;
	}
	
	@Override
	public boolean sendMessage(String fromId, String toId, String content,
			String processingName) {

		return false;
	}
	
	@Override
	public void bindCommunicator() {
		// TODO Implement CommunicationAMQPAnalyzer and bind to it here
		
	}


	@Override
	public void postMessageProcessingOperations() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void preMessageProcessingOperations() {
		// TODO Auto-generated method stub
		
	}

	//TODO this is just a sketch
	@Override
	public void processModuleDependentMessages(Message msg) {
		PlatformInfo pi = null;
		switch (msg.getType()) {
			case PLATFORM_INFO:
				pi = JSONHandler.getPlatformInfo(msg.getContent());
				this.selectSchedulingPolicy(pi);
				//TODO send result to scheduling agent
				break;
			default: SchedulingAnalyzer.logger.error("Task type: " + 
					msg.getType().toString() + 
					" not allowed in this context");
				break;
		}
		
	}

	@Override
	public void sleepFor() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Selects the scheduling policy based on the given platform information
	 * @param pi a reference to an object holding platform information
	 * @return the selected scheduling policy
	 */
	private String selectSchedulingPolicy(PlatformInfo pi) {
		//TODO implement logic
		return null;
	}
}
