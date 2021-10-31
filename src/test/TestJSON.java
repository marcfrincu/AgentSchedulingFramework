package test;

import java.util.UUID;

import modules.communication.json.JSONHandler;
import modules.communication.json.beans.Message;
import modules.communication.json.beans.Task;
import modules.communication.json.beans.Winner;

/**
 * Tests JSON methods
 * @author Marc Frincu
 * @since 2010
 */
public class TestJSON {

	public static void main(String args[]) {
		
		// Create task JSON
		String task = JSONHandler.makeTask(1, 
							1, 
							"description", 
							"executable_location",
							"dependencies",
							0,
							System.currentTimeMillis(),
							System.currentTimeMillis(),
							2500,
							7000,
							false,
							2,
							UUID.randomUUID().toString(),
							false,
							UUID.randomUUID().toString());
		
		// Create winner JSON
		String winner = JSONHandler.makeWinnerMessage(UUID.randomUUID().toString());
				
		// Create message JSON
		String message = JSONHandler.makeTaskMessage(UUID.randomUUID().toString(),
											UUID.randomUUID().toString(),
											task,
											"processingName");
		
		// Display JSONs
		System.out.println(task);
		System.out.println(winner);
		System.out.println(message);
		
		// Retrieve beans from JSONs
		Task t = JSONHandler.getTask(task);
		Winner w = JSONHandler.getWinnerMessage(winner);
		Message m = JSONHandler.getMessage(message);
		t = JSONHandler.getTask(m.getContent());
		
		System.out.println("Task: ");
		System.out.println(t.toString());
		System.out.println("Message: ");
		System.out.println(m.toString());
		System.out.println("Winner: ");
		System.out.println(w.toString());
	}
}
