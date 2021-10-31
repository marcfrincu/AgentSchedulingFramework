package modules.communication;

import java.io.IOException;
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
 * Communication module used by the Healing module
 * @author Marc Frincu
 * @since 2010
 *
 */
public class CommunicationAMQPHealing implements ICommunicationAMQP {

	private static Logger logger = Logger.getLogger(
			CommunicationAMQPHealing.class.getPackage().getName());	

	private static String QUEUE_REGISTRATION_NAME = "asf.registration.queue";
	private static String QUEUE_ACTIVATION_NAME = "asf.activation.queue";
	private static String QUEUE_SIMILAR_REQUEST_NAME = "asf.similar.request.queue";
	private static String QUEUE_SIMILAR_RESPONSE_NAME = "asf.similar.response.queue";
	private static String QUEUE_REGISTRATION_HEALER_NAME = "asf.registration.healer.queue";
	
	private Connection conn = null;
	private Vector<Channel> channels = null;
	private Vector<QueueingConsumer> consumers = null;
	
	private String agentId = null;
	String uuid ;
	
	public CommunicationAMQPHealing(String agentId) {
		
		uuid = "";//UUID.randomUUID().toString();

		
		this.agentId = agentId;
		try {			
			this.init();
		} catch (Exception e) {
			e.printStackTrace();
			CommunicationAMQPHealing.logger.fatal("Cannot initialise AMQP. Message: " + 
					e.getMessage());
			System.exit(0);
		}
	}
	
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
		
		this.consumers = new Vector<QueueingConsumer>();		
		
