package modules.communication;

import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

import modules.communication.Message.TYPE;
import modules.communication.json.JSONHandler;

import org.apache.log4j.Logger;

import settings.SystemSettings;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Communication module used by the Scheduling module
 * @author Marc Frincu
 * @since 2010
 *
 */
public class CommunicationAMQPScheduler implements ICommunicationAMQP {
	private static Logger logger = Logger.getLogger(
			CommunicationAMQPScheduler.class.getPackage().getName());
	
	private static String EXCHANGE_BID_REQUEST_NAME = "asf.bid.request";
	public static String EXCHANGE_BID_RESPONSE_NAME = "asf.bid.response";
	public static String EXCHANGE_RESCHEDULING_REQUEST_NAME = "asf.task.rescheduling";
	private static String EXCHANGE_BID_RESULT_NAME = "asf.bid.result";
	
	public static String EXCHANGE_DIRECT_TO_SCHEDULER = "asf.direct.scheduler";
	
	private static String QUEUE_BID_REQUEST_NAME = "asf.bid.request.queue";
	private static String QUEUE_BID_RESULT_NAME = "asf.bid.result.queue";
	private static String QUEUE_ACTIVATION_NAME = "asf.activation.queue";
	private static String QUEUE_SIMILAR_REQUEST_NAME = "asf.similar.request.queue";
	private static String QUEUE_SIMILAR_RESPONSE_NAME = "asf.similar.response.queue";
	
	private static String QUEUE_DIRECT_TO_SCHEDULER = "asf.direct.scheduler.queue";
	
	private Connection conn = null;
	private Vector<Channel> channels = null;
	private Vector<QueueingConsumer> consumers = null;
	
	private String agentId = null;
	String uuid;

	public CommunicationAMQPScheduler(String agentId) {
		uuid = UUID.randomUUID().toString();
		this.agentId = agentId;
		try {			
			this.init();
		} catch (Exception e) {
			CommunicationAMQPScheduler.logger.fatal("Cannot initialise AMQP. Message: " + 
					e.getMessage());
			System.exit(0);
		}
	}
	
	/**
	 * Initializes an AMQP connection and channel using RabbitMQ
	 * @throws IOException
	 */
	private void init() throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(SystemSettings.getSystemSettings().getMq_username());
		factory.setPassword(SystemSettings.getSystemSettings().getMq_password());
		factory.setVirtualHost(SystemSettings.getSystemSettings().getMq_virtual_host());
		factory.setHost(SystemSettings.getSystemSettings().getMq_host_name());
		factory.setPort(SystemSettings.getSystemSettings().getMq_port_number());
		this.conn = factory.newConnection();
		
