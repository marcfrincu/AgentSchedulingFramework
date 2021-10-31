package simulator;

import java.sql.ResultSet;
import java.util.Vector;

import services.utils.DbSimulator;

/**
 * Class responsible for setting the failure time of modules
 * @author Marc Frincu
 * @since 2010
 *
 */
public final class Failures {

	/**
	 * This class fails all the executors part of a scheduler and randomly either
	 * the scheduler or the negotiator attached to the platform.
	 * It does this by setting the failure time of the modules in the database.
	 * Every module periodically queries the database and if the time has been 
	 * reached it produces a System.exit(0) call.
	 * Only running modules are assumed to be capable of failing
	 * @param percentToFail the percent of the total modules scheduled for failure 
	 * @throws Exception 
	 */
	public static void failModules(float percentToFail) throws Exception {
		
		if (percentToFail > 100 || percentToFail < 0) {
			System.out.println("Invalid argument. Should be between 0 and 100");
			return;
		}
		
		int time = 50000;
		
		DbSimulator db = DbSimulator.getDb();
		
		db.executeStatement("DELETE FROM simulator;");
		
		// Select all agent IDs
		ResultSet rs = db.getQuery("SELECT uuid FROM agent WHERE " +
				" available=true;");
		int noModules = Integer.parseInt(db.getFirst("SELECT count(*) FROM agent_module WHERE " +
				" status='RUNNING' AND NOT type='HEALING';"));

		Vector<String> agentUuids = new Vector<String>();
		while (rs.next()) {
			if (! agentUuids.contains(rs.getString(1)))
				agentUuids.add(rs.getString(1));
			
		}
		
		String wfId = null;
		ResultSet rs2 = null;
		int picked = 0;
		int i = 0;
		String uuid = null;
		//while (picked < noModules * percentToFail / 100) {
			System.out.println(picked + " " + (noModules * percentToFail / 100) );
			i = 0;
			while (i<agentUuids.size()) {
				// randomly pick agent modules
				//if (Math.random() < 0.5) {
					uuid = agentUuids.remove(i);
					rs2 = db.getQuery("SELECT workflow_id FROM agent_module WHERE " +
							"type='EXECUTOR' AND status='RUNNING' AND agent_uuid='" + uuid + "';");
					// set all their executors to fail
					while (rs2.next()) {
						//if (picked < noModules * percentToFail / 100) {
							db.executeStatement("INSERT INTO simulator (agent_uuid, workflow_id, type, failtime)" +
								" VALUES ("+
								"\'" + uuid + "\'," +
								"\'" + rs2.getString(1) + "\'," +
								"\'EXECUTOR\'," +
								(System.currentTimeMillis() + time) 
								+");");
							picked++;
						//}
					}
					
					// randomly pick whether their scheduler should also fail 
					//if (Math.random() < 0.5 && picked <= noModules * percentToFail / 100) {
						wfId = db.getFirst("SELECT workflow_id FROM agent_module WHERE " +
							"type='SCHEDULING' AND status='RUNNING' AND agent_uuid='" + uuid + "';");
						if (wfId != null) {
							db.executeStatement("INSERT INTO simulator (agent_uuid, workflow_id, type, failtime)" +
								" VALUES ("+
								"\'" + uuid + "\'," +
								"\'" + wfId + "\'," +
								"\'SCHEDULING\'," +
								(System.currentTimeMillis() + time) 
								+");");
							picked++;
						}
					//}
	
					// randomly pick whether the global negotiator should also fail
					//if (Math.random() < 0.5 && picked <= noModules * percentToFail / 100) {
						wfId = db.getFirst("SELECT workflow_id FROM agent_module WHERE " +
						"type='NEGOTIATION' AND status='RUNNING' AND agent_uuid='" + uuid + "';");
						if (wfId != null) {
							db.executeStatement("INSERT INTO simulator (agent_uuid, workflow_id, type, failtime)" +
								" VALUES ("+
								"\'" + uuid + "\'," +
								"\'" + wfId + "\'," +
								"\'NEGOTIATION\'," +
								(System.currentTimeMillis() + time) 
								+");");
							picked++;
						}
					//}
				//}
				//else {
//					i++;
//				}
			}
		//}
	}
	
	/**
	 * This class fails randomly healing modules.
	 * It does this by setting the failure time of the modules in the database.
	 * Every module periodically queries the database and if the time has been 
	 * reached it produces a System.exit(0) call.
	 * Only running modules are assumed to be capable of failing
	 * @param percentToFail the percent of the total modules scheduled for failure
	 * @throws Exception 
	 */
	public static void failHealers(float percentToFail) throws Exception {
		DbSimulator db = DbSimulator.getDb();
		
		if (percentToFail > 100 || percentToFail < 0) {
			System.out.println("Invalid argument. Should be between 0 and 100");
			return;
		}
		
		int time = 5000;
		
		//db.executeStatement("DELETE FROM simulator;");
		
		// Select all agent Ids
		ResultSet rs = db.getQuery("SELECT agent_uuid FROM agent_module WHERE " +
				" type='HEALING' AND status='RUNNING';");
		int noModules = Integer.parseInt(db.getFirst("SELECT count(*) FROM agent_module WHERE " +
				" type='HEALING' AND status='RUNNING';"));
		System.out.println(noModules);
		Vector<String> agentUuids = new Vector<String>();
		while (rs.next()) {
			if (! agentUuids.contains(rs.getString(1)))
				agentUuids.add(rs.getString(1));
			
		}
		
		int picked = 0, i = 0;
		String uuid = null;
		while (picked < noModules * percentToFail / 100) {
			i = 0;
			while (i<agentUuids.size()) {
				// randomly pick healing modules that will fail
				if (Math.random() < 0.5) {
					uuid = agentUuids.remove(i);
					if (picked < noModules * percentToFail / 100) {
						db.executeStatement("INSERT INTO simulator (agent_uuid, workflow_id, type, failtime)" +
							" VALUES ("+
							"\'" + uuid + "\'," +
							"\'-1\'," +
							"\'HEALING\'," +
							(System.currentTimeMillis() + time) 
							+");");
						picked++;
					}
				}
				else {
					i++;
				}
			}
		}
	}
	
}