		CommunicationAMQPHealing.QUEUE_ACTIVATION_NAME = 
			SystemSettings.getSystemSettings().getQueue_activation() + ".queue";
		CommunicationAMQPHealing.QUEUE_REGISTRATION_NAME = 
			SystemSettings.getSystemSettings().getQueue_registration() + ".queue";
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(0).exchangeDeclare(CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(0).queueDeclare(CommunicationAMQPHealing.QUEUE_ACTIVATION_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(0).queueBind(CommunicationAMQPHealing.QUEUE_ACTIVATION_NAME+ this.agentId + uuid, 
				CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME, 
								"asf.activation");

		this.consumers.add(new QueueingConsumer(this.channels.get(0)));

		// the bid request as direct type for broadcasting purposes
		this.channels.get(1).exchangeDeclare(CommunicationAMQPHealing.EXCHANGE_REGISTRATION_NAME, 
										"direct", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(1).queueDeclare(CommunicationAMQPHealing.QUEUE_REGISTRATION_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(1).queueBind(CommunicationAMQPHealing.QUEUE_REGISTRATION_NAME + this.agentId + uuid, 
				CommunicationAMQPHealing.EXCHANGE_REGISTRATION_NAME, 
								SystemSettings.getSystemSettings().getRouting_key_registration());

		this.consumers.add(new QueueingConsumer(this.channels.get(1)));
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(2).exchangeDeclare(CommunicationAMQPHealing.EXCHANGE_SIMILAR_REQUEST_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(2).queueDeclare(CommunicationAMQPHealing.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(2).queueBind(CommunicationAMQPHealing.QUEUE_SIMILAR_REQUEST_NAME+ this.agentId + uuid, 
				CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_REQUEST_NAME, 
								"asf.similar.request");
		
		this.consumers.add(new QueueingConsumer(this.channels.get(2)));

		// the bid request as fanout type for broadcasting purposes
		this.channels.get(3).exchangeDeclare(CommunicationAMQPHealing.EXCHANGE_SIMILAR_RESPONSE_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(3).queueDeclare(CommunicationAMQPHealing.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(3).queueBind(CommunicationAMQPHealing.QUEUE_SIMILAR_RESPONSE_NAME+ this.agentId + uuid, 
				CommunicationAMQPNegotiator.EXCHANGE_SIMILAR_RESPONSE_NAME, 
								"asf.similar.response");
		
		this.consumers.add(new QueueingConsumer(this.channels.get(3)));

		// the bid request as direct type for broadcasting purposes
		this.channels.get(4).exchangeDeclare(CommunicationAMQPHealing.EXCHANGE_REGISTRATION_HEALER_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(4).queueDeclare(CommunicationAMQPHealing.QUEUE_REGISTRATION_HEALER_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(4).queueBind(CommunicationAMQPHealing.QUEUE_REGISTRATION_HEALER_NAME + this.agentId + uuid, 
				CommunicationAMQPHealing.EXCHANGE_REGISTRATION_HEALER_NAME, 
								"asf.registration.healer");

		this.consumers.add(new QueueingConsumer(this.channels.get(4)));


	}
	
	@Override
	public void onMessage() throws IOException {
		for (int i=0; i< SystemSettings.getSystemSettings().getHealing_msg_batch_no(); i++) {
			//CommunicationAMQPHealing.logger.info("Processing AGENT_ACTIVATION message");		
			this.processMessage(CommunicationAMQPHealing.QUEUE_ACTIVATION_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_ACTIVATION_REQUEST);
			//CommunicationAMQPHealing.logger.info("Processing AGENT_REGISTRATION message");
			this.processMessage(CommunicationAMQPHealing.QUEUE_REGISTRATION_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_REGISTRATION_REQUEST);
			//CommunicationAMQPHealing.logger.info("Processing AGENT_SIMILAR_REQUEST message");
			this.processMessage(CommunicationAMQPHealing.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REQUEST);
			//CommunicationAMQPHealing.logger.info("Processing AGENT_SIMILAR_RESPONSE message");
			this.processMessage(CommunicationAMQPHealing.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY);
			//CommunicationAMQPHealing.logger.info("Processing AGENT_REGISTRATION_HEALER message");
			this.processMessage(CommunicationAMQPHealing.QUEUE_REGISTRATION_HEALER_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_HEALING_REGISTRATION_REQUEST);	
		}
	}

	@Override
	public boolean sendMessage(Message msg, String exchangeName) {
		if (exchangeName.compareTo(
				CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPHealing.EXCHANGE_REGISTRATION_NAME) != 0 &&
						exchangeName.compareTo(
								CommunicationAMQPHealing.EXCHANGE_SIMILAR_REQUEST_NAME) != 0 &&
								exchangeName.compareTo(
										CommunicationAMQPHealing.EXCHANGE_SIMILAR_RESPONSE_NAME) != 0 &&
								exchangeName.compareTo(CommunicationAMQPHealing.EXCHANGE_REGISTRATION_HEALER_NAME) != 0) {
				CommunicationAMQPHealing.logger.error("Destination exchange not valid");
			return false;
		}
		
		String routingKey = null;
		int index = 0;
		if (exchangeName.compareTo(
				CommunicationAMQPHealing.EXCHANGE_ACTIVATION_NAME) == 0) {
			routingKey = "asf.activation";
			index = 0;
		}
		if (exchangeName.compareTo(
				CommunicationAMQPHealing.EXCHANGE_REGISTRATION_NAME) == 0) {
			routingKey = SystemSettings.getSystemSettings().getRouting_key_registration();
			index = 1;
		}		

		if (exchangeName.compareTo(
				CommunicationAMQPHealing.EXCHANGE_SIMILAR_REQUEST_NAME) == 0) {
			routingKey = "asf.similar.request";
			index = 2;
		}		

		if (exchangeName.compareTo(
				CommunicationAMQPHealing.EXCHANGE_SIMILAR_RESPONSE_NAME) == 0) {
			routingKey = "asf.similar.response";
			index = 3;
		}		
		
		if (exchangeName.compareTo(
				CommunicationAMQPHealing.EXCHANGE_REGISTRATION_HEALER_NAME) == 0) {
			routingKey = "asf.registration.healer";
			index = 4;
		}		
		
		// convert to JSON
		final String str = JSONHandler.makeTaskMessage(msg.getFromId(),
									msg.getToId(),
									msg.getContent(),
									msg.getProcessingName());
		
		byte[] messageBodyBytes = str.getBytes();
	    
		try {
			//CommunicationAMQPHealing.logger.debug("SCH exchange: " + 
			//		exchangeName + 
			//		" routing key: " + routingKey); 
			
			this.channels.get(index).basicPublish(exchangeName, 
									routingKey, 
					  				MessageProperties.PERSISTENT_TEXT_PLAIN, 
					  				messageBodyBytes) ;
		} catch (IOException e) {
			CommunicationAMQPHealing.logger.error("Message: " + str + 
					" could not be sent. Message: " + e.getMessage());
			return false;
		}
		return true;
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
			CommunicationAMQPHealing.logger.error("Could not close connection or " +
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
				case AGENT_MODULE_ACTIVATION_REQUEST:
					this.channels.get(0).basicConsume(queueName, 
							false, 
							this.consumers.get(0));
					
			    	delivery = this.consumers.get(0).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_REGISTRATION_REQUEST:
					this.channels.get(1).basicConsume(queueName, 
							false, 
							this.consumers.get(1));
					
			    	delivery = this.consumers.get(1).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
					this.channels.get(2).basicConsume(queueName, 
							false, 
							this.consumers.get(2));
					
			    	delivery = this.consumers.get(2).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
					this.channels.get(3).basicConsume(queueName, 
							false, 
							this.consumers.get(3));
					
			    	delivery = this.consumers.get(3).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_HEALING_REGISTRATION_REQUEST:
					this.channels.get(4).basicConsume(queueName, 
							false, 
							this.consumers.get(4));
					
			    	delivery = this.consumers.get(4).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				default:
					CommunicationAMQPHealing.logger.error("Invalid message type" + type.toString() 
							+ " in the context of message consuming");
					return;
			}
			
	    	if (delivery == null)
	    		return;
	    	
	    	//CommunicationAMQPHealing.logger.debug("Message received: " + 
			//		new String(delivery.getBody()));
	        
	        switch (type) {
				case AGENT_MODULE_ACTIVATION_REQUEST:
			        this.channels.get(0).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case AGENT_MODULE_REGISTRATION_REQUEST:
					this.channels.get(1).basicAck(delivery.getEnvelope().getDeliveryTag(), false);					
					break;
				case AGENT_MODULE_HEALING_REGISTRATION_REQUEST:
					this.channels.get(4).basicAck(delivery.getEnvelope().getDeliveryTag(), false);					
					break;
	        }
	        
	        //extract message from JSON
	        modules.communication.json.beans.Message msg = JSONHandler.getMessage(new String(delivery.getBody()));
	        if (msg == null) {
	        	CommunicationAMQPHealing.logger.error("Message not well formed");
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
    	   CommunicationAMQPHealing.logger.error("Error retrieving message content. " +
       			"Message: " + ie.getMessage());
       }
	}
	
}
