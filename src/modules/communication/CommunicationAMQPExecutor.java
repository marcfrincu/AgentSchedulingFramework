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
 * Communication module used by the Executor module
 * @author Marc Frincu
 * @since 2010
 *
 */
public class CommunicationAMQPExecutor implements ICommunicationAMQP {

	private static Logger logger = Logger.getLogger(
			CommunicationAMQPExecutor.class.getPackage().getName());
		
	private static String QUEUE_ACTIVATION_NAME = "asf.activation.queue";
	private static String QUEUE_SIMILAR_REQUEST_NAME = "asf.similar.request.queue";
	private static String QUEUE_SIMILAR_RESPONSE_NAME = "asf.similar.response.queue";
	
	private Connection conn = null;
	private Vector<Channel> channels = null;
	private Vector<QueueingConsumer> consumers = null;
	
	private String agentId = null;
	String uuid;
	public CommunicationAMQPExecutor(String agentId) {
		uuid = UUID.randomUUID().toString();
		this.agentId = agentId;
		try {
			this.init();
		} catch (Exception e) {
			CommunicationAMQPExecutor.logger.fatal("Cannot initialise AMQP. Message: " + 
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

		
		this.consumers = new Vector<QueueingConsumer>();
			
		CommunicationAMQPExecutor.QUEUE_ACTIVATION_NAME = 
			SystemSettings.getSystemSettings().getQueue_activation() + ".queue";
				
	
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(0).exchangeDeclare(CommunicationAMQPExecutor.EXCHANGE_REGISTRATION_NAME, 
										"direct", 
										true, false, null);
		
		// the bid request as fanout type for broadcasting purposes
		this.channels.get(1).exchangeDeclare(CommunicationAMQPExecutor.EXCHANGE_ACTIVATION_NAME, 
										"fanout", 
										true, false, null);
		
		// Consumer for the bid request queue
		this.channels.get(1).queueDeclare(CommunicationAMQPExecutor.QUEUE_ACTIVATION_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(1).queueBind(CommunicationAMQPExecutor.QUEUE_ACTIVATION_NAME + this.agentId + uuid, 
				CommunicationAMQPExecutor.EXCHANGE_ACTIVATION_NAME, 
								"asf.activation");
		//1
		this.consumers.add(new QueueingConsumer(this.channels.get(1)));
		
		this.channels.get(2).exchangeDeclare(CommunicationAMQPExecutor.EXCHANGE_SIMILAR_REQUEST_NAME, 
										"fanout", 
										true, false, null);
		
		this.channels.get(2).queueDeclare(CommunicationAMQPExecutor.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(2).queueBind(CommunicationAMQPExecutor.QUEUE_SIMILAR_REQUEST_NAME+ this.agentId + uuid, 
				CommunicationAMQPExecutor.EXCHANGE_SIMILAR_REQUEST_NAME, 
								"asf.similar.request");
		//2
		this.consumers.add(new QueueingConsumer(this.channels.get(2)));

		this.channels.get(3).exchangeDeclare(CommunicationAMQPExecutor.EXCHANGE_SIMILAR_RESPONSE_NAME, 
										"fanout", 
										true, false, null);
		
		this.channels.get(3).queueDeclare(CommunicationAMQPExecutor.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid, 
				true, 
				false, 
				false, 
				null);
		
		this.channels.get(3).queueBind(CommunicationAMQPExecutor.QUEUE_SIMILAR_RESPONSE_NAME+ this.agentId + uuid, 
				CommunicationAMQPExecutor.EXCHANGE_SIMILAR_RESPONSE_NAME, 
								"asf.similar.response");
		//3
		this.consumers.add(new QueueingConsumer(this.channels.get(3)));

	}
	
	
	@Override
	public void onMessage() throws IOException {
		for (int i=0; i< SystemSettings.getSystemSettings().getExecutor_msg_batch_no(); i++) {
			//CommunicationAMQPExecutor.logger.info("Processing AGENT_MODULE_ACTIVATION_REQUEST message");
			this.processMessage(CommunicationAMQPExecutor.QUEUE_ACTIVATION_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_ACTIVATION_REQUEST);
			//CommunicationAMQPExecutor.logger.info("Processing AGENT_MODULE_CHECK_SIMILAR_REQUEST message");
			this.processMessage(CommunicationAMQPExecutor.QUEUE_SIMILAR_REQUEST_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REQUEST);
			//CommunicationAMQPExecutor.logger.info("Processing AGENT_MODULE_CHECK_SIMILAR_REPLY message");
			this.processMessage(CommunicationAMQPExecutor.QUEUE_SIMILAR_RESPONSE_NAME + this.agentId + uuid,
					Message.TYPE.AGENT_MODULE_CHECK_SIMILAR_REPLY);
		}
	}

	@Override
	public boolean sendMessage(Message msg, String exchangeName) {
		if (exchangeName.compareTo(
						CommunicationAMQPExecutor.EXCHANGE_ACTIVATION_NAME) != 0 &&
				exchangeName.compareTo(
						CommunicationAMQPExecutor.EXCHANGE_REGISTRATION_NAME) != 0	&&
						exchangeName.compareTo(
								CommunicationAMQPExecutor.EXCHANGE_SIMILAR_REQUEST_NAME) != 0 &&
								exchangeName.compareTo(
										CommunicationAMQPExecutor.EXCHANGE_SIMILAR_RESPONSE_NAME) != 0) {
			CommunicationAMQPExecutor.logger.error("Destination exchange not valid");
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
				CommunicationAMQPExecutor.EXCHANGE_ACTIVATION_NAME) == 0) {
			routingKey = "asf.activation";
			index = 1;
		}
		if (exchangeName.compareTo(
				CommunicationAMQPExecutor.EXCHANGE_REGISTRATION_NAME) == 0) {
			routingKey = SystemSettings.getSystemSettings().getRouting_key_registration();
			index = 0;

		}	
		if (exchangeName.compareTo(
				CommunicationAMQPExecutor.EXCHANGE_SIMILAR_REQUEST_NAME) == 0) {
			routingKey = "asf.similar.request";
			index = 2;

		}	
		if (exchangeName.compareTo(
				CommunicationAMQPExecutor.EXCHANGE_SIMILAR_RESPONSE_NAME) == 0) {
			routingKey = "asf.similar.response";
			index = 3;

		}	
		
		try {
			//CommunicationAMQPExecutor.logger.debug("NEG exchange: " + 
			//		exchangeName + " routing key: " +
			//		routingKey); 

			this.channels.get(index).basicPublish(exchangeName, 
									routingKey, 
					  				MessageProperties.PERSISTENT_TEXT_PLAIN, 
					  				messageBodyBytes) ;	  				
		} catch (IOException e) {
			CommunicationAMQPExecutor.logger.error("Message: " + str + 
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
			CommunicationAMQPExecutor.logger.error("Could not close connection or " +
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
				case AGENT_MODULE_ACTIVATION_REQUEST:
					this.channels.get(1).basicConsume(queueName, 
							false, 
							this.consumers.get(0));
					
			    	delivery = this.consumers.get(0).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
					this.channels.get(2).basicConsume(queueName, 
							false, 
							this.consumers.get(1));
					
			    	delivery = this.consumers.get(1).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
					this.channels.get(3).basicConsume(queueName, 
							false, 
							this.consumers.get(2));
					
			    	delivery = this.consumers.get(2).nextDelivery(SystemSettings.getSystemSettings().getMessage_timeout());
					break;
				default: 
					CommunicationAMQPExecutor.logger.error("Invalid message type" + type.toString() 
							+ " in the context of message consuming");
					return;				
			}
	    	
	    	if (delivery == null)
	    		return;
	    	
			switch (type) {
				case AGENT_MODULE_ACTIVATION_REQUEST:
			        this.channels.get(1).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REQUEST:
					this.channels.get(2).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
				case AGENT_MODULE_CHECK_SIMILAR_REPLY:
					this.channels.get(3).basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					break;
			}
	    	
			//CommunicationAMQPExecutor.logger.debug("Message received: " + 
			//		new String(delivery.getBody()));
	        
	        //extract message from JSON
	        modules.communication.json.beans.Message msg = JSONHandler.getMessage(new String(delivery.getBody()));
	        if (msg == null) {
	        	CommunicationAMQPExecutor.logger.error("Message not well formed");
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
        	CommunicationAMQPExecutor.logger.error("Error retrieving message content. " +
        			"Message: " + ie.getMessage());
        }
        catch (IOException ioe) {
        	CommunicationAMQPExecutor.logger.error("IO error when retrieving message content. " +
        			"Message: " + ioe.getMessage());
        }
        catch (Exception e) {
        	CommunicationAMQPExecutor.logger.error("Unexpected error. " +
        			"Message: " + e.getMessage());
        	e.printStackTrace();
        }
	}
}
