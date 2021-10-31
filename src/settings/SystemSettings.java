package settings;

import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * Class handling the <i>system.properties</i> file for the scheduling simulator
 * 
 * @author Marc Frincu
 * @since 2010
 */
public class SystemSettings {

	/**
	 * The keys of the properties in the system.properties file.
	 */
	private enum PropertyKeys {
		negotiation_class,
		scheduling_class,
		queue_bid_request,
		queue_bid_response,
		queue_bid_result,
		queue_new_task,
		queue_rescheduling_task,
		message_timeout,
		mq_username,
		mq_password,
		mq_virtual_host,
		mq_host_name,
		mq_port_number,
		osyris_scheduling_rule_file,
		osyris_system_properties_file,
		negotiator_min_number_bid_responses,
		negotiator_bid_response_timeout,
		agent_token,
		hadoop_host,
		hadoop_port,
		hadoop_dir,
		local_dir,
		hadoop_user_credentials,
		healing_ping_timeout,
		healing_idle_time,
		negotiator_idle_time,
		scheduling_idle_time,
		executor_idle_time,
		queue_registration,		
		queue_activation,
		routing_key_registration,
		db_name,
		db_user,
		db_password,
		ssh_username,
		scheduling_msg_batch_no,
		negotiator_msg_batch_no,
		healing_msg_batch_no,
		executor_msg_batch_no,
		similar_search_timeout,
		negotiator_bid_selection_policy_class
	};

	private String negotiation_class, scheduling_class, queue_bid_request,
			queue_bid_response, queue_bid_result, queue_new_task, 
			queue_rescheduling_task, mq_username, mq_password,
			mq_virtual_host, mq_host_name, osyris_scheduling_rule_file,
			osyris_system_properties_file, agent_token, hadoop_host,
			hadoop_port, hadoop_dir, local_dir, hadoop_user_credentials,
			queue_registration, queue_activation,routing_key_registration,
			db_name, db_user, db_password, ssh_username, negotiator_bid_selection_policy_class;
			
	private int message_timeout, mq_port_number, negotiator_min_number_bid_responses,
			negotiator_bid_response_timeout, healing_ping_timeout, healing_idle_time,
			negotiator_idle_time, scheduling_idle_time,
			scheduling_msg_batch_no, negotiator_msg_batch_no, healing_msg_batch_no,
			executor_msg_batch_no, executor_idle_time, similar_search_timeout;


	public String getNegotiator_bid_selection_policy_class() {
		return negotiator_bid_selection_policy_class;
	}
	
	public int getSimilar_search_timeout() {
		return similar_search_timeout;
	}
	
	public int getExecutor_idle_time() {
		return executor_idle_time;
	}
	
	public int getExecutor_msg_batch_no() {
		return executor_msg_batch_no;
	}
		
	public int getScheduling_msg_batch_no() {
		return scheduling_msg_batch_no;
	}

	public int getNegotiator_msg_batch_no() {
		return negotiator_msg_batch_no;
	}

	public int getHealing_msg_batch_no() {
		return healing_msg_batch_no;
	}

	public String getSsh_username() {
		return this.ssh_username;
	}
	
	public String getDb_name() {
		return this.db_name;
	}
	
	public String getDb_user() {
		return this.db_user;
	}
	
	public String getDb_password() {
		return this.db_password;
	}
	
	public String getRouting_key_registration() {
		return routing_key_registration;
	}
	
	public String getQueue_activation() {
		return queue_activation;
	}
	
	public String getQueue_registration() {
		return this.queue_registration;
	}
	
	public int getNegotiator_idle_time() {
		return negotiator_idle_time;
	}
	
	public int getScheduling_idle_time() {
		return scheduling_idle_time;
	}
	
	public int getHealing_idle_time() {
		return healing_idle_time;
	}
	
	public int getHealing_ping_timeout() {
		return healing_ping_timeout;
	}
	
	public String getHadoop_host() {
		return hadoop_host;
	}
	
	public String getHadoop_port() {
		return hadoop_port;
	}
	
	public String getHadoop_dir() {
		return hadoop_dir;
	}
	
	public String getLocal_dir() {
		return local_dir;
	}
	
	public String getHadoop_user_credentials() {
		return hadoop_user_credentials;
	}
	
