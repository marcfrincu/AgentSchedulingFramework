package modules.communication;

import java.io.IOException;
import java.util.Vector;

public interface ICommunicationAMQP {
	public String EXCHANGE_REGISTRATION_NAME = "asf.registration";
	public String EXCHANGE_ACTIVATION_NAME = "asf.activation";
	public String EXCHANGE_SIMILAR_REQUEST_NAME = "asf.similar.request";
	public String EXCHANGE_SIMILAR_RESPONSE_NAME = "asf.similar.response";
	public String EXCHANGE_REGISTRATION_HEALER_NAME = "asf.registration.healer";
	
	public Vector<Message> msgs = new Vector<Message>();
	
	/**
	 * Sends a message.
	 * @param msg the message to be sent
	 * @param exchangeName the name of the exchange where the message is to be sent 
	 * @return true if the message has been sent, false otherwise
	 */
	public boolean sendMessage(Message msg,
							String exchangeName);
	
	/**
	 * Handles the receive event of one message
	 * @throws IOException
	 */
	public void onMessage() throws IOException;
}
