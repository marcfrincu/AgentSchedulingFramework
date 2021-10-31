package modules;

import java.lang.reflect.Constructor;

import modules.communication.ICommunicationAMQP;
import modules.communication.Message;
import modules.communication.json.JSONHandler;
import modules.communication.json.beans.Task;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.AgentService;
import services.TaskService;
import services.TraceService;
import services.utils.DbTask.TASK_STATUS;
import settings.SystemSettings;


/**
 * Module used for scheduling tasks
 * @author Marc Frincu
 * @since 2010
 *
 */
public class Scheduling extends BasicModule {

	private static Logger logger = Logger.getLogger(Scheduling.class
			.getPackage().getName());


	private static Scheduling sched = null;
	
	private TaskService ts = null;
			
	private Scheduling() throws Exception {
		this.ts = new TaskService();
		this.as = new AgentService();
		this.trs = new TraceService();
		
		this.type = MODULE_TYPE.SCHEDULING;
		Scheduling.logger.setLevel(Level.DEBUG);
	}
	
	/**
	 * Each module is built as a singleton.
	 * @return a reference to the <i>Scheduling</i> object
	 */
	public static Scheduling getSchedulingModule() {
		if (Scheduling.sched == null) {
			try {
				Scheduling.sched = new Scheduling();
			} catch (Exception e) {
				Scheduling.logger.fatal("Could not start OSyRIS workflow." +
						" Message: " + e.getMessage());
				return null;
			}
			return Scheduling.sched;
		}
		else
			return Scheduling.sched;
	}
	
	
	
	/**
	 * Sends a message to another agent queue
	 * @param fromId the sending agent's ID
	 * @param toId the receiver agent's ID
	 * @param content the content of the message
	 * @param processingName the name of the atom as described by the 
	 * <i>processing</i>
	 * meta-attribute in the SiLK file 
	 */
	
	@Override
	public boolean sendMessage(String fromId, 
							String toId, 
							String content, 
							String processingName) {
		/*try {
			this.com.sendMessage(new Message(fromId, 
					toId, 
					content, 
					processingName, 
					Message.TYPE.valueOf(processingName))
					, null);
			return true;
		} catch (Exception e) {
			Scheduling.logger.error("Error sending message: " + e.getMessage());
			return false;
		}*/
		return false;
	}

	@Override
	public void bindCommunicator() {
		try {			
			Class<?> cls = Class.forName(
					SystemSettings.getSystemSettings().getScheduling_class());
			Constructor<?> constructor = cls.getConstructor(new Class[]{String.class});
			this.com = (ICommunicationAMQP) constructor.newInstance(this.uuid); 
		} catch (Exception e) {
			e.printStackTrace();
			Scheduling.logger.fatal("Failed to create communication channel: " 
					+ e.getMessage());
			System.exit(0);
		}		
	}

	@Override
	public void postMessageProcessingOperations() {
		// TODO Add scheduling policy here		
	}

	@Override
	public void preMessageProcessingOperations() {
		// TODO add here or in previous method the BID_RESPONSE message as a response to a bid
		return;	
	}

	Task task = null;
	String taskId = null;
	@Override
	public void processModuleDependentMessages(Message msg) {
		try {
			switch (msg.getType()) {
				case SCHEDULING_POLICY_CHANGE:
					if (this.status != MODULE_STATUS.RUNNING) {
						break;
					}
					//TODO add logic. this message should be sent only by the SchedulingAnalyzer
					break;
				case BID_REQUEST:
					if (this.status != MODULE_STATUS.RUNNING) {
						break;
					}
					Scheduling.logger.info("Received BID_REQUEST message");
					task = JSONHandler.getTask(msg.getContent());
					try {
						taskId = this.ts.addTask(Integer.toString(task.getOriginal_agent_id()), 
								task.getDescription(), 
								task.getExecutable_location(), 
								task.getDependencies(), 
								task.getUuid(), 
								TASK_STATUS.SUBMITTED.toString(), 
								task.getSubmiter_agent_uuid());
						Scheduling.logger.info("Added task with ID: " + taskId);
					} catch (Exception e) {
						Scheduling.logger.error("Error inserting task into DB. Message: " +
								e.getMessage());							
					}
					break;
				case BID_WINNER:
					if (this.status != MODULE_STATUS.RUNNING) {
						break;
					}
					Scheduling.logger.info("Received BID_WINNER message");
					task = JSONHandler.getTask(msg.getContent());
					taskId = this.ts.getTaskByUuid(task.getUuid());
					if (taskId.compareTo(Scheduling.EMPTY_SYMBOL) == 0) {
						Scheduling.logger.error("Could not locate task with UUID: " +
								task.getUuid());
						break;
					}
					// if the UUID do not match delete temporary task
					// i.e. this agent was not the best bidder
					if (this.uuid.compareTo(msg.getToId()) != 0) { 								
						if (Boolean.getBoolean(this.ts.deleteTemporaryTask(taskId)) == false) {
							Scheduling.logger.error("Could not delete temporary task with UUID: " +
								task.getUuid());
						}
						Scheduling.logger.info("Deleted temporary task with UUID: " + 
								task.getUuid());
					}
					// if the UUID match change task status to ASSIGNED
					else {
						this.ts.assignTask(taskId, this.as.getAgentIDByUuid(msg.getToId()));
						Scheduling.logger.info("Assigned to agent temporary task with UUID: " + 
								task.getUuid());
						// add trace
						this.trs.addTrace(this.uuid, 
								this.getWorkflowUuid(), 
								this.type.toString(), 
								"end negotiation task " + taskId, 
								System.currentTimeMillis());
	
					}
					break;
				default:
					Scheduling.logger.error("Task type: " + 
							msg.getType().toString() + 
							" not allowed in this context");
					break;
			}
		}
		catch (Exception e) {
			Scheduling.logger.error("Error while processing Scheduling specific messages: " + 
					e.getMessage());
		}
	}
	
	@Override
	public void sleepFor() {
		try {
			Thread.sleep(SystemSettings.getSystemSettings().getScheduling_idle_time());
		} catch (InterruptedException e) {
			Scheduling.logger.fatal("Error attempting to sleep: " + e.getMessage());
		}		
	}
}