	public String getNegotiation_class() {
		return negotiation_class;
	}

	public String getScheduling_class() {
		return scheduling_class;
	}

	public String getQueue_bid_request() {
		return queue_bid_request;
	}

	public String getQueue_bid_response() {
		return queue_bid_response;
	}

	public String getQueue_bid_result() {
		return queue_bid_result;
	}

	public String getQueue_new_task() {
		return queue_new_task;
	}

	public String getQueue_rescheduling_task() {
		return queue_rescheduling_task;
	}

	public String getMq_username() {
		return mq_username;
	}

	public String getMq_password() {
		return mq_password;
	}

	public String getMq_virtual_host() {
		return mq_virtual_host;
	}

	public String getMq_host_name() {
		return mq_host_name;
	}

	public String getOsyris_scheduling_rule_file() {
		return osyris_scheduling_rule_file;
	}

	public String getOsyris_system_properties_file() {
		return osyris_system_properties_file;
	}

	public int getMessage_timeout() {
		return message_timeout;
	}

	public int getMq_port_number() {
		return mq_port_number;
	}

	public int getNegotiator_min_number_bid_responses() {
		return negotiator_min_number_bid_responses;
	}

	public int getNegotiator_bid_response_timeout() {
		return negotiator_bid_response_timeout;
	}

	public String getAgentToken() {
		return this.agent_token;
	}
	
	/**
	 * The system settings object.
	 */
	private static SystemSettings settings = null;

	/**
	 * Private constructor.
	 */
	private SystemSettings() {
	}

	/**
	 * Returns the system settings object.
	 * <p>
	 * 
	 * @return the system settings object
	 */
	public static SystemSettings getSystemSettings() {
		if (SystemSettings.settings == null) {
			SystemSettings.settings = new SystemSettings();
		}
		return SystemSettings.settings;
	}
	
