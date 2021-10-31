package modules;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import modules.communication.CommunicationAMQPNegotiator;
import modules.communication.ICommunicationAMQP;
import modules.communication.Message;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.Task;
import modules.negotiation.IStrategy;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.AgentService;
import services.TraceService;
import settings.SystemSettings;

/**
 * Module used for negotiating between agents
 * 
 * @author Marc Frincu
 * @since 2010
 */
public class Negotiation extends BasicModule {
	
	private static Logger logger = Logger.getLogger(Negotiation.class
			.getPackage().getName());
	
	private Vector<Message> bidResponses = null;
	private Hashtable<String, Integer> bidCounts= null;
	IStrategy bidStrategy = null;
	
	private static Negotiation negotiation = null;
	
	@SuppressWarnings("unchecked")
	private Negotiation() {
		this.as = new AgentService();
		this.trs = new TraceService();
		
		this.bidResponses = new Vector<Message>();
		this.bidCounts = new Hashtable<String, Integer>();
		Negotiation.logger.setLevel(Level.DEBUG);
		
		this.type = MODULE_TYPE.NEGOTIATION;
		
		Class cls;
		try {
			cls = Class.forName(SystemSettings.getSystemSettings().getNegotiator_bid_selection_policy_class());
			this.bidStrategy = (IStrategy) cls.newInstance();
		} catch (ClassNotFoundException e) {			
			logger.fatal("Could not load bid policy class: " + SystemSettings.getSystemSettings().getNegotiator_bid_selection_policy_class());
			logger.fatal("Error: " + e.getMessage());
			System.exit(0);
		} catch (InstantiationException e) {
			logger.fatal("Could not load bid policy class: " + SystemSettings.getSystemSettings().getNegotiator_bid_selection_policy_class());
			logger.fatal("Error: " + e.getMessage());
			System.exit(0);
		} catch (IllegalAccessException e) {
			logger.fatal("Could not load bid policy class: " + SystemSettings.getSystemSettings().getNegotiator_bid_selection_policy_class());
			logger.fatal("Error: " + e.getMessage());
			System.exit(0);
		}	
	}
	
	/**
	 * Each module is built as a singleton.
	 * @return a reference to the <i>Healing</i> object
	 */
	public static Negotiation getNegotiationModule() {
		if (Negotiation.negotiation == null) {
			Negotiation.negotiation = new Negotiation();
			return Negotiation.negotiation;
		}
		else
			return Negotiation.negotiation;
	}
	
	/**
	 * Each module is built as a singleton.
	 * @return a reference to the <i>Negotiation</i> object
	 */
	/*
	public static Negotiation getNegotiationModule() {
		if (Negotiation.neg == null) {
			Negotiation.neg = new Negotiation();
			return Negotiation.neg;
		}
		else
			return Negotiation.neg;
	}
	*/
	
	
	@Override
	public boolean sendMessage(String fromId, String toId, String content,
			String processingName) {
		return false;
	}
		
	@Override
	public void bindCommunicator() {
		try {
			Class<?> cls = Class.forName(
						SystemSettings.getSystemSettings().getNegotiation_class());
			Constructor<?> constructor = cls.getConstructor(new Class[]{String.class});
			this.com = (ICommunicationAMQP) constructor.newInstance(this.uuid); 
		} catch (Exception e) {
			Negotiation.logger.fatal("Failed to create communication channel: " 
					+ e.getMessage());
		}		
	}

	@Override
	public void postMessageProcessingOperations() {
		return;
	}

	long lastTime = System.currentTimeMillis();
	
	
	Message msgRsp = null, msgBest = null, msg = null;
	@Override
	public void preMessageProcessingOperations() {

		long crtTime = 0;
		if (this.status == MODULE_STATUS.RUNNING) {				
			if (crtTime - lastTime > 
					SystemSettings.getSystemSettings().
							getNegotiator_bid_response_timeout()) {
				
				lastTime = System.currentTimeMillis();
				
				Negotiation.logger.info("Bid responses timeout. Sending current " +
				"results");
				
				//handle bid responses in case of timeout
				Enumeration<String> keys = this.bidCounts.keys();
				if (keys != null) {
					String key = null;
					while (keys.hasMoreElements()) {
						key = keys.nextElement();
						//TODO This is where the bid selection takes place. Use own implementation if required
						this.msgBest = this.bidStrategy.selectBestBid(key, this.bidResponses);
						if (this.msgBest == null) {
							Negotiation.logger.error("Error retrieving best offer." +
							" Possible task loss");
						}
						else {
							// create bid result broadcast message
							Negotiation.logger.debug("Winner ID for task: " + 
									key +
									" : " + this.msgBest.getFromId() + " with bid: " +
									JSONHandler.getTask(this.msgBest.getContent()).getEstimated_completion_time_on_resource());
							this.msgRsp = new Message(this.uuid,
									this.msgBest.getFromId(),
									this.msgBest.getContent(),
									Message.TYPE.BID_WINNER.toString(),
									Message.TYPE.BID_WINNER
							);
							
							this.com.sendMessage(msgRsp, 
									CommunicationAMQPNegotiator.
										EXCHANGE_BID_RESULT_NAME);
							
							// remove the bids for the task
							Negotiation.logger.debug("Size of bids before cleanup: " + 
									this.bidResponses.size());
							Negotiation.logger.info("Removing bids for task: " +
									key);
							this.bidCounts.remove(key);
							this.removeBids(key);
							Negotiation.logger.debug("Size of bids after cleanup: " + 
									this.bidResponses.size());
						}
					}		
				}
			}
		}
		
	}

