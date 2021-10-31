package test;

import java.io.IOException;
import java.util.UUID;

import settings.SystemSettings;

import modules.communication.CommunicationAMQPNegotiator;
import modules.communication.CommunicationAMQPScheduler;
import modules.communication.Message;

/**
 * Tests RabbitMQ communication
 * @author Marc Frincu
 * @since 2010
 */
public class TestCommunication {
	public static void main(String args[]) throws IOException {
		
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");
		
		CommunicationAMQPNegotiator comNeg = 
			new CommunicationAMQPNegotiator(UUID.randomUUID().toString()); 
		
		CommunicationAMQPScheduler comSch = 
					new CommunicationAMQPScheduler(UUID.randomUUID().toString());
		
		CommunicationAMQPScheduler comSch2 = 
			new CommunicationAMQPScheduler(UUID.randomUUID().toString());
		
		// Send bid response from scheduler
		Message msg = new Message("fromSch", 
							"toNeg", 
							"contentSch", 
							"bid_response", Message.TYPE.BID_RESPONSE);
		comSch.sendMessage(msg,
						CommunicationAMQPScheduler.EXCHANGE_BID_RESPONSE_NAME);
		
		// Send rescheduling request from scheduler
		msg = new Message("fromSch", 
				"toNeg", 
				"contentSch", 
				"rescheduling", Message.TYPE.RESCHEDULING_TASK);
		
		comSch.sendMessage(msg,
				CommunicationAMQPScheduler.EXCHANGE_RESCHEDULING_REQUEST_NAME);
		
		// Send bid request from negotiator
		msg = new Message("fromNeg", 
							"*", 
							"contentNeg", 
							"bid_request", Message.TYPE.BID_REQUEST);
		
		comNeg.sendMessage(msg,
							CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME);

		// Send bid results
		msg = new Message("fromNeg", 
				"*", 
				"contentNeg", 
				"bid_winner", Message.TYPE.BID_WINNER);
		comNeg.sendMessage(msg,
				CommunicationAMQPNegotiator.EXCHANGE_BID_RESULT_NAME);

		
		System.out.println("Scheduling module 1: ");
		comSch.onMessage();
		System.out.println("Scheduling module 2: ");
		comSch2.onMessage();
		System.out.println("Negotiation module: ");
		comNeg.onMessage();
		
		comSch.closeConnectionAndChannel();
		comSch2.closeConnectionAndChannel();
		comNeg.closeConnectionAndChannel();
		
	}
}
