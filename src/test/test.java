package test;

import modules.negotiation.IStrategy;
import services.AgentService;
import settings.SystemSettings;

public class test {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");
		/*new AgentService().addModuleToHealer("26853a29-0073-4ab8-b3ad-5aacf442d0f5", 
				"26853a29-0073-4ab8-b3ad-5aacf442d0f5", 
				"-1",
				"HEALING");
		 */
		IStrategy bidStrategy = null;
		Class cls;
		try {
			cls = Class.forName(SystemSettings.getSystemSettings().getNegotiator_bid_selection_policy_class());
			bidStrategy = (IStrategy) cls.newInstance();
			bidStrategy.selectBestBid("111", null);
			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}	
	}

}
