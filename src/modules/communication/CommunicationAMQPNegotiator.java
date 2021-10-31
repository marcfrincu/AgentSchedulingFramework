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
 * Communication module used by the Negotiator module
 * @author Marc Frincu
 * @since 2010
 *
 */
public class CommunicationAMQPNegotiator implements ICommunicationAMQP {
	private static Logger logger = Logger.getLogger(
			CommunicationAMQPNegotiator.class.getPackage().getName());
	
	public static String EXCHANGE_BID_REQUEST_NAME = "asf.bid.request";
	private static String EXCHANGE_BID_RESPONSE_NAME = "asf.bid.response";
	private static String EXCHANGE_RESCHEDULING_REQUEST_NAME = "asf.task.rescheduling";
	private static String EXCHANGE_NEW_TASK_REQUEST_NAME = "asf.task.new";
	public static String EXCHANGE_BID_RESULT_NAME = "asf.bid.result";
		
	private static String QUEUE_RESCHEDULING_NAME = "asf.task.rescheduling.queue";
	private static String QUEUE_NEW_TASK_NAME = "asf.task.new.queue";
	private static String QUEUE_BID_RESPONSE_NAME = "asf.bid.response.queue";
	private static String QUEUE_ACTIVATION_NAME = "asf.activation.queue";
	private static String QUEUE_SIMILAR_REQUEST_NAME = "asf.similar.request.queue";
	private static String QUEUE_SIMILAR_RESPONSE_NAME = "asf.similar.response.queue";
		
	private Connection conn = null;
	private Vector<Channel> channels = null;
	private Vector<QueueingConsumer> consumers = null;
	
