package utils.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This class is responsible for copying and starting remote applications
 * @author Marc Frincu
 * @since 2010
 */
public class Executor {
	private static Logger logger = Logger.getLogger(Executor.class
			.getPackage().getName());
		
	/**
	 * Default constructor
	 */
	public Executor() {		
	}
	
	/**
	 * Executes a list of commands. The command could be executed locally or remotely
	 * @param cmds the list of commands
	 * @return true if the commands have been successfully executed, or false otherwise
	 */
	public void runCommands(Vector<String> cmds) {
		new Execute(cmds).start();
	}
	
	/**
	 * Class for reading the standard output
	 * @author Marc Frincu
	 *
	 */
	class ReadStdOut extends Thread {
		BufferedReader buff = null;
		StringBuffer output;
		
		public ReadStdOut(BufferedReader buff, StringBuffer output) {
			this.buff = buff;
			this.output = output;
		}
	
		public void run() {
			String tempBuf;
			Executor.logger.info("StdOut:");
			try {
				while((tempBuf = this.buff.readLine()) != null) {
					this.output.append(tempBuf);
					this.output.append("\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Class for reading the standard error
	 * @author Marc Frincu
	 *
	 */
	class ReadStdErr extends Thread {
		BufferedReader buff = null;
		StringBuffer output;
		
		
		public ReadStdErr(BufferedReader buff, StringBuffer output) {		
			this.buff = buff;
			this.output = output; 
		}
	
		public void run() {
			String tempBuf;
			Executor.logger.info("StdErr:");
			try {
				while((tempBuf = this.buff.readLine()) != null) {
					this.output.append(tempBuf);
					this.output.append("\n");
					//Executor.logger.info(tempBuf);
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	class Execute extends Thread {
		private StringBuffer stdout, stderr;
		private Vector<String> cmds = null;
		/**
		 * Retrieves the standard output as given by the execution of the commands
		 * @return
		 */
		public String getStdOut() {
			return this.stdout.toString();
		}
		
		/**
		 * Retrieves the standard error as given by the execution of the commands
		 * @return
		 */
		public String getStdErr() {
			return this.stderr.toString();
		}

		
		
		public Execute (Vector<String> cmds) {
			this.cmds = cmds;
		}
		
		public void run() {
			Runtime rt = Runtime.getRuntime();
			Process shell = null;

			try {
				ReadStdOut rsi = null;
				ReadStdErr rse = null;
				Thread t1, t2;			
					
				this.stderr = new StringBuffer();
				this.stdout = new StringBuffer();
				
				Executor.logger.debug("Start executing commands at : " + System.currentTimeMillis()); 
				
				for (String cmd : cmds) {
					Executor.logger.info("Executing command:\n" + cmd);
					shell = rt.exec(new String[] {"/bin/sh", "-c", cmd});				
				
					if (this.stderr.length()>0)
						this.stderr.delete(0, this.stderr.length()-1);
					if (this.stdout.length()>0)
						this.stdout.delete(0, this.stdout.length()-1);

					//if the command is abuot to start a script do not catch its std{in|out} just start it
					//this is to avoid blocking calls when deploying from a console
					if (!cmd.trim().endsWith("&")) {
						rsi = new ReadStdOut(new BufferedReader(
												new InputStreamReader(shell.getInputStream())), this.stdout);
						rse = new ReadStdErr(new BufferedReader(
												new InputStreamReader(shell.getErrorStream())), this.stderr);			
						
						t1 = new Thread(rsi);
						t2 = new Thread(rse);
						
						t1.start();
						t2.start();
						
						//Thread.sleep(1000);
						
						shell.waitFor();
											
						t1.interrupt();
						t2.interrupt();
						t1.join();
						t2.join();
					}
					
					Executor.logger.debug("End executing commands at: " + System.currentTimeMillis());
					
					if (this.stderr.toString().trim().length() != 0) {
						Executor.logger.error("Error executing command: " + cmd + 
										"\nMessage: " + this.stderr.toString());						
					}
				}				
				
			} catch (IOException e) {
				Executor.logger.error(e.getMessage());
				System.exit(0);
			} catch (InterruptedException e) {
				Executor.logger.error(e.getMessage());
				System.exit(0);
			}			

		}
	} 
}
