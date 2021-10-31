package modules.communication;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Starts a thread for listening to messages arriving on a specific Communication module
 * @author Marc Frincu
 * @since 2010
 *
 */
public class RunMessageListenerThread extends Thread {
		
	private static Logger logger = Logger.getLogger(
			RunMessageListenerThread.class.getPackage().getName());
	private static int IDLE_TIME = 5 * 1000;
	
	ICommunicationAMQP com = null;
		
	public RunMessageListenerThread(ICommunicationAMQP com) {
		this.com = com;
	}
		
	public void run() {
		while (true) {
			try {
				this.com.onMessage();
			} catch (IOException e) {
				RunMessageListenerThread.logger.error("Error when trying to receive a message. " +
						"Message: " + e.getMessage());
			}
			try {
				Thread.sleep(RunMessageListenerThread.IDLE_TIME);
			} catch (InterruptedException e) {
				RunMessageListenerThread.logger.error("Error when trying to sleep. " +
						"Message: " + e.getMessage());
			}
		}
	}	
}