	private String agentId = null;
	String uuid;
	public CommunicationAMQPNegotiator(String agentId) throws IOException {
		this.agentId = agentId;
		uuid = UUID.randomUUID().toString();
		try {
			this.init();
		} catch (Exception e) {
			CommunicationAMQPNegotiator.logger.fatal("Cannot initialise AMQP. Message: " + 
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
		// add channels
		this.channels = new Vector<Channel>();
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());
		this.channels.add(this.conn.createChannel());

		this.consumers = new Vector<QueueingConsumer>();
			
		CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_request();
		CommunicationAMQPNegotiator.EXCHANGE_BID_RESPONSE_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_response();
		CommunicationAMQPNegotiator.EXCHANGE_RESCHEDULING_REQUEST_NAME = 
			SystemSettings.getSystemSettings().getQueue_rescheduling_task();
		CommunicationAMQPNegotiator.EXCHANGE_BID_RESULT_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_result();
		CommunicationAMQPNegotiator.EXCHANGE_NEW_TASK_REQUEST_NAME = 
			SystemSettings.getSystemSettings().getQueue_new_task();
		
		CommunicationAMQPNegotiator.QUEUE_ACTIVATION_NAME = 
			SystemSettings.getSystemSettings().getQueue_activation() + ".queue";
		CommunicationAMQPNegotiator.QUEUE_RESCHEDULING_NAME = 
			SystemSettings.getSystemSettings().getQueue_rescheduling_task() + ".queue";
		CommunicationAMQPNegotiator.QUEUE_NEW_TASK_NAME = 
			SystemSettings.getSystemSettings().getQueue_new_task() + ".queue";
		CommunicationAMQPNegotiator.QUEUE_BID_RESPONSE_NAME = 
			SystemSettings.getSystemSettings().getQueue_bid_response() + ".queue";
				
		// the bid response as direct for 1-1 communications
		this.channels.get(0).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_BID_RESPONSE_NAME, 
				"direct", 
				true, false, null);
		
		// Consumer for the bidding response queue
		this.channels.get(0).queueDeclare(CommunicationAMQPNegotiator.QUEUE_BID_RESPONSE_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(0).queueBind(CommunicationAMQPNegotiator.QUEUE_BID_RESPONSE_NAME + this.agentId + uuid, 
								CommunicationAMQPNegotiator.EXCHANGE_BID_RESPONSE_NAME, 
								"asf.bid.response");
		//0
		this.consumers.add(new QueueingConsumer(this.channels.get(0)));
		
		// the rescheduling request as direct for 1-1 communications
		this.channels.get(1).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_RESCHEDULING_REQUEST_NAME, 
				"direct", 
				true, false, null);
		
		// Consumer for the new task queue
		this.channels.get(1).queueDeclare(CommunicationAMQPNegotiator.QUEUE_RESCHEDULING_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(1).queueBind(CommunicationAMQPNegotiator.QUEUE_RESCHEDULING_NAME + this.agentId + uuid, 
								CommunicationAMQPNegotiator.EXCHANGE_RESCHEDULING_REQUEST_NAME, 
								"asf.task.rescheduling");
		//1
		this.consumers.add(new QueueingConsumer(this.channels.get(1)));
		
		// the new task request for 1-1 communications. Maybe in the future it could become
		// a fanout as multiple negotiators could exist
		this.channels.get(2).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_NEW_TASK_REQUEST_NAME, 
				"direct", 
				false, false, null);
		
		// Consumer for the new task queue
		this.channels.get(2).queueDeclare(CommunicationAMQPNegotiator.QUEUE_NEW_TASK_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(2).queueBind(CommunicationAMQPNegotiator.QUEUE_NEW_TASK_NAME + this.agentId + uuid, 
								CommunicationAMQPNegotiator.EXCHANGE_NEW_TASK_REQUEST_NAME, 
								"asf.task.new");
		//2
		this.consumers.add(new QueueingConsumer(this.channels.get(2)));
	
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(3).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME, 
										"fanout", 
										true, false, null);
	
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(3).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_BID_RESULT_NAME, 
										"fanout", 
										true, false, null);
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(4).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_ACTIVATION_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(4).queueDeclare(CommunicationAMQPNegotiator.QUEUE_ACTIVATION_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(4).queueBind(CommunicationAMQPNegotiator.QUEUE_ACTIVATION_NAME+ this.agentId + uuid, 
				CommunicationAMQPNegotiator.EXCHANGE_ACTIVATION_NAME, 
								"asf.activation");
		//3
		this.consumers.add(new QueueingConsumer(this.channels.get(4)));
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(5).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_REQUEST_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(5).queueDeclare(CommunicationAMQPNegotiator.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(5).queueBind(CommunicationAMQPNegotiator.QUEUE_SIMILAR_REQUEST_NAME+ this.agentId + uuid, 
				CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_REQUEST_NAME, 
								"asf.similar.request");
		//4
		this.consumers.add(new QueueingConsumer(this.channels.get(5)));

		// the bid request as fanout type for broadcasting purposes
		this.channels.get(6).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_RESPONSE_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(6).queueDeclare(CommunicationAMQPNegotiator.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(6).queueBind(CommunicationAMQPNegotiator.QUEUE_SIMILAR_RESPONSE_NAME+ this.agentId + uuid, 
				CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_RESPONSE_NAME, 
								"asf.similar.response");
		//5
		this.consumers.add(new QueueingConsumer(this.channels.get(6)));
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(7).exchangeDeclare(CommunicationAMQPNegotiator.EXCHANGE_REGISTRATION_NAME, 
										"direct", 
										true, false, null);
		//6
		this.consumers.add(new QueueingConsumer(this.channels.get(7)));

	}
	
	/**
	 * Sends a message.
	 * @param msg the message to be sent
	 * @param exchangeName the name of the exchange where the message is to be sent. 
	 * This parameter is not used in the case of this module. Set it to null
	 * @return true if the message has been sent, false otherwise
	 */
	public boolean sendMessage(Message msg,
							String exchangeName) {
		
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPNegotiator.EXCHANGE_BID_RESULT_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPNegotiator.EXCHANGE_ACTIVATION_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPNegotiator.EXCHANGE_REGISTRATION_NAME) != 0	&&
						exchangeName.compareTo(
								CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_REQUEST_NAME) != 0 &&
								exchangeName.compareTo(
										CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_RESPONSE_NAME) != 0) {
			CommunicationAMQPNegotiator.logger.error("Destination exchange not valid");
			return false;
		}
		
		// convert to JSON
		final String str = JSONHandler.makeTaskMessage(msg.getFromId(),
									msg.getToId(),
									msg.getContent(),
									msg.getProcessingName());
		
		byte[] messageBodyBytes = str.getBytes();
	    
		String routingKey = null;
		int index = 0;
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME) == 0) {
			routingKey = "asf.bid.request";
			index = 3;
		}
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_BID_RESULT_NAME) == 0) {
			routingKey = "asf.bid.result";
			index = 3;
		}
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_ACTIVATION_NAME) == 0) {
			routingKey = "asf.activation";
			index = 4;
		}
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_REGISTRATION_NAME) == 0) {
			routingKey = SystemSettings.getSystemSettings().getRouting_key_registration();
			index = 7;

		}	
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_REQUEST_NAME) == 0) {
			routingKey = "asf.similar.request";
			index = 5;

		}	
		if (exchangeName.compareTo(
				CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_RESPONSE_NAME) == 0) {
			routingKey = "asf.similar.response";
			index = 6;

		}	
		
		
		try {
			//CommunicationAMQPNegotiator.logger.debug("NEG exchange: " + 
			//		exchangeName + " routing key: " +
			//		routingKey); 

			this.channels.get(index).basicPublish(exchangeName, 
									routingKey, 
					  				MessageProperties.PERSISTENT_TEXT_PLAIN, 
					  				messageBodyBytes) ;	  				
		} catch (IOException e) {
			CommunicationAMQPNegotiator.logger.error("Message: " + str + 
					" could not be sent. Message: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Handles the receive event of one message
	 * @throws IOException
	 */
	public void onMessage() {	
		for (int i=0;i<SystemSettings.getSystemSettings().getNegotiator_msg_batch_no(); i++) {
			//CommunicationAMQPNegotiator.logger.info("Processing BID_RESPONSE message");		
			this.processMessage(CommunicationAMQPNegotiator.QUEUE_BID_RESPONSE_NAME + this.agentId  + uuid,
					Message.TYPE.BID_RESPONSE);
			//CommunicationAMQPNegotiator.logger.info("Processing RESCHEDULING_TASK message");
			this.processMessage(CommunicationAMQPNegotiator.QUEUE_RESCHEDULING_NAME + this.agentId + uuid,
					Message.TYPE.RESCHEDULING_TASK);
			//CommunicationAMQPNegotiator.logger.info("Processing NEW_TASK message");
			this.processMessage(CommunicationAMQPNegotiator.QUEUE_NEW_TASK_NAME + this.agentId + uuid,
					Message.TYPE.NEW_TASK);
			//CommunicationAMQPNegotiator.logger.info("Processing AGENT_ACTIVATION message");		
			this.processMessage(CommunicationAMQPNegotiator.QUEUE_ACTIVATION_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_ACTIVATION_REQUEST);
			//CommunicationAMQPNegotiator.logger.info("Processing AGENT_SIMILAR_REQUEST message");		
			this.processMessage(CommunicationAMQPNegotiator.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REQUEST);
			//CommunicationAMQPNegotiator.logger.info("Processing AGENT_SIMILAR_RESPONSE message");		
			this.processMessage(CommunicationAMQPNegotiator.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY);
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
			CommunicationAMQPNegotiator.logger.error("Could not close connection or " +
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
	private void processMessage(String queueName, TYPE type) {
		
		QueueingConsumer.Delivery delivery = null;
		try {
			switch (type) {
				case BID_RESPONSE:
					this.channels.get(0).basicConsume(queueName, 
							false, 
							this.consumers.get(0));
	
					delivery = this.consumers.get(0).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case RESCHEDULING_TASK:
					this.channels.get(1).basicConsume(queueName, 
							false, 
							this.consumers.get(1));
	
					delivery = this.consumers.get(1).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case NEW_TASK:
					this.channels.get(2).basicConsume(queueName, 
							false, 
							this.consumers.get(2));
	
					delivery = this.consumers.get(2).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_ACTIVATION_REQUEST:
					this.channels.get(4).basicConsume(queueName, 
							false, 
							this.consumers.get(3));
					
			    	delivery = this.consumers.get(3).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
					this.channels.get(5).basicConsume(queueName, 
							false, 
							this.consumers.get(4));
					
			    	delivery = this.consumers.get(4).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
					this.channels.get(6).basicConsume(queueName, 
							false, 
							this.consumers.get(5));
					
			    	delivery = this.consumers.get(5).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				default: 
					CommunicationAMQPNegotiator.logger.error("Invalid message type" + type.toString() 
							+ " in the context of message consuming");
					return;				
			}
	    	
	    	if (delivery == null)
	    		return;
	    	
			switch (type) {
				case BID_RESPONSE:
					this.channels.get(0).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case RESCHEDULING_TASK:
					this.channels.get(1).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case NEW_TASK:
					this.channels.get(2).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case AGENT_MODULE_ACTIVATION_REQUEST:
			        this.channels.get(4).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
					this.channels.get(5).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
					this.channels.get(6).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
			}
	    	
	        //CommunicationAMQPNegotiator.logger.debug("Message received: " + 
			//		new String(delivery.getBody()));
	        
	        //extract message from JSON
	        modules.communication.json.beans.Message msg = JSONHandler.getMessage(new String(delivery.getBody()));
	        if (msg == null) {
	        	CommunicationAMQPNegotiator.logger.error("Message not well formed");
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
        	CommunicationAMQPNegotiator.logger.error("Error retrieving message content. " +
        			"Message: " + ie.getMessage());
        }
        catch (IOException ioe) {
        	CommunicationAMQPNegotiator.logger.error("IO error when retrieving message content. " +
        			"Message: " + ioe.getMessage());
        }
        catch (Exception e) {
        	CommunicationAMQPNegotiator.logger.error("Unexpected error. " +
        			"Message: " + e.getMessage());
        	e.printStackTrace();
        }
	}
}