	@Override
	public void processModuleDependentMessages(Message msg) {
		Task task = null;
		Integer bidCount = 0;
		switch (msg.getType()) {
			case BID_RESPONSE: //Message sent by a Scheduling module
				if (this.status != MODULE_STATUS.RUNNING) {
					break;
				}
				task = JSONHandler.getTask(msg.getContent());
				// add the bid response 
				this.bidResponses.add(msg);
				// add the bid count response for the received task UUID
				bidCount = this.bidCounts.get(task.getUuid());
				if (bidCount == null) {
					this.bidCounts.put(task.getUuid(), 1);
				}
				else {
					this.bidCounts.put(task.getUuid(), ++bidCount);
				}
				
				Negotiation.logger.debug("Task: " + task.getUuid() +
									" responses " + 
									this.bidCounts.get(task.getUuid()));
				
				// handle bid responses in case we received the desired number of replies
				if (this.bidCounts.get(task.getUuid()) >= 
							SystemSettings.getSystemSettings().
								getNegotiator_min_number_bid_responses()) {
					Negotiation.logger.info("Bid responses received. Sending " +
							"results");
					this.lastTime = System.currentTimeMillis();
					//TODO This is where the bid selection takes place. Use own implementation if required
					this.msgBest = this.bidStrategy.selectBestBid(task.getUuid(), this.bidResponses);
					if (this.msgBest == null) {
						Negotiation.logger.error("Error retrieving best offer." +
								" Possible task loss");
					}
					else {
						// create bid result broadcast message
						Negotiation.logger.debug("Winner ID for task: " + 
								task.getUuid() +
								" : " + this.msgBest.getFromId() + " with bid: " +
								JSONHandler.getTask(this.msgBest.getContent()).getEstimated_completion_time_on_resource());
						this.msgRsp = new Message(this.uuid,
								this.msgBest.getFromId(),
								this.msgBest.getContent(),
								Message.TYPE.BID_WINNER.toString(),
								Message.TYPE.BID_WINNER
						);
						
						this.com.sendMessage(this.msgRsp, 
								CommunicationAMQPNegotiator.
									EXCHANGE_BID_RESULT_NAME);									
						// remove the bids for the task
						Negotiation.logger.debug("Size of bids before cleanup: " + 
								this.bidResponses.size());
						Negotiation.logger.info("Removing bids for task: " +
								task.getUuid());
						this.bidCounts.remove(task.getUuid());
						this.removeBids(task.getUuid());
						Negotiation.logger.debug("Size of bids after cleanup: " + 
								this.bidResponses.size());
					}
				}
				break;
			// send the new task to the scheduling agents
			case NEW_TASK:
				if (this.status != MODULE_STATUS.RUNNING) {
					break;
				}
				this.msgRsp = new Message(this.uuid,
						msg.getFromId(),
						msg.getContent(),
						Message.TYPE.BID_REQUEST.toString(),
						Message.TYPE.BID_REQUEST
				);
				this.com.sendMessage(msgRsp, 
						CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME);
				//TODO find a better solution
				this.lastTime = System.currentTimeMillis();
				break;
			// send the task to the scheduling agents
			case RESCHEDULING_TASK: 
				if (this.status != MODULE_STATUS.RUNNING) {
					break;
				}
				this.msgRsp = new Message(this.uuid,
						msg.getFromId(),
						msg.getContent(),
						Message.TYPE.BID_REQUEST.toString(),
						Message.TYPE.BID_REQUEST
				);
				this.com.sendMessage(msgRsp, 
						CommunicationAMQPNegotiator.EXCHANGE_BID_REQUEST_NAME);
				//TODO find a better solution
				this.lastTime = System.currentTimeMillis();
				break;
			// message type not allowed in this context
			default: 
				Negotiation.logger.error("Task type: " + 
										msg.getType().toString() + 
										" not allowed in this context");
				break;
		}
		
	}

	@Override
	public void sleepFor() {
		try {
			Thread.sleep(SystemSettings.getSystemSettings().getNegotiator_idle_time());
		} catch (InterruptedException e) {
			Negotiation.logger.fatal("Error attempting to sleep: " + e.getMessage());
		}		
	}
	
	/**
	 * Removes all bids made for a certain task
	 * @param taskUUID
	 * @return true if the bids were removed, false if there were no bids
	 */
	private boolean removeBids(String taskUUID) {
		if (this.bidResponses == null || this.bidResponses.size() == 0) {
			return false;
		}
		Task task = null;
		int i=0; 
		while (i<this.bidResponses.size()) {
			task = JSONHandler.getTask(this.bidResponses.get(i).getContent());
			if (task.getUuid().compareTo(taskUUID) == 0) {
				this.bidResponses.remove(i);
			}
			else {
				i++;
			}
		}
		
		return true;
	}

}