	/**
	 * This method loads the properties from the property file
	 * "system.properties" previously added to the class path either explicit or
	 * implicit by being part of a .jar file added to the CLASSPATH.
	 * <p>
	 * 
	 * @param propertiesFilePath
	 *            path to properties file
	 */
	public void loadProperties(String propertiesFilePath) {

		Properties props = new Properties();

		URL url = SystemSettings.class.getClassLoader().getResource(
				propertiesFilePath);

		if (url == null) {
			throw new MissingResourceException(
					"Unable to load the properties file." + " File not found: "
							+ url, null, null);
		}
		try {
			props.load(url.openStream());
		} catch (IOException e) {
			throw new MissingResourceException(
					"The properties file cannot be accessed.", null, null);
		}

		validateFile(props);

		// read the properties
		this.mq_virtual_host = props.getProperty(
								PropertyKeys.mq_virtual_host.toString());
		this.mq_username = props.getProperty(
				PropertyKeys.mq_username.toString());
		this.mq_password = props.getProperty(
				PropertyKeys.mq_password.toString());
		this.mq_host_name = props.getProperty(
				PropertyKeys.mq_host_name.toString());
		this.mq_port_number = Integer.parseInt(props.getProperty(
				PropertyKeys.mq_port_number.toString()));
		this.negotiation_class = props.getProperty(
				PropertyKeys.negotiation_class.toString());
		this.scheduling_class = props.getProperty(
				PropertyKeys.scheduling_class.toString());
		this.queue_bid_request = props.getProperty(
				PropertyKeys.queue_bid_request.toString());
		this.queue_bid_response = props.getProperty(
				PropertyKeys.queue_bid_response.toString());
		this.queue_bid_result = props.getProperty(
				PropertyKeys.queue_bid_result.toString());
		this.queue_new_task = props.getProperty(
				PropertyKeys.queue_new_task.toString());
		this.queue_rescheduling_task = props.getProperty(
				PropertyKeys.queue_rescheduling_task.toString());
		this.message_timeout = Integer.parseInt(props.getProperty(
				PropertyKeys.message_timeout.toString()));
		this.osyris_scheduling_rule_file = props.getProperty(
				PropertyKeys.osyris_scheduling_rule_file.toString());
		this.osyris_system_properties_file = props.getProperty(
				PropertyKeys.osyris_system_properties_file.toString());
		this.negotiator_bid_response_timeout = Integer.parseInt(props.getProperty(
				PropertyKeys.negotiator_bid_response_timeout.toString()));
		this.negotiator_min_number_bid_responses = Integer.parseInt(props.getProperty(
				PropertyKeys.negotiator_min_number_bid_responses.toString()));
		this.negotiator_bid_selection_policy_class = props.getProperty(
				PropertyKeys.negotiator_bid_selection_policy_class.toString());
		this.agent_token = props.getProperty(PropertyKeys.agent_token.toString());
		this.hadoop_host = props.getProperty(PropertyKeys.hadoop_host.toString());
		this.hadoop_port = props.getProperty(PropertyKeys.hadoop_port.toString());
		this.hadoop_dir = props.getProperty(PropertyKeys.hadoop_dir.toString());
		this.local_dir = props.getProperty(PropertyKeys.local_dir.toString());
		this.hadoop_user_credentials = props.getProperty(PropertyKeys.hadoop_user_credentials.toString());
		this.healing_ping_timeout = Integer.parseInt(props.getProperty(
				PropertyKeys.healing_ping_timeout.toString()));
		this.healing_idle_time = Integer.parseInt(props.getProperty(
				PropertyKeys.healing_idle_time.toString()));
		this.negotiator_idle_time = Integer.parseInt(props.getProperty(
				PropertyKeys.negotiator_idle_time.toString()));
		this.scheduling_idle_time = Integer.parseInt(props.getProperty(
				PropertyKeys.scheduling_idle_time.toString()));		
		this.queue_activation = props.getProperty(
				PropertyKeys.queue_activation.toString());
		this.queue_registration = props.getProperty(
				PropertyKeys.queue_registration.toString());
		this.routing_key_registration = props.getProperty(
				PropertyKeys.routing_key_registration.toString());
		this.db_name = props.getProperty(
				PropertyKeys.db_name.toString());
		this.db_user = props.getProperty(
				PropertyKeys.db_user.toString());
		this.db_password = props.getProperty(
				PropertyKeys.db_password.toString());
		this.ssh_username = props.getProperty(
				PropertyKeys.ssh_username.toString());
		this.scheduling_msg_batch_no = Integer.parseInt(props.getProperty(
				PropertyKeys.scheduling_msg_batch_no.toString()));
		this.negotiator_msg_batch_no = Integer.parseInt(props.getProperty(
				PropertyKeys.negotiator_msg_batch_no.toString()));
		this.healing_msg_batch_no = Integer.parseInt(props.getProperty(
				PropertyKeys.healing_msg_batch_no.toString()));
		this.executor_msg_batch_no = Integer.parseInt(props.getProperty(
				PropertyKeys.executor_msg_batch_no.toString()));
		this.executor_idle_time = Integer.parseInt(props.getProperty(
				PropertyKeys.executor_idle_time.toString()));
		this.similar_search_timeout = Integer.parseInt(props.getProperty(
				PropertyKeys.similar_search_timeout.toString()));
	}

	/**
	 * Logs an error message and throws a MissingResourceException.
	 * <p>
	 * 
	 * @param mssg
	 *            the message
	 * @param e
	 *            any exception that may have caused the error
	 */
	private void error(String mssg, Exception e) {
		Logger logger = Logger.getLogger(SystemSettings.class.getPackage()
				.getName());

		logger.error(mssg, e);
		throw new MissingResourceException(mssg, null, null);
	}

	/**
	 * Validates the properties file.
	 * <p>
	 * 
	 * @param props
	 *            the properties object
	 * @return <code>true</code> if all entries are valid, <code>false</code>
	 *         otherwise
	 */
	private boolean validateFile(Properties props) {
		String loc = SystemSettings.class.getSimpleName()
				+ ".validateFile() - ";

		String keyName, keyValue;

		PropertyKeys[] properties = PropertyKeys.values();
		for (PropertyKeys pk : properties) {
			keyName = pk.name();
			keyValue = props.getProperty(keyName);
			if (((keyValue == null) || (keyValue.trim().compareTo("") == 0))) {
				error(loc + "Missing or illegal value in settings file"
						+ " for key: " + keyName, null);
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		throw new UnsupportedOperationException();
	}
}