		this.channels = new Vector<Channel>();
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());		
		this.channels.add(this.conn.createChannel());		

		
		this.consumers = new Vector<QueueingConsumer>();
		
		CommunicationAMQPScheduler.EXCHANGE_BID_REQUEST_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_request();
		CommunicationAMQPScheduler.EXCHANGE_BID_RESPONSE_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_response();
		CommunicationAMQPScheduler.EXCHANGE_RESCHEDULING_REQUEST_NAME = 
			SystemSettings.getSystemSettings().getQueue_rescheduling_task();
		CommunicationAMQPScheduler.EXCHANGE_BID_RESULT_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_result();
		
		CommunicationAMQPScheduler.EXCHANGE_DIRECT_TO_SCHEDULER += this.agentId;
		
		CommunicationAMQPScheduler.QUEUE_ACTIVATION_NAME = 
			SystemSettings.getSystemSettings().getQueue_activation() + ".queue";
		CommunicationAMQPScheduler.QUEUE_BID_REQUEST_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_request() + ".queue";
		CommunicationAMQPScheduler.QUEUE_BID_RESULT_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_result() + ".queue";
		
		// Declare the exchanges:
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(0).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_BID_REQUEST_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(0).queueDeclare(CommunicationAMQPScheduler.QUEUE_BID_REQUEST_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(0).queueBind(CommunicationAMQPScheduler.QUEUE_BID_REQUEST_NAME + this.agentId + uuid, 
								CommunicationAMQPScheduler.EXCHANGE_BID_REQUEST_NAME, 
								"asf.bid.request");
		//0
		this.consumers.add(new QueueingConsumer(this.channels.get(0)));
		
		// the bid response as direct for 1-1 communications
		this.channels.get(1).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_BID_RESULT_NAME, 
				"fanout", 
				true, false, null);
		
		// Consumer for the bid result queue
		this.channels.get(1).queueDeclare(CommunicationAMQPScheduler.QUEUE_BID_RESULT_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		this.channels.get(1).queueBind(CommunicationAMQPScheduler.QUEUE_BID_RESULT_NAME + this.agentId + uuid, 
								CommunicationAMQPScheduler.EXCHANGE_BID_RESULT_NAME, 
								"asf.bid.result");
		//1
		this.consumers.add(new QueueingConsumer(this.channels.get(1)));
		
		// the bid response as direct for 1-1 communications
		this.channels.get(2).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_BID_RESPONSE_NAME, 
				"direct", 
				true, false, null);
		// the rescheduling request as direct for 1-1 communications
		this.channels.get(2).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_RESCHEDULING_REQUEST_NAME, 
				"direct", 
				true, false, null);
		// the rescheduling request as direct for 1-1 communications
		this.channels.get(2).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_REGISTRATION_NAME,
				"direct", 
				true, false, null);
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(3).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_ACTIVATION_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(3).queueDeclare(CommunicationAMQPScheduler.QUEUE_ACTIVATION_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(3).queueBind(CommunicationAMQPScheduler.QUEUE_ACTIVATION_NAME+ this.agentId + uuid, 
				CommunicationAMQPScheduler.EXCHANGE_ACTIVATION_NAME, 
								"asf.activation");
		//2
		this.consumers.add(new QueueingConsumer(this.channels.get(3)));
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(4).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_SIMILAR_REQUEST_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(4).queueDeclare(CommunicationAMQPScheduler.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(4).queueBind(CommunicationAMQPScheduler.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid, 
				CommunicationAMQPScheduler.EXCHANGE_SIMILAR_REQUEST_NAME, 
								"asf.similar.request");
		//3
		this.consumers.add(new QueueingConsumer(this.channels.get(4)));


		// the bid request as fanout type for broadcasting purposes
		this.channels.get(5).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_SIMILAR_RESPONSE_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(5).queueDeclare(CommunicationAMQPScheduler.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(5).queueBind(CommunicationAMQPScheduler.QUEUE_SIMILAR_RESPONSE_NAME+ this.agentId + uuid, 
				CommunicationAMQPScheduler.EXCHANGE_SIMILAR_RESPONSE_NAME, 
								"asf.similar.response");
		//4
		this.consumers.add(new QueueingConsumer(this.channels.get(5)));

		
		
		// the direct link with the scheduler as direct type for broadcasting purposes
		this.channels.get(6).exchangeDeclare(CommunicationAMQPScheduler.EXCHANGE_BID_REQUEST_NAME, 
										"direct", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(6).queueDeclare(CommunicationAMQPScheduler.QUEUE_DIRECT_TO_SCHEDULER + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(6).queueBind(CommunicationAMQPScheduler.QUEUE_DIRECT_TO_SCHEDULER + this.agentId + uuid, 
								CommunicationAMQPScheduler.EXCHANGE_DIRECT_TO_SCHEDULER, 
								"asf.direct.scheduler");
		//5
		this.consumers.add(new QueueingConsumer(this.channels.get(6)));
	}
	
	/**
	 * Sends a message.
	 * @param msg the message to be sent
	 * @param exchangeName the name of the exchange where the message is to be sent 
	 * valid values include: <i>asf.reschedulingRequest</i> and <i>asf.bidResponse</i>
	 * @return true if the message has been sent, false otherwise
	 */
	public boolean sendMessage(Message msg,
							String exchangeName) {
		
		if (exchangeName.compareTo(
					CommunicationAMQPScheduler.EXCHANGE_BID_RESPONSE_NAME) != 0 &&
				exchangeName.compareTo(
					CommunicationAMQPScheduler.EXCHANGE_RESCHEDULING_REQUEST_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPScheduler.EXCHANGE_ACTIVATION_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPScheduler.EXCHANGE_REGISTRATION_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPScheduler.EXCHANGE_SIMILAR_REQUEST_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPScheduler.EXCHANGE_SIMILAR_RESPONSE_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPScheduler.EXCHANGE_DIRECT_TO_SCHEDULER) != 0
					) {
			CommunicationAMQPScheduler.logger.error("Destination exchange not valid");
			return false;
		}
		
		String routingKey = null;
		int index = 0;
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_BID_RESPONSE_NAME) == 0) {
			routingKey = "asf.bid.response";
			index = 2;
			
		}
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_RESCHEDULING_REQUEST_NAME) == 0) {
			routingKey = "asf.task.rescheduling";
			index = 2;
		}		
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_ACTIVATION_NAME) == 0) {
			routingKey = "asf.activation";
			index = 3;
		}
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_REGISTRATION_NAME) == 0) {
			routingKey = SystemSettings.getSystemSettings().getRouting_key_registration();
			index = 2;
		}	
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_SIMILAR_REQUEST_NAME) == 0) {
			routingKey = "asf.similar.request";
			index = 4;
		}	
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_SIMILAR_RESPONSE_NAME) == 0) {
			routingKey = "asf.similar.response";
			index = 5;
		}	
		if (exchangeName.compareTo(
				CommunicationAMQPScheduler.EXCHANGE_DIRECT_TO_SCHEDULER) == 0) {
			routingKey = "asf.direct.scheduler";
			index = 6;
			
		}
		// convert to JSON
		final String str = JSONHandler.makeTaskMessage(msg.getFromId(),
									msg.getToId(),
									msg.getContent(),
									msg.getProcessingName());
		
		byte[] messageBodyBytes = str.getBytes();
	    
		try {
			//CommunicationAMQPScheduler.logger.debug("SCH exchange: " + 
			//		exchangeName + 
			//		" routing key: " + routingKey); 
			
			this.channels.get(index).basicPublish(exchangeName, 
									routingKey, 
					  				MessageProperties.PERSISTENT_TEXT_PLAIN, 
					  				messageBodyBytes) ;
		} catch (IOException e) {
			CommunicationAMQPScheduler.logger.error("Message: " + str + 
					" could not be sent. Message: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Handles the receive event of one message
	 * @throws IOException
	 */
	public void onMessage() throws IOException {
		for (int i=0; i< SystemSettings.getSystemSettings().getScheduling_msg_batch_no(); i++) {
			//CommunicationAMQPScheduler.logger.info("Processing BID_REQUEST message");		
			this.processMessage(CommunicationAMQPScheduler.QUEUE_BID_REQUEST_NAME + this.agentId + uuid,
					Message.TYPE.BID_REQUEST);
			//CommunicationAMQPScheduler.logger.info("Processing BID_WINNER message");
			this.processMessage(CommunicationAMQPScheduler.QUEUE_BID_RESULT_NAME + this.agentId + uuid,
					Message.TYPE.BID_WINNER);
			//CommunicationAMQPScheduler.logger.info("Processing AGENT_ACTIVATION message");		
			this.processMessage(CommunicationAMQPScheduler.QUEUE_ACTIVATION_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_ACTIVATION_REQUEST);
			//CommunicationAMQPScheduler.logger.info("Processing AGENT_SIMILAR_REQUEST message");	
			this.processMessage(CommunicationAMQPScheduler.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REQUEST);
			//CommunicationAMQPScheduler.logger.info("Processing AGENT_SIMILAR_RESPONSE message");	
			this.processMessage(CommunicationAMQPScheduler.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY);
			//CommunicationAMQPScheduler.logger.info("Processing AGENT_SIMILAR_RESPONSE message");	
			this.processMessage(CommunicationAMQPScheduler.QUEUE_DIRECT_TO_SCHEDULER + this.agentId + uuid,
					Message.TYPE.DIRECT_MSG_TO_SCHEDULER);
		}
	}
	
	/**
	 * Closes the connection and the communication channel.
	 * @return true if the operation succeeded, false otherwise
	 */
	public boolean closeConnectionAndChannel() {
	    try {
	      	for (Channel channel : this.channels) {
		   		channel.close();
		   	}
			this.conn.close();
		} catch (IOException e) {
			CommunicationAMQPScheduler.logger.error("Could not close connection or " +
					"communication channel. Message: " + e.getMessage());
			return false;
		}
		return true;
	      
	}
	
	/**
	 * Handles a possible message. If no message is present in the queue it 
	 * timeouts and returns
	 * @param queueName the name of the queue
	 * @throws IOException
	 */
	private void processMessage(String queueName, TYPE type) throws IOException {
		
		QueueingConsumer.Delivery delivery = null;
		try {
			switch (type) {
				case BID_REQUEST:
					this.channels.get(0).basicConsume(queueName, 
							false, 
							this.consumers.get(0));
					
			    	delivery = this.consumers.get(0).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case BID_WINNER:
					this.channels.get(1).basicConsume(queueName, 
							false, 
							this.consumers.get(1));
					
			    	delivery = this.consumers.get(1).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_ACTIVATION_REQUEST:
					this.channels.get(3).basicConsume(queueName, 
							false, 
							this.consumers.get(2));
					
			    	delivery = this.consumers.get(2).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
					this.channels.get(4).basicConsume(queueName, 
							false, 
							this.consumers.get(3));
					
			    	delivery = this.consumers.get(3).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
					this.channels.get(5).basicConsume(queueName, 
							false, 
							this.consumers.get(4));
					
			    	delivery = this.consumers.get(4).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case DIRECT_MSG_TO_SCHEDULER:
					this.channels.get(6).basicConsume(queueName, 
							false, 
							this.consumers.get(5));
					
			    	delivery = this.consumers.get(5).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				default:
					CommunicationAMQPScheduler.logger.error("Invalid message type" + type.toString() 
							+ " in the context of message consuming");
					return;
			}
			
	    	if (delivery == null)
	    		return;
	    	
	        //CommunicationAMQPScheduler.logger.debug("Message received: " + 
			//		new String(delivery.getBody()));
	        
	        switch (type) {
				case BID_REQUEST:
			        this.channels.get(0).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case BID_WINNER:
					this.channels.get(1).basicAck(delivery.getEnvelope().getDeliveryTag(), false);					
					break;
				case AGENT_MODULE_ACTIVATION_REQUEST:
			        this.channels.get(3).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			        break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
			        this.channels.get(4).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			        break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
			        this.channels.get(5).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
	        }
	        
	        //extract message from JSON
	        modules.communication.json.beans.Message msg = JSONHandler.getMessage(new String(delivery.getBody()));
	        if (msg == null) {
	        	CommunicationAMQPScheduler.logger.error("Message not well formed");
	        	return;
	        }
	        synchronized (ICommunicationAMQP.msgs) {
	        		ICommunicationAMQP.msgs.add(new Message(
	        						msg.getFromId(),
	        						msg.getToId(),
	        						msg.getContent(),
	        						msg.getProcessingName(),
	        						type)
	        				);
	        }
		 } 
        catch (InterruptedException ie) {
        	CommunicationAMQPScheduler.logger.error("Error retrieving message content. " +
        			"Message: " + ie.getMessage());
        }
	}
}
